package com.tigshop.bean.vo.setting.config;

import com.tigshop.bean.annotations.settings.ConfigItemField;
import com.tigshop.bean.enums.setting.SettingsEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 店铺配置
 *
 * @author kidd
 * @since 2025/4/8 17:08
 */
@Schema(description = "店铺配置")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShopSettingsVO {

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
