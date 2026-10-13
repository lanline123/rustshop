<template>
    <view>
        <tig-layout :title="$t('TA送你一张礼品卡')">
            <template v-if="!loading && !loadFailed">
                <view class="bg" />
                <view class="gift-card-container">
                    <view class="card-info">
                        <view class="card-wrapper">
                            <giftCard :card-info="giftCardInfo" :show-actions="isClaimGiftCardSuccess" />
                        </view>
                    </view>
                    <view class="use-step group">
                        <view class="use-step-header group-header">
                            <view class="line left" />
                            <view class="group-title">{{ $t("使用方法") }}</view>
                            <view class="line right" />
                        </view>
                        <view class="use-step-content">
                            <view class="use-step-item step-1">
                                <view class="icon">
                                    <img class="img" :src="staticResource('giftCard/cart.png')" />
                                </view>
                                <view class="text">{{ $t("礼品卡下单") }}</view>
                            </view>
                            <view class="next-icon">
                                <view class="iconfont-h5 icon-a-gengduo20" />
                            </view>
                            <view class="use-step-item">
                                <view class="icon">
                                    <img class="img" :src="staticResource('giftCard/exchange.png')" />
                                </view>
                                <view class="text">{{ $t("礼品卡兑换") }}</view>
                            </view>
                            <view class="next-icon">
                                <view class="iconfont-h5 icon-a-gengduo20" />
                            </view>
                            <view class="use-step-item">
                                <view class="icon">
                                    <img class="img" :src="staticResource('giftCard/card.png')" />
                                </view>
                                <view class="text">{{ $t("输入卡号卡密") }}</view>
                            </view>
                            <view class="next-icon">
                                <view class="iconfont-h5 icon-a-gengduo20" />
                            </view>
                            <view class="use-step-item">
                                <view class="icon">
                                    <img class="img" :src="staticResource('giftCard/product.png')" />
                                </view>
                                <view class="text">{{ $t("立即兑换") }}</view>
                            </view>
                        </view>
                    </view>
                    <view v-if="giftCardInfo?.giftCardProducts?.length" class="products group">
                        <view class="group-header">
                            <view class="line left" />
                            <view class="group-title">{{ $t("兑换商品") }}</view>
                            <view class="line right" />
                        </view>
                        <view class="group-sub-header">{{ $t("该兑换卡可兑换商品") }}</view>
                        <view class="product-box">
                            <view v-for="(item, index) in giftCardInfo.giftCardProducts" :key="index">
                                <productItem :product="item" :product-style="styleMap[item.productId]" />
                            </view>
                        </view>
                    </view>
                    <view class="exchange-flow group">
                        <view class="group-header">
                            <view class="line left" />
                            <view class="group-title">{{ $t("兑换流程") }}</view>
                            <view class="line right" />
                        </view>
                        <view class="group-sub-header">{{ $t("该兑换卡可兑换商品") }}</view>
                        <view class="flows">
                            <view class="flow">
                                <view class="flow-title">
                                    <view class="flow-no">1</view>
                                    <view class="flow-title-text">{{ $t("购买礼品卡") }}</view>
                                </view>
                                <view class="flow-content">
                                    <view class="flow-tips">
                                        {{ $t("在商城选择所需购买的礼品卡，点击立即购买，并支付订单。 支付成功后，礼品卡需在礼品卡兑换中兑换才可使用") }}
                                    </view>
                                </view>
                            </view>
                            <view class="flow">
                                <view class="flow-title">
                                    <view class="flow-no">2</view>
                                    <view class="flow-title-text">{{ $t("礼品卡兑换") }}</view>
                                </view>
                                <view class="flow-content">
                                    <view class="flow-tips">
                                        {{ $t("在订单底部点击礼品卡兑换按钮进行兑换") }}
                                    </view>
                                </view>
                            </view>
                            <view class="flow">
                                <view class="flow-title">
                                    <view class="flow-no">3</view>
                                    <view class="flow-title-text">{{ $t("使用礼品卡") }}</view>
                                </view>
                                <view class="flow-content">
                                    <view class="flow-tips">
                                        {{ $t("进入兑换页面输入礼品卡的卡号卡密进行兑换") }}
                                    </view>
                                </view>
                            </view>
                            <view class="flow">
                                <view class="flow-title">
                                    <view class="flow-no">4</view>
                                    <view class="flow-title-text">{{ $t("礼品卡支付") }}</view>
                                </view>
                                <view class="flow-content">
                                    <view class="flow-tips">
                                        {{ $t("点击去兑换按钮，在订单提交页面进行提交") }}
                                    </view>
                                </view>
                            </view>
                        </view>
                    </view>
                    <tig-fixed-placeholder height="104rpx" background-color="#fff">
                        <view class="order-info-btn">
                            <view class="order-info-btn-box">
                                <tig-button class="btn" :disabled="!isClaimGiftCardSuccess || giftCardInfo.status != 0" @click="handleExchange">
                                    {{ $t("去兑换") }}
                                </tig-button>
                            </view>
                        </view>
                    </tig-fixed-placeholder>
                </view>
            </template>
            <template v-if="loadFailed">
                <view class="error-container">
                    <error :message="errorMessage" />
                </view>
            </template>
        </tig-layout>
    </view>
</template>
<script setup lang="ts">
import { computed, ref } from "vue";
import { useProductStyle } from "@/hooks/useProductStyle";
import { onLoad } from "@dcloudio/uni-app";
import { useUserStore } from "@/store/user";
import { redirect } from "@/utils";
import giftCard from "./src/giftCard.vue";
import productItem from "./src/productItem.vue";
import { handleLogin } from "@/utils/handleLogin";
import { getGiftCardDetail, giftCardClaim } from "@/api/giftCard/giftCard";
import { staticResource } from "@/utils/index";
import error from "./src/error.vue";
import type { GiftCardInfo } from "@/types/giftCard/giftCard";
import { useI18n } from "vue-i18n";

const { t } = useI18n();
const giftCardNumber = ref("");
const giftCardSn = ref("");
const userStore = useUserStore();
const hasToken = computed(() => {
    return userStore.token || uni.getStorageSync("token");
});
const loadFailed = ref(false);
const loading = ref(true);
const errorMessage = ref(t("无效的礼品卡"));

const handleExchange = async () => {
    redirect({
        url: "/pages/giftCard/exchange"
    });
};
const giftCardInfo = ref<GiftCardInfo>({
    giftCardsId: 71,
    cardGroupsId: 5,
    cardNumber: "",
    cardPassword: "",
    cardGroupName: "",
    shopId: 0,
    shopTitle: "",
    giftCardSn: ""
});
const isClaimGiftCardSuccess = ref(false);

const giftCardProductList = computed(() => giftCardInfo.value?.giftCardProducts || []);
const { styleMap } = useProductStyle(giftCardProductList);

const _getGiftCardDetail = async () => {
    const result = await getGiftCardDetail(giftCardNumber.value, giftCardSn.value);
    giftCardInfo.value = result;
};

/**
 * 领取礼品卡
 */
const _claimGiftCard = async () => {
    try {
        await giftCardClaim({ cardNum: giftCardNumber.value, giftCardSn: giftCardSn.value });
        isClaimGiftCardSuccess.value = true;
    } catch (error: any) {
        uni.showToast({
            title: error.message,
            icon: "none"
        });
        isClaimGiftCardSuccess.value = false;
    }
};

const getDetail = async () => {
    try {
        // 领取礼品卡
        await _claimGiftCard();
        // 拿到礼品卡信息
        await _getGiftCardDetail();
    } catch (error: any) {
        errorMessage.value = error.message;
        loadFailed.value = true;
    } finally {
        loading.value = false;
    }
};

// 页面加载
onLoad((option) => {
    // 需要登录
    if (!hasToken.value) {
        handleLogin();
        return;
    }
    if (option?.giftCardSn) {
        // giftCardsId.value = Number(option.cardId);
        giftCardNumber.value = option.cardNum;
        giftCardSn.value = option.giftCardSn;
        getDetail();
    } else {
        loadFailed.value = true;
    }
});
</script>
<style scoped lang="scss">
.bg {
    position: fixed;
    inset: 0;
    background: #fbeede;
}
.gift-card-container {
    padding: 0 30rpx 100rpx;
    background-color: #fbeede;
    position: relative;
    z-index: 1;
}

.card-info {
    padding: 90rpx 0;
    display: flex;
    justify-content: center;
    border-bottom: 1rpx solid #a9781b;
    .card-wrapper {
        width: 546rpx;
        height: 330rpx;
        .gift-card-box {
            box-shadow: 0rpx 12rpx 9rpx 0rpx rgba(0, 14, 43, 0.17);
        }
    }
}

.group {
    .group-header {
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 20rpx;
        .group-title {
            font-size: 40rpx;
            color: #a9781b;
        }
        .line {
            height: 2rpx;
            width: 100rpx;
            &.left {
                background: linear-gradient(90deg, #fbeede, #bd983d);
            }
            &.right {
                background: linear-gradient(90deg, #bd983d, #fbeede);
            }
        }
    }
    .group-sub-header {
        color: #a9781b;
        font-size: 24rpx;
        padding-top: 22rpx;
        text-align: center;
    }
}

.use-step {
    padding-top: 70rpx;
    .use-step-content {
        padding-top: 40rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        .use-step-item {
            display: flex;
            // flex-shrink: 0;
            // flex-grow: 0;
            box-sizing: border-box;
            align-items: center;
            gap: 11rpx;
            border: 1rpx solid #a9781b;
            background-color: #fff;
            padding: 13rpx 9rpx 13rpx;
            border-radius: 12rpx;
            width: 150rpx;
            height: 86rpx;
            .icon {
                width: 34rpx;
                height: 34rpx;
                flex-shrink: 0;
                .img {
                    width: 100%;
                    height: 100%;
                }
            }
            .text {
                font-size: 24rpx;
                color: #a9781b;
            }
        }
        .next-icon {
            .icon-a-gengduo20 {
                font-size: 24rpx;
                padding: 0 9rpx;
                color: #a9781b;
            }
        }
    }
}

.products {
    margin-top: 80rpx;
    .product-box {
        border: 1rpx solid #a9781b;
        border-radius: 12rpx;
        padding: 25rpx 16rpx;
        background: #fff;
        margin-top: 55rpx;
        display: flex;
        flex-direction: column;
        row-gap: 30rpx;
    }
}
.exchange-flow {
    margin-top: 90rpx;
    .flows {
        margin-top: 50rpx;
        display: flex;
        flex-direction: column;
        gap: 32rpx;

        .flow {
            padding: 24rpx 28rpx;
            border-radius: 16rpx;
        }

        .flow-title {
            display: flex;
            align-items: baseline;
            gap: 15rpx;
            .flow-no {
                font-size: 71rpx;
                width: 30rpx;
                color: #a9781b;
                font-weight: 400;
                font-family: DengXian;
                // 倾斜
                transform: skewX(-6deg);
            }
            .flow-title-text {
                color: #000000;
                font-size: 24rpx;
                font-weight: 400;
            }
        }
        .flow-content {
            padding-left: 44rpx;
        }
        .flow-tips {
            color: #6a6a6a;
            font-size: 24rpx;
            font-weight: 400;
            line-height: 36rpx;
            margin-top: 10rpx;
        }
    }
}
.order-info-btn {
    height: 100%;
    width: 100%;
    display: flex;
    justify-content: flex-end;
    padding: 11rpx 25rpx;
    .order-info-btn-box {
        display: flex;
        align-items: center;
        flex: 1;
        .btn {
            margin-right: 10rpx;
            flex: 1;
            height: 100%;
            &:last-child {
                margin-right: 0;
            }
        }
    }
}
.error-container {
    position: fixed;
    inset: 0;
    top: var(--nav-height);
    background: #f5f5f5;
}

:deep(.fixed-placeholder) {
    .content {
        transform: translateY(4rpx);
    }
}
</style>
