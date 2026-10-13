<template>
    <tig-popup v-model:show="showDiscountInfo" :z-index="999" :show-close="false" background-color="#fff">
        <view class="discount">
            <view class="discount-title">
                <view class="title-text">{{ $t("优惠明细") }}</view>
                <view class="close-icon" @click="showDiscountInfo = false">
                    <image :src="staticResource('order/close.png')" />
                </view>
            </view>
            <view class="discount-content">
                <view class="discount-item-box">
                    <view class="item-label fontWeight">
                        {{ $t("商品总额") }}
                        <text v-if="itemCount > 0" class="item-label-text">{{ `(${itemCount}${$t("件")})` }}</text>
                    </view>
                    <view class="item-value">
                        <format-price :show-text="false" :is-bottom="false" :price-data="total.productAmount" />
                    </view>
                </view>
                <view class="line" />
                <view v-if="total.serviceFee && total.serviceFee > 0" class="discount-item-box">
                    <view class="item-label">{{ $t("附加费用") }}</view>
                    <view class="item-value">
                        <format-price :show-text="false" :is-bottom="false" :price-data="total.serviceFee" />
                    </view>
                </view>
                <view class="discount-item-box">
                    <view class="item-label">{{ $t("共优惠") }}</view>
                    <view class="item-value discounts-value" style="color: var(--general)">
                        -
                        <format-price :show-text="false" :is-bottom="false" :price-data="total.discounts" />
                    </view>
                </view>
                <view v-if="total.discountCouponAmount > 0" class="discount-item-box">
                    <view class="item-label">{{ $t("优惠券") }}</view>
                    <view class="item-value discounts-value">
                        -
                        <format-price :show-text="false" :is-bottom="false" :price-data="total.discountCouponAmount" />
                    </view>
                </view>
                <view v-if="total.discountDiscountAmount > 0" class="discount-item-box">
                    <view class="item-label">{{ $t("其他优惠") }}</view>
                    <view class="item-value discounts-value">
                        -
                        <format-price :show-text="false" :is-bottom="false" :price-data="total.discountDiscountAmount" />
                    </view>
                </view>
                <view class="line" />
                <view class="discount-item-box discount-total">
                    <view class="item-label fontWeight">{{ $t("合计") }}</view>
                    <view class="item-value">
                        <format-price :show-text="false" :is-bottom="false" :price-data="total.discountAfter || total.totalAmount" :font-style="{}" />
                    </view>
                </view>
                <view v-if="!discountType" class="discount-desc">{{ $t("以上优惠不包含运费，实际优惠金额请以确认订单页为准") }}</view>
            </view>
        </view>
    </tig-popup>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { staticResource } from "@/utils";

const props = defineProps({
    modelValue: {
        type: Boolean,
        default: false
    },
    total: {
        type: Object,
        default: () => ({})
    },
    discountType: {
        type: String,
        default: ""
    }
});

const emit = defineEmits(["update:modelValue"]);
const showDiscountInfo = computed({
    get() {
        return props.modelValue;
    },
    set(value) {
        emit("update:modelValue", value);
    }
});

// 购物车传「已勾选件数」，订单确认页只有 totalCount，取不到则为 0（模板里不渲染件数）
const itemCount = computed(() => {
    const t = (props.total || {}) as { checkedCount?: number; totalCount?: number };
    return Number(t.checkedCount ?? t.totalCount ?? 0) || 0;
});
</script>

<style lang="scss" scoped>
.fontWeight {
    font-weight: 700;
}

/* 负边距把分隔线拉到弹层左右边缘 */
.line {
    height: 1px;
    margin-left: -28rpx;
    margin-right: -28rpx;
    background-color: #f5f6fa;
}

.discount {
    min-height: 60vh;
    border-radius: 28rpx 28rpx 0 0;
    overflow: hidden;
}

.discount-title {
    display: flex;
    justify-content: center;
    padding: 35rpx 28rpx 28rpx;
    background-color: #fff;

    .title-text {
        flex: 1;
        display: flex;
        justify-content: center;
        font-size: 32rpx;
        line-height: 42rpx;
        font-weight: 700;
    }

    .close-icon {
        display: flex;
        align-items: center;

        image {
            width: 35rpx;
            height: 35rpx;
        }
    }
}

.discount-content {
    padding: 28rpx;
    padding-top: 0;
    font-size: 24.5rpx;
    .discount-item-box {
        display: flex;
        align-items: center;
        justify-content: space-between;
        height: 60rpx;

        &.discount-total {
            height: 84rpx;
        }

        .item-label-text {
            font-weight: 400;
            margin-left: 7rpx;
        }

        .item-value {
            font-size: 24.5rpx;
        }

        &:last-child {
            padding-top: 0;
        }
    }

    .discount-desc {
        font-size: 21rpx;
        color: #969799;
        letter-spacing: 0;
    }
}
</style>
