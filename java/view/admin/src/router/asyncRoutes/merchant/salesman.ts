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
            name: "performanceSettlement",
            meta: { title: "分销结算" },
            redirect: "/salesman/performance-settlement/list",
            children: [
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
            redirect: "/salesman/material/list",
            children: [
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
