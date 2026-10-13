import request from "@/utils/request";
import type {
    CreditSettlementCancelBody,
    CreditSettlementHistoryPage,
    CreditSettlementHistoryQuery,
    CreditSettlementHistoryOrdersPage,
    CreditSettlementHistoryOrdersQuery,
    CreditSettlementPendingOrderRecord,
    CreditSettlementPendingOrdersPage,
    CreditSettlementPendingOrdersQuery,
    CreditSettlementSubmitBody,
    CreditSettlementUserListPage,
    CreditSettlementUserListQuery
} from "@/types/termPay/creditSettlement.d";

/**
 * 4.1 结算会员列表
 * @description GET /adminapi/credit/settlement/userList
 */
export function getCreditSettlementUserList(params: CreditSettlementUserListQuery) {
    return request<CreditSettlementUserListPage>({
        url: "/credit/settlement/userList",
        method: "get",
        params
    });
}

/**
 * 4.2 待结算订单列表
 * @description GET /adminapi/credit/settlement/pendingOrders
 */
export function getCreditSettlementPendingOrders(params: CreditSettlementPendingOrdersQuery) {
    return request<CreditSettlementPendingOrdersPage>({
        url: "/credit/settlement/pendingOrders",
        method: "get",
        params
    });
}

/**
 * 4.3 确认结算
 * @description POST /adminapi/credit/settlement/submit
 */
export function submitCreditSettlement(data: CreditSettlementSubmitBody) {
    return request<null>({
        url: "/credit/settlement/submit",
        method: "post",
        data
    });
}

/**
 * 4.4 历史结算列表
 * @description GET /adminapi/credit/settlement/history
 */
export function getCreditSettlementHistory(params: CreditSettlementHistoryQuery) {
    return request<CreditSettlementHistoryPage>({
        url: "/credit/settlement/history",
        method: "get",
        params
    });
}

/**
 * 4.6 历史结算订单明细
 * @description GET /adminapi/credit/settlement/historyOrders
 */
export function getCreditSettlementHistoryOrders(params: CreditSettlementHistoryOrdersQuery) {
    return request<CreditSettlementHistoryOrdersPage>({
        url: "/credit/settlement/historyOrders",
        method: "get",
        params
    });
}

/**
 * 4.5 撤销结算
 * @description POST /adminapi/credit/settlement/cancel
 */
export function cancelCreditSettlement(data: CreditSettlementCancelBody) {
    return request<null>({
        url: "/credit/settlement/cancel",
        method: "post",
        data
    });
}
