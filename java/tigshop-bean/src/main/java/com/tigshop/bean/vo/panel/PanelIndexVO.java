package com.tigshop.bean.vo.panel;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Tigshop团队
 * @create 2025/4/1 16:29
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "控制台数据")
public class PanelIndexVO {
    @Schema(description = "控制台数据")
    private ConsoleData consoleData;
    @Schema(description = "实时数据")
    private RealTimeData realTimeData;
    @Schema(description = "统计图表")
    private PanelStatisticalData panelStatisticalData;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ConsoleData {
        @Schema(description = "待付款订单")
        private Long awaitPay;
        @Schema(description = "待发货的订单")
        private Long awaitShip;
        @Schema(description = "待售后的订单")
        private Long awaitAfterSale;
        @Schema(description = "待回复的商品评价")
        private Long awaitComment;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RealTimeData {
        @Schema(description = "支付金额")
        private BigDecimal todayOrderAmount;
        @Schema(description = "昨日支付金额")
        private BigDecimal yesterdayOrderAmount;
        @Schema(description = "支付金额增长率")
        private BigDecimal orderAmountGrowthRate;
        @Schema(description = "访客数")
        private Integer todayVisitNum;
        @Schema(description = "昨日访客数")
        private Integer yesterdayVisitNum;
        @Schema(description = "访客数增长率")
        private BigDecimal visitGrowthRate;
        @Schema(description = "支付买家数")
        private Long todayBuyerNum;
        @Schema(description = "昨日支付买家数")
        private Long yesterdayBuyerNum;
        @Schema(description = "支付买家数增长率")
        private BigDecimal buyerGrowthRate;
        @Schema(description = "浏览量")
        private Integer todayViewNum;
        @Schema(description = "昨日浏览量")
        private Integer yesterdayViewNum;
        @Schema(description = "浏览量增长率")
        private BigDecimal viewGrowthRate;
        @Schema(description = "支付订单数")
        private Long todayOrderNum;
        @Schema(description = "昨日支付订单数")
        private Long yesterdayOrderNum;
        @Schema(description = "支付订单数增长率")
        private BigDecimal orderGrowthRate;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PanelStatisticalData {
        private String[] horizontalAxis;
        private List<BigDecimal> longitudinalAxisAccess;
        private List<BigDecimal> longitudinalAxisOrderNum;
        private List<BigDecimal> longitudinalAxisOrderAmount;
    }
}
