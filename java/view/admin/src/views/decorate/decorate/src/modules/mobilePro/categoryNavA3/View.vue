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
                <div :class="' decorate-slide-box ' + module.listStyleType" :style="flexWidth + slideBoxStyle">
                    <template v-for="(item, index) in module.categoryList" :key="index">
                        <div class="category-nav-item" :style="itemStyle">
                            <div class="item-box" :style="`padding-left:${module.categoryPadding}px;`">
                                <div class="img-box" :style="imgBoxStyle">
                                    <div class="img-box-bg" :style="{ backgroundColor: module.imageBackgroundColor }"></div>
                                    <div class="category-nav-item-img" :style="`background-image:url(${imageFormat(item.picUrl)});` + imgStyle"></div>
                                </div>
                                <p
                                    v-if="module.subTitle?.showTitle"
                                    class="category-nav-item-text"
                                    :style="
                                        commonStyle.moduleSubTitle.textBlod + commonStyle.moduleSubTitle.textColor + commonStyle.moduleSubTitle.textFontSize
                                    "
                                >
                                    {{ item.picTitle }}
                                </p>
                                <div class="category-nav-item-desc">
                                    <template v-if="module.logoPic?.picUrl">
                                        <img class="desc-icon" :src="imageFormat(module.logoPic?.picUrl)" />
                                    </template>

                                    <div class="desc-text">{{ item.picDesc }}</div>
                                </div>
                            </div>
                        </div>
                    </template>
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref, computed, Ref, onMounted, watchEffect } from "vue";
import { ModuleType, ModuleCategoryA1Type } from "@/types/decorate/decorate.d";
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
        picId: 1519,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1732608267mJFZA5uFuT5974JXl8.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/1732608267mJFZA5uFuT5974JXl8.png",
        picName: "FgisG78Lwl4UG3y5fBiEUnLrxIV",
        picTitle: t('诚信经营'),
        picDesc: t('诚信经营')
    },
    {
        picId: 1522,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1732608267sJb7DHOuVX8896buhT.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/1732608267sJb7DHOuVX8896buhT.png",
        picName: "FpnzuzjBafleMMwgzc5MvNITxVHj",
        picTitle: t('品质保障'),
        picDesc: t('诚信经营')
    },
    {
        picId: 1521,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1732608267yXWHECJxn75eqyk60Q.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/1732608267yXWHECJxn75eqyk60Q.png",
        picName: "Fh56a7Prcs9wghXkxYtAHGlF3_uT1",
        picTitle: t('贴心服务'),
        picDesc: t('诚信经营')
    },
    {
        picId: 1520,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/17326082679hhyPXOj9i02unfUgw.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/17326082679hhyPXOj9i02unfUgw.png",
        picName: "Fn5u9A8vmu2DAG404sp8z_016zdK",
        picTitle: t('购物无忧'),
        picDesc: t('诚信经营')
    }
];
const logo = {
    picUrl: "http://oss.tigshop.com/img/gallery/202409/1727056331nhxqtK8Mm5u8MXokoI.png",
    picThumb: "https://oss.tigshop.com/img/gallery/202409/1727056331nhxqtK8Mm5u8MXokoI.png?x-oss-process=image/resize,m_pad,h_200,h_200",
    picId: 1221,
    picName: "logoWhite"
};
const module = defineModel<ModuleType & ModuleCategoryA1Type>("module") as Ref<ModuleType & ModuleCategoryA1Type>;
const defaultModule = ref({
    logoPic: logo,
    categoryList: categoryList, //分类列表
    listStyleType: "list", // 列表样式 list:固定, slide:横向滑动
    rowNum: 4, //每行显示数量
    imageBackgroundColor: "", // 图片背景色
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

const imgBoxStyle = computed(() => {
    return `padding-top:${module.value.picPercent}%;width:${module.value.picPercent}%;border-radius:${module.value.radius}px;`;
});

const imgStyle = computed(() => {
    return `border-radius:${module.value.radius}px;background-size:${module.value.picFillType};`;
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

const flexWidth = computed(() => {
    switch (module.value.listStyleType) {
        case "slide2":
            return `width: ${itemWidth.value * Math.ceil(module.value.categoryList.length / 2)}px;`;
        case "slide":
            return `width: ${itemWidth.value * module.value.categoryList.length}px;`;
        default:
            return `width: auto;`;
    }
});

const slideBoxStyle = computed(() => {
    return `transform: translate(${-module.value.categoryPadding}px, 0px);`;
});

const itemBoxRef = ref();

onMounted(() => {
    boxWidth.value = itemBoxRef.value.clientWidth;
});
watchEffect(() => {
    if (module.value.moduleStyle.boxMargin > 0 || module.value.contentStyle.paddingLeftRight > 0) {
        boxWidth.value = itemBoxRef.value?.clientWidth;
    }
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

    .img-box {
        position: relative;
        width: 100%;
        height: 0;
        overflow: hidden;
        margin: 0 auto;

        .img-box-bg {
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
        }
    }
}

.category-nav-item-img {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-repeat: no-repeat;
    background-position: center;
}
.category-nav-item-text {
    display: block;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    text-align: center;
    height: 20px;
    line-height: 20px;
    margin-top: 8px;
    font-size: 14px;
    width: 100%;
}

.category-nav-item-desc {
    display: flex;
    align-items: center;
    justify-content: center;
    color: #888b94;
    font-size: 12px;
    line-height: 16px;
    margin-top: 8px;
    width: 100%;

    .desc-text {
        font-size: 12px;
        color: #666;
    }

    .desc-icon {
        width: 16px;
        height: 16px;
        margin-right: 4px;
    }
}

.line {
    box-sizing: content-box;
}
</style>
