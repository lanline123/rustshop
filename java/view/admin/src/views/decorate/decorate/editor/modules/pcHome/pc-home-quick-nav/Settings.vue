<template>
    <perfect-scrollbar class="decorate-edit-con">
        <!-- 标题 -->
        <div class="dec-edit-title">
            <h3>快捷导航</h3>
            <div class="dec-edit-desc">横向图文导航条，支持图标+文字+链接配置</div>
        </div>

        <SettingsTabs v-model="activeTab" :tabs="settingsTabs">
            <template #content>
                <div class="dec-edit-title-box">
                    <div class="title">内容设置</div>
                </div>

                <!-- 导航项设置 -->
                <div class="dec-edit-group dec-edit-group-block">
                    <div class="dec-edit-group-desc">
                        <div class="pic-change-desc">提示：您可以通过拖拽图标区域进行排序</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <ModulePicList
                            v-model:photos="localData.items"
                            :is-multiple="true"
                            :show-image="true"
                            :show-text="true"
                            :show-desc="false"
                            :show-link="true"
                            title="添加导航项"
                            decorate-type="pc"
                            @change="handleItemsChange"
                        />
                    </div>
                </div>

                <div class="dec-edit-title-box">
                    <div class="title">样式设置</div>
                </div>

                <!-- 字号 -->
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">字号</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-range-group">
                            <el-slider
                                :model-value="styleConfig.fontSize ?? 14"
                                :min="12"
                                :max="24"
                                show-input
                                :show-input-controls="false"
                                size="small"
                                input-size="default"
                                @update:model-value="(v: number | number[]) => updateStyleField('fontSize', Array.isArray(v) ? v[0] : v)"
                            />
                        </div>
                    </div>
                </div>

                <!-- 文字粗细 -->
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">文字粗细</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-radio-group
                            class="dec-radio-group"
                            :model-value="Number(styleConfig.fontWeight ?? 400)"
                            @update:model-value="(v: string | number | boolean | undefined) => updateStyleField('fontWeight', (Number(v) === 700 ? 700 : 400) as 400 | 700)"
                        >
                            <el-radio-button :value="400">常规体</el-radio-button>
                            <el-radio-button :value="700">加粗体</el-radio-button>
                        </el-radio-group>
                    </div>
                </div>

                <!-- 行高 -->
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">行高</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-range-group">
                            <el-slider
                                :model-value="styleConfig.lineHeight ?? 20"
                                :min="12"
                                :max="40"
                                show-input
                                :show-input-controls="false"
                                size="small"
                                input-size="default"
                                @update:model-value="(v: number | number[]) => updateStyleField('lineHeight', Array.isArray(v) ? v[0] : v)"
                            />
                        </div>
                    </div>
                </div>

                <!-- 文字颜色 -->
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">文字颜色</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-color-group">
                            <div class="dec-color-button">
                                <a class="dec-color-reset" @click="updateStyleField('textColor', '#333333')">重置</a>
                                <SelectColor :color="styleConfig.textColor || '#333333'" @update:color="(v: string) => updateStyleField('textColor', v)" />
                            </div>
                        </div>
                    </div>
                </div>

                <!-- hover 颜色 -->
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">悬停颜色</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-color-group">
                            <div class="dec-color-button">
                                <a class="dec-color-reset" @click="updateStyleField('hoverColor', '#e1251b')">重置</a>
                                <SelectColor :color="styleConfig.hoverColor || '#e1251b'" @update:color="(v: string) => updateStyleField('hoverColor', v)" />
                            </div>
                        </div>
                    </div>
                </div>

                <div class="dec-edit-title-box">
                    <div class="title">图标设置</div>
                </div>

                <!-- 图标尺寸 -->
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">图标尺寸</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-range-group">
                            <el-slider
                                :model-value="styleConfig.iconSize ?? 16"
                                :min="12"
                                :max="32"
                                show-input
                                :show-input-controls="false"
                                size="small"
                                input-size="default"
                                @update:model-value="(v: number | number[]) => updateStyleField('iconSize', Array.isArray(v) ? v[0] : v)"
                            />
                        </div>
                    </div>
                </div>

                <!-- 图标与文字间距 -->
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">图文间距</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-range-group">
                            <el-slider
                                :model-value="styleConfig.iconGap ?? 4"
                                :min="0"
                                :max="16"
                                show-input
                                :show-input-controls="false"
                                size="small"
                                input-size="default"
                                @update:model-value="(v: number | number[]) => updateStyleField('iconGap', Array.isArray(v) ? v[0] : v)"
                            />
                        </div>
                    </div>
                </div>

                <div class="dec-edit-title-box">
                    <div class="title">布局设置</div>
                </div>

                <!-- 整体排布 -->
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">整体排布</div>
                        <div class="value">{{ containerAlignLabel }}</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-radio-group
                            class="dec-radio-group"
                            :model-value="Number(styleConfig.containerAlign ?? 1)"
                            @update:model-value="(v: string | number | boolean | undefined) => updateStyleField('containerAlign', Number(v) as 1 | 2 | 3)"
                        >
                            <el-radio-button :value="1">居左</el-radio-button>
                            <el-radio-button :value="2">居中</el-radio-button>
                            <el-radio-button :value="3">居右</el-radio-button>
                        </el-radio-group>
                    </div>
                </div>

                <!-- 导航项间距 -->
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">项目间距</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-range-group">
                            <el-slider
                                :model-value="styleConfig.itemGap ?? 20"
                                :min="8"
                                :max="60"
                                show-input
                                :show-input-controls="false"
                                size="small"
                                input-size="default"
                                @update:model-value="(v: number | number[]) => updateStyleField('itemGap', Array.isArray(v) ? v[0] : v)"
                            />
                        </div>
                    </div>
                </div>

                <!-- 单行模式 -->
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">单行模式</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-radio-group
                            class="dec-radio-group"
                            :model-value="Number(styleConfig.singleLine ?? 1)"
                            @update:model-value="(v: string | number | boolean | undefined) => updateStyleField('singleLine', (Number(v) === 0 ? 0 : 1) as 0 | 1)"
                        >
                            <el-radio-button :value="1">单行不换行</el-radio-button>
                            <el-radio-button :value="0">自动换行</el-radio-button>
                        </el-radio-group>
                    </div>
                </div>
            </template>

            <template #extend>
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
 * PC 首页快捷导航模块 - 设置组件
 */
import { computed, ref } from "vue";
import { SelectColor } from "@/components/select";
import type { PcHomeQuickNavModule, PcHomeQuickNavStyle } from "./types";
import { pcHomeQuickNavDefaults } from ".";
import { useModuleSettings } from "../../../composables";
import { ModuleContainerSettings, ModulePicList, SettingsTabs } from "../_shared";

defineOptions({ name: "PcHomeQuickNavSettings" });

// 使用通用的模块设置 Composable
const { localData, updateField } = useModuleSettings<PcHomeQuickNavModule>(pcHomeQuickNavDefaults);

// ==================== Tab 配置 ====================
const activeTab = ref("content");
const settingsTabs = [
    { name: "content", label: "内容设置" },
    { name: "extend", label: "扩展设置" }
];

// ==================== 计算属性 ====================

/** 样式配置 */
const styleConfig = computed<PcHomeQuickNavStyle>(() => {
    return localData.value.style || {};
});

const containerAlignLabels: Record<1 | 2 | 3, string> = { 1: "居左", 2: "居中", 3: "居右" };
const containerAlignLabel = computed(() => {
    const v = Number(styleConfig.value.containerAlign ?? 1) as 1 | 2 | 3;
    return containerAlignLabels[v] || "居左";
});

// ==================== 更新方法 ====================

/**
 * 更新样式字段
 */
function updateStyleField<K extends keyof PcHomeQuickNavStyle>(key: K, value: unknown) {
    updateField("style", { ...(localData.value.style || {}), [key]: value as PcHomeQuickNavStyle[K] });
}

/**
 * 处理导航项变化
 */
function handleItemsChange() {
    const newList = (localData.value.items ?? []).map((item) => ({ ...item }));
    updateField("items", newList);
}
</script>

<style lang="less" scoped>
.decorate-edit-con {
    height: 100%;
}
</style>
