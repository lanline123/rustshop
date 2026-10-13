<template>
    <tig-popup v-model:show="show" background-color="#fff">
        <scroll-view scroll-y="true" class="popup-container">
            <view class="popup-title">{{ $t(shippingRuleTitle) }}</view>
            <view class="popup-content">
                <template v-for="(value, key) in formatstoreShippingRule" :key="key">
                    <view class="shippingfee-item">
                        <view class="store-title"> {{ `${value?.shopTitle}${ruleTitleSuffix}` }}</view>
                        <view class="desc">{{ ruleDesc }}</view>
                        <view class="base-freight"
                            >{{ $t("-订单基础") }}{{ getBaseRadius(value) }}{{ $t("km内") }}{{ baseFeeLabel }}<text class="">{{ getBaseFee(value) }}</text
                            >{{ configStore.dollarSignCn }}</view
                        >
                        <template v-if="value.distancePricing && value.distancePricing.length > 0">
                            <view class="delivery-rules">
                                <template v-for="(item, index) in value.distancePricing" :key="index">
                                    <view class="rule-item">
                                        <text>-</text>
                                        <text>
                                            {{ $t('超过') }}{{ item.distanceThreshold }}{{ $t('km，每增加') }}{{ item.distanceUnit }}{{ $t('km，')
                                            }}{{ feeIncreaseLabel }}{{ item.additionalFee }}{{ configStore.dollarSignCn }}</text
                                        >
                                    </view>
                                </template>
                            </view>
                        </template>
                    </view>
                </template>
            </view>
        </scroll-view>
    </tig-popup>
</template>

<script setup lang="ts">
import { useI18n } from "vue-i18n";
/**
 * 运费详情弹窗组件
 * - 展示各店铺的运费明细
 * - 显示同城配送费用详情
 */
import type { CartList } from "@/types/order/check";
import { useConfigStore } from "@/store/config";
import { computed } from "vue";

const { t } = useI18n();
interface Props {
    storeShippingRule: any;
    cartList: CartList[];
    /**
     * 费用类型
     * - localDelivery: 同城配送（默认）
     * - homeService: 预约上门服务
     */
    feeType?: "localDelivery" | "homeService";
}

const props = defineProps<Props>();

const show = defineModel({
    type: Boolean,
    default: false
});

const configStore = useConfigStore();

const isHomeService = computed(() => props.feeType === "homeService");

const shippingRuleTitle = computed(() => (isHomeService.value ? t("上门服务规则") : t("配送规则")));
const ruleTitleSuffix = computed(() => (isHomeService.value ? t("上门服务规则") : t("配送规则")));
const ruleDesc = computed(() => (isHomeService.value ? t("根据商家设置上门服务距离进行收费") : t("根据商家设置配送距离进行收费")));
const baseFeeLabel = computed(() => (isHomeService.value ? t("基础上门服务费") : t("基础配送费")));
const feeIncreaseLabel = computed(() => (isHomeService.value ? t("上门服务费增加") : t("配送费增加")));

const getBaseRadius = (value: any) => {
    const radiusInMeter = isHomeService.value ? value?.serviceRadius : value?.deliveryRadius;
    const radiusKm = Number(radiusInMeter) / 1000;
    if (Number.isNaN(radiusKm)) return radiusInMeter;
    return Number.isInteger(radiusKm) ? radiusKm : radiusKm.toFixed(2).replace(/\.?0+$/, "");
};

const getBaseFee = (value: any) => {
    return isHomeService.value ? value?.homeFee : value?.baseDeliveryFee;
};

const formatstoreShippingRule = computed(() => {
    let obj = props.storeShippingRule || {};

    if (Object.keys(obj).length > 0) {
        props.cartList.forEach((item) => {
            if (obj[item.shopId]) {
                obj[item.shopId].shopTitle = item.shopTitle;
            }
        });
    }

    return obj;
});
</script>

<style lang="scss" scoped>
.popup-container {
    height: 50vh;
    border-radius: 18rpx 18rpx 0 0;
    overflow: hidden;

    .popup-content {
        padding: 0 30rpx 30rpx;
        display: flex;
        flex-direction: column;
        row-gap: 20rpx;

        .shippingfee-item {
            border-radius: 12rpx;
            background-color: #f4f4f4;
            padding: 25rpx 20rpx;
            color: #9a9a9a;
            font-size: 24rpx;
            display: flex;
            flex-direction: column;
            row-gap: 20rpx;

            .store-title {
                font-weight: bold;
                font-size: 28rpx;
                color: #262626;
            }

            .delivery-rules {
                display: flex;
                flex-direction: column;
                row-gap: 20rpx;

                .rule-item {
                    display: flex;
                    width: 100%;

                    text {
                        word-break: keep-all;
                        line-height: 36rpx;
                    }
                }
            }
        }

        .num {
            color: var(--general);
            font-size: 24rpx;
            padding: 0 8rpx;
        }
    }
}
</style>
