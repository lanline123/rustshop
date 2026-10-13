import router, { resetRouter } from "./router";
import { useAppStore } from "@/store/app";
import { setDocumentTitle } from "@/utils/domUtils";
import { useMenusStore } from "@/store/menu";
import { getMenu } from "@/utils/menus";
import { loginOut } from "@/utils/storage";
import { processRoutes, getDocTitle, removeSuffix } from "@/utils/authorize";
import { useVersionConfigManager } from './utils/versionConfigManager'
import { switchRole } from "@/utils/util";
import i18n from "@/i18n";
import { embedRedirectLocation } from "@/utils/loginRedirect";

const allowList = ["loginIndex", "merchantLogin"]; // 免登录名单
const loginRoutePath = "/login/index"; // 确保与路由配置一致
let isLoggingOut = false; // 标志位，避免重复执行登出逻辑
router.beforeEach(async (to, from, next) => {
    // 设置文档标题（根据授权状态动态决定是否带后缀，标题按当前语言现翻）
    // 先去后缀再翻译：meta.title 可能已被追加后缀，带后缀会导致命中不到词条
    const t = i18n.global.t;
    const rawTitle = to.meta?.title ? removeSuffix(String(to.meta.title)) : "";
    if (rawTitle) {
        setDocumentTitle(getDocTitle(t(rawTitle)));
    }
    const appStore = useAppStore();
    appStore.setHeaderTitle(rawTitle || (to.meta.title as string));

    // SSO 中转页自行处理登录态注入与跳转，直接放行，避免用旧 token 预拉菜单
    if (to.name === "ssoEntry") {
        next();
        return;
    }

    const accessToken = localStorage.getItem("accessToken");

    // 处理登出逻辑
    if (to.path === loginRoutePath && to.query.logout === "1" && !isLoggingOut) {
        isLoggingOut = true;
        loginOut();
        resetRouter();
        next({ path: loginRoutePath, query: {} });
        // 刷新页面
        setTimeout(() => {
            location.reload();
        }, 300);
        return;
    }

    // 处理登录状态
    if (accessToken) {
        // 处理切换角色逻辑
        if (to.query.type && (to.query.id || to.query.id == '0') && to.query.isDemo) {
            const id = Array.isArray(to.query.id) ? to.query.id[0] : to.query.id;
            const type = Array.isArray(to.query.type) ? to.query.type[0] : to.query.type;
            if (id !== null && id !== undefined && type !== null && type !== undefined) {
                switchRole(id, type);
                return
            }
        }
        // useVersionConfigManager()
        const menuStore = useMenusStore();
        if (to.path === loginRoutePath) {
            // 已登录访问登录页：仅 web-view 嵌入深链回跳目标页，其余一律回首页
            const loc = embedRedirectLocation(to.query.redirect);
            if (loc) {
                next({ ...loc, replace: true });
            } else {
                next({ path: menuStore.routers[0] ? menuStore.routers[0].path : "/", replace: true });
            }
        } else {
            if (menuStore.hasAddRoutes) {
                next();
            } else {
                const routers = await getMenu();
                menuStore.setRouters(processRoutes(routers) || []);
                menuStore.routers.forEach((route: any) => router.addRoute(route));

                router.addRoute({
                    path: "/:pathMatch(.*)*",
                    name: "notFound",
                    meta: { title: "404", is404: true },
                    component: () => import("@/layouts/base/index.vue"),
                    children: [
                        {
                            path: "",
                            name: "notFoundIndex",
                            meta: { title: "404" },
                            component: () => import("@/layouts/base/404.vue")
                        }
                    ]
                });

                menuStore.setHasAddRoutes(true);
                next({ ...to, replace: true });
            }
        }
    } else {
        if (allowList.includes(<string>to.name) || to.path === loginRoutePath) {
            next(); // 在免登录名单，或已在登录页，继续
        } else {
            // 保住深链目标（含 embed），登录/选店后可回跳
            next({ path: loginRoutePath, query: { redirect: to.fullPath } });
        }
    }
    isLoggingOut = false; // 清理标志位
});

router.afterEach(() => { });


// //高频路由预加载
// router.afterEach((to) => {
//     if (to.path === '/') {
//         // 首页后预加载高频模块
//         setTimeout(() => {
//             import('@/views/product/product/List.vue');
//             import('@/views/order/order/List.vue');
//             import('@/views/panel/statisticsOrder/List.vue');
//         }, 1000);
//     }
// })
