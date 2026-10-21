import request from "@/utils/request";
import type { RegionFilterParams } from "@/types/setting/region";
import type { ProxyShoppingAddress } from "@/types/order/proxyShopping.d";

/**
 * 代客下单-获取地区树
 * @description 返回结构与 setting/region/getAllRegionTree 一致
 * @param params - 查询参数（可选）
 */
export const getProxyShoppingRegionTreeList = (params?: RegionFilterParams) => {
    return request<any>({
        url: "proxyShopping/address/getAllRegionTree",
        method: "get",
        params
    });
};

// 代客下单：获取会员地址列表
export const getProxyShoppingAddressList = (params: { userId: number; page?: number; size?: number }) => {
    return request<
        | ProxyShoppingAddress[]
        | {
              records?: ProxyShoppingAddress[];
              list?: ProxyShoppingAddress[];
              total?: number;
          }
    >({
        url: "proxyShopping/address/list",
        method: "post",
        data: params
    });
};

// 代客下单：新增会员收货地址
export type ProxyShoppingAddressCreateDTO = {
    userId: number;
    consignee: string;
    regionIds: number[];
    address: string;
    positionAddress: string;
    longitude?: number;
    latitude?: number;
    mobileAreaCode?: string;
    mobile: string;
    email?: string;
    isDefault?: number | boolean;
    [key: string]: any;
};

export const createProxyShoppingAddress = (data: ProxyShoppingAddressCreateDTO) => {
    return request<any>({
        url: "proxyShopping/address/create",
        method: "post",
        data
    });
};

// 代客下单：编辑会员收货地址
export const updateProxyShoppingAddress = (data: ProxyShoppingAddressCreateDTO & { addressId: number }) => {
    return request<any>({
        url: "proxyShopping/address/update",
        method: "post",
        data
    });
};

// 代客下单：切换并确认选中地址
export const setSelectedAddress = (id: number) => {
    return request<any>({
        url: "proxyShopping/address/setSelected",
        method: "post",
        data: { id }
    });
};


