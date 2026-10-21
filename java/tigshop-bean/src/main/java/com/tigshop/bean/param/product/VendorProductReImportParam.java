package com.tigshop.bean.param.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class VendorProductReImportParam {


    @Schema(description = "供应商商品ID")
    private Long vendorProductId;

    @Schema(description = "商品ID")
    private Integer productId;
}
