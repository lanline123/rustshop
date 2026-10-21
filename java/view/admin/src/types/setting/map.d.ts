/**
 * 地图服务商标识
 * amap-高德地图（默认）/ tianditu-天地图
 */
export type MapProvider = "amap" | "tianditu";

/**
 * MapSettingsVO，地图服务商设置
 * 除 mapProvider 外的 9 个字段均为敏感字段，use 为空或 0 时后端按中间段掩码返回
 */
export interface MapSettingsResponse {
    /**
     * 地图服务商，空值或未知取值后端按 amap 处理
     */
    mapProvider?: MapProvider | string;
    /**
     * 高德 Web 端 Key，服务端调用
     */
    amapWebKey?: string;
    /**
     * 高德 Web 端 Secret，服务端调用
     */
    amapWebSecret?: string;
    /**
     * 高德 JS API Key，浏览器加载 SDK
     */
    amapJsKey?: string;
    /**
     * 高德 JS API Secret，浏览器加载 SDK
     */
    amapJsSecret?: string;
    /**
     * 高德小程序 Key
     */
    amapMiniKey?: string;
    /**
     * 高德小程序 Secret
     */
    amapMiniSecret?: string;
    /**
     * 天地图服务端 tk，切到天地图后必填
     */
    tiandituWebTk?: string;
    /**
     * 天地图 JS API tk，浏览器加载 SDK
     */
    tiandituJsTk?: string;
    /**
     * 天地图小程序 tk
     */
    tiandituMiniTk?: string;
}

/**
 * MapSettingsParam，字段与 MapSettingsResponse 相同
 * 敏感字段未改动时原样回传掩码串，后端判定为未改不写库
 */
export type MapSettingsParam = MapSettingsResponse;
