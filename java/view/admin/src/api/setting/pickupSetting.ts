import request from "@/utils/request";
import { pickupSettingData } from "@/types/setting/pickupSetting";


export const getShopPickupTpl = (params?: object) => {
    return request<pickupSettingData>({
        url: "o2o/shopPickupConfig/getShopPickupTpl",
        method: "get",
        params
    });
};

export const saveShopPickupTpl = (data: pickupSettingData) => {
    return request({
        url: "o2o/shopPickupConfig/saveShopPickupTpl",
        method: "post",
        data
    });
};
