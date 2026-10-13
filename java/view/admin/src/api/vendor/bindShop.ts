import request from "@/utils/request";
import type { FilterParams, FilterResult } from "@/types/vendor/bindShop.d";

export const getvendorShopBindList = (params: FilterParams) => {
    return request<FilterResult>({
        url: "vendor/vendorShopBind/list",
        method: "get",
        params
    });
};
export const vendorShopBindShopList = (params: Object) => {
    return request<any>({
        url: "vendor/vendorShopBind/shopList",
        method: "get",
        params
    });
};
export const vendorShopBindMerchantList = (params: Object) => {
    return request<any>({
        url: "vendor/vendorShopBind/merchantList",
        method: "get"
    });
};

export const getvendorShopBindProductList = (params: Object) => {
    return request<any>({
        url: "vendor/vendorShopBind/productList",
        method: "get",
        params
    });
};
export const getvendorShopBindOrderList = (params: Object) => {
    return request<any>({
        url: "vendor/vendorShopBind/orderList",
        method: "get",
        params
    });
};
