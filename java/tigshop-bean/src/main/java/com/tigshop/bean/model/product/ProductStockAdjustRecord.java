package com.tigshop.bean.model.product;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "商品出入库记录明细表")
@TableName("product_stock_adjust_record")
public class ProductStockAdjustRecord {

    @Schema(description = "商品调整记录ID")
    @TableId(type = IdType.AUTO)
    private Integer adjustRecordId;

    @Schema(description = "商品ID")
    private Integer productId;

    @Schema(description = "商品规格ID")
    private Integer skuId;

    @Schema(description = "调整类型")
    private Integer adjustType;

    @Schema(description = "调整数量")
    private Integer quantity;

    @Schema(description = "创建人")
    private String addBy;

    @Schema(description = "创建时间")
    private Long addTime;

    @Schema(description = "商城ID")
    private Integer shopId;

    @Schema(description = "调整前数量")
    private Integer changeBeforeQuantity;

    @Schema(description = "调整后数量")
    private Integer changeAfterQuantity;

}
