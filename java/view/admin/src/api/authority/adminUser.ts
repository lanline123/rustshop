import request from "@/utils/request";
import {AdminUserFilterResult, AdminUserFormState, AdminUserFilterParams} from "@/types/authority/adminUser"

/**
 * 获取管理员列表
 * @description 分页获取后台管理员数据
 * @param params - 筛选和分页参数
 * @returns 管理员列表
 */
export const getAdminUserList = (params: AdminUserFilterParams) => {
    return request<AdminUserFilterResult>({
        url: "authority/adminUser/list",
        method: "get",
        params,
    });
}
/**
 * 搜索会员
 * @description 通过手机号搜索会员
 * @param params - 手机号
 * @returns 会员信息
 */
export const getAdminUserSearch = (params:any) => {
    return request<any>({
        url: "user/user/search",
        method: "get",
        params: {
            mobile: params
        }
    });
}

/**
 * 获取管理员配置
 * @description 获取管理员相关的配置项
 * @returns 配置信息
 */
export const getAdminUserConfig = () => {
    return request<any>({
        url: "authority/adminUser/config",
        method: "get"
    });
}

/**
 * 删除管理员
 * @description 删除指定管理员账号
 * @param data - 管理员ID
 * @returns 操作结果
 */
export const delAdminUser= (data: object) => {
    return request({
        url: "authority/adminUser/del",
        method: "post",
        data,
    });
}

/**
 * 获取管理员详情
 * @description 获取管理员的完整信息
 * @param action - 操作类型
 * @param params - 管理员ID等参数
 * @returns 管理员详情
 */
export const getAdminUser  = (action: string, params: object) => {
    return request<AdminUserFormState>({
        url: "authority/adminUser/" + action,
        method: "get",
        params
    });
}

/**
 * 更新管理员
 * @description 创建或更新管理员
 * @param operation - 操作类型（create/update）
 * @param data - 管理员数据
 * @returns 操作结果
 */
export const updateAdminUser = (operation: string, data: object) => {
    return request({
        url: "authority/adminUser/" + operation,
        method: "post",
        data
    });
}

/**
 * 批量操作管理员
 * @description 批量处理管理员
 * @param type - 操作类型
 * @param data - 操作数据
 * @returns 操作结果
 */
export const batchSubmit = (type: string, data: object) => {
    return request({
        url: "authority/adminUser/batch",
        method: "post",
        data: {type, ...data}
    });
}
/**
 * 更新管理员字段
 * @description 更新管理员的单个字段值
 * @param data - 字段更新数据
 * @returns 操作结果
 */
export const updateAdminUserField = (data:object) => {
    return request({
        url: "authority/adminUser/updateField",
        method: "post",
        data
    });
}

/**
 * 获取店铺管理员列表
 * @description 分页获取商户店铺管理员数据
 * @param params - 筛选和分页参数
 * @returns 店铺管理员列表
 */
export const getShopUserList = (params: AdminUserFilterParams) => {
    return request<AdminUserFilterResult>({
        url: "merchant/adminUserShop/list",
        method: "get",
        params,
    });
}
/**
 * 删除店铺管理员
 * @description 删除指定店铺管理员
 * @param data - 管理员ID
 * @returns 操作结果
 */
export const delShopUser= (data: object) => {
    return request({
        url: "merchant/adminUserShop/del",
        method: "post",
        data,
    });
}

/**
 * 获取店铺管理员详情
 * @description 获取店铺管理员的完整信息
 * @param action - 操作类型
 * @param params - 管理员ID等参数
 * @returns 店铺管理员详情
 */
export const getShopUser  = (action: string, params: object) => {
    return request<AdminUserFormState>({
        url: "merchant/adminUserShop/" + action,
        method: "get",
        params
    });
}

/**
 * 更新店铺管理员
 * @description 创建或更新店铺管理员
 * @param operation - 操作类型（create/update）
 * @param data - 管理员数据
 * @returns 操作结果
 */
export const updateShopUser = (operation: string, data: object) => {
    return request({
        url: "merchant/adminUserShop/" + operation,
        method: "post",
        data
    });
}

/**
 * 批量操作店铺管理员
 * @description 批量处理店铺管理员
 * @param type - 操作类型
 * @param data - 操作数据
 * @returns 操作结果
 */
export const batchShopSubmit = (type: string, data: object) => {
    return request({
        url: "merchant/adminUserShop/batch",
        method: "post",
        data: {type, ...data}
    });
}
/**
 * 更新店铺管理员字段
 * @description 更新店铺管理员的单个字段值
 * @param data - 字段更新数据
 * @returns 操作结果
 */
export const updateShopUserField = (data:object) => {
    return request({
        url: "merchant/adminUserShop/updateField",
        method: "post",
        data
    });
}