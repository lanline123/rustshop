package com.tigshop.bean.vo.setting.config;

import com.tigshop.bean.annotations.settings.ConfigItemField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.tigshop.bean.enums.setting.SettingsEnum.MOBILE_SEARCH_STYLE_TYPE;

/**
 * 移动端搜索风格配置
 *
 * @author Tigshop团队
 */
@Schema(description = "移动端搜索风格配置")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MobileSearchStyleSettingsVO {

    @ConfigItemField(MOBILE_SEARCH_STYLE_TYPE)
    @Schema(description = "移动端搜索风格；1-普通商品搜索，2-门店聚合搜索")
    private Integer mobileSearchStyleType;

}
