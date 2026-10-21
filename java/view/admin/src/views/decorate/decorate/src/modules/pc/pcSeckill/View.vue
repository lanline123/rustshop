<template>
    <div class="module-ad-con">
        <div class="pc-module-box" style="height: 100px">
            <div class="title">{{ $t('限时秒杀') }}</div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref } from "vue";
import { ModuleType, PcSeckillType } from "@/types/decorate/decorate.d";
import { mergeDefaultModule } from "@/views/decorate/decorate/src/modules/";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const defaultModule = ref({
    seckillTitle: t('全场优惠限时9折起'),
});
const module = defineModel<ModuleType & PcSeckillType>("module");
mergeDefaultModule(module.value, defaultModule.value);
</script>

<style lang="less" scoped></style>
