/**
 * @file 应用根组件
 * @description 全局配置入口，包含Ant Design Vue和Element Plus的全局配置
 * @features
 * - Ant Design Vue主题配置
 * - Element Plus中文配置
 * - 自定义CSS加载
 * - 版本配置管理
 */
<template>
    <a-config-provider
        :theme="{
            token: {
                fontSize: 14,
                borderRadius: 2,
                colorPrimary: '#1456f0',
                wireframe: true
            }
        }"
        :locale="antLocale"
        :dropdown-match-select-width="false"
    >
        <el-config-provider :locale="elLocale">
            <RouterView />
        </el-config-provider>
    </a-config-provider>
</template>
<script setup lang="ts">
import { RouterView, useRoute } from "vue-router";
import { useI18n } from "vue-i18n";
import zhCnAnt from "ant-design-vue/es/locale/zh_CN";
import enUsAnt from "ant-design-vue/es/locale/en_US";
import zhLocale from "element-plus/dist/locale/zh-cn.mjs";
import enLocale from "element-plus/dist/locale/en.mjs";
import { useConfigStore } from "@/store/config";
import { useLoadCss } from "@/utils/domUtils";
import { useVersionConfigManager } from "./utils/versionConfigManager";
import { useAppStore } from "@/store/app";
import { computed, watch } from "vue";

const appStore = useAppStore();
const route = useRoute();

const { locale } = useI18n();
// 组件库 locale 随语言联动：zh → 中文包，其余 → 英文包
const elLocale = computed(() => (locale.value === "zh" ? zhLocale : enLocale));
const antLocale = computed(() => (locale.value === "zh" ? zhCnAnt : enUsAnt));

// 已登录用户初始化版本配置管理
if (localStorage.getItem("accessToken")) {
    useVersionConfigManager();
}

// 加载自定义图标CSS
const configStore = useConfigStore();
if (configStore.config.icoDefinedCss) {
    useLoadCss(configStore.config.icoDefinedCss);
}
</script>
<style lang="less" scoped>
// 临时解决方法，避免表格因渲染问题导致表格背景色为灰色
:deep(.el-table) {
    .el-table__row:not(.hover-row):not(.current-row) {
        td {
            background-color: #fff;
        }
    }
}
</style>
