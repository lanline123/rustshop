package com.tigshop.bean.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ProductStockAllocateOperationRecordVO {
    @Schema(description = "操作类型")
    private String operationType;

    @Schema(description = "操作时间")
    private String addTime;

    @Schema(description = "操作人")
    private String addBy;

    @Schema(description = "审核驳回原因")
    private String reviewRejectedReason;
}
