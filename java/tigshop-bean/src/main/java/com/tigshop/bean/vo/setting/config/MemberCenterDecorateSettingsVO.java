package com.tigshop.bean.vo.setting.config;

import com.tigshop.bean.annotations.settings.ConfigItemField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.tigshop.bean.enums.setting.SettingsEnum.USE_NEW_MEMBER_CENTER;

/**
 * 个人中心页装修配置
 */
@Schema(description = "个人中心页装修配置")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberCenterDecorateSettingsVO {

    @ConfigItemField(USE_NEW_MEMBER_CENTER)
    @Schema(description = "是否使用新版个人中心页；0-旧版离散装修，1-新版整页装修")
    private Integer useNewMemberCenter;

}
