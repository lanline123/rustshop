<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>{{ $t('固定功能区') }}</h3>
            <div class="dec-edit-desc">{{ $t('系统固定功能区，包含账期、配送等端内必备入口') }}</div>
        </div>
        <Tabs :tabs="tabs" v-model="activeName"></Tabs>
        <div v-if="activeName === 'content'">
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('内容说明') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-edit-tip">{{ $t('该模块为系统固定功能区，会根据当前端启用的能力自动展示对应入口卡片，可调整位置和样式，不可删除/复制/隐藏。') }}</div>
                </div>
            </div>
        </div>
        <div v-if="activeName === 'extend'">
            <CommonFrameEdit v-model="module.frame"></CommonFrameEdit>
        </div>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import { ref } from "vue";
import Tabs from "@/components/tabs/Index.vue";
import CommonFrameEdit from "../../src/commonFrame/Edit.vue";
import { ModuleType, ModuleFrameType } from "@/types/decorate/decorate.d";
import { mergeDefaultModule, defaultFrame } from "@/views/decorate/decorate/src/modules/";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
type ModuleFixedFunctionalAreaType = Omit<ModuleType, "frame"> & {
    frame: ModuleFrameType;
};

const module = defineModel<ModuleFixedFunctionalAreaType>("module", { default: () => ({}) });

const tabs = ref<any[]>([
    { label: t('内容设置'), name: "content" },
    { label: t('扩展设置'), name: "extend" }
]);
const activeName = ref("content");

mergeDefaultModule(module.value, {
    frame: {
        ...defaultFrame,
        backgroundColor: "#ffffff",
        boxRadius: 12,
        innerPadding: 16,
        boxPadding: 5,
        boxPaddingTop: 10,
        boxPaddingBottom: 0
    }
});
</script>
<style lang="less" scoped>
.dec-edit-tip {
    color: #999;
    font-size: 12px;
    line-height: 1.6;
}
</style>
