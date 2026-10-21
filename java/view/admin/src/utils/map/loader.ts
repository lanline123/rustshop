import AMapLoader from "@amap/amap-jsapi-loader";
import i18n from "@/i18n";

const t = i18n.global.t;

const AMAP_VERSION = "2.0";
const TIANDITU_VERSION = "4.0";
const TIANDITU_SDK_URL = "https://api.tianditu.gov.cn/api";
/** 天地图脚本 onload 后仍可能在异步补齐命名空间，轮询等待上限 */
const TIANDITU_READY_TIMEOUT = 15000;
const TIANDITU_READY_INTERVAL = 50;

let tiandituPending: Promise<any> | null = null;

/**
 * 加载高德 JS SDK
 * @param options.jsKey - 高德 JS API Key（明文）
 * @param options.jsSecret - 高德 JS API Secret（明文），用于 _AMapSecurityConfig
 * @param options.plugins - 需要的插件列表
 * @returns AMap 命名空间
 */
export const loadAmapSdk = (options: { jsKey?: string; jsSecret?: string; plugins?: string[] }): Promise<any> => {
    const { jsKey, jsSecret, plugins = [] } = options;
    if (!jsKey) {
        return Promise.reject(new Error(t("未配置高德地图 JS Key，请先在地图设置中填写")));
    }
    if (jsSecret) {
        (window as any)._AMapSecurityConfig = { securityJsCode: jsSecret };
    }
    return AMapLoader.load({ key: jsKey, version: AMAP_VERSION, plugins });
};

/** 轮询等待天地图命名空间就绪 */
const waitForTiandituNamespace = (): Promise<any> => {
    return new Promise((resolve, reject) => {
        const startedAt = Date.now();
        const tick = () => {
            const T = (window as any).T;
            if (T && T.Map) {
                resolve(T);
                return;
            }
            if (Date.now() - startedAt >= TIANDITU_READY_TIMEOUT) {
                reject(new Error(t("天地图 SDK 加载超时，请检查网络或 tk 是否有效")));
                return;
            }
            window.setTimeout(tick, TIANDITU_READY_INTERVAL);
        };
        tick();
    });
};

/**
 * 加载天地图 JS SDK
 * @description 天地图无官方 loader 包，动态注入 script 并做并发去重；失败时清空缓存允许重试
 * @param tk - 天地图 JS API tk（明文）
 * @returns T 命名空间
 */
export const loadTiandituSdk = (tk?: string): Promise<any> => {
    const existing = (window as any).T;
    if (existing && existing.Map) return Promise.resolve(existing);
    if (tiandituPending) return tiandituPending;
    if (!tk) {
        return Promise.reject(new Error(t("未配置天地图 JS tk，请先在地图设置中填写")));
    }

    tiandituPending = new Promise((resolve, reject) => {
        const script = document.createElement("script");
        script.type = "text/javascript";
        script.async = true;
        script.src = `${TIANDITU_SDK_URL}?v=${TIANDITU_VERSION}&tk=${encodeURIComponent(tk)}`;
        script.onload = () => {
            waitForTiandituNamespace().then(resolve, reject);
        };
        script.onerror = () => {
            reject(new Error(t("天地图 SDK 加载失败，请检查网络连接")));
        };
        document.head.appendChild(script);
    }).catch((error) => {
        tiandituPending = null;
        throw error;
    });

    return tiandituPending;
};
