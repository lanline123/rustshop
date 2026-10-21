/** 客户标记图标 - 根据标记值显示对应颜色的星星图标 */
<template>
    <template v-if="props.value">
        <i class="iconfont-tig icon-tianchongxing-" :style="`color: ${signMap[value]}`"></i>
    </template>
</template>

<script setup lang="ts">
const props = defineProps({
    /** 标记值：1-红, 2-黄, 3-绿, 4-蓝, 5-紫 */
    value: {
        type: Number,
        required: true
    }
});

/** 标记值到颜色的映射 */
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
