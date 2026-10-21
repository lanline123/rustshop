package com.tigshop.bean.vo.finance;

import com.tigshop.bean.vo.order.OrderItemVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 用户账期支付信息详情VO
 * （需求文档：docs/账期支付/1、需求文档.md -> 用户账期支付信息详情）
 */
@Data
@Schema(description = "用户账期支付信息详情")
public class UserCreditDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "信用额度")
    private BigDecimal creditLimit;

    @Schema(description = "已使用额度")
    private BigDecimal usedAmount;

    @Schema(description = "可用额度")
    private BigDecimal availableAmount;

    @Schema(description = "结算账期类型：1-月结，2-季结")
    private Integer settlementType;

    @Schema(description = "账期天数：30或90")
    private Integer settlementDays;

    @Schema(description = "剩余账期天数")
    private Integer remainingDays;

    @Schema(description = "未结算订单列表")
    private List<UserCreditOrderVO> pendingOrders;

    @Schema(description = "已结算订单列表")
    private List<UserCreditOrderVO> settledOrders;

    @Data
    @Schema(description = "账期订单列表项")
    public static class UserCreditOrderVO implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "订单ID")
        private Integer orderId;

        @Schema(description = "订单号")
        private String orderSn;

        @Schema(description = "下单时间")
        private String orderTime;

        @Schema(description = "账期付款金额（剩余待结算金额口径）")
        private BigDecimal creditAmount;

        @Schema(description = "共多少件商品")
        private Integer totalQuantity;

        @Schema(description = "结算状态")
        private Integer settlementStatus;

        @Schema(description = "结算状态文本")
        private String settlementStatusText;

        @Schema(description = "状态：0-正常，1-逾期")
        private Integer riskStatus;

        @Schema(description = "状态文本：正常/逾期")
        private String riskStatusText;

        @Schema(description = "商品信息")
        private List<OrderItemVO> items;
    }
}

