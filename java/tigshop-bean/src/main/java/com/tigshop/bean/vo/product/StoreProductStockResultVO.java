package com.tigshop.bean.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "商品库存结果VO")
public class StoreProductStockResultVO {

    @Schema(description = "商品ID")
    private Integer productId;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "商品单位")
    private String productUnit;

    @Schema(description = "商品缩略图")
    private String productPicThumb;

    @Schema(description = "商品编码")
    private String productSn;

    @Schema(description = "条形码")
    private String productTsn;

    @Schema(description = "商品类型")
    private Integer productType;

    @Schema(description = "卡券组ID")
    private Integer cardGroupId;

    @Schema(description = "成本价")
    private java.math.BigDecimal costPrice;

    @Schema(description = "商品库存")
    private Integer productStock;

    @Schema(description = "SKU ID")
    private Integer skuId;

    @Schema(description = "商品SKU名称")
    private String productSkuName;

    @Schema(description = "SKU库存")
    private Integer skuStock;

    @Schema(description = "门店商品ID")
    private Integer storeProductId;

    @Schema(description = "门店店铺ID")
    private Integer storeShopId;

    @Schema(description = "门店库存")
    private Integer storeStock;

    @Schema(description = "门店SKU库存")
    private Integer storeSkuStock;

    @Schema(description = "当前商品库存")
    private Integer currentProductStock;

    @Schema(description = "是否是平台分配商品")
    private Integer distributeProduct;

    @Schema(description = "平台分配门店SKU ID")
    private Integer storeProductSkuId;
    @Schema(description = "平台分配给门店SKU ID")
    private Integer storeSkuId;
}
