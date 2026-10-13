export default {
    path: "/capitalfund",
    name: "capitalfund",
    redirect: "/capitalfund/dashboard/index",
    component: () => import("@/layouts/base/index.vue"),
    meta: { title: "资产" },
    children: [
        {
            path: "dashboard/index",
            name: "dashboardManage",
            meta: { title: "资产总览" },
            component: () => import("@/views/merchant/capitalfund/dashboard/Index.vue")
        },
        {
            path: "account/list",
            name: "accountManage",
            meta: { title: "账户列表" },
            component: () => import("@/views/merchant/capitalfund/account/List.vue")
        },
        {
            path: "balance/list",
            name: "balanceManage",
            meta: { title: "店铺余额" },
            component: () => import("@/views/merchant/capitalfund/balance/List.vue")
        },
        {
            path: "withdraw/index",
            name: "withdrawManage",
            meta: { title: "提现" },
            component: () => import("@/views/merchant/capitalfund/withdraw/Index.vue")
        }
    ]
};
