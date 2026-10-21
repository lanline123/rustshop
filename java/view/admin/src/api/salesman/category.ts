import request from "@/utils/request";
import type {categoryFilterResult, categoryFormState} from "@/types/salesman/category.d";
// 获取素材分类列表
export const getcategoryList = (params: object) => {
    return request<categoryFilterResult>({
        url: "salesman/category/list",
        method: "get",
        params,
    });
}
// 素材分类页面批量操作
export const batchSubmit = (type: string, data: object) => {
    return request({
        url: "salesman/category/batch",
        method: "post",
        data: {type, ...data},
    });
}
// 素材分类页面删除项
export const delcategory = (data: object) => {
    return request({
        url: "salesman/category/del",
        method: "post",
        data,
    });
}
// 素材分类页面更新项
export const updatecategoryFiled = (data: object) => {
    return request({
        url: "salesman/category/updateField",
        method: "post",
        data,
    });
}
// 获取素材分类详情
export const getcategory = (action: string, params: object) => {
    return request<categoryFormState[]>({
        url: "salesman/category/" + action,
        method: "get",
        params
    });
}
// 更新素材分类
export const updatecategory = (operation: string, data: object) => {
    return request({
        url: "salesman/category/" + operation,
        method: "post",
        data
    });
}
// 获取素材分类配置项
export const getcategoryConfig = () => {
    return request({
        url: "salesman/category/config",
        method: "get"
    });
}