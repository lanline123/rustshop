package com.tigshop.service.pay.applepay.provider;

import cn.hutool.core.util.StrUtil;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.i18n.MessageHelper;
import com.tigshop.service.pay.applepay.AcquirerProvider;
import com.tigshop.service.pay.applepay.ApplePayAcquirerConfig;
import com.tigshop.service.pay.applepay.ApplePayAcquirerContext;
import com.tigshop.service.setting.ConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Stripe 收单适配器 —— 跨境主力，PSP 前端驱动模式。
 *
 * <p>Apple Pay 由前端 Stripe.js/Payment Element 完成（Stripe 自行做商户校验+解密），返回 paymentMethodId；
 * 后端用 {@link #submitByPaymentMethod} 创建并确认 PaymentIntent 扣款，PaymentIntent id 即 transactionId。
 * 因此本适配器<b>不需要自解密</b>，也不使用我们自建的 validateMerchant/confirm(原始token)。</p>
 *
 * <p>鉴权复用现有 Stripe Secret Key 配置（{@link SettingsEnum#STRIPE_SECRET_KEY}）；币种用 Apple Pay 结算币种。</p>
 *
 * @author Tigshop团队
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StripeAcquirerProvider implements AcquirerProvider {

    private final ConfigService configService;
    private final MessageHelper messageHelper;

    @Override
    public String providerCode() {
        return "stripe";
    }

    @Override
    public boolean requiresDecryption() {
        return false;
    }

    @Override
    public boolean supportsPaymentMethodConfirm() {
        return true;
    }

    private void initApiKey() {
        String secretKey = configService.getConfigVal(SettingsEnum.STRIPE_SECRET_KEY);
        if (StrUtil.isBlank(secretKey)) {
            throw new GlobalException(messageHelper.get("error.applepay.config.incomplete"));
        }
        Stripe.apiKey = secretKey;
    }

    private String currency() {
        String c = configService.getConfigVal(SettingsEnum.APPLE_PAY_CURRENCY);
        return StrUtil.isBlank(c) ? "usd" : c.toLowerCase();
    }

    @Override
    public String submitByPaymentMethod(String paySn, BigDecimal amount, String paymentMethodId,
                                        ApplePayAcquirerConfig config) {
        if (StrUtil.isBlank(paymentMethodId)) {
            throw new GlobalException(messageHelper.get("error.applepay.confirm.fail"));
        }
        initApiKey();
        String currency = currency();
        long minor = toMinorUnit(amount, currency);
        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(minor)
                    .setCurrency(currency)
                    .setPaymentMethod(paymentMethodId)
                    .setConfirm(true)
                    .putMetadata("pay_sn", paySn)
                    // Apple Pay 卡支付无跳转，禁止重定向类支付方式
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .setAllowRedirects(
                                            PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER)
                                    .build())
                    .build();
            PaymentIntent intent = PaymentIntent.create(params);
            if ("succeeded".equals(intent.getStatus())) {
                return intent.getId();
            }
            log.warn("Apple Pay[stripe] PaymentIntent 状态非 succeeded：{}, paySn={}", intent.getStatus(), paySn);
            throw new GlobalException(messageHelper.get("error.applepay.confirm.fail"));
        } catch (StripeException e) {
            log.error("Apple Pay[stripe] 扣款异常，paySn={}, err={}", paySn, e.getCode());
            throw new GlobalException(messageHelper.get("error.applepay.confirm.fail"));
        }
    }

    @Override
    public String submit(ApplePayAcquirerContext ctx) {
        // Stripe 走前端驱动的 submitByPaymentMethod，不支持原始 token 直提
        throw new GlobalException(messageHelper.get("error.applepay.channel.not.ready"));
    }

    @Override
    public boolean refund(String transactionId, String refundSn, BigDecimal amount, ApplePayAcquirerConfig config) {
        if (StrUtil.isBlank(transactionId)) {
            return false;
        }
        initApiKey();
        try {
            RefundCreateParams params = RefundCreateParams.builder()
                    .setPaymentIntent(transactionId)
                    .setAmount(toMinorUnit(amount, currency()))
                    .putMetadata("refund_sn", refundSn)
                    .build();
            Refund refund = Refund.create(params);
            return "succeeded".equals(refund.getStatus());
        } catch (StripeException e) {
            log.error("Apple Pay[stripe] 退款异常，refundSn={}, err={}", refundSn, e.getCode());
            return false;
        }
    }

    @Override
    public String query(String transactionId, ApplePayAcquirerConfig config) {
        if (StrUtil.isBlank(transactionId)) {
            return null;
        }
        initApiKey();
        try {
            return PaymentIntent.retrieve(transactionId).getStatus();
        } catch (StripeException e) {
            log.error("Apple Pay[stripe] 查询异常，transactionId={}, err={}", transactionId, e.getCode());
            return null;
        }
    }

    /**
     * 金额转最小货币单位（零小数币种不 *100）。
     */
    private long toMinorUnit(BigDecimal amount, String currency) {
        if (amount == null) {
            return 0L;
        }
        int multiplier = isZeroDecimalCurrency(currency) ? 1 : 100;
        return amount.multiply(BigDecimal.valueOf(multiplier)).setScale(0, RoundingMode.HALF_UP).longValue();
    }

    private boolean isZeroDecimalCurrency(String currency) {
        String[] zero = {"bif", "clp", "djf", "gnf", "jpy", "kmf", "krw", "mga", "pyg",
                "rwf", "ugx", "vnd", "vuv", "xaf", "xof", "xpf"};
        for (String c : zero) {
            if (c.equalsIgnoreCase(currency)) {
                return true;
            }
        }
        return false;
    }
}
