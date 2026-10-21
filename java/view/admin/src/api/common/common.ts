import type {CommonCleanupFormState} from "@/types/common/common.d";
import request from "@/utils/request";

/**
 * 清理缓存
 * @description 清理系统缓存数据
 * @returns 清理结果
 */
export const cleanUp = () => {
    return request<CommonCleanupFormState>({
        url: "common/cacheManage/cleanup",
        method: "post"
    });
}
/**
 * 获取管理员消息数量
 * @description 获取未读消息等统计数据
 * @param params - 查询参数
 * @returns 消息统计
 */
export const getAdminMsgCount = (params: object) => {
    return request<any>({
        url: "msg/adminMsg/msgCount",
        method: "get",
        params,
    });
}

/**
 * 获取页面提示列表
 * @description 获取后台页面的提示信息
 * @param params - 查询参数
 * @returns 提示列表
 */
export const getTipsList = (params: object) => {
    return request<any>({
        url: "common/tipsManage/list",
        method: "get",
        params,
    });
}


/**
 * 获取CSRF令牌
 * @description 获取用于表单安全验证的CSRF令牌
 * @returns CSRF令牌
 */
export const getCsrf = () => {
    return request<any>({
        url: "common/csrf/create",
        method: "get",
    });
}