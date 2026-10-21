package com.tigshop.service.sms.impl;

import com.tigshop.service.sms.PreparedSmsRequest;
import com.tigshop.bean.enums.setting.SmsProviderEnum;
import com.tigshop.service.sms.AbstractSmsProvider;
import com.tigshop.service.sms.SmsResult;
import com.volcengine.service.sms.SmsService;
import com.volcengine.service.sms.impl.SmsServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class VolcengineSmsProvider extends AbstractSmsProvider {

    private static final Logger log = LoggerFactory.getLogger(VolcengineSmsProvider.class);

    @Override
    public SmsProviderEnum getProviderType() {
        return SmsProviderEnum.VOLCENGINE;
    }

    @Override
    protected SmsResult doSend(PreparedSmsRequest dto) {
        try {
            SmsService smsService = SmsServiceImpl.getInstance();
            smsService.setAccessKey(dto.getAccessKeyId());
            smsService.setSecretKey(dto.getAccessKeySecret());

            com.volcengine.model.request.SmsSendRequest request = new com.volcengine.model.request.SmsSendRequest();
            request.setSmsAccount(dto.getSmsAccount());
            request.setSign(dto.getSignName());
            request.setTemplateId(dto.getTemplateCode());
            request.setPhoneNumbers(dto.getPhoneNumbers());
            request.setTemplateParam(dto.getTemplateParam());

            com.volcengine.model.response.SmsSendResponse response = smsService.send(request);
            if (response != null && response.getResult() != null
                    && response.getResult().getMessageId() != null
                    && !response.getResult().getMessageId().isEmpty()) {
                return SmsResult.ok();
            }
            if (response != null && response.getResponseMetadata() != null && response.getResponseMetadata().getError() != null) {
                String code = response.getResponseMetadata().getError().getCode();
                String message = response.getResponseMetadata().getError().getMessage();
                return SmsResult.fail(code, message);
            }
            return SmsResult.fail("NO_RESPONSE", "未获取到响应");
        } catch (Exception e) {
            log.error("火山引擎短信发送异常：{}", e.getMessage(), e);
            return SmsResult.fail("EXCEPTION", e.getMessage());
        }
    }
}
