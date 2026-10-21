import request from "@/utils/request";
import type { BrandFilterParams, BrandFilterResult, BrandFormState, BrandSearchFilterResult } from "@/types/product/brand.d";

/**
 * 获取商品品牌列表
 * @description 分页获取商品品牌数据
 * @param params - 筛选和分页参数
 * @returns 品牌列表
 */
export const getBrandList = (params: BrandFilterParams) => {
    return request<BrandFilterResult>({
        url: "product/brand/list",
        method: "get",
        params
    });
};
/**
 * 获取品牌审核列表
 * @description 获取待审核的商品品牌列表
 * @param params - 筛选参数
 * @returns 待审核品牌列表
 */
export const getAuditBrandList = (params: BrandFilterParams) => {
    return request<BrandFilterResult>({
        url: "product/brand/auditList",
        method: "get",
        params
    });
};

/**
 * 搜索品牌
 * @description 获取品牌搜索结果
 * @returns 品牌搜索结果
 */
export const getBrandSearch = () => {
    return request<BrandSearchFilterResult>({
        url: "product/brand/search",
        method: "get"
    });
};
/**
 * 获取待审核品牌数量
 * @description 获取等待审核的品牌数量
 * @returns 待审核数量
 */
export const getBrandAuditWaitNum = () => {
    return request<any>({
        url: "product/brand/auditWaitNum",
        method: "get"
    });
};

/**
 * 删除品牌
 * @description 删除指定品牌
 * @param data - 品牌ID
 * @returns 操作结果
 */
export const delBrand = (data: object) => {
    return request({
        url: "product/brand/del",
        method: "post",
        data
    });
};
/**
 * 获取品牌详情
 * @description 获取品牌的完整信息
 * @param action - 操作类型
 * @param params - 品牌ID等参数
 * @returns 品牌详情
 */
export const getBrand = (action: string, params: object) => {
    return request<BrandFormState>({
        url: "product/brand/" + action,
        method: "get",
        params
    });
};

/**
 * 更新品牌
 * @description 创建或更新品牌信息
 * @param operation - 操作类型（create/update）
 * @param data - 品牌数据
 * @returns 操作结果
 */
export const updateBrand = (operation: string, data: object) => {
    return request({
        url: "product/brand/" + operation,
        method: "post",
        data
    });
};

/**
 * 批量操作品牌
 * @description 批量处理品牌
 * @param type - 操作类型
 * @param data - 操作数据
 * @returns 操作结果
 */
export const batchSubmit = (type: string, data: object) => {
    return request({
        url: "product/brand/batch",
        method: "post",
        data: {type, ...data}
    });
};

/**
 * 更新品牌首字母
 * @description 批量更新所有品牌的首字母索引
 * @returns 操作结果
 */
export const updateFirstWorld = () => {
    return request({
        url: "product/brand/updateFirstWord",
        method: "post"
    });
};
/**
 * 更新品牌字段
 * @description 更新品牌的单个字段值
 * @param data - 字段更新数据
 * @returns 操作结果
 */
export const updateBrandField = (data: object) => {
    return request({
        url: "product/brand/updateField",
        method: "post",
        data
    });
};

/**
 * 审核品牌
 * @description 审核商户申请的品牌（通过/拒绝）
 * @param data - 审核数据
 * @returns 操作结果
 */
export const auditBrand = (data: object) => {
    return request({
        url: "product/brand/audit",
        method: "post",
        data
    });
};
