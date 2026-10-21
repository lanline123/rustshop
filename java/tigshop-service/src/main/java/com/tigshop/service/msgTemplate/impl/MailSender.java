package com.tigshop.service.msgTemplate.impl;

import cn.hutool.core.lang.Validator;
import cn.hutool.extra.mail.MailAccount;
import cn.hutool.extra.mail.MailUtil;
import cn.hutool.json.JSONUtil;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.tigshop.bean.dto.msg.Message;
import com.tigshop.bean.dto.setting.SmsSendDTO;
import com.tigshop.bean.enums.msg.MessageTemplateType;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.SmsUtils;
import com.tigshop.service.msgTemplate.MessageSender;
import com.tigshop.service.setting.ConfigService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static com.tigshop.bean.enums.setting.SettingsEnum.*;

/**
 * 短信发送
 */
@Slf4j
@Component
public class MailSender implements MessageSender {

    @Resource
    private ConfigService configService;

    @Override
    public boolean supports(MessageTemplateType type) {
        return type == MessageTemplateType.Mail;
    }

    @Override
    public void send(Message message) {
        try {

            if (!Validator.isEmail(JSONUtil.toJsonStr(message.getMessage().get("userEmailAddress")))){
                throw new GlobalException("邮箱格式错误！");
            }
            List<SettingsEnum> settings = List.of(MAIL_SERVICE, SMTP_SSL, SMTP_HOST,SMTP_PORT,SMTP_USER,SMTP_PASS,SMTP_MAIL,MAIL_CHARSET,TEST_MAIL_ADDRESS);
            Map<String, String> configMap = configService.getConfigMapBySettings(settings);

            // 创建邮件账户对象
            MailAccount account = new MailAccount();
            account.setHost(configService.getConfigVal(SMTP_HOST));
            account.setPort(Integer.parseInt(configService.getConfigVal(SMTP_PORT)));
            account.setAuth(true);
            account.setFrom(configService.getConfigVal(SMTP_MAIL));
            account.setPass(configService.getConfigVal(SMTP_PASS));
            account.setSslEnable(true);
            account.setStarttlsEnable(true);

            //发送邮件
            MailUtil.send(account, JSONUtil.toJsonStr(message.getMessage().get("userEmailAddress")),
                    JSONUtil.toJsonStr(message.getMessage().get("templateName")),
                    message.getMessage().get("content").toString(), false);

        } catch (Exception e) {
            log.error("邮件发送失败！{}", e.getMessage());
        }
    }
}
