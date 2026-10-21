<template>
    <view
        :class="
            'module-ad-con module-image_ad ' + ' ad-pic_type__' + ' ad-pic_page_type__' + module.picPageType + ' ad-radio_style__' + module.picRadioStyle + ' '
        "
        :style="frameFormat.boxPadding + frameFormat.boxPaddingTop + frameFormat.boxPaddingBottom + format.imgRadiusVar"
    >
        <view class="module-ad-content" :style="frameFormat.backgroundColor + ' ' + frameFormat.boxRadius">
            <commonTitle v-if="module.title.showTitle == 1" :module="module.title" />
            <template v-if="module.picList.length > 0">
                <view class="image-ad-warp" :style="frameFormat.innerPadding">
                    <view class="image-ad-con">
                        <template v-if="module.picList">
                            <template v-if="module.picType == 5">
                                <view class="image-square-layout image-square-layout-height2">
                                    <view class="image-ad-item">
                                        <view
                                            class="item-content"
                                            :style="format.imgPadding"
                                            @click="module.picList[0].picLink && redirect({ url: urlFormat(module.picList[0].picLink) })"
                                        >
                                            <imageBox :src="module.picList[0].picUrl" />
                                        </view>
                                    </view>
                                    <view class="grid-layout-2">
                                        <view class="image-ad-item">
                                            <view
                                                class="item-content"
                                                :style="format.imgPadding"
                                                @click="module.picList[1].picLink && redirect({ url: urlFormat(module.picList[1].picLink) })"
                                            >
                                                <imageBox :src="module.picList[1].picUrl" />
                                            </view>
                                        </view>
                                        <view class="image-ad-item">
                                            <view
                                                class="item-content"
                                                :style="format.imgPadding"
                                                @click="module.picList[2].picLink && redirect({ url: urlFormat(module.picList[2].picLink) })"
                                            >
                                                <imageBox :src="module.picList[2].picUrl" />
                                            </view>
                                        </view>
                                    </view>
                                </view>
                            </template>
                            <template v-else-if="module.picType == 7">
                                <view class="image-square-layout2">
                                    <view class="image-ad-item">
                                        <view
                                            class="item-content"
                                            :style="format.imgPadding"
                                            @click="module.picList[0].picLink && redirect({ url: urlFormat(module.picList[0].picLink) })"
                                        >
                                            <imageBox :src="module.picList[0].picUrl" />
                                        </view>
                                    </view>
                                    <view class="grid-layout-2">
                                        <view class="image-ad-item">
                                            <view
                                                class="item-content"
                                                :style="format.imgPadding"
                                                @click="module.picList[1].picLink && redirect({ url: urlFormat(module.picList[1].picLink) })"
                                            >
                                                <imageBox :src="module.picList[1].picUrl" />
                                            </view>
                                        </view>
                                        <view class="image-ad-item">
                                            <view
                                                class="item-content"
                                                :style="format.imgPadding"
                                                @click="module.picList[2].picLink && redirect({ url: urlFormat(module.picList[2].picLink) })"
                                            >
                                                <imageBox :src="module.picList[2].picUrl" />
                                            </view>
                                        </view>
                                    </view>
                                </view>
                            </template>
                            <template v-else-if="module.picType == 6">
                                <view class="image-square-layout image-square-layout3 image-square-layout-height2">
                                    <view class="image-ad-item">
                                        <view
                                            class="item-content"
                                            :style="format.imgPadding"
                                            @click="module.picList[0].picLink && redirect({ url: urlFormat(module.picList[0].picLink) })"
                                        >
                                            <imageBox :src="module.picList[0].picUrl" />
                                        </view>
                                    </view>
                                    <view class="grid-layout-2">
                                        <view>
                                            <view class="image-ad-item">
                                                <view
                                                    class="item-content"
                                                    :style="format.imgPadding"
                                                    @click="module.picList[1].picLink && redirect({ url: urlFormat(module.picList[1].picLink) })"
                                                >
                                                    <imageBox :src="module.picList[1].picUrl" />
                                                </view>
                                            </view>
                                        </view>
                                        <view class="grid-layout-3">
                                            <view class="image-ad-item">
                                                <view
                                                    class="item-content"
                                                    :style="format.imgPadding"
                                                    @click="module.picList[2].picLink && redirect({ url: urlFormat(module.picList[2].picLink) })"
                                                >
                                                    <imageBox :src="module.picList[2].picUrl" />
                                                </view>
                                            </view>
                                            <view class="image-ad-item">
                                                <view
                                                    class="item-content"
                                                    :style="format.imgPadding"
                                                    @click="module.picList[3].picLink && redirect({ url: urlFormat(module.picList[3].picLink) })"
                                                >
                                                    <imageBox :src="module.picList[3].picUrl" />
                                                </view>
                                            </view>
                                        </view>
                                    </view>
                                </view>
                            </template>
                            <template v-else>
                                <view :class="'image-square-layout-' + module.picType + ' image-square-layout-height1'">
                                    <view v-for="(pic, key) in module.picList" :key="key" class="image-ad-item">
                                        <view class="item-content" :style="format.imgPadding" @click="pic.picLink && redirect({ url: urlFormat(pic.picLink) })">
                                            <view class="item-img-a">
                                                <tig-image class="item-img" :src="pic.picUrl" mode="widthFix" />
                                            </view>
                                        </view>
                                    </view>
                                </view>
                            </template>
                        </template>
                    </view>
                </view>
            </template>
        </view>
    </view>
</template>
<script lang="ts" setup>
import { computed } from "vue";
import { urlFormat } from "@/utils/format";
import { formatFrame } from "@/components/modules";
import commonTitle from "@/pages/customPage/modules/commonTitle/index.vue";
import imageBox from "./src/imageBox.vue";
import { redirect } from "@/utils";
const props = defineProps({
    module: {
        type: Object,
        default: () => {}
    }
});
const { frame } = props.module;
const frameFormat = computed(() => {
    return formatFrame(frame ?? {});
});
const format = computed(() => {
    return {
        imgPadding: "padding:" + props.module.imgPadding + "px;",
        imgRadiusVar: "--img-radius:" + (props.module.imgRadius ?? 0) + "px;"
    };
});
</script>
<style lang="scss" scoped>
.module-ad-content {
    overflow: hidden;
}
.image-ad-item {
    height: 100%;
    width: 100%;
}
.item-content {
    height: 100%;
    width: 100%;
    overflow: hidden;
}
.item-img-a {
    height: 100%;
    width: 100%;
    border-radius: var(--img-radius, 0px);
    overflow: hidden;
}
.item-img {
    // height: 100%;
    width: 100%;
}
.image-square-layout-height1 {
    height: auto;
    width: 100%;
}
.image-square-layout-height2 {
    height: auto;
    width: 100%;
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
