<template>
    <!-- 分销佣金钱包（D1107）：展示可提现佣金 + 佣金明细 + 佣金提现，结构同「我的钱包」 -->
    <tig-layout :title="$t('我的佣金')">
        <view class="top-card">
            <view class="balance-row"> {{ $t("可用佣金") }} </view>
            <view class="amount-row">
                <format-price
                    base
                    :show-text="false"
                    :price-data="formState.commissionBalance || 0"
                    :decimals-style="{ fontSize: '40rpx', fontWeight: 'bold' }"
                    :currency-style="{ fontSize: '38rpx', fontWeight: 'bold' }"
                />
            </view>
            <view class="button-row">
                <view class="round-button" :class="{ active: activeTab === 'commissionWithdraw' }" @click="actionClick('commissionWithdraw')">{{
                    $t("提现")
                }}</view>
            </view>
        </view>

        <view v-show="activeTab === 'commissionDetail'">
            <detail v-model:active-tab="activeTab" wallet="commission" :refresh-key="refreshKey" />
        </view>

        <block v-if="activeTab === 'commissionWithdraw'">
            <raplyToCard :balance="formState.commissionBalance" wallet="commission" @back-detail="backDetail" />
        </block>
    </tig-layout>
</template>

<script lang="ts" setup>
/**
 * @description 分销佣金钱包页（D1107）
 * - 展示分销佣金可提现金额（commissionBalance，与会员余额隔离）
 * - 佣金明细 + 佣金提现，提现走 applyCommission
 */
import { ref } from "vue";
import detail from "@/pages/user/account/src/detail.vue";
import raplyToCard from "@/pages/user/account/src/raplyToCard.vue";
import { onShow } from "@dcloudio/uni-app";
import { getUser } from "@/api/user/user";
import type { UserItem } from "@/types/user/user";

// 佣金页跨分包复用会员钱包组件，小程序端需要声明异步组件占位。
defineOptions({
    componentPlaceholder: {
        detail: "view",
        raplyToCard: "view"
    }
});

const formState = ref<UserItem>({} as UserItem);

const __getUser = async () => {
    try {
        const result = await getUser();
        Object.assign(formState.value, result);
    } catch (error: any) {
        console.error(error.message);
    }
};

// 默认展示佣金明细（与 detail.vue 的 commission 分页标识一致）
const activeTab = ref("commissionDetail");
const refreshKey = ref(0);

const actionClick = (value: string) => {
    if (value === activeTab.value) return;
    activeTab.value = value;
};

const backDetail = (needRefresh = false) => {
    if (needRefresh) {
        __getUser();
        refreshKey.value++;
    }
    activeTab.value = "commissionDetail";
};

onShow(() => {
    __getUser();
});
</script>
<style lang="scss" scoped>
.top-card {
    background: var(--general);
    background-size: 100% 100%;
    background-repeat: no-repeat;
    background-position: center;
    border-radius: 10rpx;
    margin: 20rpx;
    color: #fff;
    box-sizing: border-box;
    display: flex;
    flex-direction: column;
    padding: 60rpx;

    .balance-row,
    .amount-row,
    .button-row {
        text-align: center;
        margin: 20rpx 20rpx 0;
    }

    .balance-row {
        font-size: 36rpx;
    }

    .amount-row {
        font-size: 52rpx;
        font-weight: 400;
    }

    .button-row {
        display: flex;
        margin-top: 60rpx;
        justify-content: center;
        gap: 20rpx;
        height: 60rpx;

        .round-button {
            height: 60rpx;
            line-height: 60rpx;
            border-radius: 46rpx;
            min-width: 200rpx;
            border: 2rpx solid white;
            color: white;
            outline: none;
            font-size: 24rpx;
            background-color: var(--general);
            &.active {
                background-color: #fff;
                color: var(--general);
                font-weight: 700;
            }
        }
    }
}
</style>
