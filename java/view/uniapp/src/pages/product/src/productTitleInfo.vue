<template>
    <view
        class="product-title-info"
        :class="{
            'has-activity-bg': isNormalWithActivityBg,
            'is-vortmall-layout': isVortmallLayout,
            'is-wholesale-mode': isWholesaleMode
        }"
    >
        <!-- 批发态：隐藏零售价与营销标题，价格改由详情页外部 WholesalePriceTable 渲染 -->
        <template v-if="!isWholesaleMode">
            <template v-if="isSeckill && !isPresale">
                <productSeckillTitle
                    :product-info="productInfo"
                    :product-origin-price="productOriginPrice!"
                    :product-price="productPrice"
                    :seckill-value="seckillValue"
                    :activity-bg-image="activityBgImage"
                    :overseas="useActivityCardLayout"
                />
            </template>
            <template v-if="isDiscount && !isPresale">
                <productDiscountTitle
                    :product-info="productInfo"
                    :product-origin-price="productOriginPrice!"
                    :product-price="productPrice"
                    :discount-value="discountValue"
                    :product-stock="productStock"
                    :activity-bg-image="activityBgImage"
                    :overseas="useActivityCardLayout"
                />
            </template>
            <template v-if="isPresale">
                <productPresaleTitle
                    :product-info="productInfo"
                    :product-price="productPrice"
                    :presale-info="presaleInfo"
                    :sold-count="soldCount"
                    :activity-bg-image="activityBgImage"
                    :overseas="useActivityCardLayout"
                />
            </template>
            <template v-if="isGroupon && !isPresale">
                <productGrouponTitle
                    :product-info="productInfo"
                    :product-origin-price="productOriginPrice!"
                    :product-price="productPrice"
                    :groupon-value="grouponValue"
                    :product-stock="productStock"
                    :activity-bg-image="activityBgImage"
                    :vortmall="false"
                    :overseas="useActivityCardLayout"
                />
            </template>
            <template v-if="isNormalWithActivityBg">
                <view class="activity-bg-banner" :style="{ background: `url(${activityBgImage}) center/cover no-repeat` }">
                    <view class="activity-bg-overlay" />
                    <view class="activity-bg-content">
                        <view class="activity-bg-price-row">
                            <format-price
                                :font-style="{ fontSize: '48rpx', fontWeight: 'bold' }"
                                :decimals-style="{ fontSize: '32rpx', fontWeight: 'bold' }"
                                :currency-style="{ fontSize: '28rpx', fontWeight: 'bold' }"
                                :price-data="productPrice"
                            />
                            <template v-if="type === 'exchange'">
                                <view class="exchange-box">
                                    <text class="exchange-icon">+</text>
                                    <image class="exchange-img" :src="staticResource('exchange/exchange.png')" />
                                    <text class="exchange-text">{{ exchangeIntegral }}</text>
                                </view>
                            </template>
                            <view v-else-if="showMarketPrice && !showB2bText" class="activity-bg-origin-price">
                                <format-price :is-bottom="false" :price-data="normalStrikePrice" :font-style="{ 'text-decoration': 'line-through' }" />
                            </view>
                        </view>
                    </view>
                </view>
            </template>

            <template v-if="!isSeckill && !isDiscount && !isGroupon && !isCycleBuy && !isSubCard && !isPresale && !isNormalWithActivityBg">
                <!-- 标准风（A1）：标题 → 销量 → 价格，对齐后台预览与 vortmall -->
                <template v-if="isVortmallLayout">
                    <view class="vortmall-hero">
                        <view
                            class="product-name-text"
                            :style="[titleMaxLines ? nameClampStyle : nameCollapsedStyle, titleFontStyle, titleColor ? { color: titleColor } : {}]"
                            @click="!titleMaxLines && showNameExpandBtn && (isNameExpanded = !isNameExpanded)"
                        >
                            <view class="product-name-inner">
                                {{ productInfo.productName }}
                            </view>
                            <view v-if="!titleMaxLines && showNameExpandBtn && !isNameExpanded" class="product-name-btn product-name-expand">
                                <text class="iconfont-h5 icon-a-gengduo20" />
                            </view>
                            <view v-if="!titleMaxLines && showNameExpandBtn && isNameExpanded" class="product-name-btn product-name-collapse">
                                <text class="iconfont-h5 icon-a-gengduo20 iconTop" />
                            </view>
                        </view>
                        <view v-if="configStore.showSelledCount" class="vortmall-sold"
                            >{{ $t("已售") }}{{ soldCount || productInfo.virtualSales || 0 }}{{ $t("件") }}</view
                        >
                        <view class="vortmall-price">
                            <format-price
                                :font-style="{ fontSize: '49rpx', fontWeight: '600' }"
                                :decimals-style="{ fontSize: '49rpx', fontWeight: '600' }"
                                :currency-style="{ fontSize: '30rpx', fontWeight: '600' }"
                                :price-data="productPrice"
                            />
                            <template v-if="type === 'exchange'">
                                <view class="exchange-box">
                                    <text class="exchange-icon">+</text>
                                    <image class="exchange-img" :src="staticResource('exchange/exchange.png')" />
                                    <text class="exchange-text">{{ exchangeIntegral }}</text>
                                </view>
                            </template>
                            <template v-if="type !== 'exchange' && showMarketPrice && !showB2bText">
                                <view class="title-top-market_price">
                                    <format-price
                                        :is-bottom="false"
                                        :price-data="normalStrikePrice"
                                        :font-style="{
                                            fontWeight: '400',
                                            textDecoration: 'line-through',
                                            fontSize: '26rpx',
                                            color: marketPriceColor || '#999'
                                        }"
                                    />
                                </view>
                            </template>
                        </view>
                        <slot name="before-store-switch" />
                        <!-- A1 门店卡与活动/批发态使用同一外层宽度，避免被标题内容的横向内边距挤窄。 -->
                        <view class="vortmall-store-switch">
                            <slot name="store-switch" />
                        </view>
                        <template v-if="promotionList.length > 0">
                            <view class="vortmall-coupon" @click="handleCoupon">
                                <view class="vortmall-coupon-text line1">{{ promotionList[0]?.promotionName || $t("优惠活动") }}</view>
                                <view class="vortmall-coupon-more">{{ $t("更多优惠券") }}<text class="iconfont-h5 icon-a-gengduo20" /></view>
                            </view>
                        </template>
                        <view v-if="productInfo.productBrief" class="product-brief-text">{{ productInfo.productBrief }}</view>
                    </view>
                </template>
                <!-- 门店风（A2）等：价格在上、标题卡片在下 -->
                <template v-else>
                    <view class="title-info-top" :class="{ 'title-info-top--overseas': isOverseasLayout }">
                        <view class="title-top-price" :style="priceColor ? { color: priceColor } : {}">
                            <format-price
                                :font-style="{ fontSize: '48rpx', fontWeight: 'bold' }"
                                :decimals-style="{
                                    fontSize: '32rpx',
                                    fontWeight: 'bold'
                                }"
                                :currency-style="{
                                    fontSize: '28rpx',
                                    fontWeight: 'bold'
                                }"
                                :price-data="productPrice"
                            />
                            <template v-if="type === 'exchange'">
                                <view class="exchange-box">
                                    <text class="exchange-icon">+</text>
                                    <image class="exchange-img" :src="staticResource('exchange/exchange.png')" />
                                    <text class="exchange-text">{{ exchangeIntegral }}</text>
                                </view>
                            </template>
                            <template v-if="type !== 'exchange' && showMarketPrice && !showB2bText">
                                <view class="title-top-market_price">
                                    <format-price
                                        :is-bottom="false"
                                        :price-data="normalStrikePrice"
                                        :font-style="{ textDecoration: 'line-through', color: marketPriceColor || '#999' }"
                                    />
                                </view>
                            </template>
                        </view>
                        <!-- 跨境风（A3）：优惠券条与价格同处一张白底卡片，样式与标准风（A1）一致 -->
                        <view v-if="isOverseasLayout && promotionList.length > 0" class="vortmall-coupon" @click="handleCoupon">
                            <view class="vortmall-coupon-text line1">{{ promotionList[0]?.promotionName || $t("优惠活动") }}</view>
                            <view class="vortmall-coupon-more">{{ $t("更多优惠券") }}<text class="iconfont-h5 icon-a-gengduo20" /></view>
                        </view>
                    </view>
                </template>
            </template>
        </template>

        <productShare
            v-model="showShare"
            :product-id="Number(productId)"
            :product-info="productInfo"
            :product-price="productPrice"
            :layout-style="layoutStyle"
        />
        <!-- A2 名称卡片；A1 普通商品标题在 hero 中渲染，活动商品名称统一位于活动卡片下方 -->
        <view
            v-if="!isWholesaleMode && (isOverseasLayout ? !!productInfo.productBrief : !isVortmallLayout || isActivityProduct)"
            class="product-name-card"
            :class="{
                'has-activity': isActivityProduct && !isVortmallLayout,
                'product-name-card--vortmall-activity': isActivityProduct && isVortmallLayout
            }"
        >
            <template v-if="promotionList.length > 0 && !isOverseasLayout && !isVortmallLayout">
                <view class="activity-box" @click="handleCoupon">
                    <view class="flex">
                        <template v-for="item in promotionList.length > 2 ? promotionList.slice(0, 2) : promotionList" :key="item.promotionId">
                            <activityCard :activity-info="item" card-type="detail" />
                        </template>
                    </view>
                    <view class="activity-btn"> {{ $t("领券") }}<text class="iconfont-h5 icon-youjiantou" /> </view>
                </view>
            </template>
            <view
                v-if="!isOverseasLayout"
                class="product-name-text"
                :style="[titleMaxLines ? nameClampStyle : nameCollapsedStyle, titleFontStyle, titleColor ? { color: titleColor } : {}]"
                @click="!titleMaxLines && showNameExpandBtn && (isNameExpanded = !isNameExpanded)"
            >
                <view class="product-name-inner">
                    {{ productInfo.productName }}
                </view>
                <view v-if="!titleMaxLines && showNameExpandBtn && !isNameExpanded" class="product-name-btn product-name-expand">
                    <text class="iconfont-h5 icon-a-gengduo20" />
                </view>
                <view v-if="!titleMaxLines && showNameExpandBtn && isNameExpanded" class="product-name-btn product-name-collapse">
                    <text class="iconfont-h5 icon-a-gengduo20 iconTop" />
                </view>
            </view>
            <view v-if="isVortmallLayout && isActivityProduct && configStore.showSelledCount" class="vortmall-activity-name-sold">
                {{ $t("已售") }}{{ soldCount || productInfo.virtualSales || 0 }}{{ $t("件") }}
            </view>
            <view v-if="productInfo.productBrief" class="product-brief-text">{{ productInfo.productBrief }}</view>
        </view>
    </view>
</template>

<script setup lang="ts">
/**
 * 商品标题信息组件
 * - 显示商品名称、副标题、价格、划线价
 * - 销量统计、收藏功能
 * - 分享入口、活动标签展示
 */
import type { ProductItem, SkuPromotion } from "@/types/product/product";
import type { PresaleInfo } from "@/types/presale/presale";
import { ref, computed, watch, nextTick, getCurrentInstance, onMounted } from "vue";
import type { PropType } from "vue";
import { getCollectProduct, delCollectProduct, updateCollectProduct } from "@/api/product/product";
import productShare from "./productShare.vue";
import activityCard from "@/components/masonry/activityCard.vue";
import { staticResource } from "@/utils";
import { useI18n } from "vue-i18n";
import productGrouponTitle from "./productGrouponTitle.vue";
import productSeckillTitle from "./productSeckillTitle.vue";
import productDiscountTitle from "./productDiscountTitle.vue";
import productPresaleTitle from "./productPresaleTitle.vue";
import { isShowB2bPriceText } from "@/utils/b2bPrice";
import { ensureShareLogin } from "@/utils/shareLogin";
import { useConfigStore } from "@/store/config";

const configStore = useConfigStore();
const instance = getCurrentInstance();

// 是否是周期购
const isCycleBuy = defineModel<boolean>("isCycleBuy", { type: Boolean, default: false });

// 是否是次卡
const isSubCard = defineModel<boolean>("isSubCard", { type: Boolean, default: true });

const props = defineProps({
    productInfo: {
        type: Object as PropType<ProductItem>,
        default: () => ({})
    },
    productPrice: {
        type: [String, Number],
        default: "0"
    },
    isSeckill: {
        type: Number,
        default: 0
    },
    isGroupon: {
        type: Number,
        default: 0
    },
    isDiscount: {
        type: Number,
        default: 0
    },
    isPresale: {
        type: Number,
        default: 0
    },
    promotionList: {
        type: Array as PropType<SkuPromotion[]>,
        default: () => []
    },
    showCoupon: {
        type: Boolean,
        default: false
    },
    type: {
        type: String,
        default: ""
    },
    exchangeIntegral: {
        type: Number,
        default: 0
    },
    productOriginPrice: {
        type: [String, Number],
        default: "0"
    },
    grouponValue: {
        type: Object,
        default: () => {}
    },
    productStock: {
        type: Number,
        default: 0
    },
    activityBgImage: {
        type: String,
        default: ""
    },
    seckillValue: {
        type: Object,
        default: () => ({})
    },
    discountValue: {
        type: Object,
        default: () => ({})
    },
    presaleInfo: {
        type: Object as PropType<PresaleInfo>,
        default: () => null
    },
    presalePaymentTimeText: {
        type: String,
        default: ""
    },
    presaleDeliveryTimeText: {
        type: String,
        default: ""
    },
    soldCount: {
        type: Number,
        default: 0
    },
    // 批发态标记：由详情页下传；纯零售构建下详情页该值被裁切为 undefined，此处默认 false 保证零售渲染
    isWholesaleMode: {
        type: Boolean,
        default: false
    },
    // 标题颜色（风格一 装修字段，未配置时用默认色）
    titleColor: {
        type: String,
        default: ""
    },
    // 普通价格颜色（风格一 装修字段，仅作用于非活动价格；活动标题保持主题色）
    priceColor: {
        type: String,
        default: ""
    },
    // 普通商品划线价颜色（A1/A2/A3 共用，未配置时保持当前默认色）
    marketPriceColor: {
        type: String,
        default: ""
    },
    // 标题字号（风格一 装修字段：large/medium/small）
    titleSize: {
        type: String,
        default: ""
    },
    // 标题最多显示行数（风格一 装修字段：1/2/3；配置后按行数截断，替代默认展开/收起）
    titleMaxLines: {
        type: Number,
        default: 0
    },
    // 布局风格：A1 标准风 / A2 门店风 / A3 跨境风
    layoutStyle: {
        type: String,
        default: "o2o"
    }
});

const emit = defineEmits(["update:showCoupon"]);

const { t } = useI18n();

/** 标准风（A1）：标题在上、价格在下，对齐后台预览 */
const isVortmallLayout = computed(() => props.layoutStyle === "A1");

/** 跨境风（A3）：标题+已售在主图上方由页面渲染，本组件在主图下方只渲染价格与优惠券 */
const isOverseasLayout = computed(() => props.layoutStyle === "A3");

/** A1 活动商品复用 A3 活动卡片结构，A2 保持原有活动样式。 */
const useActivityCardLayout = computed(() => isVortmallLayout.value || isOverseasLayout.value);

const showB2bText = computed(() => isShowB2bPriceText());

const isNameExpanded = ref(false);
const showNameExpandBtn = ref(false);

const doMeasureName = (retries = 3) => {
    // 风格一配置了最大行数时改用 line-clamp 截断，无需测高与展开按钮
    if (props.titleMaxLines) {
        showNameExpandBtn.value = false;
        return;
    }
    uni.createSelectorQuery()
        .in(instance)
        .select(".product-name-inner")
        .boundingClientRect((rect: any) => {
            if (rect && rect.height > 0) {
                const lineHeight = uni.upx2px(36) * 1.4;
                const overflows = rect.height > lineHeight * 2.5;
                showNameExpandBtn.value = overflows;
                if (overflows) isNameExpanded.value = false;
            } else if (retries > 0) {
                setTimeout(() => doMeasureName(retries - 1), 200);
            } else {
                isNameExpanded.value = false;
            }
        })
        .exec();
};

const checkNameOverflow = () => {
    isNameExpanded.value = true;
    nextTick(() => {
        setTimeout(() => doMeasureName(), 150);
    });
};

const nameCollapsedStyle = computed(() => {
    if (isNameExpanded.value) return {};
    return {
        maxHeight: `${Math.ceil(50.4 * 2)}rpx`,
        overflow: "hidden"
    };
});

// ===== 标题装修（风格一）：字号与行数截断，与标准风视觉一致 =====
const titleSizeMap: Record<string, string> = { large: "36rpx", medium: "31rpx", small: "28rpx" };

const titleFontStyle = computed(() => (props.titleSize ? { fontSize: titleSizeMap[props.titleSize] || "31rpx" } : {}));

// 配置了最大行数时用 line-clamp 截断，替代默认展开/收起逻辑
const nameClampStyle = computed(() => {
    if (!props.titleMaxLines) return {};
    return {
        display: "-webkit-box",
        overflow: "hidden",
        "-webkit-box-orient": "vertical",
        "-webkit-line-clamp": String(props.titleMaxLines)
    };
});

const isActivityProduct = computed(() => {
    return !!(props.isSeckill || props.isDiscount || props.isGroupon || isCycleBuy.value || isSubCard.value || props.isPresale || props.activityBgImage);
});

const isNormalWithActivityBg = computed(() => {
    return !props.isSeckill && !props.isDiscount && !props.isGroupon && !isCycleBuy.value && !isSubCard.value && !props.isPresale && !!props.activityBgImage;
});

const isSupplierProduct = computed(() => {
    const suppliersId = props.productInfo?.suppliersId;
    if (suppliersId === null || suppliersId === undefined || suppliersId === "") {
        return false;
    }
    const numericId = Number(suppliersId);
    return Number.isFinite(numericId) && numericId > 0;
});

const normalStrikePrice = computed(() => {
    // 供应商商品：仅使用市场价作为划线价，没有市场价则不展示划线价
    if (isSupplierProduct.value) {
        if (props.productInfo.marketPrice && Number(props.productInfo.marketPrice) > 0) {
            return props.productInfo.marketPrice;
        }
        return null;
    }
    if (props.productOriginPrice && Number(props.productOriginPrice) > Number(props.productPrice)) {
        return props.productOriginPrice;
    }
    if (props.productInfo.marketPrice && Number(props.productInfo.marketPrice) > 0) {
        return props.productInfo.marketPrice;
    }
    return null;
});

const showMarketPrice = computed(() => {
    return Number(configStore.showMarketprice) === 1 && !!normalStrikePrice.value;
});

const isCollect = ref(false);

const productId = ref<number>(0);

const handleCoupon = () => {
    emit("update:showCoupon", true);
};
const getCollect = async (id: any) => {
    try {
        const result = await getCollectProduct({ id });
        isCollect.value = result;
    } catch (error: any) {
        if (error.code === 401) return (isCollect.value = false);
        uni.showToast({
            title: error.message,
            icon: "none"
        });
    }
};
const addCollect = async () => {
    try {
        if (isCollect.value) {
            await delCollectProduct({
                id: productId.value
            });
        } else {
            await updateCollectProduct({
                productId: productId.value
            });
        }
        uni.showToast({
            title: isCollect.value ? t("取消收藏成功") : t("收藏成功"),
            icon: "none"
        });
        await getCollect(productId.value);
    } catch (error: any) {
        uni.showToast({
            title: error.message,
            icon: "none"
        });
    }
};

const showShare = ref(false);
const handleShare = () => {
    if (!ensureShareLogin(true)) {
        return;
    }
    showShare.value = true;
};

watch(
    () => props.productInfo,
    (newValue) => {
        if (newValue) {
            productId.value = newValue.productId;
            getCollect(newValue.productId);
            checkNameOverflow();
        }
    }
);

onMounted(() => {
    if (props.productInfo?.productName) {
        checkNameOverflow();
    }
});

defineExpose({
    addCollect,
    handleShare,
    isCollect
});
</script>

<style lang="scss" scoped>
.product-title-info {
    margin-top: 20rpx;
    border-radius: 24rpx;
    overflow: hidden;
    // background-color: #fff;

    &.is-vortmall-layout {
        margin-top: 0;
        border-radius: 0;
    }

    // 批发价格块已承担顶部商品信息，空标题容器不再额外制造上下间距。
    &.is-wholesale-mode {
        margin-top: 0;
    }

    .vortmall-hero {
        background: var(--product-content-bg, #fff);
        padding: 20rpx 32rpx 24rpx;

        .vortmall-store-switch {
            // 活动态/批发态的门店卡位于内容区全宽，普通 A1 也保持相同左右边界。
            margin-left: -32rpx;
            margin-right: -32rpx;
        }

        .product-name-text {
            font-size: 34rpx;
            font-weight: 600;
            color: #666;
            line-height: 1.45;
            word-break: break-all;
            position: relative;

            .product-name-inner {
                display: inline;
            }
        }

        .product-name-btn {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            color: #333;

            .iconfont-h5 {
                font-size: 32rpx;
            }
            .icon-a-gengduo20 {
                transform: rotate(90deg);
            }
            .iconTop {
                transform: rotate(-90deg);
            }
        }

        .vortmall-sold {
            margin-top: 16rpx;
            font-size: 25rpx;
            color: #1a1a1a;
        }

        .vortmall-price {
            margin-top: 16rpx;
            color: #1a1a1a;
            font-size: 49rpx;
            font-weight: 600;
            display: flex;
            align-items: baseline;
        }

        .product-brief-text {
            margin-top: 12rpx;
            font-size: 24rpx;
            color: #999;
        }
    }

    /* 优惠券条：标准风（A1）与跨境风（A3）共用 */
    .vortmall-coupon {
        margin-top: 20rpx;
        padding: 20rpx 24rpx;
        background: #f7f8fa;
        border-radius: 16rpx;
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 16rpx;

        .vortmall-coupon-text {
            flex: 1;
            font-size: 26rpx;
            font-weight: 600;
            color: #ff4d4f;
        }

        .vortmall-coupon-more {
            flex-shrink: 0;
            display: flex;
            align-items: center;
            gap: 4rpx;
            font-size: 24rpx;
            color: #888b94;

            .iconfont-h5 {
                font-size: 22rpx;
            }
        }
    }

    .activity-bg-banner {
        position: relative;
        min-height: 160rpx;
        padding: 25rpx 30rpx;
        overflow: hidden;

        .activity-bg-overlay {
            position: absolute;
            inset: 0;
            background: linear-gradient(180deg, rgba(0, 0, 0, 0.05) 0%, rgba(0, 0, 0, 0.25) 100%);
        }

        .activity-bg-content {
            position: relative;
            z-index: 1;
        }

        .activity-bg-price-row {
            display: flex;
            align-items: baseline;
            color: #fff;
        }

        .activity-bg-origin-price {
            color: rgba(255, 255, 255, 0.7);
            font-size: 24rpx;
            margin-left: 15rpx;
        }
    }

    .title-info-top {
        background: var(--product-content-bg, #fff);
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 20rpx 30rpx 0;
        border-radius: 24rpx 24rpx 0 0;
        .title-top-price {
            color: var(--general);
            font-weight: bold;
            font-size: 48rpx;
            display: flex;
            align-items: baseline;
        }

        .title-top-market_price {
            display: flex;
            align-items: baseline;
            margin-left: 15rpx;
            font-size: 24rpx;
            color: #999;
        }

        /* 跨境风：价格与优惠券纵向左对齐排布在同一张白底卡片内 */
        &.title-info-top--overseas {
            flex-direction: column;
            justify-content: flex-start;
            align-items: flex-start;

            .title-top-price {
                width: 100%;
            }

            .vortmall-coupon {
                width: 100%;
                margin-top: 12rpx;
            }
        }
    }

    .product-name-card {
        background: var(--product-content-bg, #fff);
        padding: 24rpx 30rpx;
        position: relative;

        &.has-activity {
            border-radius: 24rpx;
            margin-top: -40rpx;
            z-index: 2;
        }

        &.product-name-card--vortmall-activity {
            padding: 20rpx 28rpx 24rpx;

            .product-name-text {
                font-size: 31rpx;
                font-weight: 700;
                line-height: 42rpx;
            }

            .vortmall-activity-name-sold {
                margin-top: 16rpx;
                color: #1a1a1a;
                font-size: 25rpx;
            }
        }

        .product-name-text {
            font-size: 36rpx;
            font-weight: bold;
            color: #2a2a2a;
            line-height: 1.4;
            word-break: break-all;
            position: relative;

            .product-name-inner {
                display: inline;
            }
        }

        .product-name-btn {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            color: #333;

            .iconfont-h5 {
                font-size: 32rpx;
            }
            .icon-a-gengduo20 {
                transform: rotate(90deg);
            }
            .iconTop {
                transform: rotate(-90deg);
            }
        }

        .product-name-expand {
            position: absolute;
            right: 0;
            bottom: 0;
            padding-left: 40rpx;
            background: linear-gradient(to right, rgba(255, 255, 255, 0) 0%, #fff 40%);
        }

        .product-name-collapse {
            display: inline-flex;
            vertical-align: middle;
            margin-left: 8rpx;
        }

        .product-brief-text {
            margin-top: 12rpx;
            font-size: 24rpx;
            color: #999;
            line-height: 1.4;
            word-break: break-all;
        }

        .cycleBuyTag,
        .subCardTag {
            color: var(--general);
            position: relative;
            display: inline-block;
            padding-right: 20rpx;
            vertical-align: baseline;
            &::before {
                position: absolute;
                content: "";
                right: 9rpx;
                top: 0;
                width: 2rpx;
                height: 80%;
                transform: translateY(15%);
                background-color: var(--general);
            }
        }
    }
}

.exchange-box {
    display: flex;
    align-items: center;
    .exchange-img {
        width: 35rpx;
        height: 35rpx;
        padding-right: 5rpx;
    }
    .exchange-icon {
        padding: 0 5rpx;
    }
    .exchange-text {
        font-size: 34rpx;
    }
}

.activity-box {
    width: 100%;
    background: var(--product-content-bg, #fff);
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 0 15rpx 0;
    .activity-btn {
        font-size: 24rpx;
        color: var(--main-bg);
        display: flex;
        align-items: center;
        padding-right: 6rpx;
        .icon-youjiantou {
            font-size: 36rpx;
            margin-left: -8rpx;
        }
    }
}
</style>
