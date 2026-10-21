/**
 * @file 提示框组件
 * @description 基于 Element Plus Tooltip 封装的提示信息组件
 * @features
 * - 可选显示问号图标
 * - 自定义提示内容
 * - 自定义弹出位置
 * - 插槽支持自定义触发元素
 */
<template>
    <div class="tooltip-con">
        <el-tooltip effect="light" :placement="props.placement" :show-after="100">
            <div class="tooltip-title">
                <slot> </slot>
                <i v-if="props.ico" class="iconfont-admin icon-yiwen"></i>
            </div>
            <template #content>
                <div style="max-width:300px">{{ props.content }}</div>
            </template>
        </el-tooltip>
    </div>
</template>
<script lang="ts" setup>
/**
 * 组件属性定义
 * @property {string} content - 提示框显示的内容
 * @property {boolean} ico - 是否显示问号图标，默认 false
 * @property {string} placement - 弹出位置，默认 "top"
 */
const props = defineProps({
    content: {
        type: String,
        default: "",
    },
    ico: {
        type: Boolean,
        default: false,
    },
    placement: {
        type: String,
        default: "top",
    },
});
</script>
<style lang="less" scoped>
.icon-yiwen {
    color: #bab8b8;
    padding-left: 2px;
    padding-right: 2px;
    font-size: 14px;
    &:hover {
        color: #acacac;
    }
}
.tooltip-title {
    display: flex;
    flex-wrap: nowrap;
}
</style>
