<template>
    <el-select v-model="value" style="width:100px">
        <el-option value="" :label="$t('请选择')" />
        <el-option value="hot" :label="$t('热销商品')" />
        <el-option value="best" :label="$t('推荐商品')" />
        <el-option value="new" :label="$t('新品商品')" />
        <!-- <el-option value="promote" label="秒杀商品" /> -->
    </el-select>
</template>
<script setup lang="ts">
import { ref, toRefs, onMounted } from "vue"
const value = defineModel('modelValue');   
</script>
<style lang="less" scoped></style>
