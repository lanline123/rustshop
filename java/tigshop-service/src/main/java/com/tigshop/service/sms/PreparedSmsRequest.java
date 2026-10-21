package com.tigshop.service.sms;

import com.tigshop.bean.dto.setting.SmsSendDTO;
import lombok.Getter;

/**
 * 供应商专用不可变快照。号码已转换，其他字段原样复制；不持有原 DTO。
 * 不生成 toString，避免意外输出凭证和短信内容。
 */
@Getter
public final class PreparedSmsRequest {
    private final Integer smsProvider;
    private final String accessKeyId;
    private final String accessKeySecret;
    private final String phoneNumbers;
    private final String signName;
    private final String templateCode;
    private final String templateParam;
    private final String sdkAppId;
    private final String channel;
    private final String endpoint;
    private final String smsAccount;
    private final String messageContent;
    private final String internationalMessageType;
    private final String senderId;
    private final Boolean useInternational;

    // 只能在公共发送边界创建，具体供应商不能绕过转换自行构造。
    PreparedSmsRequest(SmsSendDTO source, String phoneNumbers) {
        this.smsProvider = source.getSmsProvider();
        this.accessKeyId = source.getAccessKeyId();
        this.accessKeySecret = source.getAccessKeySecret();
        this.phoneNumbers = phoneNumbers;
        this.signName = source.getSignName();
        this.templateCode = source.getTemplateCode();
        this.templateParam = source.getTemplateParam();
        this.sdkAppId = source.getSdkAppId();
        this.channel = source.getChannel();
        this.endpoint = source.getEndpoint();
        this.smsAccount = source.getSmsAccount();
        this.messageContent = source.getMessageContent();
        this.internationalMessageType = source.getInternationalMessageType();
        this.senderId = source.getSenderId();
        this.useInternational = source.getUseInternational();
    }
}
