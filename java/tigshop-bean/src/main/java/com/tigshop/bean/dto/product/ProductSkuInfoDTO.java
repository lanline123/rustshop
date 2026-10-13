package com.tigshop.bean.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author wzh
 */
@Data
public class ProductSkuInfoDTO {
    @Schema(description = "规格ID")
    private Integer skuId;

    @Schema(description = "货品库存")
    private Integer skuStock;

    @Schema(description = "货品价格（快捷更新时可选）")
    private BigDecimal skuPrice;
}
