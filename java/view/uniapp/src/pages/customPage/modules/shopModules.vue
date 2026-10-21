<template>
    <view>
        <template v-for="(module, index) in modules" :key="index">
            <template v-if="module.type == 'notice'">
                <notice :module="module.module" />
            </template>

            <template v-if="module.type == 'titleAd'">
                <titleAd :module="module.module" />
            </template>

            <template v-if="module.type == 'imageAd'">
                <imageAd :module="module.module" />
            </template>

            <template v-if="module.type == 'imageNav'">
                <imageNav :module="module.module" />
            </template>

            <template v-if="module.type == 'coupon'">
                <coupon :module="module.module" :shop-id="props.shopInfo.shopId" />
            </template>
            <template v-if="module.type == 'whiteBlank'">
                <whiteBlank :module="module.module" />
            </template>

            <template v-if="module.type == 'whiteLine'">
                <whiteLine :module="module.module" />
            </template>

            <template v-if="module.type == 'imageSquareAd'">
                <imageSquareAd :module="module.module" />
            </template>

            <template v-if="module.type == 'imageHotarea'">
                <imageHotarea :module="module.module" />
            </template>

            <template v-if="module.type == 'shopInfo'">
                <shopInfo :module="module.module" :shop-info="props.shopInfo" @refresh-shop-detail="emit('refreshShopDetail')" />
            </template>

            <template v-if="module.type == 'product'">
                <product :shop-id="props.shopInfo.shopId" :module="module.module" :module-index="module.moduleIndex" />
            </template>

            <template v-if="module.type == 'productA1'">
                <productA1 :shop-id="props.shopInfo.shopId" :module="module.module" :module-index="module.moduleIndex" />
            </template>

            <template v-if="module.type == 'limitedSpecial'">
                <limitedSpecial :shop-id="props.shopInfo.shopId" :module="module.module" :module-index="module.moduleIndex" />
            </template>

            <template v-if="module.type == 'productSlide'">
                <productSlide :shop-id="props.shopInfo.shopId" :module="module.module" :module-index="module.moduleIndex" />
            </template>

            <template v-if="module.type == 'categoryNav'">
                <categoryNav :module="module.module" :module-index="module.moduleIndex" />
            </template>
            <template v-if="module.type == 'categoryNavA2'">
                <categoryNavA2 :module="module.module" :module-index="module.moduleIndex" />
            </template>
            <template v-if="module.type == 'banner'">
                <banner :module="module.module" :module-index="module.moduleIndex" :index="index" />
            </template>
            <template v-if="module.type == 'slidingImage'">
                <SlidingImage :module="module.module" :module-index="module.moduleIndex" :index="index" />
            </template>
            <template v-if="module.type == 'slideLargeImage'">
                <slideLargeImage :module="module.module" :module-index="module.moduleIndex" :index="index" />
            </template>
            <template v-if="module.type == 'comboSuggestions'">
                <comboSuggestions :module="module.module" :module-index="module.moduleIndex" :index="index" />
            </template>
            <template v-if="module.type == 'noticeA2'">
                <noticeA2 :module="module.module" :module-index="module.moduleIndex" :index="index" />
            </template>
            <template v-if="module.type == 'specialOffer'">
                <specialOffer :module="module.module" :module-index="module.moduleIndex" :index="index" />
            </template>
            <template v-if="module.type == 'mixedProductDisplay'">
                <mixedProductDisplay :module="module.module" :module-index="module.moduleIndex" :index="index" />
            </template>
            <template v-if="module.type == 'categoryRec'">
                <categoryRec :module="module.module" :module-index="module.moduleIndex" :index="index" />
            </template>
            <template v-if="module.type == 'categoryRecA1'">
                <categoryRecA1 :module="module.module" :module-index="module.moduleIndex" :index="index" />
            </template>
            <template v-if="module.type == 'noticeA3'">
                <noticeA3 :module="module.module" :module-index="module.moduleIndex" :index="index" />
            </template>
            <template v-if="module.type == 'shopTips'">
                <shopTips :module="module.module" :module-index="module.moduleIndex" :index="index" />
            </template>
            <template v-if="module.type == 'countdown'">
                <countdown :module="module.module" :module-index="module.moduleIndex" :index="index" />
            </template>
            <template v-if="module.type == 'groupSliding'">
                <groupSliding :module="module.module" :module-index="module.moduleIndex" :index="index" />
            </template>
            <template v-if="module.type == 'productGrouping'">
                <productGrouping :module="module.module" :module-index="module.moduleIndex" :index="index" />
            </template>
            <template v-if="module.type == 'imageTextComponent'">
                <imageTextComponent :module="module.module" :module-index="module.moduleIndex" :index="index" />
            </template>
            <template v-if="module.type == 'imageShowcase'">
                <imageShowcase :module="module.module" :module-index="module.moduleIndex" :index="index" />
            </template>
            <template v-if="module.type == 'tagNav'">
                <tagNav :module="module.module" :module-index="module.moduleIndex" :index="index" />
            </template>
            <template v-if="module.type == 'productCarousel'">
                <productCarousel :shop-id="props.shopInfo.shopId" :module="module.module" :module-index="module.moduleIndex" />
            </template>
            <template v-if="module.type == 'categoryNavA1'">
                <categoryNavA1 :module="module.module" :module-index="module.moduleIndex" :index="index" />
            </template>
            <template v-if="module.type == 'seckill' && module?.isShow">
                <seckill :module="module.module" />
            </template>
        </template>
    </view>
</template>

<script lang="ts" setup>
import notice from "@/pages/customPage/modules/notice/index.vue";
import titleAd from "@/pages/customPage/modules/titleAd/index.vue";
import imageAd from "@/pages/customPage/modules/imageAd/index.vue";
import whiteBlank from "@/pages/customPage/modules/whiteBlank/index.vue";
import whiteLine from "@/pages/customPage/modules/whiteLine/index.vue";
import imageSquareAd from "@/pages/customPage/modules/imageSquareAd/index.vue";
import imageNav from "@/pages/customPage/modules/imageNav/index.vue";
import coupon from "@/pages/customPage/modules/coupon/index.vue";
import imageHotarea from "@/pages/customPage/modules/imageHotarea/index.vue";
import product from "@/pages/customPage/modules/product/index.vue";
import productA1 from "@/pages/customPage/modules/productA1/index.vue";
import limitedSpecial from "@/pages/customPage/modules/limitedSpecial/index.vue";
import productSlide from "@/pages/customPage/modules/productSlide/index.vue";
import shopInfo from "@/pages/customPage/modules/shopInfo/index.vue";
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
import seckill from "@/pages/customPage/modules/seckill/index.vue";
import categoryRecA1 from "@/pages/customPage/modules/categoryRecA1/index.vue";
import noticeA3 from "@/pages/customPage/modules/noticeA3/index.vue";
import shopTips from "@/pages/customPage/modules/shopTips/index.vue";
import type { ShopDetailItem } from "@/types/shop/shop";

interface Props {
    modules: any[];
    shopInfo: ShopDetailItem;
}
const props = defineProps<Props>();
const emit = defineEmits(["refreshShopDetail"]);
</script>
<style>
@import "@/static/css/modules.css";
</style>
