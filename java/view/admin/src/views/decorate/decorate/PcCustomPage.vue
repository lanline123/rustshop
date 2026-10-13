<template>
    <DecorateIndex :decorateType="3" />
</template>
<script lang="ts" setup>
import DecorateIndex from "./Index.vue";
import { useThemeStore } from "@/store/theme";

const themeStore = useThemeStore();
themeStore.getThemeInfo();
</script>
<style lang="less" scoped>
.modules-item-pc_cat_product_normal {
    width: 50%;
}
:deep(.list-item){
    cursor: pointer;
}
</style>
