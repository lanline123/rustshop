/**
 * @file 勾选图标组件
 * @description 可切换选中状态的圆形勾选图标，常用于列表项的选择操作
 * @features
 * - 点击切换选中/未选中状态
 * - 选中时显示绿色背景和勾选图标
 * - 支持双向绑定 v-model:selected
 */
<template>
    <div @click="toggleSelected" class="box-warp">
        <div class="check-icon iconfont-admin icon-gou" :class="{ 'selected': selected }"></div>
    </div>
</template>

<script setup lang="ts">

import { computed } from "vue";

/** 选中状态，支持 v-model 双向绑定 */
const selected = defineModel<Boolean>("selected");
const emit = defineEmits(["update:selected", "selected-change"]);

/**
 * 切换选中状态
 * @description 点击时触发，反转当前选中状态并通过事件通知父组件
 */
const toggleSelected = () => {
    // selected.value = !selected.value;
};

/**
 * 计算当前是否选中
 * @returns {boolean} 选中状态
 */
// const isSelected = computed(() => {
//     return selected.value;
// });

</script>

<style lang="less" scoped>
.box-warp {
    display: flex;
    width: 40px;
    height: 40px;
    justify-content: center;
    align-items: center;

    .check-icon {
        width: 16px;
        height: 16px;
        cursor: pointer;
        border-radius: 50%;
        background-color: #c8c9cc;
        padding: 2px;
        display: flex;
        justify-content: center;
        align-items: center;
        box-sizing: border-box;
        color: white;
        font-size: 12px;
    }

    .selected {
        background-color: green;
    }
}


</style>
