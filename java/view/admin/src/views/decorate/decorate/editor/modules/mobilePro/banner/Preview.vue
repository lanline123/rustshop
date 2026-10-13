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
import { ref, computed } from "vue";
import { mergeDefaultModule, formatCommonStyle, gradientMap } from "@/views/decorate/decorate/src/modules/index";
import { Swiper } from "@/components/swiper";
import { imageFormat } from "@/utils/format";
import { bannerDefaults } from "./index";
import type { BannerModule } from "./types";

const props = defineProps<{ module: BannerModule }>();
const module = props.module;
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
const commonStyle = computed(() => {
    return formatCommonStyle(module.moduleStyle!);
});
// 旧数据缺字段时烧录默认值（原地写回 store 模块对象，与旧 View.vue 行为一致，契约）
mergeDefaultModule(module, bannerDefaults);
if (module.bannerStyle && (module.bannerStyle.showSearchBtn === undefined || module.bannerStyle.showSearchBtn === null)) {
    module.bannerStyle.showSearchBtn = 1;
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
        logoHeight: "height:" + module?.logoHeight + "px;",
        logoPic: module?.logoPic
    };
});

const menuIcon = computed(() => {
    return module.searchContent?.menuPic?.picUrl
        ? module.searchContent?.menuPic?.picUrl
        : defaultIconImage.meun[module.searchContent.menuIcon - 1]?.picUrl;
});
const signIcon = computed(() => {
    return module.searchContent?.signPic?.picUrl
        ? module.searchContent?.signPic?.picUrl
        : defaultIconImage.sign[module.searchContent?.signIcon - 1]?.picUrl;
});
const swiperPageColor = computed(() => {
    return module.bannerContent.swiperPageColor || "#fff";
});
const circleStyle = computed(() => {
    return `background-image: linear-gradient(${gradientMap[module.bannerContent.gradientType]} ,${module.bannerContent.picList[realIndex.value].gradientColorA},${module.bannerContent.picList[realIndex.value].gradientColorB});height:${module.bannerContent.height}px; border-radius: 0px 0px ${module.bannerContent.backgroundRadius}% ${module.bannerContent.backgroundRadius}%;`;
});
const searchStyle = computed(() => {
    return `height: ${module.bannerStyle?.searchHeight}px; border-radius: ${module.bannerStyle?.searchRadius}px; background-color: ${module.bannerStyle?.searchBackgroundColor}`;
});
const searchTnputStyle = computed(() => {
    return `height: ${module.bannerStyle?.searchHeight}px; line-height: ${module.bannerStyle?.searchHeight}px; color: ${module.bannerStyle?.searchFontColor};`;
});
const searchBtnStyle = computed(() => {
    return `height: ${module.bannerStyle?.searchHeight - 6}px; line-height: ${module.bannerStyle?.searchHeight - 6}px; color: #fff; background-color: ${module.bannerContent.picList[realIndex.value].gradientColorA};border-radius: ${module.bannerStyle?.searchRadius}px;right:3px;top:3px`;
});
const searchWordStyle = computed(() => {
    return `margin-top: 8px; justify-content: ${module.bannerStyle?.textAlignment}; font-size: ${module.bannerStyle?.navFontSize}px`;
});
const wordStyle = computed(() => {
    return `margin: 0px ${module.bannerStyle?.navLabelMargin}px;color: ${module.bannerStyle?.navFontColor}; background-color: ${module.bannerStyle?.navBackgroundColor};padding: 0px 8px;border-radius: 20px;font-weight: ${module.bannerStyle?.navFontBold};`;
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
