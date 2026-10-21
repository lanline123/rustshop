package com.tigshop.bean.model.finance;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 退款日志表
 */
@TableName(value ="paylog_refund")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaylogRefund implements Serializable {

    @Schema(description = "主键")
    @TableId(type = IdType.AUTO)
    private Integer paylogRefundId;

    @Schema(description = "支付日志id")
    private Integer paylogId;

    @Schema(description = "退款单号")
    private String refundSn;

    @Schema(description = "操作备注")
    private String paylogDesc;

    @Schema(description = "订单编号")
    private Integer orderId;

    @Schema(description = "退款金额")
    private BigDecimal refundAmount;

    @Schema(description = "添加时间")
    private Long addTime;

    @Schema(description = "操作人员id")
    private Integer adminId;

    @Schema(description = "支付方式code，比如wechat")
    private String payCode;

    @Schema(description = "处理状态 0申请中 1成功")
    private Integer status;

    @Schema(description = "通知时间")
    private Long notifyTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}