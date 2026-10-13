package com.tigshop.bean.dto.vendor;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 审核提现参数
 *
 * @author Tigshop团队
 */
@Data
@Schema(description = "审核提现参数")
public class VendorWithdrawAuditDTO {

    @Schema(description = "id")
    private Integer id;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "备注")
    private String auditRemark;
}
