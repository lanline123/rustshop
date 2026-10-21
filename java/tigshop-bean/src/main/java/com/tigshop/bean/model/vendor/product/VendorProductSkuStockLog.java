package com.tigshop.bean.model.vendor.product;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tigshop.common.core.entity.BasePO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/**
 * 供货商商品规格库存日志
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value = "vendor_product_sku_stock_log")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorProductSkuStockLog extends BasePO implements Serializable {

    @Schema(description = "供货商商品ID")
    private Long vendorProductId;

    @Schema(description = "供应商商品规格 ID")
    private Long vendorProductSkuId;

    @Schema(description = "操作类型；1-增加，2-减少")
    private Integer operationType;

    @Schema(description = "变更前库存")
    private Integer beforeStock;

    @Schema(description = "变动数量")
    private Integer changeNum;

    @Schema(description = "变更后库存")
    private Integer afterStock;

    @Schema(description = "业务类型；1-商品新增，2-库存编辑")
    private Integer bizType;

    @Schema(description = "业务备注")
    private String bizRemark;

    @Schema(description = "供应商 ID")
    private Integer vendorId;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}