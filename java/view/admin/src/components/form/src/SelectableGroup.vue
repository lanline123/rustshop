/**
 * @file 可选组组件
 * @description 类似按钮组的选项选择器，支持单选和多选模式
 * @features
 * - 支持单选模式（默认）
 * - 支持多选模式
 * - 选中项高亮显示
 * - 悬停效果
 * - 支持 v-model 双向绑定
 */
<template>
    <!--    <el-input style="width: 180px" v-model="modelValue"></el-input>-->
    <div :class="['selectable-group', { 'is-single': !isMultiple }, { 'is-multiple': isMultiple }]">
        <div v-for="(option, index) in options" :key="index" :class="['option', { 'is-selected': isSelected(option) }]" @click="toggleSelection(option)">
            {{ option.label }}
        </div>
    </div>
</template>

<script setup>
import { ref } from "vue";

/**
 * 组件属性定义
 * @property {Array} options - 选项数组，每项包含 label 和 value
 * @property {boolean} isMultiple - 是否多选模式，默认 false
 */
const props = defineProps({
    options: {
        type: Array,
        required: true
    },
    isMultiple: {
        type: Boolean,
        default: false
    }
});

/** 选中的值，单选为单值，多选为数组 */
const modelValue = defineModel("modelValue");

/**
 * 切换选项选中状态
 * @description 单选模式直接替换值，多选模式切换选中/取消
 * @param {Object} option - 被点击的选项对象
 */
const toggleSelection = (option) => {
    if (!props.isMultiple) {
        modelValue.value = option.value;
    } else {
        const index = modelValue.value.findIndex((o) => o === option.value);
        if (index === -1) {
            modelValue.value.push(option.value);
        } else {
            modelValue.value.splice(index, 1);
        }
    }
};

/**
 * 判断选项是否被选中
 * @param {Object} option - 选项对象
 * @returns {boolean} 是否选中
 */
const isSelected = (option) => {
    if (!props.isMultiple) {
        return modelValue.value === option.value;
    }
    return modelValue.value.some((o) => o === option.value);
};
</script>

<style lang="less" scoped>
.selectable-group {
    display: flex;
    flex-wrap: wrap;
    justify-content: flex-start;
    gap: 10px;

    .option {
        padding: 0 8px;
        border: 1px solid #c8c9cc;
        cursor: pointer;
        box-sizing: border-box;
        display: flex;
        align-items: center;
        height: 30px;
        justify-content: center;
        font-size: 14px;
        border-radius: 2px;
        transition: all 0.2s ease-in-out;
        color: #c8c9cc;

        &:hover {
            color: var(--tig-primary);
            border-color: var(--tig-primary);
        }

        &.is-selected {
            color: var(--tig-primary);
            border-color: var(--tig-primary);
        }
    }

    .checkmark {
        width: 16px;
        height: 16px;
        border-radius: 50%;
        background-color: var(--tig-primary);
        margin: auto;
    }
}
</style>
