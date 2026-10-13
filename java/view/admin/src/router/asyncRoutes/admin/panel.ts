export default {
    path: "/",
    name: "panel",
    redirect: "/",
    component: () => import("@/layouts/base/index.vue"),
    meta: { title: "面板" },
    children: [
        {
            path: "/",
            name: "consoleManage",
            meta: { title: "概览" },
            component: () => import("@/views/panel/Index.vue")
        },
        {
            path: "",
            name: "salesOverview",
            meta: { title: "销售统计" },
            redirect: "/statistics-order/list",
            children: [
                {
                    path: "statistics-order/list",
                    name: "statisticsOrder",
                    meta: { title: "销售概览" },
                    component: () => import("@/views/panel/statisticsOrder/List.vue")
                },
                {
                    path: "statistics-sale/list",
                    name: "statisticsSale",
                    meta: { title: "销售明细" },
                    component: () => import("@/views/panel/statisticsSale/List.vue")
                },
                {
                    path: "sale-targets/list",
                    name: "saleTargets",
                    meta: { title: "销售指标" },
                    component: () => import("@/views/panel/saleTargets/List.vue")
                },
                {
                    path: "statistics-goods/list",
                    name: "statisticsGoods",
                    meta: { title: "销售排行" },
                    component: () => import("@/views/panel/statisticsGoods/List.vue")
                }
            ]
        },
        {
            path: "",
            name: "userOverview",
            meta: { title: "用户统计" },
            redirect: "/statistics-user/list",
            children: [
                {
                    path: "statistics-user/list",
                    name: "statisticsUser",
                    meta: { title: "用户概览" },
                    component: () => import("@/views/panel/statisticsUser/List.vue")
                },
                {
                    path: "statistics-access/list",
                    name: "statisticsAccess",
                    meta: { title: "访问统计" },
                    component: () => import("@/views/panel/statisticsAccess/List.vue")
                },
                {
                    path: "consumer-ranking/list",
                    name: "consumerRanking",
                    meta: { title: "消费排行" },
                    component: () => import("@/views/panel/consumerRanking/List.vue")
                },
                {
                    path: "new-members/list",
                    name: "newMembers",
                    meta: { title: "新增会员" },
                    component: () => import("@/views/panel/newMembers/List.vue")
                }
            ]
        }
        // {
        //     path: "/:pathMatch(.*)",
        //     name: "notFound",
        //     meta: { is404: true },
        //     component: NotFound
        // }
    ]
};
