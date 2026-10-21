import request from "@/utils/request";
import type {
    ProxyProductAvailabilityParams,
    ProxyProductAvailabilityVO,
    ProxyProductDetailParams,
    ProxyProductListDTO,
    ProxyProductListResDTO
} from "@/types/order/proxyShopping.d";

// 获取商品详情（用于规格/sku 选择弹窗）
export const getProxyProductDetail = (params: ProxyProductDetailParams) => {
    return request<any>({
        url: "proxyShopping/product/productDetail",
        method: "get",
        params
    });
};

// 获取商品可用数据（SKU 价格/库存等）
export const getProxyProductAvailability = (params: ProxyProductAvailabilityParams) => {
    return request<ProxyProductAvailabilityVO>({
        url: "proxyShopping/product/getProductAvailability",
        method: "get",
        params
    });
};

// 获取可用于代客下单的商品列表
export const getProxyProductList = (data: ProxyProductListDTO) => {
    return request<ProxyProductListResDTO>({
        url: "proxyShopping/product/getProduct",
        method: "post",
        data
    });
};

// 与 POS 端保持一致：库存日历（代客下单当前业务可能不会用到，但保留实现便于复用）
export const getStockCalendar = (params: object) => {
    return request<any>({
        url: "proxyShopping/order/OrderReservation/getStockCalendar",
        method: "get",
        params
    });
};


