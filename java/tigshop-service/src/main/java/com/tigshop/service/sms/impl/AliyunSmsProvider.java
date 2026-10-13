package com.tigshop.service.sms.impl;

import com.tigshop.service.sms.PreparedSmsRequest;
import com.tigshop.bean.enums.setting.SmsProviderEnum;
import com.tigshop.common.utils.SmsUtils;
import com.tigshop.service.sms.AbstractSmsProvider;
import com.tigshop.service.sms.SmsResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AliyunSmsProvider extends AbstractSmsProvider {

    private static final Logger log = LoggerFactory.getLogger(AliyunSmsProvider.class);

    @Override
    public SmsProviderEnum getProviderType() {
        return SmsProviderEnum.ALIYUN;
    }

    @Override
    protected SmsResult doSend(PreparedSmsRequest dto) {
        try {
            SmsUtils.AliyunSmsSendResult response;
            if (Boolean.TRUE.equals(dto.getUseInternational())) {
                // 海外通道：SendMessageToGlobe（SenderID + 明文内容）
                response = SmsUtils.sendGlobeSms(
                        dto.getAccessKeyId(),
                        dto.getAccessKeySecret(),
                        dto.getPhoneNumbers(),
                        dto.getSenderId(),
                        dto.getMessageContent(),
                        dto.getInternationalMessageType());
            } else {
                // 国内通道：SendSms（签名 + 模板 + 参数）
                response = SmsUtils.sendTemplateSms(
                        dto.getAccessKeyId(),
                        dto.getAccessKeySecret(),
                        dto.getPhoneNumbers(),
                        dto.getSignName(),
                        dto.getTemplateCode(),
                        dto.getTemplateParam());
            }
            if (response.isSuccess()) {
                return SmsResult.ok();
            }
            return SmsResult.fail(response.getCode(), response.getMessage());
        } catch (Exception e) {
            log.error("阿里云短信发送异常：{}", e.getMessage(), e);
            return SmsResult.fail("EXCEPTION", e.getMessage());
        }
    }
}
