import request from "@/utils/request";
import { statisticsUserFilterParams, FilterResult } from "@/types/panel/statisticsUser.d";

// 用户统计
export const getUserStatisticsPanel = (params: statisticsUserFilterParams) => {
    return request<FilterResult>({
        url: "panel/statisticsUser/userStatisticsPanel",
        method: "get",
        params
    });
};

export const exportUserStatisticsPanel = (params: statisticsUserFilterParams) => {
    return request({
        url: "panel/statisticsUser/userStatisticsPanel",
        method: "get",
        responseType: 'arraybuffer',
        params
    });
};
