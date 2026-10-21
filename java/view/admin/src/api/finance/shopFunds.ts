import request from "@/utils/request";
import type { StatementListFilterParams, StatementListFilterState } from "@/types/finance/shopFunds.d"
// 获取商品分类列表
export const getStatementList = (params: StatementListFilterParams) => {
    return request<StatementListFilterState>({
        url: "finance/statement/getStatementList",
        method: "get",
        params,
    });
}


// 导出报表
export const exportStatement = (params: object) => {
    return request({
        url: "finance/statement/exportStatement",
        method: "get",
        responseType: "arraybuffer",
        params
    })
}