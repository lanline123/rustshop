import request from "@/utils/request";
import type { getProductListFilterState } from "@/types/decorate/mobileProduct";

export const getProductList = (params: object) => {
    return request<getProductListFilterState>({
        url: "decorate/decorateRequest/productList",
        method: "get",
        params
    });
};
