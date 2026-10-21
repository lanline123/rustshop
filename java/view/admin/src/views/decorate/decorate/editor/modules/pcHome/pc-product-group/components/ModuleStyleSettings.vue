<template>
    <div class="module-style-settings">
        <div class="dec-edit-title-box">
            <div class="title">模块样式设置</div>
        </div>

        <!-- 模块顶部间距 -->
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">模块顶部间距</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-range-group">
                    <el-slider
                        :model-value="localStyle.boxPaddingTop ?? 0"
                        :max="30"
                        show-input
                        :show-input-controls="false"
                        size="small"
                        input-size="default"
                        @update:model-value="(v: number | number[]) => updateStyleField('boxPaddingTop', Array.isArray(v) ? v[0] : v)"
                    />
                </div>
            </div>
        </div>

        <!-- 模块底部间距 -->
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">模块底部间距</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-range-group">
                    <el-slider
                        :model-value="localStyle.boxPaddingBottom ?? 0"
                        :max="30"
                        show-input
                        :show-input-controls="false"
                        size="small"
                        input-size="default"
                        @update:model-value="(v: number | number[]) => updateStyleField('boxPaddingBottom', Array.isArray(v) ? v[0] : v)"
                    />
                </div>
            </div>
        </div>

        <!-- 模块左右间距 -->
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">模块左右间距</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-range-group">
                    <el-slider
                        :model-value="localStyle.boxPadding ?? 10"
                        :max="30"
                        show-input
                        :show-input-controls="false"
                        size="small"
                        input-size="default"
                        @update:model-value="(v: number | number[]) => updateStyleField('boxPadding', Array.isArray(v) ? v[0] : v)"
                    />
                </div>
            </div>
        </div>

        <!-- 模块背景颜色 -->
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">模块背景颜色</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-color-group">
                    <div class="dec-color-button">
                        <a class="dec-color-reset" @click="updateStyleField('backgroundColor', '')">重置</a>
                        <SelectColor :color="localStyle.backgroundColor || ''" @update:color="(v: string) => updateStyleField('backgroundColor', v)" />
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
/**
 * 模块样式设置组件（移植自 Vort _shared/module-style/ModuleStyleSettings，控件 Element 化）
 * 精简为 pc-product-group 实际使用的控件集（顶/底/左右间距 + 背景颜色，无背景图片）
 */
import { computed } from "vue";
import { SelectColor } from "@/components/select";
import type { ModuleStyle } from "../types";

defineOptions({ name: "ModuleStyleSettings" });

const props = defineProps<{
    /** 模块样式配置对象 */
    modelValue?: ModuleStyle;
}>();

const emit = defineEmits<{
    /** 更新模块样式配置 */
    (e: "update:modelValue", value: ModuleStyle): void;
}>();

// 默认模块样式
const defaultStyle: ModuleStyle = {
    boxPaddingTop: 0,
    boxPaddingBottom: 0,
    boxPadding: 10,
    backgroundColor: "",
    backgroundPic: { picUrl: "", picThumb: "" },
    backgroundPicFillType: "cover"
};

// 本地样式数据
const localStyle = computed(() => ({
    ...defaultStyle,
    ...props.modelValue
}));

// 更新样式字段
function updateStyleField<K extends keyof ModuleStyle>(key: K, value: ModuleStyle[K]) {
    emit("update:modelValue", {
        ...localStyle.value,
        [key]: value
    });
}
</script>

<style lang="less" scoped>
.dec-range-group {
    display: flex;
    align-items: center;
}
</style>
