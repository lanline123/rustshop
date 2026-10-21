<template>
    <div class="module-ad-con">
        <div class="pc-module-box" style="height: 100px">
            <div class="title">{{ $t('优惠券') }}</div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref } from "vue";
import { ModuleType } from "@/types/decorate/decorate.d";
import { mergeDefaultModule } from "@/views/decorate/decorate/src/modules/";
const defaultModule = ref({});
const module = defineModel<ModuleType>("module");
mergeDefaultModule(module.value, defaultModule.value);
</script>

<style lang="less" scoped></style>
