import { getShopType } from "@/utils/storage";
export default {
    path: "/setting",
    name: "setting",
    component: () => import("@/layouts/base/index.vue"),
    meta: { title: "设置" },
    children: [
        {
            path: "",
            name: "merchantSetting",
            meta: { title: getShopType() === 2 ? "门店设置" : "店铺设置" },
            redirect: "/setting/shop-info",
            children: [
                {
                    path: "shop-info",
                    name: "ShopInfoManage",
                    meta: { title: getShopType() === 2 ? "门店信息" : "店铺信息" },
                    component: () => import("@/views/merchant/setting/shopInfo/Index.vue")
                },
                {
                    path: "shop-team",
                    name: "ShopTeamManage",
                    meta: { title: getShopType() === 2 ? "门店设置" : "店铺设置" },
                    component: () => import("@/views/merchant/setting/team/Index.vue")
                },
                {
                    path: "mobile-shop-decorate",
                    name: "mobileShopDecorate",
                    meta: { title: "页面管理" },
                    component: () => import("@/views/merchant/setting/mobileShopDecorate/List.vue")
                },
                {
                    path: "base-receipt",
                    name: "shopBaseReceiptManage",
                    meta: { title: "小票打印" },
                    component: () => import("@/views/setting/config/receiptPrintSetting/List.vue")
                }
            ]
        },
        {
            path: "",
            name: "shopShipping",
            meta: { title: "配送设置" },
            redirect: "/setting/shop-shipping-type/list",
            children: [
                {
                    path: "shop-shipping-type/list",
                    name: "shopShippingTypeManage",
                    meta: { title: "配送类型" },
                    component: () => import("@/views/setting/shippingType/List.vue")
                },
                {
                    path: "logistics-company-config/list",
                    name: "shopLogisticsCompanyConfigManage",
                    meta: { title: "物流设置" },
                    component: () => import("@/views/setting/logisticsCompanyConfig/List.vue")
                },
                {
                    path: "shop-shipping-tpl/list",
                    name: "shopShippingTplManage",
                    meta: { title: "运费模板" },
                    component: () => import("@/views/setting/shippingTpl/List.vue")
                },
                {
                    path: "ship-receive-info",
                    name: "shopShipReceiveInfoManage",
                    meta: { title: "配送设置", shipInfoMode: "shop" },
                    component: () => import("@/views/setting/shipInfo/Index.vue")
                },
            ]
        },
        {
            path: "",
            name: "accountManagement",
            meta: { title: "账号管理" },
            redirect: "/setting/account-editing/index",
            children: [
                {
                    path: "account-editing/index",
                    name: "accountEditingManage",
                    meta: { title: "账号设置" },
                    component: () => import("@/views/authority/accountEditing/Index.vue")
                },
                {
                    path: "shop-role/list/",
                    name: "ShopRoleManage",
                    meta: { title: "角色管理" },
                    component: () => import("@/views/authority/adminRole/List.vue")
                },
                {
                    path: "employee/list/",
                    name: "EmployeeManagement",
                    meta: { title: "员工管理" },
                    component: () => import("@/views/authority/adminUser/ShopList.vue")
                },
                {
                    path: "employee-log/list/",
                    name: "EmployeeLogManagement",
                    meta: { title: "操作日志" },
                    component: () => import("@/views/merchant/setting/employeeLog/List.vue")
                }
            ]
        },
        {
            path: "",
            name: "materialManagement",
            meta: { title: "素材管理" },
            redirect: "/setting/material-center",
            children: [
                {
                    path: "material-center",
                    name: "materialCenter",
                    meta: { title: "素材中心" },
                    component: () => import("@/views/content/material-center/Index.vue")
                }
            ]
        },
    ]
};
