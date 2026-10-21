<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>绘制热区</h3>
        </div>

        <SettingsTabs v-model="activeTab" :tabs="settingsTabs">
            <template #content>
                <div class="dec-edit-group dec-edit-group-block">
                    <div class="dec-edit-group-desc">
                        <div class="pic-change-desc">提示：添加图片后，点击图片进入绘制</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <PcImageHotarea
                            :key="store.activeModule?.moduleIndex"
                            :photos="localData.picList || []"
                            @update:photos="handlePhotosChange"
                        />
                    </div>
                </div>

                <div class="dec-edit-title-box">
                    <div class="title">标题设置</div>
                </div>
                <ModuleTitleSettings :model-value="localData.title" @update:model-value="updateField('title', $event)" />
            </template>

            <template #extend>
                <div class="dec-edit-title-box">
                    <div class="title">容器设置</div>
                </div>
                <ModuleContainerSettings :model-value="localData.frame" show-inner-padding @update:model-value="updateField('frame', $event)" />
            </template>
        </SettingsTabs>
    </perfect-scrollbar>
</template>

<script setup lang="ts">
/**
 * PC 热图绘制模块 - 设置组件
 * 热区绘制复用旧编辑器的 PcImageHotarea 弹窗组件（picList[].hotarea 数据结构与 Vort 同构）；
 * 它对传入数组原地增删 + deep watch 后 emit update:photos，这里统一经 updateField 同步 store
 */
import { ref } from "vue";
import PcImageHotarea from "@/components/decorate/src/PcImageHotarea.vue";
import type { PcImageHotareaModule } from "./types";
import { pcImageHotareaDefaults } from ".";
import { useModuleSettings } from "../../../composables";
import { ModuleTitleSettings, ModuleContainerSettings, SettingsTabs } from "../_shared";

defineOptions({ name: "PcImageHotareaSettings" });

const { localData, updateField, store } = useModuleSettings<PcImageHotareaModule>(pcImageHotareaDefaults);

const activeTab = ref("content");
const settingsTabs = [
    { name: "content", label: "内容设置" },
    { name: "extend", label: "扩展设置" }
];

function handlePhotosChange(photos: unknown[]) {
    updateField("picList", photos);
}
</script>

<style lang="less" scoped>
.decorate-edit-con {
    height: 100%;
}
</style>

<style lang="less">
/* 热区拖拽控件全局样式（@gausszhou/vue3-drag-resize-rotate 渲染在弹窗内，需非 scoped，与旧 pcCustomPage 实现一致） */
.handle {
    box-sizing: border-box;
    position: absolute;
    background: #fff;
    border: 1px solid #155bd4;
}

.vue-drag-resize-rotate {
    touch-action: none;
    position: absolute;
    box-sizing: border-box;
    border: 1px solid transparent;
    background: rgba(21, 91, 212, 0.6);
    color: #fff;
}

.vue-drag-resize-rotate.active {
    background: rgba(21, 91, 212, 0.9);
    border: 1px solid #155bd4;
}

.vue-drag-resize-rotate:hover {
    border: 1px solid #155bd4;
}
</style>
