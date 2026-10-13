<template>
    <view class="price-content" :class="{ bottom: isBottom }">
        <template v-if="showB2bText">
            <view class="line1 b2b-text">{{ $t(b2bText) }}</view>
        </template>
        <template v-else>
            <view v-if="props.currencyFormat" :style="hasContent(currencyStyle) ? currencyStyle : fontStyle" class="num util">
                {{ currency }}
            </view>
            <view class="num integer" :style="hasContent(integerStyle) ? integerStyle : fontStyle">{{ price.integer }}</view>
            <view v-if="showDecimals" class="num decimal" :style="hasContent(decimalsStyle) ? decimalsStyle : fontStyle">{{ price.decimals }}</view>
        </template>
    </view>
</template>
<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import { computed } from "vue";
import { useConfigStore } from "@/store/config";
import { useCurrencyStore } from "@/store/currency";
import { useUserStore } from "@/store/user";
import { isOverseas, moneyFormat } from "@/utils";

import { isB2B } from "@/utils";

const { t } = useI18n();

const configStore = useConfigStore();
const currencyStore = useCurrencyStore();
const userStore = useUserStore();

const props = defineProps({
    priceData: [String, Number],
    isBottom: { type: Boolean, default: true },
    currencyFormat: { type: Boolean, default: true },
    currencyStyle: {
        type: Object,
        default: () => ({})
    },
    fontStyle: {
        type: Object,
        default: () => ({})
    },
    decimalsStyle: {
        type: Object,
        default: () => ({})
    },
    integerStyle: {
        type: Object,
        default: () => ({})
    },
    showDecimals: {
        type: Boolean,
        default: true
    },
    showText: {
        type: Boolean,
        default: true
    },
    base: {
        type: Boolean,
        default: false
    }
});
const hasContent = (styleObj: any) => {
    return styleObj && Object.keys(styleObj).length > 0;
};

const b2bText = computed(() => {
    let str = t("登录查看");
    if (Object.keys(userStore.userInfo).length > 0) {
        str = t("实名查看");
    }
    return str;
});

const showB2bText = computed(() => {
    let value = false;
    // B2B/批发构建：实名价格遮罩仅在 b2b 构建启用（对齐标准/跨境源仓 isB2B() 前置）；纯零售构建此守卫被裁切，退回原 isIdentity 逻辑，行为零变化
    if (!isB2B()) return false;
    if (props.showText && configStore.isIdentity === 1) {
        if (Object.keys(userStore.userInfo).length === 0) {
            value = true;
        } else if (!userStore.userInfo.isCompanyAuth) {
            value = true;
        }
    }
    return value;
});

const currency = computed(() => {
    const baseCurrency = configStore.dollarSign || "￥";
    if (props.base) return baseCurrency;
    return isOverseas() ? currencyStore.currentCurrencyData.symbol || baseCurrency : baseCurrency;
});
const price = computed(() => {
    let num = typeof props.priceData === "number" ? String(props.priceData) : props.priceData;
    if (!num) {
        return {
            integer: "0",
            decimals: ".00"
        };
    }
    if (!props.base && isOverseas() && currencyStore.currentCurrencyData.rate) {
        num = moneyFormat((Number(num) * Number(currencyStore.currentCurrencyData.rate)).toString());
    }
    // 检测小数点和后续数字
    const match = num.match(/^(\d+)(\.\d+)?$/);
    if (match) {
        const integerPart = match[1] || "0";
        const rawDecimal = match[2] || ".00";
        const decimalPart = rawDecimal.length < 3 ? rawDecimal.padEnd(3, "0") : rawDecimal;
        return {
            integer: integerPart,
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
<style lang="scss" scoped>
.price-content {
    height: auto;
    display: inline-block;
    .util {
        padding-right: 3rpx;
        // #ifdef MP-WEIXIN
        padding-right: 5rpx;
        // #endif
    }

    .num {
        display: inline-flex;
        flex-direction: column;
        justify-content: center;
    }

    .util,
    .decimal {
        height: inherit;
    }

    &.bottom {
        .num {
            justify-content: flex-end;
        }

        .util,
        .decimal {
            padding-bottom: 2rpx;
            // #ifdef MP-WEIXIN
            padding-bottom: 3rpx;
            // #endif
        }
    }

    .b2b-text {
        font-size: 22rpx !important;
        font-weight: normal !important;
    }
}
</style>
