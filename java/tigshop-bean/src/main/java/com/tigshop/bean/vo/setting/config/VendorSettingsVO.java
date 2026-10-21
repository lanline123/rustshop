package com.tigshop.bean.vo.setting.config;

import com.tigshop.bean.annotations.settings.ConfigItemField;
import com.tigshop.bean.enums.setting.SettingsEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 供应商配置
 *
 * @author kidd
 * @since 2025/7/4 17:08
 */
@Schema(description = "供应商配置")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorSettingsVO {

    @ConfigItemField(SettingsEnum.VENDOR_PRODUCT_NEED_CHECK)
    @Schema(description = "是否需要审核商品；0-否，1-是")
    private Integer vendorProductNeedCheck;

    @ConfigItemField(SettingsEnum.VENDOR_MAX_SUB_ADMINISTRATOR)
    @Schema(description = "最大管理员数")
    private Integer vendorMaxSubAdministrator;

    @ConfigItemField(SettingsEnum.VENDOR_SET_PRICE_TYPE)
    @Schema(description = "供应商设价方式：(1按比例，2-按固定数值加价，3-默认售价)")
    private Integer vendorSetPriceType;

    @ConfigItemField(SettingsEnum.VENDOR_SET_PRICE_AUTO_VALUE)
    @Schema(description = "智能设价（百分比或固定数值）")
    private String vendorSetPriceAutoValue;

}
