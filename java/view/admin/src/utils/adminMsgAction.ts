export type AdminMsgRoute =
    | "memberFeedback"
    | "productInquiry"
    | "order"
    | "aftersale"
    | "refundApply"
    | "product"
    | "withdrawal"
    | "shopWithdrawal"
    | "invoice"
    | "deposit";

export interface AdminMsgActionItem {
    msgType?: number;
    route?: string;
    relatedData?: Record<string, any>;
    orderId?: number;
    goodsId?: number;
    productId?: number;
}

export interface AdminMsgAction {
    route: AdminMsgRoute;
    id?: number;
    feedbackType?: number;
    params?: Record<string, any>;
}

const MSG_TYPE_ROUTE: Record<number, AdminMsgRoute> = {
    11: "order",
    12: "order",
    13: "order",
    15: "memberFeedback",
    16: "memberFeedback",
    21: "product",
    22: "product",
    23: "product",
    24: "product",
    25: "memberFeedback",
    26: "productInquiry",
    31: "order",
    32: "aftersale",
    33: "withdrawal",
    34: "invoice",
    35: "invoice",
    36: "refundApply",
    44: "deposit",
    53: "memberFeedback",
    54: "order",
    55: "memberFeedback",
    56: "memberFeedback",
    57: "memberFeedback"
};

const ROUTES = new Set<AdminMsgRoute>([
    "memberFeedback",
    "productInquiry",
    "order",
    "aftersale",
    "refundApply",
    "product",
    "withdrawal",
    "shopWithdrawal",
    "invoice",
    "deposit"
]);
const FEEDBACK_TYPE_BY_MSG_TYPE: Record<number, number> = { 15: 5, 16: 6, 25: 2, 55: 0, 56: 1, 57: 3 };

const toPositiveId = (value: unknown): number | undefined => {
    const id = Number(value);
    return Number.isFinite(id) && id > 0 ? id : undefined;
};

export const getFeedbackId = (relatedData?: Record<string, any>): number | undefined => toPositiveId(relatedData?.feedbackId) ?? toPositiveId(relatedData?.id);

export function resolveAdminMsgAction(item: AdminMsgActionItem): AdminMsgAction | null {
    const relatedData = item.relatedData || {};
    const rawRoute = relatedData.route ?? item.route;
    const msgType = Number(item.msgType);
    const route = relatedData.shopWithdrawLogId
        ? "shopWithdrawal"
        : msgType === 36
          ? "refundApply"
          : ROUTES.has(rawRoute as AdminMsgRoute)
            ? (rawRoute as AdminMsgRoute)
            : MSG_TYPE_ROUTE[msgType];
    if (!route) return null;

    if (route === "memberFeedback") {
        return {
            route,
            id: getFeedbackId(relatedData),
            feedbackType: toPositiveId(relatedData.type) ?? (relatedData.type === 0 ? 0 : FEEDBACK_TYPE_BY_MSG_TYPE[msgType])
        };
    }
    if (route === "productInquiry") return { route, id: toPositiveId(relatedData.inquiryId) };
    if (route === "order") return { route, id: toPositiveId(relatedData.orderId) ?? toPositiveId(item.orderId) };
    if (route === "aftersale") return { route, id: toPositiveId(relatedData.aftersaleId) };
    if (route === "refundApply") return { route, id: toPositiveId(relatedData.refundId) };
    if (route === "product") {
        const shopId = toPositiveId(relatedData.shopId);
        return {
            route,
            id: toPositiveId(relatedData.productId) ?? toPositiveId(relatedData.vendorProductId) ?? toPositiveId(item.productId) ?? toPositiveId(item.goodsId),
            params: shopId ? { shopId } : undefined
        };
    }
    if (route === "withdrawal") return { route, id: toPositiveId(relatedData.withdrawApplyId) };
    if (route === "shopWithdrawal") return { route, id: toPositiveId(relatedData.shopWithdrawLogId) };
    if (route === "invoice") {
        return {
            route,
            id: toPositiveId(relatedData.invoiceId) ?? toPositiveId(relatedData.orderInvoiceId)
        };
    }
    return {
        route,
        id: toPositiveId(relatedData.ownerId),
        params: { ownerId: toPositiveId(relatedData.ownerId), ownerType: relatedData.ownerType, shopId: relatedData.shopId }
    };
}
