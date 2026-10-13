/**
 * 搜索页（PC）API 适配层
 * 将页面组件使用的命名空间调用（searchApi / shopApi / decorateApi）
 * 映射到 o2o 现有的接口函数，返回值使用宽松类型以避免跨类型体系摩擦。
 */
import { request } from "~/utils/request";
import { getSearchFilter, getSearchEsFilter, getSearchProductList } from "~/api/search/search";
import { getShopList } from "~/api/shop/shop";

export const searchApi = {
    getFilter: (data: Record<string, unknown>) => getSearchFilter(data) as Promise<any>,
    getEsFilter: (data: Record<string, unknown>) => getSearchEsFilter(data) as Promise<any>,
    getProductList: (data: Record<string, unknown>) => getSearchProductList(data) as Promise<any>
};

export const shopApi = {
    getList: (params: Record<string, unknown>) => getShopList(params) as Promise<any>
};

export const decorateApi = {
    /** PC 搜索页装修；传入 previewId 时拉预览草稿 */
    getPcSearch: (params?: Record<string, unknown>) =>
        request<any>({ url: "home/home/pcSearch", method: "get", params })
};
