<template>
    <div class="dec-image-hotarea" :style="cssVars">
        <div class="dec-image-hotarea__content">
            <ModuleTitle v-if="moduleConfig.title?.showTitle" :title="moduleConfig.title" />

            <div v-if="!picList.length" class="dec-image-hotarea__empty">
                <div class="dec-image-hotarea__empty-icon"></div>
                <div class="dec-image-hotarea__empty-text">点击添加热图绘制</div>
            </div>

            <template v-else>
                <div class="dec-image-hotarea__wrapper">
                    <div class="dec-image-hotarea__list">
                        <div v-for="(pic, index) in picList" :key="index" class="dec-image-hotarea__item">
                            <div :ref="(el) => setPicWrapRef(el as HTMLDivElement | null, index)" class="dec-image-hotarea__pic-wrap">
                                <img class="dec-image-hotarea__image" :src="formatImage(pic.picUrl)" :alt="pic.picTitle" />
                                <a
                                    v-for="(hotarea, idx) in pic.hotarea"
                                    :key="idx"
                                    class="dec-image-hotarea__area"
                                    :href="hotarea.link?.link || 'javascript:;'"
                                    :style="getHotareaStyle(hotarea, index)"
                                >
                                    <span class="dec-image-hotarea__area-text">{{ hotarea.link?.name }}</span>
                                </a>
                            </div>
                        </div>
                    </div>
                </div>
            </template>
        </div>
    </div>
</template>

<script setup lang="ts">
/**
 * PC 热图绘制模块 - 预览组件
 * 说明：在图片上显示可点击热区，热区位置根据容器宽度自动缩放
 */
import { ref, computed, onUnmounted } from "vue";
import type { PcImageHotareaModule, HotareaItem } from "./types";
import { pcImageHotareaDefaults } from ".";
import { ModuleTitle } from "../_shared";
import { deepMerge } from "../../../utils/object";

defineOptions({ name: "PcImageHotareaPreview" });

// 热区绘制区域原始宽度
const HOTAREA_ORIGINAL_WIDTH = 500;

const props = defineProps<{
    module: Record<string, unknown>;
}>();

// 每张图片容器宽度（用于把 500px 基准坐标缩放到实际预览宽度）
const picWrapWidths = ref<Record<number, number>>({});
const picWrapEls = new Map<number, HTMLDivElement>();
let resizeObserver: ResizeObserver | null = null;

const moduleConfig = computed<PcImageHotareaModule>(() => {
    return deepMerge(pcImageHotareaDefaults, props.module as Partial<PcImageHotareaModule>);
});

const picList = computed(() => moduleConfig.value.picList || []);

const cssVars = computed(() => {
    const data = moduleConfig.value;
    const frame = data.frame;

    return {
        "--dec-box-padding": `${frame?.boxPadding ?? 10}px`,
        "--dec-box-padding-top": `${frame?.boxPaddingTop ?? 5}px`,
        "--dec-box-padding-bottom": `${frame?.boxPaddingBottom ?? 5}px`,
        "--dec-box-radius": `${frame?.boxRadius ?? 0}px`,
        "--dec-bg-color": frame?.backgroundColor ?? "#ffffff",
        "--dec-inner-padding": `${frame?.leftAndRightPadding ?? frame?.innerPadding ?? 0}px`,
        "--dec-item-gap": `${data.imgPadding ?? 0}px`
    };
});

function resizeValue(size: number, picIndex: number): number {
    const width = picWrapWidths.value[picIndex] || 0;
    if (!width) return size;
    return (width / HOTAREA_ORIGINAL_WIDTH) * size;
}

function getHotareaStyle(hotarea: HotareaItem, picIndex: number): Record<string, string> {
    return {
        width: `${resizeValue(hotarea.width ?? 0, picIndex)}px`,
        height: `${resizeValue(hotarea.height ?? 0, picIndex)}px`,
        top: `${resizeValue(hotarea.top ?? 0, picIndex)}px`,
        left: `${resizeValue(hotarea.left ?? 0, picIndex)}px`
    };
}

function ensureResizeObserver() {
    if (resizeObserver || typeof ResizeObserver === "undefined") return;
    resizeObserver = new ResizeObserver((entries) => {
        let changed = false;
        const next: Record<number, number> = { ...picWrapWidths.value };
        for (const entry of entries) {
            const target = entry.target as HTMLDivElement;
            const idx = Number(target.dataset.hotareaIndex ?? -1);
            if (idx >= 0) {
                const width = Number(entry.contentRect.width.toFixed(2));
                if (next[idx] !== width) {
                    next[idx] = width;
                    changed = true;
                }
            }
        }
        if (changed) {
            picWrapWidths.value = next;
        }
    });
}

function setPicWrapRef(el: HTMLDivElement | null, index: number) {
    ensureResizeObserver();
    const prev = picWrapEls.get(index);
    if (!el) {
        if (prev && resizeObserver) {
            resizeObserver.unobserve(prev);
        }
        picWrapEls.delete(index);
        if (Object.prototype.hasOwnProperty.call(picWrapWidths.value, index)) {
            const { [index]: _, ...rest } = picWrapWidths.value;
            picWrapWidths.value = rest;
        }
        return;
    }

    // ref 回调在每次渲染都会执行，这里避免无意义写入导致递归更新
    if (prev === el) {
        const width = Number(el.offsetWidth.toFixed(2));
        if (picWrapWidths.value[index] !== width) {
            picWrapWidths.value = { ...picWrapWidths.value, [index]: width };
        }
        return;
    }

    if (prev && resizeObserver) {
        resizeObserver.unobserve(prev);
    }
    el.dataset.hotareaIndex = String(index);
    picWrapEls.set(index, el);
    const width = Number(el.offsetWidth.toFixed(2));
    if (picWrapWidths.value[index] !== width) {
        picWrapWidths.value = { ...picWrapWidths.value, [index]: width };
    }
    if (resizeObserver) {
        resizeObserver.observe(el);
    }
}

function formatImage(url?: string): string {
    if (!url) {
        return 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" width="100" height="100"><rect fill="%23f0f0f0" width="100" height="100"/></svg>';
    }
    return url;
}

onUnmounted(() => {
    if (resizeObserver) {
        resizeObserver.disconnect();
        resizeObserver = null;
    }
    picWrapEls.clear();
});
</script>

<style lang="less" src="./preview.less" />
