<template>
    <div class="dec-pc-notice" :style="cssVars">
        <div class="dec-pc-notice__wrapper">
            <div v-if="!displayText" class="dec-pc-notice__empty">
                <div class="dec-pc-notice__empty-text">点击添加通知公告</div>
            </div>

            <div v-else class="dec-pc-notice__content">
                <div v-if="noticeIcon" class="dec-pc-notice__icon">
                    <img class="dec-pc-notice__icon-img" :src="noticeIcon" alt="" />
                </div>
                <div class="dec-pc-notice__text" :title="displayText">{{ displayText }}</div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
/**
 * PC 公告模块 - 预览组件
 */
import { computed } from "vue";
import { productImage } from "@/utils/format";
import type { PcNoticeModule } from "./types";
import { pcNoticeDefaults } from ".";
import { deepMerge } from "../../../utils/object";

defineOptions({ name: "PcNoticePreview" });

const props = defineProps<{
    module: Record<string, unknown>;
}>();

const moduleConfig = computed<PcNoticeModule>(() => {
    return deepMerge(pcNoticeDefaults, props.module as Partial<PcNoticeModule>);
});

const displayText = computed(() => {
    const text = String(moduleConfig.value.text ?? "").trim();
    return text;
});

const noticeIcon = computed(() => productImage(moduleConfig.value.icoPic));

const cssVars = computed(() => {
    const frame = moduleConfig.value.frame;
    return {
        "--dec-box-padding": `${frame?.boxPadding ?? 10}px`,
        "--dec-box-padding-top": `${frame?.boxPaddingTop ?? 5}px`,
        "--dec-box-padding-bottom": `${frame?.boxPaddingBottom ?? 5}px`,
        "--dec-box-radius": `${frame?.boxRadius ?? 0}px`,
        "--dec-bg-color": frame?.backgroundColor ?? "#ffffff",
        "--dec-item-bg-color": frame?.itemBackgroundColor ?? "#fff8e6",
        "--dec-item-height": `${frame?.itemHeight ?? 35}px`,
        "--dec-item-radius": `${frame?.itemRadius ?? 4}px`,
        "--dec-text-color": frame?.textColor ?? "#555555"
    };
});
</script>

<style lang="less" src="./preview.less" />
