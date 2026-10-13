<template>
    <div class="dec-pc-title-ad" :class="moduleClasses" :style="cssVars">
        <div class="dec-pc-title-ad__content">
            <div v-if="!hasTitleText" class="dec-pc-title-ad__empty">
                <div class="dec-pc-title-ad__empty-text">点击添加标题</div>
            </div>

            <div v-else class="dec-pc-title-ad__wrap">
                <div class="dec-pc-title-ad__inner" :style="titleContainerStyle">
                    <div class="dec-pc-title-ad__left">
                        <div class="dec-pc-title-ad__title" :style="titleColorStyle">{{ title.titleText }}</div>
                        <div v-if="title.descText" class="dec-pc-title-ad__desc" :style="descColorStyle">{{ title.descText }}</div>
                    </div>

                    <a v-if="Number(title.showMore) === 1" class="dec-pc-title-ad__more" :href="title.moreLink?.link || 'javascript:;'" :style="moreColorStyle">
                        <span>查看更多</span>
                        <span class="dec-pc-title-ad__more-arrow">&gt;</span>
                    </a>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
/**
 * PC 文本标题模块 - 预览组件
 */
import { computed } from "vue";
import type { PcTitleAdModule } from "./types";
import { pcTitleAdDefaults } from ".";
import { deepMerge } from "../../../utils/object";

defineOptions({ name: "PcTitleAdPreview" });

const props = defineProps<{
    module: Record<string, unknown>;
}>();

const moduleConfig = computed<PcTitleAdModule>(() => {
    return deepMerge(pcTitleAdDefaults, props.module as Partial<PcTitleAdModule>);
});

const title = computed(() => moduleConfig.value.title ?? {});
const frame = computed(() => moduleConfig.value.frame ?? {});

const hasTitleText = computed(() => {
    const text = String(title.value.titleText ?? "").trim();
    return text.length > 0;
});

const moduleClasses = computed(() => {
    const t = title.value;
    const align = Number(t.titleAlign ?? 1);
    const style = Number(t.titleStyle ?? 1);
    return [`dec-pc-title-ad--align-${align}`, `dec-pc-title-ad--style-${style}`];
});

const cssVars = computed(() => {
    const f = frame.value;
    return {
        "--dec-bg-color": f.backgroundColor ?? "#ffffff",
        "--dec-box-radius": `${f.boxRadius ?? 0}px`,
        "--dec-box-padding": `${f.boxPadding ?? 10}px`,
        "--dec-box-padding-top": `${f.boxPaddingTop ?? 5}px`,
        "--dec-box-padding-bottom": `${f.boxPaddingBottom ?? 5}px`,
        "--dec-inner-padding": `${f.leftAndRightPadding ?? f.innerPadding ?? 0}px`,
        "--dec-left-right-padding": `${f.leftAndRightPadding ?? 0}px`
    };
});

const titleContainerStyle = computed(() => {
    const t = title.value;
    const bg1 = String(t.titleBackground ?? "");
    const bg2 = String(t.titleBackground2 ?? "");
    const radius = Number(title.value.titleRadius ?? 0);

    let background = "";
    if (bg1 && bg2) {
        background = `linear-gradient(to right, ${bg1}, ${bg2})`;
    } else if (bg1) {
        background = bg1;
    }

    return {
        borderRadius: `${Number.isFinite(radius) ? radius : 0}px`,
        background: background || ""
    };
});

const titleColorStyle = computed(() => ({ color: title.value.titleColor || "" }));
const descColorStyle = computed(() => ({ color: title.value.descColor || "" }));
const moreColorStyle = computed(() => ({ color: title.value.moreColor || "" }));
</script>

<style lang="less" src="./preview.less" />
