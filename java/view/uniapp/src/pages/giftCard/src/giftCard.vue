<template>
    <view class="gift-card-box" :class="`size-${props.cardSize}`">
        <view class="gift-card">
            <view class="gift-shop-info">
                <!-- <image v-if="cardInfo.shopLogo" class="shop-img" :src="imageFormat(cardInfo.shopLogo || '')" mode="aspectFill" />
                <view v-else class="shop-img">{{ cardInfo.shopTitle.at(0) }}</view> -->
                <view class="shop-title">{{ cardInfo.shopTitle }}</view>
            </view>
            <view class="gift-card-no">
                <view class="gift-card-label">{{ $t("卡号") }}</view>
                <view v-if="!showGiftCardNo" class="gift-card-no-box">
                    <!-- <text
                        v-for="(value, index) in formatCardNo(cardInfo.cardNumber, 4, 2)"
                        :key="index"
                        class="card-no-item"
                        :class="{ 'hide-info': value?.isHide }"
                        >{{ value.text }}
                    </text> -->
                    <text class="card-no-item">{{ hideCardNumber }}</text>
                </view>
                <view v-else class="gift-card-no-box">
                    <text class="card-no-item">{{ cardNumber }}</text>
                </view>
                <view v-if="showActions" class="copy-btn" @click="handleCardNumberCopy">{{ $t("复制") }}</view>
            </view>
            <view class="gift-card-pwd">
                <view class="gift-card-label">{{ $t("密码") }}</view>
                <view v-if="!showGiftCardNo" class="card-no-item">
                    <text>{{ hideCardPassword }}</text>
                </view>
                <view v-else class="card-no-item">
                    <text>{{ cardPassword }}</text>
                </view>
                <view v-if="showActions" class="copy-btn" @click="handlePasswordCopy">{{ $t("复制") }}</view>
            </view>
            <view class="gift-card-info">
                <view class="gift-card-name">{{ cardInfo.cardGroupName }}</view>
                <view class="gift-card-date">
                    <view v-if="cardInfo.expiredTime == 0" class="gift-card-date-text">
                        {{ $t("长期有效") }}
                    </view>
                    <view v-else-if="cardInfo.expiredTime && cardInfo.expiredTime != 0" class="gift-card-date-text"
                        >{{ $t("有效期至") }} {{ formatTimestamp(cardInfo.expiredTime * 1000) }}</view
                    >
                </view>
            </view>
            <view v-if="showActions" class="gift-card-actions-box">
                <view class="gift-card-action-item" @click="handleCopyAll">
                    <view class="iconfont-h5 icon-fuzhi1" />
                    {{ $t("复制券码") }}
                </view>
                <view class="gift-card-action-divider" />
                <view class="gift-card-action-item" @click="handleShowGiftCardNo">
                    <view class="iconfont-h5" :class="showGiftCardNo ? 'icon-xianshi' : 'icon-buxianshi'" />
                    <text v-if="!showGiftCardNo">{{ $t("显示券码") }}</text>
                    <text v-else>{{ $t("隐藏券码") }}</text>
                </view>
            </view>
        </view>
        <view class="gift-card-status">
            <view class="gift-card-status-item">
                {{ displayStatus }}
            </view>
        </view>
    </view>
</template>

<script lang="ts" setup>
import { computed, type PropType, ref } from "vue";
import { copy, staticResource } from "@/utils";
import { useI18n } from "vue-i18n";
import type { GiftCardInfo } from "@/types/giftCard/giftCard";
import { getGiftCardPassword } from "@/api/giftCard/giftCard";
import { copyGiftCard } from "@/utils/giftCard";
import dayjs from "dayjs";

const props = defineProps({
    cardSize: {
        type: String as PropType<"large" | "small">,
        default: "small"
    },
    cardInfo: {
        type: Object as PropType<GiftCardInfo>,
        default: (): GiftCardInfo => ({}) as GiftCardInfo
    },
    showActions: {
        type: Boolean,
        default: true
    }
});

const cardBg = ref("");
cardBg.value = staticResource("giftCard/card_bg.png");

const { t } = useI18n();

const showGiftCardNo = ref(false);
const isGetCardPassword = ref(false);
const hideCardNumber = ref(props.cardInfo.cardNumber);
const hideCardPassword = ref(props.cardInfo.cardPassword);
const cardNumber = ref("");
const cardPassword = ref("");
const cardBackgroundImg = computed(() => {
    return `url(${props.cardInfo.imageUrl || cardBg.value}) center no-repeat`;
});
const statusMap: Record<number, string> = {
    0: t("待兑换"),
    1: t("已兑换"),
    2: t("已过期")
};

// 判断礼品卡是否已过期
const isExpired = computed(() => {
    const { expiredTime } = props.cardInfo;
    // 如果过期时间为0或不存在，表示长期有效，不会过期
    if (!expiredTime || expiredTime == 0) {
        return false;
    }
    // 将秒级时间戳转换为毫秒级，然后与当前时间比较
    const expiredTimestamp = expiredTime * 1000;
    const currentTimestamp = Date.now();
    return currentTimestamp > expiredTimestamp;
});

// 计算显示的状态
const displayStatus = computed(() => {
    const status = props.cardInfo.status || 0;
    // 如果是已兑换，显示已兑换
    if (status === 1) {
        return statusMap[1]; // 已兑换
    }
    // 如果是未兑换，根据过期时间判断
    if (isExpired.value) {
        return statusMap[2]; // 已过期
    }
    return statusMap[0]; // 待兑换
});

const _getGiftCardPassword = async () => {
    try {
        const res = await getGiftCardPassword(props.cardInfo.cardNumber, props.cardInfo.giftCardSn);
        isGetCardPassword.value = true;
        cardNumber.value = res.cardNumber;
        cardPassword.value = res.cardPassword;
    } catch (error: any) {
        console.error(error);
    }
};

const formatTimestamp = (timestamp: string | number) => {
    // 使用dayjs格式化时间
    return dayjs(timestamp).format("YYYY-MM-DD");
};

const handleShowGiftCardNo = async () => {
    if (!isGetCardPassword.value) {
        await _getGiftCardPassword();
    }
    if (!isGetCardPassword.value) {
        return;
    }
    showGiftCardNo.value = !showGiftCardNo.value;
};

const handleCardNumberCopy = async () => {
    if (!isGetCardPassword.value) {
        await _getGiftCardPassword();
    }
    copy(cardNumber.value, () => {
        uni.showToast({
            title: t("复制成功"),
            icon: "none"
        });
    });
};

const handlePasswordCopy = async () => {
    if (!isGetCardPassword.value) {
        await _getGiftCardPassword();
    }
    copy(cardPassword.value, () => {
        uni.showToast({
            title: t("复制成功"),
            icon: "none"
        });
    });
};

// 一键复制卡号和卡密
const handleCopyAll = async () => {
    if (!isGetCardPassword.value) {
        await _getGiftCardPassword();
    }
    if (!isGetCardPassword.value) {
        return;
    }
    const success = await copyGiftCard(cardNumber.value, cardPassword.value);
    if (success) {
        uni.showToast({
            title: t("复制成功"),
            icon: "none"
        });
    }
};
</script>

<style lang="scss" scoped>
.gift-card-box {
    box-sizing: border-box;
    width: 100%;
    height: 100%;
    border-radius: 20rpx;
    padding: 0 20rpx;
    overflow: hidden;
    position: relative;
    &::before {
        content: "";
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: v-bind(cardBackgroundImg);
        background-size: cover;
        border-radius: 22rpx;
        z-index: 1;
    }
    .gift-card {
        position: relative;
        z-index: 2;
        box-sizing: border-box;
        color: #fff;
        .gift-shop-info {
            padding: 20rpx 0;
            display: flex;
            align-items: center;
            .shop-img {
                width: 60rpx;
                height: 60rpx;
                border-radius: 50%;
                overflow: hidden;
                display: flex;
                justify-content: center;
                align-items: center;
                background-color: #fff;
                color: #000;
                font-weight: bold;
            }
            .shop-title {
                // padding-left: 18rpx;
                font-size: 32rpx;
                // line-height: 32rpx;
                display: -webkit-box;
                -webkit-line-clamp: 1;
                -webkit-box-orient: vertical;
                overflow: hidden;
            }
        }
        .gift-card-label {
            white-space: nowrap;
            font-size: 20rpx;
            line-height: 20rpx;
            // opacity: 0.8;
        }
        .gift-card-no {
            display: flex;
            align-items: center;
            gap: 18rpx;
            margin-top: 4rpx;
            height: 40rpx;
            .gift-card-no-box {
                display: flex;
                align-items: center;
                gap: 30rpx;
                line-height: 30rpx;
                .card-no-item {
                    display: flex;
                    align-items: center;
                    font-size: 30rpx;
                    line-height: 30rpx;
                }
            }
        }
        .gift-card-pwd {
            display: flex;
            align-items: center;
            gap: 18rpx;
            margin-top: 12rpx;
            overflow: hidden;
            height: 40rpx;
        }
        .gift-card-info {
            margin-top: 16rpx;
            .gift-card-name {
                font-size: 26rpx;
                font-size: 26rpx;
                display: -webkit-box;
                -webkit-line-clamp: 1;
                -webkit-box-orient: vertical;
                overflow: hidden;
                max-width: 70%;
            }
            .gift-card-date {
                display: flex;
                align-items: flex-end;
                padding-top: 12rpx;
                .gift-card-date-text {
                    font-size: 20rpx;
                    line-height: 20rpx;
                    opacity: 0.51;
                }
            }
        }
        .gift-card-actions-box {
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 0;
            margin: 10rpx -20rpx 0;
            background-color: rgba(0, 0, 0, 0.65);
            border-radius: 20rpx;
            padding: 10rpx 0;
            .gift-card-action-item {
                flex: 1;
                display: flex;
                align-items: center;
                justify-content: center;
                gap: 8rpx;
                font-size: 24rpx;
                line-height: 24rpx;
                cursor: pointer;
                .iconfont-h5 {
                    padding-top: 3.2rpx;
                    font-size: 28rpx;
                }
                .icon-fuzhi1 {
                    font-size: 24rpx;
                    padding-top: 0;
                }
            }
            .gift-card-action-divider {
                width: 1rpx;
                height: 24rpx;
                background-color: rgba(255, 255, 255, 0.3);
            }
        }
        .copy-btn {
            font-size: 20rpx;
            line-height: 20rpx;
            background-color: #0000004d;
            border: 1px solid rgb(255 255 255 / 20%);
            border-radius: 20rpx;
            padding: 5rpx 14rpx;
            white-space: nowrap;
            cursor: pointer;
        }
        .hide-info {
            padding-top: 8rpx;
        }
    }
    .gift-card-status {
        position: absolute;
        right: 0;
        top: 0;
        overflow: hidden;
        z-index: 2;
        .gift-card-status-item {
            font-size: 24rpx;
            line-height: 24rpx;
            background-color: #f8e0c8;
            color: #996500;
            padding: 10rpx 20rpx;
            border-left: 2rpx solid #fff;
            border-bottom: 2rpx solid #fff;
            border-bottom-left-radius: 20rpx;
        }
    }
    &.size-small {
        border-bottom-left-radius: 22rpx;
        border-bottom-right-radius: 22rpx;
    }
    &.size-large {
        padding: 0 25rpx;
        .gift-shop-info {
            padding: 24rpx 0;
        }
        .gift-card-label {
            font-size: 24rpx;
            line-height: 24rpx;
        }
        .gift-card-no {
            margin-top: 18rpx;
            gap: 24rpx;
            .gift-card-no-box {
                gap: 32rpx;
                .card-no-item {
                    font-size: 36rpx;
                    line-height: 36rpx;
                    font-weight: bold;
                }
            }
            .hide-info {
                padding-top: 12rpx;
            }
        }
        .gift-card-pwd {
            gap: 24rpx;
            margin-top: 22rpx;
            .card-no-item {
                font-size: 28rpx;
                line-height: 28rpx;
                // font-weight: bold;
            }
        }
        .gift-card-info {
            margin-top: 30rpx;
            .gift-card-name {
                font-size: 30rpx;
                line-height: 30rpx;
                display: -webkit-box;
                -webkit-line-clamp: 1;
                -webkit-box-orient: vertical;
                overflow: hidden;
                max-width: 70%;
            }
            .gift-card-date {
                padding-top: 20rpx;
                .gift-card-date-text {
                    font-size: 24rpx;
                    line-height: 24rpx;
                }
            }
        }
        .gift-card-actions-box {
            padding: 12rpx 0;
            margin: 10rpx -25rpx 0;
            .gift-card-action-item {
                font-size: 28rpx;
                line-height: 28rpx;
                .iconfont-h5 {
                    padding-top: 3.5rpx;
                    font-size: 28rpx;
                }
                .icon-fuzhi1 {
                    font-size: 24rpx;
                }
            }
            .gift-card-action-divider {
                height: 28rpx;
            }
        }
    }

    // &.size-small {
    //     .gift-shop-info {
    //         padding: 10rpx 0;
    //     }
    //     .gift-card-no {
    //         margin-top: 5rpx;
    //     }
    //     .gift-card-pwd {
    //         margin-top: 10rpx;
    //     }
    //     .gift-card-info {
    //         padding-top: 15rpx;
    //     }
    // }
}
</style>
