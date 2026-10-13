package com.tigshop.bean.param.finance;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 进件审核参数（平台端：通过 / 拒绝）
 *
 * @author Tigshop团队
 */
@Data
@Schema(description = "进件审核参数")
public class MerchantApplicationAuditParam {

    @NotNull(message = "缺少进件申请ID")
    @Schema(description = "进件申请ID")
    private Long id;

    @Schema(description = "驳回原因（拒绝时必填）")
    private String rejectReason;
}
