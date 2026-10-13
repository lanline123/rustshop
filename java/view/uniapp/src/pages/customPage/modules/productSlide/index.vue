<template>
    <view v-if="productList.length > 0" class="module-ad-con module-product_slide" :style="outerStyle">
        <image v-if="moduleBgUrl" class="ps-module-bg" :src="moduleBgUrl" mode="aspectFill" />
        <view class="ps-wrap" :style="wrapStyle">
            <view class="ps-scroll">
                <view class="ps-track" :style="trackStyle">
                    <view
                        v-for="(item, idx) in productList"
                        :key="(item.productId || 0) + '_' + idx"
                        class="ps-item"
                        :style="itemStyle(idx)"
                        @click="item.productId && redirect({ url: urlFormat({ path: 'product', data: { id: item.productId } }) })"
                    >
                        <view v-if="heroTitle(idx)" class="ps-hero" :style="heroStyle">{{ heroTitle(idx) }}</view>
                        <view v-if="showProductName" class="ps-name" :style="descStyle">
                            <text>{{ item.productName }}</text>
                        </view>
                        <view v-if="showFeature && getFeature(item)" class="ps-feature" :style="descStyle">{{ getFeature(item) }}</view>

                        <view class="ps-photo" :style="photoStyle">
                            <view class="ps-img-wrap" :style="imgWrapStyle">
                                <image v-if="ratioPadding" class="ps-img ps-img-ratio" :mode="fillMode" :style="platformStyle" :src="productImage(item)" />
                                <image v-else class="ps-img ps-img-fluid" mode="widthFix" :style="platformStyle" :src="productImage(item)" />
                            </view>
                            <view v-if="showBrief && getBrief(item)" class="ps-badge" :style="badgeStyle">{{ getBrief(item) }}</view>
                        </view>

                        <view class="ps-bottom" :style="bottomStyle">
                            <view class="ps-prices">
                                <view v-if="showPrice" class="price-current">
                                    <format-price
                                        :font-style="priceFontStyle"
                                        :currency-style="priceCurrencyStyle"
                                        :price-data="item.deliveryMode === 2 ? item.cyclePrice : item.productPrice"
                                    />
                                </view>
                                <view v-if="showMarketPrice && marketPriceVisible(item)" class="price-market">
                                    <format-price :font-style="marketPriceStyle" :currency-style="marketPriceStyle" :price-data="item.marketPrice" />
                                </view>
                                <view
                                    v-if="buyBtnStyleNum > 0"
                                    class="buy-btn"
                                    :class="`buy-btn-style__${buyBtnStyleNum}`"
                                    :style="buyBtnStyle"
                                    @click.stop="item.productId && redirect({ url: `/pages/product/index?id=${item.productId}` })"
                                >
                                    <template v-if="buyBtnStyleNum === 5 || buyBtnStyleNum === 6"
                                        ><text class="buy-btn-text">{{ $t("购买") }}</text></template
                                    >
                                    <template v-else-if="buyBtnStyleNum === 7 || buyBtnStyleNum === 8"
                                        ><text class="buy-btn-text">{{ $t("马上抢") }}</text></template
                                    >
                                    <template v-else><text class="iconfont-h5 buy-btn-icon" /></template>
                                </view>
                            </view>
                            <view v-if="showSales" class="ps-sales" :style="`color:${salesColor}`">{{ $t("已售") }}{{ getSales(item) }}</view>
                        </view>
                    </view>
                </view>
            </view>
        </view>
    </view>
</template>

<script lang="ts" setup>
import { ref, computed, watch } from "vue";
import { imageFormat, urlFormat, productImage } from "@/utils/format";
import { redirect } from "@/utils";
import { getDecorateProducts } from "@/components/modules/useDecorateProducts";

const props = defineProps({
    module: {
        type: Object,
        default: () => ({})
    },
    moduleIndex: {
        type: Number,
        default: 0
    },
    shopId: {
        type: Number,
        default: -1
    }
});

// 逐卡渐变兜底调色板（与 admin defaultProductSlide 对齐）
const gradientFallback = [
    ["#B6A8F5", "#8E7BF0"],
    ["#FF8FB8", "#FF5E96"],
    ["#FFD98A", "#FBC25A"],
    ["#C7BEF8", "#9C8CF0"]
];

// admin defaultProductSlide 同源 fallback：处理老数据 / 不完整 payload
const showProductName = computed(() => Number(props.module.showProductName ?? 1) === 1);
const showFeature = computed(() => Number(props.module.showFeature ?? 0) === 1);
const showBrief = computed(() => Number(props.module.showBrief ?? 0) === 1);
const showPrice = computed(() => Number(props.module.showPrice ?? 1) === 1);
const showSales = computed(() => Number(props.module.showSales ?? 0) === 1);
const showMarketPrice = computed(() => Number(props.module.showMarketPrice ?? 0) === 1);
const buyBtnStyleNum = computed(() => Number(props.module.buyBtnStyle ?? 0));
const salesColor = computed(() => props.module.productSalesColor || "#FFFFFF");

const moduleBgUrl = computed(() => {
    const url = props.module.moduleBgImage?.picUrl;
    return url ? imageFormat(url) : "";
});

const getSales = (item: any) => Number(item.salesVolume ?? item.virtualSales ?? item.salesCount ?? 0);
const getBrief = (item: any) => item.brief || item.sellingPoint || item.productBrief || "";
const getFeature = (item: any) => item.feature || item.productFeature || item.intro || "";
const heroTitle = (idx: number | string) => {
    const list = props.module.cardTitleList;
    return Array.isArray(list) ? list[Number(idx)] || "" : "";
};
const marketPriceVisible = (item: any) => Number(item.marketPrice) > Number(item.productPrice);

const outerStyle = computed(() => {
    const f: any = props.module.frame || {};
    return [
        `padding-left:${f.boxPadding ?? 8}px;`,
        `padding-right:${f.boxPadding ?? 8}px;`,
        `padding-top:${f.boxPaddingTop ?? 5}px;`,
        `padding-bottom:${f.boxPaddingBottom ?? 0}px;`,
        f.backgroundColor ? `background-color:${f.backgroundColor};` : ""
    ].join("");
});

const wrapStyle = computed(() => {
    const r = (props.module.frame as any)?.boxRadius ?? 16;
    const bg = props.module.innerBgColor || "transparent";
    return `background-color:${bg};border-radius:${r}px;`;
});

const ratioPaddingMap: Record<string, string> = {
    default: "",
    "1:1": "100%",
    "3:4": "133.333%",
    "3:2": "66.667%"
};
const ratioPadding = computed(() => ratioPaddingMap[props.module.productPicRatio || "1:1"] || "");
const fillMode = computed(() => ((props.module.productPicFillType || "cover") === "contain" ? "aspectFit" : "aspectFill"));

const trackStyle = computed(() => {
    const gap = props.module.productItemGap ?? 10;
    return `gap:${gap}px;`;
});

const itemStyle = (idx: number | string) => {
    const i = Number(idx);
    const startList = props.module.cardGradientStartList;
    const endList = props.module.cardGradientEndList;
    const a = (Array.isArray(startList) && startList[i]) || gradientFallback[i % gradientFallback.length][0];
    const b = (Array.isArray(endList) && endList[i]) || gradientFallback[i % gradientFallback.length][1];
    const percent = props.module.productPercent ?? 42;
    const radius = props.module.cardRadius ?? 16;
    const gap = props.module.layerGap ?? 10;
    const align = props.module.textAlign || "center";
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

const heroStyle = computed(() => `color:${props.module.cardTitleColor || "#FFFFFF"};font-weight:${props.module.textWeight || 700};`);
const descStyle = computed(() => `color:${props.module.descColor || "#FFFFFF"};`);

const photoStyle = computed(() => (ratioPadding.value ? `padding-top:${ratioPadding.value};` : "padding-top:0;"));

const cardStyleBg = () => {
    switch (props.module.cardStyle) {
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
    const radius = props.module.productImgRadius ?? 12;
    const base = cardStyleBg();
    if (ratioPadding.value) {
        return `${base}border-radius:${radius}px;position:absolute;top:0;left:0;width:100%;height:100%;`;
    }
    return `${base}border-radius:${radius}px;position:static;`;
});

const platformStyle = computed(() => (Number(props.module.showImagePlatform ?? 0) === 1 ? "filter:drop-shadow(0 6px 5px rgba(0,0,0,0.22));" : ""));

const badgeStyle = computed(() => `background-color:${props.module.badgeColor || "#FE3A01"};color:#ffffff;`);

const bottomStyle = computed(() => {
    const align = props.module.textAlign || "center";
    const justify = align === "left" ? "flex-start" : align === "right" ? "flex-end" : "center";
    return `justify-content:${justify};`;
});

const priceFontStyle = computed(() => {
    const size = props.module.priceFontSize ?? 15;
    return {
        fontSize: `${size}px`,
        fontWeight: props.module.priceBold === 0 ? 400 : 700,
        color: props.module.productPriceColor || "#FFFFFF",
        lineHeight: 1
    };
});
const priceCurrencyStyle = computed(() => {
    const size = props.module.priceFontSize ?? 15;
    return {
        fontSize: `${Math.round(size * 0.7)}px`,
        color: props.module.productPriceColor || "#FFFFFF"
    };
});
const marketPriceStyle = computed(() => ({
    fontSize: "11px",
    color: props.module.productMarketPriceColor || "#FFFFFF"
}));

const buyBtnStyle = computed(() => {
    const a = props.module.buyBtnGradientStart || "#FF6FA8";
    const b = props.module.buyBtnGradientEnd || "#FF3B8B";
    const color = props.module.buyBtnTextColor || "#FFFFFF";
    return `background-image:linear-gradient(to right, ${a}, ${b});color:${color};`;
});

const productList = ref<any[]>([]);
const _getProductList = async () => {
    try {
        productList.value = await getDecorateProducts(props.module.products, {
            shopId: props.shopId,
            extraParams: { productStatus: 1, checkStatus: 1 }
        });
    } catch (error) {
        console.log(error);
    }
};
_getProductList();

watch(
    () => JSON.stringify(props.module.products ?? {}),
    () => {
        _getProductList();
    }
);
</script>

<style lang="scss" scoped>
.module-product_slide {
    position: relative;
    background-size: cover;
    background-repeat: no-repeat;
    background-position: center;
    .ps-module-bg {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        z-index: 0;
    }
    .ps-wrap {
        position: relative;
        z-index: 1;
        box-sizing: border-box;
        min-height: 120px;
        overflow: hidden;
    }
    .ps-scroll {
        overflow-x: auto;
        overflow-y: hidden;
        &::-webkit-scrollbar {
            display: none;
            width: 0;
            height: 0;
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
        flex-shrink: 0;
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
    .cycleBuyTag {
        display: inline-block;
        color: var(--general);
        position: relative;
        padding-right: 10px;
        vertical-align: baseline;
        &::before {
            position: absolute;
            content: "";
            right: 4px;
            top: 0;
            width: 1px;
            height: 70%;
            transform: translateY(22%);
            background-color: var(--general);
        }
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
    }
    .ps-img {
        display: block;
    }
    .ps-img-fluid {
        width: 100%;
        height: auto;
    }
    .ps-img-ratio {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
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
    }
    .price-current {
        white-space: nowrap;
    }
    .price-market {
        text-decoration: line-through;
        opacity: 0.85;
        :deep(.price-content),
        :deep(.num) {
            text-decoration: inherit;
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
        color: #fff;
        display: flex;
        align-items: center;
        justify-content: center;
        .buy-btn-icon {
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
    .buy-btn-style__1 .buy-btn-icon::before {
        content: "\e611";
    }
    .buy-btn-style__2 .buy-btn-icon::before {
        content: "\e664";
    }
    .buy-btn-style__3 .buy-btn-icon::before {
        content: "\e74a";
    }
    .buy-btn-style__4 .buy-btn-icon::before {
        content: "\e620";
    }
}
</style>
