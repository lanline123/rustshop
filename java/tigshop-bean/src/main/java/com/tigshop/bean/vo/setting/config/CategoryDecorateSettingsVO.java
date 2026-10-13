package com.tigshop.bean.vo.setting.config;

import com.tigshop.bean.annotations.settings.ConfigItemField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.tigshop.bean.enums.setting.SettingsEnum.PRODUCT_CATEGORY_DECORATE_TYPE;

/**
 * 分类页装修配置
 *
 * @author kidd
 * @since 2025/4/8 14:03
 */
@Schema(description = "分类页装修配置")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDecorateSettingsVO {

    @ConfigItemField(PRODUCT_CATEGORY_DECORATE_TYPE)
    @Schema(description = "分类页装修类型；1-样式一，2-样式二，3-样式三")
    private Integer productCategoryDecorateType;

}
