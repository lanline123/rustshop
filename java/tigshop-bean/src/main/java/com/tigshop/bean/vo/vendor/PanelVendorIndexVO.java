package com.tigshop.bean.vo.vendor;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Tigshop团队
 * @create 2025/4/1 16:29
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "控制台数据")
public class PanelVendorIndexVO {
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
        @Schema(description = "待发货订单")
        private Long awaitShip;
        @Schema(description = "待结算订单")
        private Long awaitSettlement;
        @Schema(description = "待处理售后")
        private Long awaitAfterSale;
        @Schema(description = "售罄商品")
        private Long saleOutProductNum;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RealTimeData {
        @Schema(description = "今日结算总额")
        private BigDecimal todaySettlementAmount;
        @Schema(description = "今日结算订单数")
        private Long todaySettlementNum;
        @Schema(description = "在售商品数")
        private Long saleProductNum;
        @Schema(description = "断供商品数")
        private Long outageProductNum;

        @Schema(description = "账户余额")
        private BigDecimal accountBalance;
        @Schema(description = "待结算金额")
        private BigDecimal awaitSettlementAmount;
        @Schema(description = "关联店铺数")
        private Long bindShopNum;

    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PanelStatisticalData {
        private String[] horizontalAxis;
        //收益金额
        @Schema(description = "收益金额")
        private List<BigDecimal> longitudinalAxisIncome;
        //订单数量
        @Schema(description = "订单数量")
        private List<Long> longitudinalAxisOrderNum;
    }
}
