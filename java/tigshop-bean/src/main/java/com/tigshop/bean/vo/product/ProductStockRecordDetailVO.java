package com.tigshop.bean.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ProductStockRecordDetailVO {
    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "商品sku名称")
    private String productSkuName;

    @Schema(description = "商品sku单位")
    private String productUnit;

    @Schema(description = "商品数量")
    private Integer quantity;

    @Schema(description = "商品图片")
    private String productPicThumb;
}
