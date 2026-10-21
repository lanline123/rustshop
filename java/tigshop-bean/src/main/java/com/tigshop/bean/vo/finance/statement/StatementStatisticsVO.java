package com.tigshop.bean.vo.finance.statement;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 账单流水统计
 *
 * @author Tigshop项目组
 * @create 2025年08月12日 14:44
 */
@Data
@Builder
public class StatementStatisticsVO {
    @Schema(description = "日期")
    private String statementDate;

    @Schema(description = "收入金额")
    private BigDecimal income;

    @Schema(description = "支出金额")
    private BigDecimal expenditure;

    @Schema(description = "收入笔数")
    private Long incomeCount;

    @Schema(description = "支出笔数")
    private Long expenditureCount;

    @Schema(description = "账户")
    private Integer accountType;

    @Schema(description = "账户类型备注")
    private String accountTypeText;

}