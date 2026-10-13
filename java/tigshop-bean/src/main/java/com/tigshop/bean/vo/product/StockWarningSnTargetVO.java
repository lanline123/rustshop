package com.tigshop.bean.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 导入时按编码解析出的商品/SKU 行
 *
 * @author Tigshop团队
 */
@Data
@Schema(description = "按编码解析出的商品/SKU 行")
public class StockWarningSnTargetVO {

    /** 命中的编码：多规格是 product_sku.sku_sn，单规格是 product.product_sn */
    @Schema(description = "命中的编码")
    private String sn;

    @Schema(description = "商品ID")
    private Integer productId;

    @Schema(description = "SKU ID，商品编码命中时为 0")
    private Integer skuId;

    /** 1=命中规格编码（多规格），2=命中商品编码 */
    @Schema(description = "命中方式：1=规格编码，2=商品编码")
    private Integer matchType;

    /** 命中商品编码时该商品的规格数，用于区分单规格与多规格 */
    @Schema(description = "该商品的规格数量")
    private Integer skuCount;
}
