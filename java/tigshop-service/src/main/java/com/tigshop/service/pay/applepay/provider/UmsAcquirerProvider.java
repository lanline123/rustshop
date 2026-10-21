package com.tigshop.service.pay.applepay.provider;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.i18n.MessageHelper;
import com.tigshop.service.pay.applepay.AcquirerNotifyResult;
import com.tigshop.service.pay.applepay.AcquirerProvider;
import com.tigshop.service.pay.applepay.ApplePayAcquirerConfig;
import com.tigshop.service.pay.applepay.ApplePayAcquirerContext;
import com.tigshop.service.setting.ConfigService;
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
import java.security.MessageDigest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

/**
 * 银联商务（ChinaUMS）收单适配器 —— 境内，统一下单 + 异步通知模型。
 *
 * <p><b>模型说明</b>：银联商务 Apple Pay 是 {@code applepay.order} 下单接口
 * （{@code {gateway}/v1/netpay/applepay/order}），后端下单后由<b>客户端银联 SDK 唤起 Apple Pay</b>，
 * 最终支付结果以<b>异步通知</b>为准；本质偏 APP，境内无 "web 商户自解密直提" 通道。
 * 故 {@link #requiresDecryption()} = false，{@link #submit} 为下单/受理，最终成功由
 * {@link #parseNotify} 驱动 {@code paySuccess}。</p>
 *
 * <p><b>配置映射</b>：</p>
 * <ul>
 *   <li>{@code applePayAcquirerGateway} → 银商网关基地址（生产 https://api-mop.chinaums.com，测试 https://test-api-open.chinaums.com）</li>
 *   <li>{@code applePayAcquirerMerchantNo} → "mid" 或 "mid|tid"（商户号|终端号）</li>
 *   <li>{@code applePayAcquirerKey} → 银联商务分配的签名密钥</li>
 *   <li>{@code applePayUmsSignType} → 签名算法 SHA256(默认)/SM3</li>
 * </ul>
 *
 * <p>签名：非空非 sign 参数按 key 字典序拼 {@code k=v&...}，末尾拼签名密钥，按 signType 做
 * SHA256 或 SM3 摘要后转大写。请求用配置 signType；通知按报文携带的 signType 自动验签。
 * SM3 依赖 BouncyCastle（未引入时给出明确提示，不强加依赖）。</p>
 *
 * <p>⚠️ 精确 Apple Pay token 字段名、通知报文字段与实际开通产品，以银联商务商户接入文档为准。
 * 开放平台 HmacSHA256（OPEN-FORM-PARAM header 模式）属另一产品档、不同传输，未在此启用。</p>
 *
 * @author Tigshop团队
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UmsAcquirerProvider implements AcquirerProvider {

    private static final String INST_MID = "APPDEFAULT";
    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ConfigService configService;
    private final MessageHelper messageHelper;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    @Override
    public String providerCode() {
        return "ums";
    }

    @Override
    public boolean requiresDecryption() {
        return false;
    }

    @Override
    public boolean isAsyncSettlement() {
        // 银联商务：下单仅受理，最终支付成功以异步通知为准
        return true;
    }

    @Override
    public String submit(ApplePayAcquirerContext ctx) {
        ApplePayAcquirerConfig config = ctx.getConfig();
        String[] midTid = splitMidTid(config);

        Map<String, String> params = new TreeMap<>();
        params.put("msgId", UUID.randomUUID().toString().replace("-", ""));
        params.put("requestTimestamp", LocalDateTime.now().format(TS_FMT));
        params.put("mid", midTid[0]);
        params.put("tid", midTid[1]);
        params.put("instMid", INST_MID);
        params.put("msgType", "applepay.order");
        params.put("merOrderId", ctx.getPaySn());
        params.put("totalAmount", String.valueOf(toFen(ctx.getAmount())));
        // Apple Pay 支付数据（token.paymentData）交银联解密；确切字段名以银联商务文档为准
        params.put("appleData", extractPaymentData(ctx.getRawTokenJson()));

        JSONObject resp = signAndPost(config.getGateway() + "/v1/netpay/applepay/order", params, config.getKey());
        if (resp == null || !"SUCCESS".equalsIgnoreCase(resp.getString("errCode"))) {
            String reason = resp == null ? null : resp.getString("errMsg");
            log.warn("Apple Pay[ums] 下单失败，paySn={}, errCode={}", ctx.getPaySn(),
                    resp == null ? null : resp.getString("errCode"));
            throw new GlobalException(StrUtil.isNotBlank(reason) ? reason
                    : messageHelper.get("error.applepay.confirm.fail"));
        }
        String seqId = resp.getString("seqId");
        return StrUtil.isNotBlank(seqId) ? seqId : resp.getString("targetOrderId");
    }

    @Override
    public boolean refund(String transactionId, String refundSn, BigDecimal amount, ApplePayAcquirerConfig config) {
        String[] midTid = splitMidTid(config);
        Map<String, String> params = new TreeMap<>();
        params.put("msgId", UUID.randomUUID().toString().replace("-", ""));
        params.put("requestTimestamp", LocalDateTime.now().format(TS_FMT));
        params.put("mid", midTid[0]);
        params.put("tid", midTid[1]);
        params.put("instMid", INST_MID);
        params.put("merOrderId", transactionId);
        params.put("refundOrderId", refundSn);
        params.put("refundAmount", String.valueOf(toFen(amount)));

        JSONObject resp = signAndPost(config.getGateway() + "/v1/netpay/refund", params, config.getKey());
        boolean ok = resp != null && "SUCCESS".equalsIgnoreCase(resp.getString("errCode"));
        if (!ok) {
            log.warn("Apple Pay[ums] 退款失败，refundSn={}, errCode={}", refundSn,
                    resp == null ? null : resp.getString("errCode"));
        }
        return ok;
    }

    @Override
    public String query(String transactionId, ApplePayAcquirerConfig config) {
        if (config == null || StrUtil.isBlank(transactionId)) {
            return null;
        }
        String[] midTid = splitMidTid(config);
        Map<String, String> params = new TreeMap<>();
        params.put("msgId", UUID.randomUUID().toString().replace("-", ""));
        params.put("requestTimestamp", LocalDateTime.now().format(TS_FMT));
        params.put("mid", midTid[0]);
        params.put("tid", midTid[1]);
        params.put("instMid", INST_MID);
        params.put("merOrderId", transactionId);

        JSONObject resp = signAndPost(config.getGateway() + "/v1/netpay/query", params, config.getKey());
        return resp == null ? null : resp.getString("status");
    }

    @Override
    public AcquirerNotifyResult parseNotify(Map<String, Object> params, ApplePayAcquirerConfig config) {
        if (params == null || params.isEmpty() || config == null || StrUtil.isBlank(config.getKey())) {
            return AcquirerNotifyResult.fail();
        }
        Map<String, String> signParams = new TreeMap<>();
        String notifySign = null;
        for (Map.Entry<String, Object> e : params.entrySet()) {
            if (e.getValue() == null) {
                continue;
            }
            if ("sign".equals(e.getKey())) {
                notifySign = String.valueOf(e.getValue());
                continue;
            }
            signParams.put(e.getKey(), String.valueOf(e.getValue()));
        }
        // 通知按报文携带的 signType 自动验签（缺省回退配置）
        String signType = StrUtil.blankToDefault(signParams.get("signType"), currentSignType());
        String expect = sign(signParams, config.getKey(), signType);
        if (notifySign == null || !expect.equalsIgnoreCase(notifySign)) {
            log.warn("Apple Pay[ums] 通知验签失败");
            return AcquirerNotifyResult.fail();
        }
        String status = signParams.get("status");
        String errCode = signParams.get("errCode");
        String paySn = signParams.get("merOrderId");
        String transactionId = StrUtil.blankToDefault(signParams.get("seqId"), signParams.get("targetOrderId"));
        boolean paid = ("TRADE_SUCCESS".equalsIgnoreCase(status) || "SUCCESS".equalsIgnoreCase(status))
                && (errCode == null || "SUCCESS".equalsIgnoreCase(errCode));
        if (!paid || StrUtil.isBlank(paySn)) {
            return AcquirerNotifyResult.fail();
        }
        // 通知金额（分）转元，供 ApplePayServiceImpl 与本地 paylog 交叉校验（#4）
        BigDecimal amount = parseFenToYuan(signParams.get("totalAmount"));
        return AcquirerNotifyResult.success(paySn, transactionId, amount, "SUCCESS");
    }

    /**
     * merchantNo 约定承载 "mid" 或 "mid|tid"。
     */
    private String[] splitMidTid(ApplePayAcquirerConfig config) {
        if (config == null || StrUtil.isBlank(config.getGateway())
                || StrUtil.isBlank(config.getMerchantNo()) || StrUtil.isBlank(config.getKey())) {
            throw new GlobalException(messageHelper.get("error.applepay.config.incomplete"));
        }
        String[] seg = config.getMerchantNo().split("\\|", 2);
        return new String[]{seg[0], seg.length > 1 ? seg[1] : ""};
    }

    private String extractPaymentData(String rawTokenJson) {
        if (StrUtil.isBlank(rawTokenJson)) {
            return "";
        }
        try {
            JSONObject root = JSONObject.parseObject(rawTokenJson);
            JSONObject paymentData = root.getJSONObject("paymentData");
            return paymentData != null ? paymentData.toJSONString() : rawTokenJson;
        } catch (Exception e) {
            return rawTokenJson;
        }
    }

    private String currentSignType() {
        return StrUtil.blankToDefault(configService.getConfigVal(SettingsEnum.APPLE_PAY_UMS_SIGN_TYPE), "SHA256");
    }

    /**
     * 非空非 sign 参数按 key 字典序拼 k=v&...，末尾拼签名密钥，按 signType 摘要后转大写。
     */
    private String sign(Map<String, String> params, String signKey, String signType) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : params.entrySet()) {
            if ("sign".equals(e.getKey()) || StrUtil.isBlank(e.getValue())) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append('&');
            }
            sb.append(e.getKey()).append('=').append(e.getValue());
        }
        sb.append(signKey);
        return digestHex(sb.toString(), signType).toUpperCase();
    }

    /**
     * 按 signType 做摘要：SHA256（JDK）或 SM3（hutool，依赖 BouncyCastle）。
     */
    private String digestHex(String content, String signType) {
        if ("SM3".equalsIgnoreCase(signType)) {
            try {
                return cn.hutool.crypto.SmUtil.sm3(content);
            } catch (Throwable t) {
                log.error("Apple Pay[ums] SM3 摘要失败（需引入 bouncycastle 依赖）：{}", t.getClass().getSimpleName());
                throw new GlobalException(messageHelper.get("error.applepay.channel.not.ready"));
            }
        }
        return sha256Hex(content);
    }

    private String sha256Hex(String content) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(content.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : bytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new GlobalException(messageHelper.get("error.applepay.confirm.fail"));
        }
    }

    private JSONObject signAndPost(String url, Map<String, String> params, String signKey) {
        String signType = currentSignType();
        params.put("signType", signType);
        params.put("sign", sign(params, signKey, signType));
        JSONObject body = new JSONObject();
        body.putAll(params);
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(8))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toJSONString()))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2 || StrUtil.isBlank(response.body())) {
                log.warn("Apple Pay[ums] 网关响应异常，url={}, status={}", url, response.statusCode());
                return null;
            }
            return JSONObject.parseObject(response.body());
        } catch (Exception e) {
            log.error("Apple Pay[ums] 网关请求异常，url={}, err={}", url, e.getClass().getSimpleName());
            return null;
        }
    }

    private long toFen(BigDecimal amount) {
        if (amount == null) {
            return 0L;
        }
        return amount.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).longValue();
    }

    private BigDecimal parseFenToYuan(String fen) {
        if (StrUtil.isBlank(fen)) {
            return null;
        }
        try {
            return new BigDecimal(fen.trim()).movePointLeft(2);
        } catch (Exception e) {
            return null;
        }
    }
}
