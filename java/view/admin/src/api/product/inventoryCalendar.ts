import request from "@/utils/request";
import type {
    InventoryCalendarFilterParams,
    InventoryCalendarFilterResult,
    InventoryCalendarFilterDailyStockResult,
    InventoryCalendarFilterDailyStockRequest
} from "@/types/product/inventoryCalendar.d";

// 获取库存日历列表
export const getInventoryCalendarList = (params: InventoryCalendarFilterParams) => {
    return request<InventoryCalendarFilterResult>({
        url: "o2o/stockCalendar/pageList",
        method: "get",
        params
    });
};

// 获取某日期库存信息
export const getInventoryCalendarDailyStock = (params: InventoryCalendarFilterDailyStockRequest) => {
    return request<InventoryCalendarFilterDailyStockResult>({
        url: "o2o/stockCalendar/getDailyStock",
        method: "get",
        params
    });
};

//修改某日期库存
export const updateInventoryCalendarDailyStock = (data: object) => {
    return request({
        url: "o2o/stockCalendar/updateDailyStock",
        method: "post",
        data
    });
};

//统一设置库存
export const setInventoryCalendarUnifiedStock = (data: object) => {
    return request({
        url: "o2o/stockCalendar/setUnifiedStock",
        method: "post",
        data
    });
};

//按时间段设置库存
export const setInventoryCalendarTimeSlotStock = (data: object) => {
    return request({
        url: "o2o/stockCalendar/setTimeSlotStock",
        method: "post",
        data
    });
};

//按时间段设置库存详情
export const getInventoryCalendarProductTimeList = (params: object) => {
    return request({
        url: "o2o/stockCalendar/getProductTimeList",
        method: "get",
        params
    });
};
