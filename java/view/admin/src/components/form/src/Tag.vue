/**
 * @file 标签组件
 * @description 用于展示状态、分类等信息的小标签
 * @features
 * - 自定义颜色
 * - 透明/实心两种样式模式
 * - 紧凑的尺寸设计
 */
<template>
    <div :class="['tag-style', { 'transparent-style': transparent, 'non-transparent-style': !transparent }]" :style="tagStyle">
        {{ text }}
    </div>
</template>
<script lang="ts" setup>
import { computed } from "vue";

/**
 * 组件属性定义
 * @property {string} text - 标签显示文本
 * @property {string} color - 标签颜色，默认绿色 #31c19e
 * @property {boolean} transparent - 是否透明样式（边框+文字着色），默认 true
 */
const props = defineProps({
    text: String,
    color: {
        type: String,
        default: "#31c19e"
    },
    transparent: {
        type: Boolean,
        default: true
    }
});

/**
 * 计算标签样式
 * @description 透明模式：白底+彩色边框+彩色文字；实心模式：彩色底+白色文字
 * @returns {Object} 样式对象
 */
const tagStyle = computed(() => {
    if (props.transparent) {
        return {
            color: props.color,
            border: `1px solid ${props.color}`,
            backgroundColor: "white"
        };
    } else {
        return {
            color: "white",
            backgroundColor: props.color,
            border: `1px solid ${props.color}`
        };
    }
});
</script>
<style lang="less" scoped>
.tag-style {
    display: inline-block; /* 使其按内容大小调整宽度 */
    padding: 0 3px; /* 设置内边距，增加文字与边框之间的距离 */
    border-radius: 2px; /* 圆角 */
    font-size: 12px;
    line-height: 14px;
    margin: 6px 3px 6px 6px;
    vertical-align: middle;
    white-space: nowrap;
    cursor: pointer;
}
</style>
