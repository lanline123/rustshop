<template>
    <div
        :class="
            'module-ad-con module-goods_ad ad-style__' +
            module.style +
            ' ad-buy_btn_style__' +
            module.buyBtnStyle +
            ' ad-goods_style__' +
            module.goodsStyle +
            ' ad-goods_radio_style__' +
            module.goodsRadioStyle +
            ' ad-text_align__' +
            module.textAlign +
            ' ad-text_weight__' +
            module.textWeight +
            ' ad-goods_name_row__' +
            module.goodsNameRow +
            ' ad-goods_name_padding__' +
            module.goodsNamePadding +
            ' ad-goods-title_style__' +
            module.title?.titleStyle +
            (isWaterfall ? ' ad-waterfall__1' : '')
        "
        :style="frameFormat.boxPadding + frameFormat.boxPaddingTop + frameFormat.boxPaddingBottom"
    >
        <div
            class="module-ad-content"
            :style="
                frameFormat.backgroundColor +
                ' ' +
                frameFormat.boxRadius +
                allFormat.backgroundColor +
                allFormat.boxPadding +
                allFormat.boxPaddingBottom +
                allFormat.boxPaddingTop +
                allFormat.boxRadius +
                allFormat.innerPadding
            "
        >
            <div class="module-ad-empty empty-image_ad" v-if="!productList || productList.length == 0">
                <div class="image-empty-goods-bg"></div>
                <div class="desc">{{ $t('点击添加商品') }}</div>
            </div>
            <CommonTitle v-if="title?.showTitle" v-model="title"></CommonTitle>
            <div class="goods-ad-warp" style="">
                <div class="goods-ad-con">
                    <template v-if="module.style === 7">
                        <Swiper :swiperOption="swiperOption" v-model="swiperList">
                            <template v-slot:default="{ item }">
                                <div class="goods-ad-item" v-for="(subItem, index) in item" :key="index">
                                    <div class="item-content" :style="allFormat.goodsPadding">
                                        <div class="item-con">
                                            <div class="item-photo">
                                                <a href="" title="" class="item-image-a"><img :src="productImage(subItem)" alt="" /></a>
                                            </div>
                                            <div class="item-info">
                                                <div class="item-name">
                                                    <a v-if="module?.showName" href="" title="" class="item-name-a">{{ subItem.productName }}</a>
                                                    <a v-if="module?.showBrief" href="" title="" class="item-brief">{{ $t('商品描述') }}</a>
                                                </div>
                                                <div class="item-action" v-if="module?.showPrice">
                                                    <div class="item-price">
                                                        <b class="price_format">
                                                            {{ priceFormat(Number(subItem.productPrice)) }}
                                                        </b>
                                                    </div>
                                                    <div class="item-buy"></div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </template>
                        </Swiper>
                    </template>

                    <template v-else-if="isWaterfall">
                        <div class="goods-ad-col" v-for="(col, colIndex) in waterfallColumns" :key="'wf-col-' + colIndex">
                            <div class="goods-ad-item" v-for="item in col" :key="item.productId">
                                <div class="item-content" :style="allFormat.goodsPadding">
                                    <div class="item-con">
                                        <div class="item-photo">
                                            <a href="" title="" class="item-image-a"><img :src="productImage(item)" alt="" /></a>
                                        </div>
                                        <div class="item-info">
                                            <div class="item-name">
                                                <a v-if="module?.showName" href="" title="" class="item-name-a">{{ item.productName }}</a>
                                                <a v-if="module?.showBrief" href="" title="" class="item-brief">{{ $t('商品描述') }}</a>
                                            </div>
                                            <div class="item-action" v-if="module?.showPrice">
                                                <div class="item-price">
                                                    <b class="price_format">
                                                        {{ priceFormat(Number(item.productPrice)) }}
                                                    </b>
                                                </div>
                                                <div class="item-buy">
                                                    <a href="javascript:;" class="buy-btn lyecs-buyBtn">
                                                        <template v-if="module.buyBtnStyle === 5 || module.buyBtnStyle === 6">
                                                            <span>{{ $t('购买') }}</span>
                                                        </template>
                                                        <template v-if="module.buyBtnStyle === 7 || module.buyBtnStyle === 8">
                                                            <span>{{ $t('马上抢') }}</span>
                                                        </template>
                                                        <template v-if="module.buyBtnStyle === 1">
                                                            <span class="iconfont-h5 icon-gouwuche iconh5-style"></span>
                                                        </template>
                                                        <template v-if="module.buyBtnStyle === 2">
                                                            <span class="iconfont-h5 icon-gouwuche1 iconh5-style"></span>
                                                        </template>
                                                        <template v-if="module.buyBtnStyle === 3">
                                                            <span class="iconfont-h5 icon-jia iconh5-style"></span>
                                                        </template>
                                                        <template v-if="module.buyBtnStyle === 4">
                                                            <span class="iconfont-h5 icon-jia1 iconh5-style"></span>
                                                        </template>
                                                    </a>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </template>

                    <template v-else>
                        <div class="goods-ad-item" v-for="item in productList" :key="item.productId">
                            <div class="item-content" :style="allFormat.goodsPadding">
                                <div class="item-con">
                                    <div class="item-photo">
                                        <a href="" title="" class="item-image-a"><img :src="productImage(item)" alt="" /></a>
                                    </div>
                                    <div class="item-info">
                                        <div class="item-name">
                                            <a v-if="module?.showName" href="" title="" class="item-name-a">{{ item.productName }}</a>
                                            <a v-if="module?.showBrief" href="" title="" class="item-brief">{{ $t('商品描述') }}</a>
                                        </div>
                                        <div class="item-action" v-if="module?.showPrice">
                                            <div class="item-price">
                                                <b class="price_format">
                                                    {{ priceFormat(Number(item.productPrice)) }}
                                                </b>
                                            </div>
                                            <div class="item-buy">
                                                <a href="javascript:;" class="buy-btn lyecs-buyBtn">
                                                    <template v-if="module.buyBtnStyle === 5 || module.buyBtnStyle === 6">
                                                        <span>{{ $t('购买') }}</span>
                                                    </template>
                                                    <template v-if="module.buyBtnStyle === 7 || module.buyBtnStyle === 8">
                                                        <span>{{ $t('马上抢') }}</span>
                                                    </template>
                                                    <template v-if="module.buyBtnStyle === 1">
                                                        <span class="iconfont-h5 icon-gouwuche iconh5-style"></span>
                                                    </template>
                                                    <template v-if="module.buyBtnStyle === 2">
                                                        <span class="iconfont-h5 icon-gouwuche1 iconh5-style"></span>
                                                    </template>
                                                    <template v-if="module.buyBtnStyle === 3">
                                                        <span class="iconfont-h5 icon-jia iconh5-style"></span>
                                                    </template>
                                                    <template v-if="module.buyBtnStyle === 4">
                                                        <span class="iconfont-h5 icon-jia1 iconh5-style"></span>
                                                    </template>
                                                </a>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </template>
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref, computed, onMounted, watch, Ref, watchEffect } from "vue";
import { priceFormat, productImage } from "@/utils/format";
import { ModuleType, ModuleActivityType } from "@/types/decorate/decorate.d";
import { getProductList } from "@/api/product/product";
import type { ProductFormState } from "@/types/product/product.d";
import { mergeDefaultModule, defaultFrame, defaultProducts, formatFrame, defaultTitle, CommonTitle } from "@/views/decorate/decorate/src/modules/";
import { Swiper } from "@/components/swiper";
// 在modules加入要使用的模块
const module = defineModel<ModuleType & ModuleActivityType>("module") as Ref<ModuleType & ModuleActivityType>;
const defaultModule = ref({
    style: 1,
    goodsStyle: 1,
    goodsRadioStyle: 1,
    textAlign: 1,
    textWeight: 1,
    goodsNameRow: 2,
    goodsNamePadding: 1,
    showName: 1,
    showBrief: 1,
    showPrice: 1,
    goodsPadding: 5,
    buyBtnStyle: 1,
    backgroundColor: "",
    boxRadius: 0,
    innerPadding: 0,
    boxPadding: 10,
    boxPaddingTop: 5,
    boxPaddingBottom: 5,
    imgPadding: 1,
    waterfall: 0,
    swiperPageColor: "",
    frame: defaultFrame,
    title: defaultTitle,
    products: defaultProducts
});
mergeDefaultModule(module.value, defaultModule.value);
const { frame, title } = module.value;
const productList = ref<ProductFormState[]>();
const frameFormat = computed(() => {
    return formatFrame(frame ?? {});
});
const allFormat = computed(() => {
    return {
        backgroundColor: `background-color: ${module?.value?.backgroundColor};`,
        boxPadding: `margin-left: ${module?.value?.boxPadding}px;margin-right: ${module?.value?.boxPadding}px;`,
        boxPaddingBottom: `margin-bottom: ${module?.value?.boxPaddingBottom}px;`,
        boxPaddingTop: `margin-top: ${module?.value?.boxPaddingTop}px;`,
        boxRadius: `border-radius: ${module?.value?.boxRadius}px;`,
        innerPadding: `padding: ${module?.value?.innerPadding}px;`,
        goodsPadding: `padding: ${module?.value?.goodsPadding}px;`
    };
});

const paramsValueMap: { [key: number]: string[] } = {
    1: ["productIds"],
    2: ["productCategoryId", "productNumber"],
    3: ["productTag", "productNumber"]
};
const paramsKeyMap: { [key: string]: string } = {
    productIds: "ids",
    productCategoryId: "categoryId",
    productNumber: "size",
    productTag: "introType"
};

const _getproductList = async () => {
    let param: any = {};
    if (typeof module.value.products === "object") {
        if (module.value.products.productSelectType) {
            const type = module.value.products.productSelectType;
            const keys = paramsValueMap[type];
            if (type === 1) {
                param[paramsKeyMap[keys[0]]] = module.value.products.productIds.join(",");
            } else {
                param.page = 1;
                keys.forEach((key) => {
                    param[paramsKeyMap[key]] = module.value.products[key];
                });
            }
        }
    }
    try {
        const result = await getProductList({ productStatus: 1, checkStatus: 1, ...param });
        productList.value = result.records;
        swiperList.value = [];
        getSwiperList();
    } catch (error) {
    } finally {
    }
};
_getproductList();

const isWaterfall = computed(() => Number(module.value.style) === 2 && Number(module.value.waterfall) === 1);
const waterfallColumns = computed(() => {
    const list = productList.value || [];
    const left: ProductFormState[] = [];
    const right: ProductFormState[] = [];
    list.forEach((item, index) => {
        (index % 2 === 0 ? left : right).push(item);
    });
    return [left, right];
});

const swiperPageColor = computed(() => {
    return module.value.swiperPageColor || "#333";
});
const swiperList = ref<any[]>([]);
const getSwiperList = () => {
    if (module.value.style === 7 && productList.value) {
        let swiperListNum = Math.ceil(productList.value.length / 3);
        for (let index = 0; index < swiperListNum; index++) {
            swiperList.value.push(productList.value.slice(index * 3, (index + 1) * 3));
        }
    }
};
watchEffect(() => {
    _getproductList();
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
</script>
<style lang="less" scoped>
.iconh5-style {
    color: #f23030;
    font-size: 18px;
    margin-right: 5px;
}
:deep(.swiper-pagination-bullet-active) {
    background: v-bind("swiperPageColor") !important;
}

.ad-style__7 :deep(.swiper-pagination-bullets.swiper-pagination-horizontal) {
    bottom: 15px;
}
.ad-style__7 :deep(.swiper-pagination-bullet) {
    width: 12px;
    border-radius: 0;
    height: 2px;
    margin: 0 1px;
}
.image-empty-goods-bg {
  background: url("../../../img/goods.png") no-repeat center center;
  background-size: 100% auto;
  width: 100px;
  margin: 0 auto 10px;
  height: 100px;
}
</style>
