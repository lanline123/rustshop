<template>
    <div
        class="module-ad-con"
        :style="
            commonStyle.moduleStyle.boxPaddingBottom +
            commonStyle.moduleStyle.boxPaddingTop +
            commonStyle.moduleStyle.boxPaddingBottom +
            commonStyle.moduleStyle.boxPadding +
            commonStyle.moduleStyle.backgroundSize +
            commonStyle.moduleStyle.backgroundImage
        "
    >
        <div
            class="module-ad-content"
            :style="
                commonStyle.moduleContentStyle.backgroundImage +
                commonStyle.moduleContentStyle.backgroundSize +
                commonStyle.moduleContentStyle.boxRadius +
                commonStyle.moduleContentStyle.boxShadow +
                commonStyle.moduleContentStyle.innerPadding +
                commonStyle.moduleContentStyle.paddingBottom +
                commonStyle.moduleContentStyle.paddingTop +
                commonStyle.moduleContentStyle.paddingLeftRight
            "
        >
            <div class="category-nav" ref="itemBoxRef">
                <div :class="' decorate-slide-box ' + module.listStyleType" :style="flexWidth">
                    <template v-for="(item, index) in module.categoryList" :key="index">
                        <div class="category-nav-item" :style="AnimationFormat + transitionDuration(index) + itemStyle">
                            <div class="item-box" :style="`padding:${module.categoryPadding}px;`">
                                <div class="category-nav-item-img" :style="`background-image:url(${imageFormat(item.picUrl)});` + imgStyle"></div>
                                <p
                                    v-if="module.subTitle?.showTitle"
                                    class="category-nav-item-text"
                                    :style="
                                        commonStyle.moduleSubTitle.textBlod + commonStyle.moduleSubTitle.textColor + commonStyle.moduleSubTitle.textFontSize
                                    "
                                >
                                    {{ item.picTitle }}
                                </p>
                            </div>
                        </div>
                    </template>
                </div>
            </div>
            <template v-if="module.listStyleType !== 'list' && module.indicatorBar === 1">
                <div class="line-box">
                    <div :style="lineStyle.baseStyle" class="line">
                        <div :style="lineStyle.onStyle"></div>
                    </div>
                </div>
            </template>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref, computed, Ref, onMounted, watchEffect } from "vue";
import { ModuleType, ModuleCategoryA2Type, ModuleCategoryType } from "@/types/decorate/decorate.d";
import {
    mergeDefaultModule,
    defaultSubTitle,
    defaultModuleStyle,
    defaultContentStyle,
    defaultAnimation,
    formatCommonStyle,
    formatAnimation
} from "@/views/decorate/decorate/src/modules/";
import { imageFormat } from "@/utils/format";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const categoryList = [
    {
        picId: 1390,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1730958340xhqljwDakcNeJXCPna.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/1730958340xhqljwDakcNeJXCPna.png",
        picName: "FoAbizq7oeHiwJzoJVaBmgV50oRN",
        picTitle: t('折扣专区')
    },
    {
        picId: 1396,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1730958341AwZmIgqi2MgyFeZuUt.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/1730958341AwZmIgqi2MgyFeZuUt.png",
        picName: "Ft0N3qmFphVAkmv8Pn6QDp61Qw7",
        picTitle: t('运动旅行')
    },
    {
        picId: 1398,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1730958342pNWuK2IUhmpjICKmzC.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/1730958342pNWuK2IUhmpjICKmzC.png",
        picName: "FuiW1ysk28blbkbFpw6FHFJDKiOA",
        picTitle: t('个护护理')
    },
    {
        picId: 1393,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/17309583411C1elxz6qd8TAbdnTB.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/17309583411C1elxz6qd8TAbdnTB.png",
        picName: "Fra7mrH3EH2NejP7y3K9LrcoGEaH",
        picTitle: t('彩妆护肤')
    },
    {
        picId: 1392,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1730958341pqcAwTAnyf1I8qQtRN.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/1730958341pqcAwTAnyf1I8qQtRN.png",
        picName: "FrPMN2wDsjJoc6ydlCHge82oon6R",
        picTitle: t('母婴用品')
    },
    {
        picId: 1387,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1730958340219IHpDSXO5TlSVDNb.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/1730958340219IHpDSXO5TlSVDNb.png",
        picName: "FjF9irxfLxeOpMunPtgK2Gjoocb",
        picTitle: t('数码家电')
    },
    {
        picId: 1395,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1730958341ltUrGddGs3uvLccUTu.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/1730958341ltUrGddGs3uvLccUTu.png",
        picName: "Fr9nt9qxQxefziAVXZIGkQZx1goQ",
        picTitle: t('洗护用品')
    },
    {
        picId: 1399,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1730958342xoZNYWFWnp2ltRzpzO.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/1730958342xoZNYWFWnp2ltRzpzO.png",
        picName: "FulNvSiPYQdW_EOcqb851h3fuIrp",
        picTitle: t('整理收纳')
    },
    {
        picId: 1386,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1730958340uaWpBXrJkO4hghTti6.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/1730958340uaWpBXrJkO4hghTti6.png",
        picName: "FnbMGPun7yw92rulVAnOChQpfukU",
        picTitle: t('家居生活')
    },
    {
        picId: 1397,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1730958341CjpCfA4M71ErjOZuBA.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/1730958341CjpCfA4M71ErjOZuBA.png",
        picName: "Ftn_vGbQk_MQfqgB0ttsh0dWhj",
        picTitle: t('服装服饰')
    },
    {
        picId: 1394,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1730958341DTU3wlQ5Iv2Phj2RUd.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/1730958341DTU3wlQ5Iv2Phj2RUd.png",
        picName: "Fs4k1iKK6V1vSz1BeXlrHmMPCNwZ",
        picTitle: t('家居生活')
    },
    {
        picId: 1389,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1730958340zy5c1MTCtT41yA6Y7X.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/1730958340zy5c1MTCtT41yA6Y7X.png",
        picName: "FkmgdX63YoSx3nXZLroarmC5b",
        picTitle: t('美味零食')
    },
    {
        picId: 1391,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1730958340LIruk4KbAEdw3LgOA6.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/1730958340LIruk4KbAEdw3LgOA6.png",
        picName: "FpttgR5n4ZIHVXn4jf0_ZM9PpBl",
        picTitle: t('酒水饮料')
    },
    {
        picId: 1388,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1730958340Jh4hoAMEWqjuMWVSzf.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/1730958340Jh4hoAMEWqjuMWVSzf.png",
        picName: "Fmv5JqMd6qokW4bBjoSaBTWEaMt",
        picTitle: t('鲜花礼品')
    }
];
const module = defineModel<ModuleType & ModuleCategoryA2Type>("module") as Ref<ModuleType & ModuleCategoryType>;
const defaultModule = ref({
    listStyleType: "list", // 列表样式 list:固定, slide:单列滑动 slide2:双排滑动
    categoryList: categoryList, //分类列表
    rowNum: "custom", //行数 number | string
    rowPicPercent: 18, //图片占比宽度
    animation: defaultAnimation, //动画
    picFillType: "cover", // 图片填充类型 cover:填充, contain:周边留白
    picPercent: 90, // 图片百分比
    radius: 200, // 圆角
    categoryPadding: 10, // 分类间距
    subTitle: defaultSubTitle, //分类标题文字

    indicatorBar: 1, // 底部指示条
    barWidth: 30, // 底部指示条宽度
    barHeight: 2, // 底部指示条高度
    barWidthOn: 30, // 底部指示条选中高度
    barColor: "#eeeeee", // 底部指示条默认颜色
    barColorOn: "#ff6600", // 底部指示条选中颜色

    moduleStyle: defaultModuleStyle, // 模块样式
    contentStyle: defaultContentStyle // 内容样式
});
mergeDefaultModule(module.value, defaultModule.value);
const commonStyle = computed(() => {
    return formatCommonStyle(module.value.moduleStyle ?? {}, module.value.contentStyle ?? {}, module.value.subTitle ?? {});
});
const AnimationFormat = computed(() => {
    return `transform: translate(0px, 0px) scale(1) rotateY(0deg);opacity:1;`;
});
const transitionDuration = (index: number) => {
    return `transition-duration:0s;`;
};

const imgStyle = computed(() => {
    return `padding-top:${module.value.picPercent}%;width:${module.value.picPercent}%;border-radius:${module.value.radius}px;background-size:${module.value.picFillType};`;
});

const boxWidth = ref(0);
const itemWidth = computed(() => {
    let num = 0;
    if (module.value.rowNum === "custom") {
        num = Number((boxWidth.value * module.value.rowPicPercent * 0.01).toFixed(2));
    } else {
        num = Number((boxWidth.value / module.value.rowNum).toFixed(2));
    }
    return num;
});
const itemStyle = computed(() => {
    let str = `flex-shrink: 0;`;
    str += `width:${itemWidth.value}px;`;

    return str;
});

const lineStyle = computed(() => {
    return {
        baseStyle: `margin: 0 auto;width:${module.value.barWidth}%;height:${module.value.barHeight}px;border-radius: 4px;background: ${module.value.barColor};overflow: hidden;`,
        onStyle: `width:${module.value.barWidthOn}%;height:${module.value.barHeight}px;border-radius: 10px;background:${module.value.barColorOn};transition: 0.3s;position: relative;left: 0%;`
    };
});

const flexWidth = computed(() => {
    switch (module.value.listStyleType) {
        case "slide2":
            return `width: ${itemWidth.value * Math.ceil(module.value.categoryList.length / 2)}px`;
        case "slide":
            return `width: ${itemWidth.value * module.value.categoryList.length}px`;
        default:
            return `width: auto`;
    }
});
const itemBoxRef = ref();
onMounted(() => {
    // console.log("clientWidth", itemBoxRef.value.clientWidth);
    boxWidth.value = itemBoxRef.value.clientWidth;
});
watchEffect(() => {
    if (module.value.moduleStyle.boxMargin > 0 || module.value.contentStyle.paddingLeftRight > 0) {
        boxWidth.value = itemBoxRef.value?.clientWidth;
    }
    // if(module.value.contentStyle.padding > 0) {
    //     boxWidth.value = itemBoxRef.value?.clientWidth;
    // }
});
</script>
<style lang="less" scoped>
.decorate-slide-box::-webkit-scrollbar {
    display: none;
    height: 0;
}
.module-ad-con {
    background-size: cover;
    background-repeat: no-repeat;
    background-position: center top;
    position: relative;
}
.list {
    display: flex;
    flex-wrap: wrap;
}

.slide {
    display: flex;
    flex-wrap: nowrap;
    overflow-x: scroll;
}
.slide2 {
    display: flex;
    flex-wrap: wrap;
}
.category-nav {
    overflow: hidden;
}
.category-nav-item {
    overflow: hidden;
}
.item-box {
    display: block;
    text-align: center;
    overflow: hidden;
    width: 100%;
}
.category-nav-item-img {
    width: 100%;
    height: 0;
    background-repeat: no-repeat;
    background-position: center;
    margin: 0 auto;
}
.category-nav-item-text {
    display: block;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    text-align: center;
    height: 20px;
    line-height: 20px;
    margin-top: 3px;
    font-size: 14px;
    width: 100%;
}

.line-box {
    padding: 5px 0 10px;
    .line {
        box-sizing: content-box;
    }
}
</style>
