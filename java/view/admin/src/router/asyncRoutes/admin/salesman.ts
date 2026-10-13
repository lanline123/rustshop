export default {
    path: "/salesman",
    name: "salesman",
    component: () => import("@/layouts/base/index.vue"),
    meta: { title: "分销" },
    children: [
        {
            path: "/salesman",
            name: "overviewManage",
            meta: { title: "分销概览" },
            component: () => import("@/views/salesman/overview/Index.vue")
        },
        {
            path: "",
            name: "distributionSettings",
            meta: { title: "分销设置" },
            redirect: "/salesman/promoteMode/list",
            children: [
                {
                    path: "promoteMode/list",
                    name: "promoteModeManage",
                    meta: { title: "分销模式" },
                    component: () => import("@/views/salesman/promoteMode/List.vue")
                }
            ]
        },

        {
            path: "",
            name: "salesmanProduct",
            meta: { title: "分销商品" },
            redirect: "/salesman/product/list",
            children: [
                {
                    path: "product/list",
                    name: "salesmanProductManage",
                    meta: { title: "商品佣金管理" },
                    component: () => import("@/views/salesman/product/List.vue")
                },
                {
                    path: "overview/analysis-table",
                    name: "AnalysisTableManage",
                    meta: { title: "成交分析" },
                    component: () => import("@/views/salesman/overview/src/AnalysisTable.vue")
                }
            ]
        },
        {
            path: "",
            name: "salesmanManage",
            meta: { title: "分销员" },
            redirect: "/salesman/promoter/list",
            children: [
                {
                    path: "promoter/list",
                    name: "promoterManage",
                    meta: { title: "分销员管理" },
                    component: () => import("@/views/salesman/promoter/List.vue")
                },
                {
                    path: "subgroup/list",
                    name: "subgroupManage",
                    meta: { title: "分销员分组" },
                    component: () => import("@/views/salesman/subgroup/List.vue")
                },
                {
                    path: "overview/ranking-table",
                    name: "RankingTableManage",
                    meta: { title: "分销员排行" },
                    component: () => import("@/views/salesman/overview/src/RankingTable.vue")
                },
                {
                    path: "salesman-detailed/list",
                    name: "salesmanDetailedManage",
                    meta: { title: "分销员明细" },
                    component: () => import("@/views/salesman/salesmanDetailed/List.vue")
                },
                {
                    path: "customer-transactions/list",
                    name: "customerTransactionsManage",
                    meta: { title: "客户成交" },
                    component: () => import("@/views/salesman/customerTransactions/List.vue")
                }
            ]
        },
        {
            path: "",
            name: "performanceSettlement",
            meta: { title: "分销结算" },
            redirect: "/salesman/performance-settlement/setting/index",
            children: [
                {
                    path: "performance-settlement/setting/index",
                    name: "performanceSettlementSettingManage",
                    meta: { title: "结算方案设置" },
                    component: () => import("@/views/salesman/performanceSettlement/setting/Index.vue")
                },
                {
                    path: "performance-settlement/list",
                    name: "performanceSettlementManage",
                    meta: { title: "业绩结算" },
                    component: () => import("@/views/salesman/performanceSettlement/performanceSettlement/List.vue")
                }
            ]
        },
        {
            path: "",
            name: "noticeManage",
            meta: { title: "内容管理" },
            redirect: "/salesman/notice/list",
            children: [
                {
                    path: "notice/list",
                    name: "salesmanNoticeManage",
                    meta: { title: "分销攻略" },
                    component: () => import("@/views/salesman/notice/List.vue")
                },
                {
                    path: "material-category/list",
                    name: "materialCategoryManage",
                    meta: { title: "素材分类" },
                    component: () => import("@/views/salesman/category/List.vue")
                },
                {
                    path: "material/list",
                    name: "materialManage",
                    meta: { title: "素材管理" },
                    component: () => import("@/views/salesman/material/List.vue")
                }
            ]
        }
    ]
};
