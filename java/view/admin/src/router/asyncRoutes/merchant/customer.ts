// 店铺/门店端「会员」模块：会员 → 会员管理 →（会员管理 / 会员标签 / 商品咨询）。
// 三级结构必须与后端权限树同构：会员(user) → 会员管理(userManagement) → 叶子；
// 否则中间层缺 authoritySn/isShow 会被 pageLeftMenu 过滤掉（详见 spec §12）。
// 叶子 authoritySn 以后端接口文档为准（shopMemberCustomerManage / memberTagManage / feedbackManage）。
// o2o（getShopType()===2）文案「店铺」→「门店」由页面内运行时处理。
export default {
    path: "/user",
    name: "user",
    component: () => import("@/layouts/base/index.vue"),
    meta: { title: "会员" },
    children: [
        {
            path: "",
            name: "userManagement",
            meta: { title: "会员管理" },
            redirect: "/user/shop-customer/list",
            children: [
                {
                    path: "shop-customer/list",
                    name: "shopMemberCustomerManage",
                    meta: { title: "会员管理" },
                    component: () => import("@/views/merchant/customer/customer/List.vue")
                },
                {
                    // 标签管理与平台端「会员标签」完全一致，直接复用平台组件（后端按登录店铺维度隔离数据）
                    path: "shop-customer-tag/list",
                    name: "memberTagManage",
                    meta: { title: "会员标签" },
                    component: () => import("@/views/user/userTags/List.vue")
                },
                {
                    path: "feedback/list",
                    name: "feedbackManage",
                    meta: { title: "商品咨询" },
                    component: () => import("@/views/user/feedback/List.vue")
                }
            ]
        }
    ]
};
