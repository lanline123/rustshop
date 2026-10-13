package com.tigshop.service.sms.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tigshop.service.sms.PreparedSmsRequest;
import com.tigshop.bean.enums.setting.SmsProviderEnum;
import com.tigshop.service.sms.AbstractSmsProvider;
import com.tigshop.service.sms.SmsResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class HuaweiSmsProvider extends AbstractSmsProvider {

    private static final Logger log = LoggerFactory.getLogger(HuaweiSmsProvider.class);
    private static final String API_PATH = "/sms/batchSendSms/v1";

    @Override
    public SmsProviderEnum getProviderType() {
        return SmsProviderEnum.HUAWEI;
    }

    @Override
    protected SmsResult doSend(PreparedSmsRequest dto) {
        try {
            String appKey = dto.getAccessKeyId();
            String appSecret = dto.getAccessKeySecret();
            String endpoint = dto.getEndpoint();
            String sender = dto.getChannel();

            String receiver = dto.getPhoneNumbers();

            String templateParas = buildTemplateParas(dto.getTemplateParam());

            String body = "from=" + urlEncode(sender)
                    + "&to=" + urlEncode(receiver)
                    + "&templateId=" + urlEncode(dto.getTemplateCode())
                    + "&templateParas=" + urlEncode(templateParas)
                    + "&signature=" + urlEncode(dto.getSignName());

            String wsseHeader = buildWsseHeader(appKey, appSecret);
            String apiUrl = endpoint + API_PATH;

            HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Authorization", "WSSE realm=\"SDP\",profile=\"UsernameToken\",type=\"Appkey\"");
            connection.setRequestProperty("X-WSSE", wsseHeader);

            try (OutputStream os = connection.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = connection.getResponseCode();
            String responseBody = new String(
                    (responseCode >= 200 && responseCode < 300 ? connection.getInputStream() : connection.getErrorStream())
                            .readAllBytes(), StandardCharsets.UTF_8);

            if (responseCode == 200) {
                JSONObject json = JSON.parseObject(responseBody);
                String code = json.getString("code");
                if ("000000".equals(code)) {
                    return SmsResult.ok();
                }
                return SmsResult.fail(code, json.getString("description"));
            }
            return SmsResult.fail(String.valueOf(responseCode), responseBody);
        } catch (Exception e) {
            log.error("华为云短信发送异常：{}", e.getMessage(), e);
            return SmsResult.fail("EXCEPTION", e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private String buildTemplateParas(String templateParam) {
        if (templateParam == null || templateParam.isBlank()) {
            return "[]";
        }
        try {
            Object parsed = JSON.parse(templateParam);
            if (parsed instanceof List) {
                return JSON.toJSONString(parsed);
            }
            if (parsed instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) parsed;
                List<String> values = new ArrayList<>();
                map.forEach((k, v) -> values.add(String.valueOf(v)));
                return JSON.toJSONString(values);
            }
        } catch (Exception e) {
            log.warn("华为云短信模板参数解析失败，原始值：{}", templateParam);
        }
        return "[\"" + templateParam + "\"]";
    }

    private String buildWsseHeader(String appKey, String appSecret) {
        String nonce = UUID.randomUUID().toString().replace("-", "");
        String created = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(new Date());
        String passwordDigest = base64Sha256(nonce + created + appSecret);
        return String.format(
                "UsernameToken Username=\"%s\",PasswordDigest=\"%s\",Nonce=\"%s\",Created=\"%s\"",
                appKey, passwordDigest, nonce, created);
    }

    private String base64Sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 digest failed", e);
        }
    }

    private String urlEncode(String value) {
        if (value == null) return "";
        return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
