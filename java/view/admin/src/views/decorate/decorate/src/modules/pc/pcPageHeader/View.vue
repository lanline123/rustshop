<template>
    <div class="module-ad-con">
        <div class="pc-module-box" style="height:100px">
            <div class="title">{{ $t('通用页面头部') }}</div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref } from 'vue';
import { ModuleType ,PcPageHeaderType} from '@/types/decorate/decorate.d';
import { mergeDefaultModule } from '@/views/decorate/decorate/src/modules/'
const defaultModule = ref({
    headerStyle: 1,
    logo: {
        'picUrl': '',
        'picThumb': '',
    },
})
const module = defineModel<ModuleType & PcPageHeaderType>('module');
mergeDefaultModule(module.value, defaultModule.value)
</script>
<style lang="less" scoped></style>