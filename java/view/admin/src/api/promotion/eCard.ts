import request from "@/utils/request";
import type {
    ECardGroupFilterParams,
    ECardGroupFilterResult,
    ECardGroupFormState,
    ECardFilterParams,
    ECardFilterResult,
    ECardFormState
} from "@/types/promotion/eCard";

// 获取电子卡券
export const getECardList = (params: ECardFilterParams) => {
    return request<ECardFilterResult>({
        url: "product/eCard/list",
        method: "get",
        params,
    });
};
// 电子卡券页面批量操作
export const batchSubmitECard = (type: string, data: object) => {
    return request({
        url: "product/eCard/batch",
        method: "post",
        data: {type, ...data}
    });
};
// 电子卡券页面删除项
export const delECard = (data: object) => {
    return request({
        url: "product/eCard/del",
        method: "post",
        data,
    });
};
// 电子卡券页面更新项
export const updateECardFiled = (data: object) => {
    return request({
        url: "product/eCard/updateField",
        method: "post",
        data,
    });
};
// 获取卡券详情
export const getECard = (action: string, params: object) => {
    return request<ECardFormState>({
        url: "product/eCard/" + action,
        method: "get",
        params,
    });
};

// 更新卡券详情
export const updateECard = (operation: string, data: object) => {
    return request({
        url: "product/eCard/" + operation,
        method: "post",
        data
    });
}





// 获取电子卡券组
export const getECardGroupList = (params: ECardGroupFilterParams) => {
    return request<ECardGroupFilterResult>({
        url: "product/eCardGroup/list",
        method: "get",
        params,
    });
};

// 获取电子卡券组(不分页)
export const getECardGroupCardList = () => {
    return request({
        url: "product/eCardGroup/cardList",
        method: "get",
    });
};
// 电子卡券组页面批量操作
export const batchSubmitECardGroup = (type: string, data: object) => {
    return request({
        url: "product/eCardGroup/batch",
        method: "post",
        data: {type, ...data}
    });
};
// 电子卡券组页面删除项
export const delECardGroup = (data: object) => {
    return request({
        url: "product/eCardGroup/del",
        method: "post",
        data,
    });
};
// 电子卡券组页面更新项
export const updateECardGroupFiled = (data: object) => {
    return request({
        url: "product/eCardGroup/updateField",
        method: "post",
        data,
    });
};
// 获取卡券组详情
export const getECardGroup = (action: string, params: object) => {
    return request<ECardGroupFormState>({
        url: "product/eCardGroup/" + action,
        method: "get",
        params,
    });
};

// 更新卡券组详情
export const updateECardGroup = (operation: string, data: object) => {
    return request({
        url: "product/eCardGroup/" + operation,
        method: "post",
        data
    });
}


// 批量导入电子卡券
export const importECardGroup = (data: object) => {
    return request({
        url: "product/eCardGroup/import",
        method: "post",
        headers: {
            'Content-Type': 'multipart/form-data'
        },
        data
    });
}

// 导出订单
export const ECardGroupExport = (params: object) => {
    return request({
        url: "product/eCardGroup/list",
        method: "get",
        responseType: "arraybuffer",
        params
    })
}
