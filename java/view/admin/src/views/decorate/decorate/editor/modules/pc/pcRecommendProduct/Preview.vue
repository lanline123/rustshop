<template>
    <div class="module-ad-con">
        <div class="pc-module-box" style="height:100px">
            <div class="title">推荐商品-瀑布流</div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { mergeDefaultModule } from "@/views/decorate/decorate/src/modules/index";
import { pcRecommendProductDefaults } from "./index";
import type { PcRecommendProductModule } from "./types";

const props = defineProps<{ module: PcRecommendProductModule }>();
const module = props.module;
// 旧数据缺字段时烧录默认值（原地写回 store 模块对象，与旧 View.vue 行为一致，契约）
mergeDefaultModule(module, pcRecommendProductDefaults);
</script>

<style lang="less" scoped></style>
