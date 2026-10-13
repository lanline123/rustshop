/**
 * @file 订单标记图标组件
 * @description 显示订单颜色标记图标
 * @features
 * - 5种颜色标记对应不同数值（1-5）
 * - 红色(1)/橙色(2)/绿色(3)/蓝色(4)/紫色(5)
 */
<template>
    <template v-if="props.value">
        <i class="iconfont-tig icon-tianchongxing-" :style="`color: ${signMap[value]}`"></i>
    </template>
</template>

<script setup lang="ts">
const props = defineProps({
    value: {
        type: Number,
        required: true
    }
});

const signMap: { [key: number]: string } = {
    1: "#ff5050",
    2: "#ffba1f",
    3: "#2ecf92",
    4: "#16afff",
    5: "#792dff"
};
</script>

<style lang="less" scoped>
.icon-tianchongxing- {
    cursor: pointer;
}
</style>
