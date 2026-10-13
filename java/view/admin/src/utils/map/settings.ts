import { getMapSettings } from "@/api/setting/map";
import type { MapSettingsResponse } from "@/types/setting/map";

let pending: Promise<MapSettingsResponse> | null = null;

/**
 * 获取运行态地图配置（明文）
 * @description 加载 SDK 需要明文 key，故传 use=1；同一页面多个地图组件共享同一次请求，失败不缓存以便重试
 * @param force - 是否强制重新拉取
 */
export const getRuntimeMapSettings = (force = false): Promise<MapSettingsResponse> => {
    if (force || !pending) {
        pending = getMapSettings({ use: 1 }).catch((error) => {
            pending = null;
            throw error;
        });
    }
    return pending;
};

/**
 * 清空运行态配置缓存
 * @description 地图设置保存成功后调用，避免同一会话内仍使用旧的服务商与密钥
 */
export const clearRuntimeMapSettingsCache = (): void => {
    pending = null;
};
