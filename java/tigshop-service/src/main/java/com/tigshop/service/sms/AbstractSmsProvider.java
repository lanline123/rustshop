package com.tigshop.service.sms;

import com.tigshop.bean.dto.setting.SmsSendDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 所有同步、MQ 短信共享的发送边界。 */
public abstract class AbstractSmsProvider implements SmsProvider {
    private static final Logger log = LoggerFactory.getLogger(AbstractSmsProvider.class);

    @Override
    public final SmsResult sendSms(SmsSendDTO dto) {
        try {
            String recipient = SmsPhoneNumberCodec.format(
                    dto.getPhoneNumbers(), getProviderType(), dto.getUseInternational());
            log.info("短信号码统一处理：{}",
                    SmsPhoneNumberCodec.diagnostic(dto.getPhoneNumbers(), recipient,
                            getProviderType(), dto.getUseInternational()));
            return doSend(new PreparedSmsRequest(dto, recipient));
        } catch (Exception e) {
            // 保持供应商原来的异常结果协议；诊断不输出请求内容。
            log.error("短信发送准备异常：provider={}, exceptionType={}",
                    getProviderType(), e.getClass().getSimpleName());
            return SmsResult.fail("EXCEPTION", e.getMessage());
        }
    }

    protected abstract SmsResult doSend(PreparedSmsRequest request);
}
