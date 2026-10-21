package com.tigshop.bean.vo.config;

import com.tigshop.bean.annotations.settings.ConfigItemField;
import com.tigshop.bean.enums.setting.SettingsEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 主题配置项
 *
 * @author kidd
 * @since 2025/4/9 09:40
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThemeSettingsVO {

    @ConfigItemField(SettingsEnum.THEME_STYLE)
    @Schema(description = "主题配置")
    private String themeStyle;

    public ThemeSettingsVO(Map<String, String> settingsMap) {
        this.themeStyle = settingsMap.get(SettingsEnum.THEME_STYLE.getBizCode());
    }
}
