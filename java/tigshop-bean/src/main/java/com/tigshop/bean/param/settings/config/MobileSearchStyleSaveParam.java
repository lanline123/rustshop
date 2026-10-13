package com.tigshop.bean.param.settings.config;

import com.tigshop.bean.annotations.settings.ConfigItemField;
import com.tigshop.bean.param.settings.config.base.ConfigSaveParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import static com.tigshop.bean.enums.setting.SettingsEnum.MOBILE_SEARCH_STYLE_TYPE;

/**
 * 移动端搜索风格配置保存参数
 *
 * @author Tigshop团队
 */
@Schema(description = "移动端搜索风格配置保存参数")
@Data
public class MobileSearchStyleSaveParam implements ConfigSaveParam {

    @ConfigItemField(MOBILE_SEARCH_STYLE_TYPE)
    @Schema(description = "移动端搜索风格；1-普通商品搜索，2-门店聚合搜索")
    private Integer mobileSearchStyleType;
}
