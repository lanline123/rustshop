export default {
    path: "/content",
    name: "content",
    component: () => import("@/layouts/base/index.vue"),
    meta: { title: "内容" },
    children: [
        {
            path: "",
            name: "articleManagement",
            meta: { title: "文章管理" },
            redirect: "/content/article/list",
            children: [
                {
                    path: "article/list",
                    name: "articleManage",
                    meta: { title: "文章列表" },
                    component: () => import("@/views/content/article/List.vue")
                },
                {
                    path: "article-category/list",
                    name: "articleCategoryManage",
                    meta: { title: "文章分类" },
                    component: () => import("@/views/content/articleCategory/List.vue")
                }
            ]
        },
        {
            path: "",
            name: "materialManagement",
            meta: { title: "素材管理" },
            redirect: "/content/material-center",
            children: [
                {
                    path: "material-center",
                    name: "materialCenter",
                    meta: { title: "素材中心" },
                    component: () => import("@/views/content/material-center/Index.vue")
                }
            ]
        },
        {
            path: "",
            name: "protocolManagement",
            meta: { title: "协议管理" },
            redirect: "/content/service-agreement/list",
            children: [
                {
                    path: "service-agreement/list",
                    name: "serviceAgreement",
                    meta: { title: "服务协议" },
                    component: () => import("@/views/content/agreement/ServiceAgreement.vue")
                },
                {
                    path: "privacy-agreement/list",
                    name: "privacyAgreement",
                    meta: { title: "隐私政策" },
                    component: () => import("@/views/content/agreement/PrivacyAgreement.vue")
                }
                // {
                //     path: "after-sale-service/list",
                //     name: "afterSaleService",
                //     meta: { title: "售后服务" },
                //     component: () => import("@/views/content/agreement/AfterSaleService.vue")
                // }
            ]
        }
    ]
};
