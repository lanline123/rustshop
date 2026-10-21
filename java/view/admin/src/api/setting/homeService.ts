/**
 * 预约上门服务配置 API
 */
import request from "@/utils/request";
import type {
    ShopHomeServiceConfigDetailVO,
    ShopHomeServiceConfigSaveParam
} from "@/types/setting/homeService";

/**
 * 获取门店预约上门服务配置详情
 * @param params.shopId 门店ID（平台管理员可传，门店管理员自动获取）
 */
export const getHomeServiceConfig = (params?: { shopId?: number }) => {
    return request<ShopHomeServiceConfigDetailVO>({
        url: "o2o/homeService/getConfig",
        method: "get",
        params
    });
};

/**
 * 保存门店预约上门服务配置
 */
export const saveHomeServiceConfig = (data: ShopHomeServiceConfigSaveParam) => {
    return request({
        url: "o2o/homeService/saveConfig",
        method: "post",
        data
    });
};

