<template>
    <div
        :class="
            'module-ad-con module-image_ad ' +
            ' ad-pic_type__' +
            module.picType +
            ' ad-pic_page_type__' +
            module.picPageType +
            ' ad-radio_style__' +
            module.picRadioStyle +
            ' '
        "
        :style="frameFormat.boxPadding + frameFormat.boxPaddingTop + frameFormat.boxPaddingBottom"
    >
        <div class="module-ad-content" :style="frameFormat.backgroundColor + ' ' + frameFormat.boxRadius">
            <CommonTitle v-if="title?.showTitle" v-model="title"></CommonTitle>
            <div class="module-ad-empty empty-image_ad" v-if="module.picList.length == 0">
                <div class="image-empty-bg"></div>
                <div class="desc">点击添加图片广告</div>
            </div>
            <template v-if="module.picType == 2">
                <div class="image-ad-warp" :style="frameFormat.innerPadding">
                    <div class="image-ad-con">
                        <template v-if="module.swiperPreView && module.swiperPreView == 1">
                            <Swiper :swiperOption="swiperOption" v-model="module.picList">
                                <template v-slot:default="{ item }">
                                    <div class="image-ad-item">
                                        <div class="item-content" :style="format.imgPadding">
                                            <div class="item-img-a" :url="item.picLink ? item.picLink.link : ''">
                                                <img class="item-img" :src="imageFormat(item.picUrl)" />
                                            </div>
                                        </div>
                                    </div>
                                </template>
                            </Swiper>
                        </template>
                        <template v-else>
                            <Swiper :swiperOption="swiperOption" v-model="swiperList">
                                <template v-slot:default="{ item }">
                                    <div class="swiper-main">
                                        <div v-for="(subItem, index) in item" :key="index">
                                            <div class="image-ad-item">
                                                <div class="item-content" :style="format.imgPadding">
                                                    <div class="item-img-a">
                                                        <img class="item-img" :src="imageFormat(subItem.picUrl)" />
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </template>
                            </Swiper>
                        </template>
                    </div>
                </div>
            </template>
            <template v-else>
                <div class="image-ad-warp" :style="frameFormat.innerPadding">
                    <div class="image-ad-con">
                        <template v-if="module.picList" v-for="(pic, key) in module.picList">
                            <div class="image-ad-item">
                                <div class="item-content" :style="format.imgPadding">
                                    <div class="item-img-a" :url="pic.picLink ? pic.picLink.link : ''">
                                        <img class="item-img" :src="imageFormat(pic.picUrl)" />
                                    </div>
                                </div>
                            </div>
                        </template>
                    </div>
                </div>
            </template>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref, computed, watchEffect } from "vue";
import { Swiper } from "@/components/swiper";
import { imageFormat } from "@/utils/format";
import { mergeDefaultModule, formatFrame, CommonTitle } from "@/views/decorate/decorate/src/modules/index";
import { imageAdDefaults } from "./index";
import type { ImageAdModule } from "./types";

import "swiper/css";
import "swiper/css/pagination"; // 轮播图底面的小圆点

const props = defineProps<{ module: ImageAdModule }>();
const module = props.module;
// 旧数据缺字段时烧录默认值（原地写回 store 模块对象，与旧 View.vue 行为一致，契约）
mergeDefaultModule(module, imageAdDefaults);
const { frame, title } = module;
const frameFormat = computed(() => {
    return formatFrame(frame ?? {});
});
const format = computed(() => {
    return {
        imgPadding: "padding:" + module?.imgPadding + "px;"
    };
});
const swiperPageColor = computed(() => {
    return module.swiperPageColor || "#333";
});
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
const columns = computed(() => {
    return module.swiperPreView || 1;
});
const imgPadding = computed(() => {
    return module.imgPadding;
});
const swiperList = ref<any[]>([]);
const getSwiperList = () => {
    swiperList.value = [];
    let itemNum = module.swiperPreView || 1;
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

.swiper-main {
    display: grid;
    grid-template-columns: repeat(v-bind("columns"), 1fr);
    grid-gap: 0 v-bind("imgPadding + 'px'");
    box-sizing: border-box;
    width: 100%;
    padding-bottom: 10px;
}

:deep(.swiper-main){
    padding-bottom: 0;
}
</style>
