package com.tigshop.bean.vo.vendor.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 供应商库存日志项（字段对齐店铺端 ProductInventoryLogVO 以复用前端页面）
 * 数据来源 vendor_product_sku_stock_log（全量库存流水）
 *
 * @author Tigshop团队
 */
@Data
public class VendorInventoryLogVO {

    @Schema(description = "日志id")
    private Integer logId;

    @Schema(description = "商品id（vendor_product.id）")
    private Integer productId;

    @Schema(description = "规格id（vendor_product_sku.id）")
    private Long specId;

    @Schema(description = "剩余库存")
    private Integer number;

    @Schema(description = "原始库存量")
    private Integer oldNumber;

    @Schema(description = "1增2减")
    private Integer type;

    @Schema(description = "变化量")
    private Integer changeNumber;

    @Schema(description = "描述")
    private String desc;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "修改时间")
    private String addTime;

    @Schema(description = "货号（sku_sn）")
    private String sn;

    @Schema(description = "规格值")
    private String skuValue;

    @Schema(description = "操作人")
    private String addBy;
}
