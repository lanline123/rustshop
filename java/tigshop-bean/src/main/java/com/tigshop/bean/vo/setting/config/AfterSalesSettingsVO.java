package com.tigshop.bean.vo.setting.config;

import com.tigshop.bean.annotations.settings.ConfigItemField;
import com.tigshop.bean.enums.setting.SettingsEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 售后设置
 *
 * @author kidd
 * @since 2025/4/3 14:54
 */
@Schema(description = "售后设置")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AfterSalesSettingsVO {

    @ConfigItemField(SettingsEnum.TEMPLATE_CONTENT)
    @Schema(description = "售后服务")
    private String templateContent;

    @ConfigItemField(SettingsEnum.SHOW_TEMPLATE_CONTENT)
    @Schema(description = "是否展示售后服务")
    private Boolean showTemplateContent;

}
