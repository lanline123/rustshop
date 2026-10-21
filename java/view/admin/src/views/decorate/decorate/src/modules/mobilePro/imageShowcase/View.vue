<template>
    <div
        class="module-ad-con module-image_showcase"
        :style="
            commonStyle.moduleStyle.boxPaddingTop +
            commonStyle.moduleStyle.boxPaddingBottom +
            commonStyle.moduleStyle.boxPadding +
            commonStyle.moduleStyle.backgroundColor +
            commonStyle.moduleStyle.backgroundImage +
            commonStyle.moduleStyle.backgroundSize
        "
    >
        <div
            class="module-ad-content"
            :style="
                commonStyle.moduleContentStyle.backgroundImage +
                commonStyle.moduleContentStyle.backgroundSize +
                commonStyle.moduleContentStyle.boxRadius2 +
                commonStyle.moduleContentStyle.boxShadow +
                commonStyle.moduleContentStyle.innerPadding +
                commonStyle.moduleContentStyle.backgroundColor
            "
        >
            <div v-if="!module.picList || module.picList.length === 0" class="module-ad-empty empty-image_ad">
                <div class="image-empty-bg"></div>
                <div class="desc">{{ $t('点击添加图片') }}</div>
            </div>

            <div v-else :class="['image-showcase-list', renderListStyle === 'scroll' ? 'is-scroll' : 'is-fixed']">
                <div
                    v-for="(pic, key) in module.picList"
                    :key="key"
                    class="image-showcase-item"
                    :style="itemStyle"
                >
                    <div class="item-image-box" :style="imageBoxStyle">
                        <img class="item-image" :src="imageFormat(pic.picUrl)" :style="imageStyle" />
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { computed, type Ref } from "vue";
import { imageFormat } from "@/utils/format";
import { ModuleType, ImageShowcaseType } from "@/types/decorate/decorate.d";
import {
    mergeDefaultModule,
    defaultImageShowcase,
    imageShowcasePaddingMap,
    formatCommonStyle
} from "@/views/decorate/decorate/src/modules/";

const module = defineModel<ModuleType & ImageShowcaseType>("module") as Ref<ModuleType & ImageShowcaseType>;

mergeDefaultModule(module.value, defaultImageShowcase);

const commonStyle = computed(() =>
    formatCommonStyle((module.value.moduleStyle ?? {}) as any, (module.value.contentStyle ?? {}) as any)
);

const renderListStyle = computed(() => {
    if (module.value.listStyle !== "scroll") return "fixed";
    return (module.value.picList?.length ?? 0) > (module.value.rowNum ?? 1) ? "scroll" : "fixed";
});

const itemStyle = computed(() => {
    const rowNum = module.value.rowNum ?? 4;
    const gap = module.value.imgPadding ?? 0;
    if (renderListStyle.value === "scroll") {
        return {
            flex: `0 0 calc((100% - ${(rowNum - 1) * gap}px) / ${rowNum})`,
            marginRight: `${gap}px`
        } as Record<string, string>;
    }
    return {};
});

// 使用 padding-top hack 锁定比例（兼容微信小程序，不依赖 aspect-ratio）
// 容器 padding-top: X%（基于父宽度），子图绝对定位填充
const imageBoxStyle = computed(() => {
    const radius = `${module.value.picRadius ?? 0}px`;
    const padding = imageShowcasePaddingMap[module.value.picRatio ?? "default"] || "";
    const shadow = module.value.boxShadow === 1 ? "0 2px 8px rgba(0, 0, 0, 0.06)" : "none";
    const style: Record<string, string> = {
        position: "relative",
        borderRadius: radius,
        boxShadow: shadow,
        overflow: "hidden"
    };
    if (padding) {
        style.paddingTop = padding;
        style.height = "0";
    }
    return style;
});

const imageStyle = computed(() => {
    if ((module.value.picRatio ?? "default") === "default") {
        return { width: "100%", height: "auto", display: "block" } as Record<string, string>;
    }
    // 比例锁定模式：图片绝对定位填充父容器
    return {
        position: "absolute",
        top: "0",
        left: "0",
        width: "100%",
        height: "100%",
        display: "block",
        objectFit: module.value.picFillType ?? "cover"
    } as Record<string, string>;
});

const gridColumns = computed(() => `${module.value.rowNum ?? 4}`);
const gridGap = computed(() => `${module.value.imgPadding ?? 0}px`);
</script>

<style lang="less" scoped>
.module-image_showcase {
    .module-ad-content {
        position: relative;
    }

    .image-showcase-list {
        &.is-fixed {
            display: grid;
            grid-template-columns: repeat(v-bind("gridColumns"), 1fr);
            gap: v-bind("gridGap");
        }

        &.is-scroll {
            display: flex;
            overflow-x: auto;
            overflow-y: hidden;
            scrollbar-width: none;
            -webkit-overflow-scrolling: touch;
            &::-webkit-scrollbar {
                display: none;
            }
        }
    }

    .image-showcase-item {
        min-width: 0;
        box-sizing: border-box;
    }

    .item-image-box {
        position: relative;
        width: 100%;
        background-color: #f7f8fa;
    }

    .item-image {
        display: block;
        width: 100%;
    }

    .module-ad-empty {
        padding-top: 30px;
        padding-bottom: 50px;
        text-align: center;
        background-color: #f2f4f6;

        .image-empty-bg {
            background: url("@/views/decorate/decorate/src/img/empty-img-bg.png") no-repeat center center;
            background-size: 100% auto;
            width: 100px;
            margin: 0 auto 10px;
            height: 100px;
        }

        .desc {
            text-align: center;
            color: #888;
            font-size: 14px;
        }
    }
}
</style>
