package com.tigshop.bean.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ProductStockDetailVO {

    @Schema(description = "出入库类型")
    private String desc;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "商品SKU名称")
    private String productSkuName;

    @Schema(description = "出入库数量")
    private Integer quantity;

    @Schema(description = "商品图片")
    private String productPicThumb;

    @Schema(description = "出入库类型")
    private Integer inOrOut;
}
