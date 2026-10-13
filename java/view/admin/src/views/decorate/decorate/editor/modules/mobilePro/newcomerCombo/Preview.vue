<template>
    <div class="module-ad-con module-newcomer_combo" :style="outerStyle">
        <div class="combo-wrap" :style="containerBgStyle">
            <div v-if="module.showHeader" class="combo-header">
                <span class="header-title">{{ module.headerTitle }}</span>
                <span v-if="module.headerSubTitle" class="header-sub">（{{ module.headerSubTitle }}）</span>
            </div>

            <div v-if="module.showSection1" class="combo-card combo-card-section1" :style="{ backgroundColor: module.decorationBgColor }">
                <div class="card-head">
                    <span class="card-badge" :style="badgeStyle">{{ module.section1Badge }}</span>
                    <span class="card-title" :style="{ color: module.productTitleColor }">{{ module.section1Title }}</span>
                    <a class="more-link" :style="{ color: module.productBriefColor || '#999999' }">查看更多</a>
                </div>
                <div class="product-empty" v-if="!productList || productList.length === 0">
                    <span>请先选择商品</span>
                </div>
                <div v-else class="product-scroll">
                    <div class="product-track" :style="productTrackStyle">
                        <div
                            class="product-item"
                            v-for="(item, idx) in productList"
                            :key="(item.productId || 0) + '_' + idx"
                            :style="productItemStyle"
                        >
                            <div class="product-photo" :style="productPhotoStyle">
                                <div class="product-img-wrap" :style="productImgWrapStyle">
                                    <img
                                        :src="productImage(item)"
                                        alt=""
                                        :style="productImgStyle"
                                    />
                                </div>
                                <div v-if="module.productBriefList?.[idx]" class="product-brief-overlap" :style="productBriefStyle">
                                    {{ module.productBriefList[idx] }}
                                </div>
                            </div>
                            <div
                                v-if="module.showProductName"
                                class="product-name"
                                :style="{ color: module.productTitleColor }"
                            >
                                {{ item.productName }}
                            </div>
                            <div class="product-bottom">
                                <div class="product-prices">
                                    <span class="price-current" :style="{ color: module.productPriceColor }">
                                        <i>¥</i><b>{{ priceFormat(Number(item.productPrice), false) }}</b>
                                    </span>
                                    <span
                                        v-if="module.showMarketPrice && Number(item.marketPrice) > 0 && Number(item.marketPrice) > Number(item.productPrice)"
                                        class="price-market"
                                        :style="{ color: module.productMarketPriceColor }"
                                    >
                                        ¥{{ priceFormat(Number(item.marketPrice), false) }}
                                    </span>
                                </div>
                                <a
                                    v-if="module.buyBtnStyle && module.buyBtnStyle > 0"
                                    class="buy-btn"
                                    :class="`buy-btn-style__${module.buyBtnStyle}`"
                                    :style="buyBtnStyle"
                                >
                                    <template v-if="module.buyBtnStyle === 5 || module.buyBtnStyle === 6">
                                        <span>购买</span>
                                    </template>
                                    <template v-else-if="module.buyBtnStyle === 7 || module.buyBtnStyle === 8">
                                        <span>马上抢</span>
                                    </template>
                                    <template v-else-if="module.buyBtnStyle === 1">
                                        <span class="iconfont-h5 icon-gouwuche"></span>
                                    </template>
                                    <template v-else-if="module.buyBtnStyle === 2">
                                        <span class="iconfont-h5 icon-gouwuche1"></span>
                                    </template>
                                    <template v-else-if="module.buyBtnStyle === 3">
                                        <span class="iconfont-h5 icon-jia"></span>
                                    </template>
                                    <template v-else-if="module.buyBtnStyle === 4">
                                        <span class="iconfont-h5 icon-jia1"></span>
                                    </template>
                                </a>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div v-if="module.showSection2" class="combo-card combo-card-section2" :style="{ backgroundColor: module.decorationBgColor }">
                <div class="card-head">
                    <span class="card-badge" :style="badgeStyle">{{ module.section2Badge }}</span>
                    <span class="card-title" :style="{ color: module.productTitleColor }">{{ module.section2Title }}</span>
                    <a class="more-link more-link-pill" :style="moreLinkPillStyle">查看更多</a>
                </div>
                <div class="coupon-row" v-if="displayCoupons.length">
                    <div class="coupon-item" v-for="(c, i) in displayCoupons" :key="c.couponId || i" :style="couponItemStyle">
                        <div class="coupon-amount" :style="{ color: module.couponAmountColor }">
                            <template v-if="c.info.couponType === 2">
                                <b>{{ c.info.couponDiscount }}</b><i class="amount-prefix">折</i>
                            </template>
                            <template v-else>
                                <i class="amount-prefix">¥</i><b>{{ c.info.couponMoney }}</b>
                            </template>
                        </div>
                        <div class="coupon-condition" :style="{ color: module.couponConditionColor }">
                            <span v-if="c.info.reduceType === 2 || !Number(c.info.minOrderAmount)">无门槛</span>
                            <span v-else>满 {{ c.info.minOrderAmount }} 可用</span>
                        </div>
                    </div>
                </div>
                <div v-else-if="couponInfoLoading" class="coupon-empty">优惠券信息加载中...</div>
                <div v-else class="coupon-empty">请在右侧选择优惠券</div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref, computed, watch, watchEffect } from "vue";
import { priceFormat, productImage } from "@/utils/format";
import { getProductList } from "@/api/product/product";
import { useCouponInfoMap } from "@/hooks/useCouponInfoMap";
import { mergeDefaultModule } from "@/views/decorate/decorate/src/modules/index";
import { newcomerComboDefaults } from "./index";
import type { NewcomerComboModule } from "./types";

const props = defineProps<{ module: NewcomerComboModule }>();
const module = props.module;
// 旧数据缺字段时烧录默认值（原地写回 store 模块对象，与旧 View.vue 行为一致，契约）
mergeDefaultModule(module, newcomerComboDefaults);

const demoProducts: any[] = [
    {
        productId: 0,
        productSn: "",
        productName: "草莓500g 新人尝鲜",
        productDesc: "",
        productPrice: 89.0,
        marketPrice: 129.0,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/17319190474y0XWFxO8GJZF0dg64.jpg"
    },
    {
        productId: 0,
        productSn: "",
        productName: "鲜切牛排 新人尝鲜",
        productDesc: "",
        productPrice: 89.0,
        marketPrice: 129.0,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1731919071axvkfUDkQNFbIzF1iY.jpg"
    },
    {
        productId: 0,
        productSn: "",
        productName: "鲜活大虾 限时折扣",
        productDesc: "",
        productPrice: 89.0,
        marketPrice: 129.0,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1731919071lOa0ZBx2mYlvq1lgjr.jpg"
    },
    {
        productId: 0,
        productSn: "",
        productName: "冷冻鸡腿 热卖中",
        productDesc: "",
        productPrice: 89.0,
        marketPrice: 129.0,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1731919071xUnObq2LokKPFecwXq.jpg"
    }
];

const productList = ref<any[]>([]);

const outerStyle = computed(() => {
    const f: any = module.frame || {};
    return [
        `padding-left:${f.boxPadding ?? 0}px;`,
        `padding-right:${f.boxPadding ?? 0}px;`,
        `padding-top:${f.boxPaddingTop ?? 0}px;`,
        `padding-bottom:${f.boxPaddingBottom ?? 0}px;`,
        f.backgroundColor ? `background-color:${f.backgroundColor};` : ""
    ].join("");
});

const containerBgStyle = computed(() => {
    const a = module.boxGradientStart || "#FF3630";
    const b = module.boxGradientEnd || "#FF7A45";
    const r = (module.frame as any)?.boxRadius ?? 8;
    const bgImg = module.containerBgImage?.picUrl;
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
    const bg = module.productBriefGradientStart || "#FF3630";
    const color = module.productBriefTextColor || "#FFFFFF";
    return `background-color:${bg};color:${color};`;
});

const productBriefStyle = computed(() => {
    const a = module.productBriefGradientStart || "#FF3630";
    const b = module.productBriefGradientEnd || "#FF7A45";
    const color = module.productBriefTextColor || "#FFFFFF";
    return `background-image:linear-gradient(to bottom, ${a}, ${b});color:${color};`;
});

const ratioPaddingMap: Record<string, string> = {
    default: "",
    "1:1": "100%",
    "3:4": "133.333%",
    "3:2": "66.667%"
};

const productTrackStyle = computed(() => {
    const gap = module.productItemGap ?? 8;
    return `gap:${gap}px;`;
});

const productItemStyle = computed(() => {
    const padding = module.productItemPadding ?? 6;
    const percent = module.productPercent ?? 28;
    const bg = module.contentBgColor || "";
    return [
        bg ? `background-color:${bg};` : "",
        `padding:${padding}px;box-sizing:border-box;`,
        `flex:0 0 ${percent}%;width:${percent}%;`
    ].join("");
});

const productPhotoStyle = computed(() => {
    const r = module.productPicRatio || "1:1";
    const padding = ratioPaddingMap[r];
    return padding ? `padding-top:${padding};` : "padding-top:0;";
});

const productImgWrapStyle = computed(() => {
    const radius = module.productImgRadius ?? 8;
    const r = module.productPicRatio || "1:1";
    const padding = ratioPaddingMap[r];
    if (padding) {
        return `border-radius:${radius}px;position:absolute;inset:0;`;
    }
    return `border-radius:${radius}px;position:static;`;
});

const productImgStyle = computed(() => {
    const fillType = module.productPicFillType || "cover";
    const r = module.productPicRatio || "1:1";
    const padding = ratioPaddingMap[r];
    if (padding) {
        return `object-fit:${fillType};position:absolute;inset:0;width:100%;height:100%;display:block;`;
    }
    return `width:100%;height:auto;display:block;`;
});

const moreLinkPillStyle = computed(() => {
    const bg = module.couponBgGradientStart || "#FFF1F1";
    const finalBg = bg === "#FFF1F1" ? "#FFEAD8" : bg;
    const color = module.couponBtnTextColor || "#FF3630";
    return `background-color:${finalBg};color:${color};`;
});

const buyBtnStyle = computed(() => {
    const a = module.buyBtnGradientStart || "#4CD964";
    const b = module.buyBtnGradientEnd || "#2EBA4F";
    const color = module.buyBtnTextColor || "#FFFFFF";
    const t = module.buyBtnStyle;
    if (t === 1 || t === 2 || t === 3 || t === 4) {
        return `background-image:linear-gradient(to right, ${a}, ${b});color:${color};`;
    }
    if (t === 5 || t === 6) {
        return `background-image:linear-gradient(to right, ${a}, ${b});color:${color};`;
    }
    if (t === 7 || t === 8) {
        return `background-image:linear-gradient(to right, ${a}, ${b});color:${color};`;
    }
    return "";
});

const couponItemStyle = computed(() => {
    const a = module.couponBgGradientStart || "#FFF1F1";
    const b = module.couponBgGradientEnd || "#FFFFFF";
    const n = module.couponPerRow || 3;
    const gap = 8;
    const basis = `calc((100% - ${(n - 1) * gap}px) / ${n})`;
    const bg = module.decorationBgColor || "#FFFFFF";
    return `background-image:linear-gradient(to bottom, ${a}, ${b});flex:0 0 ${basis};max-width:${basis};--dec-bg:${bg};`;
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
    const products: any = module.products;
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

const { couponInfoMap, fetchCouponInfo } = useCouponInfoMap();
const couponInfoLoading = ref(false);

const demoCoupons: any[] = [
    {
        couponId: 0,
        btnText: "立即领取",
        info: {
            couponName: "新人专享 满600减40",
            couponType: 1,
            couponMoney: 40,
            minOrderAmount: 600,
            reduceType: 1
        }
    },
    {
        couponId: 0,
        btnText: "立即领取",
        info: {
            couponName: "无门槛减5",
            couponType: 1,
            couponMoney: 5,
            minOrderAmount: 0,
            reduceType: 2
        }
    },
    {
        couponId: 0,
        btnText: "立即领取",
        info: {
            couponName: "无门槛减20",
            couponType: 1,
            couponMoney: 20,
            minOrderAmount: 0,
            reduceType: 2
        }
    }
];

const displayCoupons = computed(() => {
    const list = module.coupons || [];
    if (list.length === 0) {
        return demoCoupons;
    }
    return list
        .map((c) => ({ ...c, info: couponInfoMap.value.get(c.couponId) }))
        .filter((c) => c.info);
});

const couponIdsKey = computed(() =>
    (module.coupons || [])
        .map((c) => c.couponId)
        .filter((id): id is number => Boolean(id))
        .sort((a, b) => a - b)
        .join(",")
);

watch(
    couponIdsKey,
    async () => {
        const ids = (module.coupons || [])
            .map((c) => c.couponId)
            .filter((id): id is number => Boolean(id));
        if (!ids.length) return;
        couponInfoLoading.value = true;
        try {
            await fetchCouponInfo(ids);
        } finally {
            couponInfoLoading.value = false;
        }
    },
    { immediate: true }
);
</script>
<style lang="less" scoped>
.module-newcomer_combo {
    .combo-wrap {
        padding: 10px;
        box-sizing: border-box;
    }
    .combo-header {
        padding: 0 4px;
        color: #fff;
        font-size: 16px;
        font-weight: 700;
        display: flex;
        align-items: center;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
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
    .product-empty,
    .coupon-empty {
        padding: 20px 0;
        text-align: center;
        color: #999;
        font-size: 12px;
    }
    .product-scroll {
        overflow-x: auto;
        overflow-y: hidden;
        scrollbar-width: none;
        -ms-overflow-style: none;
        &::-webkit-scrollbar {
            display: none;
        }
    }
    .product-track {
        display: flex;
    }
    .product-item {
        overflow: visible;
        display: flex;
        flex-direction: column;
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
        transform: translateZ(0);
        -webkit-transform: translateZ(0);
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
        bottom: 0px;
        left: 0%;
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
            i {
                font-style: normal;
                font-size: 10px;
                margin-right: 2px;
            }
            b {
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
        cursor: pointer;
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
    .coupon-row {
        display: flex;
        gap: 8px;
        flex-wrap: nowrap;
        overflow-x: auto;
        overflow-y: hidden;
        scrollbar-width: none;
        -ms-overflow-style: none;
        &::-webkit-scrollbar {
            display: none;
        }
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
            content: '';
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
            font-style: normal;
            font-size: 11px;
            margin-right: 1px;
            line-height: 1.1;
            margin-bottom: 2px;
        }
        b {
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
