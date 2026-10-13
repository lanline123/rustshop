<template>
    <div class="scan-barcode-input" :class="{ disabled: props.disabled }">
        <el-input
            ref="inputRef"
            v-model="innerValue"
            :disabled="props.disabled"
            :placeholder="props.placeholder"
            :clearable="props.clearable"
            @keydown.enter.prevent.stop="onConfirm"
        >
            <template #prefix>
                <slot name="prefix">
                    <span class="iconfont-admin icon-saoyisao"></span>
                </slot>
            </template>
        </el-input>
        <el-button
            class="confirm-btn"
            type="primary"
            size="small"
            :loading="props.loading"
            :disabled="props.disabled"
            @click="onConfirm"
        >
            {{ props.buttonText }}
        </el-button>
    </div>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";

const props = defineProps({
    modelValue: { type: String, default: "" },
    placeholder: { type: String, default: "请输入条码" },
    buttonText: { type: String, default: "确定" },
    disabled: { type: Boolean, default: false },
    loading: { type: Boolean, default: false },
    clearable: { type: Boolean, default: true }
});

const emit = defineEmits<{
    (e: "update:modelValue", v: string): void;
    (e: "confirm"): void;
}>();

const inputRef = ref<any>();

const innerValue = computed({
    get: () => props.modelValue,
    set: (v: string) => emit("update:modelValue", v)
});

const onConfirm = () => {
    emit("confirm");
};

const focus = () => inputRef.value?.focus?.();
defineExpose({ focus });
</script>

<style lang="less" scoped>
.scan-barcode-input {
    display: flex;
    align-items: center;
    gap: 8px;
    width: 260px;
    height: 32px;
    box-sizing: border-box;
    border: 1px solid var(--el-border-color);
    border-radius: 4px;
    padding: 3px;
    background: #fff;

    :deep(.el-input) {
        flex: 1;
        min-width: 0;
    }

    :deep(.el-input__wrapper) {
        box-shadow: none !important;
        background: transparent;
        padding: 0 6px;
        height: 100%;
    }

    :deep(.el-input__wrapper.is-focus) {
        box-shadow: none !important;
    }

    :deep(.el-input__inner) {
        height: 100%;
        line-height: 1;
    }

    .confirm-btn {
        flex-shrink: 0;
        height: 100%;
        padding: 0 10px;
        border-radius: 4px;
    }
}

.scan-barcode-input.disabled {
    background: var(--el-disabled-bg-color);
}
</style>

