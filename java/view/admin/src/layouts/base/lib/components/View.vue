/**
 * @file 主视图布局组件
 * @description 管理后台的核心布局，包含顶部栏、左侧菜单和内容区
 * @features
 * - 顶部导航栏（支持多种布局模式）
 * - 左侧菜单栏
 * - 内容区滚动管理
 * - 路由切换时重置滚动位置
 */
<template>
    <div class="layout-basic" :class="{ 'is-embed': isEmbed }">
        <div v-if="!isEmbed" class="layout-top">
            <pageTopMenu v-if="themeInfo.layout == 'topMenu'"></pageTopMenu>
            <pageTopBar v-if="themeInfo.layout == 'default'"></pageTopBar>
        </div>
        <div class="layout-main">
            <div v-if="!isEmbed" class="layout-left-menu">
                <pageLeftMenu></pageLeftMenu>
            </div>
            <div class="layout-container">
                <VoiceBroadcastTip v-if="!isEmbed" />
                <el-scrollbar ref="ScrollBar" class="layout-content-warp">
                    <pageContainer>
                        <router-view />
                    </pageContainer>
                </el-scrollbar>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import pageLeftMenu from "./src/pageLeftMenu.vue";
import pageTopBar from "./src/pageTopBar.vue";
import pageTopMenu from "./src/pageTopMenu.vue";
import pageContainer from "./src/pageContainer.vue";
import VoiceBroadcastTip from "../src/VoiceBroadcast/VoiceBroadcastTip.vue";

import { useThemeStore } from "@/store/theme";
import { computed, onMounted, ref, watch } from "vue";
import { useRoute } from "vue-router";
import { ElScrollbar } from "element-plus";

const themeStore = useThemeStore();
themeStore.getThemeInfo();
/** 主题配置信息 */
const themeInfo: any = themeStore.themeInfo;
/** 滚动条组件引用 */
const ScrollBar = ref<InstanceType<typeof ElScrollbar>>();
const route = useRoute();

/**
 * 嵌入态（商家端 app web-view 打开）：隐藏顶栏与左侧菜单，仅渲染内容区。
 * query.embed 为主，sessionStorage 作为兜底（防个别页面路由跳转后丢 query）。
 */
const isEmbed = computed(() => {
    if (route.query.embed === "1") return true;
    try {
        return sessionStorage.getItem("ssoEmbed") === "1";
    } catch (e) {
        return false;
    }
});

onMounted(() => {
    ScrollBar.value?.scrollTo(0, 0);
});

// 路由切换时重置滚动位置
watch(route, (to, from) => {
    ScrollBar.value?.scrollTo(0, 0);
});

</script>
<style lang="less" scoped>
.layout-basic {
    display: flex;
    flex-direction: column;
    height: 100%;

    .layout-top {
        z-index: 20;
    }

    .layout-main {
        display: flex;
        flex: 1;
        height: 0;
        min-height: 0;
        z-index: 11;

        .layout-container {
            flex: 1;
            display: flex;
            flex-direction: column;
            overflow-y: auto;
            position: relative;
            background-color: #f5f7fa;
            min-width: 0;

            .layout-content-warp {
                flex: 1;
                width: 100%;
                min-height: 0;
            }
        }
    }
}
</style>
