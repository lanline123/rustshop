<template>
    <div class="module-ad-con">
        <div class="pc-module-box" style="height:100px">
            <div class="title">{{ $t('分类模块 - 宽') }}</div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref } from 'vue';
import { ModuleType, PcCatProductType } from '@/types/decorate/decorate.d';
import { mergeDefaultModule } from '@/views/decorate/decorate/src/modules/'
const defaultModule = ref({
    catIds: [],
})
const module = defineModel<ModuleType & PcCatProductType>('module');
mergeDefaultModule(module.value, defaultModule.value)
</script>

<style lang="less" scoped></style>
