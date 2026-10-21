import request from "@/utils/request";
import {UserFilterParams, UserFilterResult, UserFormState, UserFundFilterResult} from "@/types/user/user.d"

/**
 * 获取会员列表
 * @description 分页获取会员数据，支持多条件筛选
 * @param params - 筛选和分页参数
 * @returns 会员列表
 */
export const getUserList = (params: UserFilterParams) => {
    return request<UserFilterResult>({
        url: "user/user/list",
        method: "get",
        params,
    });
}

/**
 * 删除会员
 * @description 删除指定会员账号
 * @param data - 会员ID
 * @returns 操作结果
 */
export const delUser= (data: object) => {
    return request({
        url: "user/user/del",
        method: "post",
        data,
    });
}

/**
 * 恢复会员
 * @description 恢复已删除的会员账号
 * @param data - 会员ID
 * @returns 操作结果
 */
export const recallUser= (data: object) => {
    return request({
        url: "user/user/recall",
        method: "post",
        data,
    });
}
/**
 * 获取会员详情
 * @description 获取会员的完整信息
 * @param action - 操作类型（detail等）
 * @param params - 会员ID等参数
 * @returns 会员详情
 */
export const getUser  = (action: string, params: object) => {
    return request<UserFormState>({
        url: "user/user/" + action,
        method: "get",
        params
    });
}

/**
 * 更新会员信息
 * @description 创建或更新会员信息
 * @param operation - 操作类型（create/update）
 * @param data - 会员数据
 * @returns 操作结果
 */
export const updateUser = (operation: string, data: object) => {
    return request({
        url: "user/user/" + operation,
        method: "post",
        data
    });
}

/**
 * 批量操作会员
 * @description 对会员进行批量操作
 * @param type - 操作类型
 * @param data - 操作数据
 * @returns 操作结果
 */
export const batchSubmit = (type: string, data: object) => {
    return request({
        url: "user/user/batch",
        method: "post",
        data: {type, ...data},
    });
}

/**
 * 更新会员字段
 * @description 更新会员的单个字段值
 * @param data - 字段更新数据
 * @returns 操作结果
 */
export const updateUserField = (data:object) => {
    return request({
        url: "user/user/updateField",
        method: "post",
        data
    });
}

/**
 * 资金管理操作
 * @description 管理会员的余额、积分等资金
 * @param data - 资金操作数据
 * @returns 操作结果
 */
export const updateUserFundManagement = (data: object) => {
    return request({
        url: "user/user/fundManagement",
        method: "post",
        data
    });
}

/**
 * 获取会员资金明细
 * @description 获取会员的资金变动记录
 * @param params - 筛选和分页参数
 * @returns 资金明细列表
 */
export const getUserFundList = (params: UserFilterParams) => {
    return request<UserFundFilterResult>({
        url: "user/user/userFundDetail",
        method: "get",
        params,
    });
}

/**
 * 设置会员标签
 * @description 为会员添加管理员备注标签
 * @param data - 标签内容
 * @returns 操作结果
 */
export const setAdminNote = (data: object) => {
    return request<UserFundFilterResult>({
        url: "user/user/setAdminNote",
        method: "post",
        data,
    });
}

/**
 * 获取会员优惠券列表
 * @description 获取会员持有的优惠券
 * @param params - 筛选参数
 * @returns 优惠券列表
 */
export const getUserCouponList = (params: UserFilterParams) => {
    return request<any>({
        url: "pos/user/coupon/visible-list",
        method: "get",
        params
    })
}

/**
 * 下载会员导入模板
 * @description 下载批量导入会员的Excel模板
 * @returns 模板文件
 */
export const userImportTemplate = () => {
    return request({
        url: "user/user/downloadUserImportTemplate",
        method: "get",
        responseType: "blob"
    });
}

/**
 * 批量导入会员
 * @description 通过Excel文件批量导入会员数据
 * @param data - 上传的文件
 * @returns 导入结果
 */
export const userImportSubmit = (data: FormData) => {
    return request({
        url: "user/user/batchImport",
        method: "post",
        data,
        headers: {
            "Content-Type": "multipart/form-data"
        }
    });
}

/**
 * 获取导入失败数据
 * @description 下载导入失败的会员数据文件
 * @param data - 请求参数
 * @returns 失败数据文件
 */
export const getUserImportErrorData = (data: FormData) => {
    return request({
        url: "user/user/getImportErrorData",
        method: "post",
        data,
        responseType: "blob",
        headers: {
            "Content-Type": "multipart/form-data"
        }
    });
}

/**
 * 批量增加会员积分
 * @description 为选中会员批量增加积分
 * @param data - 积分操作数据
 * @returns 操作结果
 */
export const batchAddPoints = (data: object) => {
    return batchSubmit("incrPoints", data);
}

/**
 * 批量增加会员余额
 * @description 为选中会员批量增加余额
 * @param data - 余额操作数据
 * @returns 操作结果
 */
export const batchAddBalance = (data: object) => {
    return batchSubmit("incrBalance", data);
}
