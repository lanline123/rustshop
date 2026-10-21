package com.tigshop.service.msgTemplate.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tigshop.bean.dto.msg.Message;
import com.tigshop.bean.enums.msg.MessageTemplateType;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.service.msgTemplate.MessageSender;
import com.tigshop.service.setting.ConfigService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tigshop.bean.enums.setting.SettingsEnum.*;

/**
 * 钉钉机器人消息发送器（使用HTTP客户端实现）
 */
@Slf4j
@Component
public class DingSender implements MessageSender {

    @Resource
    private ConfigService configService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean supports(MessageTemplateType type) {
        return type == MessageTemplateType.Ding;
    }

    @Override
    public void send(Message message) {
        try {
            // 获取配置信息
            List<SettingsEnum> settings = List.of(DING_TALK_ROBOT_TOKEN, DING_APP_SECRET);
            Map<String, String> configMap = configService.getConfigMapBySettings(settings);

            if (configMap == null) {
                throw new GlobalException("钉钉聊天机器人没有配置");
            }

            String customRobotToken = configMap.get(DING_TALK_ROBOT_TOKEN.getBizCode());
            if (StringUtils.isEmpty(customRobotToken)) {
                throw new GlobalException("请配置钉钉机器人access_token");
            }

            String secret = configMap.get(DING_APP_SECRET.getBizCode());
            if (StringUtils.isEmpty(secret)) {
                throw new GlobalException("请先配置钉钉机器人secret");
            }

            // 生成签名
            Long timestamp = System.currentTimeMillis();
            String sign = generateSign(timestamp, secret);

            // 构造请求URL
            String webhookUrl = customRobotToken + "&sign=" + sign + "&timestamp=" + timestamp;

            // 创建消息内容
            Map<String, Object> requestBody = createMessageBody(message);

            // 发送HTTP请求
            String response = sendHttpRequest(webhookUrl, requestBody);

            // 解析响应
            Map responseMap = objectMapper.readValue(response, Map.class);
            if ("0".equals(responseMap.get("errcode").toString())) {
                log.info("钉钉消息发送成功: {}", response);
            } else {
                log.error("钉钉消息发送失败: {}", response);
            }
        } catch (Exception e) {
            log.error("钉钉消息发送异常", e);
        }
    }

    /**
     * 生成签名
     * @param timestamp 时间戳
     * @param secret 密钥
     * @return 签名
     * @throws Exception 加密异常
     */
    private String generateSign(Long timestamp, String secret) throws Exception {
        String stringToSign = timestamp + "\n" + secret;
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
        return URLEncoder.encode(new String(Base64.encodeBase64(signData)), StandardCharsets.UTF_8);
    }

    /**
     * 创建消息体
     * @param message 消息内容
     * @return 消息体Map
     */
    private Map<String, Object> createMessageBody(Message message) {
        Map<String, Object> body = new HashMap<>();

        // 获取消息类型，默认为text
        String msgType = message.getMessage().get("msgtype") != null ?
                message.getMessage().get("msgtype").toString() : "text";

        body.put("msgtype", msgType);

        switch (msgType) {
            case "text":
                // 文本消息
                Map<String, String> textContent = new HashMap<>();
                textContent.put("content", message.getMessage().get("content").toString());
                body.put("text", textContent);
                break;

            case "link":
                // 链接消息
                Map<String, String> linkContent = new HashMap<>();
                linkContent.put("title", message.getMessage().get("title").toString());
                linkContent.put("text", message.getMessage().get("text").toString());
                linkContent.put("messageUrl", message.getMessage().get("messageUrl").toString());
                if (message.getMessage().get("picUrl") != null) {
                    linkContent.put("picUrl", message.getMessage().get("picUrl").toString());
                }
                body.put("link", linkContent);
                break;

            case "markdown":
                // Markdown消息
                Map<String, String> markdownContent = new HashMap<>();
                markdownContent.put("title", message.getMessage().get("title").toString());
                markdownContent.put("text", message.getMessage().get("text").toString());
                body.put("markdown", markdownContent);
                break;

            default:
                // 默认文本消息
                Map<String, String> defaultText = new HashMap<>();
                defaultText.put("content", message.getMessage().get("content").toString());
                body.put("text", defaultText);
                break;
        }

        return body;
    }

    /**
     * 发送HTTP请求
     * @param url 请求URL
     * @param requestBody 请求体
     * @return 响应内容
     * @throws Exception 请求异常
     */
    private String sendHttpRequest(String url, Map<String, Object> requestBody) throws Exception {
        // 显式设置连接与请求超时，避免钉钉网络异常时永久阻塞调用线程，进而拖垮支付/订单主流程
        java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        String jsonBody = objectMapper.writeValueAsString(requestBody);

        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(url))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(10))
                .POST(java.net.http.HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        java.net.http.HttpResponse<String> response = client.send(request,
                java.net.http.HttpResponse.BodyHandlers.ofString());

        return response.body();
    }
}
