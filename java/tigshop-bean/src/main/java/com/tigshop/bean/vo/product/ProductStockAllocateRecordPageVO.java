package com.tigshop.bean.vo.product;

import com.tigshop.bean.model.product.ProductStockAllocateRecord;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ProductStockAllocateRecordPageVO {

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
    private String addTime;

    private String comment;
    /**
     * 备注
     */
    @Schema(description = "入库备注")
    private String stockInComment;

    @Schema(description = "出库备注")
    private String stockOutComment;

    @Schema(description = "出库组织名称")
    private String outboundOrganizationName;

    @Schema(description = "入库组织名称")
    private String warehousingOrganizationName;

    @Schema(description = "是否需要审核")
    private boolean isCanAudit = false;
}
