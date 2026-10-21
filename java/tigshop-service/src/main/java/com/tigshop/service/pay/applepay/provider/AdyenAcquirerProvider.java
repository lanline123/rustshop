package com.tigshop.service.pay.applepay.provider;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.i18n.MessageHelper;
import com.tigshop.service.pay.applepay.AcquirerProvider;
import com.tigshop.service.pay.applepay.ApplePayAcquirerConfig;
import com.tigshop.service.pay.applepay.ApplePayAcquirerContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

/**
 * Adyen 收单适配器 —— 跨境大商户/多币种，PSP 代解密模式（原始 token 提交）。
 *
 * <p>集成模式：不自解密。把 Apple Pay 的 {@code token.paymentData} base64 后作为
 * {@code paymentMethod.applePayToken} 提交 Adyen Checkout {@code /payments}，由 Adyen 解密扣款。</p>
 *
 * <p>配置映射（复用通用收单配置）：</p>
 * <ul>
 *   <li>{@code applePayAcquirerGateway} → Adyen Checkout 基地址（如 https://checkout-test.adyen.com/v71）</li>
 *   <li>{@code applePayAcquirerMerchantNo} → merchantAccount</li>
 *   <li>{@code applePayAcquirerKey} → X-API-Key</li>
 *   <li>{@code applePayCurrency} → 结算币种</li>
 * </ul>
 *
 * <p>⚠️ 金额最小单位、结果码、退款端点已按 Adyen Checkout 公开文档实现，具体版本号(v71)与账号环境需按你们 Adyen 账号核对。</p>
 *
 * @author Tigshop团队
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdyenAcquirerProvider implements AcquirerProvider {

    private final MessageHelper messageHelper;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    @Override
    public String providerCode() {
        return "adyen";
    }

    @Override
    public boolean requiresDecryption() {
        return false;
    }

    @Override
    public String submit(ApplePayAcquirerContext ctx) {
        ApplePayAcquirerConfig config = ctx.getConfig();
        requireConfig(config);

        String applePayToken = buildApplePayToken(ctx.getRawTokenJson());
        JSONObject amount = new JSONObject();
        amount.put("currency", (config.getCurrency() == null ? "USD" : config.getCurrency()).toUpperCase());
        amount.put("value", toMinorUnit(ctx.getAmount(), config.getCurrency()));

        JSONObject paymentMethod = new JSONObject();
        paymentMethod.put("type", "applepay");
        paymentMethod.put("applePayToken", applePayToken);

        JSONObject body = new JSONObject();
        body.put("merchantAccount", config.getMerchantNo());
        body.put("amount", amount);
        body.put("reference", ctx.getPaySn());
        body.put("paymentMethod", paymentMethod);

        JSONObject resp = post(config.getGateway() + "/payments", config.getKey(), body.toJSONString());
        if (resp == null || !"Authorised".equalsIgnoreCase(resp.getString("resultCode"))) {
            String reason = resp == null ? null : resp.getString("refusalReason");
            log.warn("Apple Pay[adyen] 扣款失败，paySn={}, resultCode={}", ctx.getPaySn(),
                    resp == null ? null : resp.getString("resultCode"));
            throw new GlobalException(StrUtil.isNotBlank(reason) ? reason
                    : messageHelper.get("error.applepay.confirm.fail"));
        }
        return resp.getString("pspReference");
    }

    @Override
    public boolean refund(String transactionId, String refundSn, BigDecimal amount, ApplePayAcquirerConfig config) {
        requireConfig(config);
        JSONObject amt = new JSONObject();
        amt.put("currency", (config.getCurrency() == null ? "USD" : config.getCurrency()).toUpperCase());
        amt.put("value", toMinorUnit(amount, config.getCurrency()));

        JSONObject body = new JSONObject();
        body.put("merchantAccount", config.getMerchantNo());
        body.put("amount", amt);
        body.put("reference", refundSn);

        // Adyen 退款端点：/payments/{pspReference}/refunds
        JSONObject resp = post(config.getGateway() + "/payments/" + transactionId + "/refunds",
                config.getKey(), body.toJSONString());
        String status = resp == null ? null : resp.getString("status");
        boolean ok = resp != null && ("received".equalsIgnoreCase(status) || StrUtil.isNotBlank(resp.getString("pspReference")));
        if (!ok) {
            log.warn("Apple Pay[adyen] 退款失败，refundSn={}, status={}", refundSn, status);
        }
        return ok;
    }

    @Override
    public String query(String transactionId, ApplePayAcquirerConfig config) {
        // Adyen Checkout 无按 pspReference 的简单同步查询接口，交易状态以异步 webhook(notification) 为准
        return null;
    }

    private void requireConfig(ApplePayAcquirerConfig config) {
        if (config == null || StrUtil.isBlank(config.getGateway()) || StrUtil.isBlank(config.getMerchantNo())
                || StrUtil.isBlank(config.getKey())) {
            throw new GlobalException(messageHelper.get("error.applepay.config.incomplete"));
        }
    }

    /**
     * applePayToken = base64( token.paymentData 的 JSON 串 )；无 paymentData 时退化为 base64(原始 token)。
     */
    private String buildApplePayToken(String rawTokenJson) {
        String payload = rawTokenJson;
        try {
            JSONObject root = JSONObject.parseObject(rawTokenJson);
            JSONObject paymentData = root.getJSONObject("paymentData");
            if (paymentData != null) {
                payload = paymentData.toJSONString();
            }
        } catch (Exception ignore) {
            // 解析失败则直接 base64 原始串
        }
        return Base64.getEncoder().encodeToString(
                (payload == null ? "" : payload).getBytes(StandardCharsets.UTF_8));
    }

    private JSONObject post(String url, String apiKey, String jsonBody) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(8))
                    .header("Content-Type", "application/json")
                    .header("X-API-Key", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2 || StrUtil.isBlank(response.body())) {
                log.warn("Apple Pay[adyen] 网关响应异常，url={}, status={}", url, response.statusCode());
                return null;
            }
            return JSONObject.parseObject(response.body());
        } catch (Exception e) {
            log.error("Apple Pay[adyen] 网关请求异常，url={}, err={}", url, e.getClass().getSimpleName());
            return null;
        }
    }

    private long toMinorUnit(BigDecimal amount, String currency) {
        if (amount == null) {
            return 0L;
        }
        int multiplier = isZeroDecimalCurrency(currency) ? 1 : 100;
        return amount.multiply(BigDecimal.valueOf(multiplier)).setScale(0, RoundingMode.HALF_UP).longValue();
    }

    private boolean isZeroDecimalCurrency(String currency) {
        if (currency == null) {
            return false;
        }
        String[] zero = {"JPY", "KRW", "VND", "BIF", "CLP", "DJF", "GNF", "KMF", "MGA",
                "PYG", "RWF", "UGX", "VUV", "XAF", "XOF", "XPF"};
        for (String c : zero) {
            if (c.equalsIgnoreCase(currency)) {
                return true;
            }
        }
        return false;
    }
}
