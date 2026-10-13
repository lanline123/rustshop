<template>
    <!-- 右侧预览占位列：大屏保留宽度；不做固定，始终随滚动容器滚动 -->
    <div class="payment-preview-aside">
        <div class="payment-preview-aside__fixed">
            <H5PayPreview :selected="selected" :hiddenKeys="hiddenKeys" :paymentOrder="paymentOrder" />
        </div>
    </div>
</template>

<script lang="ts" setup>
import H5PayPreview from "./H5PayPreview.vue";

type PayKey =
    | "wechat"
    | "alipay"
    | "paypal"
    | "yunpay"
    | "huifuWechat"
    | "huifuAlipay"
    | "lakalaWechat"
    | "lakalaAlipay"
    | "yeepayWechat"
    | "yeepayAlipay"
    | "applepay"
    ;

defineProps<{
    selected?: PayKey;
    hiddenKeys?: readonly PayKey[];
    /** 可选：用于当前页面预览的临时支付顺序（不传则使用后端基础设置 paymentOrder） */
    paymentOrder?: readonly PayKey[];
}>();
</script>

<style lang="less" scoped>
/**
 * 设计目标：
 * - 大屏（>=1441px）：右侧预览占位在内容右侧（不固定，随滚动）
 * - 小屏（<=1440px）：预览回到下方（跟随页面流）
 *
 * 说明：
 * - 项目主体内容使用 el-scrollbar 做滚动；本组件不再做 fixed/sticky，始终随滚动条滚动。
 * - 右侧固定预览需要占位列，否则会覆盖左侧表单区域。
 */
.payment-preview-aside {
    --payment-preview-width: 360px;
    /* 底部提交条是 fixed（约 60px），再留一点间距避免遮挡 */
    --payment-preview-bottom-gap: 80px;

    flex: 0 0 var(--payment-preview-width);
    width: var(--payment-preview-width);
    /* 作为 flex item 时，显式 stretch，保证 sticky 的父容器高度足够 */
    align-self: stretch;
    display: flex;
    justify-content: center;
    align-items: flex-start;
    padding: 4px 0 10px;
}

.payment-preview-aside__fixed {
    width: var(--payment-preview-width);
    display: flex;
    justify-content: center;
}

@media (min-width: 1400px) {
    .payment-preview-aside__fixed {
        width: var(--payment-preview-width);
        overflow: visible;
        position: static;
    }
}

@media (max-width: 1401x) {
    .payment-preview-aside {
        flex: none;
        width: 100%;
        justify-content: flex-start;
    }

    .payment-preview-aside__fixed {
        position: static;
        width: 100%;
        max-height: none;
        overflow: visible;
        display: flex;
        justify-content: center;
    }
}
</style>
