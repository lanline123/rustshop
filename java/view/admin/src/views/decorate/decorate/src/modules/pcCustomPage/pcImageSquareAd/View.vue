<template>
    <TotalContainer :module="module.frame" :class="'module-ad-con module-image_square_ad'">
        <Title :module="module.title"></Title>
        <!-- 占位显示：没有图片时 -->
        <div v-if="!module.picList || module.picList.length === 0" class="module-ad-empty empty-image_square_ad">
            <div class="image-empty-bg"></div>
            <div class="desc">{{ $t('点击添加图片方块广告') }}</div>
        </div>
        <!-- 有图片时显示图片列表 -->
        <template v-else-if="module.picList && module.picList.length > 0">
            <div class="image-list">
                <template v-if="module.picList">
                    <template v-if="module.picType == 5">
                        <div class="image-square-layout image-square-layout-height2">
                            <div class="image-ad-item">
                                <div v-if="module.picList[0]?.picUrl" class="item-content" :style="format.imgPadding">
                                    <imageBox :src="module.picList[0].picUrl" />
                                </div>
                            </div>
                            <div class="grid-layout-2">
                                <div class="image-ad-item">
                                    <div v-if="module.picList[1]?.picUrl" class="item-content" :style="format.imgPadding">
                                        <imageBox :src="module.picList[1].picUrl" />
                                    </div>
                                </div>
                                <div class="image-ad-item">
                                    <div v-if="module.picList[2]?.picUrl" class="item-content" :style="format.imgPadding">
                                        <imageBox :src="module.picList[2].picUrl" />
                                    </div>
                                </div>
                            </div>
                        </div>
                    </template>
                    <template v-else-if="module.picType == 7">
                        <div class="image-square-layout2">
                            <div class="image-ad-item">
                                <div v-if="module.picList[0]?.picUrl" class="item-content" :style="format.imgPadding">
                                    <imageBox :src="module.picList[0].picUrl" />
                                </div>
                            </div>
                            <div class="grid-layout-2">
                                <div class="image-ad-item">
                                    <div v-if="module.picList[1]?.picUrl" class="item-content" :style="format.imgPadding">
                                        <imageBox :src="module.picList[1].picUrl" />
                                    </div>
                                </div>
                                <div class="image-ad-item">
                                    <div v-if="module.picList[2]?.picUrl" class="item-content" :style="format.imgPadding">
                                        <imageBox :src="module.picList[2].picUrl" />
                                    </div>
                                </div>
                            </div>
                        </div>
                    </template>
                    <template v-else-if="module.picType == 6">
                        <div class="image-square-layout image-square-layout3 image-square-layout-height2">
                            <div class="image-ad-item">
                                <div v-if="module.picList[0]?.picUrl" class="item-content" :style="format.imgPadding">
                                    <imageBox :src="module.picList[0].picUrl" />
                                </div>
                            </div>
                            <div class="grid-layout-2">
                                <div>
                                    <div class="image-ad-item">
                                        <div v-if="module.picList[1]?.picUrl" class="item-content" :style="format.imgPadding">
                                            <imageBox :src="module.picList[1].picUrl" />
                                        </div>
                                    </div>
                                </div>
                                <div class="grid-layout-3">
                                    <div class="image-ad-item">
                                        <div v-if="module.picList[2]?.picUrl" class="item-content" :style="format.imgPadding">
                                            <imageBox :src="module.picList[2].picUrl" />
                                        </div>
                                    </div>
                                    <div class="image-ad-item">
                                        <div v-if="module.picList[3]?.picUrl" class="item-content" :style="format.imgPadding">
                                            <imageBox :src="module.picList[3].picUrl" />
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </template>
                    <template v-else>
                        <div :class="'image-square-layout-' + module.picType + ' image-square-layout-height1'">
                            <div v-for="(pic, key) in module.picList" :key="key" class="image-ad-item">
                                <div v-if="pic.picUrl" class="item-content" :style="format.imgPadding">
                                    <div class="item-img-a">
                                        <el-image class="item-img" fit="fill" :src="imageFormat(pic.picUrl)" />
                                    </div>
                                </div>
                            </div>
                        </div>
                    </template>
                </template>
            </div>
        </template>
    </TotalContainer>
</template>
<script lang="ts" setup>
//图片魔方组件
import { computed, ref, Ref } from "vue";
import { imageFormat } from "@/utils/format";
import { TotalContainer, imageBox } from "@/components/decorate/index";
import Title from "@/views/decorate/decorate/src/modules/pcCustomPage/components/Title.vue";
import { ModuleImageType, ModuleType } from "@/types/decorate/decorate";
import { defaultFrame, defaultTitle, mergeDefaultModule } from "@/views/decorate/decorate/src/modules";

const module = defineModel<ModuleType & ModuleImageType>("module") as Ref<ModuleType & ModuleImageType>;
const defaultModule = ref({
    picType: 1,
    picList: [],
    swiperPreView: 1, //轮播显示数量
    swiperPageColor: "",
    imgPadding: 0,
    picPageType: 1,
    picRadioStyle: 1,
    isFluxWidth: 0,
    frame: defaultFrame,
    title: defaultTitle,
});
mergeDefaultModule(module.value, defaultModule.value);

const format = computed(() => {
    return {
        imgPadding: "padding:" + module.value.imgPadding + "px;"
    };
});
</script>
<style lang="less" scoped>
.image-list {
    box-sizing: border-box;
}
.image-ad-item {
    height: 100%;
    width: 100%;
}
.item-content {
    height: 100%;
    width: 100%;
    overflow: hidden;
    box-sizing: border-box;
}
.item-img-a {
    height: 100%;
    width: 100%;
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
