package com.tigshop.bean.param.settings.config;

import com.tigshop.bean.annotations.settings.ConfigItemField;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.param.settings.config.base.ConfigSaveParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 店铺新增参数
 *
 * @author kidd
 * @since 2025/4/8 17:44
 */
@Schema(description = "店铺新增参数")
@Data
public class ShopSaveParam implements ConfigSaveParam {

    @ConfigItemField(SettingsEnum.SHOP_PRODUCT_NEED_CHECK)
    @Schema(description = "是否需要审核商品；0-否，1-是")
    private Integer shopProductNeedCheck;

    @ConfigItemField(SettingsEnum.MAX_RECOMMEND_PRODUCT_COUNT)
    @Schema(description = "最大推荐商品数量")
    private String maxRecommendProductCount;

    @ConfigItemField(SettingsEnum.MAX_SUB_ADMINISTRATOR)
    @Schema(description = "最大管理员数")
    private String maxSubAdministrator;

    @ConfigItemField(SettingsEnum.DEFAULT_SHOP_NAME)
    @Schema(description = "店铺名称")
    private String defaultShopName;

}
