<template>
    <view class="tig-number-step flex">
        <view class="minus btn flex align-center" :class="{ del: value <= step, disabled: disabled }" @touchstart.stop="" @tap.stop="onMinus" />
        <view class="value flex align-center">
            <input :value="displayValue" :disabled="disabled" type="number" @focus="onFocus" @input="onInput" @blur="onBlur" />
        </view>
        <view class="plus btn flex align-center" :class="{ disabled: disablePlus }" @touchstart.stop="" @tap.stop="onPlus" />
    </view>
</template>

<script lang="ts" setup>
/**
 * @description 购物车批发行数量步进器（外观复用零售 tig-number-stepper，注入批发倍数逻辑）
 *
 * 集中购物车批发改量交互，页面只需 `v-model:value` + 监听 `change`/`delete`：
 * - 步长 = `multiple`（≥1，=1 退化普通 ±1）
 * - 点 +/-：基于当前值「先吸附后步进」（复用 useWholesaleQuantity，解决旧倍数遇新倍数）
 * - **减到 0 走删除**：减/手输得到 0 时 emit `delete` 交父级弹确认（减号在只剩一个步长时显示删除图标）
 * - 手输：聚焦走本地文本不打断，失焦静默吸附到最近合法倍数（得 0 同样触发删除）
 */
// #ifdef MP-WEIXIN
defineOptions({
    options: { virtualHost: true }
});
// #endif
import { computed, ref } from "vue";
import { useI18n } from "vue-i18n";
import { useWholesaleQuantity } from "@/hooks/useWholesaleQuantity";

const { t } = useI18n();
const wq = useWholesaleQuantity();

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
    /** 库存上限；null/undefined=不限制 */
    max: {
        type: Number,
        default: null as unknown as number
    },
    /** 整体禁用（售罄 / 已下架） */
    disabled: {
        type: Boolean,
        default: false
    }
});

const emit = defineEmits<{
    (e: "update:value", value: number): void;
    (e: "change", value: number): void;
    (e: "delete"): void;
}>();

/** 聚焦态用本地文本，允许临时为空；失焦回显父级吸附后的值 */
const focused = ref(false);
const inputText = ref("");
const displayValue = computed(() => (focused.value ? inputText.value : String(props.value)));

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
        uni.showToast({ title: t("库存不足"), icon: "none" });
        return;
    }
    commitChange(target);
};

const onFocus = () => {
    focused.value = true;
    inputText.value = props.value > 0 ? String(props.value) : "";
};

const onInput = (e: any) => {
    inputText.value = String(e.detail?.value ?? "");
};

const onBlur = () => {
    focused.value = false;
    const raw = Number(inputText.value);
    const next = Number.isFinite(raw) ? wq.normalize(raw, props.multiple, props.max) : 0;
    inputText.value = "";
    if (next <= 0) {
        emit("delete");
        return;
    }
    if (next !== props.value) commitChange(next);
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
        /* #ifndef APP-NVUE */
        touch-action: none;
        /* #endif */

        &.del {
            background-image: url("@/static/images/common/delete.png");
        }

        &.disabled {
            background-image: url("@/static/images/common/minus2.png");
        }
    }

    .plus {
        background-image: url("@/static/images/common/plus.png");

        /* #ifndef APP-NVUE */
        touch-action: none;
        /* #endif */

        &.disabled {
            background-image: url("@/static/images/common/plus2.png");
        }
    }

    .btn {
        width: 47rpx;
        justify-content: center;
        background-repeat: no-repeat;
        background-position: 50% 50%;
        background-size: 21rpx 21rpx;
    }
}
</style>
