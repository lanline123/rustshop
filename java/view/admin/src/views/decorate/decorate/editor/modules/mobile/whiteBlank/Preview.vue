<template>
    <div :class="'module-ad-con module-image_hotarea'">
        <div class="module-ad-content">
            <div :style="styleFormat.backgroundColor + styleFormat.height"></div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { computed } from "vue";
import { mergeDefaultModule } from "@/views/decorate/decorate/src/modules/index";
import { whiteBlankDefaults } from "./index";
import type { WhiteBlankModule } from "./types";

const props = defineProps<{ module: WhiteBlankModule }>();
const module = props.module;
// 旧数据缺字段时烧录默认值（原地写回 store 模块对象，与旧 View.vue 行为一致，契约）
mergeDefaultModule(module, whiteBlankDefaults);

const styleFormat = computed(() => {
    return {
        backgroundColor: `background-color: ${module.backgroundColor};`,
        height: `height: ${module.blankHeight}px;`
    };
});
</script>
