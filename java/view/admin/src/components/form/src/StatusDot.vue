/**
 * @file 状态点组件
 * @description 用于显示状态的圆形小点，常用于表示在线状态、处理状态等
 * @features
 * - 自定义颜色
 * - 自定义尺寸
 * - 可选闪烁动画效果
 * - 支持行内（span）和块级（div）两种渲染模式
 */
<template>
    <template v-if="inline">
        <span class="ant-badge-status-dot ant-badge-status-processing" :class="{ 'flicker': props.flicker }" :style="badgeStyle"></span>
    </template>
    <template v-else>
        <div class="ant-badge-status-dot ant-badge-status-processing" :class="{ 'flicker': props.flicker }" :style="badgeStyle"></div>
    </template>
</template>
<script setup lang="ts">
import { computed } from "vue";

/**
 * 组件属性定义
 * @property {boolean} inline - 是否行内显示，默认 true
 * @property {string} color - 点的颜色，默认蓝色 #1890ff
 * @property {boolean} flicker - 是否启用闪烁动画，默认 false
 * @property {number} size - 点的尺寸（像素），默认 6
 */
const props = defineProps({
    inline: {
        type: Boolean,
        default: true
    },
    color: {
        type: String,
        default: "#1890ff"
    },
    flicker: {
        type: Boolean,
        default: false
    },
    size: {
        type: Number,
        default: 6
    }
});

/**
 * 计算状态点样式
 * @returns {Object} 包含颜色和尺寸的样式对象
 */
const badgeStyle = computed(() => {
    return {
        "--border-color": props.color,
        "background-color": props.color,
        "width": props.size + "px",
        "height": props.size + "px"
    };
});
</script>
<style scoped lang="less">
.ant-badge-status-processing {
    position: relative;
    margin-right: 4px;
}

.ant-badge-status-dot {
    position: relative;
    top: -1px;
    display: inline-block;
    //width: 6px;
    //height: 6px;
    vertical-align: middle;
    border-radius: 50%;
    box-sizing: border-box;
}

.ant-badge-status-processing:after {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    border: 1px solid var(--border-color);
    border-radius: 50%;
    content: "";
    box-sizing: border-box;
}

.ant-badge-status-processing.flicker:after {
    -webkit-animation: antStatusProcessing 1.2s ease-in-out infinite;
    animation: antStatusProcessing 1.2s ease-in-out infinite;
}

@keyframes antStatusProcessing {
    0% {
        -webkit-transform: scale(.8);
        transform: scale(.8);
        opacity: .5
    }
    to {
        -webkit-transform: scale(2.4);
        transform: scale(2.4);
        opacity: 0
    }
}
</style>
