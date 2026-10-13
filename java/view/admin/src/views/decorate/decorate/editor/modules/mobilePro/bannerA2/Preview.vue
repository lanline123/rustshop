<template>
    <div class="module-ad-con ad-pic_type__2">
        <div class="banner-bg">
            <img :src="module.bannerContent.picList[realIndex].picUrl" />
        </div>
        <div class="bg-gradient"></div>
        <div
            :style="
                commonStyle.moduleStyle.boxPaddingBottom +
                commonStyle.moduleStyle.boxPaddingTop +
                commonStyle.moduleStyle.boxPaddingBottom +
                commonStyle.moduleStyle.boxPadding +
                commonStyle.moduleStyle.backgroundSize +
                commonStyle.moduleStyle.backgroundImage
            "
        >
            <div class="module-ad-content" :style="`margin-top:${module.moduleStyle?.bannerMarginTop}px;`">
                <div class="Banner">
                    <div class="bar">
                        <div class="search-top">
                            <div class="area-select-box">
                                <div>北京市</div>
                                <span class="iconfont-h5 icon-a-gengduo20" />
                            </div>
                            <div class="search" :style="searchStyle" v-if="module.searchContent?.showSearch">
                                <div class="search-icon" :style="`color:${module.bannerStyle?.searchIconColor}`">
                                    <i class="iconfont-h5 icon-sousuo1"></i>
                                </div>
                                <form action="" class="search-form">
                                    <div class="search-input" :style="searchTnputStyle">
                                        <div class="search-placeholder">{{ module.searchContent?.searchText }}</div>
                                    </div>
                                </form>
                            </div>
                        </div>
                    </div>
                    <div class="image-ad-warp">
                        <div
                            class="image-ad-con"
                            :class="`pagination-bullet${module.bannerContent.picPageType}`"
                            :style="`border-radius: ${module.bannerContent.picRadius}px;overflow: hidden;`"
                        >
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
    </div>
</template>

<script lang="ts" setup>
import { ref, computed } from "vue";
import { mergeDefaultModule, formatCommonStyle } from "@/views/decorate/decorate/src/modules/index";
import { Swiper } from "@/components/swiper";
import { imageFormat } from "@/utils/format";
import { bannerA2Defaults } from "./index";
import type { BannerA2Module } from "./types";

const props = defineProps<{ module: BannerA2Module }>();
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
mergeDefaultModule(module, bannerA2Defaults);

const swiperPageColor = computed(() => {
    return module.bannerContent.swiperPageColor || "#fff";
});

const indicatorColor = computed(() => {
    return module.bannerContent.indicatorColor || "rgba(0, 0, 0, 0.2)";
});

const searchStyle = computed(() => {
    return `height: ${module.bannerStyle?.searchHeight}px; border-radius: ${module.bannerStyle?.searchRadius}px; background-color: ${module.bannerStyle?.searchBackgroundColor}`;
});
const searchTnputStyle = computed(() => {
    return `height: ${module.bannerStyle?.searchHeight}px; line-height: ${module.bannerStyle?.searchHeight}px; color: ${module.bannerStyle?.searchFontColor}; background-color: ${module.bannerStyle?.searchBackgroundColor};`;
});
</script>

<style lang="less" scoped>
.banner-bg {
    position: absolute;
    width: 100%;
    height: 100%;
    top: 0;
    z-index: 0;
    filter: blur(0);
    overflow: hidden;

    img {
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

:deep(.swiper-pagination-bullet-active) {
    background: v-bind("swiperPageColor") !important;
}

:deep(.swiper-pagination-bullet) {
    background: v-bind("indicatorColor");
    opacity: 1;
}

.pagination-bullet2 {
    :deep(.swiper-pagination-bullet-active) {
        width: 14px !important;
        border-radius: 5px;
    }
}

.word-list::-webkit-scrollbar {
    display: none;
    height: 0;
}
.bar {
    position: relative;
    overflow: hidden;
    margin: 0px 3px 10px;
}
.search-top {
    display: flex;
    flex-wrap: nowrap;
    justify-content: space-between;
    position: relative;
    align-items: center;
    column-gap: 10px;

    .area-select-box {
        font-weight: 600;
        font-size: 14px;
        display: flex;
        align-items: center;
        column-gap: 5px;

        .icon-a-gengduo20 {
            font-size: 9px;
            transform: rotate(90deg);
        }
    }

    .search {
        flex: 1;
        display: flex;
        flex-wrap: nowrap;
        justify-content: flex-start;
        align-items: center;
        height: 36px;
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
