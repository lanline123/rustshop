<template>
    <div class="module-ad-con">
        <div class="pc-module-box" style="height: 100px">
            <div class="title">{{ $t('拼团') }}</div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { type Ref, ref } from "vue";
import type { ModuleActivityType, ModuleType } from "@/types/decorate/decorate.d";
import { mergeDefaultModule, defaultFrame, formatFrame, defaultTitle, CommonTitle } from "@/views/decorate/decorate/src/modules/";

// 在modules加入要使用的模块
const module = defineModel<ModuleType & ModuleActivityType>("module") as Ref<ModuleType & ModuleActivityType>;
const time = ref(Date.now() + 1000 * 60 * 60 * 7);
const defaultModule = ref({
    style: 2,
    goodsStyle: 1,
    goodsRadioStyle: 1,
    textAlign: 1,
    textWeight: 1,
    goodsNameRow: 2,
    goodsNamePadding: 1,
    showName: 1,
    showBrief: 1,
    showPrice: 1,
    goodsPadding: 5,
    buyBtnStyle: 1,
    backgroundColor: "",
    boxRadius: 0,
    innerPadding: 0,
    boxPadding: 10,
    boxPaddingTop: 5,
    boxPaddingBottom: 5,
    frame: defaultFrame,
    title: defaultTitle,
    groupType: 1,
    grouponActivityItem: undefined
});
mergeDefaultModule(module.value, defaultModule.value);

if (module.value.grouponActivityItem) {
    module.value.groupType = module.value.grouponActivityItem.groupType;
}
</script>

<style lang="less" scoped></style>
