import request from "@/utils/request";
import type {
    ProxyOrderCheckParam,
    ProxyOrderCheckVO,
    ProxyOrderSubmitVO,
    ProxyPaymentType,
    ProxyStoreShippingTypeData,
    ProxyPickupPoint,
    ProxyPickupContact
} from "@/types/order/proxyShopping.d";

// 获取结算页信息
export const getProxyOrderCheckIndex = (data: ProxyOrderCheckParam) => {
    return request<ProxyOrderCheckVO>({
        url: "proxyShopping/order/check/index",
        method: "post",
        data
    });
};

// 获取可用支付方式
export const getProxyAvailablePaymentType = (params?: { flowType?: number }) => {
    return request<ProxyPaymentType[]>({
        url: "proxyShopping/order/check/getAvailablePaymentType",
        method: "get",
        params
    });
};

// 非多门店：获取按 shopId 分组的可选配送方式（结构与 PC getShippingType 一致）
export const getProxyStoreShippingType = (params: { userId: number; flowType?: number; addressId?: number; longitude?: number; latitude?: number }) => {
    return request<ProxyStoreShippingTypeData>({
        url: "proxyShopping/delivery/getStoreShippingType",
        method: "get",
        params
    });
};

// 更新结算信息（配送方式/地址/积分/余额等）
export const updateProxyOrderCheck = (data: ProxyOrderCheckParam) => {
    return request<ProxyOrderCheckVO>({
        url: "proxyShopping/order/check/update",
        method: "post",
        data
    });
};

// 更新优惠券
export const updateProxyOrderCheckCoupon = (data: ProxyOrderCheckParam) => {
    return request<ProxyOrderCheckVO>({
        url: "proxyShopping/order/check/updateCoupon",
        method: "post",
        data
    });
};

// 改价预览（不保存）
export const previewProxyOrderChangePrice = (data: ProxyOrderCheckParam) => {
    return request<ProxyOrderCheckVO>({
        url: "proxyShopping/order/check/changePriceShow",
        method: "post",
        data
    });
};

// 确认改价（保存）
export const confirmProxyOrderChangePrice = (data: ProxyOrderCheckParam) => {
    return request<ProxyOrderCheckVO>({
        url: "proxyShopping/order/check/changePrice",
        method: "post",
        data
    });
};

// 提交订单
export const submitProxyOrder = (data: ProxyOrderCheckParam) => {
    return request<ProxyOrderSubmitVO>({
        url: "proxyShopping/order/check/submit",
        method: "post",
        data
    });
};

// 到店自提：获取自提点列表
export const getProxyPickupList = (data: { shopId: number; [key: string]: any }) => {
    return request<ProxyPickupPoint[] | { records?: ProxyPickupPoint[]; list?: ProxyPickupPoint[]; total?: number }>({
        url: "proxyShopping/order/check/pickupList",
        method: "get",
        params: data
    });
};

// 到店自提：获取自提联系人列表
export const getProxyPickupContactList = (data: { userId: number; [key: string]: any }) => {
    return request<ProxyPickupContact[] | { records?: ProxyPickupContact[]; list?: ProxyPickupContact[]; total?: number }>({
        url: "proxyShopping/pickupContact/listByUserId",
        method: "get",
        params: data
    });
};

// 到店自提：新增自提联系人
export const createProxyPickupContact = (data: { userId: number; contactName: string; contactPhone: string; [key: string]: any }) => {
    return request<any>({
        url: "proxyShopping/pickupContact/create",
        method: "post",
        data
    });
};


