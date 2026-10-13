/** SVG图标组件 - 通过name引用svg sprite中的图标 */
<template>
    <svg :class="svgClass" :style="iconStyle" v-bind="$attrs">
        <use :xlink:href="iconName" :fill="iconFill" />
    </svg>
</template>

<script setup>
import { computed } from "vue";

const props = defineProps({
    /** 图标名称（对应svg sprite中的id） */
    name: { type: String, required: true },
    /** 图标颜色 */
    color: { type: String, default: "" }
});

/** 计算图标引用路径 */
const iconName = computed(() => `#icon-${props.name}`);

/** 计算图标样式类 */
const svgClass = computed(() => {
    if (props.name) {
        return `icon icon-${props.name}`;
    }
    return "icon";
});

/** 计算图标样式 */
const iconStyle = computed(() => {
    return props.color ? { color: props.color } : {};
});

/** 计算图标填充色 */
const iconFill = computed(() => {
    return props.color || "currentColor";
});
</script>

<style lang="less" scoped>
.icon {
    /* 默认设置颜色，使用 currentColor */
    fill: currentColor; /* 使得图标的颜色跟随父元素的 color */
}
</style>
