package com.tigshop.bean.param.settings.config;

import com.tigshop.bean.annotations.settings.ConfigItemField;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.param.settings.config.base.ConfigSaveParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 售后设置新增参数
 *
 * @author kidd
 * @since 2025/4/2 09:12
 */
@Schema(description = "售后设置新增参数")
@Data
public class AfterSaleSaveParam implements ConfigSaveParam {

    @ConfigItemField(SettingsEnum.TEMPLATE_CONTENT)
    @Schema(description = "售后服务")
    private String templateContent;

    @ConfigItemField(SettingsEnum.SHOW_TEMPLATE_CONTENT)
    @Schema(description = "是否展示售后服务")
    private Boolean showTemplateContent;
}
