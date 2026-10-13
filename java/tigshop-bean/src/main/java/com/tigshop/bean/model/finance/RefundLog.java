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
 * 退款记录model
 *
 * @author kidd
 * @since 2025/7/7 13:32
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("refund_log")
@Schema(description = "退款记录")
public class RefundLog {

    @TableId(value = "log_id", type = IdType.AUTO)
    @Schema(description = "接口退款日志ID")
    private Integer logId;

    @Schema(description = "来自的退款申请ID")
    private Integer refundApplyId;

    @Schema(description = "退款类型：1：接口原路退回，2：余额退回，3：线下退回")
    private Integer refundType;

    @Schema(description = "接口退款时的支付方式code，比如wechat、alipay等")
    private String refundPayCode;

    @Schema(description = "接口退款时的支付流水号")
    private String transactionId;

    @Schema(description = "实际退款金额")
    private java.math.BigDecimal refundAmount;

    @Schema(description = "退款时间")
    private Long addTime;

    @Schema(description = "退款描述")
    private String description;

    @Schema(description = "退款会员ID")
    private Integer userId;

    @Schema(description = "退款的订单ID")
    private Integer orderId;
}