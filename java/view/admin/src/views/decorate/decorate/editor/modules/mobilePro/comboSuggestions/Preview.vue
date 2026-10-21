<template>
    <div
        class="module-ad-con"
        :style="
            commonStyle.moduleStyle.boxPaddingBottom +
            commonStyle.moduleStyle.boxPaddingTop +
            commonStyle.moduleStyle.boxPadding +
            commonStyle.moduleStyle.backgroundSize +
            commonStyle.moduleStyle.backgroundImage
        "
    >
        <div class="main" ref="mainRef">
            <template v-for="(group, index) in module.comboGroups" :key="group">
                <div class="list" :style="listStyle + `${index > 1 ? 'margin-top: 10px;' : ''}`">
                    <div style="padding: 6px">
                        <div class="top">
                            <div class="top-tit">
                                <template v-if="group.titleImage.picUrl">
                                    <img class="img" :src="imageFormat(group.titleImage.picUrl)" />
                                </template>
                                <template v-else>
                                    <div class="big-title" :style="`color:${module.groupTitleColor}`">{{ group.groupTitle }}</div>
                                </template>
                                <div class="title" :style="`background:${group.groupColor}`">{{ group.groupSubTitle }}</div>
                            </div>
                            <div class="desc" :style="`color: ${group.groupColor}`">{{ group.groupDesc }}</div>
                        </div>
                        <div :style="`border-radius: ${module.groupRadius}px; overflow: hidden; position: relative; height: ${height}px;`">
                            <template v-if="group.groupType === 'image'">
                                <Swiper :swiperOption="swiperOption" v-model="group.picList" @activeIndexChange="getIndex">
                                    <template v-slot:default="{ item }">
                                        <div class="pic-box">
                                            <div class="pic-img" :style="` height: ${height}px;background-image: url(${imageFormat(item.picUrl)});`"></div>
                                            <div class="tit" :style="`color: ${module.bannerTittleColor};`">{{ item.picTitle }}</div>
                                        </div>
                                    </template>
                                </Swiper>
                                <div :style="`position: absolute; right: 10px; bottom: 5px; line-height: 18px; color:${module.bannerDotColor}; z-index: 9`">
                                    {{ `${currentIndex + 1}/${group.picList?.length}` }}
                                </div>
                            </template>
                            <template v-if="group.groupType === 'product'">
                                <Swiper
                                    :swiperOption="{ ...swiperOption, slidesPerView: 2, spaceBetween: 6 }"
                                    v-model="productGroupsList[index]"
                                    @activeIndexChange=""
                                >
                                    <template v-slot:default="{ item }">
                                        <div class="item">
                                            <div
                                                class="goods-img"
                                                :style="`width: 100%;
                                                            padding-top: 100%;
                                                            background-size: cover;
                                                            border-radius: ${module.imageRadius}px;
                                                            background-image: url(${productImage(item)});`"
                                            ></div>
                                            <div class="goods-price">
                                                <span
                                                    class="price-text"
                                                    :style="`color: ${module.regularPriceColor}; background: ${module.regularPriceBgColor || 'transparent'}; border-radius: ${module.regularPriceRadius}px; font-size: ${module.regularPriceSize}px;`"
                                                    >￥{{ item.productPrice }}</span
                                                >
                                            </div>
                                        </div>
                                    </template>
                                </Swiper>
                            </template>
                        </div>
                    </div>
                </div>
            </template>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref, computed, watchEffect } from "vue";
import { GradientType } from "@/types/decorate/decorate.d";
import { mergeDefaultModule, formatCommonStyle, gradientMap, getDemoList } from "@/views/decorate/decorate/src/modules/index";
import { Swiper } from "@/components/swiper";
import { imageFormat, productImage } from "@/utils/format";
import { getProductList } from "@/api/product/product";
import { comboSuggestionsDefaults } from "./index";
import type { ComboSuggestionsModule } from "./types";

const props = defineProps<{ module: ComboSuggestionsModule }>();
const module = props.module;
// 旧数据缺字段时烧录默认值（原地写回 store 模块对象，与旧 View.vue 行为一致，契约）
mergeDefaultModule(module, comboSuggestionsDefaults);
const swiperOption = ref<any>({
    autoplay: {
        delay: module.interval * 1000,
        disableOnInteraction: false, //用户操作swiper之后，是否禁止autoplay
        pauseOnMouseEnter: true //鼠标置于swiper是否时暂停自动切换
    },
    pagination: false
});
const commonStyle = computed(() => {
    return formatCommonStyle(module.moduleStyle ?? {}, module.contentStyle ?? {}, module.subTitle ?? {});
});

const listStyle = computed(() => {
    let str = `border-radius: ${module.groupRadius}px;background-image: linear-gradient(${gradientMap[module.backgroundGradientType as GradientType]},${module.gradientColorA} -50%, ${module.gradientColorB});`;
    if (module.groupShadow === 1) {
        str += `box-shadow: rgba(0, 0, 0, 0.06) 0px 2px 4px;`;
    }
    return str;
});

const height = ref(0);
const getHeight = () => {
    if (mainRef.value && mainRef.value.clientWidth > 0) {
        height.value = (mainRef.value.clientWidth / 2 - 4 - 12) * 0.63;
    }
};
const mainRef = ref();

const currentIndex = ref(0);
const getIndex = (e: any) => {
    currentIndex.value = e.realIndex;
};

const productGroupsList = ref<any>({});
watchEffect(() => {
    if (module.comboGroups) {
        module.comboGroups.forEach(async (group: any, index: number) => {
            if (group.groupType === "product") {
                try {
                    const result = await getProductList({ ids: group.pruductList.map((item: any) => item.productId).join(",") });
                    productGroupsList.value[index] = [...getDemoList(group.pruductList), ...result.records];
                } catch (error) {
                    console.error(error);
                }
            }
        });
    }
});

watchEffect(() => {
    if (module.autoPlay == 1) {
        swiperOption.value.autoplay = {
            delay: module.interval * 1000,
            disableOnInteraction: false, //用户操作swiper之后，是否禁止autoplay
            pauseOnMouseEnter: true //鼠标置于swiper是否时暂停自动切换
        };
    } else {
        swiperOption.value.autoplay = false;
    }

    if (module.moduleStyle?.boxPadding) {
        getHeight();
    }
});
</script>
<style lang="less" scoped>
.decorate-slide-box::-webkit-scrollbar {
    display: none;
    height: 0;
}
.module-ad-con {
    background-size: cover;
    background-repeat: no-repeat;
    background-position: center top;
    position: relative;
}
.main {
    display: flex;
    flex-wrap: wrap;
    justify-content: space-between;
    .list {
        width: calc(50% - 4px);
        flex-shrink: 0;
        overflow: hidden;
        font-size: 12px;
        .top {
            display: block;
            margin-bottom: 5px;

            .top-tit {
                height: 18px;
                display: flex;
                align-items: center;
                .big-title {
                    font-size: 15px;
                    font-weight: 700;
                    margin-right: 5px;
                }
                .img {
                    height: 15px;
                    display: block;
                    margin-right: 5px;
                }
                .title {
                    height: 18px;
                    line-height: 18px;
                    padding: 0 6px;
                    border-radius: 5px;
                    font-size: 11px;
                    color: #fff;
                }
            }
            .desc {
                margin-top: 3px;
                height: 18px;
                line-height: 18px;
                overflow: hidden;
            }
        }

        .pic-box {
            position: relative;

            .pic-img {
                display: block;
                position: relative;
                background-size: cover;
                background-position: center;
                background-repeat: no-repeat;
                width: 100%;
            }
            .tit {
                height: 18px;
                line-height: 18px;
                color: #fff;
                overflow: hidden;
                position: absolute;
                left: 10px;
                bottom: 5px;
            }
        }
        .item {
            .goods-img {
                position: relative;
                width: 100%;
                height: 0;
                background-repeat: no-repeat;
                background-position: center;
                overflow: hidden;
                flex-shrink: 0;
            }
            .goods-price {
                position: relative;
                flex-wrap: nowrap;
                margin-top: 5px;
                display: flex;
                align-items: center;
                justify-content: center;
                color: #e84a40;
                font-family: "font1";
                overflow: hidden;
                .price-text {
                    display: inline-block;
                    padding: 0 8px;
                    line-height: 1.4;
                    max-width: 100%;
                    overflow: hidden;
                    text-overflow: ellipsis;
                    white-space: nowrap;
                }
            }
        }
    }
}
</style>
