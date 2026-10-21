import request from "@/utils/request";
import type { StatementStatisticsRequest, StatementStatisticsResponse } from "@/types/finance/statementDownload.d"

// 对账单下载列表
export const getStatementStatisticsList = (params: StatementStatisticsRequest) => {
    return request<StatementStatisticsResponse[]>({
        url: "finance/statement/getStatementStatisticsList",
        method: "get",
        params,
    });
}

export const getStatementConfig = () => {
    return request<any>({
        url: "finance/statement/getStatementQueryConfig",
        method: "get",
    });
}


// 导出对账单
export const exportStatement = (params: object) => {
    return request({
        url: "finance/statement/exportStatementStatistics",
        method: "get",
        responseType: "arraybuffer",
        params
    })
}