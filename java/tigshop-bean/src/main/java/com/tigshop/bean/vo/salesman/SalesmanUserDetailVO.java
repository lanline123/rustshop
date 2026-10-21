package com.tigshop.bean.vo.salesman;

import com.tigshop.bean.model.salesman.Salesman;
import com.tigshop.bean.model.salesman.SalesmanCustomer;
import com.tigshop.bean.model.salesman.SalesmanOrder;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.common.utils.TigUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 分销员详细
 *
 * @author kidd
 * @create 2025/6/23
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分销员")
public class SalesmanUserDetailVO {

    // *** Salesman ***

    @Schema(description = "分销员ID")
    private Integer salesmanId;

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "分销员等级")
    private Integer level;

    @Schema(description = "分销员组ID")
    private Integer groupId;

    @Schema(description = "上级分销员ID")
    private Integer pid;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "销售金额")
    private BigDecimal saleAmount;

    // *** Other ***

    @Schema(description = "添加时间")
    private String addTime;

    @Schema(description = "分销员配置")
    private SalesmanConfigVO salesmanConfig;

    @Schema(description = "分销员等级名称")
    private String levelName;

    @Schema(description = "条件")
    private SalesmanConfigVO.Level.Condition condition;

    @Schema(description = "订单信息")
    private Order order;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "订单信息")
    public static class Order {

        @Schema(description = "今日订单总金额")
        private BigDecimal todaySum;

        @Schema(description = "所有订单总金额")
        private BigDecimal allSum;

        @Schema(description = "待结算订单总金额")
        private BigDecimal waitCpsSum;

        @Schema(description = "今日订单数量")
        private Long todayCount;

        @Schema(description = "所有订单数量")
        private Long allCount;

        @Schema(description = "今日用户数量")
        private Long todayUserCount;

        @Schema(description = "所有用户数量")
        private Long allUserCount;

        @Schema(description = "自购订单总金额")
        private BigDecimal selfBuySum;

        public Order(List<SalesmanOrder> salesmanOrders, List<SalesmanCustomer> salesmanCustomers, BigDecimal selfBuySum) {
            this.todaySum = salesmanOrders.stream()
                    .filter(order -> order.getStatus() != 2)
                    .filter(order -> order.getAddTime() >= StringUtils.getTodayBeginTime())
                    .map(SalesmanOrder::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            this.allSum = salesmanOrders.stream()
                    .filter(order -> order.getStatus() != 2)
                    .map(SalesmanOrder::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            this.waitCpsSum = salesmanOrders.stream()
                    .filter(order -> Constants.NO.equals(order.getStatus()))
                    .map(SalesmanOrder::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            this.todayCount = salesmanOrders.stream()
                    .filter(order -> order.getAddTime() >= StringUtils.getTodayBeginTime())
                    .count();

            this.allCount = (long) salesmanOrders.size();

            this.todayUserCount = salesmanCustomers.stream()
                    .filter(customer -> customer.getAddTime() >= StringUtils.getTodayBeginTime())
                    .count();

            this.allUserCount = (long) salesmanCustomers.size();

            this.selfBuySum = selfBuySum;

        }
    }

    public SalesmanUserDetailVO(Salesman salesman, SalesmanConfigVO salesmanConfig, List<SalesmanOrder> salesmanOrders, List<SalesmanCustomer> salesmanCustomers, BigDecimal selfBuySum) {
        this.salesmanId = salesman.getSalesmanId();
        this.userId = salesman.getUserId();
        this.level = salesman.getLevel();
        this.groupId = salesman.getGroupId();
        this.pid = salesman.getPid();
        this.shopId = salesman.getShopId();
        this.saleAmount = salesman.getSaleAmount();

        this.addTime = TigUtils.handelTime(salesman.getAddTime());

        this.salesmanConfig = salesmanConfig;
        for (SalesmanConfigVO.Level level : salesmanConfig.getLevel()) {
            if (level.getId().equals(salesman.getLevel())) {
                this.levelName = level.getName();
                this.condition = level.getCondition();
            }
        }

        this.order = new Order(salesmanOrders, salesmanCustomers, selfBuySum);
    }

}
