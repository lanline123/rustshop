<template>
    <view class="presale-box">
        <!-- 全款预售步骤 -->
        <template v-if="presaleInfo?.saleMode === 1">
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
                            <text class="text-desc" v-if="fullPresaleStepTwoDate">{{ fullPresaleStepTwoDate }}</text>
                        </view>
                    </view>
                </view>
            </view>
        </template>
        <!-- 定金预售步骤 -->
        <template v-else>
            <view class="presale-flow">
                <view class="flow-item active">
                    <view class="flow-left">
                        <view class="dot" />
                        <view class="line" />
                    </view>
                    <view class="flow-right">
                        <view class="flow-header">
                            <view class="title-box">
                                <text class="title">{{ $t("定金(预售商品，定金不退)") }}</text>
                                <view class="rule-box" @click="handleShowPopup">
                                    <text class="split">|</text>
                                    <text class="rule">{{ $t("规则") }}</text>
                                </view>
                            </view>
                            <format-price class="price" :price-data="total?.depositAmount" />
                        </view>
                    </view>
                </view>
                <view class="flow-item">
                    <view class="flow-left">
                        <view class="dot" />
                        <view class="line" />
                    </view>
                    <view class="flow-right">
                        <view class="flow-header">
                            <text class="title">{{ $t("尾款") }}({{ PaymentTimeText }})</text>
                            <text class="price">
                                <format-price class="price" :price-data="total?.finalPaymentAmount" />
                            </text>
                        </view>
                        <view class="desc">{{ $t("如有运费、赠品、优惠等将在尾款结算") }}</view>
                    </view>
                </view>
                <view class="flow-item">
                    <view class="flow-left">
                        <view class="dot" />
                    </view>
                    <view class="flow-right">
                        <view class="flow-header">
                            <text class="title">{{ depositPresaleStepThreeTitle }}</text>
                            <text class="text-desc" v-if="depositPresaleStepThreeDate">{{ depositPresaleStepThreeDate }}</text>
                        </view>
                    </view>
                </view>
            </view>
        </template>

        <tig-popup v-model:show="show" background-color="#fff">
            <scroll-view scroll-y="true" class="popup-container">
                <view class="popup-title">{{ $t("预售规则") }}</view>
                <view class="popup-content">
                    <view class="rule-item">
                        <view class="rule-item-title">{{ $t("下单规则") }}</view>
                        <view class="rule-item-content">
                            1、{{
                                $t(
                                    "预售商品，有营销（会员价、优惠券等）时自动推荐使用，若为定金预售则在营销（会员价、优惠券等）的基础上计算尾款，付尾款时以定金的价格为基础计算尾款金额;"
                                )
                            }}
                        </view>
                        <view class="rule-item-content">2、{{ $t("订单优惠信息将在尾款时计算") }}</view>
                    </view>
                    <view class="rule-item">
                        <view class="rule-item-title">{{ $t("售后规则") }}</view>
                        <view class="rule-item-content">1、{{ $t("全款预售商品，支付预售款后可以正常发起售后流程;") }}</view>
                        <view class="rule-item-content">2、{{ $t("定金预售商品，未付尾款的订单到期也可支付尾款；付尾款后，可正常申请售后流程") }}</view>
                    </view>
                </view>
            </scroll-view>
        </tig-popup>
    </view>
</template>

<script setup lang="ts">
/**
 * @description 预售商品流程展示组件（结算页）
 * - 全款预售：付款 → 发货
 * - 定金预售：付定金 → 付尾款 → 发货
 */
import { computed, ref } from "vue";
import { useI18n } from "vue-i18n";

const { t } = useI18n();

interface Props {
    presaleInfo: any;
    total: any;
    deliveryType?: number;
}
const props = defineProps<Props>();

// 获取订单状态（结算页一般为 0）
const orderStatus = computed(() => {
    return props.total?.orderStatus ?? 0;
});

const formatShippingDate = (dateStr: string) => {
    if (!dateStr) return "";
    const [datePart, timePart] = dateStr.split(" ");
    const [year, month, day] = datePart.split("-");
    return `${year}${t("年")}${month}${t("月")}${day}${t("日")}  ${timePart || ""}`;
};

const pendingShipNodeTitle = computed(() => {
    if (props.deliveryType === 2) return t("自提");
    if (props.deliveryType === 3) return t("配送");
    return t("发货");
});

const completedShipNodeTitle = computed(() => {
    if (props.deliveryType === 2 || props.deliveryType === 3) return t("已备货");
    return t("已发货");
});

const shippingActionText = computed(() => {
    if (props.deliveryType === 2) return t("自提");
    if (props.deliveryType === 3) return t("配送");
    return t("发货");
});

// 全款预售步骤二标题
const fullPresaleStepTwoTitle = computed(() => {
    const status = orderStatus.value;
    if (status >= 2 && status !== 3) return completedShipNodeTitle.value;
    return pendingShipNodeTitle.value;
});

// 全款预售步骤二日期
const fullPresaleStepTwoDate = computed(() => {
    const status = orderStatus.value;
    if (status >= 2 && status !== 3) {
        if (props.total?.shippingTime) return formatShippingDate(props.total.shippingTime);
        return "";
    }
    return deliverTimeText.value;
});

// 定金预售步骤三标题
const depositPresaleStepThreeTitle = computed(() => {
    const st = props.total?.orderStatus ?? 0;
    if (st >= 2 && st !== 3 && props.presaleInfo?.presaleStatus === 2) return completedShipNodeTitle.value;
    return pendingShipNodeTitle.value;
});

// 定金预售步骤三日期
const depositPresaleStepThreeDate = computed(() => {
    const st = props.total?.orderStatus ?? 0;
    if (st >= 2 && st !== 3 && props.presaleInfo?.presaleStatus === 2) {
        if (props.total?.shippingTime) return formatShippingDate(props.total.shippingTime);
        return "";
    }
    return deliverTimeText.value;
});

const PaymentTimeText = computed(() => {
    const info = props.presaleInfo;
    if (!info) return "";

    if (Number(info.finalPaymentType) === 1) {
        return info.finalPaymentRelativeDays ? t("付定金{0}天后开始", [info.finalPaymentRelativeDays]) : t("付定金后开始");
    }
    return info.finalPaymentStartTime ? `${info.finalPaymentStartTime}${t("开始")}` : "";
});

const deliverTimeText = computed(() => {
    const info = props.presaleInfo;
    if (!info) return "";

    if (Number(info.deliveryType) === 1) {
        const days = info.deliveryRelativeDays ? `${info.deliveryRelativeDays}${t("天")}` : "";
        const hours = info.deliveryRelativeHours ? `${info.deliveryRelativeHours}${t("小时")}` : "";
        return `${Number(info.saleMode) === 1 ? t("付全款") : t("付尾款")}${days}${hours}${t("后")}${shippingActionText.value}`;
    }
    return info.deliveryDate ? `${info.deliveryDate}${t("开始")}` : "";
});

const show = ref(false);
const handleShowPopup = () => {
    show.value = true;
};
</script>

<style lang="scss" scoped>
.presale-box {
    padding-top: 30rpx;

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
            padding-bottom: 40rpx;

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
                width: 3rpx;
                background-color: #ebedf0;
                position: absolute;
                top: 26rpx;
                bottom: -12rpx;
                left: 50%;
                transform: translateX(-50%);
                margin: 12rpx 0;
                overflow: hidden;
            }
        }

        .flow-right {
            flex: 1;
            padding-bottom: 40rpx;

            .flow-header {
                display: flex;
                justify-content: space-between;
                align-items: flex-start;
                font-size: 26rpx;
                line-height: 36rpx;
                color: #999;

                .title-box {
                    display: flex;
                    align-items: center;

                    .rule-box {
                        margin-left: 15rpx;
                        font-size: 24rpx;
                        color: var(--general);
                        display: flex;
                        align-items: center;

                        .split {
                            margin-right: 15rpx;
                            color: #ddd;
                        }
                    }
                }

                .price {
                    font-size: 28rpx;
                }
            }

            .desc {
                font-size: 22rpx;
                color: #9a9a9a;
                margin-top: 8rpx;
            }
        }
    }
}

.popup-container {
    height: 50vh;
    border-radius: 18rpx 18rpx 0 0;
    overflow: hidden;

    .popup-content {
        padding: 0 30rpx 30rpx;
        display: flex;
        flex-direction: column;
        row-gap: 35rpx;
        font-size: 26rpx;

        .rule-item {
            display: flex;
            flex-direction: column;
            row-gap: 20rpx;
            .rule-item-title {
                font-size: 26rpx;
                font-weight: bold;
            }
            .rule-item-content {
                padding-left: 20rpx;
                font-size: 26rpx;
                line-height: 38rpx;
                color: #6a6a6a;
            }
        }
    }
}
</style>
