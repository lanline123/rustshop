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
                <div class="desc">{{ $t('点击添加分类') }}</div>
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
                                >{{ pic.picTitle || $t('标题') }}</span
                            >
                            <span class="item-desc" :style="`color:${module.subTitleColor || '#999999'};`">{{ pic.picDesc || $t('描述') }}</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { computed, type Ref } from "vue";
import { imageFormat } from "@/utils/format";
import { ModuleType, CategoryRecA1Type } from "@/types/decorate/decorate.d";
import { mergeDefaultModule, defaultSubTitle, defaultModuleStyle, defaultContentStyle, formatCommonStyle } from "@/views/decorate/decorate/src/modules/";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const module = defineModel<ModuleType & CategoryRecA1Type>("module") as Ref<ModuleType & CategoryRecA1Type>;

const defaultPicList = [
    {
        picUrl: "https://oss.tigshop.com/official/img/pc/202605/1779325953rpmWhZWipxQ7nmuAQj.png",
        picThumb: "https://oss.tigshop.com/official/img/pc/202605/1779325953rpmWhZWipxQ7nmuAQj.png",
        picTitle: t('领取优惠'),
        picDesc: t('优惠券'),
        picLink: { link: "", title: "" }
    },
    {
        picUrl: "https://oss.tigshop.com/official/img/pc/202605/1779325953xOBpJxh9kzrNjJp3rB.png",
        picThumb: "https://oss.tigshop.com/official/img/pc/202605/1779325953xOBpJxh9kzrNjJp3rB.png",
        picTitle: t('爆款水果'),
        picDesc: t('立即探鲜'),
        picLink: { link: "", title: "" }
    },
    {
        picUrl: "https://oss.tigshop.com/official/img/pc/202605/1779325953xOBpJxh9kzrNjJp3rB.png",
        picThumb: "https://oss.tigshop.com/official/img/pc/202605/1779325953xOBpJxh9kzrNjJp3rB.png",
        picTitle: t('在家火锅'),
        picDesc: t('温暖冬季'),
        picLink: { link: "", title: "" }
    },
    {
        picUrl: "https://oss.tigshop.com/official/img/pc/202605/1779325953xOBpJxh9kzrNjJp3rB.png",
        picThumb: "https://oss.tigshop.com/official/img/pc/202605/1779325953xOBpJxh9kzrNjJp3rB.png",
        picTitle: t('精致生活'),
        picDesc: t('居家好货'),
        picLink: { link: "", title: "" }
    }
];

const defaultModule = {
    picList: defaultPicList,
    picFillType: "contain",
    picRadius: 8,
    subTitle: {
        ...defaultSubTitle,
        titleFontSize: 14,
        titleFontBold: 700
    },
    subTitleColor: "#999999",
    moduleStyle: {
        ...defaultModuleStyle,
        boxPaddingTop: 0,
        boxPaddingBottom: 0,
        boxPadding: 6
    },
    contentStyle: defaultContentStyle
};

mergeDefaultModule(module.value, defaultModule);

const commonStyle = computed(() =>
    formatCommonStyle((module.value.moduleStyle ?? {}) as any, (module.value.contentStyle ?? {}) as any, (module.value.subTitle ?? {}) as any)
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
