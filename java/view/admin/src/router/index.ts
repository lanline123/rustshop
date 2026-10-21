import { createRouter, createWebHistory, createWebHashHistory } from "vue-router";
import { processRoutes } from "@/utils/authorize";
import constantsRoutes from "@/router/constantsRoutes";
// import { adminRouters, merchantRouters } from "@/router/asyncRoutes";
// import posRouters from "@/views/pos/router/asyncRoutes/pos";

const { VITE_BASE_DIR } = import.meta.env;
// const routes = [...constantsRoutes];
const routes = [...constantsRoutes];

// 白名单应该包含基本静态路由
const WHITE_NAME_LIST: string[] = [];
const router = createRouter({
    history: createWebHistory(VITE_BASE_DIR ?? ""),
    routes
});

// 修改路由标题 是否授权  - powered by tigshop
processRoutes(router.getRoutes());

// reset router
export function resetRouter() {
    router.getRoutes().forEach((route) => {
        const { name } = route;
        if (name && !WHITE_NAME_LIST.includes(name as string)) {
            router.hasRoute(name) && router.removeRoute(name);
        }
    });
}

export default router;
