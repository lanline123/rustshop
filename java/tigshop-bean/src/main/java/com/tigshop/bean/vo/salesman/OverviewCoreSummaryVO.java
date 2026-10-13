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

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author Tigshop团队
 * @create 2025/4/2 10:12
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "核心汇总")
public class OverviewCoreSummaryVO {

    @Schema(description = "新增分销员数")
    private Long newSalesmanCount;

    @Schema(description = "分销员销售额")
    private BigDecimal salesmanAmount;

    @Schema(description = "成交客户数")
    private Long customNum;

    @Schema(description = "支出佣金")
    private BigDecimal salesmanCommission;
}
