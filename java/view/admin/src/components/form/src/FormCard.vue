/**
 * @file 表单卡片组件
 * @description 带标题的表单分组卡片，用于将表单内容按功能模块分区展示
 * @features
 * - 带左侧竖线装饰的标题栏
 * - 灰色背景标题区域
 * - 插槽支持自定义表单内容
 */
<template>
    <div class="form-warp">
        <div class="title">
            <p class="xian"></p>
            {{ title }}
        </div>
        <div class="form-content">
            <slot></slot>
        </div>
    </div>
</template>

<script setup lang="ts">
import { defineProps } from 'vue'

/**
 * 组件属性定义
 * @property {string} title - 卡片标题，必填
 */
defineProps({
    title: {
        type: String,
        required: true,
        default: ''
    }
})
</script>


<style scoped lang="less">
.form-warp{
    .title {
        background-color: #f5f7fa;
        font-size: 14px;
        font-weight: bold;
        padding: 12px 10px;
        display: flex;
        align-items: center;
        margin-bottom: 20px;

        .xian {
            width: 3px;
            height: 14px;
            background-color: var(--tig-primary);
            margin-right: 5px;
        }
    }
}
</style>
