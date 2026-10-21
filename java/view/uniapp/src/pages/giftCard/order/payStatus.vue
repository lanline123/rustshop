<template>
    <tig-layout>
        <template v-if="!isLoading">
            <view class="pay-status">
                <view class="pay-status-content">
                    <view class="status-content">
                        <animation />
                        <view class="status-text">{{ $t("充值成功") }}</view>
                    </view>
                    <view class="money">
                        <view class="money-text">{{ $t("实付") }}</view>
                        <view class="money-value">
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
                                :price-data="info.orderAmount"
                            />
                        </view>
                    </view>
                    <view class="status-btns">
                        <view class="btn-item" @click="redirect({ url: '/pages/user/account/index' })">{{ $t("查看充值") }}</view>
                        <view class="btn-item primary" @click="redirect({ url: '/pages/index/index' })">{{ $t("继续逛逛") }}</view>
                    </view>
                </view>

                <template v-if="configStore.isGuestLikeGoods">
                    <view class="recommend">
                        <view class="recommend-mask" />
                        <view class="recommend-content">
                            <recommend title-bg="#fff" />
                        </view>
                    </view>
                </template>
            </view>
        </template>

        <tig-loadingpage v-model="isLoading" />
    </tig-layout>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import { useThemeStore } from "@/store/theme";
import { useConfigStore } from "@/store/config";
import { getPayLog } from "@/api/order/pay";
import animation from "@/pages/order/src/animation.vue";
import recommend from "@/components/recommend/index.vue";
import { redirect } from "@/utils";
import type { PayLogData } from "@/types/order/payStatus";

const configStore = useConfigStore();

// onLoad((options) => {
//     if (options && options.id) {
//         isLoading.value = true;
//         getPayLogData(Number(options.id));
//     } else {
//         redirect({ url: "/pages/index/index" });
//     }
// });

const isLoading = ref(false);

const info = ref<PayLogData>({} as PayLogData);
// 是否是礼品卡兑换订单
const isGiftCardExchangeOrder = computed(() => {
    return info.value.orderType === 12;
});
// const getPayLogData = async (id: number) => {
//     try {
//         const result = await getPayLog({ id });
//         info.value = result;
//     } catch (e) {
//         console.error(e);
//     } finally {
//         isLoading.value = false;
//     }
// };

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
