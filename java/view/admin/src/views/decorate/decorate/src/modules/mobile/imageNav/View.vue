<template>
    <div
        :class="
            'img-nav-pic module-ad-con module-image_ad ad-pic_type__2' +
            ' ad-pic_page_type__' +
            module.picPageType +
            ' ad-radio_style__' +
            module.radioStyle +
            ' '
        "
        :style="frameFormat.boxPadding + frameFormat.boxPaddingTop + frameFormat.boxPaddingBottom"
    >
        <div class="module-ad-content" :style="frameFormat.innerPadding + frameFormat.backgroundColor + frameFormat.boxRadius">
            <div class="module-ad-empty empty-image_ad" v-if="module.picList.length === 0">
                <div class="image-empty-bg"></div>
                <div class="desc">{{ $t('点击添加图文导航') }}</div>
            </div>
            <template v-else>
                <template v-if="module.navType === 1">
                    <div class="imagenav-main">
                        <div class="imagenav-main-item" v-for="(item, index) in module.picList" :key="index">
                            <img
                                :class="{ 'img-height': module.navStyle === 2 }"
                                v-if="module.navStyle === 2 || module.navStyle === 1"
                                class="imagenav-item-img img-radius"
                                :src="imageFormat(item.picUrl)"
                                alt=""
                            />
                            <div v-if="module.navStyle === 3 || module.navStyle === 1" class="imagenav-item-text">{{ item.picTitle }}</div>
                        </div>
                    </div>
                </template>
                <template v-else>
                    <div class="image-ad-warp">
                        <div class="image-ad-con">
                            <Swiper :swiperOption="swiperOption" v-model="swiperList" :swiper-page-color="module.swiperPageColor">
                                <template v-slot:default="{ item }">
                                    <div class="swiper-main">
                                        <div class="swiper-item" v-for="(subItem, index) in item" :key="index">
                                            <img
                                                class="img-radius"
                                                v-if="module.navStyle === 2 || module.navStyle === 1"
                                                :src="imageFormat(subItem.picUrl)"
                                            />
                                            <div v-if="module.navStyle === 3 || module.navStyle === 1" class="imagenav-item-text">
                                                {{ subItem.picTitle }}
                                            </div>
                                        </div>
                                    </div>
                                </template>
                            </Swiper>
                        </div>
                    </div>
                </template>
            </template>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref, computed, watchEffect, watch, Ref } from "vue";
import { Swiper } from "@/components/swiper";
import { imageFormat } from "@/utils/format";
import { ModuleType, ModuleImageType } from "@/types/decorate/decorate.d";
import { mergeDefaultModule, defaultFrame, formatFrame } from "@/views/decorate/decorate/src/modules/";
// 在modules加入要使用的模块
import "swiper/css";
import "swiper/css/pagination"; // 轮播图底面的小圆点
// import 'swiper/css/navigation' // 轮播图两边的左右箭头
const module = defineModel<ModuleType & ModuleImageType>("module") as Ref<ModuleType & ModuleImageType>;

const defaultModule = ref({
    navType: 1,
    navStyle: 1,
    rowNum: 4,
    colNum: 2,
    picList: [],
    imgPadding: 8,
    radioStyle: 1,
    picPageType: 1,
    swiperPageColor: "",
    frame: defaultFrame
});
mergeDefaultModule(module.value, defaultModule.value);
const { frame } = module.value;
const frameFormat = computed(() => {
    return formatFrame(frame ?? {});
});
const swiperPageColor = computed(() => {
    return module.value.swiperPageColor || "#333";
});
const imgPadding = computed(() => {
    return module.value.imgPadding;
});
const rowNum = computed(() => {
    return module.value.rowNum;
});
const colNum = computed(() => {
    return module.value.colNum;
});
const swiperList = ref<any[]>([]);
const swiperOption = ref<any>({
    autoplay: {
        delay: 3000,
        disableOnInteraction: false, //用户操作swiper之后，是否禁止autoplay
        pauseOnMouseEnter: true //鼠标置于swiper是否时暂停自动切换
    },
    pagination: {
        clickable: true
    }
    // slidesPerView: 4, // 一屏显示的slide个数  'auto'
});

const getSwiperList = () => {
    if (module.value.navType === 1) return;
    swiperList.value = [];
    let itemNum;

    if (module.value.rowNum && module.value.colNum) {
        itemNum = module.value.rowNum * module.value.colNum;
    } else {
        itemNum = 0;
    }

    let swiperListNum = Math.ceil(module.value.picList.length / itemNum);
    for (let index = 0; index < swiperListNum; index++) {
        swiperList.value.push(module.value.picList.slice(index * itemNum, (index + 1) * itemNum));
    }
};
watchEffect(() => {
    getSwiperList();
});
</script>
<style lang="less" scoped>
:deep(.swiper-pagination-bullet-active) {
    background: v-bind("swiperPageColor") !important;
}

.ad-radio_style__2 {
    .imagenav-main,
    .swiper-main {
        .img-radius {
            border-radius: 8px;
            overflow: hidden;
            display: block;
        }
    }
}

.imagenav-main {
    display: grid;
    grid-template-columns: repeat(v-bind("rowNum"), 1fr);
    grid-template-rows: repeat(v-bind("colNum"), 1fr);
    grid-gap: 0 v-bind("imgPadding + 'px'");
    box-sizing: border-box;

    .imagenav-main-item {
        margin-top: 8px;
        margin-bottom: 2px;
        padding-bottom: 6px;

        .imagenav-item-img {
            width: 100%;
            height: inherit;
        }
        .img-height {
            height: inherit;
        }

        .imagenav-item-text {
            font-size: inherit;
            text-align: center;
            line-height: 20px;
            height: 30%;
        }
    }
}

.swiper-main {
    display: grid;
    grid-template-columns: repeat(v-bind("rowNum"), 1fr);
    grid-gap: 0 v-bind("imgPadding + 'px'");
    box-sizing: border-box;
    width: 100%;
    padding-bottom: 10px;

    .swiper-item {
        margin-top: 8px;
        margin-bottom: 2px;
        padding-bottom: 6px;
        img {
            width: 100%;
            height: inherit;
        }
        div {
            font-size: inherit;
            text-align: center;
            line-height: 20px;
            height: 30%;
        }
    }
}
</style>
