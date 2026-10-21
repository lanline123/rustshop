import request from "@/utils/request";
import type {CommentFilterParams, CommentFilterResult, CommentFormState} from "@/types/product/comment";

/**
 * 获取商品评论列表
 * @description 分页获取商品评论数据
 * @param params - 筛选和分页参数
 * @returns 评论列表
 */
export const getCommentList = (params: CommentFilterParams) => {
    return request<CommentFilterResult>({
        url: "product/comment/list",
        method: "get",
        params,
    });
}
/**
 * 批量操作评论
 * @description 批量处理商品评论（审核/删除等）
 * @param type - 操作类型
 * @param data - 操作数据
 * @returns 操作结果
 */
export const batchSubmit = (type: string, data: object) => {
    return request({
        url: "product/comment/batch",
        method: "post",
        data: {type, ...data},
    });
}

/**
 * 删除评论
 * @description 删除指定商品评论
 * @param data - 评论ID
 * @returns 操作结果
 */
export const delComment = (data: object) => {
    return request({
        url: "product/comment/del",
        method: "post",
        data,
    });
}
/**
 * 更新评论字段
 * @description 更新评论的单个字段值
 * @param data - 字段更新数据
 * @returns 操作结果
 */
export const updateCommentFiled = (data: object) => {
    return request({
        url: "product/comment/updateField",
        method: "post",
        data,
    });
}

/**
 * 获取评论详情
 * @description 获取商品评论的完整信息
 * @param action - 操作类型
 * @param params - 评论ID等参数
 * @returns 评论详情
 */
export const getComment = (action: string, params: object) => {
    return request<CommentFormState>({
        url: "product/comment/" + action,
        method: "get",
        params
    });
}
/**
 * 更新评论
 * @description 审核或更新商品评论
 * @param operation - 操作类型
 * @param data - 评论数据
 * @returns 操作结果
 */
export const updateComment = (operation: string, data: object) => {
    return request({
        url: "product/comment/" + operation,
        method: "post",
        data
    });
}

/**
 * 回复评论
 * @description 商家回复用户评论
 * @param data - 回复内容
 * @returns 操作结果
 */
export const replyComment = (data: object) => {
    return request({
        url: "product/comment/replyComment",
        method: "post",
        data
    });
}
