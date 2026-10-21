import request from "@/utils/request";
import type { ArticleFilterResult, ArticleFormState,ArticleFilterParams } from "@/types/content/article.d";

/**
 * 获取文章列表
 * @description 分页获取文章数据
 * @param params - 筛选和分页参数
 * @returns 文章列表
 */
export const getArticleList = (params: ArticleFilterParams) => {
    return request<ArticleFilterResult>({
        url: "content/article/list",
        method: "get",
        params,
    });
}
/**
 * 批量操作文章
 * @description 批量处理文章（删除/发布等）
 * @param type - 操作类型
 * @param data - 操作数据
 * @returns 操作结果
 */
export const batchSubmit = (type: string, data: object) => {
    return request({
        url: "content/article/batch",
        method: "post",
        data: {type, ...data}
    });
}

/**
 * 删除文章
 * @description 删除指定文章
 * @param data - 文章ID
 * @returns 操作结果
 */
export const delArticle = (data: object) => {
    return request({
        url: "content/article/del",
        method: "post",
        data,
    });
}
/**
 * 更新文章字段
 * @description 更新文章的单个字段值
 * @param data - 字段更新数据
 * @returns 操作结果
 */
export const updateArticleFiled = (data: object) => {
    return request({
        url: "content/article/updateField",
        method: "post",
        data,
    });
}

/**
 * 获取文章详情
 * @description 获取文章的完整信息
 * @param action - 操作类型
 * @param params - 文章ID等参数
 * @returns 文章详情
 */
export const getArticle = (action: string, params: object) => {
    return request<ArticleFormState>({
        url: "content/article/" + action,
        method: "get",
        params
    });
}
/**
 * 更新文章
 * @description 创建或更新文章
 * @param operation - 操作类型（create/update）
 * @param data - 文章数据
 * @returns 操作结果
 */
export const updateArticle = (operation: string, data: object) => {
    return request({
        url: "content/article/" + operation,
        method: "post",
        data
    });
}
