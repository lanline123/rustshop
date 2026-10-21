/**
 * @file SSO 免登录中转页
 * @description
 * 接收外部容器（商家端 app web-view）透传的登录态，写入本地后整页跳转到目标页。
 * - 解析 query：token / adminType / shopId / redirect
 * - 换账号保护：本地已存在不同 token 时先清理旧会话，避免串号
 * - 门店 / POS 身份补拉店铺信息写入 shopType：对齐正常登录，避免依赖 getShopType()
 *   的页面（如商品列表「添加商品」按钮）在首屏同步渲染时读不到 shopType
 * - 写入 localStorage 后用 location.replace 跳目标路由：既抹除 URL 上的敏感参数，
 *   又让路由守卫从干净内存态重新拉菜单、注册路由
 * - embed 标志随 redirect 透传（query），并写一份 sessionStorage 作为兜底
 */
<template>
    <div class="sso-wrap" />
</template>

<script setup lang="ts">
import { onMounted } from "vue";
import { loginOut } from "@/utils/storage";
import { getShopInfo } from "@/api/authority/accountEditing";

/**
 * 归一化路由 base：
 * VITE_BASE_DIR 可能是 "admin"（无斜杠，生产子路径）或空（dev 根路径）。
 * 统一成 "/admin" 或 ""，确保后续 location.replace 用绝对路径，避免相对路径拼接错位。
 */
const rawBaseDir = (import.meta.env.VITE_BASE_DIR ?? "") as string;
const BASE_DIR = rawBaseDir ? `/${rawBaseDir.replace(/^\/+|\/+$/g, "")}` : "";

/** 从地址栏解析 SSO 参数（URLSearchParams 会自动做一次 decode） */
function parseQuery() {
    const params = new URLSearchParams(window.location.search);
    return {
        token: params.get("token") || "",
        adminType: params.get("adminType") || "",
        shopId: params.get("shopId") || "",
        redirect: params.get("redirect") || ""
    };
}

onMounted(async () => {
    const { token, adminType, shopId, redirect } = parseQuery();

    // 无 token：无法建立会话，回登录页（带上原 redirect，登录后可回跳营销等深链）
    if (!token) {
        const q =
            redirect && redirect.charAt(0) === "/" && !redirect.startsWith("//")
                ? `?redirect=${encodeURIComponent(redirect)}`
                : "";
        window.location.replace(`${BASE_DIR}/login/index${q}`);
        return;
    }

    // 换账号保护：本地是另一个账号的会话时，先彻底清理，避免新旧身份串号
    const oldToken = localStorage.getItem("accessToken");
    if (oldToken && oldToken !== token) {
        loginOut();
    }

    localStorage.setItem("accessToken", token);
    if (adminType) localStorage.setItem("adminType", adminType);
    if (shopId) localStorage.setItem("shopId", shopId);

    // 门店 / POS 身份补拉店铺信息，与正常登录一致写入 shopType；
    // 必须在跳转前完成，确保目标页首屏同步读取 getShopType() 时已就位。
    if (adminType === "shop" || adminType === "pos") {
        try {
            const shop = await getShopInfo();
            localStorage.setItem("shopInfo", JSON.stringify(shop));
            localStorage.setItem("shopType", shop.shopType);
        } catch (e) {
            // 拉取失败不阻断跳转，目标页按无 shopType 时的既有逻辑处理
        }
    }

    // embed 兜底：query 为主，sessionStorage 防个别页面路由跳转后丢参
    try {
        sessionStorage.setItem("ssoEmbed", "1");
    } catch (e) {
        // 隐私模式等场景忽略
    }

    // redirect 为以 / 开头的应用内路径（已含自身的 ?embed=1）
    const target = redirect && redirect.charAt(0) === "/" ? redirect : "/";
    window.location.replace(`${BASE_DIR}${target}`);
});
</script>

<style lang="less" scoped>
.sso-wrap {
    height: 100vh;
    background: #fff;
}
</style>
