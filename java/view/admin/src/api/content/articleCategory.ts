import request from "@/utils/request";
import { ArticleCategoryFilterResult, ArticleCategoryFormState ,ArticleCategoryFilterParams,ArticleCategoryFilterState} from "@/types/content/articleCategory";

/**
 * 获取文章分类列表
 * @description 分页获取文章分类数据
 * @param params - 筛选和分页参数
 * @returns 文章分类列表
 */
export const getArticleCategoryList = (params: ArticleCategoryFilterParams) => {
    return request<ArticleCategoryFilterResult>({
        url: "content/articleCategory/list",
        method: "get",
        params,
    });
}

/**
 * 获取文章分类树
 * @description 获取文章分类的树形结构
 * @returns 分类树形数据
 */
export const getArticleCategoryTree = () => {
    return request<ArticleCategoryFilterState[]>({
        url: "content/articleCategory/tree",
        method: "get"
    });
}

/**
 * 批量操作文章分类
 * @description 批量处理文章分类
 * @param type - 操作类型
 * @param data - 操作数据
 * @returns 操作结果
 */
export const batchSubmit = (type: string, data: object) => {
    return request({
        url: "content/articleCategory/batch",
        method: "post",
        data: {type, ...data}
    });
}

/**
 * 更新文章分类字段
 * @description 更新文章分类的单个字段值
 * @param data - 字段更新数据
 * @returns 操作结果
 */
export const updateArticleCategoryFiled = (data: object) => {
    return request({
        url: "content/articleCategory/updateField",
        method: "post",
        data,
    });
}

/**
 * 删除文章分类
 * @description 删除指定文章分类
 * @param data - 分类ID
 * @returns 操作结果
 */
export const delArticleCategory = (data: object) => {
    return request({
        url: "content/articleCategory/del",
        method: "post",
        data,
    });
}

/**
 * 获取文章分类详情
 * @description 获取文章分类的完整信息
 * @param action - 操作类型
 * @param params - 分类ID等参数
 * @returns 文章分类详情
 */
export  const getArticleCategory = (action: string, params: object) => {
    return request<ArticleCategoryFormState>({
        url: "content/articleCategory/" + action,
        method: "get",
        params
    });
}

/**
 * 更新文章分类
 * @description 创建或更新文章分类
 * @param operation - 操作类型（create/update）
 * @param data - 分类数据
 * @returns 操作结果
 */
export const updateArticleCategory = (operation: string, data: object) => {
    return request({
        url: "content/articleCategory/" + operation,
        method: "post",
        data
    });
}

/**
 * 移动文章分类
 * @description 调整文章分类的层级位置
 * @param data - 移动参数
 * @returns 操作结果
 */
export const moveArticleCategory = ( data: object) => {
    return request({
        url: "content/articleCategory/moveCat",
        method: "post",
        data
    });
}
