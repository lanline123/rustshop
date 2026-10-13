<template>
    <div class="dec-pc-spacer" :style="cssVars">
        <div class="dec-pc-spacer__content"></div>
    </div>
</template>

<script setup lang="ts">
/**
 * PC 空白辅助模块 - 预览组件
 * 简单的空白占位模块，支持自定义高度和背景色
 */
import { computed } from "vue";
import type { PcSpacerModule } from "./types";
import { pcSpacerDefaults } from ".";
import { deepMerge } from "../../../utils/object";

defineOptions({ name: "PcSpacerPreview" });

const props = defineProps<{
    module: Record<string, unknown>;
}>();

const moduleConfig = computed<PcSpacerModule>(() => {
    return deepMerge(pcSpacerDefaults, props.module as Partial<PcSpacerModule>);
});

const cssVars = computed(() => {
    const data = moduleConfig.value;
    const frame = data.frame;

    return {
        "--dec-pc-spacer-height": `${data.blankHeight ?? 30}px`,
        "--dec-pc-spacer-bg": data.backgroundColor || "transparent",
        "--dec-box-padding": `${frame?.boxPadding ?? 0}px`,
        "--dec-box-padding-top": `${frame?.boxPaddingTop ?? 0}px`,
        "--dec-box-padding-bottom": `${frame?.boxPaddingBottom ?? 0}px`,
        "--dec-box-radius": `${frame?.boxRadius ?? 0}px`
    };
});
</script>

<style lang="less" src="./preview.less" />
