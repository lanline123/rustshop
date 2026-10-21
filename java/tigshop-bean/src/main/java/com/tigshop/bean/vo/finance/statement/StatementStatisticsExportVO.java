package com.tigshop.bean.vo.finance.statement;

import cn.idev.excel.annotation.ExcelProperty;
import cn.idev.excel.annotation.write.style.ColumnWidth;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tigshop.common.core.BigDecimalSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 账单流水统计
 *
 * @author Tigshop项目组
 * @create 2025年08月12日 14:44
 */
@Data
@ColumnWidth(22)
public class StatementStatisticsExportVO {
    @Schema(description = "日期")
    @ExcelProperty("日期")
    private String statementDate;

    @Schema(description = "收入金额")
    @ExcelProperty("收入金额")
    @JsonSerialize(using = BigDecimalSerializer.class)
    private BigDecimal income;

    @Schema(description = "支出金额")
    @ExcelProperty("支出金额")
    @JsonSerialize(using = BigDecimalSerializer.class)
    private BigDecimal expenditure;

    @Schema(description = "收入笔数")
    @ExcelProperty("收入笔数")
    private Long incomeCount;

    @Schema(description = "支出笔数")
    @ExcelProperty("支出笔数")
    private Long expenditureCount;

    @Schema(description = "账户类型备注")
    @ExcelProperty("账户类型备注")
    private String accountTypeText;

}