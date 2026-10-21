package com.tigshop.bean.param.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 供应商商品价格校验
 *
 * @author kidd
 * @since 2025/7/17 17:17
 */
@Data
public class VendorMaxPriceParam {

    @NotNull(message = "商品SKU ID不能为空")
    @Schema(description = "商品SKU ID")
    private Integer vendorProductSkuId;

}
