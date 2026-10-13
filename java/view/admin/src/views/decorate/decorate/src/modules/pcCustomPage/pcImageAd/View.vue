<template>
    <TotalContainer :module="module.frame" :class="'module-ad-con module-image_ad'">
        <Title :module="module.title"></Title>
        <!-- 占位显示：没有图片时 -->
        <div v-if="!module.picList || module.picList.length === 0" class="module-ad-empty empty-image_ad">
            <div class="image-empty-bg"></div>
            <div class="desc">{{ $t('点击添加图片广告') }}</div>
        </div>
        <!-- 有图片时显示图片列表 -->
        <div v-else class="image-ad-warp" :style="frameFormat.innerPadding">
            <OneRowX :size="1" v-if="module.picType == 1" :module="module"></OneRowX>
            <Carousel v-if="module.picType == 2" :module="module"></Carousel>
            <LateralSlip size="big" v-if="module.picType == 3" :module="module"></LateralSlip>
            <LateralSlip size="small" v-if="module.picType == 4" :module="module"></LateralSlip>
            <OneRowX :size="2" v-if="module.picType == 5" :module="module"></OneRowX>
            <OneRowX :size="3" v-if="module.picType == 6" :module="module"></OneRowX>
            <OneRowX :size="4" v-if="module.picType == 7" :module="module"></OneRowX>
        </div>
    </TotalContainer>
</template>
<script setup lang="ts">
//pc端装修图片广告组件
import { computed, ref, Ref } from "vue";
import { TotalContainer, OneRowX, LateralSlip, Carousel } from "@/components/decorate/index";
import Title from "@/views/decorate/decorate/src/modules/pcCustomPage/components/Title.vue";
import { ModuleType, ModuleImageType } from "@/types/decorate/decorate.d";
import { mergeDefaultModule, defaultFrame, formatFrame, defaultTitle } from "@/views/decorate/decorate/src/modules/";

// 使用 defineModel 实现双向绑定，确保设置生效
const module = defineModel<ModuleType & ModuleImageType>("module", { default: () => ({}) }) as Ref<ModuleType & ModuleImageType>;

// 合并默认值，确保 frame 和 title 有初始值
const defaultModule = ref({
    picType: 1,
    picList: [],
    swiperPreView: 1,
    swiperPageColor: "",
    imgPadding: 0,
    picPageType: 1,
    picRadioStyle: 1,
    isFluxWidth: 0,
    frame: defaultFrame,
    title: defaultTitle
});
mergeDefaultModule(module.value, defaultModule.value);

const { frame } = module.value;

// 格式化 frame 设置
const frameFormat = computed(() => {
    return formatFrame(frame ?? {});
});
</script>
<style scoped lang="less">
.image-ad-warp {
    box-sizing: border-box;
    width: 100%;
}

/* 占位图片样式 */
.module-ad-empty {
    padding-top: 30px;
    padding-bottom: 50px;
    text-align: center;
    background-color: #f2f4f6;

    .image-empty-bg {
        background: url("@/views/decorate/decorate/src/img/empty-img-bg.png") no-repeat center center;
        background-size: 100% auto;
        width: 100px;
        margin: 0 auto 10px;
        height: 100px;
    }

    .desc {
        text-align: center;
        color: #888;
        font-size: 14px;
    }
}
</style>
