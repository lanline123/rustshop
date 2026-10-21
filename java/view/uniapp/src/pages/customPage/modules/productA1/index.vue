<template>
    <view
        v-if="productList?.length > 0"
        :class="[
            'module-ad-con module-goods_ad module-product_a1',
            styleClass,
            'ad-goods_style__' + (props.module.goodsStyle ?? 1),
            'ad-text_weight__' + (props.module.textWeight ?? 1),
            'ad-goods_name_row__' + (props.module.goodsNameRow ?? 2),
            'ad-goods_name_padding__' + (props.module.goodsNamePadding ?? 1)
        ]"
        :style="allFormat.boxPadding + allFormat.boxPaddingTop + allFormat.boxPaddingBottom"
    >
        <view class="module-ad-content" :style="allFormat.backgroundColor + allFormat.boxRadius + allFormat.innerPadding">
            <view class="goods-ad-warp">
                <view class="goods-ad-con">
                    <view v-for="(item, idx) in productList" :key="item.productId" class="goods-ad-item" :style="itemGapStyle">
                        <view class="item-content">
                            <view class="item-con" :style="itemConRadiusStyle + allFormat.goodsPadding">
                                <view class="item-photo" :style="itemPhotoRadiusStyle">
                                    <view :class="['item-photo-box', photoRatioMode ? 'is-ratio' : 'is-fluid']">
                                        <navigator :url="urlFormat({ path: 'product', data: { id: item.productId } })" class="item-image-a">
                                            <image v-if="photoRatioMode" class="item-image item-image-ratio" mode="aspectFill" :src="productImage(item)" />
                                            <image v-else class="item-image" mode="widthFix" :src="productImage(item)" />
                                        </navigator>
                                    </view>
                                    <text v-if="showProductLabel && tagListSafe[idx]" class="item-label-tag" :style="labelTagStyle">
                                        {{ tagListSafe[idx] }}
                                    </text>
                                </view>
                                <view class="item-info" :style="itemInfoRadiusStyle">
                                    <view class="item-name" :style="nameAlignStyle">
                                        <navigator v-if="showName" :url="urlFormat({ path: 'product', data: { id: item.productId } })" class="item-name-a">
                                            <text>{{ $t(item.productName ?? "商品名称") }}</text>
                                        </navigator>
                                        <view v-if="showBrief && briefTags(idx).length" class="item-brief-list" :style="briefListAlignStyle">
                                            <text v-for="(brief, bi) in briefTags(idx)" :key="bi" class="item-brief-tag" :style="briefTagStyle">
                                                {{ brief }}
                                            </text>
                                        </view>
                                    </view>
                                    <view v-if="showPrice" class="item-action" :style="actionAlignStyle">
                                        <view class="item-price" :style="priceAlignStyle">
                                            <format-price
                                                :font-style="{ fontSize: '32rpx' }"
                                                :currency-style="{ fontSize: '22rpx', position: 'relative', top: '-3rpx' }"
                                                :price-data="item.deliveryMode === 2 ? item.cyclePrice : item.productPrice"
                                            />
                                            <view v-if="marketPriceVisible(item)" class="market_price">
                                                <format-price
                                                    :font-style="{ fontSize: '22rpx', color: '#999' }"
                                                    :currency-style="{ fontSize: '20rpx', color: '#999' }"
                                                    :price-data="item.marketPrice"
                                                />
                                            </view>
                                        </view>
                                        <view
                                            v-if="showBuyBtn"
                                            class="item-buy"
                                            :style="itemBuyStyle"
                                            @click="redirect({ url: `/pages/product/index?id=${item.productId}` })"
                                        >
                                            <commonBuyButton :style-options="safeBuyBtnStyle" />
                                        </view>
                                    </view>
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
import { urlFormat, productImage } from "@/utils/format";
import { redirect } from "@/utils";
import commonBuyButton from "@/pages/customPage/modules/commonBuyButton/index.vue";
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

// admin defaultProductA1 同源 fallback：处理老数据 / 不完整 payload
const showName = computed(() => Number(props.module.showName ?? 1) === 1);
const showPrice = computed(() => Number(props.module.showPrice ?? 1) === 1);
const showBrief = computed(() => Number(props.module.showBrief ?? 1) === 1);
const showProductLabel = computed(() => Number(props.module.showProductLabel ?? 1) === 1);
const showMarketPrice = computed(() => Number(props.module.showMarketPrice ?? 0) === 1);
const labelBackgroundColor = computed(() => props.module.labelBackgroundColor ?? "#3DC275");
const labelTextColor = computed(() => props.module.labelTextColor ?? "#FFFFFF");
const labelRadius = computed(() => Number(props.module.labelRadius ?? 8));
const labelRadiusStyle = computed(() => Number(props.module.labelRadiusStyle ?? 0));
const labelFontSize = computed(() => Number(props.module.labelFontSize ?? 12));
const labelPosition = computed(() => props.module.labelPosition ?? "top-left");
const labelOffsetX = computed(() => Number(props.module.labelOffsetX ?? 0));
const labelOffsetY = computed(() => Number(props.module.labelOffsetY ?? 0));
const briefBackgroundColor = computed(() => props.module.briefBackgroundColor ?? "#FFF1E6");
const itemGap = computed(() => Number(props.module.itemGap ?? 0));
const goodsRadius = computed(() => Number(props.module.goodsRadius ?? 0));

// 购买按钮：admin 已从「数字式 buyBtnStyle(1~8)」升级为「对象式 BuyButtonStyleType」并复用 commonBuyButton。
// 默认值与 admin defaultBuyButtonStyle 同源；历史草稿 buyBtnStyle 曾为数字，非对象时回退默认对象（保持显示购物车图标按钮，与 admin 兜底一致）。
const defaultBuyBtnStyle = {
    showBtn: 1,
    btnType: 1,
    iconImage: { picUrl: "", picThumb: "" },
    iconSize: 14,
    text: "",
    textColor: "#ffffff",
    textSize: 12,
    iconColor: "#ffffff",
    gradientType: "leftRight",
    gradientColorA: "#333333",
    gradientColorB: "#333333",
    radius: 16,
    paddingTopBottom: 0,
    paddingLeftRight: 10,
    wrap: 0,
    wrapAlign: 2
};
const safeBuyBtnStyle = computed(() => {
    const b: any = props.module.buyBtnStyle;
    const base: any = b && typeof b === "object" ? b : defaultBuyBtnStyle;
    // admin「按钮宽度/高度」滑块上限 10、圆角上限 20；老数据/模板可能存在越界值把按钮撑满整行，按 admin 合法区间夹紧
    const clamp = (v: any, max: number, fallback: number) => {
        const n = Number(v);
        return Math.min(Math.max(Number.isFinite(n) ? n : fallback, 0), max);
    };
    return {
        ...base,
        paddingLeftRight: clamp(base.paddingLeftRight, 10, 10),
        paddingTopBottom: clamp(base.paddingTopBottom, 10, 0),
        radius: clamp(base.radius, 20, 16)
    };
});

// 与 admin 同源：购买按钮显示与否严格按 Number(showBtn) === 1 判定，
// 必须读 safeBuyBtnStyle，保证历史/数字型 buyBtnStyle 命中默认对象（showBtn=1）时按钮可见，
// 同时与 buyBtnWrapped、itemBuyStyle 等保持同一数据源
const showBuyBtn = computed(() => Number(safeBuyBtnStyle.value?.showBtn) === 1);

const tagListSafe = computed<string[]>(() => (Array.isArray(props.module.tagList) ? props.module.tagList : []));
const briefListSafe = computed<string[]>(() => (Array.isArray(props.module.briefList) ? props.module.briefList : []));
const briefStyleSafe = computed(() => props.module.briefStyle ?? { showTitle: 1, titleColor: "#FF6600", titleFontSize: 11, titleFontBold: 400 });

const allFormat = computed(() => ({
    backgroundColor: props.module.backgroundColor ? `background-color: ${props.module.backgroundColor};` : "",
    boxPadding: `margin-left: ${props.module.boxPadding ?? 0}px;margin-right: ${props.module.boxPadding ?? 0}px;`,
    boxPaddingBottom: `margin-bottom: ${props.module.boxPaddingBottom ?? 0}px;`,
    boxPaddingTop: `margin-top: ${props.module.boxPaddingTop ?? 0}px;`,
    boxRadius: `border-radius: ${props.module.boxRadius ?? 0}px;`,
    innerPadding: `padding: ${props.module.innerPadding ?? 0}px;`,
    goodsPadding: `padding: ${props.module.goodsPadding ?? 0}px;`
}));

// admin styleClassMap：style:4 详细列表 → 复用 ad-style__5 大图横排
const styleClassMap: Record<number, string> = { 1: "ad-style__1", 2: "ad-style__2", 3: "ad-style__3", 4: "ad-style__5" };
const styleClass = computed(() => styleClassMap[Number(props.module.style ?? 1)] || "ad-style__1");
const styleNum = computed(() => Number(props.module.style ?? 1));
const isListStyle = computed(() => styleNum.value === 4);
// 一行 2 / 一行 3 / 详细列表 都需要锁正方形比例以避免错位
const photoRatioMode = computed(() => styleNum.value === 2 || styleNum.value === 3 || styleNum.value === 4);

const itemConRadiusStyle = computed(() => {
    const r = goodsRadius.value;
    return r ? `border-radius:${r}px;overflow:hidden;` : "";
});
const itemPhotoRadiusStyle = computed(() => {
    const r = goodsRadius.value;
    if (!r) return "";
    return isListStyle.value ? `border-radius:${r}px 0 0 ${r}px;overflow:hidden;` : `border-radius:${r}px ${r}px 0 0;overflow:hidden;`;
});
const itemInfoRadiusStyle = computed(() => {
    const r = goodsRadius.value;
    if (!r) return "";
    return isListStyle.value ? `border-radius:0 ${r}px ${r}px 0;` : `border-radius:0 0 ${r}px ${r}px;`;
});

const labelTagStyle = computed(() => {
    const r = labelRadius.value;
    const mode = labelRadiusStyle.value;
    const pos = labelPosition.value;
    const ox = labelOffsetX.value;
    const oy = labelOffsetY.value;
    let radiusCss = "";
    if (mode === 0) radiusCss = `border-radius:${r}px;`;
    else if (mode === 1) radiusCss = `border-radius:${r}px 0 0 ${r}px;`;
    else if (mode === 2) radiusCss = `border-radius:0 ${r}px ${r}px 0;`;
    else if (mode === 3) radiusCss = `border-top-left-radius:${r}px;border-bottom-right-radius:${r}px;`;
    else if (mode === 4) radiusCss = `border-top-right-radius:${r}px;border-bottom-left-radius:${r}px;`;
    const posMap: Record<string, string> = {
        "top-left": `left:${ox}px;top:${oy}px;right:auto;bottom:auto;`,
        "top-right": `right:${ox}px;top:${oy}px;left:auto;bottom:auto;`,
        "bottom-left": `left:${ox}px;bottom:${oy}px;right:auto;top:auto;`,
        "bottom-right": `right:${ox}px;bottom:${oy}px;left:auto;top:auto;`
    };
    return [
        labelBackgroundColor.value ? `background-color:${labelBackgroundColor.value};` : "",
        labelTextColor.value ? `color:${labelTextColor.value};` : "",
        radiusCss,
        `font-size:${labelFontSize.value}px;`,
        posMap[pos] || posMap["top-left"]
    ].join("");
});

const itemGapStyle = computed(() => {
    const g = itemGap.value;
    return g > 0 ? `padding:${g / 2}px;` : "";
});

const nameAlignStyle = computed(() => (Number(props.module.nameAlign ?? 1) === 2 ? "text-align:center;" : "text-align:left;"));
const briefListAlignStyle = computed(() => (Number(props.module.nameAlign ?? 1) === 2 ? "justify-content:center;" : "justify-content:flex-start;"));
const priceAlignStyle = computed(() => (Number(props.module.priceAlign ?? 1) === 2 ? "justify-content:center;" : "justify-content:flex-start;"));
const actionAlignStyle = computed(() => {
    const base = Number(props.module.priceAlign ?? 1) === 2 ? "justify-content:center;" : "";
    return base + (buyBtnWrapped.value ? "flex-wrap:wrap;" : "");
});

// 购买按钮换行：开启换行时，按钮独占一行并按 wrapAlign 对齐（与 admin View 同步）
const buyBtnWrapped = computed(() => {
    const b = safeBuyBtnStyle.value;
    return Number(b?.showBtn) === 1 && Number(b?.wrap) === 1;
});
const itemBuyStyle = computed(() => {
    if (!buyBtnWrapped.value) return "";
    const alignMap: Record<number, string> = { 1: "left", 2: "center", 3: "right" };
    const align = alignMap[Number(safeBuyBtnStyle.value?.wrapAlign ?? 2)] || "center";
    // display:block 覆盖 .item-buy 的 flex，使内部 inline-flex 按钮统一受 text-align 控制
    return `width:100%;max-width:none;display:block;text-align:${align};margin-top:8rpx;`;
});

const briefTags = (idx: number | string) => {
    const i = typeof idx === "number" ? idx : Number(idx);
    const raw = briefListSafe.value[i] || "";
    return raw
        .split("\n")
        .map((s) => s.trim())
        .filter(Boolean);
};
const briefTagStyle = computed(() => {
    const s = briefStyleSafe.value;
    return [
        s.titleColor ? `color:${s.titleColor};` : "",
        s.titleFontSize ? `font-size:${s.titleFontSize}px;` : "",
        s.titleFontBold ? `font-weight:${s.titleFontBold};` : "",
        briefBackgroundColor.value ? `background-color:${briefBackgroundColor.value};` : ""
    ].join("");
});

const marketPriceVisible = (item: any) => {
    if (!showMarketPrice.value) return false;
    const mp = Number(item.marketPrice);
    const pp = Number(item.productPrice);
    return mp > 0 && mp > pp;
};

const productList = ref<any[]>([]);
const _getproductList = async () => {
    try {
        productList.value = await getDecorateProducts(props.module.products, {
            shopId: props.shopId,
            allowEmptyParams: true
        });
    } catch (error) {
        console.log(error);
    }
};
_getproductList();

watch(
    () => JSON.stringify(props.module.products ?? {}),
    () => {
        _getproductList();
    }
);
</script>

<style lang="scss" scoped>
.module-product_a1 {
    .module-ad-content {
        position: relative;
        overflow: hidden;
        z-index: 1;
    }
    .goods-ad-warp .goods-ad-con {
        display: flex;
        flex-wrap: wrap;
    }
    .goods-ad-warp .goods-ad-item {
        width: 100%;
        position: relative;
        overflow: hidden;
        display: inline-block;
        box-sizing: border-box;
    }
    // 商品边距(goodsPadding)>0 时 padding 区随之为白底，形成完整白色卡片；
    // =0 时图文铺满白底被遮住，对默认观感无影响（与 admin canvas 同源）
    .goods-ad-warp .goods-ad-item .item-con {
        background-color: #ffffff;
    }
    .goods-ad-warp .goods-ad-item .item-photo {
        background: #ffffff;
        text-align: center;
        position: relative;
    }
    .item-photo-box.is-fluid .item-image {
        width: 100%;
        height: auto;
        display: block;
    }
    .item-photo-box.is-ratio {
        position: relative;
        padding-top: 100%;
        width: 100%;
        overflow: hidden;
    }
    .item-photo-box.is-ratio .item-image-a {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        display: block;
    }
    // 提高优先级压过全局 modulesCommon.css 的
    // `.module-ad-con.module-goods_ad ... .item-photo image { height:auto }`，
    // 同时用 top/bottom/left/right 四向锚定，即使 height 被覆盖也能撑满方形容器
    .item-photo .item-photo-box.is-ratio .item-image-ratio {
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        width: 100%;
        height: 100%;
    }
    .item-label-tag {
        position: absolute;
        padding: 4rpx 16rpx;
        line-height: 1.4;
        z-index: 2;
        white-space: nowrap;
        font-weight: 500;
    }
    .goods-ad-warp .goods-ad-item .item-info {
        background: #ffffff;
        position: relative;
        padding-top: 20rpx;
    }
    .goods-ad-warp .goods-ad-item .item-info .item-name {
        margin: 0 20rpx;
        display: block;
    }
    .goods-ad-warp .goods-ad-item .item-info .item-name .item-name-a {
        line-height: 40rpx;
        height: 80rpx;
        overflow: hidden;
        display: -webkit-box;
        color: #2a3145;
        text-overflow: ellipsis;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        font-weight: bold;
    }
    .item-brief-list {
        display: flex;
        flex-wrap: wrap;
        margin-top: 8rpx;
        padding: 0 20rpx;
    }
    .item-brief-tag {
        padding: 2rpx 12rpx;
        margin-right: 8rpx;
        margin-bottom: 8rpx;
        border-radius: 4rpx;
        line-height: 32rpx;
        display: inline-block;
        white-space: nowrap;
        font-size: 22rpx;
    }
    .goods-ad-warp .goods-ad-item .item-info .item-action {
        padding: 0 16rpx 16rpx;
        position: relative;
        display: flex;
        justify-content: space-between;
        align-items: center;
        flex-wrap: nowrap;
        gap: 8rpx;
    }
    .goods-ad-warp .goods-ad-item .item-info .item-price {
        font-size: 32rpx;
        color: var(--general);
        display: flex;
        align-items: baseline;
        flex-wrap: wrap;
        gap: 8rpx;
    }
    .market_price {
        line-height: 1;
        font-weight: normal;
        text-decoration: line-through;
        // format-price 内部把货币 / 整数 / 小数拆成多个 view（display: inline-flex），
        // 需在子节点显式继承 text-decoration 才能呈连续划线（与 admin canvas 视觉对齐）
        :deep(.price-content),
        :deep(.num) {
            text-decoration: inherit;
        }
    }
    .goods-ad-warp .goods-ad-item .item-info .item-buy {
        justify-content: center;
        display: flex;
        align-items: center;
    }

    &.ad-goods_name_padding__0 .goods-ad-warp .goods-ad-item .item-info .item-name {
        margin-left: 0;
        margin-right: 0;
    }
    &.ad-goods_name_padding__0 .item-brief-list {
        padding-left: 0;
        padding-right: 0;
    }
    &.ad-goods_name_padding__0 .goods-ad-warp .goods-ad-item .item-info .item-action {
        padding-left: 0;
        padding-right: 0;
    }
    &.ad-goods_name_row__1 .goods-ad-warp .goods-ad-item .item-info .item-name .item-name-a {
        line-height: 40rpx;
        height: 40rpx;
        -webkit-line-clamp: 1;
        text-overflow: ellipsis;
        display: -webkit-box;
        -webkit-box-orient: vertical;
        overflow: hidden;
    }
    &.ad-text_weight__1 .goods-ad-warp .goods-ad-item .item-info .item-name .item-name-a {
        font-weight: normal;
    }
}

/* style 1: 一行一个 */
.module-product_a1.ad-style__1 .goods-ad-warp .goods-ad-con .goods-ad-item {
    width: 100%;
}

/* style 2: 一行两个 */
.module-product_a1.ad-style__2 .goods-ad-warp .goods-ad-con .goods-ad-item {
    width: 50%;
}
.module-product_a1.ad-style__2 .goods-ad-warp .goods-ad-item .item-info .item-action {
    flex-direction: column;
    align-items: stretch;
    gap: 8rpx;
    flex-wrap: nowrap;
}
.module-product_a1.ad-style__2 .goods-ad-warp .goods-ad-item .item-info .item-buy {
    width: 100%;
    max-width: 160rpx;
}

/* style 3: 一行三个 */
.module-product_a1.ad-style__3 .goods-ad-warp .goods-ad-con .goods-ad-item {
    width: 33.3333%;
}
.module-product_a1.ad-style__3 .goods-ad-warp .goods-ad-item .item-info .item-name .item-name-a {
    font-size: 24rpx;
}
.module-product_a1.ad-style__3 .goods-ad-warp .goods-ad-item .item-info .item-price {
    font-size: 26rpx;
}
.module-product_a1.ad-style__3 .goods-ad-warp .goods-ad-item .item-info .item-action {
    flex-direction: column;
    align-items: stretch;
    gap: 4rpx;
    padding: 0 12rpx 12rpx;
}

/* style 4 (映射 __5): 详细列表 — 大图横排 */
.module-product_a1.ad-style__5 .goods-ad-warp .goods-ad-con {
    display: flex;
    flex-wrap: wrap;
}
.module-product_a1.ad-style__5 .goods-ad-warp .goods-ad-con .goods-ad-item {
    width: 100%;
}
.module-product_a1.ad-style__5 .goods-ad-warp .goods-ad-item .item-content {
    display: flex;
    flex-wrap: nowrap;
}
.module-product_a1.ad-style__5 .goods-ad-warp .goods-ad-item .item-content .item-con {
    display: flex;
    flex-wrap: nowrap;
    flex: 1;
    align-items: stretch;
}
.module-product_a1.ad-style__5 .goods-ad-warp .goods-ad-item .item-photo {
    width: 40%;
    flex: 0 0 40%;
}
.module-product_a1.ad-style__5 .goods-ad-warp .goods-ad-item .item-info {
    width: 60%;
    flex: 1;
    text-align: left;
    display: flex;
    flex-direction: column;
    padding: 20rpx 16rpx;
    box-sizing: border-box;
}
.module-product_a1.ad-style__5 .goods-ad-warp .goods-ad-item .item-info .item-name {
    flex: 1;
    margin: 0;
}
.module-product_a1.ad-style__5 .goods-ad-warp .goods-ad-item .item-info .item-name .item-name-a {
    height: auto;
    line-height: 36rpx;
}
.module-product_a1.ad-style__5 .item-brief-list {
    padding: 0;
    margin-top: 12rpx;
}
.module-product_a1.ad-style__5 .goods-ad-warp .goods-ad-item .item-info .item-action {
    padding: 0;
    margin-top: 16rpx;
    align-items: flex-end;
}

/* 商品卡片外观 */
.module-product_a1.ad-goods_style__2 .goods-ad-warp .goods-ad-con .goods-ad-item .item-con {
    box-shadow: 0 4rpx 8rpx rgba(0, 0, 0, 0.06);
}
.module-product_a1.ad-goods_style__3 .goods-ad-warp .goods-ad-con .goods-ad-item .item-con {
    border: 2rpx solid rgba(50, 50, 51, 0.1);
}
/* 透明底样式：卡片融入页面背景，item-con 不加白底（与 admin 同源） */
.module-product_a1.ad-goods_style__4 .goods-ad-warp .goods-ad-item .item-con {
    background: transparent;
}
.module-product_a1.ad-goods_style__4 .goods-ad-warp .goods-ad-item .item-info {
    background: transparent;
}

/* O2O 周期购标签 */
.module-product_a1 .cycleBuyTag {
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
</style>
