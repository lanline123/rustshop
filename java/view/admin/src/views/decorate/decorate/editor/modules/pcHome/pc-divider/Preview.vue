<template>
    <div :class="['dec-divider', `dec-divider--type-${moduleConfig.lineType ?? 2}`, `dec-divider--style-${moduleConfig.lineStyle ?? 1}`]" :style="cssVars">
        <div class="dec-divider__content">
            <div class="dec-divider__wrapper">
                <template v-if="moduleConfig.lineType === 2">
                    <div class="dec-divider__line"></div>
                </template>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
/**
 * PC 分割线模块 - 预览组件
 */
import { computed } from "vue";
import type { PcDividerModule } from "./types";
import { pcDividerDefaults } from ".";
import { deepMerge } from "../../../utils/object";

defineOptions({ name: "PcDividerPreview" });

const props = defineProps<{
    module: Record<string, unknown>;
}>();

const moduleConfig = computed<PcDividerModule>(() => {
    return deepMerge(pcDividerDefaults, props.module as Partial<PcDividerModule>);
});

const cssVars = computed(() => {
    const data = moduleConfig.value;
    const frame = data.frame;

    return {
        "--dec-box-padding": `${frame?.boxPadding ?? 10}px`,
        "--dec-box-padding-top": `${frame?.boxPaddingTop ?? 5}px`,
        "--dec-box-padding-bottom": `${frame?.boxPaddingBottom ?? 5}px`,
        "--dec-box-radius": `${frame?.boxRadius ?? 0}px`,
        "--dec-bg-color": frame?.backgroundColor ?? "#ffffff",
        "--dec-item-height": `${frame?.itemHeight ?? 10}px`
    };
});
</script>

<style lang="less" src="./preview.less" />
