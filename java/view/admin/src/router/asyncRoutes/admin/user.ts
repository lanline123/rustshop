export default {
    path: "/user",
    name: "user",
    component: () => import("@/layouts/base/index.vue"),
    meta: { title: "客户" },
    children: [
        {
            path: "",
            name: "userManagement",
            meta: { title: "客户管理" },
            redirect: "/user/list",
            children: [
                {
                    path: "list",
                    name: "userManage",
                    meta: { title: "客户管理" },
                    component: () => import("@/views/user/user/List.vue")
                },
                {
                    path: "level-manage/list",
                    name: "levelManageManage",
                    meta: { title: "等级权益" },
                    component: () => import("@/views/user/levelManage/List.vue")
                },
                {
                    path: "level-manage-pro/list",
                    name: "userLevelProManage",
                    meta: { title: "等级权益" },
                    component: () => import("@/views/user/levelManagePro/List.vue")
                },
                {
                    path: "user-tags/list",
                    name: "memberTagManage",
                    meta: { title: "客户标签" },
                    component: () => import("@/views/user/userTags/List.vue")
                },
                {
                    // D1116-shopMember：客户设置（店铺客户总开关 + 信息脱敏/添加/导入）。
                    // 权限点 authoritySn=shopMemberSettingManage（对齐后端接口文档），菜单显示名以后端 authorityName 为准。
                    path: "customer-setting",
                    name: "shopMemberSettingManage",
                    meta: { title: "客户设置" },
                    component: () => import("@/views/user/shopMember/Index.vue")
                },
                {
                    path: "feedback/list",
                    name: "feedbackManage",
                    meta: { title: "留言反馈" },
                    component: () => import("@/views/user/feedback/List.vue")
                }
            ]
        },
        {
            path: "",
            name: "realNameAuthentication",
            meta: { title: "实名认证" },
            redirect: "/user/user-authentication",
            children: [
                {
                    path: "user-authentication",
                    name: "userAuthenticationManage",
                    meta: { title: "认证设置" },
                    component: () => import("@/views/setting/config/base/src/AuthenticationSettings.vue")
                },
                {
                    path: "user-certification/list",
                    name: "userCertificationManage",
                    meta: { title: "客户认证" },
                    component: () => import("@/views/user/userCertification/List.vue")
                }
            ]
        },
        {
            path: "",
            name: "messagegManage",
            meta: { title: "消息管理" },
            redirect: "/user/message-log/list",
            children: [
                {
                    path: "message-log/list",
                    name: "messageLogManage",
                    meta: { title: "站内信" },
                    component: () => import("@/views/user/messageLog/List.vue")
                }
            ]
        }

        // {
        //     path: "",
        //     name: "userfundsManagement",
        //     meta: { title: "资金管理" },
        //     children: [
        //         {
        //             path: "account-panel/list",
        //             name: "accountPanelManage",
        //             meta: { title: "资金总览" },
        //             component: () => import("@/views/finance/accountPanel/List.vue")
        //         },
        //         {
        //             path: "user-withdraw-apply/list",
        //             name: "userWithdrawApplyManage",
        //             meta: { title: "提现管理" },
        //             component: () => import("@/views/finance/userWithdrawApply/List.vue")
        //         },
        //         {
        //             path: "user-recharge-order/list",
        //             name: "userRechargeOrderManage",
        //             meta: { title: "充值管理" },
        //             component: () => import("@/views/finance/userRechargeOrder/List.vue")
        //         },
        //         {
        //             path: "integral-log/list",
        //             name: "integralLogManage",
        //             meta: { title: "积分日志" },
        //             component: () => import("@/views/user/integralLog/List.vue")
        //         }
        //     ]
        // },
        // {
        //     path: "",
        //     name: "userInvoice",
        //     meta: { title: "发票管理" },
        //     children: [
        //         {
        //             path: "user-invoice/list/",
        //             name: "userInvoiceManage",
        //             meta: { title: "发票资质" },
        //             component: () => import("@/views/finance/userInvoice/List.vue")
        //         },
        //         {
        //             path: "order-invoice/list/",
        //             name: "orderInvoiceManage",
        //             meta: { title: "发票申请" },
        //             component: () => import("@/views/finance/orderInvoice/List.vue")
        //         }
        //     ]
        // },
        // {
        //     path: "",
        //     name: "refundManagement",
        //     meta: { title: "退款管理" },
        //     children: [
        //         {
        //             path: "refund-apply/list",
        //             name: "refundApplyManage",
        //             meta: { title: "退款申请" },
        //             component: () => import("@/views/finance/refundApply/List.vue")
        //         },
        //         {
        //             path: "refund-log/list",
        //             name: "refundLogManage",
        //             meta: { title: "退款日志" },
        //             component: () => import("@/views/finance/refundLog/List.vue")
        //         }
        //     ]
        // },
        // {
        //     path: "",
        //     name: "transactionManagement",
        //     meta: { title: "交易管理" },
        //     children: [
        //         {
        //             path: "paylog/list",
        //             name: "payLogManage",
        //             meta: { title: "交易日志" },
        //             component: () => import("@/views/finance/payLog/List.vue")
        //         },
        //         {
        //             path: "user-balance-log/list",
        //             name: "userBalanceLogManage",
        //             meta: { title: "余额日志" },
        //             component: () => import("@/views/finance/userBalanceLog/List.vue")
        //         }
        //     ]
        // }
    ]
};
