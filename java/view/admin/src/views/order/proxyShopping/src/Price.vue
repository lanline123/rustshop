<template>
    <div class="price align-bottom" :style="priceStyle">
        <div class="price-row">
            <div
                v-if="props.currencyFormat"
                :class="{ 'align-top': currencyAlignment === 'top', 'align-bottom': currencyAlignment === 'bottom' }"
                :style="hasContent(currencyStyle) ? { ...fontStyle, ...currencyStyle } : fontStyle"
                class="util"
            >
                {{ currency }}
            </div>
            <div class="num integer" :style="fontStyle">{{ price.integer }}</div>
            <div class="num decimal" :style="hasContent(decimalsStyle) ? { ...fontStyle, ...decimalsStyle } : fontStyle">{{ price.decimals }}</div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { computed } from "vue";

const props = defineProps({
    modelValue: [String, Number],
    currencyAlignment: { type: String, default: "bottom" },
    currencyFormat: { type: Boolean, default: true },
    currencyStyle: {
        type: Object,
        default: {}
    },
    fontStyle: {
        type: Object,
        default: {}
    },
    decimalsStyle: {
        type: Object,
        default: {}
    },
    showText: {
        type: Boolean,
        default: true
    },
    priceStyle: {
        type: Object,
        default: {}
    },
    // 是否支持负数显示
    negative: {
        type: Boolean,
        default: false
    }
});
const hasContent = (styleObj: any) => {
    return styleObj && Object.keys(styleObj).length > 0;
};

const currency = "¥";

const price = computed(() => {
    let num = typeof props.modelValue === "number" ? String(props.modelValue) : props.modelValue;
    if (!num) {
        return {
            integer: "0",
            decimals: ".00"
        };
    }
    // 检测小数点和后续数字
    const match = props.negative ? num.match(/^(-?\d+)(\.\d+)?$/) : num.match(/^(\d+)(\.\d+)?$/);
    if (match) {
        const integerPart = match[1] || "0";
        const decimalPart = match[2] || ".00";
        return {
            integer: props.negative ? Math.abs(+integerPart) : integerPart,
            decimals: decimalPart
        };
    } else {
        return {
            integer: "0",
            decimals: ".00"
        };
    }
});
</script>
<style lang="less" scoped>
.price {
    display: inline-flex;
    align-items: stretch; /* 子元素高度拉伸以匹配最高的子元素 */
    .text {
        font-size: 12px;
    }

    .util {
        margin-right: 2px;
    }

    .align-top {
        display: flex; /* 使用 Flexbox 布局 */
        align-items: flex-start; /* 使所有子元素沿底部对齐 */
    }

    .align-bottom {
        display: flex; /* 使用 Flexbox 布局 */
        align-items: flex-end; /* 使所有子元素沿底部对齐 */
    }

    .price-row {
        display: flex;
        align-items: baseline;
    }
}
</style>
