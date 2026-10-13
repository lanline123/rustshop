package com.tigshop.service.sms;

import com.tigshop.bean.dto.setting.SmsSendDTO;
import com.tigshop.bean.enums.setting.SmsProviderEnum;

public interface SmsProvider {

    SmsProviderEnum getProviderType();

    SmsResult sendSms(SmsSendDTO dto);
}
