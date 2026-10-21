<template>
    <div class="cart-num-box flex" :class="disabled ? 'disabled-box' : ''">
        <template v-if="value > step">
            <span class="min" @click="onMinus"></span>
        </template>
        <template v-else>
            <span class="delete" @click="onMinus"></span>
        </template>
        <input type="text" :value="value" :disabled="disabled" style="background: #f7f8fc" @input="onInput" @change="onChange" />
        <span class="plus" :class="{ disabled: disablePlus }" @click="onPlus"></span>
    </div>
</template>
<script lang="ts" setup>
/**
 * @description 购物车批发行数量步进器（外观复用零售 InputNumber，注入批发倍数逻辑）
 *
 * 集中购物车批发改量交互，页面只需 `v-model:value` + 监听 `change`/`delete`：
 * - 步长 = `multiple`（≥1，=1 退化普通 ±1）
 * - 点 +/-：基于当前值「先吸附后步进」（复用 useWholesaleQuantity，解决旧倍数遇新倍数）
 * - **减到 0 走删除**：减/手输得到 0 时 emit `delete` 交父级弹确认（减号在只剩一个步长时显示删除图标，与零售 InputNumber 一致）
 * - 手输：失焦静默吸附到最近合法倍数（得 0 同样触发删除）
 */
import { useWholesaleQuantity } from "@/hooks/useWholesaleQuantity";
import { message } from "ant-design-vue";

const { t } = useI18n();
const wq = useWholesaleQuantity();

const emit = defineEmits<{
    (e: "update:value", value: number): void;
    (e: "change", value: number): void;
    (e: "delete"): void;
}>();

const props = defineProps({
    /** 当前数量（通过 v-model:value 双向绑定） */
    value: {
        type: Number,
        default: 0
    },
    /** 倍数；1=不限制（普通 ±1） */
    multiple: {
        type: Number,
        default: 1
    },
    /** 库存上限；undefined=不限制 */
    max: {
        type: Number,
        default: undefined
    },
    /** 整体禁用（售罄 / 已下架） */
    disabled: {
        type: Boolean,
        default: false
    }
});

/** 步长 = multiple（≥1） */
const step = computed(() => wq.stepOf(props.multiple));

/** 到上限：再加超库存无合法值 → 禁用加号 */
const disablePlus = computed(() => props.disabled || wq.plusTarget(props.value, props.multiple, props.max) === null);

const commitChange = (next: number) => {
    emit("update:value", next);
    emit("change", next);
};

const onMinus = () => {
    if (props.disabled) return;
    const target = wq.minusTarget(props.value, props.multiple);
    if (target <= 0) {
        emit("delete");
        return;
    }
    commitChange(target);
};

const onPlus = () => {
    if (props.disabled) return;
    const target = wq.plusTarget(props.value, props.multiple, props.max);
    if (target == null) {
        message.error(t("库存不足"));
        return;
    }
    commitChange(target);
};

const onInput = (event: Event) => {
    const input = event.target as HTMLInputElement;
    input.value = input.value.replace(/\D/g, "");
};

const onChange = (event: Event) => {
    const input = event.target as HTMLInputElement;
    const raw = Number(input.value);
    const next = Number.isFinite(raw) ? wq.normalize(raw, props.multiple, props.max) : 0;
    if (next <= 0) {
        // 回填原值再交父级确认删除，避免取消删除后输入框残留空/0
        input.value = String(props.value);
        emit("delete");
        return;
    }
    input.value = String(next);
    if (next !== props.value) commitChange(next);
};
</script>
<style lang="less" scoped>
.cart-num-box {
    display: flex;
    background: #f7f8fc;
    border-radius: 4px;
    padding: 0 5px;
    height: 36px;

    .min {
        background: url("~/assets/images/common/min.png") no-repeat 50% / 16px 16px;

        &.disabled {
            background: url("~/assets/images/common/min-disabled.png") no-repeat 50% / 16px 16px;
        }
    }

    .plus {
        background: url("~/assets/images/common/plus.png") no-repeat 50% / 16px 16px;
        &.disabled {
            background: url("~/assets/images/common/plus-disabled.png") no-repeat 50% / 16px 16px;
        }
    }

    .delete {
        background: url("~/assets/images/common/delete.png") no-repeat 50% / 16px 16px;
    }

    span {
        display: block;
        overflow: hidden;
        cursor: pointer;
        text-align: center;
        width: 21px;
    }

    .disabled {
        color: #dfdfdf;
        cursor: default;
    }

    input {
        border: none;
        padding: 0;
        outline: medium none;
        text-align: center;
        width: 48px;
        font-size: 18px;
    }
}
.disabled-box {
    background-color: #eee;
}
</style>
