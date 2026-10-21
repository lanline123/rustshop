package com.tigshop.bean.vo.vendor.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 供应商库存出入库明细项（字段对齐店铺端 ProductStockDetailVO）
 *
 * @author Tigshop团队
 */
@Data
public class VendorStockDetailVO {

    @Schema(description = "出入库类型/描述")
    private String desc;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "规格名称")
    private String productSkuName;

    @Schema(description = "出入库数量")
    private Integer quantity;

    @Schema(description = "商品缩略图")
    private String productPicThumb;

    @Schema(description = "1增2减")
    private Integer inOrOut;
}
