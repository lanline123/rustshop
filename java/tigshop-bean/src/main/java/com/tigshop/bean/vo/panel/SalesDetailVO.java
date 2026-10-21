package com.tigshop.bean.vo.panel;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * 销售详情统计信息
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Getter
@Setter
@Schema(description = "销售详情统计信息")
public class SalesDetailVO {

    @Schema(description = "销售数据")
    private SalesData salesData;

    @Schema(description = "销售统计数据")
    private SalesStatisticsData salesStatisticsData;

    @Data
    @Schema(description = "销售数据")
    public static class SalesData {

        @Schema(description = "商品浏览量")
        private Integer productView;

        @Schema(description = "商品浏览量增长率")
        private BigDecimal productViewGrowthRate;

        @Schema(description = "商品访客数")
        private Integer productVisitor;

        @Schema(description = "商品访客数增长率")
        private BigDecimal productVisitorGrowthRate;

        @Schema(description = "订单数量")
        private Integer orderNum;

        @Schema(description = "订单数量增长率")
        private String orderNumGrowthRate;

        @Schema(description = "支付金额")
        private BigDecimal paymentAmount;

        @Schema(description = "支付金额增长率")
        private BigDecimal paymentAmountGrowthRate;

        @Schema(description = "退款金额")
        private BigDecimal refundAmount;

        @Schema(description = "退款金额增长率")
        private String refundAmountGrowthRate;

        @Schema(description = "退款数量")
        private Integer refundQuantity;

        @Schema(description = "退款数量增长率")
        private String refundQuantityGrowthRate;
    }

    @Data
    @Schema(description = "销售统计数据")
    public static class SalesStatisticsData {

        @Schema(description = "横轴数据（日期）")
        private List<String> horizontalAxis;

        @Schema(description = "支付金额纵轴数据")
        private List<BigDecimal> longitudinalAxisPaymentAmount;

        @Schema(description = "退款金额纵轴数据")
        private List<BigDecimal> longitudinalAxisRefundAmount;

        @Schema(description = "商品浏览量纵轴数据")
        private List<BigDecimal> longitudinalAxisProductView;

        @Schema(description = "商品访客数纵轴数据")
        private List<BigDecimal> longitudinalAxisProductVisitor;
    }
}
