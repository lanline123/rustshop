<template>
    <div v-if="tip" class="wholesale-cart-tip" :class="statusClass">
        <span class="tip-icon">
            <template v-if="tip.status === 'OK'">✓</template>
            <template v-else>!</template>
        </span>
        <div class="tip-content">
            <div class="tip-message">{{ displayMessage }}</div>
            <div v-if="tip.ladderTip" class="tip-ladder">{{ tip.ladderTip }}</div>
        </div>
    </div>
</template>

<script setup lang="ts">
import type { WholesaleCartTip } from "@/types/wholesale/cart";
import { WHOLESALE_MOQ_NOT_MET_HINT } from "@/utils/wholesale";

interface Props {
    /** 提示对象；为 null/undefined 时不渲染 */
    tip?: WholesaleCartTip | null;
}

const props = defineProps<Props>();
const { t } = useI18n();

const statusClass = computed(() => {
    if (!props.tip) return "";
    return props.tip.status === "OK" ? "is-ok" : "is-pending";
});

/**
 * 显示文案：优先 tip.message（上游已翻译）；为空但 PENDING 时用本地兜底
 */
const displayMessage = computed(() => {
    if (!props.tip) return "";
    if (props.tip.message) return props.tip.message;
    if (props.tip.status === "OK") return t("已满足起订要求");
    // 本地兜底（仅在后端未给 message 时使用）
    const { gap, step } = props.tip;
    if (step && step > 1) {
        return t("请按 {0} 件倍数购买", [step]);
    }
    if (gap > 0) {
        return t(WHOLESALE_MOQ_NOT_MET_HINT);
    }
    return "";
});
</script>

<style scoped lang="less">
.wholesale-cart-tip {
    display: flex;
    align-items: flex-start;
    gap: 8px;
    padding: 8px 12px;
    border-radius: 4px;
    font-size: 13px;
    line-height: 1.5;

    &.is-ok {
        background: #f0fff4;
        color: #38a169;
    }

    &.is-pending {
        background: #fff8f1;
        color: #dd6b20;
    }
}

.tip-icon {
    flex: 0 0 16px;
    width: 16px;
    height: 16px;
    border-radius: 50%;
    background: currentColor;
    color: #fff;
    text-align: center;
    line-height: 16px;
    font-size: 12px;
    font-weight: bold;
}

.tip-content {
    flex: 1;
    min-width: 0;
}

.tip-message {
    word-break: break-all;
}

.tip-ladder {
    margin-top: 2px;
    color: #1976d2;
    font-size: 12px;
}
</style>
