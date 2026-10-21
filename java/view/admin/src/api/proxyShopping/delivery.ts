import request from "@/utils/request";

export interface ProxyShoppingDeliveryOptionsParams {
    userId?: number;
    shopId?: number;
    latitude?: number;
    longitude?: number;
    [k: string]: any;
}

// 代客下单（O2O）：获取商品可用配送方式
// GET /adminapi/proxyShopping/delivery/o2o/getDeliveryOptions?shopId=...&latitude=...&longitude=...
export const getProxyShoppingO2ODeliveryOptions = (params: ProxyShoppingDeliveryOptionsParams) => {
    return request<any>({
        url: "proxyShopping/delivery/getDeliveryOptions",
        method: "get",
        params
    });
};


