import request from "@/utils/request";
import type { ApplyFilterParams, ApplyFilterResult, ApplyFormState } from "@/types/adminMerchant/apply";

/**
 * 获取商户入驻申请列表
 * @description 分页获取商户入驻申请数据
 * @param params - 筛选和分页参数
 * @returns 入驻申请列表
 */
export const getApplyList = (params: ApplyFilterParams) => {
    return request<ApplyFilterResult>({
        url: "merchant/apply/list",
        method: "get",
        params
    });
};

/**
 * 获取入驻申请配置
 * @description 获取商户入驻相关的配置项
 * @returns 配置信息
 */
export const getApplyConfig = () => {
    return request<any[]>({
        url: "merchant/apply/config",
        method: "get"
    });
};

/**
 * 删除入驻申请
 * @description 删除指定入驻申请
 * @param data - 申请ID
 * @returns 操作结果
 */
export const delApply = (data: object) => {
    return request({
        url: "merchant/apply/del",
        method: "post",
        data
    });
};
/**
 * 获取入驻申请详情
 * @description 获取入驻申请的完整信息
 * @param action - 操作类型
 * @param params - 申请ID等参数
 * @returns 申请详情
 */
export const getApply = (action: string, params: object) => {
    return request<ApplyFormState>({
        url: "merchant/apply/" + action,
        method: "get",
        params
    });
};

/**
 * 审核入驻申请
 * @description 审核商户入驻申请（通过/拒绝）
 * @param data - 审核数据
 * @returns 操作结果
 */
export const auditApply = ( data: object) => {
    return request({
        url: "merchant/apply/audit",
        method: "post",
        data
    });
};

/**
 * 批量操作入驻申请
 * @description 批量处理入驻申请
 * @param type - 操作类型
 * @param data - 操作数据
 * @returns 操作结果
 */
export const batchSubmit = (type: string, data: object) => {
    return request({
        url: "merchant/apply/batch",
        method: "post",
        data: {type, ...data}
    });
};

