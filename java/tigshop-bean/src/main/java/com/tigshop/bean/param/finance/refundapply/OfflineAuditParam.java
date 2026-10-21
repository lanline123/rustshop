package com.tigshop.bean.param.finance.refundapply;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 确认线下转账参数
 *
 * @author kidd
 * @since 2025/5/7 13:51
 */
@Data
@Schema(description = "确认线下转账参数")
public class OfflineAuditParam {

    @Schema(description = "退款申请主键")
    private Long refundId;

    @Schema(description = "退款备注")
    private String refundNote;

    @Schema(description = "打款凭证")
    private String paymentVoucher;
}
