<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3> {{ $t('分类模块（宽）设置') }} </h3>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('选择分类') }}</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-goods-group">
                    <SelectCategory :multiple="true" v-model:categoryId="module.catIds"></SelectCategory>
                </div>
            </div>
        </div>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import { PicList, LinkList } from '@/components/decorate'
import { ModuleType, PcCatProductType } from '@/types/decorate/decorate.d';
import { SelectCategory, SelectColor } from '@/components/select'
import { ProductSelect } from '@/components/decorate'
const module = defineModel<ModuleType & PcCatProductType>('module', {default: () => ({})});
</script>
