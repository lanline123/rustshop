<template>
    <view v-if="shouldRender" class="module-ad-con module-newcomer_combo" :style="outerStyle">
        <view class="combo-wrap" :style="containerBgStyle">
            <view v-if="showHeaderFinal" class="combo-header">
                <text class="header-title">{{ headerTitle }}</text>
                <text v-if="headerSubTitle" class="header-sub">（{{ headerSubTitle }}）</text>
            </view>

            <view v-if="showSection1Final && productList.length > 0" class="combo-card combo-card-section1" :style="{ backgroundColor: decorationBgColor }">
                <view class="card-head">
                    <text class="card-badge" :style="badgeStyle">{{ section1Badge }}</text>
                    <text class="card-title" :style="{ color: productTitleColor }">{{ section1Title }}</text>
                    <view class="more-link" :style="{ color: productBriefColor || '#999999' }" @click="onMoreClick(section1MoreLink)">
                        {{ $t("查看更多") }}
                    </view>
                </view>
                <scroll-view scroll-x class="product-scroll" :show-scrollbar="false">
                    <view class="product-track" :style="productTrackStyle">
                        <navigator
                            v-for="(item, idx) in productList"
                            :key="(item.productId || 0) + '_' + idx"
                            :url="urlFormat({ path: 'product', data: { id: item.productId } })"
                            class="product-item"
                            :style="productItemStyle"
                            hover-class="none"
                        >
                            <view class="product-photo" :style="productPhotoStyle">
                                <view class="product-img-wrap" :style="productImgWrapStyle">
                                    <image :src="productImage(item)" :mode="imageMode" :style="productImgStyle" />
                                </view>
                                <text v-if="productBriefListSafe[Number(idx)]" class="product-brief-overlap" :style="productBriefStyle">
                                    {{ productBriefListSafe[Number(idx)] }}
                                </text>
                            </view>
                            <view v-if="showProductNameFinal" class="product-name" :style="{ color: productTitleColor }">
                                {{ item.productName }}
                            </view>
                            <view class="product-bottom">
                                <view class="product-prices">
                                    <text class="price-current" :style="{ color: productPriceColor }">
                                        <text class="cur-prefix">¥</text>
                                        <text class="cur-num">{{ formatPrice(item.productPrice) }}</text>
                                    </text>
                                    <text
                                        v-if="showMarketPriceFinal && marketPriceVisible(item)"
                                        class="price-market"
                                        :style="{ color: productMarketPriceColor }"
                                    >
                                        ¥{{ formatPrice(item.marketPrice) }}
                                    </text>
                                </view>
                                <view v-if="buyBtnStyleNum > 0" class="buy-btn" :class="`buy-btn-style__${buyBtnStyleNum}`" :style="buyBtnInlineStyle">
                                    <text v-if="buyBtnStyleNum === 5 || buyBtnStyleNum === 6">{{ $t("购买") }}</text>
                                    <text v-else-if="buyBtnStyleNum === 7 || buyBtnStyleNum === 8">{{ $t("马上抢") }}</text>
                                    <text v-else-if="buyBtnStyleNum === 1" class="iconfont-h5 icon-gouwuche"></text>
                                    <text v-else-if="buyBtnStyleNum === 2" class="iconfont-h5 icon-gouwuche1"></text>
                                    <text v-else-if="buyBtnStyleNum === 3" class="iconfont-h5 icon-jia"></text>
                                    <text v-else-if="buyBtnStyleNum === 4" class="iconfont-h5 icon-jia1"></text>
                                </view>
                            </view>
                        </navigator>
                    </view>
                </scroll-view>
            </view>

            <view v-if="showSection2Final && displayCoupons.length > 0" class="combo-card combo-card-section2" :style="{ backgroundColor: decorationBgColor }">
                <view class="card-head">
                    <text class="card-badge" :style="badgeStyle">{{ section2Badge }}</text>
                    <text class="card-title" :style="{ color: productTitleColor }">{{ section2Title }}</text>
                    <view class="more-link more-link-pill" :style="moreLinkPillStyle" @click="onMoreClick(section2MoreLink)">
                        {{ $t("查看更多") }}
                    </view>
                </view>
                <scroll-view scroll-x class="coupon-row-scroll" :show-scrollbar="false">
                    <view class="coupon-row">
                        <view class="coupon-item" v-for="(c, i) in displayCoupons" :key="(c.couponId || 0) + '_' + i" :style="couponItemStyle">
                            <view class="coupon-amount" :style="{ color: couponAmountColor }">
                                <template v-if="Number(c.info?.couponType) === 2">
                                    <text class="amount-num">{{ c.info?.couponDiscount }}</text>
                                    <text class="amount-prefix">{{ $t("折") }}</text>
                                </template>
                                <template v-else>
                                    <text class="amount-prefix">¥</text>
                                    <text class="amount-num">{{ c.info?.couponMoney }}</text>
                                </template>
                            </view>
                            <view class="coupon-condition" :style="{ color: couponConditionColor }">
                                <text v-if="Number(c.info?.reduceType) === 2 || !Number(c.info?.minOrderAmount)">{{ $t("无门槛") }}</text>
                                <text v-else>{{ $t("满") }} {{ c.info?.minOrderAmount }} {{ $t("可用") }}</text>
                            </view>
                        </view>
                    </view>
                </scroll-view>
            </view>
        </view>
    </view>
</template>

<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import { ref, computed, watch } from "vue";
import { urlFormat, productImage } from "@/utils/format";
import { getProductsList } from "@/api/common";
import { redirect } from "@/utils";
import { useCouponInfoMap } from "@/hooks/useCouponInfoMap";

const { t } = useI18n();
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

// admin defaultNewcomerCombo 同源 fallback：处理老数据 / 不完整 payload，保证视觉与 admin canvas 一致
const showHeaderFinal = computed(() => Number(props.module.showHeader ?? 1) === 1);
const headerTitle = computed(() => props.module.headerTitle ?? t("新人专享二重礼"));
const headerSubTitle = computed(() => props.module.headerSubTitle ?? t("限量供应 先到先得"));

const showSection1Final = computed(() => Number(props.module.showSection1 ?? 1) === 1);
const section1Badge = computed(() => props.module.section1Badge ?? t("1重礼"));
const section1Title = computed(() => props.module.section1Title ?? t("新人尝鲜价"));
const section1MoreLink = computed(() => props.module.section1MoreLink ?? { link: "", title: "" });

const showSection2Final = computed(() => Number(props.module.showSection2 ?? 1) === 1);
const section2Badge = computed(() => props.module.section2Badge ?? t("2重礼"));
const section2Title = computed(() => props.module.section2Title ?? t("新人专享礼包"));
const section2MoreLink = computed(() => props.module.section2MoreLink ?? { link: "", title: "" });

const showProductNameFinal = computed(() => Number(props.module.showProductName ?? 1) === 1);
const showMarketPriceFinal = computed(() => Number(props.module.showMarketPrice ?? 1) === 1);
const buyBtnStyleNum = computed(() => Number(props.module.buyBtnStyle ?? 3));

const productTitleColor = computed(() => props.module.productTitleColor ?? "#333333");
const productBriefColor = computed(() => props.module.productBriefColor ?? "#999999");
const productPriceColor = computed(() => props.module.productPriceColor ?? "#FF3630");
const productMarketPriceColor = computed(() => props.module.productMarketPriceColor ?? "#999999");
const productBriefGradientStart = computed(() => props.module.productBriefGradientStart ?? "#FF3630");
const productBriefGradientEnd = computed(() => props.module.productBriefGradientEnd ?? "#FF7A45");
const productBriefTextColor = computed(() => props.module.productBriefTextColor ?? "#FFFFFF");
const buyBtnGradientStart = computed(() => props.module.buyBtnGradientStart ?? "#4CD964");
const buyBtnGradientEnd = computed(() => props.module.buyBtnGradientEnd ?? "#2EBA4F");
const buyBtnTextColor = computed(() => props.module.buyBtnTextColor ?? "#FFFFFF");

const couponBgGradientStart = computed(() => props.module.couponBgGradientStart ?? "#FFF1F1");
const couponBgGradientEnd = computed(() => props.module.couponBgGradientEnd ?? "#FFFFFF");
const couponAmountColor = computed(() => props.module.couponAmountColor ?? "#FF3630");
const couponConditionColor = computed(() => props.module.couponConditionColor ?? "#FF3630");
const couponBtnTextColor = computed(() => props.module.couponBtnTextColor ?? "#FF3630");

const boxGradientStart = computed(() => props.module.boxGradientStart ?? "#FF3630");
const boxGradientEnd = computed(() => props.module.boxGradientEnd ?? "#FF7A45");
const containerBgImage = computed(() => props.module.containerBgImage ?? { picUrl: "", picThumb: "" });
const contentBgColor = computed(() => props.module.contentBgColor ?? "#FFFFFF");
const decorationBgColor = computed(() => props.module.decorationBgColor ?? "#FFFFFF");

const productPicRatio = computed<string>(() => props.module.productPicRatio || "1:1");
const productPicFillType = computed<string>(() => props.module.productPicFillType || "cover");
const productImgRadius = computed(() => Number(props.module.productImgRadius ?? 8));
const productPercent = computed(() => Number(props.module.productPercent ?? 28));
const productItemGap = computed(() => Number(props.module.productItemGap ?? 8));
const productItemPadding = computed(() => Number(props.module.productItemPadding ?? 6));
const couponPerRow = computed(() => Number(props.module.couponPerRow ?? 3));

const productBriefListSafe = computed<string[]>(() => (Array.isArray(props.module.productBriefList) ? props.module.productBriefList : []));

const frameSafe = computed<any>(() => props.module.frame ?? {});

const outerStyle = computed(() => {
    const f = frameSafe.value;
    return [
        `padding-left:${f.boxPadding ?? 10}px;`,
        `padding-right:${f.boxPadding ?? 10}px;`,
        `padding-top:${f.boxPaddingTop ?? 5}px;`,
        `padding-bottom:${f.boxPaddingBottom ?? 5}px;`,
        f.backgroundColor ? `background-color:${f.backgroundColor};` : ""
    ].join("");
});

const containerBgStyle = computed(() => {
    const a = boxGradientStart.value;
    const b = boxGradientEnd.value;
    const r = frameSafe.value.boxRadius ?? 8;
    const bgImg = containerBgImage.value?.picUrl;
    const layers: string[] = [];
    const sizes: string[] = [];
    if (bgImg) {
        layers.push(`url(${bgImg})`);
        sizes.push("cover");
    }
    layers.push(`linear-gradient(to bottom, ${a}, ${b})`);
    sizes.push("auto");
    return `background-image:${layers.join(",")};background-size:${sizes.join(",")};background-position:center;background-repeat:no-repeat;border-radius:${r}px;`;
});

const badgeStyle = computed(() => {
    const bg = productBriefGradientStart.value;
    const color = productBriefTextColor.value;
    return `background-color:${bg};color:${color};`;
});

const productBriefStyle = computed(() => {
    const a = productBriefGradientStart.value;
    const b = productBriefGradientEnd.value;
    const color = productBriefTextColor.value;
    return `background-image:linear-gradient(to bottom, ${a}, ${b});color:${color};`;
});

const ratioPaddingMap: Record<string, string> = {
    default: "",
    "1:1": "100%",
    "3:4": "133.333%",
    "3:2": "66.667%"
};

const productTrackStyle = computed(() => `gap:${productItemGap.value}px;`);

const productItemStyle = computed(() => {
    const padding = productItemPadding.value;
    const percent = productPercent.value;
    const bg = contentBgColor.value;
    return [bg ? `background-color:${bg};` : "", `padding:${padding}px;box-sizing:border-box;`, `flex:0 0 ${percent}%;width:${percent}%;`].join("");
});

const productPhotoStyle = computed(() => {
    const r = productPicRatio.value;
    const padding = ratioPaddingMap[r];
    return padding ? `padding-top:${padding};` : "padding-top:0;";
});

const productImgWrapStyle = computed(() => {
    const radius = productImgRadius.value;
    const r = productPicRatio.value;
    const padding = ratioPaddingMap[r];
    if (padding) {
        return `border-radius:${radius}px;position:absolute;left:0;right:0;top:0;bottom:0;`;
    }
    return `border-radius:${radius}px;position:static;`;
});

const productImgStyle = computed(() => {
    const r = productPicRatio.value;
    const padding = ratioPaddingMap[r];
    if (padding) {
        return `position:absolute;left:0;right:0;top:0;bottom:0;width:100%;height:100%;display:block;`;
    }
    return `width:100%;height:auto;display:block;`;
});

const imageMode = computed(() => {
    const r = productPicRatio.value;
    if (!ratioPaddingMap[r]) return "widthFix";
    return productPicFillType.value === "contain" ? "aspectFit" : "aspectFill";
});

const moreLinkPillStyle = computed(() => {
    const bg = couponBgGradientStart.value;
    const finalBg = bg === "#FFF1F1" ? "#FFEAD8" : bg;
    const color = couponBtnTextColor.value;
    return `background-color:${finalBg};color:${color};`;
});

const buyBtnInlineStyle = computed(() => {
    const a = buyBtnGradientStart.value;
    const b = buyBtnGradientEnd.value;
    const color = buyBtnTextColor.value;
    return `background-image:linear-gradient(to right, ${a}, ${b});color:${color};`;
});

const couponItemStyle = computed(() => {
    const a = couponBgGradientStart.value;
    const b = couponBgGradientEnd.value;
    const n = couponPerRow.value;
    const gap = 8;
    const basis = `calc((100% - ${(n - 1) * gap}px) / ${n})`;
    const bg = decorationBgColor.value;
    return `background-image:linear-gradient(to bottom, ${a}, ${b});flex:0 0 ${basis};max-width:${basis};--dec-bg:${bg};`;
});

const formatPrice = (v: number | string | undefined | null): string => {
    const n = typeof v === "string" ? parseFloat(v) : (v ?? 0);
    const safe = Number.isFinite(Number(n)) ? Number(n) : 0;
    const round = Math.round((safe + Number.EPSILON) * 100) / 100;
    return round.toFixed(2);
};

const marketPriceVisible = (item: any) => {
    const mp = Number(item.marketPrice);
    const pp = Number(item.productPrice);
    return mp > 0 && mp > pp;
};

const onMoreClick = (link: any) => {
    if (!link) return;
    const url = urlFormat(link);
    if (url) redirect({ url });
};

const paramsValueMap: Record<number, string[]> = {
    1: ["productIds"],
    2: ["productCategoryId", "productNumber"],
    3: ["productTag", "productNumber"]
};
const paramsKeyMap: Record<string, string> = {
    productIds: "ids",
    productCategoryId: "categoryId",
    productNumber: "size",
    productTag: "introType"
};
const productList = ref<any[]>([]);
const _getProductList = async () => {
    const param: any = {};
    const products = props.module.products as any;
    if (!products || typeof products !== "object" || !products.productSelectType) {
        productList.value = [];
        return;
    }
    const type = products.productSelectType;
    const keys = paramsValueMap[type];
    if (!keys) {
        productList.value = [];
        return;
    }
    if (type == 1) {
        const ids: number[] = products.productIds || [];
        if (!ids.length) {
            productList.value = [];
            return;
        }
        param[paramsKeyMap[keys[0]]] = ids.join(",");
        param.size = ids.length;
    } else {
        param.page = 1;
        keys.forEach((key) => {
            param[paramsKeyMap[key]] = products[key];
        });
    }
    if (props.shopId && props.shopId != -1) {
        param.shopId = props.shopId;
    }
    try {
        const result = await getProductsList(param);
        productList.value = result.records ?? [];
    } catch (error) {
        productList.value = [];
    }
};
_getProductList();

watch(
    () => JSON.stringify(props.module.products ?? {}),
    () => {
        _getProductList();
    }
);

// 优惠券：按 coupons[].couponId 主动调 promotion/coupon/listByIds 拉真实数据（与 admin 同接口）
// - 优先用 c.info（后端预聚合场景的快速路径）
// - 否则从 module-level 单例 couponInfoMap 取（avoid 重复请求）
const { couponInfoMap, fetchCouponInfo } = useCouponInfoMap();

const couponIdsKey = computed(() => {
    const list = Array.isArray(props.module.coupons) ? props.module.coupons : [];
    return list
        .map((c: any) => Number(c?.couponId))
        .filter((id) => id > 0)
        .sort((a, b) => a - b)
        .join(",");
});

watch(
    couponIdsKey,
    () => {
        const ids = (Array.isArray(props.module.coupons) ? props.module.coupons : []).map((c: any) => Number(c?.couponId)).filter((id) => id > 0);
        if (ids.length) fetchCouponInfo(ids);
    },
    { immediate: true }
);

const displayCoupons = computed(() => {
    const list = Array.isArray(props.module.coupons) ? props.module.coupons : [];
    return list
        .map((c: any) => ({
            ...c,
            info: c?.info || couponInfoMap.value.get(Number(c?.couponId))
        }))
        .filter((c: any) => c.info);
});

const shouldRender = computed(() => {
    const section1Visible = showSection1Final.value && productList.value.length > 0;
    const section2Visible = showSection2Final.value && displayCoupons.value.length > 0;
    const headerVisible = showHeaderFinal.value && (section1Visible || section2Visible);
    return section1Visible || section2Visible || headerVisible;
});
</script>

<style lang="scss" scoped>
.module-newcomer_combo {
    .combo-wrap {
        padding: 10px;
        box-sizing: border-box;
        overflow: hidden;
    }
    .combo-header {
        padding: 0 4px;
        color: #fff;
        display: flex;
        align-items: center;
        white-space: nowrap;
        overflow: hidden;
        .header-title {
            font-size: 16px;
            font-weight: 700;
            margin-right: 4px;
        }
        .header-sub {
            font-size: 14px;
            font-weight: 400;
        }
    }
    .combo-card {
        margin-top: 10px;
        border-radius: 10px;
        padding: 10px 10px 12px;
        box-sizing: border-box;
    }
    .card-head {
        display: flex;
        align-items: center;
        height: 24px;
        margin-bottom: 8px;
        .card-badge {
            font-size: 13px;
            line-height: 1;
            padding: 3px 8px;
            border-radius: 10px;
            font-weight: 600;
            white-space: nowrap;
        }
        .card-title {
            font-size: 14px;
            font-weight: 600;
            margin-left: 6px;
            flex: 1;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }
        .more-link {
            font-size: 12px;
            white-space: nowrap;
        }
        .more-link-pill {
            padding: 4px 10px;
            border-radius: 10px;
            font-size: 13px;
            font-weight: 500;
        }
    }
    .product-scroll {
        width: 100%;
        white-space: nowrap;
        ::-webkit-scrollbar {
            display: none;
        }
    }
    .product-track {
        display: flex;
        flex-wrap: nowrap;
    }
    .product-item {
        display: flex;
        flex-direction: column;
        overflow: visible;
    }
    .product-photo {
        position: relative;
        width: 100%;
        overflow: visible;
    }
    .product-img-wrap {
        overflow: hidden;
        box-sizing: border-box;
        background-color: #fafafa;
    }
    .product-name {
        font-size: 12px;
        line-height: 1.3;
        margin-top: 6px;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
    }
    .product-brief-overlap {
        position: absolute;
        bottom: 0;
        left: 0;
        padding: 2px 6px;
        font-size: 12px;
        line-height: 1.2;
        border-radius: 4px;
        font-weight: 500;
        max-width: 90%;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        box-sizing: border-box;
        z-index: 5;
        box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
    }
    .product-bottom {
        padding: 10px 0 0;
        display: flex;
        justify-content: space-between;
        align-items: center;
        gap: 4px;
    }
    .product-prices {
        display: flex;
        flex-direction: column;
        line-height: 1.1;
        flex: 1;
        min-width: 0;
        .price-current {
            font-weight: 700;
            .cur-prefix {
                font-size: 10px;
                margin-right: 2px;
            }
            .cur-num {
                font-size: 13px;
            }
        }
        .price-market {
            font-size: 10px;
            text-decoration: line-through;
            margin-top: 2px;
            font-weight: 400;
        }
    }
    .buy-btn {
        flex-shrink: 0;
        width: 20px;
        height: 20px;
        line-height: 20px;
        border-radius: 50%;
        text-align: center;
        font-size: 14px;
        color: #fff;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        .iconfont-h5 {
            font-size: 10px;
            line-height: 1;
        }
    }
    .buy-btn-style__5,
    .buy-btn-style__6,
    .buy-btn-style__7,
    .buy-btn-style__8 {
        width: auto;
        height: 20px;
        line-height: 20px;
        padding: 0 8px;
        font-size: 10px;
        border-radius: 10px;
    }
    .coupon-row-scroll {
        width: 100%;
        white-space: nowrap;
        ::-webkit-scrollbar {
            display: none;
        }
    }
    .coupon-row {
        display: flex;
        gap: 8px;
        flex-wrap: nowrap;
    }
    .coupon-item {
        min-width: 0;
        padding: 12px 4px 10px;
        border-radius: 6px;
        text-align: center;
        box-sizing: border-box;
        position: relative;
        overflow: visible;
        &::before,
        &::after {
            content: "";
            position: absolute;
            top: 50%;
            width: 8px;
            height: 8px;
            background-color: var(--dec-bg, #ffffff);
            border-radius: 50%;
            z-index: 2;
        }
        &::before {
            left: -4px;
            transform: translateY(-50%);
        }
        &::after {
            right: -4px;
            transform: translateY(-50%);
        }
    }
    .coupon-amount {
        font-weight: 700;
        line-height: 1;
        white-space: nowrap;
        display: flex;
        align-items: flex-end;
        justify-content: center;
        .amount-prefix {
            font-size: 11px;
            margin-right: 1px;
            line-height: 1.1;
            margin-bottom: 2px;
        }
        .amount-num {
            font-size: 28px;
            line-height: 1;
        }
    }
    .coupon-condition {
        margin-top: 5px;
        font-size: 12px;
        line-height: 1.3;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
    }
}
</style>
