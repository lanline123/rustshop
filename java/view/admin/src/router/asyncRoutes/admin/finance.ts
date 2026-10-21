import { isMerchant, isStore } from "@/utils/version";

const shopTypeName = isMerchant() ? "店铺" : isStore() ? "门店" : "店铺";
export default {
    path: "/finance",
    name: "finance",
    component: () => import("@/layouts/base/index.vue"),
    meta: { title: "财务" },
    children: [
        {
            path: "",
            name: "userfundsManagement",
            meta: { title: "会员资金管理" },
            redirect: "/finance/account-panel/list",
            children: [
                {
                    path: "account-panel/list",
                    name: "accountPanelManage",
                    meta: { title: "资金总览" },
                    component: () => import("@/views/finance/accountPanel/List.vue")
                },
                {
                    path: "user-withdraw-apply/list",
                    name: "userWithdrawApplyManage",
                    meta: { title: "会员提现" },
                    component: () => import("@/views/finance/userWithdrawApply/Index.vue")
                },
                {
                    path: "user-recharge-order/list",
                    name: "userRechargeOrderManage",
                    meta: { title: "充值管理" },
                    component: () => import("@/views/finance/userRechargeOrder/List.vue")
                },
                {
                    path: "distribution-commission/list",
                    name: "distributionCommissionManage",
                    meta: { title: "分销佣金" },
                    component: () => import("@/views/salesman/performanceSettlement/Index.vue")
                },
                {
                    path: "integral-log/list",
                    name: "integralLogManage",
                    meta: { title: "积分日志" },
                    component: () => import("@/views/user/integralLog/List.vue")
                }
            ]
        },
        {
            path: "",
            name: "profitsharingManage",
            meta: { title: "分账管理" },
            redirect: "/finance/profitsharing-setting",
            children: [
                {
                    path: "profitsharing-setting",
                    name: "profitsharingSettingManage",
                    meta: { title: "分账设置" },
                    component: () => import("@/views/finance/profitsharingSetting/Index.vue")
                },
                {
                    path: "category-service-fee-rule",
                    name: "categoryFeeRuleManage",
                    meta: { title: "服务费率" },
                    component: () => import("@/views/finance/categoryServiceFeeRule/List.vue")
                },
                {
                    path: "withdrawal-setting",
                    name: "withdrawalSettingManage",
                    meta: { title: "提现设置" },
                    component: () => import("@/views/finance/withdrawalSetting/Index.vue")
                },
                // 线上分账（通用能力）：分账结算、子商户绑定、进件管理
                {
                    path: "profit-sharing/list",
                    name: "profitSharingSettlementManage",
                    meta: { title: "分账结算" },
                    component: () => import("@/views/finance/profitSharing/List.vue")
                },
                {
                    path: "sub-merchant/list",
                    name: "subMerchantManage",
                    meta: { title: "子商户管理" },
                    component: () => import("@/views/finance/subMerchant/List.vue")
                },
                {
                    path: "merchant-application/list",
                    name: "merchantApplicationManage",
                    meta: { title: "进件管理" },
                    component: () => import("@/views/finance/merchantApplication/List.vue")
                }
            ]
        },
        {
            path: "",
            name: "depositManage",
            meta: { title: "保证金管理" },
            redirect: "/finance/deposit-query/list",
            children: [
                {
                    path: "deposit-setting",
                    name: "depositSettingManage",
                    meta: { title: "保证金设置" },
                    component: () => import("@/views/finance/deposit/setting/Index.vue")
                },
                {
                    path: "deposit-rule/list",
                    name: "depositRuleManage",
                    meta: { title: "保证金规则" },
                    component: () => import("@/views/finance/deposit/rule/List.vue")
                },
                {
                    path: "deposit-account/list",
                    name: "depositPayeeAccountManage",
                    meta: { title: "保证金账户" },
                    component: () => import("@/views/finance/deposit/account/List.vue")
                },
                {
                    path: "deposit-query/list",
                    name: "depositAccountManage",
                    meta: { title: "保证金查询" },
                    component: () => import("@/views/finance/deposit/shop/List.vue")
                },
                {
                    path: "deposit-audit/list",
                    name: "depositPaymentAuditManage",
                    meta: { title: "款项审核" },
                    component: () => import("@/views/finance/deposit/audit/List.vue")
                },
                {
                    path: "deposit-transaction/list",
                    name: "depositLogManage",
                    meta: { title: "交易明细" },
                    component: () => import("@/views/finance/deposit/transaction/List.vue")
                }
            ]
        },
        {
            path: "",
            name: "shopUserfundsManagement",
            meta: { title: shopTypeName + "资金管理" },
            redirect: "/finance/statement-download/list",
            children: [
                {
                    path: "shop-account/list",
                    name: "shopAccountManage",
                    meta: { title: "资金概览" },
                    component: () => import("@/views/shop/account/List.vue")
                },
                {
                    path: "shop-funds/list",
                    name: "shopFundsManage",
                    meta: { title: shopTypeName + "资金" },
                    component: () => import("@/views/shop/shopFunds/List.vue")
                },
                {
                    path: "statement-download/list",
                    name: "statementDownload",
                    meta: { title: "对账单下载" },
                    component: () => import("@/views/finance/shopFunds/statementDownload/List.vue")
                },
                {
                    path: "statement-details/list",
                    name: "statementDetails",
                    meta: { title: "对账单明细" },
                    component: () => import("@/views/finance/shopFunds/statementDetails/List.vue")
                },
                {
                    path: "shop-withdraw-apply/list",
                    name: "shopWithdrawApplyManage",
                    meta: { title: "提现管理" },
                    component: () => import("@/views/finance/shopFunds/shopAccountRaply/List.vue")
                },
                {
                    path: "shop-withdraw-summary/list",
                    name: "shopWithdrawApplySummary",
                    meta: { title: "提现汇总" },
                    component: () => import("@/views/finance/shopFunds/withdrawSummary/List.vue")
                },
                {
                    path: "shop-financial-log/list",
                    name: "financialLogManage",
                    meta: { title: "资金日志" },
                    component: () => import("@/views/shop/financialLog/List.vue")
                }
            ]
        },
        {
            path: "",
            name: "termPayManagement",
            meta: { title: "账期支付" },
            redirect: "/order/termOrder/list",
            children: [
                {
                    path: "termOrder/list",
                    name: "termOrderManage",
                    meta: { title: "账期支付订单" },
                    component: () => import("@/views/order/termPay/termOrder/List.vue")
                },
                {
                    path: "termPay/list",
                    name: "termPayManage",
                    meta: { title: "账期结算" },
                    component: () => import("@/views/order/termPay/termPay/List.vue")
                },
                {
                    path: "termMember/list",
                    name: "termMemberManage",
                    meta: { title: "授信客户" },
                    component: () => import("@/views/order/termPay/termMember/List.vue")
                }
            ]
        },
        {
            path: "",
            name: "userInvoice",
            meta: { title: "发票管理" },
            redirect: "/finance/user-invoice/list",
            children: [
                {
                    path: "user-invoice/list",
                    name: "userInvoiceManage",
                    meta: { title: "资质审核" },
                    component: () => import("@/views/finance/userInvoice/List.vue")
                },
                {
                    path: "order-invoice/list",
                    name: "orderInvoiceManage",
                    meta: { title: "发票申请" },
                    component: () => import("@/views/finance/orderInvoice/List.vue")
                }
            ]
        },
        {
            path: "",
            name: "refundManagement",
            meta: { title: "退款管理" },
            redirect: "/finance/refund-apply/list",
            children: [
                {
                    path: "refund-apply/list",
                    name: "refundApplyManage",
                    meta: { title: "退款申请" },
                    component: () => import("@/views/finance/refundApply/List.vue")
                },
                {
                    path: "refund-log/list",
                    name: "refundLogManage",
                    meta: { title: "退款日志" },
                    component: () => import("@/views/finance/refundLog/List.vue")
                }
            ]
        },
        {
            path: "",
            name: "transactionManagement",
            meta: { title: "交易管理" },
            redirect: "/finance/paylog/list",
            children: [
                {
                    path: "paylog/list",
                    name: "payLogManage",
                    meta: { title: "交易日志" },
                    component: () => import("@/views/finance/payLog/List.vue")
                },
                {
                    path: "user-balance-log/list",
                    name: "userBalanceLogManage",
                    meta: { title: "余额日志" },
                    component: () => import("@/views/finance/userBalanceLog/List.vue")
                }
            ]
        }
    ]
};

/**
 *  {
            path: "account_panel/list",
            name: "accountPanelManage",
            meta: { title: "账户总览" },
            component: () => import("@/views/finance/accountPanel/List.vue")
        },
        {
            path: "user_withdraw_apply/list",
            name: "userWithdrawApplyManage",
            meta: { title: "提现申请" },
            component: () => import("@/views/finance/userWithdrawApply/List.vue")
        },
        {
            path: "user_recharge_order/list",
            name: "userRechargeOrderManage",
            meta: { title: "充值记录" },
            component: () => import("@/views/finance/userRechargeOrder/List.vue")
        },
        {
            path: "user_balance_log/list",
            name: "userBalanceLogManage",
            meta: { title: "余额日志" },
            component: () => import("@/views/finance/userBalanceLog/List.vue")
        },
        {
            path: "",
            name: "userInvoice",
            meta: { title: "发票管理" },
            redirect: "/finance/user_invoice/list/",
            children: [
                {
                    path: "user_invoice/list/",
                    name: "userInvoiceManage",
                    meta: { title: "发票资质管理" },
                    component: () => import("@/views/finance/userInvoice/List.vue")
                },
                {
                    path: "order_invoice/list/",
                    name: "orderInvoiceManage",
                    meta: { title: "发票申请" },
                    component: () => import("@/views/finance/orderInvoice/List.vue")
                }
            ]
        },
        {
            path: "refund_apply/list",
            name: "refundApplyManage",
            meta: { title: "退款申请" },
            component: () => import("@/views/finance/refundApply/List.vue")
        },
        {
            path: "refund_log/list",
            name: "refundLogManage",
            meta: { title: "退款记录" },
            component: () => import("@/views/finance/refundLog/List.vue")
        },
        {
            path: "paylog/list",
            name: "payLogManage",
            meta: { title: "交易日志" },
            component: () => import("@/views/finance/payLog/List.vue")
        }
 *
 */
