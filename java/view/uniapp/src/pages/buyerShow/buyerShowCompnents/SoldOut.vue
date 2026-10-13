<template>
    <view class="sold-out-box">
        <view v-if="finalShowOutsale" class="outsale">{{ $t("已售罄") }}</view>
        <slot name="default" />
    </view>
</template>

<script setup lang="ts">
/**
 * 售罄标签组件
 * - 显示商品售罄状态
 */
import { computed, defineProps, type PropType } from "vue";
import { shouldShowSoldOut } from "@/utils/productSoldOut";

const props = defineProps({
    showOutsale: {
        type: Boolean,
        default: false
    },
    item: {
        type: Object as PropType<any>,
        default: () => ({})
    },
    stockField: {
        type: String,
        default: "productStock"
    }
});

const finalShowOutsale = computed(() => {
    if (props.item && props.stockField in props.item) {
        return shouldShowSoldOut(props.item, { stockField: props.stockField });
    }
    return props.showOutsale;
});
</script>

<style scoped lang="scss">
.sold-out-box {
    position: relative;
    .outsale {
        position: absolute;
        top: 50%;
        left: 50%;
        width: 70%;
        height: 70%;
        transform: translate(-50%, -50%);
        background: rgba(0, 0, 0, 0.4);
        font-size: 24rpx;
        color: #ffffff;
        letter-spacing: 0;
        border-radius: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
        z-index: 9;
    }
}
</style>
