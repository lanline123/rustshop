package com.tigshop.bean.vo.setting.config;

import com.tigshop.bean.annotations.settings.ConfigItemField;
import com.tigshop.bean.enums.setting.SettingsEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 邮箱配置
 *
 * @author kidd
 * @since 2025/4/3 14:53
 */
@Schema(description = "邮箱配置")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MailSettingsVO {

    @ConfigItemField(SettingsEnum.MAIL_SERVICE)
    @Schema(description = "邮件服务；0-本地邮件服务器，1-SMTP服务")
    private Integer mailService;

    @ConfigItemField(SettingsEnum.SMTP_SSL)
    @Schema(description = "是否启用SSL；0-否，1-是")
    private Integer smtpSsl;

    @ConfigItemField(SettingsEnum.SMTP_HOST)
    @Schema(description = "发送邮件服务器地址(SMTP)")
    private String smtpHost;

    @ConfigItemField(SettingsEnum.SMTP_PORT)
    @Schema(description = "SMTP服务器端口")
    private String smtpPort;

    @ConfigItemField(SettingsEnum.SMTP_USER)
    @Schema(description = "邮件发送帐号")
    private String smtpUser;

    @ConfigItemField(SettingsEnum.SMTP_PASS)
    @Schema(description = "帐号密码")
    private String smtpPass;

    @ConfigItemField(SettingsEnum.SMTP_MAIL)
    @Schema(description = "邮件回复地址")
    private String smtpMail;

    @ConfigItemField(SettingsEnum.MAIL_CHARSET)
    @Schema(description = "邮件编码；UTF8-国际编码(UTF8)，GB2312-简体中文(GB2312)，BIG5-繁体中文(BIG5)")
    private String mailCharset;

    @ConfigItemField(SettingsEnum.TEST_MAIL_ADDRESS)
    @Schema(description = "测试邮件地址")
    private String testMailAddress;

    @ConfigItemField(SettingsEnum.SERVICE_EMAIL)
    @Schema(description = "服务邮箱")
    private String serviceEMail;

}
