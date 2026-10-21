package com.tigshop.bean.vo.vendor.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 供应商库存调整记录项（字段对齐店铺端 ProductStockAdjustRecordDetailVO）
 *
 * @author Tigshop团队
 */
@Data
public class VendorProductStockAdjustRecordDetailVO {

    @Schema(description = "调整类型（1=增量，0=覆盖）")
    private Integer adjustType;

    @Schema(description = "调整数量")
    private Integer quantity;

    @Schema(description = "操作账号")
    private String addBy;

    @Schema(description = "创建时间")
    private String addTime;

    @Schema(description = "调整前库存")
    private Integer changeBeforeQuantity;

    @Schema(description = "调整后库存")
    private Integer changeAfterQuantity;
}
