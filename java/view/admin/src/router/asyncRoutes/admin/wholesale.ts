export default {
    path: "/wholesale",
    name: "wholesaleManage",
    component: () => import("@/layouts/base/index.vue"),
    meta: { title: "批发" },
    children: [
        {
            path: "",
            name: "wholesalerGroup",
            meta: { title: "批发商" },
            redirect: "/wholesale/wholesaler/list",
            children: [
                {
                    path: "wholesaler/list",
                    name: "wholesalerManage",
                    meta: { title: "批发商列表" },
                    component: () => import("@/views/wholesale/wholesaler/List.vue")
                },
                {
                    path: "grade/list",
                    name: "wholesalerGradeManage",
                    meta: { title: "等级管理" },
                    component: () => import("@/views/wholesale/grade/List.vue")
                },
                {
                    path: "setting",
                    name: "wholesaleSettingManage",
                    meta: { title: "批发商设置" },
                    component: () => import("@/views/wholesale/setting/Index.vue")
                }
            ]
        },
        {
            path: "",
            name: "wholesaleProductGroup",
            meta: { title: "批发商品" },
            redirect: "/wholesale/product/list",
            children: [
                {
                    path: "product/list",
                    name: "wholesaleProductManage",
                    meta: { title: "批发商品列表" },
                    component: () => import("@/views/wholesale/product/List.vue")
                }
            ]
        }
    ]
};
