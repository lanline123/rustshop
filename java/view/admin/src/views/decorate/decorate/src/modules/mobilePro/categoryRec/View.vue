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
                (module.contentStyle?.paddingTop ? commonStyle.moduleContentStyle.paddingTop : '')
            "
        >
            <div class="categoryrec">
                <div class="item-box">
                    <template v-for="item in module.categoryList" :key="item">
                        <div class="li" :style="AnimationFormat + `border-radius: ${module.radius}px;background-color:${module.backgroundColor}`">
                            <div
                                class="item"
                                :style="
                                    `padding: ${module.categoryPadding}px 15px; border-radius: ${module.radius}px; background-color:${item.backgroundColor};` +
                                    borderStyle
                                "
                            >
                                <img class="img" :src="imageFormat(imageFormat(item.catPic?.picUrl))" alt="" />
                                <div
                                    class="tit"
                                    :style="`font-size: ${module.subTitle?.titleFontSize}px; font-weight: ${module.subTitle?.titleFontBold}; color: ${item.titleColor ? item.titleColor : module.subTitle?.titleColor}`"
                                >
                                    {{ item.title }}
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
import { ref, computed, Ref } from "vue";
import { ModuleType, ModuleCategoryRecType } from "@/types/decorate/decorate.d";
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
        title: t('小零食'),
        link: {
            link: {
                link: "",
                title: ""
            },
            linkTitle: ""
        },
        titleColor: "",
        backgroundColor: "",
        catPic: {
            picId: 1462,
            picThumb: "https://oss.tigshop.com/img/gallery/202411/1731896143iynI3ymaLzxGaRLk5h.jpg?x-oss-process=image/resize,m_pad,h_200,h_200",
            picUrl: "https://oss.tigshop.com/img/gallery/202411/1731896143iynI3ymaLzxGaRLk5h.jpg",
            picName: "FkVzGj1HD2aeDYmJ7E_VaFrIdr0H"
        }
    },
    {
        title: t('粮油米面'),
        link: {
            link: {
                link: "",
                title: ""
            },
            linkTitle: ""
        },
        titleColor: "",
        backgroundColor: "",
        catPic: {
            picId: 1465,
            picThumb: "https://oss.tigshop.com/img/gallery/202411/1731896143tV11a96dva46rixHbe.jpg?x-oss-process=image/resize,m_pad,h_200,h_200",
            picUrl: "https://oss.tigshop.com/img/gallery/202411/1731896143tV11a96dva46rixHbe.jpg",
            picName: "FmJJUiXFcIwl09Uo9QGJVuiDWhSt"
        }
    },
    {
        title: t('洗发水'),
        link: {
            link: {
                link: "",
                title: ""
            },
            linkTitle: ""
        },
        titleColor: "",
        backgroundColor: "",
        catPic: {
            picId: 1464,
            picThumb: "https://oss.tigshop.com/img/gallery/202411/1731896143OEttGMFtuKVqGX0JGF.jpg?x-oss-process=image/resize,m_pad,h_200,h_200",
            picUrl: "https://oss.tigshop.com/img/gallery/202411/1731896143OEttGMFtuKVqGX0JGF.jpg",
            picName: "FjJhor6dmekuWG2RdOJkpwDL2JOY"
        }
    },
    {
        title: t('肉干'),
        link: {
            link: {
                link: "",
                title: ""
            },
            linkTitle: ""
        },
        titleColor: "",
        backgroundColor: "",
        catPic: {
            picId: 1466,
            picThumb: "https://oss.tigshop.com/img/gallery/202411/17318961436JZDXeYmtceZlmj3GK.jpg?x-oss-process=image/resize,m_pad,h_200,h_200",
            picUrl: "https://oss.tigshop.com/img/gallery/202411/17318961436JZDXeYmtceZlmj3GK.jpg",
            picName: "Ftv7Xo3nQ9qZRYPnN9oQB"
        }
    },
    {
        title: t('饮料'),
        link: {
            link: {
                link: "",
                title: ""
            },
            linkTitle: ""
        },
        titleColor: "",
        backgroundColor: "",
        catPic: {
            picId: 1463,
            picThumb: "https://oss.tigshop.com/img/gallery/202411/1731896143uW4eqHtyOfGZ1zl7hO.jpg?x-oss-process=image/resize,m_pad,h_200,h_200",
            picUrl: "https://oss.tigshop.com/img/gallery/202411/1731896143uW4eqHtyOfGZ1zl7hO.jpg",
            picName: "FgboIgXyCabilx_7jFjqAc2vBrye"
        }
    },
    {
        title: t('可爱桌面收纳盒'),
        link: {
            link: {
                link: "",
                title: ""
            },
            linkTitle: ""
        },
        titleColor: "",
        backgroundColor: "",
        catPic: {
            picId: 1461,
            picThumb: "https://oss.tigshop.com/img/gallery/202411/1731896143OQ05zAtqjDwO7YD6jA.jpg?x-oss-process=image/resize,m_pad,h_200,h_200",
            picUrl: "https://oss.tigshop.com/img/gallery/202411/1731896143OQ05zAtqjDwO7YD6jA.jpg",
            picName: "FkVNwFY9oMelIYCDC7ZTNLwpeC"
        }
    }
];
const module = defineModel<ModuleType & ModuleCategoryRecType>("module") as Ref<ModuleType & ModuleCategoryRecType>;
const defaultModule = ref({
    categoryList: categoryList, //分类列表
    animation: defaultAnimation, //动画
    categoryPadding: 3, // 分类内部间距
    radius: 25, // 圆角
    subTitle: defaultSubTitle, //分类标题文字
    backgroundColor: "#fff", // 背景颜色
    catBorder: 0, // 分类边框设置 1显示 0不显示
    catBorderWidth: 1, // 分类边框宽度
    catBorderColor: "#ffffff", // 分类边框颜色
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
const borderStyle = computed(() => {
    let str = "";
    if (Number(module.value.catBorder) === 1) {
        str = `border: ${module.value.catBorderWidth || 1}px solid ${module.value.catBorderColor || "#ffffff"};`;
    }
    return str;
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
.categoryrec {
    overflow: hidden;
}
.item-box {
    width: calc(100% + 10px);
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    box-sizing: border-box;

    .li {
        margin: 5px 10px 5px 0;

        .item {
            height: 30px;
            display: flex;
            text-align: center;
            overflow: hidden;
            align-items: center;
            box-sizing: content-box;
            .tit {
                height: 24px;
                line-height: 24px;
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
            }

            .img {
                margin-right: 5px;
                height: 100%;
                width: auto;
                display: block;
            }
        }
    }
}
</style>
