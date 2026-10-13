<template>
    <div class="dec-pc-group-banner" :class="moduleClasses" :style="cssVars">
        <div v-if="!picList.length" class="dec-pc-group-banner__empty">
            <div class="dec-pc-group-banner__empty-text">点击添加分组轮播Banner</div>
        </div>

        <div v-else class="dec-pc-group-banner__inner">
            <Swiper :key="swiperKey" :swiper-option="swiperOption" :model-value="picList">
                <template #default="{ item }">
                    <div class="dec-pc-group-banner__slide">
                        <a class="dec-pc-group-banner__link" :href="item.picLink?.link || 'javascript:;'" target="_blank" rel="noopener noreferrer">
                            <img class="dec-pc-group-banner__img" :src="formatImage(item.picUrl)" alt="" />
                        </a>
                    </div>
                </template>
            </Swiper>
        </div>
    </div>
</template>

<script setup lang="ts">
/**
 * PC 分组轮播 Banner 模块 - 预览组件
 * 说明：
 * - 复用后台 Swiper 封装
 * - 为保证 loop 在小图数时可用，按"至少 slidesPerView*2"扩展列表（参考海外前台实现）
 */
import { computed } from "vue";
import type { PcGroupBannerModule } from "./types";
import { pcGroupBannerDefaults } from ".";
import { Swiper } from "@/components/swiper";
import { deepMerge } from "../../../utils/object";

defineOptions({ name: "PcGroupBannerPreview" });

const props = defineProps<{
    module: Record<string, unknown>;
}>();

const moduleConfig = computed<PcGroupBannerModule>(() => {
    return deepMerge(pcGroupBannerDefaults, props.module as Partial<PcGroupBannerModule>);
});

const picList = computed(() => moduleConfig.value.picList || []);

const displayNum = computed(() => {
    const raw = Number(moduleConfig.value.number ?? 3);
    if (!Number.isFinite(raw)) return 3;
    return Math.min(3, Math.max(1, raw));
});

/**
 * 模块类名（布局变体）
 */
const moduleClasses = computed(() => {
    const classes: string[] = [];
    classes.push(`dec-pc-group-banner--num-${displayNum.value}`);
    return classes;
});

/**
 * CSS 变量（传递动态值到样式）
 */
const cssVars = computed(() => {
    const data = moduleConfig.value;
    const frame = data.frame;
    const backgroundColor = frame?.backgroundColor;
    return {
        "--dec-box-padding": `${frame?.boxPadding ?? 10}px`,
        "--dec-box-padding-top": `${frame?.boxPaddingTop ?? 0}px`,
        "--dec-box-padding-bottom": `${frame?.boxPaddingBottom ?? 0}px`,
        "--dec-box-radius": `${frame?.boxRadius ?? 8}px`,
        "--dec-bg-color": backgroundColor || "transparent",
        "--dec-slide-height": `${frame?.slideHeight ?? 195}px`
    };
});

/**
 * 预览策略（编辑器内）：
 * - 不复制图片"补齐"列表，避免出现"明明 2 张图却渲染出更多"的困惑
 * - 当图片数量不足以支持 loop 时自动关闭 loop，并把 slidesPerView 限制在实际数量以内
 */
const realSlidesPerView = computed(() => {
    const count = picList.value.length;
    if (count <= 0) return 1;
    return Math.min(displayNum.value, count);
});

const loopEnabled = computed(() => {
    const count = picList.value.length;
    const perView = realSlidesPerView.value;
    return count >= perView * 2;
});

const swiperOption = computed(() => {
    const num = realSlidesPerView.value;
    return {
        autoplay: {
            delay: 5000,
            disableOnInteraction: false,
            pauseOnMouseEnter: true
        },
        // 默认隐藏底部指示点（圆点）
        pagination: false,
        navigation: picList.value.length > 1,
        effect: "slide",
        spaceBetween: 30,
        slidesPerView: num,
        slidesPerGroup: num,
        loop: loopEnabled.value,
        ...(loopEnabled.value
            ? {
                  loopAdditionalSlides: num,
                  loopedSlidesLimit: false
              }
            : {})
    };
});

// 图片数量 / 一组显示数变化时强制重建 Swiper，保证画布即时预览（与 pc-image-ad 同机制）
const swiperKey = computed(() => `${picList.value.length}-${displayNum.value}`);

function formatImage(url?: string): string {
    if (!url) {
        return 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" width="100" height="100"><rect fill="%23f0f0f0" width="100" height="100"/></svg>';
    }
    return url;
}
</script>

<style lang="less" src="./preview.less" />
