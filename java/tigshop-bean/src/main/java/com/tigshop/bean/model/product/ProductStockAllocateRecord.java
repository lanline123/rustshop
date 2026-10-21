package com.tigshop.bean.model.product;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 库存调拨表
 * @TableName product_stock_allocate_record
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "库存调拨表")
@Data
@TableName(value ="product_stock_allocate_record")
public class ProductStockAllocateRecord implements Serializable {
    /**
     * 调拨主键
     */
    @Schema(description = "调拨主键")
    @TableId(type = IdType.AUTO)
    private Integer allocateId;

    /**
     * 调拨单号
     */
    @Schema(description = "调拨单号")
    private String allocateCode;

    /**
     * 出库组织
     */
    @Schema(description = "出库组织")
    private Integer outboundOrganizationId;

    /**
     * 入库组织
     */
    @Schema(description = "入库组织")
    private Integer warehousingOrganizationId;

    /**
     * 单据状态（1=待审核，2待调出，3=已完成，4=已驳回，5=已作废）	
     */
    @Schema(description = "单据状态（1=待审核，2待调出，3=待调入,4=已完成，5=已驳回，6=已作废）")
    private Integer status;

    /**
     * 经办人
     */
    @Schema(description = "经办人")
    private String addBy;

    /**
     * 创建时间	
     */
    @Schema(description = "创建时间	")
    private Long addTime;

    /**
     * 备注
     */
    @Schema(description = "入库备注")
    private String stockInComment;

    @Schema(description = "出库备注")
    private String stockOutComment;

    @Schema(description = "调拨备注")
    private String stockAllocateComment;
}