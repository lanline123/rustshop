// 物流服务商：1 快递鸟 / 2 快递100，全局单选
export type LogisticsProvider = 1 | 2;

export interface BaseLogistics {
    address?: string;
    areaName?: string;
    cityName?: string;
    logisticsProvider?: LogisticsProvider;
    kdniaoApiKey?: string;
    kdniaoBusinessId?: string;
    kuaidi100Key?: string;
    kuaidi100Secret?: string;
    kuaidi100Customer?: string;
    mobile?: string;
    provinceName?: string;
    receiveAddress?: string;
    receiveAreaName?: string;
    receiveCityName?: string;
    receiveMobile?: string;
    receiveProvinceName?: string;
    receiveSender?: string;
    sender?: string;
    defaultLogisticsName?: string;
}
