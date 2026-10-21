package com.tigshop.bean.model.vendor.product;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 供应商商品出入库记录明细，镜像店铺端 product_stock_record_detail
 *
 * @author Tigshop团队
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("vendor_product_stock_record_detail")
@Schema(description = "供应商商品出入库记录明细")
public class VendorProductStockRecordDetail implements Serializable {

    @Schema(description = "主键")
    @TableId(type = IdType.AUTO)
    private Integer recordDetailId;

    @Schema(description = "出入库单id")
    private Integer stockRecordId;

    @Schema(description = "供应商商品id（vendor_product.id）")
    private Integer vendorProductId;

    @Schema(description = "供应商商品规格id（vendor_product_sku.id）")
    private Long vendorProductSkuId;

    @Schema(description = "出入库数量")
    private Integer quantity;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
