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
                            <div class="area-select-box" :class="{ 'is-disabled': !canSwitch }">
                                <div class="area-select-text" :style="{ color: module.bannerStyle?.areaTextColor || '' }">{{ areaText }}</div>
                                <span v-if="canSwitch" class="area-switch-text" :style="{ color: module.bannerStyle?.areaSwitchColor || '' }">[切换]</span>
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
import { ref, computed, Ref, onMounted } from "vue";
import { ModuleType, ModuleBannerType } from "@/types/decorate/decorate.d";
import { mergeDefaultModule, defaultModuleStyle, formatCommonStyle, gradientMap } from "@/views/decorate/decorate/src/modules/";
import { Swiper } from "@/components/swiper";
import { imageFormat } from "@/utils/format";
import { useI18n } from "vue-i18n"
const { t } = useI18n()

// 进店模式：1-网店可切换，2-网店不可切换，3-定位可切换，4-定位不可切换
const storeEntryRule = ref<number>(1);
// 网店模式（模式一、二）显示门店名称，定位模式（模式三、四）显示定位/收货地址
const isShopMode = computed(() => storeEntryRule.value === 1 || storeEntryRule.value === 2);
// 模式一、三可切换，模式二、四不可切换
const canSwitch = computed(() => storeEntryRule.value === 1 || storeEntryRule.value === 3);
const areaText = computed(() => (isShopMode.value ? "门店名称" : "北京市"));
onMounted(async () => {
});
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

const picList = [
    {
        picId: 1414,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1731044101tXK31aimefqoUsJSrM.jpg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/1731301937REbUnsSmPehWiBMaFd.webp",
        picName: "FjfhKDiUzqW_lqfJqncKt3QJplx4"
    },
    {
        picId: 1415,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1731044101j8COoqtfDJCWrSts6b.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/1731301941uaSjIShOBNL2VBSr0y.webp",
        picName: "Fv5iP2IHByVFUlGrLkXtPkClndla"
    },
    {
        picId: 1413,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1731044101D4owQpnWiu0DcTqmKO.jpg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/1731301927a0N31LV7yy1i6Zlk4T.webp",
        picName: "FhHfxuZP5Z5P_Hc7SHBzvm6RwHud"
    }
];
const defaultModule = ref({
    bannerContent: {
        //轮播内容
        picList: picList, //图片列表
        picRadius: 0, //图片圆角

        picPageType: 1, //指示器样式
        swiperPageColor: "", //指示器选中颜色
        indicatorColor: "" //指示器默认颜色
    },
    searchContent: {
        //搜索框设置
        showSearch: 1, //是否显示搜索框 1显示 0不显示
        searchText: t('搜索商品') //搜索框文字
    },
    // navContent: {
    //     //导航文字设置
    //     // showNav: 1, //是否显示导航 1显示 0不显示
    //     navList: navList ///导航列表
    // },
    bannerStyle: {
        //样式设置
        effectType: "ceiling", //搜索框/导航效果 ceiling 滚动至顶部固定  default 正常模式
        searchBackgroundColor: "#ffffff", //搜索框背景颜色
        searchFontColor: "#999999", //搜索框文本颜色
        searchIconColor: "#999999", //搜索框图标颜色
        areaTextColor: "#1a1a1a", //门店/定位文字颜色
        areaSwitchColor: "#999999", //切换文字颜色
        searchText: t('搜索'), //搜索框文字
        searchHeight: 34, //搜索框高度
        searchRadius: 20 //搜索框圆角
    },
    moduleStyle: defaultModuleStyle // 模块样式
});
const commonStyle = computed(() => {
    return formatCommonStyle(module.value.moduleStyle!);
});
mergeDefaultModule(module.value, defaultModule.value);
// 顶层合并不会补 bannerStyle 内部缺失字段，这里对样式对象再做一次非空兜底
mergeDefaultModule(module.value.bannerStyle, defaultModule.value.bannerStyle);

const swiperPageColor = computed(() => {
    return module.value.bannerContent.swiperPageColor || "#fff";
});

const indicatorColor = computed(() => {
    return module.value.bannerContent.indicatorColor || "rgba(0, 0, 0, 0.2)";
});

const searchStyle = computed(() => {
    return `height: ${module.value.bannerStyle?.searchHeight}px; border-radius: ${module.value.bannerStyle?.searchRadius}px; background-color: ${module.value.bannerStyle?.searchBackgroundColor}`;
});
const searchTnputStyle = computed(() => {
    return `height: ${module.value.bannerStyle?.searchHeight}px; line-height: ${module.value.bannerStyle?.searchHeight}px; color: ${module.value.bannerStyle?.searchFontColor}; background-color: ${module.value.bannerStyle?.searchBackgroundColor};`;
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
        max-width: 130px;

        .area-select-text {
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
        }

        .area-switch-text {
            flex-shrink: 0;
            font-weight: 400;
            font-size: 12px;
            color: #999;
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
