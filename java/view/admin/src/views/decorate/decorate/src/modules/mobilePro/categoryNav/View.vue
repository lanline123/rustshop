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
                commonStyle.moduleContentStyle.innerPadding
            "
        >
            <div :class="'category-nav decorate-slide-box ' + module.listStyleType">
                <template v-for="(item, index) in module.categoryList" :key="index">
                    <div class="category-nav-item" :style="AnimationFormat + transitionDuration(index) + itemStyle">
                        <div class="item-box" :style="`padding:${module.categoryPadding}px;`">
                            <div class="category-nav-item-img" :style="`background-image:url(${imageFormat(item.picUrl)});` + imgStyle"></div>
                            <p
                                v-if="module.subTitle?.showTitle"
                                class="category-nav-item-text"
                                :style="commonStyle.moduleSubTitle.textBlod + commonStyle.moduleSubTitle.textColor + commonStyle.moduleSubTitle.textFontSize"
                            >
                                {{ item.picTitle }}
                            </p>
                        </div>
                    </div>
                </template>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref, computed, Ref } from "vue";
import { ModuleType, ModuleCategoryType } from "@/types/decorate/decorate.d";
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
        picId: 1377,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1730957947YV2lncqLSOSg8OvpbK.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/1730957947YV2lncqLSOSg8OvpbK.png",
        picName: "dangrisuda",
        picTitle: t('当日速达')
    },
    {
        picId: 1384,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1730957948LcXRbvCuwlCHyHy7Y0.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/1730957948LcXRbvCuwlCHyHy7Y0.png",
        picName: "xiuxianlingshi",
        picTitle: t('休闲零食')
    },
    {
        picId: 1378,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1730957947XV6cPNonZJurGcDfOP.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/1730957947XV6cPNonZJurGcDfOP.png",
        picName: "mimianliangyou",
        picTitle: t('粮油米面')
    },
    {
        picId: 1385,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/17309579481I5gopEn7PkOneF0NU.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/17309579481I5gopEn7PkOneF0NU.png",
        picName: "ruyinjiushui",
        picTitle: t('乳饮酒水')
    },
    {
        picId: 1382,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1730957948Ouz6wPq8cQkgSjDCkF.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/1730957948Ouz6wPq8cQkgSjDCkF.png",
        picName: "riyongbaihuo",
        picTitle: t('日用百货')
    },
    {
        picId: 1380,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1730957947iOLEngIwvzxZDjhtQ6.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/1730957947iOLEngIwvzxZDjhtQ6.png",
        picName: "muyingyongpin",
        picTitle: t('母婴用品')
    },
    {
        picId: 1379,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1730957947lWqY7UySCwQ89qsq7f.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/1730957947lWqY7UySCwQ89qsq7f.png",
        picName: "gerenxihu",
        picTitle: t('个人洗护')
    },
    {
        picId: 1383,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1730957948vFCfxjt0TIROPOocWI.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/1730957948vFCfxjt0TIROPOocWI.png",
        picName: "zhipinjiaqing",
        picTitle: t('纸品家清')
    },
    {
        picId: 1376,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1730957947BswaPOfoOZnn8tDSX6.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/1730957947BswaPOfoOZnn8tDSX6.png",
        picName: "meironghufu",
        picTitle: t('护肤美容')
    },
    {
        picId: 1381,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1730957947vIKQibO98m2q9oP8ct.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/1730957947vIKQibO98m2q9oP8ct.png",
        picName: "fangbiansushi",
        picTitle: t('方便速食')
    }
];
const module = defineModel<ModuleType & ModuleCategoryType>("module") as Ref<ModuleType & ModuleCategoryType>;
const defaultModule = ref({
    listStyleType: "list", // 列表样式 list:固定, slide:横向滑动
    categoryList: categoryList, //分类列表
    rowNum: 5, //行数
    animation: defaultAnimation, //动画
    picFillType: "cover", // 图片填充类型 cover:填充, contain:周边留白
    picPercent: 90, // 图片百分比
    radius: 200, // 圆角
    categoryPadding: 10, // 分类间距
    subTitle: defaultSubTitle, //分类标题文字
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
const itemStyle = computed(() => {
    let str = `flex-grow:0;flex-basis:${module.value.rowNum ? 100 / module.value.rowNum : 25}%;`;
    if (module.value.listStyleType === "slide") {
        str += `flex-shrink: 0;`;
    }
    return str;
});
const imgStyle = computed(() => {
    return `padding-top:${module.value.picPercent}%;width:${module.value.picPercent}%;border-radius:${module.value.radius}px;background-size:${module.value.picFillType};`;
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
.category-nav-item {
    overflow: hidden;
}
.item-box {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
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
    white-space: nowrap;
    text-align: center;
    height: 20px;
    line-height: 20px;
    margin-top: 3px;
    font-size: 14px;
    width: 100%;
}
</style>
