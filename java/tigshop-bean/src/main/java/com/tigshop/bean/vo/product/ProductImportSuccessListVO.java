package com.tigshop.bean.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductImportSuccessListVO {
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

    @Schema(description = "出入库数量")
    private Integer quantity;

    @Schema(description = "商品图片")
    private String productPicThumb;

    @Schema(description = "当前库存")
    private Integer currentProductStock;

}
