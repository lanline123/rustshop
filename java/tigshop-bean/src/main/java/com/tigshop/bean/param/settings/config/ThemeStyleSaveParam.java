package com.tigshop.bean.param.settings.config;

import com.tigshop.bean.annotations.settings.ConfigItemField;
import com.tigshop.bean.param.settings.config.base.ConfigSaveParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import static com.tigshop.bean.enums.setting.SettingsEnum.THEME_STYLE;

/**
 * 主题风格配置新增参数
 *
 * @author kidd
 * @since 2025/4/8 14:19
 */
@Schema(description = "主题风格配置新增参数")
@Data
public class ThemeStyleSaveParam implements ConfigSaveParam {

    @ConfigItemField(THEME_STYLE)
    @Schema(description = "主题类型")
    private String themeStyle;

}
