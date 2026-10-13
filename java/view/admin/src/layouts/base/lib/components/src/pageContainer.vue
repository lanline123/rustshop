/**
 * @file 页面容器组件
 * @description 内容区容器，包含头部、提示、核心内容和底部
 * @features
 * - 页面头部（面包屑和标题）
 * - 首页提示信息
 * - 核心内容区
 * - 页面底部
 */
<template>
    <div class="body-menu-mask"></div>
    <div class="main-warp" :class="[menusStore.childMenuShow ? 'child-menu-show' : 'child-menu-hide']">
        <div class="page-grid-content">
            <pageHeader v-if="!$route.meta.is404"></pageHeader>
            <pageTips v-show="fullPath === '/' && getAdminType() === 'admin'"></pageTips>
            <div class="core-container">
                <router-view :key="appStore.routerKey"></router-view>
            </div>
            <pageFooter v-if="!$route.meta.is404"></pageFooter>
        </div>
    </div>
</template>
<script setup lang="ts">
import { ref, watchEffect } from "vue";
import { useRouter } from "vue-router";
import pageHeader from "../../pageHeader.vue";
import pageFooter from "../../pageFooter.vue";
import pageTabs from "../../pageTabs.vue";
import { useMenusStore } from "@/store/menu";
import { useAppStore } from "@/store/app";
import pageTips from "./pageTips.vue";
import { getAdminType } from "@/utils/storage";

const router = useRouter();
/** 当前路由完整路径 */
const fullPath = ref(router.currentRoute.value.fullPath);
const appStore = useAppStore();
const menusStore = useMenusStore();

/** 监听路由变化，更新路径 */
watchEffect(() => {
    fullPath.value = router.currentRoute.value.fullPath;
});
</script>
<style lang="less" scoped>
.main-warp {
    z-index: 10;
    width: 100%;
    .page-grid-content {
        display: flex;
        flex-direction: column;
        gap: 12px;
        .core-container {
            margin: 0 16px;
            min-height: calc(100vh - 210px);
        }
    }
}
.child-menu-show {
    transition: all 0.3s;
}
.child-menu-hide {
    transition: all 0.3s;
}

.main-layout-box {
    position: absolute;
    top: 60px;
    left: 0;
    right: 0;
    z-index: 2;
    background: #fff;
    bottom: 0;
    overflow-y: auto;
}

@media only screen and (max-width: 767px) {
    .main-warp {
        left: 0 !important;
        padding-top: 0px !important;
    }
}
</style>
