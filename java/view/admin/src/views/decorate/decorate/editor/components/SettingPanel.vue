<template>
    <div class="decorate-edit-wrap">
        <!-- 模块设置：统一 v-model:module 接口，legacy 旧 Edit.vue（defineModel）与新 Settings.vue（store 读写）均兼容 -->
        <component
            v-if="store.editMode === 'module' && store.activeModule && settingsComp"
            :is="settingsComp"
            :key="store.activeModule.moduleIndex"
            v-model:module="store.activeModule.module"
        ></component>
        <!-- 搜索页与商品详情页的页面结构收敛在业务模块内，不提供独立页面设置 -->
        <PageEdit v-if="store.editMode === 'page' && ![6, 8].includes(store.pageType)" v-model:module="store.pageModule" :page-type="store.pageType"></PageEdit>
        <!-- PC 头部设置：仅 PC 首页（搜索页装修不可进入） -->
        <PcHeaderSettingsPanel v-if="store.editMode === 'pcHeader' && !(store.decorateType === 2 && store.pageType === 6)" />
    </div>
</template>
<script lang="ts" setup>
import { shallowRef, watch, type Component } from "vue";
import PageEdit from "@/views/decorate/decorate/src/modules/src/commonPage/Edit.vue";
import { PcHeaderSettingsPanel } from "../modules/pcHome/_shared/pc-header";
import { getSettingsComponent } from "../config/module-registry";
import { useDecorateEditorStore } from "@/store/decorateEditor";

const store = useDecorateEditorStore();

const settingsComp = shallowRef<Component | null>(null);

// 异步 load 竞态保护：丢弃过期的 getSettingsComponent 结果
let loadToken = 0;

watch(
    () => [store.editMode, store.activeModule?.type, store.activeModule?.url, store.activeModuleIndex],
    async () => {
        if (store.editMode !== "module" || !store.activeModule) {
            settingsComp.value = null;
            return;
        }
        // 切换时立即卸载旧 Settings
        settingsComp.value = null;
        const token = ++loadToken;
        const type = store.activeModule.type;
        const url = store.activeModule.url;
        const comp = await getSettingsComponent(type, url);
        if (token !== loadToken) return;
        settingsComp.value = comp;
        if (!comp) {
            console.warn(`[SettingPanel] 未注册的模块类型: ${type}@${url}`);
        }
    },
    { immediate: true }
);
</script>
