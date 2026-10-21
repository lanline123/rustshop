/**
 * @file 店铺保证金 平台端接口（统一聚合 5.1~5.6）
 * @description 接口 url 为后端路由契约，联调时以后端为准
 */
import request from "@/utils/request";
import dayjs from "dayjs";
import type {
    AccountFilterParams,
    AccountFilterResult,
    AdjustDepositParams,
    AdjustLogParams,
    AdjustLogResult,
    AuditRechargeParams,
    AuditRefundParams,
    DeductDepositParams,
    DepositOwnerParams,
    DepositQueryDetail,
    DepositQueryFilterParams,
    DepositQueryFilterResult,
    DepositRuleFilterParams,
    DepositRuleFilterResult,
    DepositRuleFilterState,
    DepositSettingState,
    DepositTransactionDetail,
    DepositTransactionFilterParams,
    DepositTransactionFilterResult,
    PaymentLogParams,
    PaymentLogResult,
    RechargeAuditDetail,
    RechargeAuditFilterParams,
    RechargeAuditFilterResult,
    RefundAuditDetail,
    RefundAuditFilterParams,
    RefundAuditFilterResult,
    SendWarningParams
} from "@/types/finance/deposit.d";

/* ---------------- 5.1 保证金设置 ---------------- */

export const getDepositSetting = () => request<DepositSettingState>({ url: "deposit/setting/detail", method: "get" });

export const updateDepositSetting = (data: DepositSettingState) => request({ url: "deposit/setting/save", method: "post", data });

/* ---------------- 5.2 保证金规则 ---------------- */

export const getDepositRuleList = (params: DepositRuleFilterParams) => {
    const { startDate, endDate, ...rest } = params;
    return request<DepositRuleFilterResult>({
        url: "deposit/rule/list",
        method: "get",
        params: {
            ...rest,
            startTime: startDate ? dayjs(startDate).startOf("day").unix() : undefined,
            endTime: endDate ? dayjs(endDate).endOf("day").unix() : undefined
        }
    });
};

export const getDepositRule = (action: string, params: object) =>
    request<DepositRuleFilterState>({ url: `deposit/rule/${action}`, method: "get", params });

export const updateDepositRule = (operation: string, data: object) => request({ url: `deposit/rule/${operation}`, method: "post", data });
// operation: create / update / status

export const delDepositRule = (data: object) => request({ url: "deposit/rule/del", method: "post", data });

/* ---------------- 5.3 保证金账户（平台收款账户） ---------------- */

export const getDepositAccountList = (params?: AccountFilterParams) =>
    request<AccountFilterResult>({ url: "deposit/payeeAccount/list", method: "get", params });

export const delDepositAccount = (data: object) => request({ url: "deposit/payeeAccount/del", method: "post", data });

export const updateDepositAccount = (operation: string, data: object) => request({ url: "deposit/payeeAccount/" + operation, method: "post", data });
// operation: create / update

/* ---------------- 5.4 保证金查询（店铺保证金账户） ---------------- */

export const getDepositQueryList = (params: DepositQueryFilterParams) => {
    const { startDate, endDate, ...rest } = params;
    return request<DepositQueryFilterResult>({
        url: "deposit/account/list",
        method: "get",
        params: {
            ...rest,
            joinTimeStart: startDate ? dayjs(startDate).startOf("day").unix() : undefined,
            joinTimeEnd: endDate ? dayjs(endDate).endOf("day").unix() : undefined
        }
    });
};

export const getDepositQueryDetail = (params: DepositOwnerParams) =>
    request<DepositQueryDetail>({ url: "deposit/account/detail", method: "get", params });

export const getDepositPaymentLog = (params: PaymentLogParams) =>
    request<PaymentLogResult>({ url: "deposit/account/logList", method: "get", params });

export const getDepositAdjustLog = (params: AdjustLogParams) =>
    request<AdjustLogResult>({ url: "deposit/account/adjustLogList", method: "get", params });

export const adjustDeposit = (data: AdjustDepositParams) => request({ url: "deposit/account/adjust", method: "post", data });

export const deductDeposit = (data: DeductDepositParams) => request({ url: "deposit/account/deduct", method: "post", data });

export const sendDepositWarning = (data: SendWarningParams) => request({ url: "deposit/account/sendWarning", method: "post", data });

/* ---------------- 5.5 款项审核 ---------------- */

export const getRechargeAuditList = (params: RechargeAuditFilterParams) => {
    const { startDate, endDate, ...rest } = params;
    return request<RechargeAuditFilterResult>({
        url: "deposit/recharge/list",
        method: "get",
        params: {
            ...rest,
            startTime: startDate ? dayjs(startDate).startOf("day").unix() : undefined,
            endTime: endDate ? dayjs(endDate).endOf("day").unix() : undefined
        }
    });
};

export const getRechargeAuditDetail = (params: { id: number }) =>
    request<RechargeAuditDetail>({ url: "deposit/recharge/detail", method: "get", params });

// data: { id, pass: boolean, auditRemark }
export const auditRecharge = (data: AuditRechargeParams) => request({ url: "deposit/recharge/audit", method: "post", data });

export const getRefundAuditList = (params: RefundAuditFilterParams) => {
    const { startDate, endDate, ...rest } = params;
    return request<RefundAuditFilterResult>({
        url: "deposit/refund/list",
        method: "get",
        params: {
            ...rest,
            startTime: startDate ? dayjs(startDate).startOf("day").unix() : undefined,
            endTime: endDate ? dayjs(endDate).endOf("day").unix() : undefined
        }
    });
};

export const getRefundAuditDetail = (params: { id: number }) =>
    request<RefundAuditDetail>({ url: "deposit/refund/detail", method: "get", params });

// data: { id, pass: boolean, payVoucher?, auditRemark }；一步式：通过时 payVoucher 必填
export const auditRefund = (data: AuditRefundParams) => request({ url: "deposit/refund/audit", method: "post", data });

/* ---------------- 5.6 交易明细 ---------------- */

export const getDepositTransactionList = (params: DepositTransactionFilterParams) => {
    const { startDate, endDate, ...rest } = params;
    return request<DepositTransactionFilterResult>({
        url: "deposit/log/list",
        method: "get",
        params: {
            ...rest,
            startTime: startDate ? dayjs(startDate).startOf("day").unix() : undefined,
            endTime: endDate ? dayjs(endDate).endOf("day").unix() : undefined
        }
    });
};

export const getDepositTransaction = (params: { id: number }) =>
    request<DepositTransactionDetail>({ url: "deposit/log/detail", method: "get", params });
