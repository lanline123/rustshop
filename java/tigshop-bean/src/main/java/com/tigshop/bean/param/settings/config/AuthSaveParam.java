package com.tigshop.bean.param.settings.config;

import com.tigshop.bean.annotations.settings.ConfigItemField;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.param.settings.config.base.ConfigSaveParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 会员认证配置新增参数
 *
 * @author kidd
 * @since 2025/4/3 16:30
 */
@Data
public class AuthSaveParam implements ConfigSaveParam {

    // *** 会员认证设置 ***

    @ConfigItemField(SettingsEnum.CLOSE_AUTH)
    @Schema(description = "是否关闭；1-否，0-是")
    private Integer closeAuth;

    @ConfigItemField(SettingsEnum.TYPE)
    @Schema(description = "认证类型；0-个人认证 + 企业认证，1-企业认证")
    private Integer type;

    @ConfigItemField(SettingsEnum.IS_IDENTITY)
    @Schema(description = "是否实名后才能查看价格；0-关闭，1-开启")
    private Integer isIdentity;

    @ConfigItemField(SettingsEnum.SMS_NOTE)
    @Schema(description = "是否短信通知；0-关闭，1-开启")
    private Integer smsNote;

    @ConfigItemField(SettingsEnum.TIPS)
    @Schema(description = "审核周期")
    private String tips;

}
