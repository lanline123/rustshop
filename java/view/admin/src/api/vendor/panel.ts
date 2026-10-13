import request from "@/utils/request";
import { panelIndexFilterState } from "@/types/panel/index";
import { PanelStatisticalData } from "@/types/vendor/panel";
// 供应商面板首页
export const getPanelIndex = () => {
    return request<panelIndexFilterState>({
        url: "panel/panel/vendorIndex",
        method: "get"
    });
};
// 供应商首页收益统计图表数据（按天数筛选：7 / 15 / 30）
export const getVendorPanelStatisticalData = (days: number) => {
    return request<PanelStatisticalData>({
        url: "panel/panel/vendorStatisticalData",
        method: "get",
        params: { days }
    });
};
