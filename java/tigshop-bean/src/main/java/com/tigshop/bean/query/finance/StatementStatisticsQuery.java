package com.tigshop.bean.query.finance;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 统计日期查询
 *
 * @author Tigshop项目组
 * @create 2025年08月12日 14:49
 */
@Data
public class StatementStatisticsQuery {
    @Schema(description = "日期:yyyy-mm/yyyy/yyyy")
    private String statementDate;

    @Schema(description = "日期类型：day/month/year")
    private String statementDateType;

    @Schema(description = "账户类型")
    private Integer accountType;

    @Schema(description = "来源:shop/vendor")
    private String source;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "供应商id")
    private Integer vendorId;
}