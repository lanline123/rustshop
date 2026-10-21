<template>
    <div class="module-ad-con module-limited_special" :style="outerStyle">
        <div class="ls-wrap" :style="containerBgStyle">
            <a class="ls-more" :style="moreStyle"></a>
            <div class="product-empty" v-if="!productList || productList.length === 0">
                <span>{{ $t('请先选择商品') }}</span>
            </div>
            <div v-else class="ls-scroll">
                <div class="ls-track" :style="trackStyle">
                    <div
                        class="ls-item"
                        v-for="(item, idx) in productList"
                        :key="(item.productId || 0) + '_' + idx"
                        :style="itemStyle"
                    >
                        <div class="ls-photo" :style="photoStyle">
                            <div class="ls-img-wrap" :style="imgWrapStyle">
                                <img :src="productImage(item)" alt="" :style="imgStyle" />
                            </div>
                            <div v-if="module.showBrief && module.productBriefList?.[idx]" class="ls-brief" :style="briefStyle">
                                {{ module.productBriefList[idx] }}
                            </div>
                        </div>
                        <div class="ls-info" :style="infoStyle">
                            <div v-if="module.showProductName" class="ls-name" :style="nameStyle">
                                {{ item.productName }}
                            </div>
                            <div class="ls-bottom">
                                <div class="ls-prices">
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
                                <div v-if="module.showSales || module.showComment" class="ls-extra" :style="{ color: module.productSalesColor }">
                                    <span v-if="module.showSales">{{ $t('已售{getSales}', { getSales: getSales(item) }) }}</span>
                                    <span v-if="module.showComment">{{ $t('{getComment}评价', { getComment: getComment(item) }) }}</span>
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
import { ref, computed, Ref, watchEffect } from "vue";
import { priceFormat, productImage } from "@/utils/format";
import { ModuleType, LimitedSpecialType } from "@/types/decorate/decorate.d";
import { getProductList } from "@/api/product/product";
import { mergeDefaultModule, defaultLimitedSpecial } from "@/views/decorate/decorate/src/modules/";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const module = defineModel<ModuleType & LimitedSpecialType>("module") as Ref<ModuleType & LimitedSpecialType>;
mergeDefaultModule(module.value, defaultLimitedSpecial);

const demoProducts: any[] = [
    {
        productId: 0,
        productName: t('婴儿连体衣 会员特价'),
        productPrice: 699.0,
        marketPrice: 999.0,
        salesVolume: 1200,
        commentCount: 320,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/17319190474y0XWFxO8GJZF0dg64.jpg"
    },
    {
        productId: 0,
        productName: t('纯棉新生儿套装 超级会员日'),
        productPrice: 699.0,
        marketPrice: 999.0,
        salesVolume: 860,
        commentCount: 210,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1731919071axvkfUDkQNFbIzF1iY.jpg"
    },
    {
        productId: 0,
        productName: t('可折叠婴儿浴盆 限时特惠'),
        productPrice: 699.0,
        marketPrice: 999.0,
        salesVolume: 540,
        commentCount: 130,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1731919071lOa0ZBx2mYlvq1lgjr.jpg"
    },
    {
        productId: 0,
        productName: t('婴儿洗护礼盒 热卖中'),
        productPrice: 699.0,
        marketPrice: 999.0,
        salesVolume: 320,
        commentCount: 88,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1731919071xUnObq2LokKPFecwXq.jpg"
    }
];

const productList = ref<any[]>([]);

const getSales = (item: any) => Number(item.salesVolume ?? item.virtualSales ?? item.salesCount ?? 0);
const getComment = (item: any) => Number(item.commentCount ?? item.commentNumber ?? 0);

const outerStyle = computed(() => {
    const f: any = module.value.frame || {};
    return [
        `padding-left:${f.boxPadding ?? 0}px;`,
        `padding-right:${f.boxPadding ?? 0}px;`,
        `padding-top:${f.boxPaddingTop ?? 0}px;`,
        `padding-bottom:${f.boxPaddingBottom ?? 0}px;`,
        f.backgroundColor ? `background-color:${f.backgroundColor};` : ""
    ].join("");
});

const containerBgStyle = computed(() => {
    const a = module.value.boxGradientStart || "#FF9EC4";
    const b = module.value.boxGradientEnd || "#FF6FA8";
    const r = (module.value.frame as any)?.boxRadius ?? 8;
    const bgImg = module.value.containerBgImage?.picUrl;
    const layers: string[] = [];
    const sizes: string[] = [];
    if (bgImg) {
        layers.push(`url(${bgImg})`);
        sizes.push("cover");
    }
    layers.push(`linear-gradient(to right, ${a}, ${b})`);
    sizes.push("auto");
    const py = module.value.productAreaPaddingY ?? 8;
    return `padding:${py}px 0;background-image:${layers.join(",")};background-size:${sizes.join(",")};background-position:center;background-repeat:no-repeat;border-radius:${r}px;`;
});

const ratioPaddingMap: Record<string, string> = {
    default: "",
    "1:1": "100%",
    "3:4": "133.333%",
    "3:2": "66.667%"
};

const trackStyle = computed(() => {
    const gap = module.value.productItemGap ?? 8;
    const left = module.value.productAreaLeft ?? 38;
    return `gap:${gap}px;padding-left:${left}%;`;
});

const moreStyle = computed(() => {
    const left = module.value.productAreaLeft ?? 38;
    return `width:${left}%;`;
});

const itemStyle = computed(() => {
    const padding = module.value.productItemPadding ?? 6;
    const percent = module.value.productPercent ?? 30;
    const bg = module.value.cardBgColor || "#FFFFFF";
    const radius = module.value.cardRadius ?? 8;
    return [
        `background-color:${bg};`,
        `border-radius:${radius}px;`,
        `padding:${padding}px;box-sizing:border-box;`,
        `flex:0 0 ${percent}%;width:${percent}%;`
    ].join("");
});

const photoStyle = computed(() => {
    const r = module.value.productPicRatio || "1:1";
    const padding = ratioPaddingMap[r];
    return padding ? `padding-top:${padding};` : "padding-top:0;";
});

const imgWrapStyle = computed(() => {
    const radius = module.value.productImgRadius ?? 8;
    const r = module.value.productPicRatio || "1:1";
    const padding = ratioPaddingMap[r];
    if (padding) {
        return `border-radius:${radius}px;position:absolute;inset:0;`;
    }
    return `border-radius:${radius}px;position:static;`;
});

const imgStyle = computed(() => {
    const fillType = module.value.productPicFillType || "cover";
    const r = module.value.productPicRatio || "1:1";
    const padding = ratioPaddingMap[r];
    if (padding) {
        return `object-fit:${fillType};position:absolute;inset:0;width:100%;height:100%;display:block;`;
    }
    return `width:100%;height:auto;display:block;`;
});

const briefStyle = computed(() => {
    const a = module.value.briefGradientStart || "#FF3B8B";
    const b = module.value.briefGradientEnd || "#FF7AB0";
    const color = module.value.briefTextColor || "#FFFFFF";
    return `background-image:linear-gradient(to bottom, ${a}, ${b});color:${color};`;
});

const infoStyle = computed(() => {
    return `text-align:${module.value.textAlign || "center"};`;
});

const nameStyle = computed(() => {
    return `color:${module.value.productTitleColor || "#333333"};font-weight:${module.value.textWeight || 400};`;
});

const priceStyle = computed(() => {
    const size = module.value.priceFontSize ?? 13;
    const weight = module.value.priceBold === 0 ? 400 : 700;
    return `color:${module.value.productPriceColor || "#FF3B8B"};font-size:${size}px;font-weight:${weight};`;
});

const buyBtnStyle = computed(() => {
    const a = module.value.buyBtnGradientStart || "#FF3B8B";
    const b = module.value.buyBtnGradientEnd || "#FF7AB0";
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
.module-limited_special {
    .ls-wrap {
        position: relative;
        box-sizing: border-box;
        min-height: 110px;
        overflow: hidden;
    }
    .ls-more {
        position: absolute;
        top: 0;
        left: 0;
        height: 100%;
        z-index: 6;
        display: block;
        cursor: pointer;
    }
    .ls-more:hover {
        outline: 1px dashed rgba(255, 255, 255, 0.9);
        outline-offset: -2px;
        background-color: rgba(0, 0, 0, 0.04);
    }
    .ls-more:hover::after {
        content: "查看更多 链接区";
        position: absolute;
        left: 50%;
        top: 8px;
        transform: translateX(-50%);
        padding: 1px 6px;
        font-size: 10px;
        line-height: 1.4;
        white-space: nowrap;
        color: #fff;
        background-color: rgba(0, 0, 0, 0.45);
        border-radius: 8px;
    }
    .product-empty {
        padding: 36px 0;
        text-align: center;
        color: #fff;
        font-size: 12px;
    }
    .ls-scroll {
        overflow-x: auto;
        overflow-y: hidden;
        scrollbar-width: none;
        -ms-overflow-style: none;
        &::-webkit-scrollbar {
            display: none;
        }
    }
    .ls-track {
        display: flex;
    }
    .ls-item {
        overflow: visible;
        display: flex;
        flex-direction: column;
    }
    .ls-photo {
        position: relative;
        width: 100%;
        overflow: visible;
    }
    .ls-img-wrap {
        overflow: hidden;
        box-sizing: border-box;
        background-color: #fafafa;
        transform: translateZ(0);
        -webkit-transform: translateZ(0);
    }
    .ls-brief {
        position: absolute;
        bottom: 0;
        left: 0;
        padding: 2px 6px;
        font-size: 12px;
        line-height: 1.2;
        border-radius: 4px;
        font-weight: 500;
        white-space: nowrap;
        max-width: 90%;
        overflow: hidden;
        text-overflow: ellipsis;
        box-sizing: border-box;
        z-index: 5;
        box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
    }
    .ls-info {
        margin-top: 6px;
    }
    .ls-name {
        font-size: 12px;
        line-height: 1.3;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        margin-bottom: 4px;
    }
    .ls-bottom {
        display: flex;
        flex-direction: column;
        gap: 2px;
    }
    .ls-prices {
        display: flex;
        align-items: baseline;
        gap: 4px;
        line-height: 1.1;
        flex-wrap: wrap;
        .price-current {
            white-space: nowrap;
            i {
                font-style: normal;
                font-size: 0.75em;
                margin-right: 1px;
            }
            b {
                font-size: inherit;
                font-weight: inherit;
            }
        }
        .price-market {
            font-size: 10px;
            text-decoration: line-through;
            font-weight: 400;
        }
    }
    .ls-extra {
        font-size: 10px;
        line-height: 1.3;
        display: flex;
        gap: 6px;
        flex-wrap: wrap;
    }
    .buy-btn {
        flex-shrink: 0;
        width: 22px;
        height: 22px;
        line-height: 22px;
        border-radius: 50%;
        text-align: center;
        font-size: 14px;
        color: #fff;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        margin-left: auto;
        .iconfont-h5 {
            font-size: 11px;
            line-height: 1;
        }
    }
    .buy-btn-style__5,
    .buy-btn-style__6,
    .buy-btn-style__7,
    .buy-btn-style__8 {
        width: auto;
        height: 22px;
        line-height: 22px;
        padding: 0 8px;
        font-size: 11px;
        border-radius: 11px;
    }
}
</style>
