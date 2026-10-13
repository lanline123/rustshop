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

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

/**
 * 通用可配置 HTTP 收单适配器（自解密模式）。
 *
 * <p>向配置的收单网关 POST JSON（HMAC-SHA256 签名），解析 {@code {code, transactionId, tradeState, message}}。
 * 通用契约，选定具体机构后可直接用或作为新适配器模板。</p>
 *
 * @author Tigshop团队
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GenericAcquirerProvider implements AcquirerProvider {

    private final MessageHelper messageHelper;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    @Override
    public String providerCode() {
        return "generic";
    }

    @Override
    public boolean requiresDecryption() {
        return true;
    }

    @Override
    public String submit(ApplePayAcquirerContext ctx) {
        ApplePayAcquirerConfig config = ctx.getConfig();
        requireGateway(config);
        String amountStr = ctx.getAmount() == null ? "0" : ctx.getAmount().toPlainString();
        long ts = System.currentTimeMillis() / 1000;

        JSONObject body = new JSONObject();
        body.put("merchantNo", config.getMerchantNo());
        body.put("outTradeNo", ctx.getPaySn());
        body.put("amount", amountStr);
        body.put("currency", config.getCurrency());
        // 解密后的卡凭据（Base64 承载，禁止日志打印 BR-07）
        String decrypted = ctx.getDecryptedPaymentData();
        body.put("paymentData", Base64.getEncoder().encodeToString(
                decrypted == null ? new byte[0] : decrypted.getBytes(StandardCharsets.UTF_8)));
        body.put("timestamp", ts);
        body.put("sign", sign(config.getKey(),
                config.getMerchantNo() + ctx.getPaySn() + amountStr + config.getCurrency() + ts));

        JSONObject resp = post(config.getGateway() + "/pay", body.toJSONString());
        if (resp == null || !"SUCCESS".equalsIgnoreCase(resp.getString("code"))) {
            String reason = resp == null ? null : resp.getString("message");
            log.warn("Apple Pay[generic] 收单扣款失败，paySn={}, reason={}", ctx.getPaySn(), reason);
            throw new GlobalException(StrUtil.isNotBlank(reason) ? reason
                    : messageHelper.get("error.applepay.confirm.fail"));
        }
        return resp.getString("transactionId");
    }

    @Override
    public boolean refund(String transactionId, String refundSn, BigDecimal amount, ApplePayAcquirerConfig config) {
        requireGateway(config);
        String amountStr = amount == null ? "0" : amount.toPlainString();
        long ts = System.currentTimeMillis() / 1000;

        JSONObject body = new JSONObject();
        body.put("merchantNo", config.getMerchantNo());
        body.put("origTransactionId", transactionId);
        body.put("outRefundNo", refundSn);
        body.put("amount", amountStr);
        body.put("currency", config.getCurrency());
        body.put("timestamp", ts);
        body.put("sign", sign(config.getKey(),
                config.getMerchantNo() + transactionId + refundSn + amountStr + ts));

        JSONObject resp = post(config.getGateway() + "/refund", body.toJSONString());
        boolean ok = resp != null && "SUCCESS".equalsIgnoreCase(resp.getString("code"));
        if (!ok) {
            log.warn("Apple Pay[generic] 收单退款失败，refundSn={}, reason={}", refundSn,
                    resp == null ? null : resp.getString("message"));
        }
        return ok;
    }

    @Override
    public String query(String transactionId, ApplePayAcquirerConfig config) {
        if (config == null || StrUtil.isBlank(config.getGateway()) || StrUtil.isBlank(transactionId)) {
            return null;
        }
        long ts = System.currentTimeMillis() / 1000;
        JSONObject body = new JSONObject();
        body.put("merchantNo", config.getMerchantNo());
        body.put("transactionId", transactionId);
        body.put("timestamp", ts);
        body.put("sign", sign(config.getKey(), config.getMerchantNo() + transactionId + ts));

        JSONObject resp = post(config.getGateway() + "/query", body.toJSONString());
        return resp == null ? null : resp.getString("tradeState");
    }

    private void requireGateway(ApplePayAcquirerConfig config) {
        if (config == null || StrUtil.isBlank(config.getGateway()) || StrUtil.isBlank(config.getMerchantNo())
                || StrUtil.isBlank(config.getKey())) {
            throw new GlobalException(messageHelper.get("error.applepay.config.incomplete"));
        }
    }

    private JSONObject post(String url, String jsonBody) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(8))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200 || StrUtil.isBlank(response.body())) {
                log.warn("Apple Pay[generic] 收单网关响应异常，url={}, status={}", url, response.statusCode());
                return null;
            }
            return JSONObject.parseObject(response.body());
        } catch (Exception e) {
            log.error("Apple Pay[generic] 收单网关请求异常，url={}, err={}", url, e.getClass().getSimpleName());
            return null;
        }
    }

    private String sign(String key, String content) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] bytes = mac.doFinal(content.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new GlobalException(messageHelper.get("error.applepay.confirm.fail"));
        }
    }
}
