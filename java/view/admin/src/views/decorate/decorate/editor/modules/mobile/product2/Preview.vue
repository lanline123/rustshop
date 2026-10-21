<template>
    <div
        :class="
            'module-ad-con module-goods_ad ad-style__2 ' +
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
            module.title?.titleStyle
        "
        :style="frameFormat.boxPadding + frameFormat.boxPaddingTop + frameFormat.boxPaddingBottom"
    >
        <div
            class="module-ad-content"
            :style="
                frameFormat.backgroundColor +
                ' ' +
                frameFormat.boxRadius +
                allFormat.boxPadding +
                allFormat.boxPaddingBottom +
                allFormat.boxPaddingTop +
                allFormat.boxRadius +
                allFormat.innerPadding
            "
        >
            <div class="module-ad-empty empty-image_ad" v-if="!productList || productList.length == 0">
                <div class="image-empty-bg"></div>
                <div class="desc">点击添加图片广告</div>
            </div>
            <div class="goods-ad-warp">
                <div class="goods-ad-con">
                    <div class="goods-ad-item" v-for="item in productList" :key="item.productId">
                        <div class="item-content" :style="allFormat.goodsPadding + allFormat.goodsRadius">
                            <div class="item-con">
                                <div class="item-photo">
                                    <a href="" title="" class="item-image-a">
                                        <div class="item-image-bg" :style="{ backgroundColor: module.imageBackgroundColor }"></div>
                                        <img :src="productImage(item)" alt="" />
                                    </a>
                                </div>
                                <div class="item-info">
                                    <div class="item-name">
                                        <a href="" title="" class="item-name-a">{{ item.productName }}</a>
                                    </div>
                                    <div class="item-action">
                                        <div class="item-price">
                                            <b class="price_format">
                                                {{ priceFormat(Number(item.productPrice)) }}
                                            </b>
                                        </div>
                                        <a class="buy-btn lyecs-buyBtn">
                                            <span class="iconfont-h5 icon-gouwuche3 iconh5-style"></span>
                                        </a>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref, computed, watchEffect } from "vue";
import { priceFormat, productImage } from "@/utils/format";
import { getProductList } from "@/api/product/product";
import type { ProductFormState } from "@/types/product/product.d";
import { mergeDefaultModule, formatFrame, CommonTitle } from "@/views/decorate/decorate/src/modules/index";
import { product2Defaults } from "./index";
import type { Product2Module } from "./types";

const props = defineProps<{ module: Product2Module }>();
const module = props.module;
// 旧数据缺字段时烧录默认值（原地写回 store 模块对象，与旧 View.vue 行为一致，契约）
mergeDefaultModule(module, product2Defaults);
const { frame, title } = module;
const productList = ref<ProductFormState[]>();
const frameFormat = computed(() => {
    return formatFrame(frame ?? {});
});
const allFormat = computed(() => {
    return {
        boxPadding: `margin-left: ${module?.boxPadding}px;margin-right: ${module?.boxPadding}px;`,
        boxPaddingBottom: `margin-bottom: ${module?.boxPaddingBottom}px;`,
        boxPaddingTop: `margin-top: ${module?.boxPaddingTop}px;`,
        boxRadius: `border-radius: ${module?.boxRadius}px;`,
        innerPadding: `padding: ${module?.innerPadding}px;`,
        goodsPadding: `padding: ${module?.goodsPadding}px;`,
        goodsRadius: `border-radius: ${module?.goodsRadius}px;`
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
    if (typeof module.products === "object") {
        if (module.products.productSelectType) {
            const type = module.products.productSelectType;
            const keys = paramsValueMap[type];
            if (type === 1) {
                param[paramsKeyMap[keys[0]]] = module.products.productIds.join(",");
            } else {
                param.page = 1;
                keys.forEach((key) => {
                    param[paramsKeyMap[key]] = module.products[key];
                });
            }
        }
    }
    try {
        const result = await getProductList({ productStatus: 1, checkStatus: 1, ...param });
        productList.value = result.records;
    } catch (error) {
    } finally {
    }
};
_getproductList();

watchEffect(() => {
    _getproductList();
});
</script>
<style lang="less" scoped>
.icon-gouwuche3 {
    color: #f23030;
}

.goods-ad-con {
    gap: 8px;
}
.ad-style__2 .goods-ad-warp .goods-ad-con .goods-ad-item {
    width: calc(100% / 2 - 4px);
}

.item-content {
    overflow: hidden;
}

.item-image-a {
    position: relative;
    display: block;

    .item-image-bg {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
    }

    img {
        z-index: 1;
        position: relative;
    }
}
</style>
