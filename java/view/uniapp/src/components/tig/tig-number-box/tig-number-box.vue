<template>
    <up-number-box
        v-model="number"
        :icon-style="iconStyle"
        :button-size="buttonSize"
        :min="min"
        :max="max"
        :input-width="inputWidth"
        :disabled="disabled"
        @overlimit="overlimit"
        @change=""
    >
        <template #input>
            <input
                class="u-number-box__input number-box-input"
                type="number"
                :disabled="disabled"
                :value="displayValue"
                :style="inputStyle"
                @focus="onFocus"
                @input="onInput"
                @blur="onBlur"
            />
        </template>
    </up-number-box>
</template>

<script setup lang="ts">
import { computed, ref, watch } from "vue";

const props = defineProps({
    modelValue: {
        type: Number,
        default: 1
    },
    max: {
        type: Number,
        default: 99
    },
    min: {
        type: Number,
        default: 1
    },
    inputWidth: {
        type: [String, Number],
        default: 35
    },
    buttonSize: {
        type: [String, Number],
        default: 30
    },
    buttonWidth: {
        type: [String, Number],
        default: 35
    },
    iconStyle: {
        type: String,
        default: ""
    },
    disabled: {
        type: Boolean,
        default: false
    }
});
const emit = defineEmits(["update:modelValue", "overlimit", "change"]);

/** 聚焦时可清空；失焦且未输入时回填最小值 */
const focused = ref(false);
const inputText = ref("");

const clampValue = (value: number) => {
    const min = Number(props.min);
    const max = Number(props.max);
    return Math.max(Math.min(max, value), min);
};

const readInputValue = (e: any) => {
    if (typeof e?.detail?.value === "string") return e.detail.value;
    if (typeof e?.target?.value === "string") return e.target.value;
    return "";
};

const displayValue = computed(() => (focused.value ? inputText.value : String(props.modelValue ?? props.min)));

const number = computed({
    get() {
        return props.modelValue;
    },
    set(value) {
        emit("update:modelValue", value);
        emit("change", value);
    }
});

watch(
    () => props.modelValue,
    (val) => {
        if (!focused.value) return;
        const textNum = Number(inputText.value);
        if (inputText.value !== "" && Number.isFinite(textNum) && textNum === val) return;
        inputText.value = val == null ? "" : String(val);
    }
);

const onFocus = () => {
    focused.value = true;
    inputText.value = props.modelValue == null ? "" : String(props.modelValue);
};

const onInput = (e: any) => {
    const value = readInputValue(e).replace(/[^\d]/g, "");
    inputText.value = value;
    if (value === "") return;
    const num = Number(value);
    if (!Number.isFinite(num)) return;
    number.value = clampValue(num);
};

const onBlur = () => {
    focused.value = false;
    const raw = Number(inputText.value);
    const next = inputText.value !== "" && Number.isFinite(raw) ? clampValue(raw) : Number(props.min);
    inputText.value = "";
    if (next === props.modelValue) return;
    number.value = next;
};

const overlimit = (val: any) => {
    emit("overlimit", val);
};

const getButtonWidth = computed(() => {
    return props.buttonWidth + "px";
});

const inputStyle = computed(() => ({
    width: `${props.inputWidth}px`,
    height: `${props.buttonSize}px`
}));
</script>

<style lang="scss" scoped>
.u-number-box {
    :deep(.u-number-box__plus) {
        width: v-bind(getButtonWidth);
    }
    :deep(.u-number-box__minus) {
        width: v-bind(getButtonWidth);
    }
}

.number-box-input {
    text-align: center;
    font-size: 15px;
    padding: 0;
    margin: 0 2px;
    color: #323233;
    background-color: #ebecee;
    box-sizing: border-box;
}
</style>
