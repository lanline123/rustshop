import request from "@/utils/request";
import type { CategoryFilterResult } from "@/types/article/category.d";

/**
 * @description 获取帮助中心文章分类列表
 * @param params 筛选条件
 * @returns 分类列表
 */
export const getBzzxCategoryList = (params: object) => {
    return request<CategoryFilterResult>({
        url: "article/category/indexBzzxList",
        method: "get",
        params
    });
};
