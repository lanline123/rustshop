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
 * 供应商商品出入库记录（单头），镜像店铺端 product_stock_record，按 vendor_id 隔离
 *
 * @author Tigshop团队
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("vendor_product_stock_record")
@Schema(description = "供应商商品出入库记录")
public class VendorProductStockRecord implements Serializable {

    @Schema(description = "主键")
    @TableId(type = IdType.AUTO)
    private Integer productStockId;

    @Schema(description = "单据编号")
    private String stockCode;

    @Schema(description = "类型（1=入库，2=出库）")
    private Integer inOrOut;

    @Schema(description = "11=采购入库，12=退货入库，10=其他入库，21=过期出库，22=订单出库，23=其他出库")
    private Integer type;

    @Schema(description = "创建时间")
    private Integer addTime;

    @Schema(description = "经办人")
    private String addBy;

    @Schema(description = "备注")
    private String comment;

    @Schema(description = "供应商id")
    private Integer vendorId;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
