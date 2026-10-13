package com.tigshop.bean.model.finance;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 账期支付订单实体
 *
 * @author Tigshop团队
 * @create 2026-01-14
 */
@Data
@TableName("credit_order")
@Schema(description = "账期支付订单")
public class CreditOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "credit_order_id", type = IdType.AUTO)
    @Schema(description = "账期订单ID")
    private Integer creditOrderId;

    @Schema(description = "订单ID")
    private Integer orderId;

    @Schema(description = "订单号")
    private String orderSn;

    @Schema(description = "会员ID")
    private Integer userId;

    @Schema(description = "授信会员ID")
    private Integer creditUserId;

    @Schema(description = "账期支付金额（单位：元）")
    private BigDecimal creditAmount;

    @Schema(description = "结算状态：0-待结算，1-已结算")
    private Integer settlementStatus;

    @Schema(description = "结算记录ID")
    private Integer settlementId;

    @Schema(description = "回款流水单号")
    private String settlementSn;

    @Schema(description = "下单时间")
    private Long orderTime;

    @Schema(description = "结算时间")
    private Long settlementTime;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "更新时间")
    private Long updateTime;
}
