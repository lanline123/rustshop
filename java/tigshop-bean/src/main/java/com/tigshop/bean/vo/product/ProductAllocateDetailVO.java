package com.tigshop.bean.vo.product;

import com.tigshop.bean.model.product.ProductStockAllocateRecordDetail;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ProductAllocateDetailVO extends ProductStockAllocateRecordDetail {

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "商品sku名称")
    private String skuName;

    @Schema(description = "商品单位")
    private String productUnit;

    @Schema(description = "商品图片")
    private String productPicThumb;
}
