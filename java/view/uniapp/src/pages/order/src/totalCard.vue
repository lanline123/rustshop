<template>
    <view class="total-card-box">
        <view class="total-card">
            <view class="total-card-item">
                <view class="card-item-label">
                    <text class="overstriking card-item-label-text">{{ $t("小计") }}</text>
                    <text>{{ `(${Number(total?.totalCount) || 0}${$t("件")})` }}</text>
                </view>
                <view class="card-item-value"><format-price :is-bottom="false" :price-data="total.productAmount" /></view>
            </view>
            <template v-if="total.serviceFee && total.serviceFee > 0">
                <view class="total-card-item">
                    <view class="card-item-label">{{ $t("附加费用") }}</view>
                    <view class="card-item-value"><format-price :is-bottom="false" :price-data="total.serviceFee" /></view>
                </view>
            </template>
            <template v-if="showShippingFeeRow">
                <view class="total-card-item" @click="$emit('showShippingFee')">
                    <view class="card-item-label shipping-label">
                        {{ $t(shippingFeeTitleKey) }}
                    </view>
                    <view class="card-item-value">
                        <template v-if="showShippingFeeAmount">
                            <format-price :is-bottom="false" :price-data="shippingFeeAmount" />
                        </template>
                        <template v-else>
                            <text>{{ $t("包邮") }}</text>
                        </template>
                    </view>
                </view>
            </template>
            <template v-if="total.balance > 0">
                <view class="total-card-item">
                    <view class="card-item-label">{{ $t("使用余额") }}</view>
                    <view class="card-item-value">- <format-price :is-bottom="false" :price-data="total.balance" /></view>
                </view>
            </template>
            <template v-if="total.pointsAmount > 0 && flowType != 3">
                <view class="total-card-item">
                    <view class="card-item-label">{{ `${configStore.integralName}${$t("减免")}` }}</view>
                    <view class="card-item-value">- <format-price :is-bottom="false" :price-data="total.pointsAmount" /></view>
                </view>
            </template>
            <template v-if="total.discounts > 0">
                <view class="total-card-item">
                    <view class="card-item-label discount-label" @click="handleShowDiscountInfo">
                        {{ $t("共优惠") }}
                        <up-icon style="padding-left: 4rpx" size="16" color="#969799" name="info-circle" />
                    </view>
                    <view class="card-item-value">- <format-price :is-bottom="false" :price-data="total.discounts" /></view>
                </view>
            </template>
            <view class="total-card-item unpaid-amount-box">
                <view class="card-item-label">
                    <text class="overstriking card-item-label-text">{{ $t("订单合计") }}</text>
                </view>
                <view class="card-item-value unpaid-amount">
                    <!-- flowType12 是电子实体卡兑换商品，不显示共减 -->
                    <template v-if="flowType != 12">
                        <view class="price-info-discount">
                            <text class="discount-text">{{ $t("共减") }}</text>
                            <format-price :is-bottom="false" :font-style="{ fontSize: '25rpx' }" :price-data="total?.discounts" />
                        </view>
                    </template>
                    <view class="price-info-total">
                        <format-price :is-bottom="false" :price-data="total?.unpaidAmount" />
                    </view>
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
/**
 * 订单合计卡片组件（通用版，标准/跨境构建使用）
 * - 显示商品小计、附加费用、配送费、抵扣与优惠
 * - 展开查看优惠明细
 * - 显示订单合计与共减
 */
import { computed, ref, type PropType } from "vue";
import discountInfo from "@/components/product/discountInfo.vue";
import type { CartList, Total as OrderTotal } from "@/types/order/check";
import { useConfigStore } from "@/store/config";
import { isO2o } from "@/utils";

type Total = OrderTotal & {
    /**
     * 附加费用（部分接口返回该字段）
     */
    serviceFee?: number;
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
    cartList: {
        type: Array as PropType<CartList[]>,
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
    /**
     * 是否存在可查看的配送规则
     */
    hasShippingRule: {
        type: Boolean,
        default: false
    }
});

defineEmits(["showShippingFee"]);

const configStore = useConfigStore();

const shippingFeeAmount = computed(() => Number(props.total?.shippingFee ?? 0));

// 是否“无需配送”（整单均无需配送时才隐藏运费行）
const isAllNoShipping = computed(() => {
    const list: any[] = (props.cartList as any[]) || [];
    if (!Array.isArray(list) || list.length === 0) return false;
    return list.every((item) => Number(item?.noShipping ?? 0) === 1);
});

// 是否存在需要配送的商品
const hasShippingProduct = computed(() => {
    const list: any[] = (props.cartList as any[]) || [];
    return Array.isArray(list) && list.some((item) => Number(item?.noShipping ?? 0) === 0);
});

// 运费行展示规则：
// - 到店自提：不展示
// - 同城配送/预约上门：仍展示（即使为 0）
// - 其它：若“无需配送”则不展示，否则展示（即使为 0）
const showShippingFeeRow = computed(() => {
    if (!isO2o()) return hasShippingProduct.value;
    if (props.deliveryType === 2) return false;
    if (props.flowType === 10) return true;
    if (props.deliveryType === 3) return true;
    return !isAllNoShipping.value;
});

const shippingFeeTitleKey = computed(() => {
    if (!isO2o()) return "快递";
    // 预约上门
    if (props.flowType === 10) return "上门服务费";
    // 同城配送
    if (props.deliveryType === 3) return "配送费";
    // 默认快递配送
    return "运费";
});

// 运费为 0 时以“包邮”替代金额
const showShippingFeeAmount = computed(() => isO2o() || shippingFeeAmount.value > 0);

const showDiscountInfo = ref(false);

const handleShowDiscountInfo = () => {
    showDiscountInfo.value = !showDiscountInfo.value;
};
</script>

<style lang="scss" scoped>
.overstriking {
    font-weight: 700;
}

.total-card-box {
    background-color: #fff;
    box-sizing: border-box;
    font-size: 24.5rpx;
    margin-bottom: 14rpx;

    .total-card {
        display: flex;
        flex-direction: column;

        .total-card-item {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 14rpx 28rpx;

            &.unpaid-amount-box {
                position: relative;
                padding-top: 26rpx;
                padding-bottom: 26rpx;

                &::before {
                    content: "";
                    position: absolute;
                    top: 0;
                    left: 0;
                    right: 0;
                    height: 1px;
                    background-color: #f0f1f2;
                    transform: scaleY(0.5);
                    transform-origin: 0 0;
                    pointer-events: none;
                }
            }

            .discount-label {
                display: flex;
                align-items: center;
            }

            .shipping-label {
                display: flex;
                align-items: baseline;
                column-gap: 6rpx;

                .icon-wenhao-xianxingyuankuang {
                    font-size: 26rpx;
                }
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

            .unpaid-amount {
                display: flex;
                align-items: center;

                .price-info-discount {
                    display: flex;
                    align-items: baseline;
                    color: var(--general);
                    margin-left: 5rpx;
                    font-size: 24.5rpx;
                    line-height: 32rpx;
                }

                .price-info-total {
                    height: 32rpx;
                    line-height: 32rpx;
                    font-size: 32rpx;
                    color: rgb(26, 26, 26);
                    margin-left: 14rpx;
                }
            }
        }

        .card-item-label-text {
            margin-right: 7rpx;
        }
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
        padding: 0 28rpx 14rpx;

        .special-text {
            color: var(--general);
            padding: 0 5rpx;
        }
    }
}
</style>
