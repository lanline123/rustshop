<template>
    <div class="module-ad-con module-product_slide" :style="outerStyle">
        <div class="ps-wrap" :style="wrapStyle">
            <div class="product-empty" v-if="!productList || productList.length === 0">
                <span>{{ $t('请先选择商品') }}</span>
            </div>
            <div v-else class="ps-scroll">
                <div class="ps-track" :style="trackStyle">
                    <div
                        class="ps-item"
                        v-for="(item, idx) in productList"
                        :key="(item.productId || 0) + '_' + idx"
                        :style="itemStyle(idx)"
                    >
                        <div v-if="heroTitle(idx)" class="ps-hero" :style="heroStyle">{{ heroTitle(idx) }}</div>
                        <div v-if="module.showProductName" class="ps-name" :style="descStyle">{{ item.productName }}</div>
                        <div v-if="module.showFeature && getFeature(item)" class="ps-feature" :style="descStyle">{{ getFeature(item) }}</div>

                        <div class="ps-photo" :style="photoStyle">
                            <div class="ps-img-wrap" :style="imgWrapStyle">
                                <img :src="productImage(item)" alt="" :style="imgStyle" />
                            </div>
                            <div v-if="module.showBrief && getBrief(item)" class="ps-badge" :style="badgeStyle">{{ getBrief(item) }}</div>
                        </div>

                        <div class="ps-bottom" :style="bottomStyle">
                            <div class="ps-prices">
                                <span v-if="module.showPrice" class="price-current" :style="priceStyle">
                                    <i>¥</i><b>{{ priceFormat(Number(item.productPrice), false) }}</b>
                                </span>
                                <span
                                    v-if="module.showMarketPrice && Number(item.marketPrice) > Number(item.productPrice)"
                                    class="price-market"
                                    :style="{ color: module.productMarketPriceColor }"
                                >
                                    ¥{{ priceFormat(Number(item.marketPrice), false) }}
                                </span>
                                <a
                                    v-if="module.buyBtnStyle && module.buyBtnStyle > 0"
                                    class="buy-btn"
                                    :class="`buy-btn-style__${module.buyBtnStyle}`"
                                    :style="buyBtnStyle"
                                >
                                    <template v-if="module.buyBtnStyle === 5 || module.buyBtnStyle === 6"><span>{{ $t('购买') }}</span></template>
                                    <template v-else-if="module.buyBtnStyle === 7 || module.buyBtnStyle === 8"><span>{{ $t('马上抢') }}</span></template>
                                    <template v-else-if="module.buyBtnStyle === 1"><span class="iconfont-h5 icon-gouwuche"></span></template>
                                    <template v-else-if="module.buyBtnStyle === 2"><span class="iconfont-h5 icon-gouwuche1"></span></template>
                                    <template v-else-if="module.buyBtnStyle === 3"><span class="iconfont-h5 icon-jia"></span></template>
                                    <template v-else-if="module.buyBtnStyle === 4"><span class="iconfont-h5 icon-jia1"></span></template>
                                </a>
                            </div>
                            <div v-if="module.showSales" class="ps-sales" :style="{ color: module.productSalesColor }">{{ $t('已售{getSales}', { getSales: getSales(item) }) }}</div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref, computed, Ref, watchEffect } from "vue";
import { priceFormat, productImage } from "@/utils/format";
import { ModuleType, ProductSlideType } from "@/types/decorate/decorate.d";
import { getProductList } from "@/api/product/product";
import { mergeDefaultModule, defaultProductSlide } from "@/views/decorate/decorate/src/modules/";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const module = defineModel<ModuleType & ProductSlideType>("module") as Ref<ModuleType & ProductSlideType>;
mergeDefaultModule(module.value, defaultProductSlide);

// 逐卡渐变兜底调色板（与默认 demo 对齐）
const gradientFallback = [
    ["#B6A8F5", "#8E7BF0"],
    ["#FF8FB8", "#FF5E96"],
    ["#FFD98A", "#FBC25A"],
    ["#C7BEF8", "#9C8CF0"]
];

const demoProducts: any[] = [
    {
        productId: 0,
        productName: t('纯棉婴儿连体衣'),
        productPrice: 89.0,
        marketPrice: 129.0,
        salesVolume: 1200,
        brief: t('新品'),
        feature: t('A类纯棉 亲肤透气'),
        picThumb: "https://oss.tigshop.com/img/gallery/202411/17319190474y0XWFxO8GJZF0dg64.jpg"
    },
    {
        productId: 0,
        productName: t('婴幼儿配方奶粉'),
        productPrice: 89.0,
        marketPrice: 129.0,
        salesVolume: 860,
        brief: t('热卖'),
        feature: t('0 蔗糖 易吸收'),
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1731919071axvkfUDkQNFbIzF1iY.jpg"
    },
    {
        productId: 0,
        productName: t('婴儿洗护套装'),
        productPrice: 89.0,
        marketPrice: 129.0,
        salesVolume: 540,
        brief: t('推荐'),
        feature: t('温和无泪 弱酸性'),
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1731919071lOa0ZBx2mYlvq1lgjr.jpg"
    },
    {
        productId: 0,
        productName: t('可折叠婴儿浴盆'),
        productPrice: 89.0,
        marketPrice: 129.0,
        salesVolume: 320,
        brief: t('好评'),
        feature: t('加大加厚 防滑'),
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1731919071xUnObq2LokKPFecwXq.jpg"
    }
];

const productList = ref<any[]>([]);

const getSales = (item: any) => Number(item.salesVolume ?? item.virtualSales ?? item.salesCount ?? 0);
const getBrief = (item: any) => item.brief || item.sellingPoint || item.productBrief || "";
const getFeature = (item: any) => item.feature || item.productFeature || item.intro || "";
const heroTitle = (idx: number) => module.value.cardTitleList?.[idx] || "";

const outerStyle = computed(() => {
    const f: any = module.value.frame || {};
    const bgImg = module.value.moduleBgImage?.picUrl;
    return [
        `padding-left:${f.boxPadding ?? 0}px;`,
        `padding-right:${f.boxPadding ?? 0}px;`,
        `padding-top:${f.boxPaddingTop ?? 0}px;`,
        `padding-bottom:${f.boxPaddingBottom ?? 0}px;`,
        f.backgroundColor ? `background-color:${f.backgroundColor};` : "",
        bgImg ? `background-image:url(${bgImg});background-size:cover;background-position:center;background-repeat:no-repeat;` : ""
    ].join("");
});

const wrapStyle = computed(() => {
    const r = (module.value.frame as any)?.boxRadius ?? 16;
    const bg = module.value.innerBgColor || "transparent";
    return `background-color:${bg};border-radius:${r}px;`;
});

const ratioPaddingMap: Record<string, string> = {
    default: "",
    "1:1": "100%",
    "3:4": "133.333%",
    "3:2": "66.667%"
};

const trackStyle = computed(() => {
    const gap = module.value.productItemGap ?? 10;
    return `gap:${gap}px;`;
});

const itemStyle = (idx: number) => {
    const a = module.value.cardGradientStartList?.[idx] || gradientFallback[idx % gradientFallback.length][0];
    const b = module.value.cardGradientEndList?.[idx] || gradientFallback[idx % gradientFallback.length][1];
    const percent = module.value.productPercent ?? 42;
    const radius = module.value.cardRadius ?? 16;
    const gap = module.value.layerGap ?? 10;
    const align = module.value.textAlign || "center";
    const itemsAlign = align === "left" ? "flex-start" : align === "right" ? "flex-end" : "center";
    return [
        `flex:0 0 ${percent}%;width:${percent}%;`,
        `background-image:linear-gradient(to bottom, ${a}, ${b});`,
        `border-radius:${radius}px;`,
        `gap:${gap}px;`,
        `align-items:${itemsAlign};`,
        `text-align:${align};`
    ].join("");
};

const heroStyle = computed(() => {
    return `color:${module.value.cardTitleColor || "#FFFFFF"};font-weight:${module.value.textWeight || 700};`;
});

const descStyle = computed(() => {
    return `color:${module.value.descColor || "#FFFFFF"};`;
});

const photoStyle = computed(() => {
    const r = module.value.productPicRatio || "1:1";
    const padding = ratioPaddingMap[r];
    return padding ? `padding-top:${padding};` : "padding-top:0;";
});

const cardStyleBg = () => {
    switch (module.value.cardStyle) {
        case "noBorderWhite":
            return "background-color:#ffffff;";
        case "shadow":
            return "background-color:#ffffff;box-shadow:0 4px 12px rgba(0,0,0,0.12);";
        case "border":
            return "background-color:#ffffff;border:1px solid rgba(0,0,0,0.08);";
        default:
            return "background-color:transparent;";
    }
};

const imgWrapStyle = computed(() => {
    const radius = module.value.productImgRadius ?? 12;
    const r = module.value.productPicRatio || "1:1";
    const padding = ratioPaddingMap[r];
    const base = cardStyleBg();
    if (padding) {
        return `${base}border-radius:${radius}px;position:absolute;inset:0;`;
    }
    return `${base}border-radius:${radius}px;position:static;`;
});

const imgStyle = computed(() => {
    const fillType = module.value.productPicFillType || "cover";
    const r = module.value.productPicRatio || "1:1";
    const padding = ratioPaddingMap[r];
    const platform = module.value.showImagePlatform ? "filter:drop-shadow(0 6px 5px rgba(0,0,0,0.22));" : "";
    if (padding) {
        return `${platform}object-fit:${fillType};position:absolute;inset:0;width:100%;height:100%;display:block;`;
    }
    return `${platform}width:100%;height:auto;display:block;`;
});

const badgeStyle = computed(() => {
    return `background-color:${module.value.badgeColor || "#FE3A01"};color:#ffffff;`;
});

const bottomStyle = computed(() => {
    const align = module.value.textAlign || "center";
    const justify = align === "left" ? "flex-start" : align === "right" ? "flex-end" : "center";
    return `justify-content:${justify};`;
});

const priceStyle = computed(() => {
    const size = module.value.priceFontSize ?? 15;
    const weight = module.value.priceBold === 0 ? 400 : 700;
    return `color:${module.value.productPriceColor || "#FFFFFF"};font-size:${size}px;font-weight:${weight};`;
});

const buyBtnStyle = computed(() => {
    const a = module.value.buyBtnGradientStart || "#FF6FA8";
    const b = module.value.buyBtnGradientEnd || "#FF3B8B";
    const color = module.value.buyBtnTextColor || "#FFFFFF";
    return `background-image:linear-gradient(to right, ${a}, ${b});color:${color};`;
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

const _getProductList = async () => {
    const products: any = module.value.products;
    if (!products || !products.productSelectType) {
        productList.value = demoProducts;
        return;
    }
    const param: any = {};
    const type = products.productSelectType;
    const keys = paramsValueMap[type];
    if (!keys) {
        productList.value = demoProducts;
        return;
    }
    if (type === 1) {
        const ids: number[] = products.productIds || [];
        if (!ids.length) {
            productList.value = demoProducts;
            return;
        }
        param[paramsKeyMap[keys[0]]] = ids.join(",");
    } else if (type === 2) {
        const catIds = products.productCategoryId;
        const hasCategory = Array.isArray(catIds) ? catIds.length > 0 : !!catIds;
        if (!hasCategory || !products.productNumber) {
            productList.value = demoProducts;
            return;
        }
        param.page = 1;
        keys.forEach((key) => {
            param[paramsKeyMap[key]] = products[key];
        });
    } else if (type === 3) {
        if (!products.productTag || !products.productNumber) {
            productList.value = demoProducts;
            return;
        }
        param.page = 1;
        keys.forEach((key) => {
            param[paramsKeyMap[key]] = products[key];
        });
    }
    try {
        const result = await getProductList({ productStatus: 1, checkStatus: 1, ...param });
        productList.value = result.records && result.records.length > 0 ? result.records : demoProducts;
    } catch (error) {
        productList.value = demoProducts;
    }
};

watchEffect(() => {
    _getProductList();
});
</script>
<style lang="less" scoped>
.module-product_slide {
    background-size: cover;
    background-repeat: no-repeat;
    background-position: center;
    .ps-wrap {
        position: relative;
        box-sizing: border-box;
        min-height: 120px;
        overflow: hidden;
    }
    .product-empty {
        padding: 40px 0;
        text-align: center;
        color: #999;
        font-size: 12px;
    }
    .ps-scroll {
        overflow-x: auto;
        overflow-y: hidden;
        scrollbar-width: none;
        -ms-overflow-style: none;
        &::-webkit-scrollbar {
            display: none;
        }
    }
    .ps-track {
        display: flex;
        padding: 6px;
        align-items: stretch;
    }
    .ps-item {
        box-sizing: border-box;
        display: flex;
        flex-direction: column;
        padding: 14px 10px;
        overflow: visible;
    }
    .ps-hero {
        font-size: 17px;
        line-height: 1.2;
        width: 100%;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
    }
    .ps-name {
        font-size: 13px;
        line-height: 1.3;
        width: 100%;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        opacity: 0.95;
    }
    .ps-feature {
        font-size: 11px;
        line-height: 1.3;
        width: 100%;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        opacity: 0.85;
    }
    .ps-photo {
        position: relative;
        width: 100%;
        overflow: visible;
        margin: 2px 0;
    }
    .ps-img-wrap {
        overflow: hidden;
        box-sizing: border-box;
        transform: translateZ(0);
        -webkit-transform: translateZ(0);
    }
    .ps-badge {
        position: absolute;
        top: 6px;
        right: 6px;
        padding: 1px 6px;
        font-size: 10px;
        line-height: 1.5;
        border-radius: 8px;
        font-weight: 500;
        white-space: nowrap;
        max-width: 80%;
        overflow: hidden;
        text-overflow: ellipsis;
        z-index: 5;
        box-shadow: 0 1px 3px rgba(0, 0, 0, 0.15);
    }
    .ps-bottom {
        display: flex;
        align-items: center;
        flex-wrap: wrap;
        gap: 4px 8px;
        width: 100%;
    }
    .ps-prices {
        display: flex;
        align-items: baseline;
        gap: 4px;
        line-height: 1.1;
        flex-wrap: wrap;
        .price-current {
            white-space: nowrap;
            i {
                font-style: normal;
                font-size: 0.7em;
                margin-right: 1px;
            }
            b {
                font-size: inherit;
                font-weight: inherit;
            }
        }
        .price-market {
            font-size: 11px;
            text-decoration: line-through;
            font-weight: 400;
            opacity: 0.85;
        }
    }
    .ps-sales {
        font-size: 10px;
        line-height: 1.3;
        opacity: 0.9;
    }
    .buy-btn {
        flex-shrink: 0;
        width: 24px;
        height: 24px;
        line-height: 24px;
        border-radius: 50%;
        text-align: center;
        font-size: 14px;
        color: #fff;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        .iconfont-h5 {
            font-size: 12px;
            line-height: 1;
        }
    }
    .buy-btn-style__5,
    .buy-btn-style__6,
    .buy-btn-style__7,
    .buy-btn-style__8 {
        width: auto;
        height: 24px;
        line-height: 24px;
        padding: 0 10px;
        font-size: 11px;
        border-radius: 12px;
    }
}
</style>
