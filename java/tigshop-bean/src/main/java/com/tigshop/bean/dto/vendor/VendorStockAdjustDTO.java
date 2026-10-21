package com.tigshop.bean.dto.vendor;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 供应商库存调整参数
 *
 * @author Tigshop团队
 */
@Data
public class VendorStockAdjustDTO {

    @Schema(description = "供应商商品id（vendor_product.id）")
    private Integer productId;

    @Schema(description = "供应商商品规格id（vendor_product_sku.id）")
    private Long skuId;

    @Schema(description = "调整类型（1=增量更新，0=覆盖更新）")
    private Integer adjustType;

    @Schema(description = "调整数量")
    private Integer quantity;
}
