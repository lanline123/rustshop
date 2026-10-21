package com.tigshop.bean.vo.panel;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 新增会员趋势统计图
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Getter
@Setter
@Schema(description = "新增会员趋势统计图")
public class SaleDataVO {

    @Schema(description = "商品支付金额")
    private BigDecimal productPayment;

    @Schema(description = "商品支付金额增长率")
    private BigDecimal productPaymentGrowthRate;

    @Schema(description = "支付订单数")
    private Long productPaymentNum;

    @Schema(description = "支付订单数增长率")
    private BigDecimal productPaymentNumGrowthRate;

    @Schema(description = "商品退款金额")
    private BigDecimal productRefund;

    @Schema(description = "商品退款金额")
    private BigDecimal prevProductRefund;

    @Schema(description = "商品退款金额增长率")
    private BigDecimal productRefundGrowthRate;

    @Schema(description = "退款订单数量")
    private Long productRefundNum;

    @Schema(description = "退款订单数量增长率")
    private BigDecimal productRefundNumGrowthRate;

    @Schema(description = "充值金额")
    private BigDecimal rechargeAmount;

    @Schema(description = "充值金额增长率")
    private BigDecimal rechargeAmountGrowthRate;

    @Schema(description = "充值订单数")
    private Long rechargeNum;

    @Schema(description = "充值订单数增长率")
    private BigDecimal rechargeNumGrowthRate;

    @Schema(description = "营业额")
    private BigDecimal turnover;

    @Schema(description = "营业额增长率")
    private BigDecimal turnoverGrowthRate;

    @Schema(description = "余额支付金额")
    private BigDecimal balancePayment;

    @Schema(description = "余额支付金额增长率")
    private BigDecimal balancePaymentGrowthRate;

    @Schema(description = "余额支付订单数")
    private Long balancePaymentNum;

    @Schema(description = "余额支付订单数")
    private BigDecimal balancePaymentNumGrowthRate;
}
