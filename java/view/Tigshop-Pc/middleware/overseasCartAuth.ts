import { isOverseas } from "@/utils/util";

/**
 * 购物车「仅跨境强制登录」中间件（融合大盘：o2o/标准/B2B 购物车游客可访问，仅跨境强登录）
 * - 运行时 isOverseas() 守卫：非跨境站直接放行；即使融合 dev 未裁，o2o 本地也不会被强制登录。
 * - 跨境站未登录 → 跳登录页并带 returnUrl。
 * - 页面侧引用处（definePageMeta.middleware）再包 @vort-if overseas，最终裁剪时非跨境包移除绑定。
 */
export default defineNuxtRouteMiddleware((to) => {
    if (!isOverseas()) return;
    const token = useCookie<string | null>("accessToken");
    if (!token.value) {
        return navigateTo(`/member/login?returnUrl=${to.fullPath}`);
    }
});
