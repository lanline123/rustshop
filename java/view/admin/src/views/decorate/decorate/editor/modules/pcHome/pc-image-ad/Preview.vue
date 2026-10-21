<template>
    <div class="dec-image-ad" :class="moduleClasses" :style="cssVars">
        <div class="dec-image-ad__content">
            <ModuleTitle v-if="moduleConfig.title?.showTitle" :title="moduleConfig.title" />

            <div v-if="!picList.length" class="dec-image-ad__empty">
                <div class="dec-image-ad__empty-icon"></div>
                <div class="dec-image-ad__empty-text">点击添加图片广告</div>
            </div>

            <template v-else>
                <template v-if="moduleConfig.picType === 2">
                    <div class="dec-image-ad__swiper">
                        <Swiper :key="swiperKey" :swiper-option="swiperOption" :model-value="picList">
                            <template #default="{ item }">
                                <div class="dec-image-ad__item">
                                    <a class="dec-image-ad__link" :href="item.picLink?.link || 'javascript:;'">
                                        <img class="dec-image-ad__image" :src="formatImage(item.picUrl)" alt="" />
                                    </a>
                                </div>
                            </template>
                        </Swiper>
                    </div>
                </template>

                <template v-else>
                    <div class="dec-image-ad__list">
                        <div v-for="(pic, index) in picList" :key="index" class="dec-image-ad__item">
                            <a class="dec-image-ad__link" :href="pic.picLink?.link || 'javascript:;'">
                                <img class="dec-image-ad__image" :src="formatImage(pic.picUrl)" alt="" />
                            </a>
                        </div>
                    </div>
                </template>
            </template>
        </div>
    </div>
</template>

<script setup lang="ts">
/**
 * PC 图片广告模块 - 预览组件
 * 支持一行一/二/三/四个与轮播布局、isFluxWidth 通宽预览
 */
import { computed } from "vue";
import type { PcImageAdModule } from "./types";
import { pcImageAdDefaults } from ".";
import { Swiper } from "@/components/swiper";
import { ModuleTitle } from "../_shared";
import { deepMerge } from "../../../utils/object";

defineOptions({ name: "PcImageAdPreview" });

const props = defineProps<{
    module: Record<string, unknown>;
}>();

const moduleConfig = computed<PcImageAdModule>(() => {
    return deepMerge(pcImageAdDefaults, props.module as Partial<PcImageAdModule>);
});

const picList = computed(() => moduleConfig.value.picList || []);

const moduleClasses = computed(() => {
    const classes: string[] = [];
    const data = moduleConfig.value;

    classes.push(`dec-image-ad--layout-${data.picType ?? 1}`);

    if (data.picType === 2) {
        classes.push(`dec-image-ad--indicator-${data.picPageType ?? 1}`);
    }

    if (Number(data.picRadioStyle) === 2) {
        classes.push("dec-image-ad--rounded");
    }

    if (data.isFluxWidth === 1) {
        classes.push("dec-image-ad--flux");
    }

    return classes;
});

const cssVars = computed(() => {
    const data = moduleConfig.value;
    const frame = data.frame;
    const isFluxWidth = data.isFluxWidth === 1;

    return {
        "--dec-box-padding": `${isFluxWidth ? 0 : (frame?.boxPadding ?? 10)}px`,
        "--dec-box-padding-top": `${frame?.boxPaddingTop ?? 5}px`,
        "--dec-box-padding-bottom": `${frame?.boxPaddingBottom ?? 5}px`,
        "--dec-box-radius": `${frame?.boxRadius ?? 0}px`,
        "--dec-bg-color": frame?.backgroundColor ?? "#ffffff",
        "--dec-inner-padding": `${data.imgPadding ?? 0}px`,
        "--dec-item-gap": `${data.imgPadding ?? 0}px`,
        "--dec-item-radius": Number(data.picRadioStyle) === 2 ? "8px" : "0",
        "--dec-indicator-color": data.swiperPageColor ?? "#333333"
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
    slidesPerView: moduleConfig.value.swiperPreView || 1
}));

// 图片数量 / 一行显示数变化时强制重建 Swiper，保证画布即时预览（与 mobilePro/productCarousel 同机制）
const swiperKey = computed(() => `${picList.value.length}-${moduleConfig.value.swiperPreView || 1}`);

function formatImage(url?: string): string {
    if (!url) {
        return 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" width="100" height="100"><rect fill="%23f0f0f0" width="100" height="100"/></svg>';
    }
    return url;
}
</script>

<style lang="less" src="./preview.less" />
