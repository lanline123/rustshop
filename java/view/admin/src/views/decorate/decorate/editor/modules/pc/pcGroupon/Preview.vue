<template>
    <div class="module-ad-con">
        <div class="pc-module-box" style="height: 100px">
            <div class="title">拼团</div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref } from "vue";
// formatFrame/CommonTitle 与旧 View.vue 保持同源导入（原样保留，不做顺手优化）
import { mergeDefaultModule, formatFrame, CommonTitle } from "@/views/decorate/decorate/src/modules/index";
import { pcGrouponDefaults } from "./index";
import type { PcGrouponModule } from "./types";

const props = defineProps<{ module: PcGrouponModule }>();
const module = props.module;
const time = ref(Date.now() + 1000 * 60 * 60 * 7);
// 旧数据缺字段时烧录默认值（原地写回 store 模块对象，与旧 View.vue 行为一致，契约）
mergeDefaultModule(module, pcGrouponDefaults);

if (module.grouponActivityItem) {
    module.groupType = module.grouponActivityItem.groupType;
}
</script>

<style lang="less" scoped></style>
