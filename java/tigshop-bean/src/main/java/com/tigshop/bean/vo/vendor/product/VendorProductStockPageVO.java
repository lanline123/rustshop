package com.tigshop.bean.vo.vendor.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 供应商库存列表项（字段对齐店铺端 ProductStockPageVO 以复用前端页面）
 *
 * @author Tigshop团队
 */
@Data
public class VendorProductStockPageVO {

    @Schema(description = "商品id（vendor_product.id）")
    private Integer productId;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "规格名称")
    private String productSkuName;

    @Schema(description = "规格id（vendor_product_sku.id）")
    private Long productSkuId;

    @Schema(description = "单位")
    private String productUnit;

    @Schema(description = "当前库存")
    private Integer currentProductStock;

    @Schema(description = "成本（供货价）")
    private BigDecimal productCost;

    @Schema(description = "商品缩略图")
    private String productPicThumb;

    @Schema(description = "商品货号（sku_sn）")
    private String productSn;

    @Schema(description = "商品条形码（sku_tsn）")
    private String productTsn;
}
