<template>
    <view class="presale-box">
        <!-- 定金预售步骤 -->
        <template v-if="presaleInfo?.saleMode === 2">
            <view class="presale-flow">
                <view class="flow-item" :class="{ active: presaleInfo?.presaleStatus === 0 }">
                    <view class="flow-left" :class="{ active: presaleInfo?.presaleStatus && presaleInfo?.presaleStatus > 0 }">
                        <view class="dot" />
                        <view class="line" />
                    </view>
                    <view class="flow-right">
                        <view class="flow-header">
                            <view class="title-box">
                                <text class="title">{{ $t("定金") }}</text>
                                <format-price class="price" :price-data="presaleInfo?.depositAmount" />
                            </view>
                            <text class="title">{{ $t("预售商品，定金不退") }}</text>
                        </view>
                    </view>
                </view>
                <view class="flow-item" :class="{ active: presaleInfo?.presaleStatus === 1 }">
                    <view class="flow-left" :class="{ active: presaleInfo?.presaleStatus && presaleInfo?.presaleStatus >= 2 }">
                        <view class="dot" />
                        <view class="line" />
                    </view>
                    <view class="flow-right">
                        <view class="flow-header">
                            <text class="title">
                                {{ $t("尾款") }}
                                <text class="price">
                                    <format-price class="price" :price-data="presaleInfo?.finalPaymentAmount" />
                                </text>
                            </text>
                            <text>{{ PaymentTimeText }}</text>
                        </view>
                    </view>
                </view>
                <view class="flow-item" :class="{ active: presaleInfo?.presaleStatus === 2 }">
                    <view class="flow-left">
                        <view class="dot" />
                    </view>
                    <view class="flow-right">
                        <view class="flow-header">
                            <text class="title">{{ depositPresaleStepThreeTitle }}</text>
                            <text v-if="depositPresaleStepThreeDate" class="text-desc">{{ depositPresaleStepThreeDate }}</text>
                        </view>
                    </view>
                </view>
            </view>
        </template>
        <!-- 全款预售步骤 -->
        <template v-else-if="presaleInfo?.saleMode === 1">
            <view class="presale-flow">
                <view class="flow-item" :class="{ active: orderStatus === 0 }">
                    <view class="flow-left" :class="{ active: orderStatus !== undefined && orderStatus > 0 && orderStatus !== 3 }">
                        <view class="dot" />
                        <view class="line" />
                    </view>
                    <view class="flow-right">
                        <view class="flow-header">
                            <text class="title">{{ $t("全款预售") }}</text>
                            <format-price class="price" :price-data="total.productAmount" />
                        </view>
                    </view>
                </view>
                <view class="flow-item" :class="{ active: orderStatus !== undefined && orderStatus >= 1 && orderStatus !== 3 }">
                    <view class="flow-left">
                        <view class="dot" />
                    </view>
                    <view class="flow-right">
                        <view class="flow-header">
                            <text class="title">{{ fullPresaleStepTwoTitle }}</text>
                            <text v-if="fullPresaleStepTwoDate" class="text-desc">{{ fullPresaleStepTwoDate }}</text>
                        </view>
                    </view>
                </view>
            </view>
        </template>
    </view>
</template>

<script setup lang="ts">
/**
 * @description 预售订单流程卡片组件（订单详情页）
 * - 定金预售：定金 → 尾款 → 发货
 * - 全款预售：全款 → 发货
 */
import { computed } from "vue";
import { useI18n } from "vue-i18n";

const { t } = useI18n();

interface Props {
    presaleInfo: any;
    total: any;
}

const props = defineProps<Props>();

const orderStatus = computed(() => {
    return props.total?.orderStatus ?? 0;
});

const pendingShipNodeTitle = computed(() => {
    if (props.total?.isPickup) return t("自提");
    if (Number(props.total?.shippingTypeId) === 3) return t("配送");
    return t("发货");
});

const completedShipNodeTitle = computed(() => {
    if (props.total?.isPickup || Number(props.total?.shippingTypeId) === 3) return t("已备货");
    return t("已发货");
});

const shippingActionText = computed(() => {
    if (props.total?.isPickup) return t("自提");
    if (Number(props.total?.shippingTypeId) === 3) return t("配送");
    return t("发货");
});

const fullPresaleStepTwoTitle = computed(() => {
    const status = orderStatus.value;
    if (status >= 2 && status !== 3) return completedShipNodeTitle.value;
    return pendingShipNodeTitle.value;
});

const fullPresaleStepTwoDate = computed(() => {
    const status = orderStatus.value;
    if (status >= 2 && status !== 3) {
        if (props.total?.shippingTime) return formatShippingDate(props.total.shippingTime);
        return "";
    }
    return deliverTimeText.value;
});

const PaymentTimeText = computed(() => {
    const info = props.presaleInfo;
    if (!info) return "";

    // 优先用后端文案（对齐跨境）
    const productPresale = info.productPresale || {};
    if (productPresale.finalPaymentStartTimeText) return productPresale.finalPaymentStartTimeText;

    if (Number(productPresale.finalPaymentType) === 1) {
        let str = productPresale.finalPaymentRelativeDays ? t("付定金{0}天后开始", [productPresale.finalPaymentRelativeDays]) : t("付定金后开始");
        const days = productPresale.finalPaymentDurationDays ? `${productPresale.finalPaymentDurationDays}${t("天")}` : "";
        const hours = productPresale.finalPaymentDurationHours ? `${productPresale.finalPaymentDurationHours}${t("小时")}` : "";
        if (days && hours) str += `, ${days}${hours}${t("结束")}`;
        return str;
    }
    return productPresale.finalPaymentStartTime ? `${productPresale.finalPaymentStartTime}${t("开始")}` : "";
});

const deliverTimeText = computed(() => {
    const info = props.presaleInfo;
    if (!info) return "";

    const productPresale = info.productPresale || {};
    if (productPresale.deliveryTimeText) return productPresale.deliveryTimeText;

    if (Number(productPresale.deliveryType) === 1) {
        const days = productPresale.deliveryRelativeDays ? `${productPresale.deliveryRelativeDays}${t("天")}` : "";
        const hours = productPresale.deliveryRelativeHours ? `${productPresale.deliveryRelativeHours}${t("小时")}` : "";
        return `${Number(info.saleMode) === 1 ? t("付全款") : t("付尾款")}${days}${hours}${t("后")}${shippingActionText.value}`;
    }
    return productPresale.deliveryDate ? `${productPresale.deliveryDate}${t("开始")}` : "";
});

const formatShippingDate = (dateStr: string) => {
    if (!dateStr) return "";
    const [datePart, timePart] = dateStr.split(" ");
    const [year, month, day] = datePart.split("-");
    return `${year}${t("年")}${month}${t("月")}${day}${t("日")}  ${timePart || ""}`;
};

const depositPresaleStepThreeTitle = computed(() => {
    const st = props.total?.orderStatus ?? 0;
    if (st >= 2 && st !== 3 && props.presaleInfo?.presaleStatus === 2) return completedShipNodeTitle.value;
    return pendingShipNodeTitle.value;
});

const depositPresaleStepThreeDate = computed(() => {
    const st = props.total?.orderStatus ?? 0;
    if (st >= 2 && st !== 3 && props.presaleInfo?.presaleStatus === 2) {
        if (props.total?.shippingTime) return formatShippingDate(props.total.shippingTime);
        return "";
    }
    return deliverTimeText.value;
});
</script>

<style lang="scss" scoped>
.presale-box {
    padding: 20rpx;
    border-radius: 20rpx;
    background-color: #fff;
    // margin: 20rpx 20rpx 0;
    overflow: hidden;

    .presale-flow {
        padding: 10rpx 0;
    }

    .flow-item {
        display: flex;
        position: relative;

        &.active {
            .flow-left {
                .dot {
                    background-color: var(--general);
                }
            }

            .flow-right {
                .flow-header {
                    color: #333;
                }
            }
        }

        &:last-child {
            .flow-left {
                padding-bottom: 0;
            }

            .flow-right {
                padding-bottom: 0;
            }
        }

        .flow-left {
            display: flex;
            flex-direction: column;
            align-items: center;
            margin-right: 20rpx;
            width: 20rpx;
            flex-shrink: 0;
            position: relative;
            padding-bottom: 30rpx;

            &.active {
                .dot {
                    background-color: var(--general);
                }

                .line {
                    background-color: var(--general);
                }
            }

            .dot {
                width: 14rpx;
                height: 14rpx;
                border-radius: 50%;
                background-color: #ebedf0;
                margin-top: 12rpx;
                position: relative;
                z-index: 1;
            }

            .line {
                width: 2rpx;
                background-color: #ebedf0;
                position: absolute;
                top: 26rpx;
                bottom: -12rpx;
                left: 50%;
                transform: translateX(-50%);
                margin: 8rpx 0;
                overflow: hidden;
            }
        }

        .flow-right {
            flex: 1;
            padding-bottom: 30rpx;

            .flow-header {
                display: flex;
                justify-content: space-between;
                align-items: flex-start;
                font-size: 26rpx;
                line-height: 36rpx;
                color: #999;

                .title-box {
                    display: flex;
                    align-items: baseline;
                    column-gap: 5rpx;
                }

                .price {
                    font-size: 26rpx;
                }
            }

            .desc {
                font-size: 22rpx;
                color: #9a9a9a;
                margin-top: 8rpx;
                max-width: 70%;
            }
        }
    }
}
</style>
