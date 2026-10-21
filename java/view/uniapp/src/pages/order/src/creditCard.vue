<template>
    <!-- 账期支付：授信客户有账期信息时展示；不可用时显示禁用原因（接口 creditPaymentInfo 为空则不展示） -->
    <view v-if="creditPaymentInfo" class="credit">
        <view class="credit-title">
            {{ $t("账期") }}
            <view v-if="disabledText" class="credit-text disabledText">{{ disabledText }}</view>
            <view v-else class="credit-text">
                {{ $t("账期剩余额度") }}：<text class="special-text">{{ creditPaymentInfo.availableAmount }}</text>
            </view>
        </view>
        <view class="credit-switch-wrap">
            <switch class="credit-switch" :checked="isCredit" color="var(--general)" @change="handleCredit" />
            <view v-if="isDisabled" class="switch-mask" />
        </view>
    </view>
</template>

<script setup lang="ts">
/**
 * 账期支付卡片组件（o2o/B2B 独有，vortmall 蓝本无此能力）
 * - 展示账期剩余额度或不可用原因
 * - 开关控制是否使用账期支付（与余额互斥，由结算页协调）
 */
import { computed, ref } from "vue";
import { useI18n } from "vue-i18n";
import type { CreditPaymentInfo } from "@/types/order/check";

const { t } = useI18n();

interface Props {
    /** 账期支付信息（非授信会员返回 null） */
    creditPaymentInfo?: CreditPaymentInfo | null;
}

const props = defineProps<Props>();

const emit = defineEmits(["sendCreditStatus"]);

const isCredit = ref(false);

const isDisabled = computed(() => {
    return [2, 3].includes(props.creditPaymentInfo?.creditPaymentStatus as number);
});

const disabledText = computed(() => {
    if (props.creditPaymentInfo?.creditPaymentStatus === 2) {
        return t("您有账期逾期待结算订单，敬请及时处理");
    } else if (props.creditPaymentInfo?.creditPaymentStatus === 3) {
        return t("账期支付功能已关闭，详情请咨询平台方");
    } else {
        return "";
    }
});

const handleCredit = () => {
    isCredit.value = !isCredit.value;
    emit("sendCreditStatus", isCredit.value);
};

defineExpose({
    isCredit
});
</script>

<style lang="scss" scoped>
.credit {
    display: flex;
    align-items: center;
    justify-content: space-between;
    min-height: 56rpx;
    background-color: #fff;
    font-size: 24.5rpx;

    .credit-title {
        display: flex;
        flex: 1;
        min-width: 0;
        align-items: center;

        .credit-text {
            font-size: 22rpx;
            padding-left: 15rpx;
            display: flex;
            align-items: center;

            .special-text {
                color: var(--general);
            }
        }

        .disabledText {
            color: var(--general);
        }
    }
}

.credit-switch-wrap {
    position: relative;
    display: flex;
    align-items: center;
}

.credit-switch {
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

.switch-mask {
    position: absolute;
    inset: 0;
    z-index: 2;
}
</style>
