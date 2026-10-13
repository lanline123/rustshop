<template>
    <div
        class="module-ad-con module-category_rec_a1"
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
                commonStyle.moduleContentStyle.boxRadius +
                commonStyle.moduleContentStyle.boxShadow +
                commonStyle.moduleContentStyle.innerPadding
            "
        >
            <div v-if="!module.picList || module.picList.length === 0" class="module-ad-empty empty-image_ad">
                <div class="image-empty-bg"></div>
                <div class="desc">点击添加分类</div>
            </div>

            <div v-else class="category-rec-a1-list" :style="`padding-left:${itemGap}px;`">
                <div v-for="(pic, index) in module.picList" :key="index" class="category-rec-a1-item" :style="itemStyle">
                    <div class="item-inner" :style="`border-radius:${module.picRadius ?? 0}px;`">
                        <div
                            class="item-bg"
                            :style="`background-image:url(${imageFormat(pic.picUrl)});background-size:${module.picFillType || 'cover'};`"
                        ></div>
                        <div class="item-content">
                            <span
                                v-if="module.subTitle?.showTitle"
                                class="item-title"
                                :style="commonStyle.moduleSubTitle.textColor + commonStyle.moduleSubTitle.textFontSize + commonStyle.moduleSubTitle.textBlod"
                                >{{ pic.picTitle || "标题" }}</span
                            >
                            <span class="item-desc" :style="`color:${module.subTitleColor || '#999999'};`">{{ pic.picDesc || "描述" }}</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { computed } from "vue";
import { imageFormat } from "@/utils/format";
import { mergeDefaultModule, formatCommonStyle } from "@/views/decorate/decorate/src/modules/index";
import { categoryRecA1Defaults } from "./index";
import type { CategoryRecA1Module } from "./types";

const props = defineProps<{ module: CategoryRecA1Module }>();
const module = props.module;
// 旧数据缺字段时烧录默认值（原地写回 store 模块对象，与旧 View.vue 行为一致，契约）
mergeDefaultModule(module, categoryRecA1Defaults);

const commonStyle = computed(() =>
    formatCommonStyle((module.moduleStyle ?? {}) as any, (module.contentStyle ?? {}) as any, (module.subTitle ?? {}) as any)
);

const itemGap = 4;

const itemStyle = computed(() => {
    return {
        flexBasis: "25%",
        paddingRight: `${itemGap}px`
    };
});
</script>

<style lang="less" scoped>
.module-category_rec_a1 {
    .module-ad-content {
        position: relative;
    }

    .category-rec-a1-list {
        display: flex;
        flex-wrap: nowrap;
    }

    .category-rec-a1-item {
        min-width: 0;
        box-sizing: border-box;
    }

    .item-inner {
        position: relative;
        overflow: hidden;
    }

    .item-bg {
        position: absolute;
        top: 0;
        right: 0;
        bottom: 0;
        left: 0;
        background-position: center;
        background-repeat: no-repeat;
        background-size: contain;
    }

    .item-content {
        position: relative;
        z-index: 1;
        display: flex;
        flex-direction: column;
        padding: 10px;
    }

    .item-title {
        display: block;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        font-size: 12px;
        font-weight: 700;
        color: #333;
        height: 20px;
        line-height: 20px;
    }

    .item-desc {
        display: block;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        font-size: 11px;
        color: #666;
        height: 16px;
        line-height: 16px;
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
