<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3> {{ $t('推荐商品-瀑布流') }} </h3>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('标题名称') }}</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-input-group">
                    <el-input v-model="module.titleName" :placeholder="$t('请输入标题名称')"></el-input>
                </div>
            </div>
        </div>
        <CommonProductEdit v-model="module.products" :numberMin="20" :numberStep="10"></CommonProductEdit>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import { ModuleType, PcRecommendProduct } from '@/types/decorate/decorate.d';
import CommonProductEdit from '../../src/commonProduct/Edit.vue'
const module = defineModel<ModuleType & PcRecommendProduct>('module', {default: () => ({})});
</script>