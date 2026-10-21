import request from "@/utils/request";
import type { CreditOrderListPage, CreditOrderListQuery } from "@/types/termPay/creditOrder.d";

/**
 * 3.1 账期订单列表
 * @description GET /adminapi/credit/order/list
 */
export function getCreditOrderList(params: CreditOrderListQuery) {
    return request<CreditOrderListPage>({
        url: "/credit/order/list",
        method: "get",
        params
    });
}

