<template>
    <TotalContainer
        :module="totalContainerModule"
        :class="
            'module-ad-con module-image_nav ad-nav_type__' +
            module.navType +
            ' ad-nav_style__' +
            module.navStyle +
            ' ad-row_num__' +
            module.rowNum +
            ' ad-col_num__' +
            module.colNum +
            ' ad-pic_page_type__' +
            module.picPageType +
            ' ad-radio_style__' +
            module.radioStyle
        "
    >
        <Title :module="module.title"></Title>
        <!-- 占位显示：没有图片时 -->
        <div v-if="!module.picList || module.picList.length === 0" class="module-ad-empty empty-image_nav">
            <div class="image-empty-bg"></div>
            <div class="desc">{{ $t('点击添加图文导航') }}</div>
        </div>
        <!-- 有图片时显示导航列表 -->
        <template v-else>
            <XRowX v-if="module.navType == 1" :module="module"></XRowX>
            <CarouselNav v-if="module.navType == 2" :module="module"></CarouselNav>
        </template>
    </TotalContainer>
</template>

<script lang="ts" setup>
//pc端装修图文导航组件
import { computed, Ref, ref } from "vue";
import { TotalContainer, XRowX, CarouselNav } from "@/components/decorate/index";
import Title from "@/views/decorate/decorate/src/modules/pcCustomPage/components/Title.vue";
import { defaultFrame, mergeDefaultModule } from "../..";
import { ModuleType, ModuleImageType } from "@/types/decorate/decorate";


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

const totalContainerModule = computed(() => {
    return {
        ...module.value?.frame
    };
});

const swiperPageColor = computed(() => {
    return module.value.swiperPageColor || "#333";
});
</script>
<style lang="less" scoped>
:deep(.swiper-pagination-bullet-active) {
    background: v-bind("swiperPageColor") !important;
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
