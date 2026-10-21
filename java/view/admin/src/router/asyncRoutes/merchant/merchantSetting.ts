export default {
    path: "/merchant_setting",
    name: "merchantSetting",
    meta: { title: "设置" },
    redirect: "/setting/mobile-shop-decorate",
    component: () => import("@/layouts/base/index.vue"),
    children: [
        {
            path: "mobile-shop-decorate",
            name: "mobileShopDecorate",
            meta: { title: "页面管理" },
            component: () => import("@/views/merchant/setting/mobileShopDecorate/List.vue")
        },
        {
            path: "shop-team",
            name: "ShopTeamManage",
            meta: { title: "店铺设置" },
            component: () => import("@/views/merchant/setting/team/Index.vue")
        },
        {
            path: "shop-info",
            name: "ShopInfoManage",
            meta: { title: "店铺信息" },
            component: () => import("@/views/merchant/setting/shopInfo/Index.vue")
        },
        {
            path: "employee/list/",
            name: "EmployeeManagement",
            meta: { title: "员工列表" },
            component: () => import("@/views/authority/adminUser/ShopList.vue")
        },
        {
            path: "employeeLog/list/",
            name: "EmployeeLogManagement",
            meta: { title: "员工操作日志" },
            component: () => import("@/views/merchant/setting/employeeLog/List.vue")
        },
        {
            path: "shop-role/list/",
            name: "ShopRoleManage",
            meta: { title: "角色管理" },
            component: () => import("@/views/authority/adminRole/List.vue")
        },
        {
            path: "shop-shipping",
            name: "shopShipping",
            meta: { title: "配送/运费设置" },
            children: [
                {
                    path: "shop-shipping-type/list",
                    name: "shopShippingTypeManage",
                    meta: { title: "配送类型" },
                    component: () => import("@/views/setting/shippingType/List.vue")
                },
                {
                    path: "shop-shipping-tpl/list",
                    name: "shopShippingTplManage",
                    meta: { title: "运费模板" },
                    component: () => import("@/views/setting/shippingTpl/List.vue")
                }
            ]
        },
    ]
};
