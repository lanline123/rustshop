package com.tigshop.bean.model.product;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;

/**
 * 商品调拨详情
 * @TableName product_stock_allocate_record_detail
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "商品调拨详情")
@TableName("product_stock_allocate_record_detail")
@ToString
public class ProductStockAllocateRecordDetail implements Serializable {
    /**
     * 调拨详情主键
     */
    @TableId(type = IdType.AUTO)
    private Integer allocateDetailId;

    /**
     * 调拨单主键
     */
    @Schema(description = "调拨单主键")
    private Integer allocateId;

    /**
     * 商品id
     */
    @Schema(description = "商品id")
    private Integer productId;

    /**
     * 商品sku
     */
    @Schema(description = "商品sku")
    private Integer skuId;

    /**
     * 调拨数量
     */
    @Schema(description = "调拨数量")
    private Integer quantity;

    @Schema(description = "入库数量")
    private Integer stockInQuantity;


    @Schema(description = "出库数量")
    private Integer stockOutQuantity;
}