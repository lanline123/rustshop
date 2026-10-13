import request from "@/utils/request";
import type {BaseLogistics, LogisticsProvider} from "@/types/setting/baseLogistics.d";

export const LOGISTICS_PROVIDER_KDNIAO: LogisticsProvider = 1;
export const LOGISTICS_PROVIDER_KUAIDI100: LogisticsProvider = 2;

export const getShippingSettings = () => {
    return request<BaseLogistics>({
        url: 'setting/config/shippingSettings',
        method: 'get'
    });
}

export const updateShippingSettings = (data: BaseLogistics) => {
    return request({
        url: 'setting/config/saveShipping',
        method: 'post',
        data
    });
}

// 物流服务商是全局单选，物流公司列表与编辑抽屉都要用；同一会话内只请求一次，配送设置保存后需调 clearLogisticsProviderCache 失效
let providerCache: Promise<LogisticsProvider> | null = null;

export const getLogisticsProvider = (): Promise<LogisticsProvider> => {
    if (!providerCache) {
        providerCache = getShippingSettings()
            .then((result) => (Number(result?.logisticsProvider) === LOGISTICS_PROVIDER_KUAIDI100 ? LOGISTICS_PROVIDER_KUAIDI100 : LOGISTICS_PROVIDER_KDNIAO))
            .catch(() => {
                // 读取失败不阻塞页面，按快递鸟渲染
                providerCache = null;
                return LOGISTICS_PROVIDER_KDNIAO;
            });
    }
    return providerCache;
}

export const clearLogisticsProviderCache = () => {
    providerCache = null;
}
