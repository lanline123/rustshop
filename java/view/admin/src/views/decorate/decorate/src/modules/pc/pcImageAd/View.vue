<template>
    <div class="module-ad-con">
        <div class="pc-module-box" style="height: 100px">
            <div class="title">{{ $t('通宽广告图') }}</div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref } from "vue";
import { ModuleType, PcImageAd } from "@/types/decorate/decorate.d";
import { mergeDefaultModule } from "@/views/decorate/decorate/src/modules/";
const defaultModule = ref({
    picList: [],
    boxPaddingTop: 10,
    boxPaddingBottom: 10,
});
const module = defineModel<ModuleType & PcImageAd>("module");
mergeDefaultModule(module.value, defaultModule.value);
</script>

<style lang="less" scoped></style>
