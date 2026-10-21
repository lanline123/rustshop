<template>
    <view v-if="picList.length > 0" class="bannerA2 module-ad-con">
        <view class="banner-bg">
            <image :src="currentImageUrl" @load="handleImageLoad" />
        </view>
        <view class="bg-gradient" />
        <view
            class="banner-box"
            :style="
                commonStyle.moduleStyle.boxPaddingBottom +
                commonStyle.moduleStyle.boxPaddingTop +
                commonStyle.moduleStyle.boxPadding +
                commonStyle.moduleStyle.backgroundSize +
                commonStyle.moduleStyle.backgroundImage
            "
        >
            <view class="banner" :style="`margin-top:${module.moduleStyle?.bannerMarginTop}px;`">
                <view class="bar">
                    <navbarContent :height="props.module.bannerStyle?.searchHeight + 'px'" :need-status-bar="needStatusBar">
                        <view class="search-top">
                            <view class="search" :style="searchStyle" @click="redirect({ url: '/pages/search/index' })">
                                <view class="search-icon" :style="`color:${module.bannerStyle?.searchIconColor}`">
                                    <text class="iconfont-h5 icon-sousuo1" />
                                </view>
                                <view class="search-form">
                                    <view class="search-input" :style="searchTnputStyle">
                                        <view class="search-placeholder">{{ module.searchContent?.searchText }}</view>
                                    </view>
                                </view>
                            </view>
                        </view>
                    </navbarContent>
                </view>

                <!-- <view
                    :class="['bar', { fixed: fixed }]"
                    :style="
                        commonStyle.moduleStyle.boxPaddingTop +
                        commonStyle.moduleStyle.boxPadding +
                        `z-index: ${fixed ? 99 : 9};display:${fixed ? 'block' : 'none'};`
                    "
                >
                    <view :style="`padding-top: ${index == 0 ? saveHeight : 0}px`" />
                    <view class="search-top">
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
                                <view class="search-btn" :style="searchBtnStyle">{{ $t(module.bannerStyle?.searchText) }}</view>
                            </form>
                        </view>
                    </view>
                </view> -->

                <view class="image-ad-warp">
                    <view class="image-ad-con">
                        <tig-swiper
                            :model-value="picList"
                            :class="{ 'uni-swiper-dot-box': module.picPageType == 3 }"
                            :dot="false"
                            :mode="mode"
                            :height="swiperHeight + 'px'"
                            @change="swiperChange"
                        >
                            <template #default="{ item }">
                                <view
                                    class="image-ad-item"
                                    :style="`border-radius: ${module.bannerContent?.picRadius || 0}px;overflow: hidden;`"
                                    @click="item.picLink && redirect({ url: urlFormat(item.picLink) })"
                                >
                                    <view class="item-content" :style="`background-image:url(${imageFormat(item.picUrl || item.picThumb)});padding-top:${imgratio * 100}%;`" />
                                </view>
                            </template>
                        </tig-swiper>
                        <view class="indicator-box" :class="{ type2: module.bannerContent.picPageType == 2 }">
                            <template v-for="item in picList.length" :key="item">
                                <view class="indicator-item" :class="{ 'indicator-item-active': realIndex + 1 === item }" />
                            </template>
                        </view>
                    </view>
                </view>
            </view>
        </view>
    </view>
</template>

<script setup lang="ts">
import { useI18n } from "vue-i18n";
import { computed, ref, onMounted, onUnmounted, getCurrentInstance } from "vue";
import { formatCommonStyle } from "@/components/modules";
import { urlFormat, imageFormat } from "@/utils/format";
import { redirect, getElementRect, isO2o } from "@/utils";
import { useScrollTop } from "@/hooks/index";
import { useImgInfo } from "@/hooks/useDecorate";
import { useConfigStore } from "@/store/config";
import { useRegionStore } from "@/store/region";
import { useUserStore } from "@/store/user";
import { getAddressList } from "@/api/user/address";
import type { AddressFilterResult } from "@/types/user/address";
import navbarContent from "@/components/navbarContent/index.vue";

const { t } = useI18n();
const props = defineProps({
    module: {
        type: Object,
        default: () => ({})
    },
    index: {
        type: Number,
        default: -1
    },
    needStatusBar: {
        type: Boolean,
        default: true
    }
});

const picList = computed(() => {
    const list = props.module?.bannerContent?.picList;
    if (!Array.isArray(list)) return [];
    return list.filter((item: any) => item?.picUrl || item?.picThumb);
});

const realIndex = ref(0);

const currentImageUrl = computed(() => {
    if (!picList.value.length) return "";
    const item = picList.value[realIndex.value] || picList.value[0];
    return imageFormat(item?.picUrl || item?.picThumb || "");
});

const configStore = useConfigStore();

const regionStore = useRegionStore();

const userStore = useUserStore();

// 定位模式下默认展示的最近收货地址（优先选中/默认地址，其次列表首条）
const latestAddress = ref<AddressFilterResult | null>(null);
const latestAddressName = computed(() => latestAddress.value?.address || "");

const fetchLatestAddress = async () => {
    if (!userStore.token) {
        latestAddress.value = null;
        return;
    }
    try {
        const result = await getAddressList({ page: 1, size: 20 });
        const records = result.records || [];
        latestAddress.value = records.find((item) => item.isSelected === 1) || records.find((item) => item.isDefault === 1) || records[0] || null;
    } catch (error) {
        console.error("获取最近收货地址失败:", error);
        latestAddress.value = null;
    }
};

// 进店模式：1-网店可切换，2-网店不可切换，3-定位可切换，4-定位不可切换
const entryMode = computed(() => Number(configStore.storeEntryRule) || 1);

// 是否走"显示当前网店"分支（模式一、二）；否则走"显示定位/收货地址"（模式三、四）
const isShopMode = computed(() => entryMode.value === 1 || entryMode.value === 2);

// 是否允许切换（模式一、三可切换，模式二、四不可切换）
const canSwitch = computed(() => entryMode.value === 1 || entryMode.value === 3);

// 头部左侧展示文本
// 网店模式：优先取用户主动选择的 currentShop，回退到按定位计算的 nearestShopInfo
// 定位模式：优先显示选中的收货地址/选点详情，回退到当前地区名
const entryText = computed(() => {
    if (isShopMode.value) {
        return regionStore.currentShop?.shopTitle || regionStore.nearestShopInfo?.shopTitle || t("选择门店");
    }
    // 定位模式：用户显式选择的展示名优先；否则默认最近收货地址；都没有才回退定位
    return regionStore.currentSelectedName || latestAddressName.value || regionStore.currentRegion?.regionName || t("定位中");
});

const handleAreaClick = () => {
    if (!canSwitch.value) return;
    if (isShopMode.value) {
        redirect({ url: "/pages/store/select/index" });
        return;
    }
    redirect({ url: "/pages/region/index" });
};

const { scrollTop } = useScrollTop();

// 测量点取 .image-ad-con 而非 swiper 内部的 .image-ad-item：后者宽度依赖 swiper 内部布局，
// 页面返回且图片命中缓存时，测量会早于布局完成拿到 0 宽，把高度固化为 0 且不再有重算时机
const HEIGHT_SELECTOR = ".image-ad-con";
const { imgratio, height, getImgratio, getHeght } = useImgInfo(currentImageUrl.value, HEIGHT_SELECTOR, false);

// swiper 必须有显式高度，测量未就绪时传 0 会折叠内容区，先用屏宽估算顶上
const swiperHeight = computed(() => {
    if (height.value > 0) return height.value;
    if (!imgratio.value) return 0;
    return (uni.getWindowInfo().screenWidth - 20) * imgratio.value;
});

// getImageInfo 在部分平台会失败，此时图片 load 事件是比例的唯一来源；
// 比例只认首图，否则轮播切换背景图会带着高度一起跳
const handleImageLoad = async (event: any) => {
    if (imgratio.value > 0) return;
    const width = Number(event?.detail?.width);
    const imageHeight = Number(event?.detail?.height);
    if (width <= 0 || imageHeight <= 0) return;
    imgratio.value = imageHeight / width;
    await getHeght(HEIGHT_SELECTOR);
};

const windowResizeCallback = async () => {
    await getHeght(HEIGHT_SELECTOR);
};
uni.onWindowResize(windowResizeCallback);
onUnmounted(() => {
    uni.offWindowResize(windowResizeCallback);
});

const fixed = computed(() => {
    let value = false;
    if (props.module.bannerStyle.effectType == "ceiling" && scrollTop.value > bannerRectTop.value + logoRectHeight.value) {
        value = true;
    }
    return value;
});

const selectColor = computed(() => {
    return props.module.bannerContent.swiperPageColor || "#333";
});

const indicatorColor = computed(() => {
    return props.module.bannerContent.indicatorColor || "rgba(0, 0, 0, 0.2)";
});

const mode = computed(() => {
    return props.module.picPageType == 1 ? "default" : "dot";
});

const commonStyle = computed(() => {
    return formatCommonStyle(props.module.moduleStyle);
});

const swiperChange = (e: any) => {
    realIndex.value = e < picList.value.length ? e : 0;
};

const searchStyle = computed(() => {
    return `height: 100%; border-radius: ${props.module.bannerStyle?.searchRadius}px; background-color: ${props.module.bannerStyle?.searchBackgroundColor}`;
});
const searchTnputStyle = computed(() => {
    return `height: ${props.module.bannerStyle?.searchHeight}px; line-height: ${props.module.bannerStyle?.searchHeight}px; color: ${props.module.bannerStyle?.searchFontColor}; background-color: ${props.module.bannerStyle?.searchBackgroundColor}; --color: ${props.module.bannerStyle?.searchFontColor}`;
});

const instance = getCurrentInstance();
const bannerRectTop = ref(0);
const logoRectHeight = ref(0);
onMounted(async () => {
    // 先拿比例再测宽度，顺序颠倒会算出 0 高度
    await getImgratio(currentImageUrl.value);
    await getHeght(HEIGHT_SELECTOR);

    if (props.index == 0 && props.module.bannerStyle.effectType == "ceiling") {
        const bannerRes = await getElementRect(".banner", instance);
        if (bannerRes) {
            bannerRectTop.value = bannerRes.top;
        }
    }
    // 定位模式下拉取最近收货地址作为默认展示
    if (!isShopMode.value) {
        fetchLatestAddress();
    }
});
</script>

<style lang="scss" scoped>
// #ifdef MP
.banner-box {
    padding-top: 0 !important;
}
.banner {
    margin-top: 0 !important;
}
// #endif

.image-ad-con {
    position: relative;

    .indicator-box {
        position: absolute;
        bottom: 10px;
        left: 50%;
        transform: translateX(-50%);
        display: flex;
        align-items: center;

        &.type2 {
            .indicator-item {
                &.indicator-item-active {
                    background-color: v-bind(selectColor);
                    width: 16px;
                    transition: all 0.3s;
                    border-radius: 4px;
                }
            }
        }

        .indicator-item {
            width: 8px;
            height: 8px;
            border-radius: 50%;
            background-color: v-bind(indicatorColor);
            margin: 0 4px;

            &.indicator-item-active {
                background-color: v-bind(selectColor);
                transition: all 0.3s;
            }
        }
    }
}

.bannerA2 {
    position: relative;
}

.banner-bg {
    position: absolute;
    width: 100%;
    height: 100%;
    top: 0;
    z-index: 0;
    filter: blur(0);
    overflow: hidden;

    image {
        width: 100%;
        height: 100%;
        filter: blur(17px);
        transform: scale(1.4);
    }
}

.bg-gradient {
    position: absolute;
    left: 0;
    top: 0;
    width: 100%;
    height: 100%;
    overflow: hidden;
    background-image: linear-gradient(rgba(248, 248, 248, 0) 0%, rgba(248, 248, 248, 0) 50%, rgb(248, 248, 248) 100%);
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

.bar {
    position: relative;
    overflow: hidden;
    padding: 0px 0px 10px;
    z-index: 9;
}

.search-top {
    display: flex;
    flex-wrap: nowrap;
    justify-content: space-between;
    align-items: center;
    position: relative;
    column-gap: 10px;
    flex: 1;
    height: 100%;

    .area-select-box {
        font-weight: 600;
        font-size: 25rpx;
        display: flex;
        align-items: center;
        column-gap: 6rpx;
        max-width: 260rpx;

        &.is-disabled {
            cursor: default;
        }

        .area-select-text {
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
        }

        .area-switch-text {
            flex-shrink: 0;
            font-weight: 400;
            font-size: 22rpx;
            color: #999;
        }
    }

    .search {
        flex: 1;
        display: flex;
        flex-wrap: nowrap;
        justify-content: flex-start;
        align-items: center;
        border-radius: 20px;
        z-index: 2;
        background-color: #ffffffcc;
        color: #969799;
        overflow: hidden;
        border: 1.5px solid rgba(26, 26, 26, 1);
        column-gap: 4px;
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
    }
}
</style>
