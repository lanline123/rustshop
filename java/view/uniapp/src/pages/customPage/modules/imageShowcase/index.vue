<template>
    <view
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
        <view
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
            <template v-if="module.picList && module.picList.length > 0">
                <view :class="['image-showcase-list', renderListStyle === 'scroll' ? 'is-scroll' : 'is-fixed']">
                    <view
                        v-for="(pic, idx) in module.picList"
                        :key="idx"
                        class="image-showcase-item"
                        :style="getItemStyle(idx)"
                        @click="urlFormat(pic.picLink) && redirect({ url: urlFormat(pic.picLink) })"
                    >
                        <view class="item-image-box" :style="imageBoxStyle">
                            <image v-if="picRatio === 'default'" class="item-image" mode="widthFix" :src="imageFormat(pic.picUrl)" />
                            <image v-else class="item-image item-image-ratio" :mode="imageMode" :src="imageFormat(pic.picUrl)" />
                        </view>
                    </view>
                </view>
            </template>
        </view>
    </view>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { formatCommonStyle, imageShowcasePaddingMap } from "@/components/modules";
import { imageFormat, urlFormat } from "@/utils/format";
import { redirect } from "@/utils";

const props = defineProps({
    module: {
        type: Object,
        default: () => ({})
    }
});

const commonStyle = computed(() => formatCommonStyle(props.module.moduleStyle ?? {}, props.module.contentStyle ?? {}));

// Fallback defaults aligned with admin's defaultImageShowcase
// (Tigshop-Admin/src/views/decorate/decorate/src/modules/index.ts).
// Admin's mergeDefaultModule normally fills these on save, but mobile keeps
// the same fallback so legacy / partial payloads render identically to canvas.
const rowNum = computed(() => Number(props.module.rowNum) || 4);
const gap = computed(() => {
    const v = props.module.imgPadding;
    return v === undefined || v === null ? 4 : Number(v) || 0;
});
const picRatio = computed(() => props.module.picRatio ?? "default");
const picFillType = computed(() => props.module.picFillType ?? "cover");
const picRadius = computed(() => {
    const v = props.module.picRadius;
    return v === undefined || v === null ? 8 : Number(v) || 0;
});
const boxShadow = computed(() => {
    const v = props.module.boxShadow;
    return v === undefined || v === null ? true : v === 1;
});

const renderListStyle = computed(() => {
    if (props.module.listStyle !== "scroll") return "fixed";
    return (props.module.picList?.length ?? 0) > rowNum.value ? "scroll" : "fixed";
});

// idx 在严格 TS 下从 v-for 推断为 number | string；统一在内部转 number
const getItemStyle = (idx: number | string): string => {
    const i = typeof idx === "number" ? idx : Number(idx);
    const n = rowNum.value;
    const g = gap.value;
    const basis = `calc((100% - ${(n - 1) * g}px) / ${n})`;
    let style = `flex: 0 0 ${basis};`;
    const total = props.module.picList?.length ?? 0;
    if (renderListStyle.value === "scroll") {
        const isLast = i === total - 1;
        if (!isLast && g) style += `margin-right:${g}px;`;
    } else {
        const lastCol = (i + 1) % n === 0;
        const lastRow = Math.ceil((i + 1) / n) === Math.ceil(total / n);
        if (!lastCol && g) style += `margin-right:${g}px;`;
        if (!lastRow && g) style += `margin-bottom:${g}px;`;
    }
    return style;
};

const imageBoxStyle = computed(() => {
    const radius = `${picRadius.value}px`;
    const padTop = imageShowcasePaddingMap[picRatio.value] || "";
    const shadow = boxShadow.value ? "0 2px 8px rgba(0, 0, 0, 0.06)" : "none";
    let style = `position:relative;border-radius:${radius};box-shadow:${shadow};overflow:hidden;`;
    if (padTop) style += `padding-top:${padTop};height:0;`;
    return style;
});

const imageMode = computed(() => (picFillType.value === "contain" ? "aspectFit" : "aspectFill"));
</script>

<style lang="scss" scoped>
.module-image_showcase {
    .module-ad-content {
        position: relative;
    }
    .image-showcase-list {
        &.is-fixed {
            display: flex;
            flex-wrap: wrap;
            align-items: flex-start;
        }
        &.is-scroll {
            display: flex;
            flex-wrap: nowrap;
            overflow-x: auto;
            overflow-y: hidden;
            -webkit-overflow-scrolling: touch;
            &::-webkit-scrollbar {
                display: none;
                height: 0;
            }
        }
    }
    .image-showcase-item {
        min-width: 0;
        box-sizing: border-box;
    }
    .item-image-box {
        width: 100%;
        background-color: #f7f8fa;
    }
    .item-image {
        display: block;
        width: 100%;
    }
    .item-image-ratio {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
    }
}
</style>
