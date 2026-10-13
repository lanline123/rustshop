package com.tigshop.bean.vo.setting.config;

import com.tigshop.bean.annotations.settings.ConfigItemField;
import com.tigshop.bean.enums.setting.SettingsEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Tigshop项目组
 * @create 2025年09月03日 16:56
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "搜索配置参数")
public class SearchSettingsVO {
    @ConfigItemField(SettingsEnum.SELECT_SEARCH_TYPE)
    @Schema(description = "搜索类型")
    private String selectSearchType;
}