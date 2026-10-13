package com.tigshop.bean.param.settings.config;

import com.tigshop.bean.annotations.settings.ConfigItemField;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.param.settings.config.base.ConfigSaveParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author Tigshop项目组
 * @create 2025年09月03日 17:00
 */
@Schema(description = "搜索配置参数")
@Data
public class SearchSaveParam implements ConfigSaveParam {
    @ConfigItemField(SettingsEnum.SELECT_SEARCH_TYPE)
    @Schema(description = "搜索类型es/mysql")
    private String selectSearchType;
}