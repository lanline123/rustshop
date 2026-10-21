import request from "@/utils/request";
import type {salesmanCustomerFilterResult,promoterFilterResult, promoterDetailsFilterResult, promoterDetailsFormResult} from "@/types/salesman/promoter.d";
// 获取分销员列表列表
export const getsalesmanList = (params: object) => {
    return request<promoterFilterResult>({
        url: "salesman/salesman/list",
        method: "get",
        params,
    });
}
// 分销员列表页面批量操作
export const batchSubmit = (type: string, data: object) => {
    return request({
        url: "salesman/salesman/batch",
        method: "post",
        data: {type, ...data},
    });
}
// 分销员列表页面删除项
export const delsalesman = (data: object) => {
    return request({
        url: "salesman/salesman/del",
        method: "post",
        data,
    });
}
// 分销员列表页面更新项
export const updatesalesmanFiled = (data: object) => {
    return request({
        url: "salesman/salesman/updateField",
        method: "post",
        data,
    });
}
// 获取分销员列表详情
export const getsalesman = (action: string, params: object) => {
    return request<promoterDetailsFormResult>({
        url: "salesman/salesman/" + action,
        method: "get",
        params
    });
}
// 更新分销员列表
export const updatesalesman = (operation: string, data: object) => {
    return request({
        url: "salesman/salesman/" + operation,
        method: "post",
        data
    });
}
// 获取分销员列表配置项
export const getsalesmanConfig = () => {
    return request({
        url: "salesman/salesman/config",
        method: "get"
    });
}
// 获取分销员明细
export const getsalesmanDetails = (params: object) => {
    return request<promoterDetailsFilterResult>({
        url: "salesman/salesman/statisticalDetails",
        method: "get",
        params
    });
}
export const exportsalesmanDetails = (params: object) => {
    return request({
        url: "salesman/salesman/statisticalDetails",
        method: "get",
        responseType: 'arraybuffer',
        params
    });
};
export const commissionDetails = (params: object) => {
    return request<any>({
        url: "salesman/salesman/commissionDetails",
        method: "get",
        params
    });
};
// 获取下拉分销员列表列表
export const selectsalesmanList = (params: object) => {
    return request<promoterFilterResult>({
        url: "salesman/salesman/salesmanList",
        method: "get",
        params,
    });
}

// 获取分销员客户列表列表
export const getsalesmanCustomerList = (params: object) => {
    return request<salesmanCustomerFilterResult>({
        url: "salesman/salesman/customerList",
        method: "get",
        params,
    });
}