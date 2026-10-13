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
                <div class="desc">点击添加图文导航</div>
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
import { ref, computed, watchEffect } from "vue";
import { Swiper } from "@/components/swiper";
import { imageFormat } from "@/utils/format";
import { mergeDefaultModule, formatFrame } from "@/views/decorate/decorate/src/modules/index";
import { imageNavDefaults } from "./index";
import type { ImageNavModule } from "./types";

import "swiper/css";
import "swiper/css/pagination"; // 轮播图底面的小圆点
// import 'swiper/css/navigation' // 轮播图两边的左右箭头
const props = defineProps<{ module: ImageNavModule }>();
const module = props.module;
// 旧数据缺字段时烧录默认值（原地写回 store 模块对象，与旧 View.vue 行为一致，契约）
mergeDefaultModule(module, imageNavDefaults);
const { frame } = module;
const frameFormat = computed(() => {
    return formatFrame(frame ?? {});
});
const swiperPageColor = computed(() => {
    return module.swiperPageColor || "#333";
});
const imgPadding = computed(() => {
    return module.imgPadding;
});
const rowNum = computed(() => {
    return module.rowNum;
});
const colNum = computed(() => {
    return module.colNum;
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
    if (module.navType === 1) return;
    swiperList.value = [];
    let itemNum;

    if (module.rowNum && module.colNum) {
        itemNum = module.rowNum * module.colNum;
    } else {
        itemNum = 0;
    }

    let swiperListNum = Math.ceil(module.picList.length / itemNum);
    for (let index = 0; index < swiperListNum; index++) {
        swiperList.value.push(module.picList.slice(index * itemNum, (index + 1) * itemNum));
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
