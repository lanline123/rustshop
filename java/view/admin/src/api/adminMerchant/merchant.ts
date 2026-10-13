import request from "@/utils/request";
import type { MerchantFilterParams, MerchantFilterResult, MerchantFormState, MerchantSearchFilterResult } from "@/types/adminMerchant/merchant";

/**
 * 获取商户列表
 * @description 分页获取已入驻商户数据
 * @param params - 筛选和分页参数
 * @returns 商户列表
 */
export const getMerchantList = (params: MerchantFilterParams) => {
    return request<MerchantFilterResult>({
        url: "merchant/merchant/list",
        method: "get",
        params
    });
};

/**
 * 搜索商户
 * @description 获取商户搜索结果
 * @returns 商户搜索结果
 */
export const getMerchantSearch = () => {
    return request<MerchantSearchFilterResult>({
        url: "merchant/merchant/search",
        method: "get"
    });
};

/**
 * 删除商户
 * @description 删除指定商户
 * @param data - 商户ID
 * @returns 操作结果
 */
export const delMerchant = (data: object) => {
    return request({
        url: "merchant/merchant/del",
        method: "post",
        data
    });
};
/**
 * 获取商户详情
 * @description 获取商户的完整信息
 * @param action - 操作类型
 * @param params - 商户ID等参数
 * @returns 商户详情
 */
export const getMerchant = (action: string, params: object) => {
    return request<MerchantFormState>({
        url: "merchant/merchant/" + action,
        method: "get",
        params
    });
};

/**
 * 更新商户
 * @description 创建或更新商户信息
 * @param operation - 操作类型（create/update）
 * @param data - 商户数据
 * @returns 操作结果
 */
export const updateMerchant = (operation: string, data: object) => {
    return request({
        url: "merchant/merchant/" + operation,
        method: "post",
        data
    });
};

/**
 * 上传营业执照
 * @description 营业执照公示专用上传，不写入公共相册
 * @param file - 待上传的营业执照图片
 * @returns 带水印的图片地址
 */
export const uploadMerchantLicense = (file: File) => {
    const data = new FormData();
    data.append("file", file);
    return request<string>({
        url: "merchant/merchant/uploadLicense",
        method: "post",
        data,
        headers: { "Content-Type": "multipart/form-data" }
    });
};

/**
 * 批量操作商户
 * @description 批量处理商户（启用/禁用等）
 * @param type - 操作类型
 * @param data - 操作数据
 * @returns 操作结果
 */
export const batchSubmit = (type: string, data: object) => {
    return request({
        url: "merchant/merchant/batch",
        method: "post",
        data: {type, ...data}
    });
};

/**
 * 更新商户字段
 * @description 更新商户的单个字段值
 * @param data - 字段更新数据
 * @returns 操作结果
 */
export const updateMerchantField = (data: object) => {
    return request({
        url: "merchant/merchant/updateField",
        method: "post",
        data
    });
};
