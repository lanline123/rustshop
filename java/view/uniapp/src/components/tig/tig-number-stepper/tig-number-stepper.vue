<template>
    <view class="tig-number-step flex">
        <view
            class="minus btn flex align-center"
            :class="{ del: currentValue <= 1 && isDelete, disabled: currentValue <= 1 && !isDelete }"
            @click.stop="step('minus')"
        />
        <view class="value flex align-center">
            <input :value="displayValue" type="number" placeholder="" @focus="onFocus" @input="onInput" @blur="onBlur" />
        </view>
        <view class="plus btn flex align-center" :class="{ disabled: currentValue >= max }" @click.stop="step('add')" />
    </view>
</template>

<script lang="ts" setup>
// #ifdef MP-WEIXIN
defineOptions({
    options: { virtualHost: true }
});
// #endif
import { computed, nextTick, ref, watch, getCurrentInstance } from "vue";
import { useI18n } from "vue-i18n";

const { t } = useI18n();

const instance = getCurrentInstance();

const props = defineProps({
    modelValue: {
        type: Number,
        default: 1
    },
    disabled: {
        type: Boolean,
        default: false
    },
    max: {
        type: Number,
        default: 100
    },
    min: {
        type: Number,
        default: 1
    },
    integer: {
        type: Boolean,
        default: true
    },
    isDelete: {
        type: Boolean,
        default: false
    }
});

const emit = defineEmits(["change", "update:modelValue", "delete"]);

const format = (value: string | number) => {
    value = filter(value);

    value = value === "" ? 0 : +value;

    value = Math.max(Math.min(props.max, value), props.min);

    return value;
};

const filter = (value: string | number) => {
    value = String(value).replace(/[^0-9.-]/g, "");

    if (props.integer && value.indexOf(".") !== -1) {
        value = value.split(".")[0];
    }

    return value;
};

const currentValue = ref(format(props.modelValue));
const focused = ref(false);
const inputText = ref("");
const displayValue = computed(() => (focused.value ? inputText.value : String(currentValue.value)));

watch(
    () => props.modelValue,
    (newValue) => {
        if (newValue !== currentValue.value) {
            currentValue.value = format(newValue);
        }
        if (focused.value) {
            const textNum = Number(inputText.value);
            if (inputText.value !== "" && Number.isFinite(textNum) && textNum === newValue) return;
            inputText.value = String(currentValue.value);
        }
    },
    { immediate: true }
);

const add = (num1: number, num2: number) => {
    const cardinal = Math.pow(10, 10);
    return Math.round((num1 + num2) * cardinal) / cardinal;
};

const step = (type: string) => {
    if (currentValue.value <= props.min && type === "minus") {
        if (props.isDelete) {
            emit("delete");
        }

        return;
    }
    if (currentValue.value >= props.max && type === "add") {
        uni.showToast({
            icon: "none",
            title: t("库存不足")
        });
        return;
    }

    const diff = type === "minus" ? -1 : +1;
    const value = format(add(+currentValue.value, diff));
    emitChange(value, type);
};

const onFocus = () => {
    focused.value = true;
    inputText.value = String(currentValue.value);
};

const onInput = (e: any) => {
    const value = String(e.detail?.value ?? e.target?.value ?? "");
    inputText.value = value;

    if (value === "") return;

    const formatted = format(value);
    emitChange(formatted);
};

const onBlur = () => {
    focused.value = false;
    const next = inputText.value === "" ? props.min : format(inputText.value);
    inputText.value = "";
    if (next === currentValue.value) return;
    emitChange(next);
};

const emitChange = async (value: number, type = "") => {
    await nextTick();
    emit("update:modelValue", value);
    currentValue.value = value;
    instance?.proxy?.$forceUpdate();
    emit("change", {
        value,
        type
    });
};
</script>

<style lang="scss" scoped>
.tig-number-step {
    background: #f7f8fc;
    border-radius: 4px;

    .value {
        width: 61rpx;
        height: 47rpx;
        display: flex;
        align-items: center;

        input {
            text-align: center;
            height: 100%;
        }
    }

    .minus {
        background-image: url("@/static/images/common/minus.png");

        &.del {
            background-image: url("@/static/images/common/delete.png");
        }

        &.disabled {
            background-image: url("@/static/images/common/minus2.png");
        }
    }

    .plus {
        background-image: url("@/static/images/common/plus.png");

        &.disabled {
            background-image: url("@/static/images/common/plus2.png");
        }
    }

    .btn {
        width: 47rpx;
        height: 47rpx;
        justify-content: center;
        background-repeat: no-repeat;
        background-position: 50% 50%;
        background-size: 21rpx 21rpx;
        /* 扩大可点区域，避免小程序端点击命中困难 */
        position: relative;
        z-index: 1;
    }
}
</style>
