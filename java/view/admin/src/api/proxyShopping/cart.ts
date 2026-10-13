import request from "@/utils/request";
import type {
    ProxyCartAddToCartDTO,
    ProxyCartGetCountParams,
    ProxyCartListDTO,
    ProxyCartListByStoreVO,
    ProxyCartRemoveItemDTO,
    ProxyCartUpdateItemDTO,
    ProxyExchangeProductToCartVO
} from "@/types/order/proxyShopping.d";

// 获取购物车列表（按店铺分组）
export const getProxyCartList = (data: ProxyCartListDTO) => {
    return request<ProxyCartListByStoreVO>({
        url: "proxyShopping/cart/list",
        method: "post",
        data
    });
};

// 添加商品到购物车
export const addToProxyCart = (data: ProxyCartAddToCartDTO) => {
    return request<ProxyExchangeProductToCartVO>({
        url: "proxyShopping/cart/addToCart",
        method: "post",
        data
    });
};

// 清空购物车
export const clearProxyCart = (data: { userId: number }) => {
    return request<void>({
        url: "proxyShopping/cart/clearCart",
        method: "post",
        data
    });
};

// 删除购物车商品（批量）
export const removeProxyCartItem = (data: ProxyCartRemoveItemDTO) => {
    return request<void>({
        url: "proxyShopping/cart/removeItem",
        method: "post",
        data
    });
};

// 更新购物车商品数量
export const updateProxyCartItem = (data: ProxyCartUpdateItemDTO) => {
    return request<void>({
        url: "proxyShopping/cart/updateItem",
        method: "post",
        data
    });
};

// 更新购物车商品选中状态
export const updateProxyCartCheck = (data: { userId: number; data: { cartId: number; isChecked: number }[] }) => {
    return request<void>({
        url: "proxyShopping/cart/updateCheck",
        method: "post",
        data
    });
};

// 获取购物车商品总数量
export const getProxyCartCount = (params: ProxyCartGetCountParams) => {
    return request<number>({
        url: "proxyShopping/cart/getCount",
        method: "get",
        params
    });
};

/**
 * 代客下单：领取优惠券
 * POST /adminapi/proxyShopping/coupon/claim
 * 入参与收银台一致：{ couponId, userId }
 */
export const claimProxyShoppingCoupon = (data: { couponId: number; userId: number }) => {
    return request<any>({
        url: "proxyShopping/coupon/claim",
        method: "post",
        data
    });
};

/**
 * 代客下单：可领取优惠券列表（对齐收银台 pos/user/coupon/visible-list）
 * POST /adminapi/proxyShopping/user/coupon/visible-list
 * 入参：{ userId, productIds? }
 */
export const getProxyShoppingCouponList = (data: { userId: number; productIds?: number[] }) => {
    return request<any>({
        url: "proxyShopping/coupon/visible-list",
        method: "post",
        data
    });
};
