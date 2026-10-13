<template>
    <view class="wholesale-stepper">
        <view class="btn minus" :class="{ disabled: value <= 0 || disabled }" @click="handleMinus">
            <text class="iconfont-h5">−</text>
        </view>
        <input
            class="value"
            type="number"
            :value="displayValue"
            :disabled="disabled"
            confirm-type="done"
            @focus="handleFocus"
            @input="handleInput"
            @blur="handleBlur"
        />
        <view class="btn plus" :class="{ disabled: isPlusDisabled }" @click="handlePlus">
            <uni-icons type="plusempty" size="16" :color="isPlusDisabled ? '#ccc' : '#2a3145'" />
        </view>
    </view>
</template>

<script lang="ts" setup>
/**
 * @description 批发步进器（数量控件）
 *
 * 步进逻辑（步进器不强制起批量 moq，moq 仅在父级确认时校验）：
 * - 默认值 0（混批多 SKU 场景，0 = 不购买该 SKU，合法）
 * - 合法值集合：{0} ∪ {multiple 整数倍}（multiple=1 即任意正整数）
 * - 步进跳变：
 *   - `0 ──点+──▶ multiple`（multiple=1 时即 +1，不跳到 moq）
 *   - `n ──点+──▶ n+multiple`
 *   - `n ──点-──▶ n−multiple`（递减到 0）
 *   - `multiple=1` 退化为普通 ±1
 * - 手输失焦：静默吸附到最近的 multiple 倍数，不弹 Toast；吸附完成后 emit change 供父级拉金额
 * - +/- 点击：立即 emit change 供父级拉金额
 */
import { computed, ref } from "vue";
import { useI18n } from "vue-i18n";
import { useWholesaleQuantity } from "@/hooks/useWholesaleQuantity";

const { t } = useI18n();
const wq = useWholesaleQuantity();

const props = defineProps({
    /** 当前数量；通过 v-model:value 双向绑定 */
    value: {
        type: Number,
        default: 0
    },
    /**
     * 起订量门槛（来自 buyRule.skuRules[].moqSku 或 SPU 维度兜底）
     * SPU 维度单 SKU 通常 moqSku=0，从 0 点+ 即 `+multiple`，起订量校验在父组件做合计
     */
    moq: {
        type: Number,
        default: 0
    },
    /** 倍数；1=不限制（普通 ±1） */
    multiple: {
        type: Number,
        default: 1
    },
    /** 库存上限（封顶值）；null=不限制 */
    max: {
        type: Number,
        default: null as unknown as number
    },
    /** 整体禁用（如售罄 / 已下架） */
    disabled: {
        type: Boolean,
        default: false
    }
});

const emit = defineEmits<{
    (e: "update:value", value: number): void;
    (e: "change", value: number, prev: number): void;
}>();

/** 是否处于输入聚焦态：聚焦时输入框走本地文本，避免删除即变 0 / 受控回填导致数字跳变 */
const focused = ref(false);
/** 编辑期本地输入文本（不即时回传父级、不吸附）；完全失焦后再统一处理 */
const inputText = ref("");

/**
 * 输入框显示值
 * - 聚焦编辑中：用本地文本，允许临时为空
 * - 失焦：回显父级已吸附的合法值
 */
const displayValue = computed(() => (focused.value ? inputText.value : String(props.value)));

/**
 * 把任意输入值吸附到最近的合法值（复用数量核心）
 * - 不强制起批量：仅按 multiple 倍数吸附；multiple=1 时为任意正整数
 */
const snapToValid = (raw: number): number => wq.snapToValid(raw, Math.max(props.multiple, 1), props.max);

/**
 * 计算点「+」后的目标数量；无法再增加时返回 null（库存不足 / 售罄）
 * - 复用数量核心 plusTarget（含 0→step、先吸附后步进、库存封顶）
 */
const getPlusTarget = (): number | null => {
    if (props.disabled) return null;
    return wq.plusTarget(props.value, props.multiple, props.max);
};

/** 加号是否禁用：已达库存上限，或首档加数已超过库存 */
const isPlusDisabled = computed(() => getPlusTarget() === null);

const setValue = (next: number) => {
    const prev = props.value;
    if (next === prev) return;
    emit("update:value", next);
    emit("change", next, prev);
};

const handleMinus = () => {
    if (props.disabled || props.value <= 0) return;
    // 先吸附再按 step 递减到 0，不因起批量门槛清 0（复用数量核心）
    setValue(wq.minusTarget(props.value, props.multiple));
};

const handlePlus = () => {
    const target = getPlusTarget();
    if (target === null) {
        uni.showToast({ title: t("库存不足"), icon: "none" });
        return;
    }
    setValue(target);
};

const handleFocus = () => {
    focused.value = true;
    // 进入编辑：0 显示为空，方便直接输入；非 0 回填当前值
    inputText.value = props.value > 0 ? String(props.value) : "";
};

const handleInput = (e: any) => {
    // 编辑期只缓存本地文本：不回传父级、不吸附（删除可变空而非立刻 0，数字不跳变）
    inputText.value = String(e.detail?.value ?? "");
};

const handleBlur = () => {
    // 完全失焦后再统一处理倍数：就近吸附到最近合法值（参考 PC el-input-number 的提交时机）
    focused.value = false;
    const raw = Number(inputText.value);
    const next = Number.isFinite(raw) ? snapToValid(raw) : 0;
    inputText.value = "";
    setValue(next);
};
</script>

<style lang="scss" scoped>
.wholesale-stepper {
    display: inline-flex;
    align-items: center;
    height: 56rpx;

    .btn {
        width: 56rpx;
        height: 56rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        background: #f5f5f5;
        color: #2a3145;
        font-size: 36rpx;
        line-height: 1;
        border-radius: 8rpx;

        &.disabled {
            color: #ccc;
            opacity: 0.7;
        }
    }

    .value {
        width: 80rpx;
        height: 56rpx;
        text-align: center;
        font-size: 28rpx;
        color: #2a3145;
        background: #fff;
        margin: 0 1rpx;
    }
}
</style>
