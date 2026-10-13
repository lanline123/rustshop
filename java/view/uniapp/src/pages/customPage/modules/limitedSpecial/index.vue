<template>
    <view v-if="productList.length > 0" class="module-ad-con module-limited_special" :style="outerStyle">
        <view class="ls-wrap" :style="wrapStyle">
            <image v-if="containerBgUrl" class="ls-bg" :src="containerBgUrl" mode="aspectFill" />
            <view class="ls-more" :style="moreStyle" @click="urlFormat(module.moreLink) && redirect({ url: urlFormat(module.moreLink) })" />
            <view class="ls-scroll">
                <view class="ls-track" :style="trackStyle">
                    <view
                        v-for="(item, idx) in productList"
                        :key="(item.productId || 0) + '_' + idx"
                        class="ls-item"
                        :style="itemStyle"
                        @click="item.productId && redirect({ url: urlFormat({ path: 'product', data: { id: item.productId } }) })"
                    >
                        <view class="ls-photo" :style="photoStyle">
                            <view class="ls-img-wrap" :style="imgWrapStyle">
                                <image v-if="ratioPadding" class="ls-img ls-img-ratio" :mode="fillMode" :src="productImage(item)" />
                                <image v-else class="ls-img ls-img-fluid" mode="widthFix" :src="productImage(item)" />
                            </view>
                            <view v-if="showBrief && briefList[idx]" class="ls-brief" :style="briefStyle">
                                {{ briefList[idx] }}
                            </view>
                        </view>
                        <view class="ls-info" :style="infoStyle">
                            <view v-if="showProductName" class="ls-name" :style="nameStyle">
                                <text>{{ item.productName }}</text>
                            </view>
                            <view class="ls-bottom">
                                <view class="ls-prices">
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
                                <view v-if="showSales || showComment" class="ls-extra" :style="`color:${salesColor}`">
                                    <text v-if="showSales">{{ $t("已售") }}{{ getSales(item) }}</text>
                                    <text v-if="showComment">{{ getComment(item) }}{{ $t("评价") }}</text>
                                </view>
                            </view>
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
import { useConfigStore } from "@/store/config";

const configStore = useConfigStore();

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

// admin defaultLimitedSpecial 同源 fallback：处理老数据 / 不完整 payload
const showProductName = computed(() => Number(props.module.showProductName ?? 0) === 1);
const showPrice = computed(() => Number(props.module.showPrice ?? 1) === 1);
const showMarketPrice = computed(() => Number(props.module.showMarketPrice ?? 0) === 1);
const showSales = computed(() => Number(props.module.showSales ?? 0) === 1);
// D1146：装修开了「显示评价数」也要服从全局评价开关
const showComment = computed(() => configStore.productCommentEnabled === 1 && Number(props.module.showComment ?? 0) === 1);
const showBrief = computed(() => Number(props.module.showBrief ?? 0) === 1);
const buyBtnStyleNum = computed(() => Number(props.module.buyBtnStyle ?? 0));
const salesColor = computed(() => props.module.productSalesColor || "#999999");
const briefList = computed<string[]>(() => (Array.isArray(props.module.productBriefList) ? props.module.productBriefList : []));

const containerBgUrl = computed(() => {
    const url = props.module.containerBgImage?.picUrl;
    return url ? imageFormat(url) : "";
});

const outerStyle = computed(() => {
    const f: any = props.module.frame || {};
    return [
        `padding-left:${f.boxPadding ?? 0}px;`,
        `padding-right:${f.boxPadding ?? 0}px;`,
        `padding-top:${f.boxPaddingTop ?? 0}px;`,
        `padding-bottom:${f.boxPaddingBottom ?? 0}px;`,
        f.backgroundColor ? `background-color:${f.backgroundColor};` : ""
    ].join("");
});

const wrapStyle = computed(() => {
    const a = props.module.boxGradientStart || "#FF9EC4";
    const b = props.module.boxGradientEnd || "#FF6FA8";
    const r = (props.module.frame as any)?.boxRadius ?? 8;
    const py = props.module.productAreaPaddingY ?? 8;
    return `padding:${py}px 0;background-image:linear-gradient(to right, ${a}, ${b});border-radius:${r}px;`;
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
    const left = props.module.productAreaLeft ?? 38;
    return `padding-left:${left}%;`;
});

const moreStyle = computed(() => {
    const left = props.module.productAreaLeft ?? 38;
    return `width:${left}%;`;
});

const itemStyle = computed(() => {
    const padding = props.module.productItemPadding ?? 6;
    const percent = props.module.productPercent ?? 30;
    const bg = props.module.cardBgColor || "#FFFFFF";
    const radius = props.module.cardRadius ?? 8;
    const gap = props.module.productItemGap ?? 8;
    return [
        `background-color:${bg};`,
        `border-radius:${radius}px;`,
        `padding:${padding}px;box-sizing:border-box;`,
        `flex:0 0 ${percent}%;width:${percent}%;`,
        `margin-right:${gap}px;`
    ].join("");
});

const photoStyle = computed(() => (ratioPadding.value ? `padding-top:${ratioPadding.value};` : "padding-top:0;"));

const imgWrapStyle = computed(() => {
    const radius = props.module.productImgRadius ?? 8;
    if (ratioPadding.value) {
        return `border-radius:${radius}px;position:absolute;top:0;left:0;width:100%;height:100%;`;
    }
    return `border-radius:${radius}px;position:static;`;
});

const briefStyle = computed(() => {
    const a = props.module.briefGradientStart || "#FF3B8B";
    const b = props.module.briefGradientEnd || "#FF7AB0";
    const color = props.module.briefTextColor || "#FFFFFF";
    return `background-image:linear-gradient(to bottom, ${a}, ${b});color:${color};`;
});

const infoStyle = computed(() => `text-align:${props.module.textAlign || "center"};`);
const nameStyle = computed(() => `color:${props.module.productTitleColor || "#333333"};font-weight:${props.module.textWeight || 400};`);

const priceFontStyle = computed(() => {
    const size = props.module.priceFontSize ?? 13;
    return {
        fontSize: `${size}px`,
        fontWeight: props.module.priceBold === 0 ? 400 : 700,
        color: props.module.productPriceColor || "#FF3B8B",
        lineHeight: 1
    };
});
const priceCurrencyStyle = computed(() => {
    const size = props.module.priceFontSize ?? 13;
    return {
        fontSize: `${Math.round(size * 0.75)}px`,
        color: props.module.productPriceColor || "#FF3B8B"
    };
});
const marketPriceStyle = computed(() => ({
    fontSize: "10px",
    color: props.module.productMarketPriceColor || "#999999"
}));

const buyBtnStyle = computed(() => {
    const a = props.module.buyBtnGradientStart || "#FF3B8B";
    const b = props.module.buyBtnGradientEnd || "#FF7AB0";
    const color = props.module.buyBtnTextColor || "#FFFFFF";
    return `background-image:linear-gradient(to right, ${a}, ${b});color:${color};`;
});

const getSales = (item: any) => Number(item.salesVolume ?? item.virtualSales ?? item.salesCount ?? 0);
const getComment = (item: any) => Number(item.commentCount ?? item.commentNumber ?? 0);
const marketPriceVisible = (item: any) => Number(item.marketPrice) > Number(item.productPrice);

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
.module-limited_special {
    .ls-wrap {
        position: relative;
        box-sizing: border-box;
        min-height: 110px;
        overflow: hidden;
    }
    .ls-bg {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        z-index: 0;
    }
    .ls-more {
        position: absolute;
        top: 0;
        left: 0;
        height: 100%;
        z-index: 6;
    }
    .ls-scroll {
        position: relative;
        z-index: 1;
        overflow-x: auto;
        overflow-y: hidden;
        &::-webkit-scrollbar {
            display: none;
            width: 0;
            height: 0;
        }
    }
    .ls-track {
        display: flex;
        flex-wrap: nowrap;
    }
    .ls-item {
        overflow: visible;
        display: flex;
        flex-direction: column;
        flex-shrink: 0;
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
    }
    .ls-img {
        display: block;
    }
    .ls-img-fluid {
        width: 100%;
        height: auto;
    }
    .ls-img-ratio {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
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
    .ls-bottom {
        display: flex;
        flex-direction: column;
        gap: 2px;
    }
    .ls-prices {
        display: flex;
        align-items: baseline;
        flex-wrap: wrap;
        gap: 4px;
        line-height: 1.1;
    }
    .price-current {
        white-space: nowrap;
    }
    .price-market {
        text-decoration: line-through;
        :deep(.price-content),
        :deep(.num) {
            text-decoration: inherit;
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
        color: #fff;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-left: auto;
        .buy-btn-icon {
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
