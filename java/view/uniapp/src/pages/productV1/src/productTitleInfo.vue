<template>
    <view class="product-title-info" :class="{ 'has-activity-bg': isNormalWithActivityBg }">
        <template v-if="isSeckill && !isPresale">
            <productSeckillTitle
                :product-info="productInfo"
                :product-origin-price="productOriginPrice!"
                :product-price="productPrice"
                :seckill-value="seckillValue"
                :activity-bg-image="activityBgImage"
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
            />
        </template>
        <template v-if="isPresale">
            <productPresaleTitle
                :product-info="productInfo"
                :product-price="productPrice"
                :presale-info="presaleInfo"
                :sold-count="soldCount"
                :activity-bg-image="activityBgImage"
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
            <view class="title-info-top">
                <view class="title-top-price">
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
                            <format-price :is-bottom="false" :price-data="normalStrikePrice" :font-style="{ 'text-decoration': 'line-through' }" />
                        </view>
                    </template>
                </view>
            </view>
        </template>

        <productShare v-model="showShare" :product-id="Number(productId)" :product-info="productInfo" :product-price="productPrice" />
        <view class="product-name-card" :class="{ 'has-activity': isActivityProduct }">
            <template v-if="promotionList.length > 0">
                <view class="activity-box" @click="handleCoupon">
                    <view class="flex">
                        <template v-for="item in promotionList.length > 2 ? promotionList.slice(0, 2) : promotionList" :key="item.promotionId">
                            <activityCard :activity-info="item" card-type="detail" />
                        </template>
                    </view>
                    <view class="activity-btn"> {{ $t("领券") }}<text class="iconfont-h5 icon-youjiantou" /> </view>
                </view>
            </template>
            <view class="product-name-text" :style="nameCollapsedStyle" @click="showNameExpandBtn && (isNameExpanded = !isNameExpanded)">
                <view class="product-name-inner">
                    {{ productInfo.productName }}
                </view>
                <view v-if="showNameExpandBtn && !isNameExpanded" class="product-name-btn product-name-expand">
                    <text class="iconfont-h5 icon-a-gengduo20" />
                </view>
                <view v-if="showNameExpandBtn && isNameExpanded" class="product-name-btn product-name-collapse">
                    <text class="iconfont-h5 icon-a-gengduo20 iconTop" />
                </view>
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
import { useUserStore } from "@/store/user";
import productGrouponTitle from "./productGrouponTitle.vue";
import productSeckillTitle from "./productSeckillTitle.vue";
import productDiscountTitle from "./productDiscountTitle.vue";
import productPresaleTitle from "./productPresaleTitle.vue";
import { isShowB2bPriceText } from "@/utils/b2bPrice";
import { useConfigStore } from "@/store/config";

const configStore = useConfigStore();
const userStore = useUserStore();
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
    soldCount: {
        type: Number,
        default: 0
    }
});

const emit = defineEmits(["update:showCoupon"]);

const { t } = useI18n();

const showB2bText = computed(() => isShowB2bPriceText());

const isNameExpanded = ref(false);
const showNameExpandBtn = ref(false);

const doMeasureName = (retries = 3) => {
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
    if (!userStore.token) {
        uni.navigateTo({
            url: "/pages/login/index"
        });
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
        background: #fff;
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
    }

    .product-name-card {
        background: #fff;
        padding: 24rpx 30rpx;
        position: relative;

        &.has-activity {
            border-radius: 24rpx;
            margin-top: -40rpx;
            z-index: 20;
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
    background: #fff;
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
