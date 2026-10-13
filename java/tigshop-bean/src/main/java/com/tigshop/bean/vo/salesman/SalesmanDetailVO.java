package com.tigshop.bean.vo.salesman;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 销售详情
 *
 * @author kidd
 * @since 2025/4/3 13:54
 */
@Schema(description = "销售详情")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesmanDetailVO {

    @Schema(description = "销售详情")
    private SalesmanVO salesmanVO;

    @Schema(description = "统计数据")
    private Statistical statistical;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Statistical {

        @Schema(description = "销售金额")
        private BigDecimal saleAmount;

        @Schema(description = "订单数")
        private Long orderNum;

        @Schema(description = "客户数")
        private Long customerNum;

        @Schema(description = "邀请数")
        private Long inviteNum;

        @Schema(description = "佣金金额")
        private BigDecimal commissionAmount;

        @Schema(description = "产品佣金金额")
        private BigDecimal productCommissionAmount;
    }
}
