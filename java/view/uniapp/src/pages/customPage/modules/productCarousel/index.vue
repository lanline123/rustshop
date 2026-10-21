<template>
    <view v-if="displayList.length > 0" class="module-ad-con module-product_carousel" :style="outerStyle">
        <image v-if="moduleBgUrl" class="pc-module-bg" :src="moduleBgUrl" mode="aspectFill" />
        <view class="pc-body">
            <view v-if="showModuleTitle && moduleTitle" class="pc-module-title" :style="titleStyle">{{ moduleTitle }}</view>
            <swiper
                class="pc-swiper"
                :style="{ height: swiperHeight + 'px' }"
                :circular="displayList.length > 2"
                :autoplay="autoplay"
                :interval="3000"
                :duration="500"
                :previous-margin="peekMargin"
                :next-margin="peekMargin"
                :indicator-dots="false"
                @change="onSwiperChange"
            >
                <swiper-item v-for="(item, idx) in displayList" :key="(item.productId || 0) + '_' + idx">
                    <view class="pc-cell" :style="cellStyle">
                        <view class="pc-measure">
                            <view class="pc-card" :class="{ 'pc-card-active': currentIndex === idx }" :style="cardVisualStyle" @click="goDetail(item)">
                                <view class="pc-photo">
                                    <view class="pc-img-box" :style="imgBoxStyle">
                                        <image v-if="ratioPadding" class="pc-img pc-img-ratio" mode="aspectFill" :src="productImage(item)" />
                                        <image v-else class="pc-img pc-img-fluid" mode="widthFix" :src="productImage(item)" @load="recalcHeight" />
                                    </view>
                                </view>
                                <view class="pc-info" :style="infoStyle">
                                    <view v-if="showName" class="pc-name" :style="nameStyle">
                                        <text>{{ item.productName }}</text>
                                    </view>
                                    <view v-if="showDesc && item.productDesc" class="pc-desc" :style="descStyle">{{ item.productDesc }}</view>
                                    <view v-if="showSales" class="pc-sales" :style="salesStyle">{{ $t("已售") }}{{ getSales(item) }}</view>
                                    <view class="pc-bottom">
                                        <view v-if="showPrice" class="pc-prices">
                                            <view class="price-current">
                                                <format-price
                                                    :font-style="priceFontStyle"
                                                    :currency-style="priceCurrencyStyle"
                                                    :price-data="item.deliveryMode === 2 ? item.cyclePrice : item.productPrice"
                                                />
                                            </view>
                                            <view v-if="showMarketPrice && marketPriceVisible(item)" class="price-market">
                                                <format-price
                                                    :font-style="marketPriceFontStyle"
                                                    :currency-style="marketPriceFontStyle"
                                                    :price-data="item.marketPrice"
                                                />
                                            </view>
                                        </view>
                                        <commonBuyButton v-if="safeBuyBtnStyle && safeBuyBtnStyle.showBtn" class="pc-buy" :style-options="safeBuyBtnStyle" />
                                    </view>
                                </view>
                            </view>
                        </view>
                    </view>
                </swiper-item>
            </swiper>
        </view>
    </view>
</template>

<script lang="ts" setup>
import { ref, computed, watch, nextTick, getCurrentInstance, onMounted, onUnmounted } from "vue";
import { imageFormat, urlFormat, productImage } from "@/utils/format";
import { redirect, getElementRect } from "@/utils";
import { getProductsList } from "@/api/common";
import commonBuyButton from "@/pages/customPage/modules/commonBuyButton/index.vue";

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

// admin defaultProductCarousel 同源 fallback：处理老数据 / 不完整 payload
const showModuleTitle = computed(() => Number(props.module.showModuleTitle ?? 0) === 1);
const moduleTitle = computed(() => props.module.moduleTitle || "");
const autoplay = computed(() => Number(props.module.autoplay ?? 0) === 1);
const showName = computed(() => Number(props.module.showName ?? 1) === 1);
const showDesc = computed(() => Number(props.module.showDesc ?? 0) === 1);
const showPrice = computed(() => Number(props.module.showPrice ?? 1) === 1);
const showSales = computed(() => Number(props.module.showSales ?? 0) === 1);
const showMarketPrice = computed(() => Number(props.module.showMarketPrice ?? 1) === 1);

// 图片比例锁定（padding-top 百分比 hack，兼容小程序）；default 不锁比例
const ratioPaddingMap: Record<string, string> = {
    default: "",
    "1:1": "100%",
    "3:4": "133.333%",
    "3:2": "66.667%"
};
const ratioPadding = computed(() => ratioPaddingMap[props.module.productPicRatio || "1:1"] || "");

const getSales = (item: any) => Number(item.salesVolume ?? item.virtualSales ?? 0);
const marketPriceVisible = (item: any) => Number(item.marketPrice) > Number(item.productPrice);

const moduleBgUrl = computed(() => {
    const url = props.module.moduleBgImage?.picUrl;
    return url ? imageFormat(url) : "";
});

const outerStyle = computed(() => {
    const f: any = props.module.frame || {};
    return [
        `padding-left:${f.boxPadding ?? 0}px;`,
        `padding-right:${f.boxPadding ?? 0}px;`,
        `padding-top:${f.boxPaddingTop ?? 10}px;`,
        `padding-bottom:${f.boxPaddingBottom ?? 10}px;`,
        f.backgroundColor ? `background-color:${f.backgroundColor};` : ""
    ].join("");
});

const titleStyle = computed(
    () =>
        `color:${props.module.moduleTitleColor || "#FFFFFF"};margin-bottom:${props.module.titleContentGap ?? 20}px;text-align:${props.module.textAlign || "left"};`
);

// 卡间距：单卡两侧各留 itemGap/2，相邻卡共得 itemGap
const cellStyle = computed(() => {
    const gap = Number(props.module.itemGap ?? 10) / 2;
    return `padding-left:${gap}px;padding-right:${gap}px;`;
});

const cardVisualStyle = computed(() => {
    const r = Number(props.module.goodsRadius ?? 12);
    const pad = Number(props.module.innerPadding ?? 10);
    let base = "";
    switch (props.module.cardStyle) {
        case "noBorderWhite":
            base = "background-color:#fff;";
            break;
        case "shadow":
            base = "background-color:#fff;box-shadow:0 2px 4px rgba(0,0,0,0.06);";
            break;
        case "border":
            base = "background-color:#fff;border:1px solid rgba(0,0,0,0.08);";
            break;
        default:
            base = "background-color:transparent;";
    }
    return `${base}border-radius:${r}px;padding:${pad}px;`;
});

const imgBoxStyle = computed(() => {
    const r = Number(props.module.goodsRadius ?? 12);
    const radiusCss = props.module.picBottomRadius ? `border-radius:${r}px;` : `border-radius:${r}px ${r}px 0 0;`;
    if (ratioPadding.value) return `${radiusCss}padding-top:${ratioPadding.value};height:0;`;
    return radiusCss;
});

const infoStyle = computed(() => `text-align:${props.module.textAlign || "left"};`);
const nameStyle = computed(() => `color:${props.module.productTitleColor || "#666"};font-weight:${props.module.textWeight || 400};`);
const descStyle = computed(() => `color:${props.module.descColor || "#999"};`);
const salesStyle = computed(() => `color:${props.module.salesColor || "#999"};`);

const priceFontStyle = computed(() => ({
    color: props.module.priceColor || "#FF5E96",
    fontWeight: 700,
    fontSize: "16px",
    lineHeight: 1.1
}));
const priceCurrencyStyle = computed(() => ({
    color: props.module.priceColor || "#FF5E96",
    fontSize: "11px"
}));
const marketPriceFontStyle = computed(() => ({
    color: props.module.marketPriceColor || "#999",
    fontSize: "11px"
}));

// 购买按钮样式兜底：admin「按钮宽度/高度」滑块上限 10、圆角上限 20；老数据/模板可能存在越界值（如 paddingLeftRight:1000）把按钮撑满整行，这里按 admin 合法区间夹紧
const safeBuyBtnStyle = computed(() => {
    const b: any = props.module.buyBtnStyle;
    if (!b || typeof b !== "object") return null;
    const clamp = (v: any, max: number, fallback: number) => {
        const n = Number(v);
        return Math.min(Math.max(Number.isFinite(n) ? n : fallback, 0), max);
    };
    return {
        ...b,
        paddingLeftRight: clamp(b.paddingLeftRight, 10, 10),
        paddingTopBottom: clamp(b.paddingTopBottom, 10, 0),
        radius: clamp(b.radius, 20, 16)
    };
});

// 露邻轮播：中间大卡占容器宽度 cardWidthPercent%，两侧露出量 = (100-占比)/2，换算成原生 swiper 的 previous/next-margin
const sysWidth = ref(375);
const peekMargin = computed(() => {
    const box = Number(props.module.frame?.boxPadding ?? 0);
    const container = Math.max(0, sysWidth.value - box * 2);
    const pct = Number(props.module.cardWidthPercent ?? 80);
    const margin = (container * (100 - pct)) / 100 / 2;
    return `${Math.max(0, margin)}px`;
});

const currentIndex = ref(0);
const onSwiperChange = (e: any) => {
    currentIndex.value = e.detail.current;
};

// 原生 swiper 需要显式高度：测量最高一张卡（.pc-measure 无 transform，量到的是自然高度；缩放在子级 .pc-card 上不影响父级布局）
const instance = getCurrentInstance();
const swiperHeight = ref(0);
const recalcHeight = () => {
    nextTick(async () => {
        const rects = (await getElementRect(".pc-measure", instance, true)) as any[];
        if (Array.isArray(rects) && rects.length) {
            const max = Math.max(...rects.map((r) => r?.height || 0));
            if (max > 0) swiperHeight.value = max;
        }
    });
};

const displayList = ref<any[]>([]);
const buildDisplay = async () => {
    const raw = Array.isArray(props.module.productList) ? props.module.productList : [];
    // 运行端只渲染真实商品（admin demo 占位 productId:0 不展示）
    const items = raw.filter((i: any) => Number(i?.productId) > 0);
    if (!items.length) {
        displayList.value = [];
        return;
    }
    const ids = items.map((i: any) => i.productId);
    const fresh: Record<number, any> = {};
    try {
        const param: any = { productStatus: 1, checkStatus: 1, ids: ids.join(","), size: ids.length, page: 1 };
        if (props.shopId && props.shopId != -1) param.shopId = props.shopId;
        const result = await getProductsList(param);
        (result.records || []).forEach((r: any) => {
            fresh[r.productId] = r;
        });
    } catch {
        // 拉取失败回退各 item 快照
    }
    currentIndex.value = 0;
    // 拉到最新商品则整体覆盖快照（携带最新 productPrice / picThumb / salesVolume 等；O2O 还需 deliveryMode / cyclePrice）
    displayList.value = items.map((it: any) => {
        const f = fresh[it.productId];
        return f ? { ...it, ...f } : { ...it };
    });
    recalcHeight();
    setTimeout(recalcHeight, 300);
};

watch(
    () => JSON.stringify(props.module.productList ?? []),
    () => {
        buildDisplay();
    }
);
watch(
    [
        () => props.module.productPicRatio,
        () => props.module.cardStyle,
        () => props.module.innerPadding,
        () => props.module.cardWidthPercent,
        () => props.module.showName,
        () => props.module.showDesc,
        () => props.module.showSales,
        () => props.module.showPrice,
        () => props.module.showMarketPrice
    ],
    () => {
        recalcHeight();
        setTimeout(recalcHeight, 300);
    }
);

const windowResizeCallback = () => {
    sysWidth.value = uni.getSystemInfoSync().windowWidth || 375;
    recalcHeight();
};

onMounted(() => {
    sysWidth.value = uni.getSystemInfoSync().windowWidth || 375;
    buildDisplay();
    uni.onWindowResize(windowResizeCallback);
});
onUnmounted(() => {
    uni.offWindowResize(windowResizeCallback);
});

const goDetail = (item: any) => {
    if (!item.productId) return;
    redirect({ url: urlFormat({ path: "product", data: { id: item.productId } }) });
};
</script>

<style lang="scss" scoped>
.module-product_carousel {
    position: relative;
    background-size: cover;
    background-repeat: no-repeat;
    background-position: center;
    .pc-module-bg {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        z-index: 0;
    }
    .pc-body {
        position: relative;
        z-index: 1;
    }
    .pc-module-title {
        font-size: 15px;
        font-weight: 600;
        padding: 0 12px;
        line-height: 1.4;
    }
    .pc-swiper {
        width: 100%;
    }
    .pc-cell {
        box-sizing: border-box;
        width: 100%;
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
    }
    .pc-measure {
        width: 100%;
    }
    .pc-card {
        box-sizing: border-box;
        display: flex;
        flex-direction: column;
        overflow: hidden;
        transition:
            transform 0.3s ease,
            opacity 0.3s ease;
        transform: scale(0.85);
        transform-origin: center center;
        opacity: 0.8;
    }
    .pc-card-active {
        transform: scale(1);
        opacity: 1;
    }
    .pc-photo {
        position: relative;
        width: 100%;
    }
    .pc-img-box {
        position: relative;
        width: 100%;
        overflow: hidden;
        background: #f5f5f5;
    }
    .pc-img {
        display: block;
    }
    .pc-img-fluid {
        width: 100%;
        height: auto;
    }
    .pc-img-ratio {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
    }
    .pc-info {
        display: flex;
        flex-direction: column;
        margin-top: 8px;
    }
    .pc-name {
        font-size: 13px;
        line-height: 1.4;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
        word-break: break-all;
    }
    /* O2O 周期购标签 */
    .cycleBuyTag {
        display: inline-block;
        color: var(--general);
        position: relative;
        padding-right: 20rpx;
        vertical-align: baseline;
        &::before {
            position: absolute;
            content: "";
            right: 9rpx;
            top: 0;
            width: 2rpx;
            height: 70%;
            transform: translateY(22%);
            background-color: var(--general);
        }
    }
    .pc-desc {
        font-size: 12px;
        line-height: 1.3;
        margin-top: 4px;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
    }
    .pc-sales {
        font-size: 11px;
        line-height: 1.3;
        margin-top: 4px;
    }
    .pc-bottom {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 6px;
        margin-top: 8px;
        flex-wrap: nowrap;
    }
    .pc-prices {
        display: flex;
        align-items: baseline;
        gap: 4px;
        flex-wrap: wrap;
        line-height: 1.1;
        flex: 0 1 auto;
        min-width: 0;
    }
    .pc-buy {
        flex-shrink: 0;
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
}
</style>
