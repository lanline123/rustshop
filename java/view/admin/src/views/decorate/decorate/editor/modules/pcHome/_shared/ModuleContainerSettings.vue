<template>
    <div class="module-container-settings">
        <!-- 背景颜色（spacer 等只需要单层颜色的模块可通过 hide-background-color 隐藏，避免与内容区背景色重复） -->
        <div v-if="!hideBackgroundColor" class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">背景颜色</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-color-group">
                    <div class="dec-color-button">
                        <a class="dec-color-reset" @click="updateContainerField('backgroundColor', '#ffffff')">重置</a>
                        <SelectColor :color="localContainer.backgroundColor ?? '#ffffff'" @update:color="(v: string) => updateContainerField('backgroundColor', v)" />
                    </div>
                </div>
            </div>
        </div>

        <!-- 容器圆角 -->
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">容器圆角</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-range-group">
                    <el-slider
                        :model-value="localContainer.boxRadius || 0"
                        :max="30"
                        show-input
                        :show-input-controls="false"
                        size="small"
                        input-size="default"
                        @update:model-value="(v: number | number[]) => updateContainerField('boxRadius', Array.isArray(v) ? v[0] : v)"
                    />
                </div>
            </div>
        </div>

        <!-- 左右边距 -->
        <div v-if="!hideHorizontalSpacing" class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">左右边距</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-range-group">
                    <el-slider
                        :model-value="localContainer.boxPadding || 0"
                        :max="30"
                        show-input
                        :show-input-controls="false"
                        size="small"
                        input-size="default"
                        @update:model-value="(v: number | number[]) => updateContainerField('boxPadding', Array.isArray(v) ? v[0] : v)"
                    />
                </div>
            </div>
        </div>

        <!-- 上边距 -->
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">上边距</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-range-group">
                    <el-slider
                        :model-value="localContainer.boxPaddingTop || 0"
                        :max="30"
                        show-input
                        :show-input-controls="false"
                        size="small"
                        input-size="default"
                        @update:model-value="(v: number | number[]) => updateContainerField('boxPaddingTop', Array.isArray(v) ? v[0] : v)"
                    />
                </div>
            </div>
        </div>

        <!-- 下边距 -->
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">下边距</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-range-group">
                    <el-slider
                        :model-value="localContainer.boxPaddingBottom || 0"
                        :max="30"
                        show-input
                        :show-input-controls="false"
                        size="small"
                        input-size="default"
                        @update:model-value="(v: number | number[]) => updateContainerField('boxPaddingBottom', Array.isArray(v) ? v[0] : v)"
                    />
                </div>
            </div>
        </div>

        <!-- 左右内边距 -->
        <div v-if="showInnerPadding && !hideHorizontalSpacing" class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">左右内边距</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-range-group">
                    <el-slider
                        :model-value="localContainer.leftAndRightPadding || 0"
                        :max="30"
                        show-input
                        :show-input-controls="false"
                        size="small"
                        input-size="default"
                        @update:model-value="(v: number | number[]) => updateContainerField('leftAndRightPadding', Array.isArray(v) ? v[0] : v)"
                    />
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
/**
 * 模块容器设置组件（移植自 Vort-Admin，控件 Element 化）
 * 用于新版 PC 首页模块的容器样式设置（背景、边距、圆角等）
 */
import { computed } from "vue";
import { SelectColor } from "@/components/select";
import type { ModuleFrame } from "./types";

const props = defineProps<{
    /** 容器配置对象 */
    modelValue?: ModuleFrame;
    /** 隐藏左右边距 & 左右内边距（通宽排版时使用） */
    hideHorizontalSpacing?: boolean;
    /** 显示左右内边距控件（默认不显示，仅在 Preview 实际接入了 leftAndRightPadding 的模块中开启） */
    showInnerPadding?: boolean;
    /** 隐藏背景颜色控件（仅纯留白/分隔类模块需要，避免与内容设置里的背景色字段语义重复） */
    hideBackgroundColor?: boolean;
}>();

const emit = defineEmits<{
    (e: "update:modelValue", value: ModuleFrame): void;
}>();

// 默认容器配置（与 Vort 一致）
const defaultContainer: ModuleFrame = {
    textColor: "",
    itemBackgroundColor: "",
    backgroundColor: "#ffffff",
    innerPadding: 0,
    itemHeight: 35,
    itemRadius: 3,
    boxRadius: 0,
    boxPadding: 10,
    leftAndRightPadding: 0,
    boxPaddingTop: 5,
    boxPaddingBottom: 5
};

const localContainer = computed(() => ({
    ...defaultContainer,
    ...props.modelValue
}));

function updateContainerField<K extends keyof ModuleFrame>(key: K, value: ModuleFrame[K]) {
    emit("update:modelValue", {
        ...localContainer.value,
        [key]: value
    });
}
</script>
