<template>
    <div class="module-ad-con">
        <div class="pc-module-box" style="height:100px">
            <div class="title">{{ $t('自定义模块1') }}</div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref } from 'vue';
import { ModuleType, PcCustomAd1Type } from '@/types/decorate/decorate.d';
import { mergeDefaultModule } from '@/views/decorate/decorate/src/modules/'
const defaultModule = ref({
    picList: [],
    picList2: [],
    picList3: [],
})
const module = defineModel<ModuleType & PcCustomAd1Type>('module');
mergeDefaultModule(module.value, defaultModule.value)
</script>

<style lang="less" scoped></style>