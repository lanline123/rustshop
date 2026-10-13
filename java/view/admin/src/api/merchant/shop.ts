import request from "@/utils/request";
import type {MerchantSearchFilterResult} from "@/types/merchant/shop.d";

export const getShopMyShop = (params: object) => {
    return request<any>({
        url: "merchant/shop/myShop",
        method: "get",
        params,
        noErrorTip: true
    });
};
export const createShop = (data: object) => {
    return request<MerchantSearchFilterResult>({
        url: "merchant/shop/create",
        method: "post",
        data
    });
};


export const chooseShop = (data: object) => {
    return request<any>({
        url: "merchant/shop/choose",
        method: "post",
        data
    });
};

export const checkIsAdmin = () => {
    return request<{ isAdmin: boolean }>({
        url: "merchant/adminUserShop/checkIsAdmin",
        method: "get"
    });
};

