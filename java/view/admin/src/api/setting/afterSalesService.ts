import request from "@/utils/request";
import type {AfterSalesServiceFilterState} from "@/types/setting/afterSalesService.d";

export const getAfterSalesSettings = () => {
    return request<AfterSalesServiceFilterState>({
        url: 'setting/config/afterSalesSettings',
        method: 'get'
    });
}

export const updateAfterSalesSettings = (data: AfterSalesServiceFilterState) => {
    return request({
        url: 'setting/config/saveAfterSales',
        method: 'post',
        data
    });
}

