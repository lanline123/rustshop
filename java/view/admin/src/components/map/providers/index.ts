import { getRuntimeMapSettings } from "@/utils/map/settings";
import { loadAmapSdk, loadTiandituSdk } from "@/utils/map/loader";
import { MAP_PROVIDER_TIANDITU, resolveMapProvider } from "@/utils/map/provider";
import { createAmapAdapter } from "./amapAdapter";
import { createTiandituAdapter } from "./tiandituAdapter";
import type { MapAdapter, MapFeature } from "./types";
import type { MapSettingsResponse } from "@/types/setting/map";

/** 高德按需加载插件；天地图全部能力内置，无需插件声明 */
const AMAP_PLUGINS: Record<MapFeature, string[]> = {
    controls: ["AMap.Scale", "AMap.ToolBar", "AMap.MapType"],
    geocoder: ["AMap.Geocoder"],
    search: ["AMap.PlaceSearch"],
    infoWindow: ["AMap.InfoWindow"],
    circleEditor: ["AMap.CircleEditor"]
};

/**
 * 按当前服务商创建地图适配器
 * @description 读取运行态配置（明文）→ 解析服务商 → 加载对应 SDK → 返回统一能力接口
 * @param options.features - 本次需要的能力，仅影响高德的插件加载
 * @param options.settings - 已有配置时可直接传入，避免重复请求
 */
export const createMapAdapter = async (
    options: { features?: MapFeature[]; settings?: MapSettingsResponse } = {}
): Promise<MapAdapter> => {
    const { features = [], settings } = options;
    const config = settings ?? (await getRuntimeMapSettings());

    if (resolveMapProvider(config.mapProvider) === MAP_PROVIDER_TIANDITU) {
        const T = await loadTiandituSdk(config.tiandituJsTk);
        return createTiandituAdapter(T);
    }

    const plugins = Array.from(new Set(features.flatMap((feature) => AMAP_PLUGINS[feature] ?? [])));
    const AMap = await loadAmapSdk({ jsKey: config.amapJsKey, jsSecret: config.amapJsSecret, plugins });
    return createAmapAdapter(AMap);
};

export type { MapAdapter, MapFeature, LngLat, NormalizedPoi, NormalizedRegeocode, CircleEditHandle } from "./types";
