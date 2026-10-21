<template>
    <div
        :class="[
            'module-ad-con module-goods_ad module-product_a1',
            styleClass,
            'ad-goods_style__' + module.goodsStyle,
            'ad-text_weight__' + module.textWeight,
            'ad-goods_name_row__' + module.goodsNameRow,
            'ad-goods_name_padding__' + module.goodsNamePadding
        ]"
        :style="allFormat.boxPadding + allFormat.boxPaddingTop + allFormat.boxPaddingBottom"
    >
        <div class="module-ad-content" :style="allFormat.backgroundColor + allFormat.boxRadius + allFormat.innerPadding">
            <div class="module-ad-empty empty-image_ad" v-if="!productList || productList.length == 0">
                <div class="image-empty-goods-bg"></div>
                <div class="desc">点击添加商品</div>
            </div>
            <div class="goods-ad-warp">
                <div class="goods-ad-con">
                    <div class="goods-ad-item" v-for="(item, idx) in productList" :key="(item.productId ?? 0) + '_' + idx" :style="itemGapStyle">
                        <div class="item-content">
                            <div class="item-con" :style="itemConRadiusStyle + goodsPaddingStyle">
                                <div class="item-photo" :style="itemPhotoRadiusStyle">
                                    <a href="" title="" class="item-image-a">
                                        <img :src="productImage(item)" alt="" />
                                    </a>
                                    <span v-if="module.showProductLabel && module.tagList?.[idx]" class="item-label-tag" :style="labelTagStyle">
                                        {{ module.tagList[idx] }}
                                    </span>
                                </div>
                                <div class="item-info" :style="itemInfoRadiusStyle">
                                    <div class="item-name" :style="nameAlignStyle">
                                        <a v-if="module?.showName" href="" title="" class="item-name-a">{{ item.productName }}</a>
                                        <div v-if="module.showBrief && briefTags(idx).length" class="item-brief-list" :style="briefListAlignStyle">
                                            <span v-for="(brief, bi) in briefTags(idx)" :key="bi" class="item-brief-tag" :style="briefTagStyle">{{
                                                brief
                                            }}</span>
                                        </div>
                                    </div>
                                    <div class="item-action" v-if="module?.showPrice" :style="actionAlignStyle">
                                        <div class="item-price" :style="priceAlignStyle">
                                            <b class="price_format">{{ priceFormat(Number(item.productPrice)) }}</b>
                                            <span
                                                v-if="
                                                    module.showMarketPrice &&
                                                    Number(item.marketPrice) > 0 &&
                                                    Number(item.marketPrice) > Number(item.productPrice)
                                                "
                                                class="market_price"
                                            >
                                                <b>{{ priceFormat(Number(item.marketPrice)) }}</b>
                                            </span>
                                        </div>
                                        <div class="item-buy" v-if="module.buyBtnStyle?.showBtn">
                                            <CommonBuyButton :styleOptions="module.buyBtnStyle"></CommonBuyButton>
                                        </div>
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
import { cloneDeep } from "lodash";
import { priceFormat, productImage } from "@/utils/format";
import { getProductList } from "@/api/product/product";
import type { ProductFormState } from "@/types/product/product.d";
import { mergeDefaultModule, CommonBuyButton } from "@/views/decorate/decorate/src/modules/index";
import { productA1Defaults } from "./index";
import type { ProductA1Module } from "./types";

const props = defineProps<{ module: ProductA1Module }>();
const module = props.module;
// 旧数据缺字段时烧录默认值（原地写回 store 模块对象，与旧 View.vue 行为一致，契约）
mergeDefaultModule(module, productA1Defaults);
// 兼容历史草稿：buyBtnStyle 曾为数字（1~8），mergeDefaultModule 不会覆盖非空旧值，这里强制回退为默认对象
if (!module.buyBtnStyle || typeof module.buyBtnStyle !== "object") {
    module.buyBtnStyle = cloneDeep(productA1Defaults.buyBtnStyle) as any;
}
const productList = ref<ProductFormState[]>();
const allFormat = computed(() => {
    return {
        backgroundColor: module?.backgroundColor ? `background-color: ${module.backgroundColor};` : "",
        boxPadding: `margin-left: ${module?.boxPadding}px;margin-right: ${module?.boxPadding}px;`,
        boxPaddingBottom: `margin-bottom: ${module?.boxPaddingBottom}px;`,
        boxPaddingTop: `margin-top: ${module?.boxPaddingTop}px;`,
        boxRadius: `border-radius: ${module?.boxRadius}px;`,
        innerPadding: `padding: ${module?.innerPadding}px;`
    };
});

const goodsRadius = computed(() => module.goodsRadius ?? 0);
const isListStyle = computed(() => module.style === 4);

const itemConRadiusStyle = computed(() => {
    const r = goodsRadius.value;
    if (!r || isListStyle.value) return r ? `border-radius:${r}px;overflow:hidden;` : "";
    return `border-radius:${r}px;overflow:hidden;`;
});
const itemPhotoRadiusStyle = computed(() => {
    const r = goodsRadius.value;
    if (!r) return "";
    if (isListStyle.value) return `border-radius:${r}px 0 0 ${r}px;overflow:hidden;`;
    return `border-radius:${r}px ${r}px 0 0;overflow:hidden;`;
});
const itemInfoRadiusStyle = computed(() => {
    const r = goodsRadius.value;
    if (!r) return "";
    if (isListStyle.value) return `border-radius:0 ${r}px ${r}px 0;`;
    return `border-radius:0 0 ${r}px ${r}px;`;
});

const styleClassMap: Record<number, string> = { 1: "ad-style__1", 2: "ad-style__2", 3: "ad-style__3", 4: "ad-style__5" };
const styleClass = computed(() => styleClassMap[module.style as number] || "ad-style__1");

const labelTagStyle = computed(() => {
    const bg = module.labelBackgroundColor;
    const color = module.labelTextColor;
    const r = module.labelRadius ?? 0;
    const mode = module.labelRadiusStyle ?? 0;
    const fontSize = module.labelFontSize;
    const pos = module.labelPosition || "top-left";
    const offsetX = module.labelOffsetX ?? 0;
    const offsetY = module.labelOffsetY ?? 0;
    let radiusCss = "";
    if (mode === 0) {
        radiusCss = `border-radius:${r}px;`;
    } else if (mode === 1) {
        radiusCss = `border-radius:${r}px 0 0 ${r}px;`;
    } else if (mode === 2) {
        radiusCss = `border-radius:0 ${r}px ${r}px 0;`;
    } else if (mode === 3) {
        radiusCss = `border-top-left-radius:${r}px;border-bottom-right-radius:${r}px;`;
    } else if (mode === 4) {
        radiusCss = `border-top-right-radius:${r}px;border-bottom-left-radius:${r}px;`;
    }
    const posMap: Record<string, string> = {
        "top-left": `left:${offsetX}px;top:${offsetY}px;right:auto;bottom:auto;`,
        "top-right": `right:${offsetX}px;top:${offsetY}px;left:auto;bottom:auto;`,
        "bottom-left": `left:${offsetX}px;bottom:${offsetY}px;right:auto;top:auto;`,
        "bottom-right": `right:${offsetX}px;bottom:${offsetY}px;left:auto;top:auto;`
    };
    return [
        bg ? `background-color:${bg};` : "",
        color ? `color:${color};` : "",
        radiusCss,
        fontSize !== undefined ? `font-size:${fontSize}px;` : "",
        posMap[pos] || posMap["top-left"]
    ].join("");
});

const itemGapStyle = computed(() => {
    const g = module.itemGap ?? 0;
    return g > 0 ? `padding:${g / 2}px;` : "";
});

// 商品边距：作用于整个商品卡内容（item-con，含图片+文字）的四周内边距，整体内缩，仅 >0 生效，
// 与 itemGap（卡片之间的间隙）职责分离；为 0 时不输出，保持各模板原观感。
const goodsPaddingStyle = computed(() => {
    const p = module.goodsPadding ?? 0;
    return p > 0 ? `padding:${p}px;` : "";
});

const nameAlignStyle = computed(() => {
    return module.nameAlign === 2 ? "text-align:center;" : "text-align:left;";
});
const briefListAlignStyle = computed(() => {
    return module.nameAlign === 2 ? "justify-content:center;" : "justify-content:flex-start;";
});
const priceAlignStyle = computed(() => {
    return module.priceAlign === 2 ? "justify-content:center;" : "justify-content:flex-start;";
});
const actionAlignStyle = computed(() => {
    return module.priceAlign === 2 ? "justify-content:center;" : "";
});

const briefTags = (idx: number) => {
    const raw = module.briefList?.[idx] || "";
    return raw
        .split("\n")
        .map((s) => s.trim())
        .filter(Boolean);
};
const briefTagStyle = computed(() => {
    const s = module.briefStyle || {};
    const bg = module.briefBackgroundColor;
    return [
        s.titleColor ? `color:${s.titleColor};` : "",
        s.titleFontSize ? `font-size:${s.titleFontSize}px;` : "",
        s.titleFontBold ? `font-weight:${s.titleFontBold};` : "",
        bg ? `background-color:${bg};` : ""
    ].join("");
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

// 默认演示商品（productId===0；admin 编辑器预览用占位，仅本组件局部使用，不写入 module）
// 与同类 mobilePro 模块（specialOffer/mixedProductDisplay/comboSuggestions）的 demo 商品风格保持一致：
// 真实运行端（uniapp）只显示后台拉到的真实商品；admin canvas 在用户尚未选商品 / 拉取空 / 拉取失败时，
// 退回这批 demo 数据，避免空模块挡住运营调样式。
const demoProductList: ProductFormState[] = [
    {
        productId: 0,
        productSn: "",
        productName: "刺身拼盘",
        productDesc: "",
        productPrice: 39.0,
        marketPrice: 99.0,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1731485756wtyOOnqoeEn6W2XfNP.jpg"
    } as ProductFormState,
    {
        productId: 0,
        productSn: "",
        productName: "冰鲜剥皮鱼",
        productDesc: "",
        productPrice: 39.0,
        marketPrice: 99.0,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1731485756sGPXRzI7o84S5HAPo5.jpg"
    } as ProductFormState,
    {
        productId: 0,
        productSn: "",
        productName: "北极甜虾",
        productDesc: "",
        productPrice: 39.0,
        marketPrice: 99.0,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1731485756JsgKabEE4BO09iRqnc.jpg"
    } as ProductFormState,
    {
        productId: 0,
        productSn: "",
        productName: "秋刀鱼",
        productDesc: "",
        productPrice: 39.0,
        marketPrice: 99.0,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1731485756ivBG9CydBhi21do1xk.jpg"
    } as ProductFormState,
    {
        productId: 0,
        productSn: "",
        productName: "德宝纸巾",
        productDesc: "",
        productPrice: 39.0,
        marketPrice: 99.0,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1731650293KF9TjvpDKHOATMR3H7.jpg"
    } as ProductFormState,
    {
        productId: 0,
        productSn: "",
        productName: "薯片",
        productDesc: "",
        productPrice: 39.0,
        marketPrice: 99.0,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1731650294a4EsPq1zkYme6p5OhC.jpg"
    } as ProductFormState
];

// 判断当前 products 配置是否有效（决定是否调 API；无效则直接走 demo fallback）
const hasValidProductsConfig = (): boolean => {
    const products = module.products;
    if (!products || typeof products !== "object" || !products.productSelectType) return false;
    const type = Number(products.productSelectType);
    if (type === 1) return Array.isArray(products.productIds) && products.productIds.length > 0;
    if (type === 2) return Number(products.productNumber) > 0;
    if (type === 3) return !!products.productTag && Number(products.productNumber) > 0;
    return false;
};

const _getproductList = async () => {
    if (!hasValidProductsConfig()) {
        productList.value = demoProductList;
        return;
    }
    const param: any = {};
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
    try {
        const result = await getProductList({ productStatus: 1, checkStatus: 1, ...param });
        const records = result?.records ?? [];
        productList.value = records.length > 0 ? records : demoProductList;
    } catch (error) {
        productList.value = demoProductList;
    }
};
_getproductList();

watchEffect(() => {
    _getproductList();
});
</script>
<style lang="less" scoped>
.image-empty-goods-bg {
    background: url("@/views/decorate/decorate/src/img/goods.png") no-repeat center center;
    background-size: 100% auto;
    width: 100px;
    margin: 0 auto 10px;
    height: 100px;
}
.module-product_a1 {
    // 商品卡主体默认白底：goodsPadding>0 时 padding 区随之为白，形成完整白色卡片；
    // goodsPadding=0 时图文铺满，白底被遮住，对默认观感无影响。
    :deep(.goods-ad-item .item-con) {
        background-color: #fff;
    }
    :deep(.goods-ad-item .item-photo) {
        position: relative;
    }
    :deep(.item-label-tag) {
        position: absolute;
        padding: 2px 8px;
        line-height: 1.4;
        z-index: 2;
        white-space: nowrap;
        font-weight: 500;
    }
    :deep(.item-brief-list) {
        display: flex;
        flex-wrap: wrap;
        margin-top: 6px;
    }
    :deep(.item-brief-tag) {
        padding: 1px 6px;
        margin-right: 4px;
        margin-bottom: 4px;
        border-radius: 2px;
        line-height: 16px;
        display: inline-block;
        white-space: nowrap;
    }
}
.module-product_a1.ad-goods_style__4 {
    // 透明底样式：卡片本就该融入页面背景，item-con 不加白底
    :deep(.goods-ad-item .item-con) {
        background-color: transparent;
    }
}
.module-product_a1.ad-style__5 {
    :deep(.goods-ad-warp .goods-ad-con .goods-ad-item .item-con) {
        align-items: stretch;
    }
    :deep(.goods-ad-warp .goods-ad-item .item-photo) {
        aspect-ratio: 1 / 1;
    }
    :deep(.goods-ad-warp .goods-ad-item .item-photo .item-image-a) {
        width: 100%;
        height: 100%;
        display: block;
    }
    :deep(.goods-ad-warp .goods-ad-item .item-photo img) {
        width: 100%;
        height: 100%;
        object-fit: cover;
    }
    :deep(.goods-ad-warp .goods-ad-item .item-con .item-info) {
        display: flex;
        flex-direction: column;
        padding: 10px 8px;
        box-sizing: border-box;
    }
    :deep(.goods-ad-warp .goods-ad-item .item-info .item-name) {
        flex: 1;
        margin: 0;
    }
    :deep(.goods-ad-warp .goods-ad-item .item-info .item-name .item-name-a) {
        height: auto;
        line-height: 20px;
        -webkit-line-clamp: 2;
        margin-bottom: 0;
    }
    :deep(.goods-ad-warp .goods-ad-item .item-info .item-action) {
        position: static !important;
        margin-top: auto;
        padding: 0;
        align-items: flex-end;
        gap: 4px;
    }
    :deep(.goods-ad-warp .goods-ad-item .item-info .item-price) {
        display: flex;
        align-items: baseline;
        flex-wrap: wrap;
        gap: 4px;
        height: auto;
        line-height: 1.4;
        min-width: 0;
    }
    :deep(.goods-ad-warp .goods-ad-item .item-info .market_price) {
        color: #999;
        font-size: 12px;
        padding-left: 0;
    }
    :deep(.goods-ad-warp .goods-ad-item .item-info .market_price b) {
        font-size: 11px;
        font-weight: normal;
        text-decoration: line-through;
    }
    :deep(.goods-ad-warp .goods-ad-item .item-info .item-price > b) {
        font-size: 16px;
    }
    :deep(.goods-ad-warp .goods-ad-item .item-info .item-price > b::first-letter) {
        font-size: 11px;
    }
}
.module-product_a1.ad-style__2,
.module-product_a1.ad-style__3 {
    :deep(.goods-ad-warp .goods-ad-item .item-photo) {
        aspect-ratio: 1 / 1;
        overflow: hidden;
    }
    :deep(.goods-ad-warp .goods-ad-item .item-photo .item-image-a) {
        width: 100%;
        height: 100%;
        display: block;
    }
    :deep(.goods-ad-warp .goods-ad-item .item-photo img) {
        width: 100%;
        height: 100%;
        object-fit: cover;
    }
    :deep(.goods-ad-warp .goods-ad-item .item-info .item-action) {
        flex-direction: column;
        align-items: stretch;
        gap: 4px;
        padding: 0 8px 10px;
        flex-wrap: nowrap;
    }
    :deep(.goods-ad-warp .goods-ad-item .item-info .item-price) {
        display: flex;
        align-items: baseline;
        flex-wrap: wrap;
        gap: 4px;
        line-height: 1.4;
        height: auto;
        min-width: 0;
        flex: none;
    }
    :deep(.goods-ad-warp .goods-ad-item .item-info .market_price) {
        color: #999;
        padding-left: 0;
    }
    :deep(.goods-ad-warp .goods-ad-item .item-info .market_price b) {
        font-weight: normal;
        text-decoration: line-through;
    }
}
.module-product_a1.ad-style__2 {
    :deep(.goods-ad-warp .goods-ad-item .item-info .item-price b) {
        font-size: 15px;
    }
    :deep(.goods-ad-warp .goods-ad-item .item-info .market_price),
    :deep(.goods-ad-warp .goods-ad-item .item-info .market_price b) {
        font-size: 12px;
    }
    :deep(.goods-ad-warp .goods-ad-item .item-info .item-buy) {
        width: 100%;
        max-width: 80px;
    }
}
.module-product_a1.ad-style__3 {
    :deep(.goods-ad-warp .goods-ad-item .item-info .item-price b) {
        font-size: 13px;
    }
    :deep(.goods-ad-warp .goods-ad-item .item-info .market_price),
    :deep(.goods-ad-warp .goods-ad-item .item-info .market_price b) {
        font-size: 10px;
    }
    :deep(.goods-ad-warp .goods-ad-item .item-info .item-buy) {
        display: flex !important;
        justify-content: center;
        height: auto;
        padding-bottom: 0;
    }
}
.item-image-a {
    display: block;

    img {
        display: block;
    }
}
</style>
