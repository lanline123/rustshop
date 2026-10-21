<template>
    <div
        :class="
            'module-ad-con module-image_ad ' +
            ' ad-pic_type__' +
            ' ad-pic_page_type__' +
            module.picPageType +
            ' ad-radio_style__' +
            module.picRadioStyle +
            ' '
        "
        :style="frameFormat.boxPadding + frameFormat.boxPaddingTop + frameFormat.boxPaddingBottom + format.imgRadiusVar">
        <div class="module-ad-content" :style="frameFormat.backgroundColor + ' ' + frameFormat.boxRadius">
            <CommonTitle v-if="title?.showTitle" v-model="title"></CommonTitle>
            <div class="module-ad-empty empty-image_ad" v-if="module.picList.length == 0">
                <div class="image-empty-bg"></div>
                <div class="desc">{{ $t('点击添加模块图片广告') }}</div>
            </div>
            <template v-else>
                <div class="image-ad-warp" :style="frameFormat.innerPadding">
                    <div class="image-ad-con">
                        <template v-if="module.picList">
                            <template v-if="module.picType == 5">
                                <div class="image-square-layout">
                                    <div class="image-ad-item">
                                        <div class="item-content" :style="format.imgPadding">
                                            <div class="item-img-a" :url="module.picList[0]?.picLink">
                                                <img class="item-img" :src="imageFormat(module.picList[0]?.picUrl)" />
                                            </div>
                                        </div>
                                    </div>
                                    <div class="grid-layout-2">
                                        <div class="image-ad-item">
                                            <div class="item-content" :style="format.imgPadding">
                                                <div class="item-img-a" :url="module.picList[1]?.picLink">
                                                    <img class="item-img" :src="imageFormat(module.picList[1]?.picUrl)" />
                                                </div>
                                            </div>
                                        </div>
                                        <div class="image-ad-item">
                                            <div class="item-content" :style="format.imgPadding">
                                                <div class="item-img-a" :url="module.picList[2]?.picLink">
                                                    <img class="item-img" :src="imageFormat(module.picList[2]?.picUrl)" />
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </template>
                            <template v-else-if="module.picType == 7">
                                <div class="image-square-layout2">
                                    <div class="image-ad-item">
                                        <div class="item-content" :style="format.imgPadding">
                                            <div class="item-img-a" :url="module.picList[0]?.picLink">
                                                <img class="item-img" :src="imageFormat(module.picList[0]?.picUrl)" />
                                            </div>
                                        </div>
                                    </div>
                                    <div class="grid-layout-2">
                                        <div class="image-ad-item">
                                            <div class="item-content" :style="format.imgPadding">
                                                <div class="item-img-a" :url="module.picList[1]?.picLink">
                                                    <img class="item-img" :src="imageFormat(module.picList[1]?.picUrl)" />
                                                </div>
                                            </div>
                                        </div>
                                        <div class="image-ad-item">
                                            <div class="item-content" :style="format.imgPadding">
                                                <div class="item-img-a" :url="module.picList[2]?.picLink">
                                                    <img class="item-img" :src="imageFormat(module.picList[2]?.picUrl)" />
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </template>
                            <template v-else-if="module.picType == 6">
                                <div class="image-square-layout image-square-layout3">
                                    <div class="image-ad-item">
                                        <div class="item-content" :style="format.imgPadding">
                                            <div class="item-img-a" :url="module.picList[0]?.picLink">
                                                <img class="item-img" :src="imageFormat(module.picList[0]?.picUrl)" />
                                            </div>
                                        </div>
                                    </div>
                                    <div class="grid-layout-2">
                                        <div>
                                            <div class="image-ad-item">
                                                <div class="item-content" :style="format.imgPadding">
                                                    <div class="item-img-a" :url="module.picList[1]?.picLink">
                                                        <img class="item-img" :src="imageFormat(module.picList[1]?.picUrl)" />
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                        <div class="grid-layout-3">
                                            <div class="image-ad-item">
                                                <div class="item-content" :style="format.imgPadding">
                                                    <div class="item-img-a" :url="module.picList[2]?.picLink">
                                                        <img class="item-img" :src="imageFormat(module.picList[2]?.picUrl)" />
                                                    </div>
                                                </div>
                                            </div>
                                            <div class="image-ad-item">
                                                <div class="item-content" :style="format.imgPadding">
                                                    <div class="item-img-a" :url="module.picList[3]?.picLink">
                                                        <img class="item-img" :src="imageFormat(module.picList[3]?.picUrl)" />
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </template>
                            <template v-else>
                                <div :class="'image-square-layout-' + module.picType">
                                    <div class="image-ad-item" v-for="(pic, key) in module.picList" :key="key">
                                        <div class="item-content" :style="format.imgPadding">
                                            <div class="item-img-a" :url="pic.picLink ? pic.picLink.link : ''">
                                                <img class="item-img" :src="imageFormat(pic.picUrl)" />
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </template>
                        </template>
                    </div>
                </div>
            </template>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref, computed, watch, Ref } from "vue";
import { imageFormat } from "@/utils/format";
import { ModuleType, ModuleImageType } from "@/types/decorate/decorate.d";
import { mergeDefaultModule, defaultFrame, formatFrame, defaultTitle, CommonTitle } from "@/views/decorate/decorate/src/modules/";
const module = defineModel<ModuleType & ModuleImageType>("module") as Ref<ModuleType & ModuleImageType>;
const defaultModule = ref({
    picType: 1,
    picList: [],
    swiperPreView: 1, //轮播显示数量
    swiperPageColor: "",
    imgPadding: 0,
    imgRadius: 0,
    picPageType: 1,
    picRadioStyle: 1,
    isFluxWidth: 0,
    frame: defaultFrame,
    title: defaultTitle,
});
mergeDefaultModule(module.value, defaultModule.value);
const { frame, title } = module.value;
const frameFormat = computed(() => {
    return formatFrame(frame ?? {});
});
const format = computed(() => {
    return {
        imgPadding: "padding:" + module.value.imgPadding + "px;",
        imgRadiusVar: "--img-radius:" + (module.value.imgRadius ?? 0) + "px;",
    };
});
</script>
<style lang="less" scoped>
.item-img-a {
    border-radius: var(--img-radius, 0px);
    overflow: hidden;
}
.image-square-layout-1 {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
}
.image-square-layout-2 {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
}
.image-square-layout-3 {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
}
.image-square-layout-4 {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    grid-template-rows: repeat(2, 1fr);

    .image-ad-item:nth-child(2) {
        grid-column: 1 / 2;
    }
    .image-ad-item:nth-child(3) {
        grid-row: 1 / 2;
        grid-column: 2 / 2;
    }
}

.image-square-layout {
    display: grid;
    grid-template-columns: repeat(2, 1fr);

    .item-content {
        height: 100%;
    }
    .item-img-a {
        height: 100%;
    }
    .item-img {
        height: 100%;
    }

    & .grid-layout-2 {
        display: grid;
        grid-template-rows: repeat(2, 1fr);
    }

    &.image-square-layout3 {
        & .grid-layout-3 {
            display: grid;
            grid-template-columns: repeat(2, 1fr);
        }
    }
}

.image-square-layout2 {
    & .grid-layout-2 {
        display: grid;
        grid-template-columns: repeat(2, 1fr);
    }
}
</style>
