package com.tigshop.bean.param.settings.config;

import com.tigshop.bean.annotations.settings.ConfigItemField;
import com.tigshop.bean.param.settings.config.base.ConfigSaveParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import static com.tigshop.bean.enums.setting.SettingsEnum.SEARCH_STYLE_TYPE;

/**
 * 搜索页风格配置保存参数
 *
 * @author Tigshop团队
 */
@Schema(description = "搜索页风格配置保存参数")
@Data
public class SearchStyleSaveParam implements ConfigSaveParam {

    @ConfigItemField(SEARCH_STYLE_TYPE)
    @Schema(description = "搜索页风格；1-样式一，2-样式二")
    private Integer searchStyleType;
}
