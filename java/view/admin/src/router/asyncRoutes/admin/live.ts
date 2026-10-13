export default {
    path: "/live",
    name: "live",
    component: () => import("@/layouts/base/index.vue"),
    meta: { title: "直播" },
    children: [
        {
            path: "",
            name: "wechatLiveManagement",
            meta: { title: "小程序直播" },
            redirect: "/content/wechat-live/list",
            children: [
                {
                    path: "wechat-live/list",
                    name: "wechatLive",
                    meta: { title: "小程序直播" },
                    component: () => import("@/views/content/wechat-live/List.vue")
                },
                {
                    path: "wechat-live-product/list",
                    name: "wechatLiveProduct",
                    meta: { title: "直播商品" },
                    component: () => import("@/views/content/wechat-live-product/List.vue")
                }
            ]
        },
        {
            path: "",
            name: "videoNumberManagement",
            meta: { title: "视频号" },
            redirect: "/content/videoNumber/list",
            children: [
                {
                    path: "videoNumber/list",
                    name: "videoNumber",
                    meta: { title: "视频号列表" },
                    component: () => import("@/views/content/videoNumber/List.vue")
                },
            ]
        }
    ]
};
