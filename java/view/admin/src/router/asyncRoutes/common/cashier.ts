/**
 * 收银管理分组（后台侧边栏，base 布局）
 *
 * - 采用与 Vortmall 一致的多级折叠菜单结构
 * - 通过将二级菜单 path 设为 ""，兼容 pageLeftMenu 路径解析与 Vue Router 嵌套渲染
 * - 收银设置 / 小票打印 复用收银台的设置页
 * - 设备管理为平台端与门店端共用节点，两端菜单均依赖后端 posCashierDeviceList 权限位
 * - 收银员列表复用员工管理里的收银员页面
 * - “收银工作台”为菜单入口，点击后切换到收银台
 */
export const createCashierRoute = () => ({
    path: "/cashier",
    name: "groupPos",
    component: () => import("@/layouts/base/index.vue"),
    meta: { title: "收银" },
    redirect: "/cashier/order-setting/order-setting",
    children: [
        {
            path: "workbench",
            name: "posWorkbench",
            meta: { title: "收银工作台", blank: true, switchToPos: true }
        },
        {
            path: "",
            name: "posSetting",
            meta: { title: "收银设置" },
            redirect: "/cashier/order-setting/order-setting",
            children: [
                {
                    path: "order-setting/order-setting",
                    name: "posOrderSetting",
                    meta: { title: "收银设置" },
                    component: () => import("@/views/pos/views/settings/OrderSetting.vue")
                },
                {
                    path: "order-setting/receipt-print",
                    name: "posReceiptPrint",
                    meta: { title: "小票打印" },
                    component: () => import("@/views/pos/views/settings/ReceiptPrintSetting.vue")
                },
                {
                    path: "cashier-manage/device-list",
                    name: "posCashierDeviceList",
                    meta: { title: "设备管理" },
                    component: () => import("@/views/adminMerchant/cashierDevice/List.vue")
                }
            ]
        },
    ]
});
