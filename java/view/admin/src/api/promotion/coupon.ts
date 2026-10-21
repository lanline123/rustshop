import request from "@/utils/request";
import type {
    CouponFilterResult,
    CouponFilterState,
    CouponFormState,
    CouponIssueUserParams,
    CouponIssueUserResult,
    CouponManualIssueParam,
    CouponManualIssuePreviewVO,
    CouponManualIssueResultVO
} from "@/types/promotion/coupon";

/**
 * 获取优惠券列表
 * @description 分页获取优惠券数据
 * @param params - 筛选和分页参数
 * @returns 优惠券列表
 */
export const getCouponList = (params: object) => {
    return request<CouponFilterResult>({
        url: "promotion/coupon/list",
        method: "get",
        params
    });
};

/**
 * 获取有效优惠券列表
 * @description 获取当前有效的优惠券（用于抽奖等活动配置）
 * @param params - 筛选参数
 * @returns 有效优惠券列表
 */
export const getValidCouponList = (params: object) => {
    return request<CouponFilterResult>({
        url: "promotion/coupon/validList",
        method: "get",
        params
    });
};

/**
 * 获取优惠券配置
 * @description 获取优惠券相关的配置项
 * @returns 配置信息
 */
export const getCouponConfig = () => {
    return request<any>({
        url: "promotion/coupon/config",
        method: "get"
    });
};
/**
 * 批量操作优惠券
 * @description 批量处理优惠券（启用/禁用等）
 * @param type - 操作类型
 * @param data - 操作数据
 * @returns 操作结果
 */
export const batchSubmit = (type: string, data: object) => {
    return request({
        url: "promotion/coupon/batch",
        method: "post",
        data: { type, ...data }
    });
};

/**
 * 删除优惠券
 * @description 删除指定优惠券
 * @param data - 优惠券ID
 * @returns 操作结果
 */
export const delCoupon = (data: object) => {
    return request({
        url: "promotion/coupon/del",
        method: "post",
        data
    });
};
/**
 * 更新优惠券字段
 * @description 更新优惠券的单个字段值
 * @param data - 字段更新数据
 * @returns 操作结果
 */
export const updateCouponFiled = (data: object) => {
    return request({
        url: "promotion/coupon/updateField",
        method: "post",
        data
    });
};

/**
 * 获取优惠券详情
 * @description 获取优惠券的完整信息
 * @param action - 操作类型
 * @param params - 优惠券ID等参数
 * @returns 优惠券详情
 */
export const getCoupon = (action: string, params: object) => {
    return request<CouponFormState>({
        url: "promotion/coupon/" + action,
        method: "get",
        params
    });
};

/**
 * 按 ids 批量获取优惠券
 * @description 用于装修等场景一次性回显多张优惠券
 * @param ids - 优惠券ID数组
 * @returns 优惠券列表（已删除的券不返回，顺序不保证与入参一致）
 */
export const getCouponListByIds = (ids: number[]) => {
    return request<CouponFilterState[]>({
        url: "promotion/coupon/listByIds",
        method: "get",
        params: { ids: ids.join(",") }
    });
};
/**
 * 更新优惠券
 * @description 创建或更新优惠券
 * @param operation - 操作类型（create/update）
 * @param data - 优惠券数据
 * @returns 操作结果
 */
export const updateCoupon = (operation: string, data: object) => {
    return request({
        url: "promotion/coupon/" + operation,
        method: "post",
        data
    });
};

/**
 * 手动发放会员搜索
 * @description 查询可发放优惠券的会员列表
 * @param params - 搜索参数
 * @returns 会员分页数据
 */
export const getCouponIssueUsers = (params: CouponIssueUserParams) => {
    return request<CouponIssueUserResult>({
        url: "promotion/coupon/issue/users",
        method: "get",
        params
    });
};

/**
 * 手动发放预览
 * @description 统计本次发放的有效会员和失败明细
 * @param data - 发放参数
 * @returns 预览结果
 */
export const previewCouponIssue = (data: CouponManualIssueParam) => {
    return request<CouponManualIssuePreviewVO>({
        url: "promotion/coupon/issue/preview",
        method: "post",
        data
    });
};

/**
 * 正式发放优惠券
 * @description 执行优惠券手动发放
 * @param data - 发放参数
 * @returns 发放结果
 */
export const sendCouponIssue = (data: CouponManualIssueParam) => {
    return request<CouponManualIssueResultVO>({
        url: "promotion/coupon/issue/send",
        method: "post",
        data
    });
};

/**
 * 获取优惠券发放批次列表
 * @param params - 分页及筛选参数
 */
export const getCouponIssueBatchList = (params: object) => {
    return request<any>({
        url: "promotion/coupon/issue/batch",
        method: "get",
        params
    });
};

/**
 * 获取优惠券发放日志列表
 * @param params - 分页及筛选参数
 */
export const getCouponIssueLogList = (params: object) => {
    return request<any>({
        url: "promotion/coupon/issue/log",
        method: "get",
        params
    });
};
