<template>
    <view class="paymentType">
        <view class="pay-list">
            <radio-group class="radio-group" @change="paymentChange">
                <template v-for="(payment, idx) in displayPaymentList" :key="idx">
                    <view class="item">
                        <view class="payment-info">
                            <template v-if="payment === 'balance'">
                                <view class="payment-info-balance-icon">¥</view>
                                <view class="payment-info-text payment-info-balance">
                                    <view>{{ $t("余额支付") }}</view>
                                    <view class="payment-info-balance__amount">
                                        {{ $t("可用余额") }}
                                        <format-price :is-bottom="false" :price-data="props.balance" />
                                    </view>
                                </view>
                            </template>
                            <template v-else>
                                <image
                                    v-if="payment !== 'offline'"
                                    class="payment-info-img"
                                    :src="staticResource('payment/pay_' + payIconName(payment!) + '.png')"
                                    :mode="payment === 'antom' ? 'aspectFit' : 'aspectFill'"
                                />
                                <view class="payment-info-text">
                                    {{ $t(paymentText[payment!] || "") }}
                                </view>
                            </template>
                        </view>
                        <radio
                            :value="payment"
                            active-background-color="var(--general)"
                            :checked="payment === modelValue"
                            :disabled="payment === 'balance' && props.balanceDisabled"
                            style="margin-right: 20rpx; transform: scale(0.9)"
                        />
                    </view>
                </template>
            </radio-group>
        </view>
    </view>
</template>

<script setup lang="ts">
import { useI18n } from "vue-i18n";
/**
 * 支付方式选择组件
 * - 展示可用支付方式列表（微信、支付宝、PayPal、线下等）
 * - 单选模式，通过v-model绑定选中的支付方式
 */
import { computed, watch } from "vue";
import { staticResource, isOverseas } from "@/utils";
import { canUseApplePay } from "@/utils/applePay";

const { t } = useI18n();

interface Props {
    paymentList: string[];
    modelValue: string;
    balance?: number | string;
    balanceDisabled?: boolean;
}
const props = withDefaults(defineProps<Props>(), {
    balance: 0,
    balanceDisabled: false
});

const emit = defineEmits(["update:modelValue"]);

const paymentText: { [key: string]: string } = {
    wechat: t("微信支付"),
    alipay: t("支付宝支付"),
    paypal: t("PayPal支付"),
    offline: t("线下支付"),
    balance: t("余额支付"),
    yabanpayWechat: t("微信支付"),
    yabanpayAlipay: t("支付宝支付"),
    yunpayWechat: t("微信支付"),
    yunpayAlipay: t("支付宝支付"),
    yunpayYunshanfu: t("云闪付"),
    huifuWechat: t("微信支付"),
    lakalaWechat: t("微信支付"),
    lakalaAlipay: t("支付宝支付"),
    yeepayWechat: "微信支付",
    yeepayAlipay: "支付宝支付",
    huifuAlipay: t("支付宝支付"),
    // Apple Pay 品牌名不翻译
    applepay: "Apple Pay"
};

// 渲染列表：applepay 仅在 H5 且设备支持时展示；跨境支付仅跨境环境展示
const displayPaymentList = computed(() =>
    props.paymentList.filter((p) => {
        if (p === "applepay") return canUseApplePay();
        return true;
    })
);

// 选中项始终保持在可见列表内，避免默认项被过滤后无选中
watch(
    displayPaymentList,
    (list) => {
        const selectableList = list.filter((item) => item !== "balance" || !props.balanceDisabled);
        if (selectableList.length && !selectableList.includes(props.modelValue)) {
            emit("update:modelValue", selectableList[0]);
        }
    },
    { immediate: true }
);

// payCode → 图标基名映射：汇付微信/支付宝复用现有微信/支付宝图标
const payIconMap: { [key: string]: string } = {
    huifuWechat: "wechat",
    lakalaWechat: "wechat",
    lakalaAlipay: "alipay",
    yeepayWechat: "wechat",
    yeepayAlipay: "alipay",
    huifuAlipay: "alipay"
};
const payIconName = (payment: string) => payIconMap[payment] || payment;

const paymentChange = (e: any) => {
    emit("update:modelValue", e.detail.value);
};
</script>

<style lang="scss" scoped>
.pay-list {
    background: #fff;
    padding: 5rpx 36rpx 15rpx;
    border-radius: 18rpx;
    margin-top: 40rpx;
}
.pay-list .item:last-child {
    border: 0;
}
.pay-list .item {
    overflow: hidden;
    position: relative;
    width: 100%;
    height: 100rpx;
    border-bottom: 1rpx solid #eee;
    padding: 20rpx 0;
    display: flex;
    align-items: center;
    justify-content: space-between;

    .payment-info {
        display: flex;
        align-items: center;

        .payment-info-text {
            min-width: 145rpx;
            padding-left: 15rpx;
        }

        .payment-info-img {
            width: 66rpx;
            height: 66rpx;
        }

        .payment-info-balance-icon {
            display: flex;
            align-items: center;
            justify-content: center;
            width: 66rpx;
            height: 66rpx;
            border-radius: 50%;
            background: #fff3df;
            color: #f59a23;
            font-size: 34rpx;
            font-weight: 600;
        }

        .payment-info-balance {
            line-height: 34rpx;
        }

        .payment-info-balance__amount {
            display: flex;
            align-items: center;
            color: #999;
            font-size: 22rpx;
        }
    }
}
</style>
