import request from "@/utils/request";
import type { RequestList, RequestInfo, RequestShopList, ResponseShopList } from "@/types/store/areaManage"

//区域列表
export const areaStoreManagerList = (params: RequestList) => {
    return request<any>({
        url: "o2o/areaStoreManager/list",
        method: "get",
        params
    });
}
//区域绑定门店列表
export const areaStoreManagerShopList = (params: RequestShopList) => {
    return request<any>({
        url: "o2o/areaStoreManager/shopList",
        method: "get",
        params
    });
}

//区域新增编辑
export const updateAreaStoreManager = (operation: string, data: object) => {
    return request<RequestInfo>({
        url: "o2o/areaStoreManager/" + operation,
        method: "post",
        data,
    });
}

//区域详情
export const getAreaStoreManager = (action: string, params: object) => {
    return request<RequestInfo>({
        url: "o2o/areaStoreManager/" + action,
        method: "get",
        params
    });
}


// 删除区域
export const delAreaStoreManager = (data: object) => {
    return request({
        url: "o2o/areaStoreManager/del",
        method: "post",
        data,
    });
}

// 删除区域绑定门店
export const areaStoreManagerRemoveShop = (data: object) => {
    return request({
        url: "o2o/areaStoreManager/removeShop",
        method: "post",
        data,
    });
}


// 页面批量操作
export const batchSubmit = (type: string, data: object) => {
    return request({
        url: "o2o/areaStoreManager/batch",
        method: "post",
        data: {type, ...data},
    });
}