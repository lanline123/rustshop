<template>
    <view
        :class="'banner module-ad-con ad-pic_type__2 ' + ' ad-pic_page_type__' + module.bannerContent.picPageType"
        :style="
            commonStyle.moduleStyle.boxPaddingBottom +
            commonStyle.moduleStyle.boxPaddingTop +
            commonStyle.moduleStyle.boxPadding +
            commonStyle.moduleStyle.backgroundSize +
            commonStyle.moduleStyle.backgroundImage +
            `margin-top:${module.moduleStyle?.bannerMarginTop}px;`
        "
    >
        <view class="module-ad-content">
            <view class="banner">
                <view class="circle" :style="circleStyle" />
                <!-- 吸顶开启时隐藏常驻栏视觉，保留占位，避免与吸顶副本叠成双头 -->
                <view class="bar" :class="{ 'is-ceiling-hidden': fixed }">
                    <view :style="`padding-top: ${index == 0 ? saveHeightNum : 0}px`" />
                    <template v-if="logoFormat.logoPic?.picUrl">
                        <image class="catnav-logo" mode="aspectFit" :style="logoFormat.logoHeight" :src="imageFormat(logoFormat.logoPic?.picUrl || '')" />
                    </template>
                    <view class="search-top banner-search-anchor">
                        <view
                            v-if="module.searchContent?.menuIcon == 1"
                            class="menu"
                            @click="urlFormat(module.searchContent.menuLink) && redirect({ url: urlFormat(module.searchContent.menuLink) })"
                        >
                            <image
                                class="menu-icon"
                                :src="imageFormat(menuIcon)"
                                :style="`width: ${module.bannerStyle?.searchIconSize}px;height:${module.bannerStyle?.searchIconSize}px`"
                            />
                        </view>
                        <view
                            v-if="module.searchContent?.showSearch == 1"
                            class="search"
                            :style="searchStyle"
                            @click="redirect({ url: '/pages/search/index' })"
                        >
                            <view class="search-icon" :style="`color:${module.bannerStyle?.searchFontColor}`"><text class="iconfont-h5 icon-sousuo1" /></view>
                            <view class="search-form">
                                <view class="search-input" :style="searchTnputStyle">
                                    <view class="search-placeholder">{{ $t(module.searchContent?.searchText) }}</view>
                                </view>
                                <view v-if="(module.bannerStyle?.showSearchBtn ?? 1) == 1" class="search-btn" :style="searchBtnStyle">{{
                                    $t(module.bannerStyle?.searchText)
                                }}</view>
                            </view>
                        </view>
                        <view
                            v-if="module.searchContent?.signIcon == 1"
                            class="sign"
                            @click="urlFormat(module.searchContent.signLink) && redirect({ url: urlFormat(module.searchContent.signLink) })"
                        >
                            <image
                                class="sign-icon"
                                :src="imageFormat(signIcon)"
                                :style="`width: ${module.bannerStyle?.searchIconSize - 2}px;height:${module.bannerStyle?.searchIconSize - 2}px`"
                            />
                        </view>
                        <template v-if="isOverseas()">
                            <view @click="handleclickLang">
                                <view class="lang-icon-box">
                                    <view class="iconfont-h5 icon-diqiu">
                                        <text class="iconfont-h5 icon-sanjiaoright" />
                                    </view>
                                </view>
                            </view>
                        </template>
                    </view>
                    <view v-if="module.navContent?.showNav == 1" class="search-word" :style="searchWordStyle">
                        <view class="tit" :style="`color: ${module.bannerStyle?.navFontColor};font-weight: ${module.bannerStyle?.navFontBold};`">
                            {{ $t(module.bannerStyle?.navPrefix) }}
                        </view>
                        <view class="word-list">
                            <template v-for="item in module.navContent?.navList" :key="item.picTitle">
                                <view class="item" @click="urlFormat(item.picLink) && redirect({ url: urlFormat(item.picLink) })">
                                    <view class="word" :style="wordStyle">
                                        {{ $t(item.picTitle ?? "") }}
                                    </view>
                                </view>
                            </template>
                        </view>
                    </view>
                </view>

                <view
                    :class="['bar', { fixed: fixed }]"
                    :style="
                        commonStyle.moduleStyle.boxPaddingTop +
                        commonStyle.moduleStyle.boxPadding +
                        `z-index: ${fixed ? 99 : 9};display:${fixed ? 'block' : 'none'};` +
                        (fixed ? `top:${headerOffset}px;` : '')
                    "
                >
                    <view :style="`padding-top: ${index == 0 ? saveHeight : 0}px`" />
                    <view class="circle" :style="circleStyle" />
                    <view class="search-top">
                        <view
                            v-if="module.searchContent?.menuIcon == 1"
                            class="menu"
                            @click="urlFormat(module.searchContent.menuLink) && redirect({ url: urlFormat(module.searchContent.menuLink) })"
                        >
                            <image
                                class="menu-icon"
                                :src="imageFormat(menuIcon)"
                                :style="`width: ${module.bannerStyle?.searchIconSize}px;height:${module.bannerStyle?.searchIconSize}px`"
                            />
                        </view>
                        <view
                            v-if="module.searchContent?.showSearch == 1"
                            class="search"
                            :style="searchStyle"
                            @click="redirect({ url: '/pages/search/index' })"
                        >
                            <view class="search-icon" :style="`color:${module.bannerStyle?.searchFontColor}`"><text class="iconfont-h5 icon-sousuo1" /></view>
                            <form action="" class="search-form">
                                <view class="search-input" :style="searchTnputStyle">
                                    <view class="search-placeholder">{{ $t(module.searchContent?.searchText) }}</view>
                                </view>
                                <view v-if="(module.bannerStyle?.showSearchBtn ?? 1) == 1" class="search-btn" :style="searchBtnStyle">{{
                                    $t(module.bannerStyle?.searchText)
                                }}</view>
                            </form>
                        </view>
                        <view
                            v-if="module.searchContent?.signIcon == 1"
                            class="sign"
                            @click="urlFormat(module.searchContent.signLink) && redirect({ url: urlFormat(module.searchContent.signLink) })"
                        >
                            <image
                                class="sign-icon"
                                :src="imageFormat(signIcon)"
                                :style="`width: ${module.bannerStyle?.searchIconSize - 2}px;height:${module.bannerStyle?.searchIconSize - 2}px`"
                            />
                        </view>
                        <template v-if="isOverseas()">
                            <view @click="handleclickLang">
                                <view class="lang-icon-box">
                                    <view class="iconfont-h5 icon-diqiu">
                                        <text class="iconfont-h5 icon-sanjiaoright" />
                                    </view>
                                </view>
                            </view>
                        </template>
                    </view>

                    <view v-if="module.navContent?.showNav == 1" class="search-word" :style="searchWordStyle">
                        <view class="tit" :style="`color: ${module.bannerStyle?.navFontColor};font-weight: ${module.bannerStyle?.navFontBold};`">
                            {{ $t(module.bannerStyle?.navPrefix) }}
                        </view>
                        <view class="word-list">
                            <template v-for="item in module.navContent?.navList" :key="item.picTitle">
                                <view class="item" @click="urlFormat(item.picLink) && redirect({ url: urlFormat(item.picLink) })">
                                    <view class="word" :style="wordStyle">
                                        {{ $t(item.picTitle ?? "") }}
                                    </view>
                                </view>
                            </template>
                        </view>
                    </view>
                </view>

                <view class="image-ad-warp">
                    <view class="image-ad-con">
                        <tig-swiper
                            v-model="module.bannerContent.picList"
                            :class="{ 'uni-swiper-dot-box': module.bannerContent.picPageType == 3 }"
                            :mode="mode"
                            :color="module.bannerContent.picPageType == 3 ? '#fff' : ''"
                            :select-color="selectColor"
                            :height="height + 'px'"
                            @change="swiperChange"
                        >
                            <template #default="{ item }">
                                <view
                                    class="image-ad-item"
                                    :style="`border-radius: ${module.bannerContent.picRadius}px;overflow: hidden;`"
                                    @click="item.picLink && redirect({ url: urlFormat(item.picLink) })"
                                >
                                    <view class="item-content" :style="`background-image:url(${imageFormat(item.picUrl)});padding-top:${imgratio * 100}%;`" />
                                </view>
                            </template>
                        </tig-swiper>
                    </view>
                </view>
            </view>
        </view>
        <template v-if="isOverseas()">
            <tig-select-lang ref="selectLangRef" :show-lang-btn="false" />
        </template>
    </view>
</template>

<script setup lang="ts">
import { computed, ref, watch, onMounted, onUnmounted, getCurrentInstance, nextTick } from "vue";
import { formatCommonStyle, gradientMap } from "@/components/modules";
import { urlFormat, imageFormat } from "@/utils/format";
import { redirect, getElementRect, isOverseas } from "@/utils";
import { useSaveTopBoxHeight, useScrollTop } from "@/hooks/index";
import { useImgInfo } from "@/hooks/useDecorate";
import { useConfigStore } from "@/store/config";
const configStore = useConfigStore();
/** 海外版 mainHeader 占位高度：吸顶栏 top 与阈值都要让开 */
const headerOffset = computed(() => (isOverseas() ? configStore.commonHeaderHeight || 0 : 0));
const props = defineProps({
    module: {
        type: Object,
        default: () => ({})
    },
    index: {
        type: Number,
        default: -1
    }
});

const logoFormat = computed(() => {
    return {
        logoHeight: "height:" + props.module.logoHeight + "px;",
        logoPic: props.module.logoPic
    };
});

const { scrollTop } = useScrollTop();
const { height: saveHeight, statusBarHeight } = useSaveTopBoxHeight();
const saveHeightNum = computed(() => {
    return logoFormat.value.logoPic?.picUrl ? statusBarHeight.value : saveHeight.value;
});

const selectLangRef = ref();
const handleclickLang = () => {
    selectLangRef.value && selectLangRef.value.handleclickLang();
};
// #ifdef APP-PLUS
const { imgratio, height, getImgratio, updateImgInfo } = useImgInfo(props.module.bannerContent.picList[0].picUrl, ".image-ad-item", false);
getImgratio(props.module.bannerContent.picList[0].picUrl);
// #endif
// #ifndef APP-PLUS
const { imgratio, height, updateImgInfo } = useImgInfo(props.module.bannerContent.picList[0].picUrl, ".image-ad-item");
// #endif
const windowResizeCallback = async () => {
    await updateImgInfo();
    // 仅在页顶附近重测，避免滚动中重测把阈值算歪
    if (scrollTop.value <= 2) {
        await measureCeilingThreshold();
    }
};
uni.onWindowResize(windowResizeCallback);
onUnmounted(() => {
    uni.offWindowResize(windowResizeCallback);
});

const instance = getCurrentInstance();
const isCeiling = computed(() => props.module.bannerStyle?.effectType == "ceiling");
/** 未测到前用 Infinity，避免阈值默认 0 导致「动一点就吸顶」 */
const ceilingThreshold = ref(Number.POSITIVE_INFINITY);

const fixed = computed(() => {
    if (!isCeiling.value) {
        return false;
    }
    return scrollTop.value > ceilingThreshold.value;
});

const measureCeilingThreshold = async () => {
    if (!isCeiling.value) {
        ceilingThreshold.value = Number.POSITIVE_INFINITY;
        return;
    }
    await nextTick();
    // 吸顶栏自上而下：mainHeader + moduleStyle.boxPaddingTop + saveHeight（安全区）+ 搜索栏
    const boxPaddingTop = Number(props.module.moduleStyle?.boxPaddingTop) || 0;
    const safePad = props.index == 0 ? Number(saveHeight.value) || 0 : 0;
    const fixedSearchOffset = headerOffset.value + boxPaddingTop + safePad;
    const searchRes = await getElementRect(".banner-search-anchor", instance);
    if (searchRes && typeof searchRes.top === "number") {
        // 常驻搜索滚到与吸顶后搜索同一位置时再 fix（含装修 padding-top，避免「搜索框挨边才吸顶」）
        ceilingThreshold.value = Math.max(0, scrollTop.value + searchRes.top - fixedSearchOffset);
        return;
    }
    // 测量失败：回落为吸顶偏移本身，绝不回落 0
    ceilingThreshold.value = Math.max(1, fixedSearchOffset || Number(statusBarHeight.value) || 40);
};
const realIndex = ref(0);
const selectColor = computed(() => {
    return props.module.swiperPageColor || "#fff";
});
const mode = computed(() => {
    return props.module.bannerContent.picPageType == 1 ? "default" : "dot";
});
const commonStyle = computed(() => {
    return formatCommonStyle(props.module.moduleStyle);
});
const swiperChange = (e: any) => {
    realIndex.value = e;
};
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
const menuIcon = computed(() => {
    return props.module.searchContent?.menuPic?.picUrl
        ? props.module.searchContent?.menuPic?.picUrl
        : defaultIconImage.meun[props.module.searchContent.menuIcon - 1]?.picUrl;
});
const signIcon = computed(() => {
    return props.module.searchContent?.signPic?.picUrl
        ? props.module.searchContent?.signPic?.picUrl
        : defaultIconImage.sign[props.module.searchContent?.signIcon - 1]?.picUrl;
});
const circleStyle = computed(() => {
    return `background-image: linear-gradient(${gradientMap[props.module.bannerContent.gradientType]} ,${
        props.module.bannerContent.picList[realIndex.value]?.gradientColorA
    },${props.module.bannerContent.picList[realIndex.value].gradientColorB});height:${
        props.index == 0 ? Number(props.module.bannerContent.height) + Number(saveHeight.value) : props.module.bannerContent.height
    }px; border-radius: 0px 0px ${props.module.bannerContent.backgroundRadius}% ${props.module.bannerContent.backgroundRadius}%;`;
});
const searchStyle = computed(() => {
    return `height: ${props.module.bannerStyle?.searchHeight}px; border-radius: ${props.module.bannerStyle?.searchRadius}px; background-color: ${props.module.bannerStyle?.searchBackgroundColor}`;
});
const searchTnputStyle = computed(() => {
    return `height: ${props.module.bannerStyle?.searchHeight}px; line-height: ${props.module.bannerStyle?.searchHeight}px; color: ${props.module.bannerStyle?.searchFontColor};`;
});
const searchBtnStyle = computed(() => {
    return `height: ${props.module.bannerStyle?.searchHeight - 6}px; line-height: ${
        props.module.bannerStyle?.searchHeight - 6
    }px; color: #fff; background-color: ${props.module.bannerContent.picList[realIndex.value]?.gradientColorA};border-radius: ${
        props.module.bannerStyle?.searchRadius
    }px;right:3px;top:3px`;
});
const searchWordStyle = computed(() => {
    return `margin-top: 8px; justify-content: ${props.module.bannerStyle?.textAlignment}; font-size: ${props.module.bannerStyle?.navFontSize}px`;
});
const wordStyle = computed(() => {
    return `margin: 0px ${props.module.bannerStyle?.navLabelMargin}px;color: ${props.module.bannerStyle?.navFontColor}; background-color: ${props.module.bannerStyle?.navBackgroundColor};padding: 0px 8px;border-radius: 20px;font-weight: ${props.module.bannerStyle?.navFontBold};`;
});

onMounted(async () => {
    await measureCeilingThreshold();
    // 布局未稳定时补测一次
    setTimeout(() => {
        measureCeilingThreshold();
    }, 100);
    // #ifdef APP-PLUS
    height.value = (configStore.windowInfo.screenWidth - 20) * imgratio.value;
    // #endif
});

// mainHeader 异步测高后，吸顶阈值与 top 需重算
watch(headerOffset, () => {
    if (isCeiling.value) {
        measureCeilingThreshold();
    }
});
</script>

<style lang="scss" scoped>
.catnav-logo {
    max-width: 210rpx;
}

.lang-icon-box {
    display: flex;
    align-items: center;
    height: 100%;
    .icon-diqiu {
        font-size: 55rpx;
        color: #fff;
        position: relative;
        padding-right: 30rpx;
        padding-left: 20rpx;

        .icon-sanjiaoright {
            font-size: 20rpx;
            color: #fff;
            font-weight: bold;
            position: absolute;
            bottom: 2rpx;
            right: 10rpx;
            transform: rotate(90deg);
        }
    }
}

.image-ad-item {
    width: 100%;
}
.fixed {
    position: fixed !important;
    top: 0;
    right: 0;
    left: 0;
    z-index: 99;
}
.item-content {
    width: 100%;
    background-repeat: no-repeat;
    background-size: cover;
    background-position: center;
}
.module-ad-con {
    background-size: cover;
    background-repeat: no-repeat;
    background-position: center top;
    position: relative;
    margin: 0 auto;
    overflow: hidden;
    position: relative;
    box-sizing: content-box;
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
    padding: 0px 0px 10px;
    z-index: 9;
}
.bar.is-ceiling-hidden {
    visibility: hidden;
}
.search-top {
    display: flex;
    flex-wrap: nowrap;
    justify-content: space-between;
    align-items: center;
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
    overflow: hidden;
    overflow-x: auto;
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
