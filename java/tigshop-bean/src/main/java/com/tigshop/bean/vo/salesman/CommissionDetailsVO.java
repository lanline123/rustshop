// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.vo.salesman;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.tigshop.bean.enums.salesman.SalesmanProductCommissionTypeEnum;
import com.tigshop.bean.model.salesman.SalesmanOrder;
import com.tigshop.common.constant.Constants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * @author Tigshop团队
 * @create 2025/4/2 10:12
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "佣金详情")
public class CommissionDetailsVO {

    @Schema(description = "分销员信息")
    private SalesmanVO item;

    @Schema(description = "佣金信息")
    private Commission commission;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "佣金数据")
    public static class Commission {

        @Schema(description = "产品交易金额")
        private BigDecimal productTransactionAmount;

        @Schema(description = "自动结算金额")
        private BigDecimal autoSettlementAmount;

        @Schema(description = "自动待结算金额")
        private BigDecimal autoWaitSettlementAmount;

        @Schema(description = "人工结算金额")
        private BigDecimal artificialSettlementAmount;

        @Schema(description = "人工待结算金额")
        private BigDecimal artificialWaitSettlementAmount;

        public Commission(List<SalesmanOrder> salesmanOrders, Integer level) {
            this.productTransactionAmount = salesmanOrders.stream()
                    .map(SalesmanOrder::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            this.autoSettlementAmount = BigDecimal.ZERO;
            this.autoWaitSettlementAmount = BigDecimal.ZERO;
            this.artificialSettlementAmount = BigDecimal.ZERO;
            this.artificialWaitSettlementAmount = BigDecimal.ZERO;

            for (SalesmanOrder salesmanOrder : salesmanOrders) {
                BigDecimal commissionAmount = computeCommission(salesmanOrder, level);
                SalesmanOrderVO.SalesmanSettingVO salesmanSetting = JSONUtil.toBean(salesmanOrder.getSalesmanSettlementData(), SalesmanOrderVO.SalesmanSettingVO.class);

                if (salesmanSetting.getSettlementType() == 1) {
                    // 自动结算
                    if (Constants.YES.equals(salesmanOrder.getStatus())) {
                        this.autoSettlementAmount = this.autoSettlementAmount.add(commissionAmount);
                    } else {
                        this.autoWaitSettlementAmount = this.autoWaitSettlementAmount.add(commissionAmount);
                    }
                } else {
                    // 人工结算
                    if (Constants.YES.equals(salesmanOrder.getStatus())) {
                        this.artificialSettlementAmount = this.artificialSettlementAmount.add(commissionAmount);
                    } else {
                        this.artificialWaitSettlementAmount = this.artificialWaitSettlementAmount.add(commissionAmount);
                    }
                }
            }
        }

        /**
         * 计算单笔 SalesmanOrder 对应的佣金
         */
        private BigDecimal computeCommission(SalesmanOrder salesmanOrder, Integer level) {
            BigDecimal orderAmount = salesmanOrder.getOrderAmount();
            SalesmanOrderVO.SalesmanProductDataVO salesmanProductData = JSONUtil.toBean(salesmanOrder.getSalesmanProductData(), SalesmanOrderVO.SalesmanProductDataVO.class);
            Integer commissionType = salesmanProductData.getCommissionType();

            if (CollUtil.isEmpty(salesmanProductData.getCommissionData())) {
                return BigDecimal.ZERO;
            }

            for (SalesmanOrderVO.SalesmanProductDataVO.CommissionDataVO commissionData : salesmanProductData.getCommissionData()) {
                for (SalesmanOrderVO.SalesmanProductDataVO.CommissionDataVO.LevelVO levelData : commissionData.getLevelArr()) {
                    if (level.equals(levelData.getLevel())) {
                        BigDecimal profitComposition = new BigDecimal(levelData.getRate());

                        if (commissionType == SalesmanProductCommissionTypeEnum.DEFAULT_RATE.getCode()
                                || commissionType == SalesmanProductCommissionTypeEnum.CUSTOM_RATE.getCode()) {
                            BigDecimal commissionRate = profitComposition.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                            return commissionRate.multiply(orderAmount);
                        } else {
                            return profitComposition;
                        }
                    }
                }
            }

            return BigDecimal.ZERO;
        }
    }

    public CommissionDetailsVO(SalesmanVO item, List<SalesmanOrder> salesmanOrders) {
        this.item = item;

        this.commission = new Commission(salesmanOrders, item.getLevel());
    }
}
