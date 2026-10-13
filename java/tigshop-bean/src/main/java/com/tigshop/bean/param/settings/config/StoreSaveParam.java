// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.param.settings.config;


import com.tigshop.bean.annotations.settings.ConfigItemField;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.param.settings.config.base.ConfigSaveParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author Tigshop团队
 * @create 2025/8/12 11:37
 */
@Schema(description = "门店设置参数")
@Data
public class StoreSaveParam implements ConfigSaveParam {
    @ConfigItemField(SettingsEnum.MAX_RECOMMEND_PRODUCT_COUNT)
    @Schema(description = "推荐商品数量（商城中新品、推荐列表显示的商品数量）")
    private String maxRecommendProductCount;

    @ConfigItemField(SettingsEnum.MAX_SUB_ADMINISTRATOR)
    @Schema(description = "最大管理员数（当前店铺可配置的最大子管理员数）")
    private String maxSubAdministrator;

    @ConfigItemField(SettingsEnum.SHOP_PRODUCT_NEED_CHECK)
    @Schema(description = "是否需要审核商品；0-否，1-是")
    private Integer shopProductNeedCheck;
    @ConfigItemField(SettingsEnum.STORE_POST_ALLOCATION_STATUS)
    @Schema(description = "商品分配后状态")
    private Integer storePostAllocationStatus;
    @ConfigItemField(SettingsEnum.STORE_INDEPENDENT_GOODS)
    @Schema(description = "门店独立商品")
    private Integer storeIndependentGoods;
    @ConfigItemField(SettingsEnum.STORE_ASSIGN_PRODUCT_NAME)
    @Schema(description = "分配商品名称")
    private Integer storeAssignProductName;
    @ConfigItemField(SettingsEnum.STORE_ASSIGN_PRODUCT_PRICE)
    @Schema(description = "分配商品价格")
    private Integer storeAssignProductPrice;
    @ConfigItemField(SettingsEnum.STORE_ASSIGN_PRODUCT_IMAGE)
    @Schema(description = "门店可修改商品主图、详情页、相册 0不允许，1允许")
    private Integer storeAssignProductImage;
    @ConfigItemField(SettingsEnum.STORE_USE_SOLO_PRODUCT_STOCK)
    @Schema(description = "库存模式 0不使用门店独立库存，1使用门店独立库存")
    private Integer storeUseSoloProductStock;
    @ConfigItemField(SettingsEnum.STORE_USE_TOTAL_PRODUCT_STOCK)
    @Schema(description = "库存模式 0不使用平台库存，1使用平台库存")
    private Integer storeUseTotalProductStock;
    @ConfigItemField(SettingsEnum.STORE_CUSTOM_SUBMIT_SHIPPING_TYPE)
    @Schema(description = "客户下单配送方式 0自提优先，1配送优先 2同城配送优先")
    private Integer storeCustomSubmitShippingType;
    @ConfigItemField(SettingsEnum.STORE_SHOW_OTHER_CITY_STORE)
    @Schema(description = "显示地区其他门店 0不显示，1显示")
    private Integer storeShowOtherCityStore;
    @ConfigItemField(SettingsEnum.STORE_ASSIGN_DELIVERY_SERVICE_FEE)
    @Schema(description = "分配商品配送员服务费 0使用总部/区域统一服务费，1使用门店独立服务费")
    private Integer storeAssignDeliveryServiceFee;

    @ConfigItemField(SettingsEnum.STORE_ASSIGN_SERVER_SERVICE_FEE)
    @Schema(description = "分配商品服务员服务费 0使用总部/区域统一服务费，1使用门店独立服务费")
    private Integer storeAssignServerServiceFee;
}
