import request from "@/utils/request";
import type {CategoryFilterParams, CategoryFilterResult, CategoryFormState, CategoryFilterState} from "@/types/product/category";

/**
 * 获取商品分类列表
 * @description 分页获取商品分类数据
 * @param params - 筛选和分页参数
 * @returns 分类列表
 */
export const getCategoryList = (params: CategoryFilterParams) => {
    return request<CategoryFilterResult>({
        url: "product/category/list",
        method: "get",
        params,
    });
}

/**
 * 获取所有商品分类
 * @description 获取完整的商品分类树
 * @param shopId - 可选的店铺ID
 * @returns 分类树数据
 */
export const getAllCategoryList = (shopId?: number) => {
    return request<CategoryFilterState[]>({
        url: "product/category/getAllCategory",
        method: "get",
        params: {
            shopId
        }
    });
}

/**
 * 批量操作商品分类
 * @description 批量处理商品分类
 * @param type - 操作类型
 * @param data - 操作数据
 * @returns 操作结果
 */
export const batchSubmit = (type: string, data: object) => {
    return request({
        url: "product/category/batch",
        method: "post",
        data: {type, ...data},
    });
}

/**
 * 更新商品分类字段
 * @description 更新商品分类的单个字段值
 * @param data - 字段更新数据
 * @returns 操作结果
 */
export const updateCategoryFiled = (data: object) => {
    return request({
        url: "product/category/updateField",
        method: "post",
        data,
    });
}

/**
 * 删除商品分类
 * @description 删除指定商品分类
 * @param data - 分类ID
 * @returns 操作结果
 */
export const delCategory = (data: object) => {
    return request({
        url: "product/category/del",
        method: "post",
        data,
    });
}

/**
 * 获取商品分类详情
 * @description 获取商品分类的完整信息
 * @param action - 操作类型
 * @param params - 分类ID等参数
 * @returns 分类详情
 */
export  const getCategory = (action: string, params: object) => {
    return request<CategoryFormState>({
        url: "product/category/" + action,
        method: "get",
        params
    });
}

/**
 * 更新商品分类
 * @description 创建或更新商品分类
 * @param operation - 操作类型（create/update）
 * @param data - 分类数据
 * @returns 操作结果
 */
export const updateCategory = (operation: string, data: object) => {
    return request({
        url: "product/category/" + operation,
        method: "post",
        data
    });
}

/**
 * 移动商品分类
 * @description 调整商品分类的层级位置
 * @param data - 移动参数
 * @returns 操作结果
 */
export const moveCategory = ( data: object) => {
    return request({
        url: "product/category/moveCat",
        method: "post",
        data
    });
}
