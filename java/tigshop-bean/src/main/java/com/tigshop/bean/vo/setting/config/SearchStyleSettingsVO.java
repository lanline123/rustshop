package com.tigshop.bean.vo.setting.config;

import com.tigshop.bean.annotations.settings.ConfigItemField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.tigshop.bean.enums.setting.SettingsEnum.SEARCH_STYLE_TYPE;

/**
 * 搜索页风格配置
 *
 * @author Tigshop团队
 */
@Schema(description = "搜索页风格配置")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchStyleSettingsVO {

    @ConfigItemField(SEARCH_STYLE_TYPE)
    @Schema(description = "搜索页风格；1-样式一，2-样式二")
    private Integer searchStyleType;

}
