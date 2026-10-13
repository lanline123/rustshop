import request from "@/utils/request";
import type { MapSettingsParam, MapSettingsResponse } from "@/types/setting/map";

/**
 * 获取地图服务商配置
 * @description 服务商选择 + 高德 6 项 + 天地图 3 项的超集端点
 * @param params - use 为 1 时返回明文（仅供加载 SDK 等可信场景），空或 0 返回掩码
 * @returns 地图服务商配置
 */
export const getMapSettings = (params?: object) => {
    return request<MapSettingsResponse>({
        url: "setting/config/mapSettings",
        method: "get",
        params
    });
};

/**
 * 保存地图服务商配置
 * @description 9 个敏感字段原样回传掩码串即视为未改动，后端不覆盖
 * @param data - 地图服务商配置数据
 * @returns 操作结果
 */
export const saveMapSettings = (data: MapSettingsParam) => {
    return request({
        url: "setting/config/saveMapSettings",
        method: "post",
        data
    });
};
