<template>
    <view class="payment-box">
        <view class="payment-title">{{ $t("支付方式") }}</view>
        <view class="payment-content">
            <template v-for="(item, index) in availablePaymentType" :key="item.typeId">
                <view v-if="item.isShow" class="payment-item" :class="{ disabled: item.disabled }" @click="handlePaymentType(index, item.disabled)">
                    <view class="payment-radio" :class="{ active: getActive(index, item.typeId) }" />
                    <view class="payment-name">{{ item?.typeName }}</view>
                </view>
            </template>
        </view>
    </view>
</template>

<script setup lang="ts">
/**
 * 支付方式选择组件
 * - 平铺单选展示可用支付方式，选中即回传（与跨境版一致）
 */
import { ref, watch } from "vue";
import type { AvailablePaymentType } from "@/types/order/check";

interface Props {
    availablePaymentType?: AvailablePaymentType[];
    payTypeId?: number;
}
const props = withDefaults(defineProps<Props>(), {
    availablePaymentType: () => [],
    payTypeId: 0
});
const emit = defineEmits(["update:payTypeId", "change"]);

const currentIndex = ref<null | number>(null);

// 已选支付方式被接口置为不可用时，清空选择并提示原因
watch(
    () => props.availablePaymentType,
    (list) => {
        const disabledItem = list.find((item) => item.disabled && item.typeId === props.payTypeId);
        if (!disabledItem) return;
        emit("update:payTypeId", 0);
        currentIndex.value = null;
        uni.showToast({
            title: disabledItem.disabledDesc,
            duration: 1500,
            icon: "none"
        });
    },
    { deep: true }
);

const getActive = (index: number, id: number) => {
    if (currentIndex.value !== null && currentIndex.value !== undefined && currentIndex.value >= 0) {
        return currentIndex.value === index;
    } else {
        return id === props.payTypeId;
    }
};
const handlePaymentType = (index: number, disabled: boolean) => {
    if (disabled) return;
    currentIndex.value = index;
    emit("update:payTypeId", props.availablePaymentType[index].typeId);
    emit("change");
};
</script>

<style lang="scss" scoped>
.payment-box {
    border-radius: 0;
    background: #fff;
    margin-bottom: 14rpx;
    padding: 20rpx 28rpx;
    font-size: 24.5rpx;
    display: flex;
    flex-direction: column;
    row-gap: 21rpx;
    color: #1a1a1a;
}

.payment-title {
    font-size: 28rpx;
    font-weight: 700;
}

.payment-content {
    display: flex;
    flex-direction: column;
    row-gap: 21rpx;

    .payment-item {
        display: flex;
        align-items: center;
        column-gap: 21rpx;

        &.disabled {
            color: #bbb;

            .payment-radio {
                border-color: #bbb;
            }
        }
    }

    .payment-radio {
        width: 14px;
        height: 14px;
        border-radius: 50%;
        border: 1.5px solid #525252;
        position: relative;
        flex-shrink: 0;

        &.active {
            border-color: var(--general);

            &::after {
                content: "";
                position: absolute;
                width: 7px;
                height: 7px;
                background-color: var(--general);
                border-radius: 50%;
                top: 50%;
                left: 50%;
                transform: translate(-50%, -50%);
            }
        }
    }
}
</style>
