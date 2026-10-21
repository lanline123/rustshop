<template>
    <div class="module-ad-con">
        <div class="pc-module-box" style="height:100px">
            <div class="title">分组轮播Banner</div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { mergeDefaultModule } from "@/views/decorate/decorate/src/modules/index";
import { pcGroupBannerDefaults } from "./index";
import type { PcGroupBannerModule } from "./types";

const props = defineProps<{ module: PcGroupBannerModule }>();
const module = props.module;
// 旧数据缺字段时烧录默认值（原地写回 store 模块对象，与旧 View.vue 行为一致，契约）
mergeDefaultModule(module, pcGroupBannerDefaults);
</script>

<style lang="less" scoped></style>
