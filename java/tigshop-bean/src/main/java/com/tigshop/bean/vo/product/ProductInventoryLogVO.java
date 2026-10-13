package com.tigshop.bean.vo.product;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tigshop.common.config.TimestampToDateSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 库存日志VO
 *
 * @author kidd
 * @create 2025/7/3
 */
@Data
@Schema(description = "库存日志参数")
public class ProductInventoryLogVO {

    // *** ProductInventoryLog ***

    @Schema(description = "日志ID")
    private Integer logId;

    @Schema(description = "商品ID")
    private Integer productId;

    @Schema(description = "规格ID")
    private Integer specId;

    @Schema(description = "数量")
    private Integer number;

    @Schema(description = "旧数量")
    private Integer oldNumber;

    @Schema(description = "类型; 1-增，2-减")
    private Integer type;

    @Schema(description = "变化数量")
    private Integer changeNumber;

    @Schema(description = "描述")
    private String desc;

    @Schema(description = "店铺ID")
    private Integer shopId;

    // *** Product ***

    @Schema(description = "商品名称")
    private String productName;

    // *** Other ***

    @JsonSerialize(using = TimestampToDateSerializer.class)
    @Schema(description = "添加时间")
    private String addTime;

    @Schema(description = "编号,如果是sku的话,就是sku编号,如果是单规格的话,就是商品编号")
    private String sn;

    @Schema(description = "格式: 规格: 规格值, 规格: 规格值")
    private String skuValue;
}