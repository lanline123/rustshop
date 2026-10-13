<template>
    <!-- 放弃支付挽留弹窗 -->
    <tig-popup v-model:show="showSheet" position="bottom" :round="24" :mask-click="false" :z-index="1002" background-color="#fff" @close="onPopupClose">
        <view class="abandon">
            <view class="abandon-icon">
                <image class="icon-img" :src="staticResource('payment/rightsAndinterests.png')" mode="aspectFit" />
            </view>
            <view class="abandon-title">{{ $t("本单可享受以下权益") }}</view>
            <view class="benefit-list">
                <view v-if="Number(rewardPoints) > 0" class="benefit">
                    <text class="benefit-label">{{ $t("下单成功") }}</text>
                    <text class="benefit-value">{{ $t("本单赠送") }}{{ rewardPoints }}{{ $t(configStore.integralName) }}</text>
                </view>
                <view v-if="Number(discountAmount) > 0" class="benefit">
                    <text class="benefit-label">{{ $t("会员下单成功") }}</text>
                    <view class="benefit-value">
                        <text>{{ $t("优惠立减") }}</text>
                        <format-price :is-bottom="false" :price-data="discountAmount" :font-style="{ color: 'var(--general)', fontSize: '28rpx' }" />
                    </view>
                </view>
            </view>

            <view class="current-pay">
                <view class="current-pay-left">
                    <image v-if="paymentType" lazy-load class="pay-icon" :src="staticResource('payment/pay_' + paymentType + '.png')" />
                    <text class="current-pay-text">{{ $t("正在使用") }}{{ $t(PAYMENT_TEXT_MAP[paymentType] || paymentType) }}</text>
                </view>
                <view class="current-pay-change" @click="onChange">{{ $t("更换方式") }} <i class="iconfont-h5 icon-a-gengduo20" /></view>
            </view>

            <view class="abandon-btns">
                <view class="abandon-btn cancel" @click="onAbandon">{{ $t("放弃支付") }}</view>
                <view class="abandon-btn confirm" @click="onConfirm">
                    <view class="abandon-btn-inner">
                        {{ $t("立即支付") }}
                        <format-price :price-data="amount" />
                    </view>
                </view>
            </view>
        </view>
    </tig-popup>
</template>

<script setup lang="ts">
/**
 * @description 放弃支付挽留弹窗
 * - 底部弹层展示本单权益、当前支付方式，引导用户继续支付
 * - 放弃支付 / 立即支付 / 更换方式 由父级处理
 */
import { computed } from "vue";
import { PAYMENT_TEXT_MAP } from "@/hooks/usePayment";
import { staticResource } from "@/utils";
import { useConfigStore } from "@/store/config";

interface Props {
    show: boolean;
    /** 应付金额（已格式化字符串） */
    amount: string | number;
    /** 当前选中的支付渠道 code */
    paymentType: string;
    /** 本单赠送积分（大于 0 才展示权益行） */
    rewardPoints?: number | string;
    /** 优惠立减金额 */
    discountAmount?: number | string;
    /** 顶部装饰图 */
    iconUrl?: string;
}
const props = withDefaults(defineProps<Props>(), {
    rewardPoints: 0,
    discountAmount: 0,
    iconUrl: ""
});
const emit = defineEmits(["update:show", "abandon", "confirm", "change"]);

const configStore = useConfigStore();

const showSheet = computed({
    get: () => props.show,
    set: (v: boolean) => emit("update:show", v)
});

/** 弹窗右上角关闭：与放弃支付同走 abandon */
const onPopupClose = () => emit("abandon");
const onAbandon = () => emit("abandon");
const onConfirm = () => emit("confirm");
const onChange = () => emit("change");
</script>

<style lang="scss" scoped>
.abandon {
    padding: 60rpx 40rpx 40rpx;
    padding-bottom: calc(40rpx + constant(safe-area-inset-bottom));
    padding-bottom: calc(40rpx + env(safe-area-inset-bottom));
    text-align: center;

    .abandon-icon {
        display: flex;
        justify-content: center;
        margin-bottom: 10rpx;

        .icon-img {
            width: 120rpx;
            height: 120rpx;
        }
    }

    .abandon-title {
        font-weight: bold;
        font-size: 32rpx;
        color: #1a1a1a;
    }
    .benefit-list {
        margin-top: 36rpx;
        display: flex;
        flex-direction: column;
        border-radius: 16rpx;
        background: #f7f8fa;
    }

    .benefit {
        padding: 26rpx 30rpx;
        display: flex;
        align-items: center;
        justify-content: space-between;

        .benefit-label {
            font-size: 28rpx;
            color: #333;
        }

        .benefit-value {
            display: flex;
            align-items: center;
            font-size: 28rpx;
            color: var(--general);
        }
    }

    .current-pay {
        margin-top: 28rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        column-gap: 16rpx;

        .current-pay-left {
            display: flex;
            align-items: center;
            column-gap: 8rpx;

            .pay-icon {
                width: 34rpx;
                height: 34rpx;
            }

            .current-pay-text {
                font-weight: 500;
                font-size: 24rpx;
                color: #1a1a1a;
            }
        }

        .current-pay-change {
            font-size: 24rpx;
            color: #6a6a6a;
            font-weight: 500;
            position: relative;
            margin-left: 16rpx;
            .icon-a-gengduo20 {
                font-size: 20rpx;
                color: #6a6a6a;
            }
            &:before {
                content: "";
                display: inline-block;
                width: 1rpx;
                height: 22rpx;
                background: #e8e8e8;
                position: absolute;
                left: -16rpx;
                top: 6rpx;
            }
        }
    }

    .abandon-btns {
        margin-top: 48rpx;
        display: flex;
        align-items: center;
        column-gap: 24rpx;

        .abandon-btn {
            flex: 1;
            height: 99rpx;
            line-height: 99rpx;
            border-radius: 44rpx;
            font-size: 30rpx;
            text-align: center;

            &.cancel {
                color: #1a1a1a;
                background: #fff;
                border: 1rpx solid #e8e8e8;
            }

            &.confirm {
                color: #fff;
                background: var(--general);
            }
        }
    }
}
</style>
