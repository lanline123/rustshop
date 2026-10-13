<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>空白辅助</h3>
        </div>

        <SettingsTabs v-model="activeTab" :tabs="settingsTabs">
            <template #content>
                <!-- 背景颜色 -->
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">背景颜色</div>
                        <div class="value"></div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-color-group">
                            <div class="dec-color-button">
                                <a class="dec-color-reset" @click="resetBackgroundColor">重置</a>
                                <SelectColor :color="localData.backgroundColor || '#ffffff'" @update:color="(v: string) => updateField('backgroundColor', v)" />
                            </div>
                        </div>
                    </div>
                </div>

                <!-- 高度 -->
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">高度</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-range-group">
                            <el-slider
                                :model-value="localData.blankHeight || 0"
                                :max="200"
                                show-input
                                :show-input-controls="false"
                                size="small"
                                input-size="default"
                                @update:model-value="(v: number | number[]) => updateField('blankHeight', Array.isArray(v) ? v[0] : v)"
                            />
                        </div>
                    </div>
                </div>
            </template>

            <template #extend>
                <div class="dec-edit-title-box">
                    <div class="title">外边距</div>
                </div>
                <!-- spacer 自身已有「背景颜色」字段，扩展设置里不再重复展示 -->
                <ModuleContainerSettings :model-value="localData.frame" hide-background-color @update:model-value="updateField('frame', $event)" />
            </template>
        </SettingsTabs>
    </perfect-scrollbar>
</template>

<script setup lang="ts">
/**
 * PC 空白辅助模块 - 设置组件
 */
import { ref } from "vue";
import { SelectColor } from "@/components/select";
import type { PcSpacerModule } from "./types";
import { pcSpacerDefaults } from ".";
import { useModuleSettings } from "../../../composables";
import { SettingsTabs, ModuleContainerSettings } from "../_shared";

defineOptions({ name: "PcSpacerSettings" });

const { localData, updateField } = useModuleSettings<PcSpacerModule>(pcSpacerDefaults);

const activeTab = ref("content");
const settingsTabs = [
    { name: "content", label: "内容设置" },
    { name: "extend", label: "扩展设置" }
];

const resetBackgroundColor = () => {
    updateField("backgroundColor", pcSpacerDefaults.backgroundColor);
};
</script>

<style lang="less" scoped>
.decorate-edit-con {
    height: 100%;
}
</style>
