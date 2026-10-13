import request from "@/utils/request";
import type { AdminUserKefuFilterParams, AdminUserKefuFilterResult, AdminUserKefuFormState, KefuRole } from "@/types/im/kefu";

/**
 * 获取客服人员列表
 * @description 分页获取当前店铺客服人员数据
 * @param params - 筛选和分页参数
 * @returns 客服人员列表
 */
export const getAdminUserKefuList = (params: AdminUserKefuFilterParams) => {
    return request<AdminUserKefuFilterResult>({
        url: "im/adminUserKefu/list",
        method: "get",
        params
    });
};

/**
 * 获取客服人员详情
 * @description 按 admin_user_shop 主键获取客服信息
 * @param action - 操作类型（detail/info）
 * @param params - id 等参数
 * @returns 客服人员详情
 */
export const getAdminUserKefu = (action: string, params: object) => {
    return request<AdminUserKefuFormState>({
        url: "im/adminUserKefu/" + action,
        method: "get",
        params
    });
};

/**
 * 获取客服角色列表
 * @returns 客服角色列表
 */
export const getAdminUserKefuRoles = () => {
    return request<KefuRole[]>({
        url: "im/adminUserKefu/roles",
        method: "get"
    });
};

/**
 * 新增/更新客服人员
 * @param operation - 操作类型（create/update）
 * @param data - 客服人员数据
 * @returns 操作结果
 */
export const updateAdminUserKefu = (operation: string, data: object) => {
    return request({
        url: "im/adminUserKefu/" + operation,
        method: "post",
        data
    });
};

/**
 * 删除客服人员
 * @param data - { id } admin_user_shop 主键
 * @returns 操作结果
 */
export const delAdminUserKefu = (data: object) => {
    return request({
        url: "im/adminUserKefu/del",
        method: "post",
        data
    });
};

/**
 * 批量操作客服人员
 * @param type - 操作类型（del）
 * @param data - 操作数据（ids）
 * @returns 操作结果
 */
export const batchAdminUserKefu = (type: string, data: object) => {
    return request({
        url: "im/adminUserKefu/batch",
        method: "post",
        data: { type, ...data }
    });
};

/**
 * 更新客服人员字段
 * @description 快捷更新（启用/停用）
 * @param data - { id, field, val }
 * @returns 操作结果
 */
export const updateAdminUserKefuField = (data: object) => {
    return request({
        url: "im/adminUserKefu/updateField",
        method: "post",
        data
    });
};
