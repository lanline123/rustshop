package com.tigshop.service.msgTemplate.impl;

import cn.hutool.core.lang.Validator;
import cn.hutool.json.JSONUtil;
import com.google.gson.Gson;
import com.tigshop.bean.dto.msg.Message;
import com.tigshop.bean.enums.msg.MessageTemplateType;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.service.msgTemplate.MessageSender;
import com.tigshop.service.setting.ConfigService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.tigshop.bean.enums.setting.SettingsEnum.*;

/**
 * 短信发送
 */
@Slf4j
@Component
public class WXWorkSender implements MessageSender {

    @Resource
    private ConfigService configService;

    @Override
    public boolean supports(MessageTemplateType type) {
        return type == MessageTemplateType.WXWork;
    }

    @Override
    public void send(Message message) {
        try {

            List<SettingsEnum> settings = List.of(WXWORK_WEBHOOK_URL);
            Map<String, String> configMap = configService.getConfigMapBySettings(settings);
            String webHookUrl = configMap.get(WXWORK_WEBHOOK_URL.getBizCode());
            if (Validator.isEmpty(webHookUrl)){
                throw new GlobalException("微信企业消息发送失败:请先配置微信企业微信");
            }
            Map<String, Object> msg = new HashMap<>();
            msg.put("msgtype", "text");

            Map<String, String> textContent = new HashMap<>();
            textContent.put("content", message.getMessage().get("content").toString());
            msg.put("text", textContent);
            //HttpUtil.post(webHookUrl, JSONUtil.toJsonStr(msg));
            // 显式设置连接/读/写超时，避免企业微信网络异常时阻塞调用线程拖垮支付/订单主流程
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .build();
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, new Gson().toJson(msg));
            Request request = new Request.Builder()
                    .url(webHookUrl)
                    .post(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new GlobalException("微信企业消息发送失败: " + response.message());
                }
            }
        } catch (Exception e) {
            log.error("微信企业消息发送失败！{}", e.getMessage());
        }
    }
}
