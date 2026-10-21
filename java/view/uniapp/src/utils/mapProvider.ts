import { useConfigStore } from "@/store/config";

/**
 * 是否走原生 <map> 渲染
 *
 * 三种情况回落原生：小程序端（renderjs 不生效，且本轮明确不处理小程序）、
 * 后端未切天地图、后端切了天地图却没下发 tk（避免白屏）。
 *
 * tig-map 组件与需要按分支切换 DOM 结构的消费页共用此判定，避免两处逻辑漂移。
 */
export const isNativeMapProvider = (): boolean => {
    let native = true;
    // #ifndef MP
    const configStore = useConfigStore();
    native = configStore.mapProvider !== "tianditu" || !configStore.tiandituJsTk;
    // #endif
    return native;
};
