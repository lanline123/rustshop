package com.tigshop.service.sms.impl;

import com.alibaba.fastjson.JSON;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.sms.v20210111.SmsClient;
import com.tencentcloudapi.sms.v20210111.models.SendSmsRequest;
import com.tencentcloudapi.sms.v20210111.models.SendSmsResponse;
import com.tencentcloudapi.sms.v20210111.models.SendStatus;
import com.tigshop.service.sms.PreparedSmsRequest;
import com.tigshop.bean.enums.setting.SmsProviderEnum;
import com.tigshop.service.sms.AbstractSmsProvider;
import com.tigshop.service.sms.SmsResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class TencentSmsProvider extends AbstractSmsProvider {

    private static final Logger log = LoggerFactory.getLogger(TencentSmsProvider.class);

    @Override
    public SmsProviderEnum getProviderType() {
        return SmsProviderEnum.TENCENT;
    }

    @Override
    protected SmsResult doSend(PreparedSmsRequest dto) {
        try {
            Credential cred = new Credential(dto.getAccessKeyId(), dto.getAccessKeySecret());
            SmsClient client = new SmsClient(cred, "ap-guangzhou");

            SendSmsRequest req = new SendSmsRequest();
            req.setSmsSdkAppId(dto.getSdkAppId());
            req.setSignName(dto.getSignName());
            req.setTemplateId(dto.getTemplateCode());

            req.setPhoneNumberSet(new String[]{dto.getPhoneNumbers()});

            String[] templateParamSet = parseTemplateParams(dto.getTemplateParam());
            if (templateParamSet != null && templateParamSet.length > 0) {
                req.setTemplateParamSet(templateParamSet);
            }

            SendSmsResponse resp = client.SendSms(req);
            SendStatus[] statuses = resp.getSendStatusSet();
            if (statuses != null && statuses.length > 0) {
                SendStatus status = statuses[0];
                if ("Ok".equals(status.getCode())) {
                    return SmsResult.ok();
                }
                return SmsResult.fail(status.getCode(), status.getMessage());
            }
            return SmsResult.fail("NO_STATUS", "未获取到发送状态");
        } catch (Exception e) {
            log.error("腾讯云短信发送异常：{}", e.getMessage(), e);
            return SmsResult.fail("EXCEPTION", e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private String[] parseTemplateParams(String templateParam) {
        if (templateParam == null || templateParam.isBlank()) {
            return new String[0];
        }
        try {
            Object parsed = JSON.parse(templateParam);
            if (parsed instanceof java.util.List) {
                java.util.List<Object> list = (java.util.List<Object>) parsed;
                return list.stream().map(String::valueOf).toArray(String[]::new);
            }
            if (parsed instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) parsed;
                return map.values().stream().map(String::valueOf).toArray(String[]::new);
            }
        } catch (Exception e) {
            log.warn("腾讯云短信模板参数解析失败，原始值：{}", templateParam);
        }
        return new String[]{templateParam};
    }
}
