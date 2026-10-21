package com.tigshop.bean.param.settings.config;

import com.tigshop.bean.annotations.settings.ConfigItemField;
import com.tigshop.bean.param.settings.config.base.ConfigSaveParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import static com.tigshop.bean.enums.setting.SettingsEnum.USE_NEW_MEMBER_CENTER;

/**
 * 个人中心页装修配置保存参数
 */
@Schema(description = "个人中心页装修配置保存参数")
@Data
public class MemberCenterDecorateSaveParam implements ConfigSaveParam {

    @ConfigItemField(USE_NEW_MEMBER_CENTER)
    @Schema(description = "是否使用新版个人中心页；0-旧版离散装修，1-新版整页装修")
    private Integer useNewMemberCenter;
}
