<template>
    <div class="dec-pc-image-nav" :class="moduleClasses" :style="cssVars">
        <div class="dec-pc-image-nav__content">
            <ModuleTitle v-if="moduleConfig.title?.showTitle" :title="moduleConfig.title" />

            <div v-if="!picList.length" class="dec-pc-image-nav__empty">
                <div class="dec-pc-image-nav__empty-icon"></div>
                <div class="dec-pc-image-nav__empty-text">点击添加图文导航</div>
            </div>

            <template v-else>
                <template v-if="Number(moduleConfig.navType) === 1">
                    <div class="dec-pc-image-nav__grid">
                        <div v-for="(item, index) in fixedList" :key="index" class="dec-pc-image-nav__item">
                            <a class="dec-pc-image-nav__link" :href="item.picLink?.link || 'javascript:;'">
                                <div v-if="Number(moduleConfig.navStyle) === 1 || Number(moduleConfig.navStyle) === 2" class="dec-pc-image-nav__icon">
                                    <img class="dec-pc-image-nav__image" :src="formatImage(item.picUrl)" :alt="item.picTitle" />
                                </div>
                                <div v-if="Number(moduleConfig.navStyle) === 1 || Number(moduleConfig.navStyle) === 3" class="dec-pc-image-nav__text">
                                    {{ item.picTitle }}
                                </div>
                            </a>
                        </div>
                    </div>
                </template>

                <template v-else>
                    <Swiper :key="swiperKey" :swiper-option="swiperOption" :model-value="swiperPages">
                        <template #default="{ item }">
                            <div class="dec-pc-image-nav__swiper-page">
                                <div v-for="(subItem, subIndex) in item" :key="subIndex" class="dec-pc-image-nav__item">
                                    <a class="dec-pc-image-nav__link" :href="subItem.picLink?.link || 'javascript:;'">
                                        <div v-if="Number(moduleConfig.navStyle) === 1 || Number(moduleConfig.navStyle) === 2" class="dec-pc-image-nav__icon">
                                            <img class="dec-pc-image-nav__image" :src="formatImage(subItem.picUrl)" :alt="subItem.picTitle" />
                                        </div>
                                        <div v-if="Number(moduleConfig.navStyle) === 1 || Number(moduleConfig.navStyle) === 3" class="dec-pc-image-nav__text">
                                            {{ subItem.picTitle }}
                                        </div>
                                    </a>
                                </div>
                            </div>
                        </template>
                    </Swiper>
                </template>
            </template>
        </div>
    </div>
</template>

<script setup lang="ts">
/**
 * PC 图文导航模块 - 预览组件
 */
import { computed } from "vue";
import type { PcImageNavModule } from "./types";
import { pcImageNavDefaults } from ".";
import { Swiper } from "@/components/swiper";
import { ModuleTitle } from "../_shared";
import { deepMerge } from "../../../utils/object";

defineOptions({ name: "PcImageNavPreview" });

const props = defineProps<{
    module: Record<string, unknown>;
}>();

const moduleConfig = computed<PcImageNavModule>(() => {
    return deepMerge(pcImageNavDefaults, props.module as Partial<PcImageNavModule>);
});

const picList = computed(() => moduleConfig.value.picList || []);

const itemsPerPage = computed(() => {
    const rowNum = Number(moduleConfig.value.rowNum ?? 4);
    const colNum = Number(moduleConfig.value.colNum ?? 2);
    const safeRowNum = Number.isFinite(rowNum) ? Math.min(6, Math.max(3, rowNum)) : 4;
    const safeColNum = Number.isFinite(colNum) ? Math.min(3, Math.max(1, colNum)) : 2;
    return safeRowNum * safeColNum;
});

const fixedList = computed(() => {
    return picList.value.slice(0, itemsPerPage.value);
});

const swiperPages = computed(() => {
    if (moduleConfig.value.navType !== 2) return [];

    const pages: (typeof picList.value)[] = [];
    for (let i = 0; i < picList.value.length; i += itemsPerPage.value) {
        pages.push(picList.value.slice(i, i + itemsPerPage.value));
    }
    return pages;
});

const moduleClasses = computed(() => {
    const classes: string[] = [];
    const data = moduleConfig.value;
    classes.push(`dec-pc-image-nav--type-${data.navType ?? 1}`);
    classes.push(`dec-pc-image-nav--style-${data.navStyle ?? 1}`);
    classes.push(`dec-pc-image-nav--indicator-${data.picPageType ?? 1}`);
    if (Number(data.radioStyle) === 2) {
        classes.push("dec-pc-image-nav--rounded");
    }
    return classes;
});

const cssVars = computed(() => {
    const data = moduleConfig.value;
    const frame = data.frame;
    // 内容背景色（内容设置）作用在内容区，容器背景色（扩展设置-容器设置）作用在外层，两者各自独立
    const contentBgColor = data.backgroundColor || "#ffffff";
    const outerBgColor = frame?.backgroundColor || "#ffffff";
    const rowNum = Number(data.rowNum ?? 4);
    const colNum = Number(data.colNum ?? 2);

    return {
        "--dec-box-padding": `${frame?.boxPadding ?? 10}px`,
        "--dec-box-padding-top": `${frame?.boxPaddingTop ?? 5}px`,
        "--dec-box-padding-bottom": `${frame?.boxPaddingBottom ?? 5}px`,
        "--dec-box-radius": `${frame?.boxRadius ?? 0}px`,
        "--dec-bg-color": contentBgColor,
        "--dec-outer-bg": outerBgColor,
        "--dec-row-num": Number.isFinite(rowNum) ? Math.min(6, Math.max(3, rowNum)) : 4,
        "--dec-col-num": Number.isFinite(colNum) ? Math.min(3, Math.max(1, colNum)) : 2,
        "--dec-item-gap": `${data.imgPadding ?? 8}px`,
        "--dec-item-radius": Number(data.radioStyle) === 2 ? "8px" : "0",
        "--dec-indicator-color": data.swiperPageColor || "#333333"
    };
});

const swiperOption = computed(() => ({
    autoplay: {
        delay: 3000,
        disableOnInteraction: false,
        pauseOnMouseEnter: true
    },
    pagination: {
        clickable: true
    },
    loop: true,
    navigation: false
}));

// 分页数 / 每页数量变化时强制重建 Swiper，保证画布即时预览（与 pc-image-ad 同机制）
const swiperKey = computed(() => `${swiperPages.value.length}-${itemsPerPage.value}`);

function formatImage(url?: string): string {
    if (!url) {
        return 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" width="100" height="100"><rect fill="%23f0f0f0" width="100" height="100"/></svg>';
    }
    return url;
}
</script>

<style lang="less" src="./preview.less" />
