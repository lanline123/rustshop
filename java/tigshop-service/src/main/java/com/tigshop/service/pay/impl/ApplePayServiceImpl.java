// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.pay.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tigshop.bean.bo.PayPlatformStateBO;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.model.finance.Paylog;
import com.tigshop.bean.vo.order.OrderVO;
import com.tigshop.bean.vo.order.pay.ApplePayConfirmVO;
import com.tigshop.bean.vo.order.pay.PayCreateVO;
import com.tigshop.bean.vo.order.pay.PayNotifyVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.i18n.MessageHelper;
import com.tigshop.service.finance.PaylogService;
import com.tigshop.service.pay.ApplePayService;
import com.tigshop.service.pay.applepay.AcquirerClient;
import com.tigshop.service.pay.applepay.AppleMerchantValidator;
import com.tigshop.service.pay.applepay.ApplePayAcquirerConfig;
import com.tigshop.service.pay.applepay.ApplePayAcquirerContext;
import com.tigshop.service.pay.applepay.ApplePayTokenDecryptor;
import com.tigshop.service.setting.ConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Apple Pay 苹果支付服务实现。
 *
 * <p>渠道 code 全链路统一 {@code applepay}。支付分两步：create 组装支付面板参数（本类 pay），
 * 前端拉起面板 → validateMerchant 商户校验 → confirm 解密+收单扣款+paySuccess。</p>
 *
 * <p>金额、币种一律以服务端为准（BR-04）；confirm 幂等（BR-05）；卡凭据禁落库/日志（BR-07）。</p>
 *
 * @author Tigshop团队
 */
@Service("ApplePayService")
@Slf4j
public class ApplePayServiceImpl extends PayBaseServiceImpl implements ApplePayService {

    /** confirm 分布式锁前缀（防同一 paySn 并发重复扣款） */
    private static final String CONFIRM_LOCK_PREFIX = "applepay:confirm:";
    private static final long CONFIRM_LOCK_SECONDS = 30L;

    private final ConfigService configService;
    private final PaylogService paylogService;
    private final AppleMerchantValidator merchantValidator;
    private final ApplePayTokenDecryptor tokenDecryptor;
    private final AcquirerClient acquirerClient;
    private final MessageHelper messageHelper;
    private final com.tigshop.common.utils.RedisCache redisCache;

    public ApplePayServiceImpl(ConfigService configService, PaylogService paylogService,
                               AppleMerchantValidator merchantValidator,
                               ApplePayTokenDecryptor tokenDecryptor,
                               AcquirerClient acquirerClient,
                               MessageHelper messageHelper,
                               com.tigshop.common.utils.RedisCache redisCache) {
        this.configService = configService;
        this.paylogService = paylogService;
        this.merchantValidator = merchantValidator;
        this.tokenDecryptor = tokenDecryptor;
        this.acquirerClient = acquirerClient;
        this.messageHelper = messageHelper;
        this.redisCache = redisCache;
    }

    @Override
    public PayCreateVO.PayInfo pay(OrderVO orderVO, String openid, String clientType) {
        String displayName = configService.getConfigVal(SettingsEnum.APPLE_PAY_DISPLAY_NAME);
        String countryCode = configService.getConfigVal(SettingsEnum.APPLE_PAY_COUNTRY_CODE);
        String currencyCode = configService.getConfigVal(SettingsEnum.APPLE_PAY_CURRENCY);
        List<String> supportedNetworks = parseNetworks(configService.getConfigVal(SettingsEnum.APPLE_PAY_SUPPORTED_NETWORKS));

        PayCreateVO.ApplePayParams applePay = new PayCreateVO.ApplePayParams();
        applePay.setPaySn(orderVO.getPaySn());
        applePay.setCountryCode(StrUtil.blankToDefault(countryCode, "CN"));
        applePay.setCurrencyCode(StrUtil.blankToDefault(currencyCode, "CNY"));
        applePay.setMerchantDisplayName(displayName);
        applePay.setSupportedNetworks(supportedNetworks);
        applePay.setMerchantCapabilities(buildMerchantCapabilities(supportedNetworks));

        PayCreateVO.ApplePayParams.Total total = new PayCreateVO.ApplePayParams.Total();
        total.setLabel(displayName);
        total.setAmount(formatAmount(orderVO.getUnpaidAmount()));
        applePay.setTotal(total);

        PayCreateVO.PayInfo payInfo = new PayCreateVO.PayInfo();
        payInfo.setApplePay(applePay);
        return payInfo;
    }

    @Override
    public String validateMerchant(String paySn, String validationUrl, String displayDomain) {
        Paylog paylog = getPaylog(paySn);
        if (paylog == null) {
            throw new GlobalException(messageHelper.get("error.applepay.paylog.not.found"));
        }
        if (Integer.valueOf(1).equals(paylog.getPayStatus())) {
            throw new GlobalException(messageHelper.get("error.applepay.order.paid"));
        }
        return merchantValidator.validateMerchant(validationUrl, displayDomain);
    }

    @Override
    public ApplePayConfirmVO confirm(String paySn, String paymentTokenJson) {
        Paylog paylog = getPaylog(paySn);
        if (paylog == null) {
            return ApplePayConfirmVO.fail(messageHelper.get("error.applepay.paylog.not.found"));
        }
        // 幂等（BR-05）：已支付直接返回成功，不重复扣款
        if (checkOrderPayStatus(paySn)) {
            return ApplePayConfirmVO.success(paylog.getTransactionId());
        }
        // 并发防重（#2）：同一 paySn 加分布式锁，未抢到锁说明正在处理，拒绝重复提交
        String lockKey = CONFIRM_LOCK_PREFIX + paySn;
        String lockVal = java.util.UUID.randomUUID().toString();
        if (!redisCache.tryLock(lockKey, lockVal, CONFIRM_LOCK_SECONDS, java.util.concurrent.TimeUnit.SECONDS)) {
            return ApplePayConfirmVO.fail(messageHelper.get("error.applepay.processing"));
        }
        try {
            // 双检：拿到锁后再确认一次未支付
            if (checkOrderPayStatus(paySn)) {
                return ApplePayConfirmVO.success(getPaylog(paySn).getTransactionId());
            }
            // 银行收单模式需自解密卡凭据（仅内存，禁落库/日志 BR-07）；PSP 模式透传原始 token 由其解密
            String decrypted = acquirerClient.requiresDecryption() ? tokenDecryptor.decrypt(paymentTokenJson) : null;
            // 提交收单扣款，金额以服务端 paylog 为准（BR-04）
            ApplePayAcquirerContext ctx = new ApplePayAcquirerContext(
                    paySn, paylog.getPayAmount(), paymentTokenJson, decrypted, buildAcquirerConfig());
            String transactionId = acquirerClient.submit(ctx);
            if (StrUtil.isBlank(transactionId)) {
                return ApplePayConfirmVO.fail(messageHelper.get("error.applepay.confirm.fail"));
            }
            // 异步结算（如银联商务）：submit 仅受理，不在此置已支付，等异步通知入账（#1）
            if (acquirerClient.isAsyncSettlement()) {
                return ApplePayConfirmVO.processing(transactionId, messageHelper.get("error.applepay.pending"));
            }
            paySuccess(paySn, transactionId);
            return ApplePayConfirmVO.success(transactionId);
        } catch (GlobalException e) {
            log.warn("Apple Pay confirm 失败，paySn={}, reason={}", paySn, e.getMessage());
            return ApplePayConfirmVO.fail(e.getMessage());
        } catch (Exception e) {
            log.error("Apple Pay confirm 异常，paySn={}", paySn, e);
            return ApplePayConfirmVO.fail(messageHelper.get("error.applepay.confirm.fail"));
        } finally {
            redisCache.unlock(lockKey, lockVal);
        }
    }

    @Override
    public ApplePayConfirmVO confirmByPaymentMethod(String paySn, String paymentMethodId) {
        Paylog paylog = getPaylog(paySn);
        if (paylog == null) {
            return ApplePayConfirmVO.fail(messageHelper.get("error.applepay.paylog.not.found"));
        }
        // 幂等（BR-05）
        if (checkOrderPayStatus(paySn)) {
            return ApplePayConfirmVO.success(paylog.getTransactionId());
        }
        if (!acquirerClient.supportsPaymentMethodConfirm()) {
            // 当前收单机构不是 PSP 前端驱动模式，应走 confirm(原始token)
            return ApplePayConfirmVO.fail(messageHelper.get("error.applepay.channel.not.ready"));
        }
        // 并发防重（#2）
        String lockKey = CONFIRM_LOCK_PREFIX + paySn;
        String lockVal = java.util.UUID.randomUUID().toString();
        if (!redisCache.tryLock(lockKey, lockVal, CONFIRM_LOCK_SECONDS, java.util.concurrent.TimeUnit.SECONDS)) {
            return ApplePayConfirmVO.fail(messageHelper.get("error.applepay.processing"));
        }
        try {
            if (checkOrderPayStatus(paySn)) {
                return ApplePayConfirmVO.success(getPaylog(paySn).getTransactionId());
            }
            String transactionId = acquirerClient.submitByPaymentMethod(
                    paySn, paylog.getPayAmount(), paymentMethodId, buildAcquirerConfig());
            if (StrUtil.isBlank(transactionId)) {
                return ApplePayConfirmVO.fail(messageHelper.get("error.applepay.confirm.fail"));
            }
            paySuccess(paySn, transactionId);
            return ApplePayConfirmVO.success(transactionId);
        } catch (GlobalException e) {
            log.warn("Apple Pay confirmByPaymentMethod 失败，paySn={}, reason={}", paySn, e.getMessage());
            return ApplePayConfirmVO.fail(e.getMessage());
        } catch (Exception e) {
            log.error("Apple Pay confirmByPaymentMethod 异常，paySn={}", paySn, e);
            return ApplePayConfirmVO.fail(messageHelper.get("error.applepay.confirm.fail"));
        } finally {
            redisCache.unlock(lockKey, lockVal);
        }
    }

    @Override
    public PayNotifyVO payRefund(String outTradeNo, String refundNo, BigDecimal refundAmount,
                                 BigDecimal totalAmount, String clientType, String subMchId) {
        Paylog paylog = getPaylog(outTradeNo);
        if (paylog == null || StrUtil.isBlank(paylog.getTransactionId())) {
            return new PayNotifyVO("fail", "找不到原支付记录");
        }
        try {
            boolean ok = acquirerClient.refund(paylog.getTransactionId(), refundNo, refundAmount, buildAcquirerConfig());
            return ok ? new PayNotifyVO("SUCCESS", "退款成功") : new PayNotifyVO("fail", "退款失败");
        } catch (Exception e) {
            log.error("Apple Pay 退款异常，refundNo={}", refundNo, e);
            return new PayNotifyVO("fail", "退款异常：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public String payNotify(Map<String, Object> map) {
        // 收单异步通知（如银联商务支付结果通知）：验签 + 解析 → 支付成功则 paySuccess
        com.tigshop.service.pay.applepay.AcquirerNotifyResult result =
                acquirerClient.parseNotify(map, buildAcquirerConfig());
        if (result == null || !result.isVerified() || !result.isPaid() || StrUtil.isBlank(result.getPaySn())) {
            return "FAIL";
        }
        Paylog paylog = getPaylog(result.getPaySn());
        if (paylog == null) {
            return "FAIL";
        }
        // 金额交叉校验（#4）：通知金额须与本地 paylog 一致，防伪造/串改金额
        if (result.getAmount() != null && paylog.getPayAmount() != null
                && result.getAmount().compareTo(paylog.getPayAmount()) != 0) {
            log.warn("Apple Pay 通知金额与本地不一致，paySn={}, notify={}, local={}",
                    result.getPaySn(), result.getAmount(), paylog.getPayAmount());
            return "FAIL";
        }
        if (!checkOrderPayStatus(result.getPaySn())) {
            paySuccess(result.getPaySn(), result.getTransactionId());
        }
        return StrUtil.blankToDefault(result.getAckResponse(), "SUCCESS");
    }

    @Override
    public String refundNotify(Map<String, Object> map) {
        // 一期走同步退款，异步回执预留
        log.info("Apple Pay refundNotify 预留通道被调用");
        return "FAIL";
    }

    @Override
    public PayPlatformStateBO payPlatformStatus(String outTradeNo, String tradeNo, String clientType) {
        if (StrUtil.isBlank(tradeNo)) {
            return null;
        }
        String state = acquirerClient.query(tradeNo, buildAcquirerConfig());
        if (state == null) {
            return null;
        }
        PayPlatformStateBO bo = new PayPlatformStateBO();
        bo.setOutTradeNo(outTradeNo);
        bo.setTradeNo(tradeNo);
        bo.setTradeState(state);
        return bo;
    }

    private Paylog getPaylog(String paySn) {
        return paylogService.getOne(new LambdaQueryWrapper<Paylog>().eq(Paylog::getPaySn, paySn));
    }

    private ApplePayAcquirerConfig buildAcquirerConfig() {
        return new ApplePayAcquirerConfig(
                configService.getConfigVal(SettingsEnum.APPLE_PAY_ACQUIRER_GATEWAY),
                configService.getConfigVal(SettingsEnum.APPLE_PAY_ACQUIRER_MERCHANT_NO),
                configService.getConfigVal(SettingsEnum.APPLE_PAY_ACQUIRER_KEY),
                configService.getConfigVal(SettingsEnum.APPLE_PAY_CURRENCY)
        );
    }

    private List<String> parseNetworks(String raw) {
        List<String> networks = new ArrayList<>();
        if (StrUtil.isNotBlank(raw)) {
            for (String s : Arrays.asList(raw.split(","))) {
                String t = s.trim();
                if (!t.isEmpty()) {
                    networks.add(t);
                }
            }
        }
        return networks;
    }

    /**
     * 商户能力：默认支持 3DS；勾选银联时追加 supportsEMV（BR：由后端按卡组织自动生成）。
     */
    private List<String> buildMerchantCapabilities(List<String> supportedNetworks) {
        List<String> capabilities = new ArrayList<>();
        capabilities.add("supports3DS");
        if (supportedNetworks != null && supportedNetworks.contains("chinaUnionPay")) {
            capabilities.add("supportsEMV");
        }
        return capabilities;
    }

    private String formatAmount(BigDecimal amount) {
        if (amount == null) {
            amount = BigDecimal.ZERO;
        }
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
