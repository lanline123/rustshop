import request from "@/utils/request";
import type {ShopInfoFormState} from "@/types/merchant/setting/team.d";
// 店铺设置
export const saveShopSetting = (data: ShopInfoFormState) => {
    return request({
        url: "merchant/shop/setting",
        method: "post",
        data
    });
}

// 店铺信息修改
export const updateShopInfo = (data: ShopInfoFormState) => {
    return request({
        url: "merchant/shop/updateInfo",
        method: "post",
        data
    });
}

// 获取员工数量
export const getUsercount = () => {
    return request({
        url: "merchant/merchant/userCount",
        method: "get",
    });
}
