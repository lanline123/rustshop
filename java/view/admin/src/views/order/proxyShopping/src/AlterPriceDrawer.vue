<template>
    <div class="drawer-body">
        <div class="price-item">
            <div class="label">{{ $t('应付金额') }}</div>
            <div class="value payable">{{ priceFormat(safePayableAmount) }}</div>
        </div>

        <div class="price-item">
            <div class="label">{{ $t('改价方式') }}</div>
            <div class="change-box">
                <el-radio-group v-model="changeType">
                    <el-radio :value="1">{{ $t('一口价') }}</el-radio>
                    <el-radio :value="2">{{ $t('折扣') }}</el-radio>
                    <el-radio :value="3">{{ $t('减价') }}</el-radio>
                </el-radio-group>

                <el-input v-model="changeValueInput" class="change-input" type="number" @input="onInputValueInput"
                    @change="onInputChange">
                    <template #suffix>
                        <span>{{ unitMap[changeType] }}</span>
                    </template>
                </el-input>
            </div>
        </div>

        <div class="price-item" v-if="showChangedAmount">
            <div class="label">{{ $t('改价后金额') }}</div>
            <div class="value changed">{{ priceFormat(finalAmount) }}</div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { message } from "ant-design-vue";
import { priceFormat } from "@/utils/format";
import { previewProxyOrderChangePrice } from "@/api/proxyShopping/orderCheck";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const props = withDefaults(
    defineProps<{
        visible?: boolean;
        userId?: number;
        flowType?: number;
        deliveryType?: number;
        payableAmount?: number;
        originalPrice?: number;
        changedAmount?: number;
        hasChangedAmount?: boolean;
        initialType?: number;
        initialValue?: number | string;
    }>(),
    {
        visible: false,
        userId: 0,
        flowType: 1,
        deliveryType: 1,
        payableAmount: 0,
        originalPrice: 0,
        changedAmount: 0,
        hasChangedAmount: false,
        initialType: 1,
        initialValue: ""
    }
);

const emit = defineEmits<{
    (e: "submitCallback", payload: { type: number; value: number; finalAmount: number }): void;
    (e: "okType", val: boolean): void;
}>();

const changeType = ref<number>(1);
const changeValueInput = ref<string>("");

const unitMap: Record<number, string> = {
    1: t('元'),
    2: "%",
    3: t('元')
};

const safePayableAmount = computed(() => {
    const n = Number(props.payableAmount || 0);
    return Number.isFinite(n) && n > 0 ? n : 0;
});

const safeOriginalPrice = computed(() => {
    const n = Number(props.originalPrice || 0);
    if (Number.isFinite(n) && n > 0) return n;
    return safePayableAmount.value;
});

const normalizeValue = (value: number, type: number) => {
    if (!Number.isFinite(value)) return 0;
    if (type === 2) {
        if (value < 0) return 0;
        if (value > 100) return 100;
        return value;
    }
    if (type === 3) {
        if (value < 0) return 0;
        if (value > safePayableAmount.value) return safePayableAmount.value;
        return value;
    }
    return value < 0 ? 0 : value;
};

const parseInputValue = () => {
    if (String(changeValueInput.value).trim() === "") return null;
    const n = Number(changeValueInput.value);
    if (!Number.isFinite(n)) return null;
    return normalizeValue(n, changeType.value);
};

const safeChangedAmount = computed(() => {
    const n = Number(props.changedAmount || 0);
    return Number.isFinite(n) && n >= 0 ? n : 0;
});

const showChangedAmount = computed(() => {
    return parseInputValue() !== null || !!props.hasChangedAmount;
});

const finalAmount = computed(() => {
    const parsed = parseInputValue();
    if (parsed === null) {
        if (props.hasChangedAmount) return safeChangedAmount.value;
        return safePayableAmount.value;
    }
    const base = safeOriginalPrice.value;
    let amount = base;
    if (changeType.value === 1) amount = parsed;
    if (changeType.value === 2) amount = (base * parsed) / 100;
    if (changeType.value === 3) amount = base - parsed;
    if (!Number.isFinite(amount) || amount < 0) amount = 0;
    return Number(amount.toFixed(2));
});

const onInputChange = () => {
    const rawValue = Number(changeValueInput.value);
    if (changeType.value === 3 && Number.isFinite(rawValue) && rawValue > safePayableAmount.value) {
        message.warning(t('减价金额必须小于等于应付金额'));
    }
    const parsed = parseInputValue();
    if (parsed === null) return;
    changeValueInput.value = String(parsed);
};

const onInputValueInput = (value: any) => {
    const nextValue = String(value ?? "");
    if (nextValue.trim() === "") {
        changeValueInput.value = "";
        return;
    }

    const n = Number(nextValue);
    if (!Number.isFinite(n)) {
        changeValueInput.value = nextValue;
        return;
    }

    changeValueInput.value = String(normalizeValue(n, changeType.value));
};

const resetFromProps = () => {
    const type = Number(props.initialType || 1);
    changeType.value = type === 2 || type === 3 ? type : 1;
    const initial = props.initialValue;
    if (initial === undefined || initial === null || String(initial).trim() === "") {
        changeValueInput.value = "";
        return;
    }
    const n = normalizeValue(Number(initial), changeType.value);
    changeValueInput.value = Number.isFinite(n) ? String(n) : "";
};

const getPreviewAmountFromRes = (res: any) => {
    const totalAmount = Number(res?.total?.totalAmount);
    if (Number.isFinite(totalAmount) && totalAmount >= 0) return Number(totalAmount.toFixed(2));
    const payAmount = Number(res?.payAmount);
    if (Number.isFinite(payAmount) && payAmount >= 0) return Number(payAmount.toFixed(2));
    return finalAmount.value;
};

watch(
    () => props.visible,
    (visible) => {
        if (!visible) return;
        emit("okType", true);
        resetFromProps();
    },
    { immediate: true }
);

watch(changeType, () => {
    if (!props.visible) return;
    const parsed = parseInputValue();
    if (parsed === null) return;
    changeValueInput.value = String(normalizeValue(parsed, changeType.value));
});

const onFormSubmit = async () => {
    const value = parseInputValue();
    if (value === null) {
        message.warning(t('请输入改价值'));
        return;
    }
    const userId = Number(props.userId || 0);
    let confirmedFinalAmount = finalAmount.value;

    if (userId > 0) {
        try {
            const res = await previewProxyOrderChangePrice({
                userId,
                flowType: Number(props.flowType || 1),
                deliveryType: Number(props.deliveryType || 1),
                totalChangePriceType: changeType.value,
                changePriceValue: Number(value),
                originalPrice: safeOriginalPrice.value
            } as any);
            confirmedFinalAmount = getPreviewAmountFromRes(res);
        } catch (error: any) {
            message.error(error?.message || t('改价预览失败'));
            return;
        }
    }

    emit("submitCallback", { type: changeType.value, value, finalAmount: confirmedFinalAmount });
};

defineExpose({ onFormSubmit });
</script>

<style scoped lang="less">
.drawer-body {
    padding: 10px 4px;
}

.price-item {
    display: flex;
    align-items: flex-start;
    margin-bottom: 28px;

    .label {
        width: 90px;
        line-height: 32px;
        color: #303133;
        font-weight: 500;
    }

    .value {
        line-height: 32px;
        font-size: 32px;
        font-weight: 600;

        &.payable {
            color: #eeab03;
        }

        &.changed {
            color: #1458ff;
        }
    }
}

.change-box {
    flex: 1;

    :deep(.el-radio-group) {
        margin-bottom: 14px;
    }

    .change-input {
        width: 220px;

        :deep(.el-input__inner) {
            &::-webkit-outer-spin-button,
            &::-webkit-inner-spin-button {
                -webkit-appearance: none;
            }
            &[type="number"] {
                -moz-appearance: textfield;
            }
        }
    }
}

</style>
