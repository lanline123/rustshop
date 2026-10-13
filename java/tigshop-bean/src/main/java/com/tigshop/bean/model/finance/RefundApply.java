package com.tigshop.bean.model.finance;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 退款申请model
 *
 * @author kidd
 * @create 2025/7/7
 */
@Data
@TableName("refund_apply")
@Schema(description = "退款申请")
public class RefundApply {

    @TableId(type = IdType.AUTO)
    @Schema(description = "退款申请ID")
    private Integer refundId;

    @Schema(description = "退款类型：1:订单,2:商品")
    private Integer refundType;

    @Schema(description = "订单ID")
    private Integer orderId;

    @Schema(description = "会员ID")
    private Integer userId;

    @Schema(description = "售后ID")
    private Integer aftersaleId;

    @Schema(description = "退款状态：0:未处理,1:处理中，2:已处理,3:取消,4:已拒绝")
    private Integer refundStatus;

    @Schema(description = "添加时间")
    private Long addTime;

    @Schema(description = "回复内容")
    private String refundNote;

    @Schema(description = "线上退款金额")
    private BigDecimal onlineBalance;

    @Schema(description = "线下退款金额")
    private BigDecimal offlineBalance;

    @Schema(description = "退款余额")
    private BigDecimal refundBalance;

    @Schema(description = "线上金额是否到账:0 无需处理1处理中 2已处理")
    private Integer isOnline;

    @Schema(description = "线下金额是否到账:0 无需处理1处理中 2已处理")
    private Integer isOffline;

    @Schema(description = "打款凭证")
    private String paymentVoucher;

    @Schema(description = "余额是否到账:0 无需处理1处理中 2已处理")
    private Integer isReceive;

    @Schema(description = "线上退款的关联ID")
    private Integer paylogRefundId;

    @Schema(description = "店铺ID")
    private Integer shopId;
}
