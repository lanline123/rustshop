<template>
    <div
        :class="'module-ad-con ad-pic_type__2 ' + ' ad-picPageType__' + module.bannerContent.picPageType"
        :style="
            commonStyle.moduleStyle.boxPaddingBottom +
            commonStyle.moduleStyle.boxPaddingTop +
            commonStyle.moduleStyle.boxPaddingBottom +
            commonStyle.moduleStyle.boxPadding +
            commonStyle.moduleStyle.backgroundSize +
            commonStyle.moduleStyle.backgroundImage +
            `margin-top:${module.moduleStyle?.bannerMarginTop}px;`
        "
    >
        <div class="module-ad-content">
            <div class="Banner">
                <div class="circle" :style="circleStyle"></div>
                <div class="bar" style="margin: 0px 3px 10px">
                    <img class="catnav-logo" v-if="logoFormat.logoPic?.picUrl" :style="logoFormat.logoHeight" :src="imageFormat(logoFormat.logoPic?.picUrl || '')" />
                    <div class="search-top">
                        <div class="menu" v-if="module.searchContent?.menuIcon">
                            <img
                                class="menu-icon"
                                :src="imageFormat(menuIcon)"
                                :style="`width: ${module.bannerStyle?.searchIconSize}px;height:${module.bannerStyle?.searchIconSize}px`"
                            />
                        </div>
                        <div class="search" :style="searchStyle" v-if="module.searchContent?.showSearch">
                            <div class="search-icon" :style="`color:${module.bannerStyle?.searchFontColor}`">
                                <i class="iconfont-h5 icon-sousuo1"></i>
                            </div>
                            <form action="" class="search-form">
                                <div class="search-input" :style="searchTnputStyle">
                                    <div class="search-placeholder">{{ module.searchContent?.searchText }}</div>
                                </div>
                                <div class="search-btn" :style="searchBtnStyle" v-if="module.bannerStyle?.showSearchBtn">{{ module.bannerStyle?.searchText }}</div>
                            </form>
                        </div>
                        <div class="sign" v-if="module.searchContent?.signIcon">
                            <img
                                class="sign-icon"
                                :src="imageFormat(signIcon)"
                                :style="`width: ${module.bannerStyle?.searchIconSize}px;height:${module.bannerStyle?.searchIconSize}px`"
                            />
                        </div>
                    </div>
                    <div class="search-word" :style="searchWordStyle" v-if="module.navContent?.showNav">
                        <div class="tit" :style="`color: ${module.bannerStyle?.navFontColor};font-weight: ${module.bannerStyle?.navFontBold};`">
                            {{ module.bannerStyle?.navPrefix }}
                        </div>
                        <ul class="word-list">
                            <template v-for="item in module.navContent?.navList" :key="item.picTitle">
                                <li class="item">
                                    <div class="word" :style="wordStyle">
                                        {{ item.picTitle }}
                                    </div>
                                </li>
                            </template>
                        </ul>
                    </div>
                </div>
                <div class="image-ad-warp">
                    <div class="image-ad-con">
                        <Swiper :swiperOption="swiperOption" v-model="module.bannerContent.picList" @activeIndexChange="handleActiveIndexChange">
                            <template v-slot:default="{ item }">
                                <div class="image-ad-item" :style="`border-radius: ${module.bannerContent.picRadius}px;overflow: hidden;`">
                                    <img class="item-img" :src="imageFormat(item.picUrl)" />
                                </div>
                            </template>
                        </Swiper>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref, computed, Ref } from "vue";
import { ModuleType, ModuleBannerType } from "@/types/decorate/decorate.d";
import { mergeDefaultModule, defaultModuleStyle, formatCommonStyle, gradientMap } from "@/views/decorate/decorate/src/modules/";
import { Swiper } from "@/components/swiper";
import { imageFormat } from "@/utils/format";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const module = defineModel<ModuleType & ModuleBannerType>("module") as Ref<ModuleType & ModuleBannerType>;
const swiperOption = ref<any>({
    autoplay: {
        delay: 3000,
        disableOnInteraction: false, //用户操作swiper之后，是否禁止autoplay
        pauseOnMouseEnter: true //鼠标置于swiper是否时暂停自动切换
    },
    pagination: {
        clickable: true
    }
});
const realIndex = ref(0);
const handleActiveIndexChange = (e: any) => {
    realIndex.value = e.realIndex;
};
const navList = [
    {
        path: "",
        label: "",
        name: "",
        data: {
            name: "",
            path: ""
        },
        picTitle: t('新品'),
        picLink: {}
    },
    {
        path: "",
        label: "",
        name: "",
        data: {
            name: "",
            path: ""
        },
        picTitle: t('百货')
    },
    {
        path: "",
        label: "",
        name: "",
        data: {
            name: "",
            path: ""
        },
        picTitle: t('零食')
    },
    {
        path: "",
        label: "",
        name: "",
        data: {
            name: "",
            path: ""
        },
        picTitle: t('折扣')
    },
    {
        path: "",
        label: "",
        name: "",
        data: {
            name: "",
            path: ""
        },
        picTitle: t('猜你喜欢')
    }
];
const picList = [
    {
        picId: 1414,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1731044101tXK31aimefqoUsJSrM.jpg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/1731301937REbUnsSmPehWiBMaFd.webp",
        picName: "FjfhKDiUzqW_lqfJqncKt3QJplx4",
        gradientColorA: "#62a05c",
        gradientColorB: "#72b76c"
    },
    {
        picId: 1415,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1731044101j8COoqtfDJCWrSts6b.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/1731301941uaSjIShOBNL2VBSr0y.webp",
        picName: "Fv5iP2IHByVFUlGrLkXtPkClndla",
        gradientColorA: "#62a05c",
        gradientColorB: "#72b76c"
    },
    {
        picId: 1413,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1731044101D4owQpnWiu0DcTqmKO.jpg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/1731301927a0N31LV7yy1i6Zlk4T.webp",
        picName: "FhHfxuZP5Z5P_Hc7SHBzvm6RwHud",
        gradientColorA: "#62a05c",
        gradientColorB: "#72b76c"
    }
];
const logo = {
    picUrl: "http://oss.tigshop.com/img/gallery/202409/1727056331nhxqtK8Mm5u8MXokoI.png",
    picThumb: "https://oss.tigshop.com/img/gallery/202409/1727056331nhxqtK8Mm5u8MXokoI.png?x-oss-process=image/resize,m_pad,h_200,h_200",
    picId: 1221,
    picName: "logoWhite"
};
const defaultModule = ref({
    logoPic: logo,
    logoHeight: 100, //logo高度
    bannerContent: {
        //轮播内容
        picList: picList, //图片列表
        picRadius: 6, //轮播图片圆角
        height: 200, //弧度背景高度
        backgroundRadius: 50, //弧度背景圆角
        gradientType: "upDown", //弧度背景方向
        picPageType: 1, //指示器样式
        swiperPageColor: "" //指示器颜色
    },
    searchContent: {
        //搜索框设置
        showSearch: 1, //是否显示搜索框 1显示 0不显示
        searchText: t('搜索商品'), //搜索框文字
        menuIcon: 1, //菜单图标选项
        menuLink: "", //菜单链接
        menuPic: {
            //自定义菜单图标
            picUrl: "",
            picThumb: ""
        },
        signIcon: 1, //签到图标选项
        signLink: "", //签到链接
        signPic: {
            //自定义签到图标
            picUrl: "",
            picThumb: ""
        }
    },
    navContent: {
        //导航文字设置
        showNav: 1, //是否显示导航 1显示 0不显示
        navList: navList ///导航列表
    },
    bannerStyle: {
        //样式设置
        effectType: "ceiling", //搜索框/导航效果 ceiling 滚动至顶部固定  default 正常模式
        searchBackgroundColor: "#ffffff", //搜索框背景颜色
        searchFontColor: "#999999", //搜索框文本颜色
        showSearchBtn: 1, //是否显示搜索按钮 1显示 0不显示
        searchText: t('搜索'), //搜索框文字
        searchHeight: 34, //搜索框高度
        searchRadius: 20, //搜索框圆角
        searchIconSize: 24, //搜索图标大小
        navPrefix: t('热搜:'), //导航文字前缀
        navFontColor: "#ffffff", //导航文字颜色
        navBackgroundColor: "rgba(255, 255, 255, 0.3)", //导航背景颜色
        navPrefixColor: "#ffffff", //导航前缀颜色
        textAlignment: "center", //导航文字对齐方式 left center right
        navFontBold: 400, //导航文字粗细
        navLabelMargin: 5, //导航文字间距
        navFontSize: 12 //导航文字大小
    },
    moduleStyle: defaultModuleStyle // 模块样式
});
const commonStyle = computed(() => {
    return formatCommonStyle(module.value.moduleStyle!);
});
mergeDefaultModule(module.value, defaultModule.value);
if (module.value.bannerStyle && (module.value.bannerStyle.showSearchBtn === undefined || module.value.bannerStyle.showSearchBtn === null)) {
    module.value.bannerStyle.showSearchBtn = 1;
}

const defaultIconImage = {
    meun: [
        {
            picUrl: "https://oss.tigshop.com/img/gallery/202411/1731044058k3D2oAM6v4j1NUqnd4.png",
            picName: "menu1"
        },
        {
            picUrl: "https://oss.tigshop.com/img/gallery/202411/1731044058yv4ngGH3BtQalK2Ews.png",
            picName: "menu2"
        },
        {
            picUrl: "https://oss.tigshop.com/img/gallery/202411/17310440580AWfxX5dX8tq2iC91X.png",
            picName: "menu3"
        },

        {
            picUrl: "https://oss.tigshop.com/img/gallery/202411/1731044058oBfYyYzrYBK3daMTiS.png",
            picName: "menu4"
        },
        {
            picUrl: "https://oss.tigshop.com/img/gallery/202411/17310440582tapohBoU1mliQxGgT.png",
            picName: "menu5"
        },
        {
            picUrl: "https://oss.tigshop.com/img/gallery/202411/1731044058Xxnsx3an6igjmg9OZh.png",
            picName: "menu6"
        },
        {
            picUrl: "https://oss.tigshop.com/img/gallery/202411/1731044060czIoPbXd4pO0GteMVZ.png",
            picName: "menu7"
        }
    ],
    sign: [
        {
            picUrl: "https://oss.tigshop.com/img/gallery/202411/1731044060WUMf13Ls2YfzTFHVYR.png",
            picName: "sign1"
        },
        {
            picUrl: "https://oss.tigshop.com/img/gallery/202411/17310440604yFr67HdqrIIk5UCzm.png",
            picName: "sign2"
        },
        {
            picUrl: "https://oss.tigshop.com/img/gallery/202411/1731044060gYalEs84jlX6pMBE0p.png",
            picName: "sign3"
        },
        {
            picUrl: "https://oss.tigshop.com/img/gallery/202411/1731044060cZffgle3HfRaQtiH3V.png",
            picName: "sign4"
        }
    ]
};

const logoFormat = computed(() => {
    return {
        logoHeight: "height:" + module?.value.logoHeight + "px;",
        logoPic: module?.value.logoPic
    };
});

const menuIcon = computed(() => {
    return module.value.searchContent?.menuPic?.picUrl
        ? module.value.searchContent?.menuPic?.picUrl
        : defaultIconImage.meun[module.value.searchContent.menuIcon - 1]?.picUrl;
});
const signIcon = computed(() => {
    return module.value.searchContent?.signPic?.picUrl
        ? module.value.searchContent?.signPic?.picUrl
        : defaultIconImage.sign[module.value.searchContent?.signIcon - 1]?.picUrl;
});
const swiperPageColor = computed(() => {
    return module.value.bannerContent.swiperPageColor || "#fff";
});
const circleStyle = computed(() => {
    return `background-image: linear-gradient(${gradientMap[module.value.bannerContent.gradientType]} ,${module.value.bannerContent.picList[realIndex.value].gradientColorA},${module.value.bannerContent.picList[realIndex.value].gradientColorB});height:${module.value.bannerContent.height}px; border-radius: 0px 0px ${module.value.bannerContent.backgroundRadius}% ${module.value.bannerContent.backgroundRadius}%;`;
});
const searchStyle = computed(() => {
    return `height: ${module.value.bannerStyle?.searchHeight}px; border-radius: ${module.value.bannerStyle?.searchRadius}px; background-color: ${module.value.bannerStyle?.searchBackgroundColor}`;
});
const searchTnputStyle = computed(() => {
    return `height: ${module.value.bannerStyle?.searchHeight}px; line-height: ${module.value.bannerStyle?.searchHeight}px; color: ${module.value.bannerStyle?.searchFontColor};`;
});
const searchBtnStyle = computed(() => {
    return `height: ${module.value.bannerStyle?.searchHeight - 6}px; line-height: ${module.value.bannerStyle?.searchHeight - 6}px; color: #fff; background-color: ${module.value.bannerContent.picList[realIndex.value].gradientColorA};border-radius: ${module.value.bannerStyle?.searchRadius}px;right:3px;top:3px`;
});
const searchWordStyle = computed(() => {
    return `margin-top: 8px; justify-content: ${module.value.bannerStyle?.textAlignment}; font-size: ${module.value.bannerStyle?.navFontSize}px`;
});
const wordStyle = computed(() => {
    return `margin: 0px ${module.value.bannerStyle?.navLabelMargin}px;color: ${module.value.bannerStyle?.navFontColor}; background-color: ${module.value.bannerStyle?.navBackgroundColor};padding: 0px 8px;border-radius: 20px;font-weight: ${module.value.bannerStyle?.navFontBold};`;
});
</script>
<style lang="less" scoped>
.catnav-logo {
    box-sizing: border-box;
    margin-top: 2.5px;
    margin-bottom: 5px;
    object-fit: scale-down;
    max-width: 110px;
}
:deep(.swiper-pagination-bullet-active) {
    background: v-bind("swiperPageColor") !important;
}
.word-list::-webkit-scrollbar {
    display: none;
    height: 0;
}
.module-ad-content {
}
.module-ad-con {
    background-size: cover;
    background-repeat: no-repeat;
    background-position: center top;
    position: relative;
    margin: 0 auto;
    overflow: hidden;
    position: relative;
}
.circle {
    position: absolute;
    top: 0;
    left: -20px;
    right: -20px;
}
.bar {
    position: relative;
    overflow: hidden;
}
.search-top {
    display: flex;
    flex-wrap: nowrap;
    justify-content: space-between;
    position: relative;
    .menu {
        margin-right: 8px;
        display: flex;
        align-items: center;

        .menu-icon {
            width: 24px;
            height: 24px;
        }
    }

    .search {
        display: flex;
        flex-wrap: nowrap;
        justify-content: flex-start;
        align-items: center;
        height: 36px;
        width: 100%;
        border-radius: 20px;
        z-index: 2;
        background-color: #ffffffcc;
        color: #969799;
        overflow: hidden;
    }
    .search-icon {
        margin-left: 12px;
        font-size: 14px;
    }
    .search-form {
        height: 100%;
        padding: 0 0 0 3px;
        flex-grow: 1;
        display: flex;
        align-items: center;
        position: relative;
        .search-input {
            --color: #323233;
            display: block;
            box-sizing: border-box;
            width: 100%;
            margin: 0;
            padding: 0;
            font-size: 14px;
            color: var(--color, #323233);
            text-align: left;
            background-color: transparent;
            border: 0;
            resize: none;
        }
        .search-btn {
            position: absolute;
            top: 0;
            right: 0;
            padding: 0 15px;
            font-size: 14px;
            text-align: center;
            color: #fff;
            background: #000;
        }
    }
    .sign {
        margin-left: 8px;
        display: flex;
        align-items: center;

        .sign-icon {
            width: 22px;
            height: 22px;
        }
    }
}
.search-word {
    height: 24px;
    line-height: 24px;
    display: flex;
    font-size: 12px;
    color: #fff;
    font-weight: 400;
    justify-content: center;
    align-items: center;
    position: relative;
    .word-list {
        margin-left: 2px;
        height: 24px;
        line-height: 24px;
        overflow-x: scroll;
        overflow-y: hidden;
        display: flex;
    }
    .tit {
        flex-shrink: 0;
    }
    .item {
        display: flex;
        flex-shrink: 0;
        .word {
            height: 24px;
            text-decoration: none;
        }
    }
}
</style>
