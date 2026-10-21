<template>
    <div class="tig-input-wrapper" :class="classType" :style="{ width }">
        <el-input
            ref="inputRef"
            :type="type == 'password' || type == 'textarea' ? type : 'text'"
            :name="name"
            :rows="rows"
            v-model="modelValue"
            :disabled="disabled"
            :placeholder="placeholder"
            :clearable="clearable"
            :readonly="readonly"
            :maxlength="maxlength"
            :show-word-limit="showWordLimit"
            :word-limit-position="wordLimitPosition"
            :min="min"
            :max="max"
            :size="size"
            :prefix-icon="prefixIcon"
            :suffix-icon="suffixIcon"
            :autosize="autosize"
            @focus="onFocus"
            @blur="onBlur"
            @input="onInput"
            @clear="onClear"
            @keyup.enter="keyupEnter"
            style="width: 100%"
        >
            <template #append v-if="$slots.append"> <slot name="append"></slot> </template>
            <template #prepend v-if="$slots.prepend"> <slot name="prepend"></slot> </template>
            <template #suffix v-if="$slots.suffix"> <slot name="suffix"></slot> </template>
            <template #prefix v-if="$slots.prefix"> <slot name="prefix"></slot> </template>
        </el-input>
    </div>
</template>
<script lang="ts" setup>
import { ref, PropType, VNode, computed } from "vue";
//**
//  TigInput
//  TigInput classType="tig-form-input"
//  TigInput classType="tig-form-input" type="integer"
//  TigInput classType="tig-form-input" type="decimal"
//  TigInput type="decimal"
//  TigInput type="integer"
//  */

const inputRef = ref();
// 定义props
const props = defineProps({
    classType: {
        // 自定义class tig-form-input 用于表单输入框 tig-input 用于普通输入框(列表筛选项)
        type: String as () => "tig-input" | "tig-form-input",
        default: ""
    },
    type: {
        // 输入框类型 integer 整数 decimal 小数
        type: String as () => "text" | "password" | "decimal" | "integer" | "textarea",
        default: "text"
    },
    decimalPlaces: {
        // 小数位数（当 fixedDecimal 为 true 时强制显示固定位数，为 false 时表示最大小数位数）
        type: Number,
        default: 2
    },
    fixedDecimal: {
        // 是否强制显示固定小数位数，false 时只限制最大小数位但不补零
        type: Boolean,
        default: true
    },
    allowNegative: {
        // 是否允许负数 type为decimal | integer时有效
        type: Boolean,
        default: false
    },
    prefixIcon: {
        type: [String, Function, null, Object] as PropType<string | (() => VNode) | null>,
        default: null
    },
    suffixIcon: {
        type: [String, Function, null, Object] as PropType<string | (() => VNode) | null>,
        default: null
    },
    min: {
        // 最小值
        type: [Number, null] as PropType<number | null>,
        default: null
    },
    max: {
        // 最大值
        type: [Number, null] as PropType<number | null>,
        default: null
    },
    maxlength: {
        type: Number,
        default: 999999
    },
    width: {
        // 自定义宽度
        type: String,
        default: ""
    },
    readonly: {
        type: Boolean,
        default: false
    },
    disabled: {
        type: Boolean,
        default: false
    },
    clearable: {
        type: Boolean,
        default: false
    },
    showWordLimit: {
        type: Boolean,
        default: false
    },
    wordLimitPosition: {
        type: String,
        default: "inside"
    },
    placeholder: {
        type: String,
        default: ""
    },
    name: {
        type: String,
        default: ""
    },
    rows: {
        type: Number,
        default: 3
    },
    size: {
        type: String,
        default: "default"
    },
    autosize: {
        type: [Object, Boolean] as PropType<{ minRows?: number; maxRows?: number } | boolean>,
        default: false
    },
    autoTrim: {
        // 自动去除首尾空格
        type: Boolean,
        default: true
    },
    formatter: {
        // 自定义格式化函数
        type: Function as PropType<(value: string) => string>,
        default: null
    },
    parser: {
        // 自定义解析函数
        type: Function as PropType<(value: string | number) => string | number>,
        default: null
    },
    formatTrigger: {
        // 触发格式化的时机
        type: Array as PropType<Array<"blur" | "input">>,
        default: () => ["blur"]
    },
    allowEmptyDecimal: {
        // integer/decimal 类型允许保持空值
        type: Boolean,
        default: false
    }
});
const emit = defineEmits(["update:modelValue", "clear", "keyupEnter", "blur", "input", "focus"]);
const modelValue = defineModel<any>("modelValue", { default: "" });
const shouldFormatOnInput = computed(() => props.formatTrigger.includes("input"));
const clampWithMinMax = (numValue: number) => {
    let next = numValue;
    if (props.min !== null && props.min !== undefined) {
        next = Math.max(props.min, next);
    }
    if (props.max !== null && props.max !== undefined) {
        next = Math.min(props.max, next);
    }
    return next;
};
const sanitizeNumber = (value: string | number): string => {
    let normalized = value ?? "";
    if (typeof normalized !== "string") {
        normalized = String(normalized);
    }
    if (props.type === "integer") {  // 整数类型  
        normalized = normalized.replace(/[^\d-]/g, "");
        if (!normalized && props.allowEmptyDecimal) {
            return "";
        }
        let numValue = normalized ? parseInt(normalized, 10) : 0;
        if (!props.allowNegative) {
            numValue = Math.max(0, numValue);
        }
        numValue = clampWithMinMax(numValue);
        normalized = String(Number.isFinite(numValue) ? numValue : 0);
    }
    if (props.type === "decimal") {  // 小数类型  
        normalized = normalized.replace(/[^\d.-]/g, "");
        const parts = normalized.split(".");
        if (parts.length > 2) {
            normalized = `${parts.shift()}.${parts.join("")}`;
        }
        if (parts.length > 1 && props.decimalPlaces >= 0) {
            normalized = `${parts[0]}.${parts[1].slice(0, props.decimalPlaces)}`;
        }
        if (!normalized && props.allowEmptyDecimal) {
            return "";
        }
        let numValue = normalized ? parseFloat(normalized) : 0;
        if (!props.allowNegative) {
            numValue = Math.max(0, numValue);
        }
        if (!Number.isFinite(numValue)) {
            numValue = 0;
        }
        numValue = clampWithMinMax(numValue);
        if (props.decimalPlaces === -1) {
            normalized = String(numValue);
        } else if (props.decimalPlaces === 0) {
            normalized = String(Math.trunc(numValue));
        } else if (props.fixedDecimal) {
            // 强制显示固定小数位数
            normalized = numValue.toFixed(props.decimalPlaces);
        } else {
            // 只限制最大小数位，不强制补零
            const factor = Math.pow(10, props.decimalPlaces);
            normalized = String(Math.round(numValue * factor) / factor);
        }
    }

    const isNumberType = props.type === "integer" || props.type === "decimal";
    const hasIllegalChar = /[\u4e00-\u9fa5]/.test(normalized) || /[^\d.-]/.test(normalized);
    if (isNumberType && hasIllegalChar) {
        normalized = "0";
    }
    if (props.type === "integer" && !normalized && !props.allowEmptyDecimal) {
        normalized = "0";
    }
    return normalized;
};
const formatPipeline = (value: string | number) => {
    let nextValue: string | number = value ?? "";
    if (props.autoTrim && typeof nextValue === "string") {
        nextValue = nextValue.trim();
    }
    if (props.parser) {
        nextValue = props.parser(nextValue);
    }
    if (props.type === "integer" || props.type === "decimal") {
        nextValue = sanitizeNumber(nextValue);
    } else if (typeof nextValue !== "string") {
        nextValue = String(nextValue);
    }
    if (props.formatter) {
        nextValue = props.formatter(String(nextValue));
    }
    return String(nextValue);
};
const syncModelValue = (trigger: "blur" | "input") => {
    const value = formatPipeline(modelValue.value);
    emit("update:modelValue", value);
    if (trigger === "blur") {
        emit("blur");
    } else {
        emit("input");
    }
};
const onBlur = () => {
    syncModelValue("blur");
};
const keyupEnter = () => {
    emit("keyupEnter");
};
const onClear = () => {
    emit("clear");
};
const onInput = () => {
    if (shouldFormatOnInput.value) {
        syncModelValue("input");
        return;
    }
    emit("input");
};
const onFocus = () => {
    emit("focus");
};
const focus = () => inputRef.value?.focus?.();
const blur = () => inputRef.value?.blur?.();
const clear = () => inputRef.value?.clear?.();
defineExpose({ focus, blur, clear });
</script>
<style scoped lang="less">
.tig-input {
    width: 100%;
}
.tig-form-input {
    width: 450px;
}
@media (max-width: 768px) {
    .tig-input,
    .tig-form-input {
        width: 80%;
    }
    .tig-input-wrapper {
        width: 100% !important;
    }
}
</style>
