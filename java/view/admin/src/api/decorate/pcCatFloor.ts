import request from "@/utils/request";
import type {PcCatFloorFilterParams, PcCatFloorFilterResult, PcCatFloorFormState} from "@/types/decorate/pcCatFloor.d";
// 获取商品分类列表
export const getPcCatFloorList = (params: PcCatFloorFilterParams) => {
    return request<PcCatFloorFilterResult>({
        url: "decorate/pcCatFloor/list",
        method: "get",
        params,
    });
}

// 商品分类页面批量操作
export const batchSubmit = (type: string, data: object) => {
    return request({
        url: "decorate/pcCatFloor/batch",
        method: "post",
        data: {type, ...data},
    });
}

// 商品分类页面更新项
export const updatePcCatFloorFiled = (data: object) => {
    return request({
        url: "decorate/pcCatFloor/updateField",
        method: "post",
        data,
    });
}

export const delPcCatFloor = (data: object) => {
    return request({
        url: "decorate/pcCatFloor/del",
        method: "post",
        data,
    });
}

export  const getPcCatFloor = (action: string, params: object) => {
    return request<PcCatFloorFormState>({
        url: "decorate/pcCatFloor/" + action,
        method: "get",
        params
    });
}

export const updatePcCatFloor = (operation: string, data: object) => {
    return request({
        url: "decorate/pcCatFloor/" + operation,
        method: "post",
        data
    });
}

export const updatePcCatFloorField = (data:object) => {
    return request({
        url: "decorate/pcCatFloor/updateField",
        method: "post",
        data
    });
}
export const clearPcCatFloorCache = () => {
    return request({
        url: "decorate/pcCatFloor/clearCache",
        method: "post"
    });
}

