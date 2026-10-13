<template>
    <tig-layout>
        <template v-if="!isLoading">
            <view class="pay-status">
                <view v-if="resultState === 'success'" class="pay-status-content">
                    <view class="status-content">
                        <animation />
                        <view class="status-text">{{ $t(info.orderReservation ? "预约成功" : "支付成功") }}</view>
                    </view>
                    <template v-if="!info.orderReservation">
                        <view class="money">
                            <view class="money-text">{{ $t("实付") }}</view>
                            <view class="money-value">
                                <!-- <format-price
                                    :font-style="{ fontSize: '38rpx' }"
                                    :currency-style="{
                                        fontSize: '30rpx'
                                    }"
                                    :decimals-style="{
                                        fontSize: '26rpx',
                                        fontWeight: '400'
                                    }"
                                    :is-bootom="true"
                                    :price-data="info.orderAmount"
                                /> -->
                                <format-price
                                    :font-style="{ fontSize: '38rpx' }"
                                    :currency-style="{
                                        fontSize: '30rpx'
                                    }"
                                    :decimals-style="{
                                        fontSize: '26rpx',
                                        fontWeight: '400'
                                    }"
                                    :is-bootom="true"
                                    :price-data="info.payAmount"
                                />
                            </view>
                        </view>
                    </template>
                    <template v-else>
                        <view class="reserv-box">
                            <template v-if="info.orderType === 9">
                                <text
                                    >{{ $t("请在") }} {{ info.orderReservation.reservationDate }} {{ info.orderReservation.startTime }}~{{
                                        info.orderReservation.endTime
                                    }}
                                    {{ $t("准时到店消费") }}</text
                                >
                            </template>
                            <template v-if="info.orderType === 10">
                                <text
                                    >{{ $t("您的服务预约在") }} {{ info.orderReservation.reservationDate }} {{ info.orderReservation.startTime }}~{{
                                        info.orderReservation.endTime
                                    }}</text
                                >
                                <text>{{ $t("如需更上门服务改时间请提前联系商家进行沟通") }}</text>
                            </template>
                        </view>
                    </template>

                    <view class="status-btns">
                        <view class="btn-item" @click="redirect({ url: '/pages/user/order/list' })">{{ $t("查看订单") }}</view>
                        <view class="btn-item primary" @click="redirect({ url: '/pages/index/index' })">{{ $t("继续逛逛") }}</view>
                    </view>
                </view>
                <template v-if="resultState === 'success' && configStore.isGuestLikeGoods">
                    <view class="recommend">
                        <view class="recommend-mask" />
                        <view class="recommend-content">
                            <recommend title-bg="#fff" />
                        </view>
                    </view>
                </template>
                <view v-if="resultState === 'error'" class="pay-result-error">
                    <view class="error-mark">!</view>
                    <view class="error-title">{{ $t("支付结果暂未确认") }}</view>
                    <view class="error-desc">{{ $t("请重新加载，或前往订单列表查看最新状态") }}</view>
                    <view class="error-btns">
                        <view class="error-btn primary" @click="retryPayResult">{{ $t("重新加载") }}</view>
                        <view class="error-btn" @click="redirect({ url: '/pages/user/order/list' })">{{ $t("查看订单") }}</view>
                    </view>
                </view>
            </view>
        </template>

        <tig-loadingpage v-model="isLoading" />
    </tig-layout>
</template>

<script setup lang="ts">
/**
 * 支付结果页
 * - 显示支付成功/预约成功状态
 * - 预约订单显示预约时间和服务信息
 * - 提供查看订单、继续逛逛入口
 * - 底部展示商品推荐
 */
import { computed, ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import { useThemeStore } from "@/store/theme";
import { useConfigStore } from "@/store/config";
import { checkPayStatus, getPayLog } from "@/api/order/pay";
import animation from "./src/animation.vue";
import recommend from "@/components/recommend/index.vue";
import { redirect } from "@/utils";
import type { PayLogData } from "@/types/order/payStatus";
import { resolvePayResult, type PayResultState } from "./src/payResult";

const configStore = useConfigStore();
const isLoading = ref(true);
const resultState = ref<PayResultState>("loading");
const currentOrderId = ref<number | null>(null);
const info = ref<PayLogData>({} as PayLogData);

const getPayLogData = async (id: number) => {
    isLoading.value = true;
    resultState.value = "loading";

    try {
        const result = await resolvePayResult({
            orderId: id,
            loadPayLog: getPayLog,
            loadOrderPayStatus: checkPayStatus
        });

        if (result.state === "success") {
            info.value = result.info;
            resultState.value = "success";
        } else {
            if (result.error) {
                console.error("获取支付结果失败:", result.error);
            }
            resultState.value = "error";
        }
    } finally {
        isLoading.value = false;
    }
};

const retryPayResult = () => {
    if (currentOrderId.value !== null) {
        getPayLogData(currentOrderId.value);
    }
};

onLoad((options) => {
    // #ifdef H5
    uni.removeStorageSync("directPayPending");
    // #endif
    const orderId = Number(options?.id);
    if (!Number.isInteger(orderId) || orderId <= 0) {
        redirect({ url: "/pages/index/index" });
        return;
    }

    currentOrderId.value = orderId;
    getPayLogData(orderId);
});

const themeStore = useThemeStore();

const primaryBg = computed(() => {
    return themeStore.hexToRgba(themeStore.themeStyle["--general"], 0.1);
});
</script>

<style lang="scss" scoped>
.pay-status {
    .pay-status-content {
        padding: 110rpx 0 80rpx;
        background: linear-gradient(203.18deg, hsla(0, 0%, 100%, 0.9), #fff 85.01%), var(--general);
        display: flex;
        flex-direction: column;
        align-items: center;
        row-gap: 38rpx;

        .status-content {
            display: flex;
            align-items: center;
            column-gap: 12rpx;
            .status-text {
                font-size: 40rpx;
                font-weight: 600;
                color: var(--general);
            }
        }

        .money {
            display: flex;
            align-items: end;
            color: var(--general);
            column-gap: 6rpx;

            .money-value {
                font-weight: 500;
            }
            .money-text {
                font-size: 24rpx;
                padding-bottom: 6rpx;
            }
        }

        .reserv-box {
            color: #757575;
            font-size: 24rpx;
            margin-top: -10rpx;
            padding-bottom: 10rpx;
            display: flex;
            align-items: center;
            justify-content: center;
            flex-direction: column;
            width: 100%;
            row-gap: 4rpx;
        }

        .status-btns {
            display: flex;
            align-items: center;
            column-gap: 30rpx;

            .btn-item {
                border: 1px solid #c4c4c4;
                padding: 14rpx 30rpx;
                color: rgba(117, 117, 117, 1);
                font-size: 24rpx;
                display: flex;
                align-items: center;
                font-weight: 600;
                border-radius: 6rpx;

                &:active {
                    background-color: rgba(117, 117, 117, 0.1);
                }

                &.primary {
                    color: var(--general);
                    border-color: var(--general);

                    &:active {
                        background-color: v-bind(primaryBg);
                    }
                }
            }
        }
    }

    .pay-result-error {
        min-height: 560rpx;
        padding: 110rpx 40rpx 80rpx;
        background: #fff;
        display: flex;
        flex-direction: column;
        align-items: center;

        .error-mark {
            width: 84rpx;
            height: 84rpx;
            margin-bottom: 28rpx;
            border: 4rpx solid #ff9a2e;
            border-radius: 50%;
            color: #ff9a2e;
            font-size: 52rpx;
            line-height: 76rpx;
            text-align: center;
            font-weight: 600;
        }

        .error-title {
            color: #333;
            font-size: 36rpx;
            font-weight: 600;
        }

        .error-desc {
            margin: 18rpx 0 48rpx;
            color: #888;
            font-size: 24rpx;
            text-align: center;
        }

        .error-btns {
            display: flex;
            align-items: center;
            column-gap: 30rpx;

            .error-btn {
                border: 1px solid #c4c4c4;
                padding: 14rpx 30rpx;
                color: rgba(117, 117, 117, 1);
                font-size: 24rpx;
                display: flex;
                align-items: center;
                font-weight: 600;
                border-radius: 6rpx;

                &:active {
                    background-color: rgba(117, 117, 117, 0.1);
                }

                &.primary {
                    color: var(--general);
                    border-color: var(--general);

                    &:active {
                        background-color: v-bind(primaryBg);
                    }
                }
            }
        }
    }

    .recommend {
        position: relative;

        .recommend-mask {
            background: linear-gradient(180deg, #fff, hsla(0, 0%, 100%, 0));
            height: 26.66667vw;
            left: 0;
            margin: auto;
            position: absolute;
            top: 80rpx;
            width: 100%;
        }
        .recommend-content {
            position: relative;
            z-index: 1;
        }
    }
}
</style>
