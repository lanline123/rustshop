package com.tigshop.bean.model.product;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@TableName("product_stock_allocate_operation_record")
public class ProductStockAllocateOperationRecord {

    @TableId(type = IdType.AUTO)
    @Schema(description = "调拨操作记录ID")
    private Integer allocateOperationId;

    @Schema(description = "调拨单ID")
    private Integer allocateId;

    @Schema(description = "操作类型")
    private String operationType;

    @Schema(description = "操作时间")
    private Long addTime;

    @Schema(description = "操作人")
    private String addBy;

    @Schema(description = "审核驳回原因")
    private String reviewRejectedReason;
}
