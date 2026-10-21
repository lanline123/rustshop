
export default [
    {
        path: "/login",
        name: "login",
        component: () => import("@/layouts/blank/index.vue"),
        meta: { title: "登录" },
        redirect: "/login/index",
        children: [
            {
                path: "index",
                name: "loginIndex",
                meta: { title: "登录" },
                component: () => import("@/views/login/Login.vue")
            }
        ]
    },
    {
        path: "/sso",
        name: "ssoLayout",
        component: () => import("@/layouts/blank/index.vue"),
        meta: { title: "跳转中" },
        children: [
            {
                path: "",
                name: "ssoEntry",
                meta: { title: "跳转中" },
                component: () => import("@/views/sso/Index.vue")
            }
        ]
    },
    {
        path: "/print",
        name: "print",
        component: () => import("@/layouts/blank/index.vue"),
        meta: { title: "打印" },
        children: [
            {
                path: "orderPrint",
                name: "orderPrintManage",
                meta: { title: "订单打印" },
                component: () => import("@/views/print/orderPrint/print/Index.vue")
            },
            {
                path: "waybill",
                name: "waybillManage",
                meta: { title: "面单打印" },
                component: () => import("@/views/print/orderPrint/Waybill.vue")
            }
        ]
    },
    {
        path: "/decorate",
        name: "decorateEdit",
        component: () => import("@/layouts/decorate/index.vue"),
        meta: { title: "装修" },
        children: [
            {
                path: "index",
                name: "mobileDecorateEdit",
                meta: { title: "装修" },
                component: () => import("@/views/decorate/decorate/editor/Index.vue"),
                props: { decorateType: 1 }
            },
            {
                path: "pc",
                name: "pcDecorateEdit",
                meta: { title: "PC首页装修" },
                component: () => import("@/views/decorate/decorate/editor/Index.vue"),
                props: { decorateType: 2 }
            },
            {
                path: "pcCustomPage",
                name: "pcCustomPageDecorateEdit",
                meta: { title: "PC自定义页装修" },
                component: () => import("@/views/decorate/decorate/editor/Index.vue"),
                props: { decorateType: 3 }
            }
        ]
    },
    {
        path: "/product/publish",
        name: "productPublishLayout",
        component: () => import("@/layouts/blank/index.vue"),
        meta: { title: "发布商品" },
        children: [
            {
                path: "",
                name: "productPublish",
                meta: { title: "发布商品" },
                component: () => import("@/views/product/product/Publish.vue")
            }
        ]
    },
    {
        path: "/im",
        name: "im",
        component: () => import("@/layouts/im/index.vue"),
        meta: { title: "客服" },
        redirect: "/im/index",
        children: [
            {
                path: "index",
                name: "imChart",
                meta: { title: "即时会话" },
                component: () => import("@/views/im/Index.vue")
            },
            {
                path: "history",
                name: "history",
                meta: { title: "历史会话" },
                component: () => import("@/views/im/History.vue")
            },
            {
                path: "systemSetting",
                name: "systemSetting",
                meta: { title: "系统设置" },
                children: [
                    {
                        path: "autoReply",
                        name: "autoReply",
                        meta: { title: "客服自动回复" },
                        component: () => import("@/views/im/AutoReply.vue")
                    }
                ]
            }
        ]
    }
    // {
    //     path: "/:pathMatch(.*)*",
    //     name: "notFound",
    //     meta: { is404: true },
    //     redirect: "/404",
    //     component: () => import("@/layouts/base/index.vue"),
    //     children: [
    //         {
    //             path: "/404",
    //             name: "notFoundIndex",
    //             meta: { title: "404" },
    //             component: () => import("@/layouts/base/404.vue")
    //         }
    //     ]
    // }
];
