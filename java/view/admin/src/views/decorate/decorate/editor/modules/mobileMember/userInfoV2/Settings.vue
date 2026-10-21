<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>用户信息2</h3>
            <div class="dec-edit-desc">简化版用户信息模块，仅支持样式配置</div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">内容说明</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-edit-tip">该模块展示用户基本信息，无需手动配置。</div>
            </div>
        </div>
        <CommonFrameEdit v-model="module.frame"></CommonFrameEdit>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import { computed } from "vue";
import CommonFrameEdit from "@/views/decorate/decorate/src/modules/src/commonFrame/Edit.vue";
import { mergeDefaultModule, defaultFrame } from "@/views/decorate/decorate/src/modules/index";
import { useDecorateEditorStore } from "@/store/decorateEditor";
import type { UserInfoV2Module } from "./types";

const store = useDecorateEditorStore();
const module = computed(() => (store.activeModuleData ?? {}) as UserInfoV2Module);

mergeDefaultModule(module.value, {
    frame: {
        ...defaultFrame,
        backgroundColor: "#ffffff",
        boxRadius: 0,
        innerPadding: 16,
        boxPadding: 0,
        boxPaddingTop: 0,
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
