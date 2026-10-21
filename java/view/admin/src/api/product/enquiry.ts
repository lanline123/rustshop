import request from "@/utils/request";
import type { PriceInquiryFilterParams, PriceInquiryFilterResult, PriceInquiryFormState } from "@/types/product/enquiry.d";

// 获取商品分类列表
export const getPriceInquiryList = (params: PriceInquiryFilterParams) => {
    return request<PriceInquiryFilterResult>({
        url: "product/priceInquiry/list",
        method: "get",
        params
    });
};

// 删除
export const delPriceInquiry = (data: object) => {
    return request({
        url: "product/priceInquiry/del",
        method: "post",
        data
    });
};
//获取分类详情
export const getPriceInquiry = (action: string, params: object) => {
    return request<PriceInquiryFormState>({
        url: "product/priceInquiry/" + action,
        method: "get",
        params
    });
};


// 批量操作
export const batchSubmit = (type: string, data: object) => {
    // Java API 当前仅提供单条删除接口，批量删除逐条复用已有契约，
    // 避免请求不存在的 /priceInquiry/batch 导致“资源未找到”。
    if (type === "del") {
        const ids = Array.isArray((data as { ids?: unknown }).ids) ? (data as { ids: unknown[] }).ids : [];
        return Promise.all(ids.map((id) => delPriceInquiry({ id })));
    }
    return request({
        url: "product/priceInquiry/batch",
        method: "post",
        data: {type, ...data}
    });
};

// 回复
export const updatePriceInquiryReply = (data: object) => {
    return request({
        url: "product/priceInquiry/reply",
        method: "post",
        data
    });
};
