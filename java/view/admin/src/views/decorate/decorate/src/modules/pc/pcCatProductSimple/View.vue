<template>
    <div class="module-ad-con">
        <div class="pc-module-box" style="height:100px">
            <div class="title">{{ $t('分类商品 - 简约') }}</div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref } from 'vue';
import { ModuleType, PcCatProductType } from '@/types/decorate/decorate.d';
import { mergeDefaultModule } from '@/views/decorate/decorate/src/modules/'
const defaultModule = ref({
    baseStyle: 1,
    title: '',
    products:{
        productSelectType: 1,
        productCategoryId: 1,
        productNumber: 8,
        productIds: [],
        productTag: '',
    },
    moreLink: '',
    picList: [],

})
const module = defineModel<ModuleType & PcCatProductType>('module');
mergeDefaultModule(module.value, defaultModule.value)
</script>

<style lang="less" scoped></style>
