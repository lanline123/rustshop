export interface StatementStatisticsRequest {
    /**
     * 账户类型
     */
    accountType?: number | string;
    /**
     * 日期:yyyy-mm/yyyy/yyyy
     */
    statementDate?: string;
    /**
     * 日期类型：day/month/year
     */
    statementDateType?: string;
    source?: string;
    [property: string]: any;
}


/**
 * 账单流水统计
 *
 * StatementStatisticsVO
 */
export interface StatementStatisticsResponse {
    /**
     * 账户
     */
    accountType?: string;
    /**
     * 支出金额
     */
    expenditure?: number;
    /**
     * 支出笔数
     */
    expenditureCount?: number;
    /**
     * 收入金额
     */
    income?: number;
    /**
     * 收入笔数
     */
    incomeCount?: number;
    /**
     * 日期
     */
    statementDate?: string;
    [property: string]: any;
}