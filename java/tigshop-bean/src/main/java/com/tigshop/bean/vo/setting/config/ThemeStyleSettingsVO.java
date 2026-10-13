package com.tigshop.bean.vo.setting.config;

import com.tigshop.bean.annotations.settings.ConfigItemField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.tigshop.bean.enums.setting.SettingsEnum.THEME_STYLE;

/**
 * 主题风格配置
 *
 * @author kidd
 * @since 2025/4/8 14:18
 */
@Schema(description = "主题风格配置")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThemeStyleSettingsVO {

    @ConfigItemField(THEME_STYLE)
    @Schema(description = "主题类型")
    private String themeStyle;

}
