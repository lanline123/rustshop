<template>
    <view
        class="item-li"
        :class="itemClasses"
        :style="{ ...decorateVars, backgroundColor: itemBackgroundColor || undefined, borderRadius: productCardRadius }"
        @click="toPage(item)"
    >
        <view class="photo" :style="{ borderRadius: productCardRadius }">
            <tig-lazy-image :src="productImage(item)" mode="widthFix" />
            <view v-if="showOutsale" class="product-status-box">
                <view class="outsale">{{ $t("已售罄") }}</view>
            </view>
            <image v-if="currentStyle?.borderImage" class="product-style-border" :src="currentStyle.borderImage" mode="scaleToFill" />
        </view>
        <view class="info">
            <view v-if="showName" class="detail presale-box" :style="detailStyle">
                <text v-if="hasGrouponActivity" class="groupon-tag">{{ $t("拼") }}</text>
                <template v-if="(item.saleMode && item.saleMode > 0) || item.productSaleMode">
                    <text class="presale-tag">{{ $t("预售") }}</text>
                    <text class="separator">|</text>
                </template>
                <text>{{ item.productName }}</text>
            </view>
            <view v-if="hasPromotionInfo" class="flex activity-box">
                <activityCard v-for="subItem in currentPromotionInfo" :key="subItem.promotionId" :activity-info="subItem" :theme-color="themeColor" />
            </view>

            <view class="action">
                <view class="pricenum">
                    <template v-if="type === 'exchange'">
                        <view class="exchange-box">
                            <format-price
                                :font-style="{ fontSize: '32rpx' }"
                                :currency-style="{ fontSize: '22rpx' }"
                                :decimals-style="{ fontSize: '25rpx' }"
                                :price-data="item.discountsPrice"
                            />
                            <text class="exchange-icon">+</text>
                            <image class="exchange-img" :src="staticResource('exchange/exchange.png')" />
                            <text class="exchange-text">{{ item.exchangeIntegral }}</text>
                        </view>
                    </template>
                    <template v-else-if="isWholesaleCardComputed">
                        <format-price
                            :font-style="{ fontSize: '32rpx' }"
                            :currency-style="{ fontSize: '22rpx' }"
                            :decimals-style="{ fontSize: '25rpx' }"
                            :price-data="item.firstWholesalePrice"
                        />
                    </template>
                    <template v-else>
                        <format-price
                            :font-style="{ fontSize: '32rpx' }"
                            :currency-style="{ fontSize: '22rpx' }"
                            :decimals-style="{ fontSize: '25rpx' }"
                            :price-data="item.price"
                        />
                    </template>
                </view>

                <view v-if="showBuyBtn" class="buy_btn" :class="buyBtnClasses" @click.stop="handleBuy(item)">
                    <template v-if="item.isCheck">
                        <up-loading-icon size="22" mode="circle" />
                    </template>
                    <!-- 装修按钮样式 1-8（来自 cate-center-v2.buyBtnStyle） -->
                    <template v-else-if="buyBtnStyle === 5 || buyBtnStyle === 6">
                        <text class="buy_btn_text">{{ $t("购买") }}</text>
                    </template>
                    <template v-else-if="buyBtnStyle === 7 || buyBtnStyle === 8">
                        <text class="buy_btn_text">{{ $t("马上抢") }}</text>
                    </template>
                    <template v-else-if="buyBtnStyle === 1">
                        <text class="iconfont-h5 buy_btn_icon icon-gouwuche" />
                    </template>
                    <template v-else-if="buyBtnStyle === 2">
                        <text class="iconfont-h5 buy_btn_icon icon-gouwuche1" />
                    </template>
                    <template v-else-if="buyBtnStyle === 3">
                        <text class="iconfont-h5 buy_btn_icon icon-jia" />
                    </template>
                    <template v-else-if="buyBtnStyle === 4">
                        <text class="iconfont-h5 buy_btn_icon icon-jia1" />
                    </template>
                    <!-- 未传 buyBtnStyle 时沿用旧图标（其他场景不受影响） -->
                    <template v-else-if="type !== 'exchange'">
                        <text class="iconfont-h5 icon-gouwuche3 buy_btn_icon" />
                    </template>
                </view>
            </view>

            <!-- <template v-if="item.shop && showShop">
                <view class="shop" @click.stop="handleToShop(item.shop.shopId)">
                    <view class="shop-name">{{ item.shop.shopTitle }}</view>
                    <view class="shop-btn">{{ $t("进店") }} <text class="iconfont-h5 icon-youjiantou" /></view>
                </view>
            </template> -->
        </view>
    </view>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { productImage } from "@/utils/format";
import activityCard from "./activityCard.vue";
import { staticResource } from "@/utils";
import { shouldShowSoldOut } from "@/utils/productSoldOut";
import dayjs from "dayjs";
import type { ProductActiveStyleVO } from "@/types/product/productStyle";
import { useProductCardBuy } from "@/hooks/useProductCardBuy";

interface Props {
    item: any;
    promotionList: any;
    styleMap?: Record<number, ProductActiveStyleVO>;
    type?: string;
    showShop?: boolean;
    isCycleBuy?: boolean;
    isSubCard?: boolean;
    /** 文本对齐：1=左对齐，2=居中对齐 */
    textAlign?: number;
    /** 文字粗细：1=常规体，2=加粗体 */
    textWeight?: number;
    /** 商品名称行数：1=1行，2=2行 */
    goodsNameRow?: number;
    /** 商品标题行数（兼容字段，等价 goodsNameRow） */
    productTitleLine?: number;
    /** 是否显示商品名称 */
    showName?: boolean;
    /** 购买按钮样式：undefined=沿用旧图标，0=不显示，1~8=对应样式 */
    buyBtnStyle?: number;
    /** 是否显示优惠券活动；undefined 表示沿用其他页面原有行为 */
    showCoupon?: boolean;
    productTitleColor?: string;
    priceColor?: string;
    themeColor?: string;
    itemBackgroundColor?: string;
    productImageRadius?: number;
}

const props = withDefaults(defineProps<Props>(), {
    type: "",
    showShop: true,
    isCycleBuy: false,
    isSubCard: false,
    styleMap: () => ({}),
    textAlign: undefined,
    textWeight: undefined,
    goodsNameRow: undefined,
    productTitleLine: undefined,
    showName: true,
    buyBtnStyle: undefined,
    showCoupon: undefined,
    productTitleColor: undefined,
    priceColor: undefined,
    themeColor: undefined,
    itemBackgroundColor: undefined,
    productImageRadius: undefined
});

const decorateVars = computed(() => ({
    "--pi-title-color": props.productTitleColor || "",
    "--pi-price-color": props.priceColor || "",
    "--pi-theme": props.themeColor || "",
    "--pi-item-bg": props.itemBackgroundColor || "",
    "--pi-image-radius": props.productImageRadius == null ? "" : `${Math.max(0, props.productImageRadius) * 2}rpx`
}));

const productCardRadius = computed(() => (props.productImageRadius == null ? undefined : `${Math.max(0, props.productImageRadius) * 2}rpx`));

const showBuyBtn = computed(() => {
    return props.buyBtnStyle !== 0;
});

const buyBtnClasses = computed(() => {
    if (props.buyBtnStyle && props.buyBtnStyle >= 1 && props.buyBtnStyle <= 8) {
        return [`buy_btn--style${props.buyBtnStyle}`];
    }
    return [];
});

const itemClasses = computed(() => {
    const cls: string[] = [];
    if (props.type === "exchange") cls.push("item-li--exchange");
    if (props.textAlign === 2) cls.push("item-li--align-2");
    if (props.textWeight === 1) cls.push("item-li--weight-1");
    const row = props.goodsNameRow ?? props.productTitleLine;
    if (row === 1) cls.push("item-li--name-row-1");
    return cls;
});

const detailStyle = computed(() => {
    const row = props.goodsNameRow ?? props.productTitleLine;
    if (row === 1 || row === 2) {
        return { "-webkit-line-clamp": row, "line-clamp": row } as Record<string, string | number>;
    }
    return {} as Record<string, string | number>;
});

const currentStyle = computed(() => {
    const productId = props.item?.productId;
    return productId ? (props.styleMap[productId] ?? null) : null;
});

// 批发卡片判定：firstWholesalePrice 存在即走批发态（仅改价格展示为「批发价…起」，购买交互由父级处理）
const { isWholesaleCard } = useProductCardBuy();
const isWholesaleCardComputed = computed(() => isWholesaleCard(props.item));

const emit = defineEmits<{
    (event: "click-item", item: any): void;
    (event: "click-buy", item: any): void;
    (event: "click-shop", shopId: number): void;
}>();

// 计算是否显示售罄状态
const showOutsale = computed(() => {
    return shouldShowSoldOut(props.item, { isSubCard: props.isSubCard });
});

// 计算促销信息
const currentPromotionInfo = computed(() => {
    const promotionItem = props.promotionList[props.item.productId];
    const activityInfo = promotionItem?.activityInfo || [];
    if (props.showCoupon === false) {
        return activityInfo.filter((item: any) => Number(item?.type) !== 2);
    }
    return activityInfo;
});

const hasPromotionInfo = computed(() => {
    return Object.keys(props.promotionList).length > 0 && currentPromotionInfo.value.length > 0;
});

const hasGrouponActivity = computed(() => {
    // 判断当前的活动是否有拼团活动
    const promotionItem = currentPromotionInfo.value.find((item: any) => item.type === 7);
    if (!promotionItem) {
        return false;
    }
    // 判断当前时间是否在活动时间内
    if (promotionItem.startTime > dayjs().format("YYYY-MM-DD HH:mm:ss") || promotionItem.endTime < dayjs().format("YYYY-MM-DD HH:mm:ss")) {
        return false;
    }
    return true;
});

const toPage = (item: any) => {
    emit("click-item", item);
};

const handleBuy = (item: any) => {
    emit("click-buy", item);
};

const handleToShop = (shopId: number) => {
    emit("click-shop", shopId);
};
</script>

<style lang="scss" scoped>
/* --pi-* 由装修配置下发（见 decorateVars），未配置时 fallback 即原有硬编码值 */
.item-li {
    width: 100%;
    margin-bottom: 15rpx;
    background: var(--pi-item-bg, #fff);
    border-radius: var(--pi-image-radius, 12rpx);
    overflow: hidden;
    padding-bottom: 10rpx;

    &--exchange {
        border-radius: 16rpx !important;
    }
}

.photo {
    text-align: center;
    position: relative;
    border-radius: var(--pi-image-radius, 12rpx);
    overflow: hidden;

    image {
        width: 100%;
    }

    .product-style-border {
        position: absolute;
        left: 0;
        top: 0;
        width: 100%;
        height: 100%;
        z-index: 5;
        pointer-events: none;
    }
}

.info {
    padding: 10rpx 16rpx 0;
    box-sizing: border-box;
}

.detail {
    margin: 0 0 10rpx;
    line-height: 40rpx;
    max-height: 80rpx;
    overflow: hidden;
    font-size: 26rpx;
    font-weight: bold;
    color: var(--pi-title-color, #2a3145);
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    line-clamp: 2;
    -webkit-box-orient: vertical;

    .groupon-tag {
        box-sizing: border-box;
        padding: 0 4rpx;
        border-radius: 4rpx;
        border: 1px solid var(--primary-light-3);
        font-weight: 500;
        font-size: 20rpx;
        line-height: 20rpx;
        color: var(--primary-light-3);
        margin-right: 4rpx;
        vertical-align: middle;
    }

    .cycleBuyTag {
        display: inline-block;
        color: var(--pi-price-color, var(--general));
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
}

.presale-box {
    .presale-tag {
        color: var(--general);
    }

    .separator {
        color: #333;
        margin: 0 8rpx;
        font-size: 22rpx;
        position: relative;
        top: -2rpx;
    }
}

.activity-box {
    height: 55rpx;
}

.shop {
    height: 50rpx;
    font-size: 26rpx;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 16rpx 10rpx;

    .shop-name {
        width: 55%;
        overflow: hidden;
        white-space: nowrap;
        text-overflow: ellipsis;
    }

    .shop-btn {
        display: flex;
        align-items: center;
        justify-content: center;

        .icon-youjiantou {
            font-size: 32rpx;
        }
    }
}

.action {
    padding: 0 0 10rpx;
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.buy_btn {
    color: var(--pi-theme, var(--general));
    display: flex;
    align-items: center;
    justify-content: center;

    .buy_btn_icon {
        font-size: 42rpx;
    }

    .buy_btn_text {
        font-size: 24rpx;
        white-space: nowrap;
    }

    // 装修按钮 1-4：单图标，与原有视觉接近
    &.buy_btn--style1,
    &.buy_btn--style2,
    &.buy_btn--style3,
    &.buy_btn--style4 {
        .buy_btn_icon {
            color: var(--pi-theme, var(--general));
        }
    }

    // 装修按钮 5 / 7：直角描边
    &.buy_btn--style5,
    &.buy_btn--style7 {
        height: 44rpx;
        padding: 0 16rpx;
        box-sizing: border-box;
        border: 2rpx solid var(--pi-theme, var(--general));
        border-radius: 4rpx;
        background-color: transparent;
        color: var(--pi-theme, var(--general));

        .buy_btn_text {
            color: var(--pi-theme, var(--general));
        }
    }

    // 装修按钮 6 / 8：圆角实心
    &.buy_btn--style6,
    &.buy_btn--style8 {
        height: 44rpx;
        padding: 0 20rpx;
        border-radius: 44rpx;
        background-color: var(--pi-theme, var(--general));
        color: #fff;

        .buy_btn_text {
            color: #fff;
        }
    }
}

// ===== 装修配置：文本对齐 / 文字粗细 / 名称行数 =====
.item-li--align-2 {
    .detail {
        text-align: center;
    }

    .action {
        justify-content: center;
        column-gap: 16rpx;
    }
}

.item-li--weight-1 .detail {
    font-weight: normal;
}

.item-li--name-row-1 .detail {
    max-height: 40rpx;
    -webkit-line-clamp: 1;
    line-clamp: 1;
}

.product-status-box {
    width: 100%;
    height: 100%;
    position: absolute;
    left: 0;
    top: 0;
    z-index: 10;
    display: flex;
    align-items: center;
    justify-content: center;

    .outsale {
        width: 180rpx;
        height: 180rpx;
        line-height: 180rpx;
        background: rgba(0, 0, 0, 0.4);
        font-size: 38rpx;
        color: #ffffff;
        letter-spacing: 0;
        border-radius: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
    }
}

.pricenum {
    color: var(--pi-price-color, var(--general));
    height: inherit;

    .exchange-box {
        display: flex;
        align-items: center;

        .exchange-img {
            width: 30rpx;
            height: 30rpx;
            padding-right: 5rpx;
        }

        .exchange-icon {
            padding: 0 5rpx;
        }

        .exchange-text {
            font-size: 28rpx;
        }
    }
}
</style>
