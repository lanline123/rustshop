<template>
    <view class="total-card">
        <view>
            <view class="total-card-item">
                <view class="total-card-item-left">{{ $t("商品总价") }}</view>
                <view class="total-card-item-right"><format-price :is-bottom="false" :price-data="total.productAmount" /></view>
            </view>
            <view v-if="total.serviceFee && total.serviceFee > 0" class="total-card-item">
                <view class="total-card-item-left">{{ $t("附加费用") }}</view>
                <view class="total-card-item-right"><format-price :is-bottom="false" :price-data="total.serviceFee" /></view>
            </view>

            <!-- 运费：除到店自提外，其它类型订单均需展示；运费=0 显示对应文案 -->
            <view v-if="showShippingFeeRow" class="total-card-item" @click="$emit('showShippingFee')">
                <view class="total-card-item-left">
                    {{ $t(shippingFeeTitleKey) }}
                    <text class="iconfont-h5 icon-wenhao-xianxingyuankuang" />
                </view>
                <view class="total-card-item-right special-text">
                    <format-price :is-bottom="false" :price-data="shippingFeeAmount" />
                </view>
            </view>

            <view v-if="!isSubCard && cardConfig?.unitPrice" class="total-card-item">
                <view class="total-card-item-left">{{ $t("次卡抵扣") }}</view>
                <view class="total-card-item-right">- <format-price :is-bottom="false" :price-data="cardConfig?.unitPrice" /></view>
            </view>
            <view v-if="total.balance > 0" class="total-card-item">
                <view class="total-card-item-left">{{ $t("使用余额") }}</view>
                <view class="total-card-item-right">- <format-price :is-bottom="false" :price-data="total.balance" /></view>
            </view>
            <view v-if="total.creditAmount && total.creditAmount > 0" class="total-card-item">
                <view class="total-card-item-left">{{ $t("使用账期") }}</view>
                <view class="total-card-item-right">- <format-price :is-bottom="false" :price-data="total.creditAmount" /></view>
            </view>
            <view v-if="total.pointsAmount > 0 && flowType != 3" class="total-card-item">
                <view class="total-card-item-left">{{ `${configStore.integralName}${$t("减免")}` }}</view>
                <view class="total-card-item-right">- <format-price :is-bottom="false" :price-data="total.pointsAmount" /></view>
            </view>
            <view v-if="total.discounts > 0" class="total-card-item">
                <view class="total-card-item-left" @click="handleShowDiscountInfo">
                    {{ $t("共优惠") }}
                    <up-icon style="padding-left: 4rpx" size="16" color="#969799" name="info-circle" />
                </view>
                <view class="total-card-item-right">
                    <view> - <format-price :is-bottom="false" :price-data="total.discounts" /> </view>
                </view>
            </view>
            <view v-if="total.orderSendPoint && total.orderSendPoint > 0" class="total-card-tig">
                * {{ $t("该订单完成后，您将预计获得") }} <text class="special-text">{{ total.orderSendPoint }}</text> {{ $t(configStore.integralName) }}
            </view>
        </view>

        <discountInfo v-model="showDiscountInfo" :total="total" discount-type="order" />
    </view>
</template>

<script setup lang="ts">
import { useI18n } from "vue-i18n";
/**
 * 订单金额汇总卡片组件
 * - 显示商品小计、运费、优惠金额
 * - 展开查看优惠明细
 * - 显示订单总价
 */
import { computed, ref, type PropType } from "vue";
import discountInfo from "@/components/product/discountInfo.vue";
import type { Total as OrderTotal } from "@/types/order/check";
import { useConfigStore } from "@/store/config";
import type { PresaleInfo } from "@/types/presale/presale";

const { t } = useI18n();
type Total = OrderTotal & {
    /**
     * 附加费用（部分接口返回该字段）
     */
    serviceFee?: number;
    cycleCardInfo?: {
        unitPrice?: number | string;
    };
    /** 跨境税率（%）：仅海外站后端返回 */
    taxRate?: number | string;
    /** 跨境税费金额：仅海外站后端返回 */
    taxRateAmount?: number | string;
};

const props = defineProps({
    total: {
        type: Object as PropType<Total>,
        default: () => ({})
    },
    // 预售（结算页透传，当前组件可按需扩展展示）
    presaleInfo: {
        type: Object as PropType<PresaleInfo | null>,
        default: null
    },
    cartList: {
        type: Array as PropType<any[]>,
        default: () => []
    },
    flowType: {
        type: Number,
        default: 0
    },
    /**
     * 配送方式：1-快递配送 2-到店自提 3-同城配送
     * 预约上门：以 flowType=10 为准
     */
    deliveryType: {
        type: Number,
        default: 0
    },
    cardConfig: {
        type: Object as PropType<any>,
        default: () => ({})
    },
    isSubCard: {
        type: Boolean,
        default: false
    }
});

defineEmits(["showShippingFee"]);

const configStore = useConfigStore();

const showDiscountInfo = ref(false);
const handleShowDiscountInfo = () => {
    showDiscountInfo.value = !showDiscountInfo.value;
};

const shippingFeeAmount = computed(() => Number(props.total?.shippingFee ?? 0));

// 是否“无需配送”（整单均无需配送时才隐藏运费行）
const isAllNoShipping = computed(() => {
    const list: any[] = (props.cartList as any[]) || [];
    if (!Array.isArray(list) || list.length === 0) return false;
    return list.every((item) => Number(item?.noShipping ?? 0) === 1);
});

// 运费行展示规则：
// - 到店自提：不展示
// - 同城配送/预约上门：仍展示（即使为 0）
// - 其它：若“无需配送”则不展示，否则展示（即使为 0）
const showShippingFeeRow = computed(() => {
    if (props.deliveryType === 2) return false;
    if (props.flowType === 10) return true;
    if (props.deliveryType === 3) return true;
    return !isAllNoShipping.value;
});

const shippingFeeTitleKey = computed(() => {
    // 预约上门
    if (props.flowType === 10) return t("上门服务费");
    // 同城配送
    if (props.deliveryType === 3) return t("配送费");
    // 默认快递配送
    return t("运费");
});

const shippingFeeFreeTextKey = computed(() => {
    if (props.flowType === 10) return t("免费上门");
    if (props.deliveryType === 3) return t("免配送费");
    return t("包邮");
});
</script>

<style lang="scss" scoped>
.more-ico {
    width: 36rpx;
    height: 36rpx;
    padding-left: 10rpx;
}
.total-card {
    background-color: #fff;
    box-sizing: border-box;
    padding: 30rpx 32rpx;
    border-radius: 18rpx;
    margin-top: 20rpx;
    margin-bottom: 20rpx;

    .total-card-item {
        padding-bottom: 35rpx;
        display: flex;
        justify-content: space-between;
        &:last-child {
            padding-bottom: 0;
        }

        .total-card-item-left {
            display: flex;
            align-items: baseline;
            column-gap: 6rpx;

            .icon-wenhao-xianxingyuankuang {
                font-size: 26rpx;
            }
        }

        .red {
            color: var(--general);
        }

        .total-card-ico {
            margin-left: 10rpx;
        }

        /* @vort-if overseas */
        .tax-label {
            display: flex;
            align-items: center;
            column-gap: 10rpx;
        }

        .tax-tip-trigger {
            width: 28rpx;
            height: 28rpx;
            line-height: 28rpx;
            text-align: center;
            border-radius: 50%;
            font-size: 20rpx;
            color: #8b8e97;
            border: 1rpx solid #c6c7cc;
            box-sizing: border-box;
        }
        /* @vort-endif */
    }

    /* @vort-if overseas */
    .tax-popup {
        background: #fff;
        padding: 32rpx 28rpx 46rpx;
        min-height: 420rpx;
        box-sizing: border-box;
        padding-bottom: calc(46rpx + env(safe-area-inset-bottom));
        padding-bottom: calc(46rpx + constant(safe-area-inset-bottom));

        .tax-popup-title {
            font-size: 30rpx;
            font-weight: 700;
            color: #1a1a1a;
            margin-bottom: 20rpx;
        }

        .tax-popup-content {
            font-size: 26rpx;
            line-height: 40rpx;
            color: #4b4d52;
        }
    }
    /* @vort-endif */

    .total-card-tig {
        display: flex;
        justify-content: flex-end;
        font-size: 24rpx;
        color: #999;
        padding-bottom: 10rpx;

        .special-text {
            color: var(--general);
            padding: 0 5rpx;
        }
    }
}
</style>
