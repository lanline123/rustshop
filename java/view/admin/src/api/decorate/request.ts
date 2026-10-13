import request from "@/utils/request";

export const getProductList = (params: object) => {
    return request({
        url: "decorate/decorateRequest/productList",
        method: "get",
        params,
    });
};
