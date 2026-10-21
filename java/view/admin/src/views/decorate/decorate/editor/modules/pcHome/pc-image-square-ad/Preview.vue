<template>
    <div class="dec-image-cube" :class="moduleClasses" :style="cssVars">
        <div class="dec-image-cube__content">
            <ModuleTitle v-if="moduleConfig.title?.showTitle" :title="moduleConfig.title" />

            <div v-if="!picList.length" class="dec-image-cube__empty">
                <div class="dec-image-cube__empty-icon"></div>
                <div class="dec-image-cube__empty-text">点击添加图片魔方</div>
            </div>

            <template v-else>
                <div class="dec-image-cube__wrapper">
                    <!-- 布局类型5：左一右二 -->
                    <template v-if="moduleConfig.picType === 5">
                        <div class="dec-image-cube__layout-lr">
                            <div class="dec-image-cube__item dec-image-cube__item--full-height">
                                <a class="dec-image-cube__link" :href="picList[0]?.picLink?.link || 'javascript:;'">
                                    <img class="dec-image-cube__image" :src="formatImage(picList[0]?.picUrl)" alt="" />
                                </a>
                            </div>
                            <div class="dec-image-cube__grid-rows">
                                <div class="dec-image-cube__item">
                                    <a class="dec-image-cube__link" :href="picList[1]?.picLink?.link || 'javascript:;'">
                                        <img class="dec-image-cube__image" :src="formatImage(picList[1]?.picUrl)" alt="" />
                                    </a>
                                </div>
                                <div class="dec-image-cube__item">
                                    <a class="dec-image-cube__link" :href="picList[2]?.picLink?.link || 'javascript:;'">
                                        <img class="dec-image-cube__image" :src="formatImage(picList[2]?.picUrl)" alt="" />
                                    </a>
                                </div>
                            </div>
                        </div>
                    </template>

                    <!-- 布局类型7：上一下二 -->
                    <template v-else-if="moduleConfig.picType === 7">
                        <div class="dec-image-cube__layout-tb">
                            <div class="dec-image-cube__item">
                                <a class="dec-image-cube__link" :href="picList[0]?.picLink?.link || 'javascript:;'">
                                    <img class="dec-image-cube__image" :src="formatImage(picList[0]?.picUrl)" alt="" />
                                </a>
                            </div>
                            <div class="dec-image-cube__grid-cols">
                                <div class="dec-image-cube__item">
                                    <a class="dec-image-cube__link" :href="picList[1]?.picLink?.link || 'javascript:;'">
                                        <img class="dec-image-cube__image" :src="formatImage(picList[1]?.picUrl)" alt="" />
                                    </a>
                                </div>
                                <div class="dec-image-cube__item">
                                    <a class="dec-image-cube__link" :href="picList[2]?.picLink?.link || 'javascript:;'">
                                        <img class="dec-image-cube__image" :src="formatImage(picList[2]?.picUrl)" alt="" />
                                    </a>
                                </div>
                            </div>
                        </div>
                    </template>

                    <!-- 布局类型6：左一右三 -->
                    <template v-else-if="moduleConfig.picType === 6">
                        <div class="dec-image-cube__layout-lr-3">
                            <div class="dec-image-cube__item dec-image-cube__item--full-height">
                                <a class="dec-image-cube__link" :href="picList[0]?.picLink?.link || 'javascript:;'">
                                    <img class="dec-image-cube__image" :src="formatImage(picList[0]?.picUrl)" alt="" />
                                </a>
                            </div>
                            <div class="dec-image-cube__grid-nested">
                                <div class="dec-image-cube__item">
                                    <a class="dec-image-cube__link" :href="picList[1]?.picLink?.link || 'javascript:;'">
                                        <img class="dec-image-cube__image" :src="formatImage(picList[1]?.picUrl)" alt="" />
                                    </a>
                                </div>
                                <div class="dec-image-cube__grid-cols">
                                    <div class="dec-image-cube__item">
                                        <a class="dec-image-cube__link" :href="picList[2]?.picLink?.link || 'javascript:;'">
                                            <img class="dec-image-cube__image" :src="formatImage(picList[2]?.picUrl)" alt="" />
                                        </a>
                                    </div>
                                    <div class="dec-image-cube__item">
                                        <a class="dec-image-cube__link" :href="picList[3]?.picLink?.link || 'javascript:;'">
                                            <img class="dec-image-cube__image" :src="formatImage(picList[3]?.picUrl)" alt="" />
                                        </a>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </template>

                    <!-- 其他布局 -->
                    <template v-else>
                        <div class="dec-image-cube__list">
                            <div v-for="(pic, index) in picList" :key="index" class="dec-image-cube__item">
                                <a class="dec-image-cube__link" :href="pic.picLink?.link || 'javascript:;'">
                                    <img class="dec-image-cube__image" :src="formatImage(pic.picUrl)" alt="" />
                                </a>
                            </div>
                        </div>
                    </template>
                </div>
            </template>
        </div>
    </div>
</template>

<script setup lang="ts">
/**
 * PC 图片魔方模块 - 预览组件
 * 说明：沿用移动端"图片魔方"的布局与样式，主要用于 PC 装修预览
 */
import { computed } from "vue";
import type { PcImageSquareAdModule } from "./types";
import { pcImageSquareAdDefaults } from ".";
import { ModuleTitle } from "../_shared";
import { deepMerge } from "../../../utils/object";

defineOptions({ name: "PcImageSquareAdPreview" });

const props = defineProps<{
    module: Record<string, unknown>;
}>();

const moduleConfig = computed<PcImageSquareAdModule>(() => {
    return deepMerge(pcImageSquareAdDefaults, props.module as Partial<PcImageSquareAdModule>);
});

const picList = computed(() => moduleConfig.value.picList || []);

const moduleClasses = computed(() => {
    const classes: string[] = [];
    const data = moduleConfig.value;
    classes.push(`dec-image-cube--layout-${data.picType ?? 1}`);
    return classes;
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
        "--dec-inner-padding": `${frame?.leftAndRightPadding ?? frame?.innerPadding ?? 0}px`,
        "--dec-item-gap": `${data.imgPadding ?? 0}px`
    };
});

function formatImage(url?: string): string {
    if (!url) {
        return 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" width="100" height="100"><rect fill="%23f0f0f0" width="100" height="100"/></svg>';
    }
    return url;
}
</script>

<style lang="less" src="./preview.less" />
