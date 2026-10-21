import request from "@/utils/request";
import { panelIndexFilterState, PanelStatisticalData } from "@/types/panel/index";
// 面板首页
export const getPanelIndex = () => {
    return request<panelIndexFilterState>({
        url: "panel/panel/index",
        method: "get"
    });
};
// 首页趋势图表数据（按天数筛选：7 / 15 / 30）
export const getPanelStatisticalData = (days: number) => {
    return request<PanelStatisticalData>({
        url: "panel/panel/statisticalData",
        method: "get",
        params: { days }
    });
};
