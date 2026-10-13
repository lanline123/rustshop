package com.tigshop.bean.param.settings.config;

import com.tigshop.bean.annotations.settings.ConfigItemField;
import com.tigshop.bean.param.settings.config.base.ConfigSaveParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import static com.tigshop.bean.enums.setting.SettingsEnum.PRODUCT_CATEGORY_DECORATE_TYPE;

/**
 * 分页页装修配置新增参数
 *
 * @author kidd
 * @since 2025/4/8 14:05
 */
@Schema(description = "分页页装修配置新增参数")
@Data
public class CategoryDecorateSaveParam implements ConfigSaveParam {

    @ConfigItemField(PRODUCT_CATEGORY_DECORATE_TYPE)
    @Schema(description = "分类页装修类型；1-样式一，2-样式二，3-样式三")
    private Integer productCategoryDecorateType;
}
