<template>
    <TotalContainer :module="module.frame" :class="'module-ad-con module-image_ad'">
        <Title :module="module.title"></Title>
        <div v-if="!module.picList || module.picList.length === 0" class="module-ad-empty empty-image_ad">
            <div class="image-empty-bg"></div>
            <div class="desc">点击添加图片广告</div>
        </div>
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
import { computed } from "vue";
import { TotalContainer, OneRowX, LateralSlip, Carousel } from "@/components/decorate/index";
import Title from "@/views/decorate/decorate/src/modules/pcCustomPage/components/Title.vue";
import { mergeDefaultModule, formatFrame } from "@/views/decorate/decorate/src/modules/index";
import { pcImageAdDefaults } from "./index";
import type { PcImageAdModule } from "./types";

const props = defineProps<{ module: PcImageAdModule }>();
const module = props.module;
// 旧数据缺字段时烧录默认值（原地写回 store 模块对象，与旧 View.vue 行为一致，契约）
mergeDefaultModule(module, pcImageAdDefaults);

const frameFormat = computed(() => {
    return formatFrame(module.frame ?? {});
});
</script>
<style scoped lang="less">
.image-ad-warp {
    box-sizing: border-box;
    width: 100%;
}

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
