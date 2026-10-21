import type {AdminRoleFilterParams, AdminRoleFilterResult, AdminRoleFormState} from "@/types/authority/adminRole.d";
import request from "@/utils/request";

/**
 * 获取管理员角色列表
 * @description 分页获取后台角色数据
 * @param params - 筛选和分页参数
 * @returns 角色列表
 */
export const getAdminRoleList = (params: AdminRoleFilterParams) => {
    return request<AdminRoleFilterResult>({
        url: "authority/adminRole/list",
        method: "get",
        params,
    });
}

/**
 * 批量操作角色
 * @description 批量处理角色
 * @param type - 操作类型
 * @param data - 操作数据
 * @returns 操作结果
 */
export const batchSubmit = (type: string, data: object) => {
    return request({
        url: "authority/adminRole/batch",
        method: "post",
        data: {type, ...data}
    });
}

/**
 * 删除角色
 * @description 删除指定角色
 * @param data - 角色ID
 * @returns 操作结果
 */
export const delAdminRole = (data: object) => {
    return request({
        url: "authority/adminRole/del",
        method: "post",
        data,
    });
}

/**
 * 获取角色详情
 * @description 获取角色的完整信息和权限配置
 * @param action - 操作类型
 * @param params - 角色ID等参数
 * @returns 角色详情
 */
export  const getAdminRole = (action: string, params: object) => {
    return request<AdminRoleFormState>({
        url: "authority/adminRole/" + action,
        method: "get",
        params
    });
}

/**
 * 更新角色
 * @description 创建或更新角色
 * @param operation - 操作类型（create/update）
 * @param data - 角色数据
 * @returns 操作结果
 */
export const updateAdminRole = (operation: string, data: object) => {
    return request({
        url: "authority/adminRole/" + operation,
        method: "post",
        data
    });
}

/**
 * 更新角色字段
 * @description 更新角色的单个字段值
 * @param data - 字段更新数据
 * @returns 操作结果
 */
export const updateAdminRoleField = (data:object) => {
    return request({
        url: "authority/adminRole/updateField",
        method: "post",
        data
    });
}
