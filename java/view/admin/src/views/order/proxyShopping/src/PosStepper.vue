<template>
    <div class="cart-num-box flex" :class="{ small: size === 'small' }">
        <template v-if="cartNum > 1 || !showDelete">
            <div class="min btn flex algin-center" :class="{ disabled: cartNum <= 1 }" @click="step('min')">
                <el-icon class="stepper-icon"><Minus /></el-icon>
            </div>
        </template>
        <template v-if="showDelete && cartNum <= 1">
            <div class="delete btn flex algin-center" @click="removeCartItem">
                <el-icon class="stepper-icon delete-icon"><Delete /></el-icon>
            </div>
        </template>
        <div class="num-box" @click="handleInput">
            <input ref="inputRef" type="text" :value="cartNum" @input="onInput" @change="onChange" />
        </div>
        <div class="plus btn flex algin-center" :class="{ disabled: cartNum >= max }" @click="step('add')">
            <el-icon class="stepper-icon"><Plus /></el-icon>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { computed, nextTick, PropType, ref, useTemplateRef } from "vue";
import { message } from "ant-design-vue";
import { Delete, Minus, Plus } from "@element-plus/icons-vue";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const emit = defineEmits(["change", "update:modelValue", "removeCartItem"]);

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
        default: 1999999
    },
    size: {
        type: String as PropType<"small" | "default">,
        default: "default"
    },
    showDelete: {
        type: Boolean,
        default: true
    }
});

const isFocus = ref(false);
const inputRef = useTemplateRef<HTMLInputElement>("inputRef");

const handleInput = async () => {
    if (props.disabled) return;
    isFocus.value = true;
    await nextTick();
    inputRef.value?.focus();
};

const cartNum = computed({
    get: () => props.modelValue,
    set: (value) => {
        emit("update:modelValue", value);
    }
});

const step = (data: string) => {
    if (props.disabled) return;
    if (data == "min" && cartNum.value > 1) {
        emit("update:modelValue", props.modelValue - 1);
        emit("change");
    } else if (data == "add") {
        if (props.max && props.modelValue >= props.max) {
            emit("update:modelValue", props.max);
            message.error(t('库存不足'));
            return;
        }
        emit("update:modelValue", props.modelValue + 1);
        emit("change");
    }
};

const removeCartItem = () => {
    emit("removeCartItem");
};

// 输入时过滤非数字字符
const onInput = (event: Event) => {
    const input = event.target as HTMLInputElement;
    input.value = input.value.replace(/\D/g, "");
    if (props.max && parseInt(input.value) > props.max) {
        input.value = props.max.toString();
        message.error(t('库存不足'));
    }
};

const onChange = (event: Event) => {
    const input = event.target as HTMLInputElement;
    let num = input.value;

    if (num === "" || num === null) {
        num = "1";
    } else {
        num = parseInt(num, 10).toString();

        if (isNaN(parseInt(num)) || parseInt(num) < 1) {
            num = "1";
        }
    }

    input.value = num;

    emit("update:modelValue", parseInt(num, 10));
    emit("change");
};
</script>

<style lang="less" scoped>
.cart-num-box {
    display: flex;
    border-radius: 4px;
    align-items: center;
    --stepper-height: 30px;

    &.small {
        --stepper-height: 25px;

        .stepper-icon {
            font-size: 16px;
        }

        .delete {
            .stepper-icon {
                font-size: 14px;
            }
        }

        .num-box {
            input {
                width: 30px;
            }
        }
    }

    .min {
        background-color: #f35e25;
    }
    .delete {
        background-color: #f35e25;
    }

    .plus {
        background-color: #fb9c1c;
    }

    .stepper-icon {
        font-size: var(--tig-pos-font-size-base);
        font-weight: bold;
        color: #fff;
        line-height: 1;
        display: inline-flex;
        align-items: center;
        justify-content: center;
    }

    .btn {
        display: flex;
        overflow: hidden;
        cursor: pointer;
        text-align: center;
        width: var(--stepper-height);
        border-radius: 50%;
        height: var(--stepper-height);
        align-items: center;
        justify-content: center;

        &.disabled {
            background-color: #d9dfe9;
            cursor: not-allowed;
        }
    }

    .num-box {
        padding: 0 4px;
        overflow: hidden;

        input {
            border: none;
            padding: 0;
            outline: medium none;
            text-align: center;
            width: 36px;
            font-size: var(--tig-pos-font-size-base);
            height: var(--stepper-height);
        }
    }
}
</style>


