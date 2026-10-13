<template>
    <div class="content-style-settings">
        <div class="dec-edit-title-box">
            <div class="title">内容区样式设置</div>
        </div>

        <!-- 内容区圆角（统一圆角） -->
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">内容区圆角半径</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-range-group">
                    <el-slider
                        :model-value="localStyle.boxRadius ?? 0"
                        :max="40"
                        show-input
                        :show-input-controls="false"
                        size="small"
                        input-size="default"
                        @update:model-value="(v: number | number[]) => updateStyleField('boxRadius', Array.isArray(v) ? v[0] : v)"
                    />
                </div>
            </div>
        </div>

        <!-- 内容区背景颜色 -->
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">内容区背景颜色</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-color-group">
                    <div class="dec-color-button">
                        <a class="dec-color-reset" @click="updateStyleField('backgroundColor', '#ffffff')">重置</a>
                        <SelectColor :color="localStyle.backgroundColor || '#ffffff'" @update:color="(v: string) => updateStyleField('backgroundColor', v)" />
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
/**
 * 内容区样式设置组件（移植自 Vort _shared/content-style/ContentStyleSettings，控件 Element 化）
 * 精简为 pc-product-group 实际使用的控件集（圆角 + 背景颜色）
 */
import { computed } from "vue";
import { SelectColor } from "@/components/select";
import type { ContentStyle } from "../types";

defineOptions({ name: "ContentStyleSettings" });

const props = defineProps<{
    /** 内容区样式配置对象 */
    modelValue?: ContentStyle;
}>();

const emit = defineEmits<{
    /** 更新内容区样式配置 */
    (e: "update:modelValue", value: ContentStyle): void;
}>();

// 默认内容区样式
const defaultStyle: ContentStyle = {
    padding: 0,
    paddingTop: 0,
    paddingBottom: 0,
    paddingLeftRight: 0,
    boxRadius: 0,
    boxRadiusTop: 0,
    boxRadiusBottom: 0,
    gradientType: "upDown",
    gradientColorA: "",
    gradientColorB: "",
    backgroundColor: "#ffffff",
    backgroundPic: { picUrl: "", picThumb: "" },
    backgroundPicFillType: "cover",
    boxShadow: 0
};

// 本地样式数据
const localStyle = computed(() => ({
    ...defaultStyle,
    ...props.modelValue
}));

// 更新样式字段
function updateStyleField<K extends keyof ContentStyle>(key: K, value: ContentStyle[K]) {
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
