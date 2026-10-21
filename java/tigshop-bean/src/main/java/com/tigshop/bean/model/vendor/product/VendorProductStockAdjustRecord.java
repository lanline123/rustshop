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
 * 供应商商品库存调整记录，镜像店铺端 product_stock_adjust_record，按 vendor_id 隔离
 *
 * @author Tigshop团队
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("vendor_product_stock_adjust_record")
@Schema(description = "供应商商品库存调整记录")
public class VendorProductStockAdjustRecord implements Serializable {

    @Schema(description = "主键")
    @TableId(type = IdType.AUTO)
    private Integer adjustRecordId;

    @Schema(description = "供应商商品id（vendor_product.id）")
    private Integer vendorProductId;

    @Schema(description = "供应商商品规格id（vendor_product_sku.id）")
    private Long vendorProductSkuId;

    @Schema(description = "调整类型（1=增量更新，0=覆盖更新）")
    private Integer adjustType;

    @Schema(description = "调整数量")
    private Integer quantity;

    @Schema(description = "调整前库存")
    private Integer changeBeforeQuantity;

    @Schema(description = "调整后库存")
    private Integer changeAfterQuantity;

    @Schema(description = "操作账号")
    private String addBy;

    @Schema(description = "创建时间")
    private Long addTime;

    @Schema(description = "供应商id")
    private Integer vendorId;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
