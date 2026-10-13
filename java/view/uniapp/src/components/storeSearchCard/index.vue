<template>
    <view class="store-search-card" :style="cardStyle" @click="handleToStore">
        <view class="card-header">
            <view class="shop-logo" :style="logoStyle">
                <tig-image :src="store.shopLogo" mode="widthFix" />
            </view>
            <view class="shop-main">
                <view class="title-row">
                    <view v-if="visible.showRating" class="store-tag">{{ $t("星级门店") }}</view>
                    <text class="shop-title line1" :style="shopTitleStyle">{{ store.shopTitle }}</text>
                    <view v-if="showCollect" class="collect-btn" :class="{ collected: isCollect }" :style="themeBtnStyle" @click.stop="handleCollect">
                        <text class="collect-icon iconfont-h5" :class="isCollect ? 'icon-yiguanzhu' : 'icon-guanzhu'" />
                        <text class="collect-text">{{ isCollect ? $t("已关注") : $t("关注") }}</text>
                    </view>
                </view>
                <view class="meta-row">
                    <text v-if="visible.showRating" class="shop-star" :style="themeTextStyle">{{ scoreText }}{{ $t("分") }}</text>
                    <template v-if="store.localDeliveryEnabled">
                        <view v-if="visible.showMinOrder" class="meta-item meta-price">
                            <text>{{ $t("起送") }}</text>
                            <format-price
                                :price-data="store.minOrderAmount"
                                :font-style="metaPriceStyle"
                                :currency-style="metaPriceStyle"
                                :decimals-style="metaPriceStyle"
                            />
                        </view>
                        <template v-if="visible.showDeliveryFee">
                            <text v-if="isFreeDelivery" class="meta-item free-delivery">{{ $t("免配送费") }}</text>
                            <view v-else class="meta-item meta-price">
                                <text>{{ $t("配送费") }}</text>
                                <format-price
                                    :price-data="store.deliveryFee"
                                    :font-style="metaPriceStyle"
                                    :currency-style="metaPriceStyle"
                                    :decimals-style="metaPriceStyle"
                                />
                            </view>
                        </template>
                    </template>
                    <text v-if="visible.showDistance && distanceText" class="meta-item">{{ $t("距离") }} {{ distanceText }}</text>
                </view>
            </view>
        </view>

        <scroll-view v-if="productList.length > 0" class="product-scroll" scroll-x show-scrollbar="false">
            <view class="product-row" :style="{ columnGap: productGapStyle }">
                <template v-for="item in productList" :key="item.productId">
                    <view class="product-item" @click.stop="handleToProduct(item)">
                        <view class="product-image" :style="productImageStyle">
                            <tig-image :src="pickPic(item)" mode="aspectFill" />
                        </view>
                        <view v-if="visible.showProductName" class="product-name" :class="productNameRowClass">
                            {{ item.productName }}
                        </view>
                        <view v-if="visible.showProductPrice" class="product-price" :style="themeTextStyle">
                            <format-price
                                :price-data="item.price ?? item.productPrice"
                                :font-style="{ fontSize: '26rpx', fontWeight: 'bold', color: themeColorValue }"
                                :currency-style="{ fontSize: '20rpx', fontWeight: 'bold', color: themeColorValue }"
                                :decimals-style="{ fontSize: '22rpx', fontWeight: 'bold', color: themeColorValue }"
                            />
                        </view>
                    </view>
                </template>
            </view>
        </scroll-view>
    </view>
</template>

<script setup lang="ts">
/**
 * 门店聚合搜索卡片
 * - 门店头部：评分、起送价、预估配送费、距离、关注
 * - 商品横滑：命中关键词的商品在前，补齐商品在后
 */
import { computed, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import { shopCollection } from "@/api/shop/shop";
import { formatPadZero, pickPic } from "@/utils/format";
import { staticResource } from "@/utils";
import type { StoreSearchProduct, StoreSearchRecord } from "@/types/search/storeSearch";

const props = withDefaults(
    defineProps<{
        store: StoreSearchRecord;
        /** 外层列表是否统一管理门店间距；开启时取消卡片自带的底部间距 */
        manageListGap?: boolean;
        /** 是否已带上有效经纬度，未定位时接口返回的 distance 不可信 */
        hasLocation?: boolean;
        /** 是否显示门店评分，0 不显示 / 1 显示，默认显示 */
        showRating?: number;
        /** 是否显示起送价，0 不显示 / 1 显示，默认显示 */
        showMinOrder?: number;
        /** 是否显示配送费，0 不显示 / 1 显示，默认显示 */
        showDeliveryFee?: number;
        /** 是否显示距离，0 不显示 / 1 显示，默认显示 */
        showDistance?: number;
        /** 是否显示关注按钮，0 不显示 / 1 显示，默认显示 */
        showFollow?: number;
        /** 商品项间距（px），默认 8 */
        productGap?: number;
        /** 是否显示商品名称，0 不显示 / 1 显示，默认显示 */
        showProductName?: number;
        /** 商品名称行数 1/2，默认 1 */
        productNameRow?: number;
        /** 是否显示商品价格，0 不显示 / 1 显示，默认显示 */
        showProductPrice?: number;
        /** 门店标题颜色 */
        shopTitleColor?: string;
        /** 门店 Logo 圆角百分比（0-50），默认 50（圆形） */
        shopLogoRadius?: number;
        /** 门店卡片圆角（px），默认 8 */
        cardRadius?: number;
        /** 门店卡片上下内间距（px），默认 12 */
        cardPaddingTopBottom?: number;
        /** 门店卡片左右内间距（px），默认 12 */
        cardPaddingLeftRight?: number;
        /** 门店标题字号（px） */
        shopTitleFontSize?: number;
        /** 门店标题粗细 */
        shopTitleFontBold?: number;
        /** 门店卡片背景色 */
        itemBackgroundColor?: string;
        /** 主题色（关注按钮、评分、价格） */
        themeColor?: string;
        /** 商品图片圆角（px） */
        productImageRadius?: number;
        /** 由列表容器统一管理门店间距时移除卡片默认底部间距 */
        manageListGap?: boolean;
        /** 是否应用搜索页门店卡片装修样式 */
        applyCardStyle?: boolean;
    }>(),
    {
        manageListGap: false,
        hasLocation: false,
        showRating: 1,
        showMinOrder: 1,
        showDeliveryFee: 1,
        showDistance: 1,
        showFollow: 1,
        productGap: 8,
        showProductName: 1,
        productNameRow: 1,
        showProductPrice: 1,
        shopLogoRadius: 50,
        cardRadius: 8,
        cardPaddingTopBottom: 12,
        cardPaddingLeftRight: 12,
        shopTitleColor: "#1a1a1a",
        shopTitleFontSize: 15,
        shopTitleFontBold: 700,
        itemBackgroundColor: "#fff",
        themeColor: "",
        productImageRadius: 6,
        manageListGap: false,
        applyCardStyle: false
    }
);

/** 装修配置项统一按 1 显示、其它值不显示 */
const visible = computed(() => ({
    showRating: Number(props.showRating) === 1,
    showMinOrder: Number(props.showMinOrder) === 1,
    showDeliveryFee: Number(props.showDeliveryFee) === 1,
    showDistance: Number(props.showDistance) === 1,
    showFollow: Number(props.showFollow) === 1,
    showProductName: Number(props.showProductName) === 1,
    showProductPrice: Number(props.showProductPrice) === 1
}));

/** 商品间距：配置为 px，转 rpx 后与卡片其它尺寸口径一致 */
const productGapStyle = computed(() => {
    const gap = Number(props.productGap);
    const px = Number.isFinite(gap) ? Math.max(0, gap) : 8;
    return `${px * 2}rpx`;
});

const pxToRpx = (value: unknown, fallback: number) => {
    const num = Number(value);
    return `${(Number.isFinite(num) ? Math.max(0, num) : fallback) * 2}rpx`;
};

const cardStyle = computed(() => ({
    ...(props.applyCardStyle
        ? {
              padding: `${pxToRpx(props.cardPaddingTopBottom, 12)} ${pxToRpx(props.cardPaddingLeftRight, 12)}`,
              borderRadius: pxToRpx(props.cardRadius, 8),
              backgroundColor: props.itemBackgroundColor || "#fff",
              overflow: "hidden",
              boxSizing: "border-box"
          }
        : {}),
    ...(props.manageListGap ? { marginBottom: "0rpx" } : {})
}));

const logoStyle = computed(() => {
    if (!props.applyCardStyle) return {};
    const radius = Number(props.shopLogoRadius);
    return { borderRadius: `${Number.isFinite(radius) ? Math.min(50, Math.max(0, radius)) : 50}%` };
});

const productImageStyle = computed(() => (props.applyCardStyle ? { borderRadius: pxToRpx(props.productImageRadius, 6) } : {}));

/** 扩展设置主题色，未配置时回落站点 --general */
const themeColorValue = computed(() => props.themeColor || "var(--general)");
const themeTextStyle = computed(() => ({ color: themeColorValue.value }));

const productNameRowClass = computed(() => (Number(props.productNameRow) === 2 ? "line2" : "line1"));

const { t } = useI18n();

const isCollect = ref<boolean>(Boolean(props.store.collectShop));
const collectLoading = ref<boolean>(false);

/** 关注按钮：未关注时用装修主题色，已关注走 .collected 样式 */
const themeBtnStyle = computed(() => {
    if (isCollect.value) return {};
    return { backgroundColor: themeColorValue.value };
});

watch(
    () => props.store.collectShop,
    (val) => {
        isCollect.value = Boolean(val);
    }
);

const productList = computed(() => props.store.productList ?? []);

/** 配置关闭时不提供关注 */
const showCollect = computed(() => visible.value.showFollow);

/** 门店标题样式：装修配置为 px，转 rpx 与卡片其它尺寸口径一致 */
const shopTitleStyle = computed(() => {
    const size = Number(props.shopTitleFontSize);
    const px = Number.isFinite(size) && size > 0 ? size : 15;
    return {
        fontSize: `${px * 2}rpx`,
        color: props.shopTitleColor || "#1a1a1a",
        fontWeight: props.shopTitleFontBold ?? 700
    };
});

const scoreText = computed(() => {
    const star = Number(props.store.shopStar);
    return formatPadZero(!star || star <= 0 ? 5 : star, 1);
});

const tagBg = computed(() => `url(${staticResource("store/tag-bg.png")})`);

const distanceText = computed(() => {
    if (!props.hasLocation) return "";
    const num = Number(props.store.distance);
    if (props.store.distance == null || isNaN(num) || num < 0) return "";
    if (num < 1000) return `${Math.round(num)}m`;
    return `${(num / 1000).toFixed(2)}km`;
});

/** 起送价与配送费沿用 meta 行的字号与颜色 */
const metaPriceStyle = {
    fontSize: "22rpx",
    fontWeight: "normal",
    color: "#999"
};

/** 配送费为 0 时不展示金额，改为「免配送费」 */
const isFreeDelivery = computed(() => Number(props.store.deliveryFee ?? 0) === 0);

const handleToStore = () => {
    uni.navigateTo({
        url: `/pages/store/index?id=${props.store.shopId}`
    });
};

const handleToProduct = (item: StoreSearchProduct) => {
    if (!item.productId) return;
    uni.navigateTo({
        url: `/pages/product/index?id=${item.productId}&shopId=${props.store.shopId}`
    });
};

const handleCollect = async () => {
    if (collectLoading.value) return;
    collectLoading.value = true;
    try {
        await shopCollection({ shopId: props.store.shopId });
        isCollect.value = !isCollect.value;
        uni.$emit("shopCollectionChanged", { shopId: props.store.shopId, collected: isCollect.value });
        uni.showToast({
            title: isCollect.value ? t("关注成功") : t("取消关注成功"),
            icon: "none"
        });
    } catch (error: any) {
        uni.showToast({
            title: error?.message || t("操作失败"),
            icon: "none"
        });
    } finally {
        collectLoading.value = false;
    }
};
</script>

<style lang="scss" scoped>
.store-search-card {
    background-color: #fff; /* 无装修时兜底；有 itemBackgroundColor 时由 cardStyle 覆盖 */
    border-radius: 16rpx;
    padding: 24rpx;
    margin-bottom: 20rpx;

    .card-header {
        display: flex;
        align-items: flex-start;

        .shop-logo {
            flex-shrink: 0;
            width: 80rpx;
            height: 80rpx;
            border-radius: 50%;
            overflow: hidden;
            background-color: #f5f5f5;

            // 非小程序端图片组件外层会多一层行内根节点，需显式撑满容器，否则图片按原始尺寸渲染
            :deep(uni-tig-image) {
                display: block;
                width: 100%;
                height: 100%;
            }
        }

        .shop-main {
            flex: 1;
            width: 0;
            margin-left: 16rpx;
        }

        .title-row {
            display: flex;
            align-items: center;
            justify-content: space-between;
            column-gap: 8rpx;

            .store-tag {
                flex-shrink: 0;
                background-image: v-bind(tagBg);
                background-size: cover;
                background-position: center;
                background-repeat: no-repeat;
                min-width: 106rpx;
                height: 26rpx;
                font-size: 16rpx;
                color: #ffffff;
                padding-left: 30rpx;
                padding-right: 10rpx;
                line-height: 26rpx;
            }

            .shop-title {
                flex: 1;
                width: 0;
                line-height: 42rpx;
            }

            .collect-btn {
                flex-shrink: 0;
                display: flex;
                align-items: center;
                height: 48rpx;
                padding: 0 20rpx;
                border-radius: 24rpx;
                background-color: var(--search-theme, var(--general));
                color: #fff;

                &.collected {
                    background-color: #f5f5f5;
                    color: #999;
                }

                .collect-icon {
                    font-size: 22rpx;
                    margin-right: 6rpx;
                }

                .collect-text {
                    font-size: 22rpx;
                    line-height: 1;
                }
            }
        }

        .meta-row {
            display: flex;
            flex-wrap: wrap;
            align-items: center;
            margin-top: 10rpx;
            column-gap: 16rpx;
            row-gap: 6rpx;

            .shop-star {
                font-size: 22rpx;
                font-weight: 600;
                color: var(--search-theme, var(--general));
            }

            .meta-item {
                font-size: 22rpx;
                color: #999;

                &.meta-price {
                    display: flex;
                    align-items: center;
                    column-gap: 4rpx;
                }

                &.free-delivery {
                    // color: var(--general);
                }
            }
        }
    }

    .product-scroll {
        width: 100%;
        margin-top: 20rpx;
        white-space: nowrap;

        .product-row {
            display: flex;
        }

        .product-item {
            flex-shrink: 0;
            width: 156rpx;

            .product-image {
                width: 156rpx;
                height: 156rpx;
                border-radius: 12rpx; /* 无装修时兜底；有 productImageRadius 时由 productImageStyle 覆盖 */
                overflow: hidden;
                background-color: #f5f5f5;

                :deep(uni-tig-image) {
                    display: block;
                    width: 100%;
                    height: 100%;
                }
            }

            .product-name {
                margin-top: 10rpx;
                font-size: 24rpx;
                color: #333;
                line-height: 34rpx;

                &.line1 {
                    overflow: hidden;
                    white-space: nowrap;
                    text-overflow: ellipsis;
                }

                &.line2 {
                    display: -webkit-box;
                    overflow: hidden;
                    text-overflow: ellipsis;
                    -webkit-line-clamp: 2;
                    -webkit-box-orient: vertical;
                    white-space: normal;
                }
            }

            .product-price {
                margin-top: 4rpx;
                color: var(--search-theme, var(--general));
            }
        }
    }
}
</style>
