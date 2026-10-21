/**
 * @file 店铺/商家后台 保证金接口
 * @description 接口地址对齐后端文档（adminapi 前缀已在请求 baseURL 中），店铺走 deposit/shop/*，供应商走 deposit/vendor/*
 */
import request from "@/utils/request";
import { getAdminType } from "@/utils/storage";
import type {
    PayeeAccountOption,
    PayeeAccountOptionParams,
    ShopDepositAuditDetailParams,
    ShopDepositAuditFilterParams,
    ShopDepositAuditResult,
    ShopDepositAuditState,
    ShopDepositFrozenFilterParams,
    ShopDepositFrozenResult,
    ShopDepositLogFilterParams,
    ShopDepositLogResult,
    DepositFullRefundCheck,
    ShopDepositOverview,
    ShopDepositProtocol,
    ShopDepositRechargeParams,
    ShopDepositRefundParams,
    ShopDepositWarning
} from "@/types/merchant/capitalfund/deposit.d";

const isVendor = () => getAdminType() === "vendor";

/** 店铺 / 供应商同功能接口路径切换 */
const depositOwnerPath = (shopPath: string, vendorPath: string) => (isVendor() ? vendorPath : shopPath);

// 首页保证金预警横幅
export const getShopDepositWarning = () =>
    request<ShopDepositWarning>({ url: depositOwnerPath("deposit/shop/warning", "deposit/vendor/warning"), method: "get" });

// 保证金协议
export const getShopDepositProtocol = () =>
    request<ShopDepositProtocol>({ url: depositOwnerPath("deposit/shop/protocol", "deposit/vendor/protocol"), method: "get" });

// 保证金总览
export const getShopDepositOverview = () =>
    request<ShopDepositOverview>({ url: depositOwnerPath("deposit/shop/overview", "deposit/vendor/overview"), method: "get" });

// 交易流水
export const getShopDepositLogList = (params: ShopDepositLogFilterParams) =>
    request<ShopDepositLogResult>({ url: depositOwnerPath("deposit/shop/logList", "deposit/vendor/logList"), method: "get", params });

// 冻结明细（平台扣款记录）
export const getShopDepositFrozenList = (params: ShopDepositFrozenFilterParams) =>
    request<ShopDepositFrozenResult>({
        url: depositOwnerPath("deposit/shop/frozenDetailList", "deposit/vendor/frozenDetailList"),
        method: "get",
        params
    });

// 款项审核合并列表（充值 + 退还混合）
export const getShopDepositAuditList = (params: ShopDepositAuditFilterParams) =>
    request<ShopDepositAuditResult>({
        url: depositOwnerPath("deposit/recharge/shopPaymentAuditList", "deposit/recharge/vendorPaymentAuditList"),
        method: "get",
        params
    });

// 款项审核详情（owner 由登录态自动锁定，不可使用平台端 recharge/detail、refund/detail）
export const getShopDepositAuditDetail = (params: ShopDepositAuditDetailParams) =>
    request<ShopDepositAuditState>({
        url: depositOwnerPath("deposit/recharge/shopPaymentAuditDetail", "deposit/recharge/vendorPaymentAuditDetail"),
        method: "get",
        params
    });

// 店铺端发起充值申请
export const getRechargePayeeAccountOptions = (params?: PayeeAccountOptionParams) =>
    request<PayeeAccountOption[]>({ url: "deposit/recharge/payeeAccountOptions", method: "get", params });

export const submitShopDepositRecharge = (data: ShopDepositRechargeParams) =>
    request({ url: depositOwnerPath("deposit/recharge/apply", "deposit/recharge/vendorApply"), method: "post", data });

// 全额退还预检
export const getShopDepositFullRefundCheck = () =>
    request<DepositFullRefundCheck>({
        url: depositOwnerPath("deposit/refund/fullRefundCheck", "deposit/refund/vendorFullRefundCheck"),
        method: "get"
    });

// 发起退还/提现申请
export const submitShopDepositRefund = (data: ShopDepositRefundParams) =>
    request({ url: depositOwnerPath("deposit/refund/apply", "deposit/refund/vendorApply"), method: "post", data });
