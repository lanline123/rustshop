<template>
    <div :class="'module-ad-con module-image_hotarea'">
        <div class="module-ad-content">
            <div :style="styleFormat.backgroundColor + styleFormat.height"></div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref, computed, Ref } from "vue";
import { imageFormat } from "@/utils/format";
import { ModuleType } from "@/types/decorate/decorate.d";
import { mergeDefaultModule, defaultFrame, formatFrame } from "@/views/decorate/decorate/src/modules/";
// 在modules加入要使用的模块
const module = defineModel<ModuleType>("module") as Ref<ModuleType>;
const defaultModule = ref({
    backgroundColor: "",
    blankHeight: 30
});
mergeDefaultModule(module.value, defaultModule.value);
const styleFormat = computed(() => {
    return {
        backgroundColor: `background-color: ${module.value.backgroundColor};`,
        height: `height: ${module.value.blankHeight}px;`
    };
});
</script>
