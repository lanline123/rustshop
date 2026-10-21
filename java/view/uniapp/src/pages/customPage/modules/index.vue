<template>
    <view>
        <template v-if="pageModles && pageModles.title">
            <navBar :module="pageModles" />
        </template>

        <template v-for="(module, index) in modules" :key="module.moduleIndex">
            <template v-if="module.type == 'searchBar' && module?.isShow">
                <searchBar :module="module.module" />
            </template>
            <template v-else-if="module.type == 'catNav' && module?.isShow">
                <catNav :module="module.module" :scroll-top="scrollTop" @change-cat-nav="onChangeCatNav" @is-cut="getShow" />
                <sticky :module="module.module" :scroll-top="scrollTop" @change-cat-nav="onChangeCatNav" @is-cut="getShow" />
            </template>
            <view :class="{ show }">
                <template v-if="showCatNav == 0">
                    <template v-if="module.type == 'notice' && module?.isShow">
                        <notice :module="module.module" />
                    </template>

                    <template v-else-if="module.type == 'titleAd' && module?.isShow">
                        <titleAd :module="module.module" />
                    </template>

                    <template v-else-if="module.type == 'imageAd' && module?.isShow">
                        <imageAd :module="module.module" />
                    </template>

                    <template v-else-if="module.type == 'imageNav' && module?.isShow">
                        <imageNav :module="module.module" />
                    </template>

                    <template v-else-if="module.type == 'coupon' && module?.isShow">
                        <coupon :module="module.module" />
                    </template>

                    <template v-else-if="module.type == 'seckill' && module?.isShow">
                        <seckill :module="module.module" />
                    </template>
                    <template v-else-if="module.type == 'whiteBlank' && module?.isShow">
                        <whiteBlank :module="module.module" />
                    </template>

                    <template v-else-if="module.type == 'whiteLine' && module?.isShow">
                        <whiteLine :module="module.module" />
                    </template>

                    <template v-else-if="module.type == 'imageSquareAd' && module?.isShow">
                        <imageSquareAd :module="module.module" />
                    </template>

                    <template v-else-if="module.type == 'imageHotarea' && module?.isShow">
                        <imageHotarea :module="module.module" />
                    </template>

                    <template v-else-if="module.type == 'product' && module?.isShow">
                        <product :module="module.module" :module-index="module.moduleIndex" />
                    </template>

                    <template v-else-if="module.type == 'productA1' && module?.isShow">
                        <productA1 :module="module.module" :module-index="module.moduleIndex" />
                    </template>

                    <template v-else-if="module.type == 'limitedSpecial' && module?.isShow">
                        <limitedSpecial :module="module.module" :module-index="module.moduleIndex" />
                    </template>

                    <template v-else-if="module.type == 'productSlide' && module?.isShow">
                        <productSlide :module="module.module" :module-index="module.moduleIndex" />
                    </template>

                    <template v-else-if="module.type == 'categoryNav' && module?.isShow">
                        <categoryNav :module="module.module" :module-index="module.moduleIndex" />
                    </template>
                    <template v-else-if="module.type == 'categoryNavA2' && module?.isShow">
                        <categoryNavA2 :module="module.module" :module-index="module.moduleIndex" />
                    </template>
                    <template v-else-if="module.type == 'banner' && module?.isShow">
                        <banner :module="module.module" :module-index="module.moduleIndex" :index="index" />
                    </template>
                    <template v-else-if="module.type == 'slidingImage' && module?.isShow">
                        <SlidingImage :module="module.module" :module-index="module.moduleIndex" :index="index" />
                    </template>
                    <template v-else-if="module.type == 'slideLargeImage' && module?.isShow">
                        <slideLargeImage :module="module.module" :module-index="module.moduleIndex" :index="index" />
                    </template>
                    <template v-else-if="module.type == 'comboSuggestions' && module?.isShow">
                        <comboSuggestions :module="module.module" :module-index="module.moduleIndex" :index="index" />
                    </template>
                    <template v-else-if="module.type == 'noticeA2' && module?.isShow">
                        <noticeA2 :module="module.module" :module-index="module.moduleIndex" :index="index" />
                    </template>
                    <template v-else-if="module.type == 'specialOffer' && module?.isShow">
                        <specialOffer :module="module.module" :module-index="module.moduleIndex" :index="index" />
                    </template>
                    <template v-else-if="module.type == 'mixedProductDisplay' && module?.isShow">
                        <mixedProductDisplay :module="module.module" :module-index="module.moduleIndex" :index="index" />
                    </template>
                    <template v-else-if="module.type == 'categoryRec' && module?.isShow">
                        <categoryRec :module="module.module" :module-index="module.moduleIndex" :index="index" />
                    </template>
                    <template v-else-if="module.type == 'categoryRecA1' && module?.isShow">
                        <categoryRecA1 :module="module.module" :module-index="module.moduleIndex" :index="index" />
                    </template>
                    <template v-else-if="module.type == 'noticeA3' && module?.isShow">
                        <noticeA3 :module="module.module" :module-index="module.moduleIndex" :index="index" />
                    </template>
                    <template v-else-if="module.type == 'shopTips' && module?.isShow">
                        <shopTips :module="module.module" :module-index="module.moduleIndex" :index="index" />
                    </template>
                    <template v-else-if="module.type == 'countdown' && module?.isShow">
                        <countdown :module="module.module" :module-index="module.moduleIndex" :index="index" />
                    </template>
                    <template v-else-if="module.type == 'groupSliding' && module?.isShow">
                        <groupSliding :module="module.module" :module-index="module.moduleIndex" :index="index" />
                    </template>
                    <template v-else-if="module.type == 'productGrouping' && module?.isShow">
                        <productGrouping :module="module.module" :module-index="module.moduleIndex" :index="index" />
                    </template>
                    <template v-else-if="module.type == 'imageTextComponent' && module?.isShow">
                        <imageTextComponent :module="module.module" :module-index="module.moduleIndex" :index="index" />
                    </template>
                    <template v-else-if="module.type == 'imageShowcase' && module?.isShow">
                        <imageShowcase :module="module.module" :module-index="module.moduleIndex" :index="index" />
                    </template>
                    <template v-else-if="module.type == 'tagNav' && module?.isShow">
                        <tagNav :module="module.module" :module-index="module.moduleIndex" :index="index" />
                    </template>
                    <template v-else-if="module.type == 'productCarousel' && module?.isShow">
                        <productCarousel :module="module.module" :module-index="module.moduleIndex" />
                    </template>
                    <template v-else-if="module.type == 'categoryNavA1' && module?.isShow">
                        <categoryNavA1 :module="module.module" :module-index="module.moduleIndex" :index="index" />
                    </template>
                    <template v-else-if="module.type == 'bannerA2' && module?.isShow">
                        <bannerA2
                            :module="module.module"
                            :module-index="module.moduleIndex"
                            :index="index"
                            :need-status-bar="!(pageModles && pageModles.title != '')"
                        />
                    </template>
                    <template v-else-if="module.type == 'newcomerCombo' && module?.isShow">
                        <newcomerCombo :module="module.module" :module-index="module.moduleIndex" />
                    </template>
                    <template v-else-if="module.type == 'groupon' && module?.isShow">
                        <groupon :module="module.module" :module-index="module.moduleIndex" :index="index" />
                    </template>
                    <!-- #ifdef MP-WEIXIN -->
                    <template v-else-if="module.type == 'live'">
                        <live :module="module.module" :module-index="module.moduleIndex" :index="index" />
                    </template>
                    <template v-else-if="module.type == 'videoNumber'">
                        <videoNumber :module="module.module" :module-index="module.moduleIndex" :index="index" />
                    </template>
                    <!-- #endif -->
                </template>
            </view>
        </template>
        <template v-if="showCatNav == 1">
            <view>
                <template v-if="imgUrlData.length > 0">
                    <view class="bannerBox" :style="'background: ' + currentColor + ';'">
                        <view :class="{ show }" class="bannerWrap">
                            <swiper
                                :current="current"
                                :indicator-dots="false"
                                :autoplay="true"
                                :circular="true"
                                interval="3000"
                                duration="500"
                                indicator-color="rgba(255,255,255,0.6)"
                                indicator-active-color="#fff"
                                :style="{ height: swiperHeight + 'px' }"
                            >
                                <template v-for="(item, index) in imgUrlData" :key="index">
                                    <swiper-item>
                                        <view class="swiper-slide">
                                            <image class="swiper-image" mode="widthFix" :src="imageFormat(item.picUrl)" @load="computeImgHeight" />
                                        </view>
                                    </swiper-item>
                                </template>
                            </swiper>
                        </view>
                    </view>
                </template>

                <template v-if="childCatInfoData.length > 0">
                    <view class="categoryBox" :style="'background: ' + currentColor + ';'">
                        <view :class="{ show }" class="categoryWrap">
                            <template v-for="(item, index) in childCatInfoData" :key="index">
                                <template v-if="index < 10">
                                    <view class="item" @click="redirect({ url: '/pages/search/result', param: { categoryId: item.categoryId } })">
                                        <view class="item-img">
                                            <tig-image :src="item.categoryPic" />
                                        </view>
                                        <view class="txt">{{ item.categoryName }}</view>
                                    </view>
                                </template>
                            </template>
                        </view>
                    </view>
                </template>

                <template v-if="brandInfoData.length > 0">
                    <view class="brandBox">
                        <view :class="{ show }" class="brandWrap">
                            <template v-for="(brand, index) in brandInfoData" :key="index">
                                <view v-if="index < 8" class="item">
                                    <view class="itemWrap">
                                        <view @click="redirect({ url: '/pages/search/result', param: { brandId: brand.brandId } })">
                                            <tig-image :src="brand.brandLogo" />
                                            <view class="txt">{{ brand.brandName }}</view>
                                        </view>
                                    </view>
                                </view>
                            </template>
                        </view>
                    </view>
                </template>
            </view>
        </template>
    </view>
</template>

<script lang="ts" setup>
import navBar from "@/pages/customPage/modules/navbar/index.vue";
import searchBar from "@/pages/customPage/modules/searchBar/index.vue";
import catNav from "@/pages/customPage/modules/catNav/index.vue";
import sticky from "@/pages/customPage/modules/catNav/sticky.vue";
import notice from "@/pages/customPage/modules/notice/index.vue";
import titleAd from "@/pages/customPage/modules/titleAd/index.vue";
import imageAd from "@/pages/customPage/modules/imageAd/index.vue";
import whiteBlank from "@/pages/customPage/modules/whiteBlank/index.vue";
import whiteLine from "@/pages/customPage/modules/whiteLine/index.vue";
import imageSquareAd from "@/pages/customPage/modules/imageSquareAd/index.vue";
import imageNav from "@/pages/customPage/modules/imageNav/index.vue";
import coupon from "@/pages/customPage/modules/coupon/index.vue";
import seckill from "@/pages/customPage/modules/seckill/index.vue";
import imageHotarea from "@/pages/customPage/modules/imageHotarea/index.vue";
import product from "@/pages/customPage/modules/product/index.vue";
import productA1 from "@/pages/customPage/modules/productA1/index.vue";
import limitedSpecial from "@/pages/customPage/modules/limitedSpecial/index.vue";
import productSlide from "@/pages/customPage/modules/productSlide/index.vue";
import categoryNav from "@/pages/customPage/modules/categoryNav/index.vue";
import categoryNavA2 from "@/pages/customPage/modules/categoryNavA2/index.vue";
import SlidingImage from "@/pages/customPage/modules/slidingImage/index.vue";
import banner from "@/pages/customPage/modules/banner/index.vue";
import slideLargeImage from "@/pages/customPage/modules/slideLargeImage/index.vue";
import comboSuggestions from "@/pages/customPage/modules/comboSuggestions/index.vue";
import noticeA2 from "@/pages/customPage/modules/noticeA2/index.vue";
import specialOffer from "@/pages/customPage/modules/specialOffer/index.vue";
import mixedProductDisplay from "@/pages/customPage/modules/mixedProductDisplay/index.vue";
import categoryRec from "@/pages/customPage/modules/categoryRec/index.vue";
import countdown from "@/pages/customPage/modules/countdown/index.vue";
import groupSliding from "@/pages/customPage/modules/groupSliding/index.vue";
import productGrouping from "@/pages/customPage/modules/productGrouping/index.vue";
import imageTextComponent from "@/pages/customPage/modules/imageTextComponent/index.vue";
import imageShowcase from "@/pages/customPage/modules/imageShowcase/index.vue";
import tagNav from "@/pages/customPage/modules/tagNav/index.vue";
import productCarousel from "@/pages/customPage/modules/productCarousel/index.vue";
import categoryNavA1 from "@/pages/customPage/modules/categoryNavA1/index.vue";
import bannerA2 from "@/pages/customPage/modules/bannerA2/index.vue";
import newcomerCombo from "@/pages/customPage/modules/newcomerCombo/index.vue";
import groupon from "@/pages/customPage/modules/groupon/index.vue";
import categoryRecA1 from "@/pages/customPage/modules/categoryRecA1/index.vue";
import noticeA3 from "@/pages/customPage/modules/noticeA3/index.vue";
import shopTips from "@/pages/customPage/modules/shopTips/index.vue";
// #ifdef MP-WEIXIN
import videoNumber from "@/pages/customPage/modules/videoNumber/index.vue";
import live from "@/pages/customPage/modules/live/index.vue";
// #endif
import { ref, getCurrentInstance, nextTick } from "vue";
import { redirect, getElementRect } from "@/utils/index";
import { imageFormat } from "@/utils/format";

interface Props {
    modules: any[];
    pageModles: {};
    scrollTop: number;
}
defineProps<Props>();
const emit = defineEmits(["load-goods-list"]);
const showCatNav = ref(0);
const catId = ref(0);
const current = "";

const childCatInfoData = ref<any[]>([]);
const brandInfoData = ref<any[]>([]);
const imgUrlData = ref<any[]>([]);
const currentColor = ref("");
const show = ref(false);
const getShow = (val: boolean) => {
    show.value = val;
};

interface CatNav {
    id: number;
    categoryId: number;
    showCatNav: number;
    catColor: string;
    childCatInfo: any[];
    brandInfo: any[];
    imgUrl: any[];
}

const onChangeCatNav = async (data: CatNav) => {
    const { categoryId, catColor, imgUrl, childCatInfo, brandInfo } = data;
    childCatInfoData.value = childCatInfo;
    brandInfoData.value = brandInfo;
    imgUrlData.value = imgUrl;
    currentColor.value = catColor;
    if (categoryId === catId.value) return;
    catId.value = categoryId; //  记录当前分类id
    nextTick(() => {
        if (catId.value > 0) {
            showCatNav.value = 1;
        } else {
            showCatNav.value = 0;
        }
        show.value = true;
        emit("load-goods-list", categoryId);
    });
};

const instance = getCurrentInstance();
const swiperHeight = ref(0);
const computeImgHeight = (e: any) => {
    nextTick(async () => {
        const res = await getElementRect(".swiper-image", instance);
        swiperHeight.value = res?.height ?? 0;
    });
};
</script>
<style scoped>
@import url("@/static/css/modules.css");
</style>
