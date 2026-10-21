import request from "@/utils/request";
import type {SalesmanConfigFormState} from "@/types/salesman/promoteMode.d";
// 获取分销模式详情
export const getSalesmanConfig = () => {
    return request<SalesmanConfigFormState>({
        url: "salesman/config/detail?code=salesmanConfig",
        method: "get"
    });
}
// 分销模式保存配置
export const saveSalesmanConfig = (data: object) => {
    return request({
        url: "salesman/config/save?code=salesmanConfig",
        method: "post",
        data,
    });
}