<template>
    <view class="balance">
        <view class="balance-title" @click="handleToBalance"
            >{{ $t("余额") }}
            <view v-if="isOverseas()" class="balance-text"
                >{{ $t("当前账户余额") }}：<format-price
                    :is-bottom="false"
                    :font-style="{ fontSize: '22rpx' }"
                    :currency-style="{ fontSize: '22rpx' }"
                    :decimals-style="{ fontSize: '22rpx' }"
                    :price-data="balance"
            /></view>
            <view v-else class="balance-text"
                >{{ $t("当前账户余额") }}：<text class="special-text">{{ balance }}</text></view
            >
        </view>
        <view class="balance-switch-wrap">
            <view v-if="isBalance" class="balance-use-text">
                {{ $t("支付") }}
                <format-price
                    :font-style="{ fontSize: '24.5rpx' }"
                    :currency-style="{ fontSize: '24.5rpx' }"
                    :decimals-style="{ fontSize: '24.5rpx' }"
                    :price-data="balanceAmount"
                />
            </view>
            <switch class="balance-switch" :checked="isBalance" color="var(--general)" @change="handleBalance" />
        </view>
    </view>
</template>

<script setup lang="ts">
/**
 * 余额抵扣卡片组件
 * - 展示当前账户余额与本单余额支付金额
 * - 开关控制是否使用余额抵扣（与账期互斥，由结算页协调）
 */
import { ref } from "vue";
import { isOverseas } from "@/utils";

interface Props {
    balance: number | string;
    /** 本单使用余额金额 */
    balanceAmount: number;
}

defineProps<Props>();

const emit = defineEmits(["sendBalanceStatus"]);

const isBalance = ref(false);

const handleBalance = () => {
    isBalance.value = !isBalance.value;
    emit("sendBalanceStatus", isBalance.value);
};

const handleToBalance = () => {
    uni.navigateTo({
        url: "/pages/user/account/index"
    });
};

defineExpose({
    isBalance
});
</script>

<style lang="scss" scoped>
.balance {
    display: flex;
    align-items: center;
    justify-content: space-between;
    min-height: 56rpx;
    background-color: #fff;
    font-size: 24.5rpx;

    .balance-title {
        display: flex;

        .balance-text {
            font-size: 22rpx;
            padding-left: 15rpx;
            display: flex;
            align-items: flex-end;

            .special-text {
                color: var(--general);
            }
        }
    }
}

.balance-switch-wrap {
    position: relative;
    display: flex;
    align-items: center;
}

.balance-switch {
    margin: 0;
    // 缩放锚定右侧，配合去掉内置 margin-right，使开关与卡片右内边距贴边
    transform: scale(0.8);
    transform-origin: right center;

    /* #ifdef H5 */
    :deep(.uni-switch-input) {
        margin-right: 0;
    }
    /* #endif */
}

.balance-use-text {
    display: flex;
    align-items: center;
    font-size: 24.5rpx;
    max-width: 300rpx;
}
</style>
