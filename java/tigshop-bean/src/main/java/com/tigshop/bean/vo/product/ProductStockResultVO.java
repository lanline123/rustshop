package com.tigshop.bean.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductStockResultVO {

    @Schema(description = "商品ID")
    private Integer  productId;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "商品总库存")
    private Integer productStock;

    @Schema(description = "商品店铺ID")
    private Integer shopId;

    @Schema(description = "商品SKU ID")
    private Integer skuId;

    @Schema(description = "商品SKU名称")
    private String productSkuName;

    @Schema(description = "商品单位")
    private String productUnit;

    @Schema(description = "当前商品库存")
    private Integer productSkuStock;

    @Schema(description = "商品成本")
    private BigDecimal productCost;

    @Schema(description = "商品SKU成本")
    private BigDecimal productSkuCost;

    @Schema(description = "平台分配给门店商品ID")
    private Integer storeProductId;

    @Schema(description = "平台分配给门店商品库存")
    private Integer storeProductStock;

    @Schema(description = "平台分配给门店的商品的门店ID")
    private Integer storeShopId;

    @Schema(description = "平台分配给门店的商品SKU ID")
    private Integer storeProductSkuId;

    @Schema(description = "平台分配给门店的商品SKU库存")
    private Integer storeSkuStock;

    @Schema(description = "调拨ID")
    private Integer allocateId;

    @Schema(description = "调拨的商品ID")
    private Integer allocateProductId;

    @Schema(description = "调拨的商品SKU ID")
    private Integer allocateProductSkuId;

    @Schema(description = "调拨的商品库存")
    private Integer allocateProductStock;

    @Schema(description = "商品图片")
    private String productPicThumb;

    @Schema(description = "商品编号")
    private String productSn;

    @Schema(description = "条形码")
    private String productTsn;

    @Schema(description = "成本价")
    private BigDecimal costPrice;

    @Schema(description = "商品类型")
    private Integer productType;

    @Schema(description = "电子卡组ID")
    private Integer cardGroupId;
}
