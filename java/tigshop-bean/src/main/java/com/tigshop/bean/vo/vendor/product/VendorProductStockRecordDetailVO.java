package com.tigshop.bean.vo.vendor.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 供应商出入库单明细项（字段对齐店铺端 ProductStockRecordDetailVO）
 *
 * @author Tigshop团队
 */
@Data
public class VendorProductStockRecordDetailVO {

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "规格名称")
    private String productSkuName;

    @Schema(description = "单位")
    private String productUnit;

    @Schema(description = "出入库数量")
    private Integer quantity;

    @Schema(description = "商品缩略图")
    private String productPicThumb;
}
