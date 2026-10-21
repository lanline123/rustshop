package com.tigshop.bean.param.finance.refundapply;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author wzh
 */
@Data
public class RefundOfflineAuditParam {

    @NotNull(message = "退款申请ID不能为空")
    @Schema(description = "退款申请ID")
    private Integer refundId;

}
