import request from "@/utils/request";
import type {customerFilterResult} from "@/types/salesman/customerTransactions.d";
// 获取分销员列表列表
export const getcustomerTransactionList = (params: object) => {
    return request<customerFilterResult>({
        url: "salesman/customerTransaction/list",
        method: "get",
        params,
    });
}


export const exportCustomerTransactionList = (params: object) => {
    return request({
        url: "salesman/customerTransaction/list",
        method: "get",
        responseType: 'arraybuffer',
        params
    });
};