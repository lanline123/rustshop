package com.tigshop.bean.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductStockPageVO {

    @Schema(description = "商品ID")
    private Integer productId;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "商品SKU名称")
    private String productSkuName;

    @Schema(description = "商品SKU ID")
    private Integer productSkuId;

    @Schema(description = "商品单位")
    private String productUnit;

    @Schema(description = "当前商品库存")
    private Integer currentProductStock;

    @Schema(description = "商品成本")
    private BigDecimal productCost;

    @Schema(description = "商品图片")
    private String productPicThumb;

    @Schema(description = "商品编码")
    private String productSn;

    @Schema(description = "条形码")
    private String productTsn;

}
