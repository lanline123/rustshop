<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>分割线</h3>
        </div>

        <SettingsTabs v-model="activeTab" :tabs="settingsTabs">
            <template #content>
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">分割类型</div>
                        <div class="value">{{ lineTypeLabel }}</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-radio-group class="dec-radio-group" :model-value="localData.lineType" @update:model-value="(v: string | number | boolean | undefined) => updateField('lineType', Number(v))">
                            <el-radio-button :value="1">空白</el-radio-button>
                            <el-radio-button :value="2">分割线</el-radio-button>
                        </el-radio-group>
                    </div>
                </div>

                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">选择样式</div>
                        <div class="value">{{ lineStyleLabel }}</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-radio-group class="dec-radio-group" :model-value="localData.lineStyle" @update:model-value="(v: string | number | boolean | undefined) => updateField('lineStyle', Number(v))">
                            <el-radio-button :value="1">实线</el-radio-button>
                            <el-radio-button :value="2">虚线</el-radio-button>
                            <el-radio-button :value="3">点状线</el-radio-button>
                        </el-radio-group>
                    </div>
                </div>
            </template>

            <template #extend>
                <!-- Vort 容器设置 type="line" 提供高度调节，Tigshop 共享组件无此形态，单独补 itemHeight 控件（同 pc-notice 范式） -->
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">组件高度</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-range-group">
                            <el-slider
                                :model-value="localData.frame?.itemHeight ?? 10"
                                :max="100"
                                show-input
                                :show-input-controls="false"
                                size="small"
                                input-size="default"
                                @update:model-value="(v: number | number[]) => updateFrameField('itemHeight', Array.isArray(v) ? v[0] : v)"
                            />
                        </div>
                    </div>
                </div>

                <div class="dec-edit-title-box">
                    <div class="title">容器设置</div>
                </div>
                <ModuleContainerSettings :model-value="localData.frame" @update:model-value="updateField('frame', $event)" />
            </template>
        </SettingsTabs>
    </perfect-scrollbar>
</template>

<script setup lang="ts">
/**
 * PC 分割线模块 - 设置组件
 */
import { computed, ref } from "vue";
import type { PcDividerModule } from "./types";
import type { ModuleFrame } from "../_shared/types";
import { pcDividerDefaults } from ".";
import { useModuleSettings } from "../../../composables";
import { ModuleContainerSettings, SettingsTabs } from "../_shared";

defineOptions({ name: "PcDividerSettings" });

const { localData, updateField } = useModuleSettings<PcDividerModule>(pcDividerDefaults);

const activeTab = ref("content");
const settingsTabs = [
    { name: "content", label: "内容设置" },
    { name: "extend", label: "扩展设置" }
];

function updateFrameField<K extends keyof ModuleFrame>(key: K, value: ModuleFrame[K]) {
    updateField("frame", { ...localData.value.frame, [key]: value });
}

const lineTypeLabels: Record<number, string> = {
    1: "空白",
    2: "分割线"
};

const lineStyleLabels: Record<number, string> = {
    1: "实线",
    2: "虚线",
    3: "点状线"
};

const lineTypeLabel = computed(() => lineTypeLabels[localData.value.lineType ?? 2] || "");
const lineStyleLabel = computed(() => lineStyleLabels[localData.value.lineStyle ?? 1] || "");
</script>

<style lang="less" scoped>
.decorate-edit-con {
    height: 100%;
}
</style>
