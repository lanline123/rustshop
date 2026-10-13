/**
 * 同城配送配置相关 API
 */
import request from "@/utils/request";
import type {
    CityDeliveryResponse,
    IntraCityPlatformConfig,
    ShopLocalDeliveryConfigDetailVO,
    ShopLocalDeliveryConfigSaveParam,
    SelfShopDetailResponse,
    SelfShopSaveParam
} from "@/types/setting/intraCityDelivery";

export const getConfigAmapAllSettings = (params?: object) => {
    return request<CityDeliveryResponse>({
        url: "setting/config/amapAllSettings",
        method: "get",
        params
    });
};
export const saveAllAmapSettings = (data: CityDeliveryResponse) => {
    return request({
        url: "setting/config/saveAllAmapSettings",
        method: "post",
        data
    });
};

// ===== 平台同城配送总控（文档 4.3，复用菜单权限 intraCityDeliveryManage） =====
// 获取平台同城配送总控配置（功能总开关 / 商家自配送 / 第三方配送）
export const getIntraCityPlatformConfig = (params?: object) => {
    return request<IntraCityPlatformConfig>({
        url: "o2o/intraCityDelivery/platformConfig/getConfig",
        method: "get",
        params
    });
};

// 保存平台同城配送总控配置
export const saveIntraCityPlatformConfig = (data: IntraCityPlatformConfig) => {
    return request({
        url: "o2o/intraCityDelivery/platformConfig/saveConfig",
        method: "post",
        data
    });
};

//获取门店同城配送配置详情
export const getIntraCityDeliveryConfig = (params?: { shopId?: number }) => {
    return request<ShopLocalDeliveryConfigDetailVO>({
        url: "o2o/localDelivery/getConfig",
        method: "get",
        params
    });
};

// 保存门店同城配送配置
export const saveIntraCityDeliveryConfig = (data: ShopLocalDeliveryConfigSaveParam) => {
    return request({
        url: "o2o/localDelivery/saveConfig",
        method: "post",
        data
    });
};

//获取平台门店详情
export const getSelfShopDetail = (params?: object) => {
    return request<SelfShopDetailResponse>({
        url: "setting/config/storeSelfOperatedSettings",
        method: "get",
        params
    });
};

//保存平台门店设置
export const saveSelfShopDetail = (data: SelfShopSaveParam) => {
    return request({
        url: "setting/config/saveStoreSelfOperated",
        method: "post",
        data
    });
};
