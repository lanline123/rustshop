package com.tigshop.bean.model.finance;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 退款申请与退款流水关联表（多对多关系）
 *
 * @author Tigshop团队
 * @create 2025年11月27日
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("refund_apply_paylog_refund")
@Schema(description = "退款申请与退款流水关联表")
public class RefundApplyPaylogRefund {

    @TableId(type = IdType.AUTO)
    @Schema(description = "关联ID")
    private Integer refundApplyPaylogRefundId;

    @Schema(description = "退款申请ID")
    private Integer refundApplyId;

    @Schema(description = "退款流水ID")
    private Integer paylogRefundId;
}

