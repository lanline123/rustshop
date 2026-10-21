<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>分类导航</h3>
        </div>

        <SettingsTabs v-model="activeTab" :tabs="settingsTabs">
            <template #content>
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">显示标题</div>
                        <div class="value">{{ (localData.showTitle ?? 1) === 1 ? "显示" : "不显示" }}</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-radio-group
                            class="dec-radio-group flex-nowrap"
                            :model-value="localData.showTitle ?? 1"
                            @update:model-value="(v: string | number | boolean | undefined) => updateField('showTitle', (Number(v) === 0 ? 0 : 1) as 0 | 1)"
                        >
                            <el-radio-button :value="1">显示</el-radio-button>
                            <el-radio-button :value="0">不显示</el-radio-button>
                        </el-radio-group>
                    </div>
                </div>

                <div v-if="(localData.showTitle ?? 1) === 1" class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">标题</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-input
                            :model-value="localData.titleName"
                            placeholder="请输入标题"
                            clearable
                            style="width: 240px"
                            @update:model-value="(v: string) => updateField('titleName', v)"
                        />
                    </div>
                </div>

                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">一屏显示数量</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-range-group">
                            <el-slider
                                :model-value="localData.displayNum ?? 6"
                                :min="1"
                                :max="10"
                                show-input
                                :show-input-controls="false"
                                size="small"
                                input-size="default"
                                @update:model-value="(v: number | number[]) => updateField('displayNum', Array.isArray(v) ? v[0] : v)"
                            />
                        </div>
                    </div>
                </div>

                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">背景颜色</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-color-group">
                            <div class="dec-color-button">
                                <a class="dec-color-reset" @click="resetBackgroundColor">重置</a>
                                <SelectColor :color="localData.backgroundColor || '#f7f7f7'" @update:color="(v: string) => updateField('backgroundColor', v)" />
                            </div>
                        </div>
                    </div>
                </div>
            </template>

            <template #extend>
                <div class="dec-edit-title-box">
                    <div class="title">外边距</div>
                </div>
                <ModuleContainerSettings :model-value="localData.frame" @update:model-value="updateField('frame', $event)" />
            </template>
        </SettingsTabs>
    </perfect-scrollbar>
</template>

<script setup lang="ts">
/**
 * PC 分类导航模块 - 设置组件
 */
import { ref } from "vue";
import { SelectColor } from "@/components/select";
import type { PcCategoryNavModule } from "./types";
import { pcCategoryNavDefaults } from ".";
import { useModuleSettings } from "../../../composables";
import { SettingsTabs, ModuleContainerSettings } from "../_shared";

defineOptions({ name: "PcCategoryNavSettings" });

const { localData, updateField } = useModuleSettings<PcCategoryNavModule>(pcCategoryNavDefaults);

const activeTab = ref("content");
const settingsTabs = [
    { name: "content", label: "内容设置" },
    { name: "extend", label: "扩展设置" }
];

const resetBackgroundColor = () => {
    updateField("backgroundColor", pcCategoryNavDefaults.backgroundColor);
};
</script>

<style lang="less" scoped>
.decorate-edit-con {
    height: 100%;
}
</style>
