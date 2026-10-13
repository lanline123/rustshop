package com.tigshop.service.msgTemplate.impl;

import cn.hutool.json.JSONUtil;
import com.tigshop.bean.dto.msg.Message;
import com.tigshop.bean.dto.setting.SmsSendDTO;
import com.tigshop.bean.enums.msg.MessageTemplateType;
import com.tigshop.bean.enums.setting.SmsProviderEnum;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.service.msgTemplate.MessageSender;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.sms.SmsChannelHelper;
import com.tigshop.service.sms.SmsProvider;
import com.tigshop.service.sms.SmsProviderFactory;
import com.tigshop.service.sms.SmsResult;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.tigshop.bean.enums.setting.SettingsEnum.*;

@Component
public class SMSSender implements MessageSender {

    private static final Logger log = LoggerFactory.getLogger(SMSSender.class);

    @Resource
    private ConfigService configService;

    @Resource
    private SmsProviderFactory smsProviderFactory;

    @Override
    public boolean supports(MessageTemplateType type) {
        return type == MessageTemplateType.SMS;
    }

    @Override
    public void send(Message message) {
        try {
            String providerVal = configService.getConfigVal(SMS_PROVIDER);
            Integer providerCode = (providerVal != null && !providerVal.isEmpty()) ? Integer.parseInt(providerVal) : 1;
            SmsProviderEnum providerEnum = SmsProviderEnum.fromCode(providerCode);

            SmsSendDTO smsSend = buildSmsSendDTO(providerEnum, message);
            if (smsSend == null) {
                // 走海外通道但未配置海外短信明文文案，已在 buildSmsSendDTO 记录日志，直接中止本条
                return;
            }

            SmsProvider provider = smsProviderFactory.getProvider(providerEnum);
            SmsResult result = provider.sendSms(smsSend);

            if (!result.isSuccess()) {
                log.error("短信发送失败！服务商：{}，号码：{}，错误码：{}，错误信息：{}",
                        providerEnum.getName(), smsSend.getPhoneNumbers(), result.getCode(), result.getMessage());
            } else {
                log.info("短信发送成功！服务商：{}，号码：{}", providerEnum.getName(), smsSend.getPhoneNumbers());
            }
        } catch (Exception e) {
            log.error("短信发送失败！{}", e.getMessage());
        }
    }

    private SmsSendDTO buildSmsSendDTO(SmsProviderEnum providerEnum, Message message) {
        SmsSendDTO dto = new SmsSendDTO();
        dto.setSmsProvider(providerEnum.getCode());
        String mobile = (String) message.getMessage().get("mobile");
        dto.setPhoneNumbers(mobile);
        String templateCode = (String) message.getMessage().get("template_code");
        String contentOversea = (String) message.getMessage().get("content_oversea");
        Object contentParams = message.getMessage().get("content");
        dto.setTemplateParam(JSONUtil.toJsonStr(contentParams));
        dto.setTemplateCode(templateCode);

        // 阿里云：按通道开关（smsAliyunChannel）+ 号码决定走国内模板（SendSms）/ 海外明文（SendMessageToGlobe）
        if (providerEnum == SmsProviderEnum.ALIYUN) {
            String channel = configService.getConfigVal(SMS_ALIYUN_CHANNEL);
            boolean useInternational = SmsChannelHelper.useInternationalChannel(channel, mobile);
            dto.setUseInternational(useInternational);
            if (useInternational) {
                if (contentOversea == null || contentOversea.isBlank()) {
                    log.error("走海外通道但未配置海外短信明文文案，跳过发送。号码：{}，国内模板：{}", mobile, templateCode);
                    return null;
                }
                // 海外明文：用业务参数替换占位符后作为 SendMessageToGlobe 的 Message
                Map<String, ?> params = (contentParams instanceof Map) ? (Map<String, ?>) contentParams : null;
                dto.setMessageContent(SmsChannelHelper.renderPlaintext(contentOversea, params));
                dto.setInternationalMessageType("NOTIFY");
            }
        }
        // 填充服务商账号配置（单套账号，阿里云含 SenderID）
        Map<String, String> cfg = configService.getConfigMapBySettings(SmsChannelHelper.configKeys(providerEnum));
        SmsChannelHelper.fillProviderConfig(dto, providerEnum, cfg);
        return dto;
    }
}
