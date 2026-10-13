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
@TableName("product_stock_record_detail")
public class ProductStockRecordDetail {

    @Schema(description = "商品出入库记录明细ID")
    @TableId(type = IdType.AUTO)
    private Integer recordDetailId;

    @Schema(description = "商品出入库记录ID")
    private Integer stockRecordId;

    @Schema(description = "商品ID")
    private Integer productId;

    @Schema(description = "商品SKU")
    private Integer skuId;

    @Schema(description = "商品数量")
    private Integer quantity;
}
