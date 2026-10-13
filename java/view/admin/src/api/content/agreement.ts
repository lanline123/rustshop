import request from "@/utils/request";
import { ArticleCategoryFilterResult, ArticleCategoryFormState ,ArticleCategoryFilterParams,ArticleCategoryFilterState} from "@/types/content/articleCategory";
// 获取商品分类列表
export const getArticleCategoryList = (params: ArticleCategoryFilterParams) => {
    return request<ArticleCategoryFilterResult>({
        url: "content/articleCategory/list",
        method: "get",
        params,
    });
}

export const getArticleCategoryTree = () => {
    return request<ArticleCategoryFilterState[]>({
        url: "content/articleCategory/tree",
        method: "get"
    });
}

// 商品分类页面批量操作
export const batchSubmit = (type: string, data: object) => {
    return request({
        url: "content/articleCategory/batch",
        method: "post",
        data: {type, ...data}
    });
}

// 商品分类页面更新项
export const updateArticleCategoryFiled = (data: object) => {
    return request({
        url: "content/articleCategory/updateField",
        method: "post",
        data,
    });
}

export const delArticleCategory = (data: object) => {
    return request({
        url: "content/articleCategory/del",
        method: "post",
        data,
    });
}

export  const getArticleCategory = (action: string, params: object) => {
    return request<ArticleCategoryFormState>({
        url: "content/articleCategory/" + action,
        method: "get",
        params
    });
}

export const updateArticleCategory = (operation: string, data: object) => {
    return request({
        url: "content/articleCategory/" + operation,
        method: "post",
        data
    });
}

export const moveArticleCategory = ( data: object) => {
    return request({
        url: "content/articleCategory/moveCat",
        method: "post",
        data
    });
}
