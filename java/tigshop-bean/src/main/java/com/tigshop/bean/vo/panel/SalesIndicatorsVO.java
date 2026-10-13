package com.tigshop.bean.vo.panel;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author Tigshop团队
 * @create 2025/4/1 17:00
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "销售指标")
public class SalesIndicatorsVO {
    @Schema(description = "订单总数")
    private Long orderNum;
    @Schema(description = "订单商品总数")
    private Long orderProductNum;
    @Schema(description = "订单总金额")
    private BigDecimal orderTotalAmount;
    @Schema(description = "会员总数")
    private Long userNum;
    @Schema(description = "消费会员总数")
    private Long consumerMembershipNum;
    @Schema(description = "人均消费数")
    private BigDecimal capitaConsumption;
    @Schema(description = "访问数 -- 商品点击数")
    private Long clickCount;
    @Schema(description = "访问转化率")
    private BigDecimal clickRate;
    @Schema(description = "订单转化率")
    private BigDecimal orderRate;
    @Schema(description = "消费会员比率")
    private BigDecimal consumerMembershipRate;
    @Schema(description = "购买率")
    private BigDecimal purchaseRate;
}
