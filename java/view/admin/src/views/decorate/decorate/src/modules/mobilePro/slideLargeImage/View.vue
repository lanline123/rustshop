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
            <div class="slidelarge-image" ref="slidelargeRef">
                <div :style="titleStyle.titleBox" v-if="module.moduleTitle && module.moduleTitle.titlePosition === 'top' && module.moduleTitle?.titleText">
                    <div :style="titleStyle.title">{{ module.moduleTitle?.titleText }}</div>
                    <div :style="titleStyle.subTitle">{{ module.moduleTitle?.subTitleText }}</div>
                </div>
                <div :style="`margin-top: ${module.picMarginTop}%; width: 100%; z-index: 1; overflow: hidden;`">
                    <div :style="slidelargeBoxStyle">
                        <div :class="[{ 'slidelarge-image-box': module.slidingEffect === 'auto' }]" :style="slidelargeBoxStyle + slidelargeAnimationStyle">
                            <template v-for="(item, index) in list" :key="index">
                                <div :style="`width: ${itemWidth}px; flex-shrink: 0`">
                                    <div :style="itemStyle">
                                        <div :style="imgStyle + `background-image: url(${imageFormat(item.picUrl)});`"></div>
                                        <div
                                            :style="
                                                textStyle.picTextBox +
                                                `${
                                                    module.picTitleDesc.fontPosition === 'internal'
                                                        ? 'position: absolute;left: 0px;right: 0px;bottom: 0px;padding: 10px 15px;'
                                                        : ''
                                                }` +
                                                `${module.picTitleDesc.fontPosition !== 'internal' ? `margin: ${module.picTitleDesc.marginTop}px ${module.picTitleDesc.marginLeft}px ${module.picTitleDesc.marginBottom}px;` : ''}`
                                            "
                                            v-if="module.picTitleDesc?.show && (item.picTitle || item.picDesc)"
                                        >
                                            <div :style="textStyle.picTextTitle" v-if="item.picTitle">{{ item.picTitle }}</div>
                                            <div :style="textStyle.picTextDesc" v-if="item.picDesc">{{ item.picDesc }}</div>
                                        </div>
                                    </div>
                                </div>
                            </template>
                        </div>
                    </div>
                </div>
                <div :style="titleStyle.titleBox" v-if="module.moduleTitle && module.moduleTitle.titlePosition === 'bottom'">
                    <div :style="titleStyle.title">{{ module.moduleTitle?.titleText }}</div>
                    <div :style="titleStyle.subTitle">{{ module.moduleTitle?.subTitleText }}</div>
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref, computed, Ref, onMounted, watchEffect } from "vue";
import { ModuleType, ModuleSlidingLargeImageType } from "@/types/decorate/decorate.d";
import { mergeDefaultModule, defaultContentStyle, defaultModuleStyle, formatCommonStyle, copyArray } from "@/views/decorate/decorate/src/modules/";
import { imageFormat } from "@/utils/format";
const module = defineModel<ModuleType & ModuleSlidingLargeImageType>("module") as Ref<ModuleType & ModuleSlidingLargeImageType>;
const picList = [
    {
        picId: 1424,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1731388896OTbuADBoe1LEFefwJo.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/1731388896OTbuADBoe1LEFefwJo.png",
        picName: "FtohhFYy2fnPEqrcsqpSq5H53DzH"
    },
    {
        picId: 1425,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/17313888965IIgWQGqsTYhoe7H7T.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/17313888965IIgWQGqsTYhoe7H7T.png",
        picName: "FlVOfGJJyjhQmsiDGJViz_S2clyl"
    },
    {
        picId: 1424,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1731388896OTbuADBoe1LEFefwJo.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/1731388896OTbuADBoe1LEFefwJo.png",
        picName: "FtohhFYy2fnPEqrcsqpSq5H53DzH"
    },
    {
        picId: 1425,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/17313888965IIgWQGqsTYhoe7H7T.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/17313888965IIgWQGqsTYhoe7H7T.png",
        picName: "FlVOfGJJyjhQmsiDGJViz_S2clyl"
    }
];
const defaultModule = ref({
    picList: picList, // 图片列表
    rowNum: 1.5, //行数
    slidingEffect: "auto", // 滚动效果 auto(自动)/manual(手动)
    slidingSpeed: 25, // 滚动速度
    picMarginTop: 0, // 图片上间距
    picMargin: 10, // 图片间距
    picRadius: 6, // 图片圆角
    picFillType: "cover", // 图片填充类型 cover:填充, contain:周边留白
    picTitleDesc: {
        //标题描述文字
        show: 1, //是否显示 1:显示 0:不显示
        titleColor: "#ffffff", //标题颜色
        titleFontSize: 14, //标题字体大小
        titleFontWeight: 400, //标题字体粗细
        descColor: "#ffffff", //描述颜色
        descFontSize: 12, //描述字体大小
        descFontWeight: 0, //描述字体粗细
        fontPosition: "", //字体位置 internal:内部 external:外部
        textAlignment: "right", //文字对齐方式 left:左对齐 center:居中 right:右对齐
        marginTop: 10, //文字上间距
        marginBottom: 10, //文字下间距
        marginLeft: 0, //文字侧边间距
        backgroundColor: "" //内容背景颜色
    },
    moduleTitle: {
        //标题设置
        titleText: "", //大标题文字
        subTitleText: "", //小标题文字
        titlePosition: "top", //标题位置 top:居上 bottom:居下
        textAlignment: "right", //文字对齐方式 left:左对齐 center:居中 right:右对齐
        titleMarginTop: 0, //标题上间距
        titleMarginBottom: 0, //标题下间距
        titleMarginLeft: 0, //标题侧边距离
        titleColor: "#ffffff", //大标题颜色
        titleFontSize: 16, //大标题字体大小
        titleFontWeight: 700, //大标题字体粗细
        subFontTitleColor: "#ffffff", //小标题颜色
        subFontTitleSize: 12, //小标题字体大小
        subFontTitleWeight: 700 //小标题字体粗细
    },
    moduleStyle: defaultModuleStyle, // 模块样式
    contentStyle: defaultContentStyle // 内容样式
});
mergeDefaultModule(module.value, defaultModule.value);
const commonStyle = computed(() => {
    return formatCommonStyle(module.value.moduleStyle ?? {}, module.value.contentStyle ?? {}, module.value.subTitle ?? {});
});
const list = computed(() => {
    if (module.value.picList) {
        return copyArray(module.value.picList, 5);
    }
    return [];
});
const slidelargeBoxStyle = computed(() => {
    return `width: ${itemWidth.value * module.value.picList?.length}px; display: flex; animation-duration: ${module.value.slidingSpeed}s; position: relative; top: 0px; left: 0px`;
});
const slidelargeAnimationStyle = computed(() => {
    return `transform: translateX(${0}px);animation-play-state:  ${module.value.slidingEffect === "manual" ? "paused" : "running"};`;
});
const itemStyle = computed(() => {
    return `margin-right: ${module.value.picMargin}px; background: none; border-radius: ${module.value.picRadius}px; position: relative; overflow: hidden`;
});
const imgStyle = computed(() => {
    return `width: 100%;height: 0px;padding-top: ${imgratio.value * 100}%;border-bottom-left-radius: 0px;border-bottom-right-radius: 0px;background-size: cover;background-repeat: no-repeat;background-position: center center;`;
});

const textStyle = computed(() => {
    return {
        picTextBox: `background: ${module.value.picTitleDesc?.backgroundColor ? module.value.picTitleDesc.backgroundColor : "none"};text-align: ${module.value.picTitleDesc?.textAlignment};`,
        picTextTitle: `color: ${module.value.picTitleDesc?.titleColor}; font-size: ${module.value.picTitleDesc?.titleFontSize}px; font-weight: ${module.value.picTitleDesc?.titleFontWeight}; overflow: hidden`,
        picTextDesc: `margin-top: 6px; font-size: ${module.value.picTitleDesc?.descFontSize}px;font-weight: ${module.value.picTitleDesc?.descFontWeight};color: ${module.value.picTitleDesc?.descColor};overflow: hidden;`
    };
});

const titleStyle = computed(() => {
    return {
        titleBox: `text-align: ${module.value.moduleTitle?.textAlignment}; margin: ${module.value.moduleTitle?.titleMarginTop}px ${module.value.moduleTitle?.titleMarginLeft}px ${module.value.moduleTitle?.titleMarginBottom}px;`,
        title: `font-size: ${module.value.moduleTitle?.titleFontSize}px; font-weight: ${module.value.moduleTitle?.titleFontWeight};color: ${module.value.moduleTitle?.titleColor};`,
        subTitle: `font-size: ${module.value.moduleTitle?.subFontTitleSize}px; margin-top: 8px; font-weight: ${module.value.moduleTitle?.subFontTitleWeight};color: ${module.value.moduleTitle?.subFontTitleColor};`
    };
});

const imgratio = ref(0);
const getImgInfo = (url: any) => {
    return new Promise((resolve, reject) => {
        const img = new Image();
        img.src = imageFormat(url);
        img.onload = () => {
            resolve({
                width: img.width,
                height: img.height
            });
        };
        img.onerror = () => {
            reject();
        };
    });
};
const getImgratio = async (url: any) => {
    try {
        const res: any = await getImgInfo(url);
        imgratio.value = Number((res.height / res.width).toFixed(3));
    } catch (error) {
        console.error(error);
    }
};
const slidelargeRef = ref();
const boxWidth = ref(0);
const itemWidth = computed(() => {
    return boxWidth.value / module.value.rowNum;
});
onMounted(async () => {
    await getImgratio(module.value.picList[0].picUrl);
    // console.log("imgratio", imgratio.value);
    boxWidth.value = slidelargeRef.value.clientWidth;
});

watchEffect(() => {
    getImgratio(module.value.picList[0].picUrl);
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
.slidelarge-image {
    width: 100%;
    overflow: hidden;
}
@keyframes left {
    0% {
        left: -200%;
    }
    100% {
        left: -300%;
    }
}
.slidelarge-image-box {
    animation: left 5000ms linear infinite running;
}
</style>
