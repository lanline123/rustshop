<template>
    <div class="module-ad-con">
        <div class="pc-module-box" style="height:100px">
            <div class="title">{{ $t('轮播Banner') }}</div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref } from 'vue';
import { ModuleType, PcBannerType } from '@/types/decorate/decorate.d';
import { mergeDefaultModule } from '@/views/decorate/decorate/src/modules/'
const defaultModule = ref({
    bannerStyle: 1,
    showCat: 1,
    logo: {
        'picUrl': '',
        'picThumb': '',
    },
    picList: [],
    picList2: [],
    picList3: [],
})
const module = defineModel<ModuleType & PcBannerType>('module');
mergeDefaultModule(module.value, defaultModule.value)
</script>

<style lang="less" scoped></style>