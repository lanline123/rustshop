/**
 * 客服管理分组（后台侧边栏，base 布局）
 *
 * - 采用与 Vortmall 一致的多级折叠菜单结构
 * - 通过将二级菜单 path 设为 ""，完美兼容 Tigshop 的 pageLeftMenu 路径解析机制与 Vue Router 嵌套渲染
 * - 管理页（历史会话 / 快捷回复 / 客服设置）跑在普通后台布局里
 * - “客服工作台”为菜单入口，点击后在新标签打开 /im 会话页（targetRouteName: imChart）
 */
export const createCustomerServiceRoute = () => ({
    path: "/customer-service",
    name: "groupCustomerService",
    component: () => import("@/layouts/base/index.vue"),
    meta: { title: "客服" },
    redirect: "/customer-service/history/list",
    children: [
        {
            path: "workbench",
            name: "kefuWorkbench",
            meta: { title: "客服工作台", blank: true, targetRouteName: "imChart" }
        },
        {
            path: "",
            name: "kefuHistory",
            meta: { title: "历史会话" },
            redirect: "/customer-service/history/list",
            children: [
                {
                    path: "history/list",
                    name: "kefuHistoryList",
                    meta: { title: "历史会话列表" },
                    component: () => import("@/views/im/History.vue")
                }
            ]
        },
        {
            path: "",
            name: "kefuManage",
            meta: { title: "客服管理" },
            redirect: "/customer-service/agents/list",
            children: [
                {
                    path: "agents/list",
                    name: "kefuManageList",
                    meta: { title: "客服列表" },
                    component: () => import("@/views/im/kefuList/List.vue")
                }
            ]
        },
        {
            path: "",
            name: "kefuPersonal",
            meta: { title: "个人设置" },
            redirect: "/customer-service/profile/quick-reply",
            children: [
                {
                    path: "profile/quick-reply",
                    name: "kefuQuickReply",
                    meta: { title: "快捷回复" },
                    component: () => import("@/views/im/AutoReply.vue")
                },
                {
                    path: "profile/account",
                    name: "kefuAccountInfo",
                    meta: { title: "账户信息" },
                    component: () => import("@/views/authority/accountEditing/Index.vue")
                }
            ]
        },
        {
            path: "",
            name: "kefuSystemSetting",
            meta: { title: "系统设置" },
            redirect: "/customer-service/settings/general",
            children: [
                {
                    path: "settings/general",
                    name: "kefuSetting",
                    meta: { title: "客服设置" },
                    component: () => import("@/views/setting/config/base/src/ServiceSettings.vue")
                },
                {
                    path: "settings/rules",
                    name: "kefuAccessSetting",
                    meta: { title: "接入规则" },
                    component: () => import("@/views/im/accessSetting/Index.vue")
                }
            ]
        }
    ]
});
