package com.tigshop.bean.param.settings.config;

import com.tigshop.bean.annotations.settings.ConfigItemField;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.param.settings.config.base.ConfigSaveParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 商户配置新增参数
 *
 * @author kidd
 * @since 2025/4/8 17:50
 */
@Schema(description = "商户配置新增参数")
@Data
public class MerchantSaveParam implements ConfigSaveParam {

    @ConfigItemField(SettingsEnum.PERSON_APPLY_ENABLED)
    @Schema(description = "是否开启个人申请；0-否，1-是")
    private Integer personApplyEnabled;

    @ConfigItemField(SettingsEnum.MERCHANT_APPLY_NEED_CHECK)
    @Schema(description = "商户申请是否需要审核；0-否，1-是")
    private Integer merchantApplyNeedCheck;

    @ConfigItemField(SettingsEnum.MAX_SHOP_COUNT)
    @Schema(description = "最大店铺数量")
    private String maxShopCount;

    @ConfigItemField(SettingsEnum.SHOP_AGREEMENT)
    @Schema(description = "店铺协议")
    private String shopAgreement;
}
