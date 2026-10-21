<template>
    <div class="wholesale-buy-bar" :class="appearance">
        <el-button
            class="buy-btn buynow"
            type="primary"
            :disabled="busy || disabled"
            :loading="loadingBuy"
            @click="onBuy">
            {{ $t("立即购买") }}
        </el-button>
        <el-button
            class="buy-btn"
            :disabled="busy || disabled"
            :loading="loadingAdd"
            @click="onAdd">
            {{ $t("加入购物车") }}
        </el-button>
        <el-button
            v-if="showInquiry"
            class="buy-btn buynow"
            :disabled="busy"
            @click="onInquiry">
            {{ $t("立即询价") }}
        </el-button>
    </div>
</template>

<script setup lang="ts">
import { ElButton } from "element-plus";

interface Props {
    appearance?: "default" | "overseas";
    /** 是否显示询价按钮（取决于现有 b2b.inquiry flag 与商品配置） */
    showInquiry?: boolean;
    /** 主按钮禁用（如商品下架） */
    disabled?: boolean;
    /** 加车 loading */
    loadingAdd?: boolean;
    /** 立即购买 loading */
    loadingBuy?: boolean;
}

withDefaults(defineProps<Props>(), {
    appearance: "default",
    showInquiry: false,
    disabled: false,
    loadingAdd: false,
    loadingBuy: false
});

const emit = defineEmits<{
    /** 点击加入购物车（外部做本地校验） */
    "add-to-cart": [];
    /** 点击立即购买（加车成功后跳购物车/结算页） */
    "buy-now": [];
    /** 点击立即询价（保持现有 b2b.inquiry能力） */
    inquiry: [];
}>();

const busy = computed(() => false); // 预留：将来可加全局禁用态

const onAdd = () => emit("add-to-cart");
const onBuy = () => emit("buy-now");
const onInquiry = () => emit("inquiry");
</script>

<style scoped lang="less">
.wholesale-buy-bar {
    display: flex;
    padding: 16px 0 25px;

    .buy-btn {
        height: 40px;
        padding: 0 20px;
        border-radius: 2px;

        &:hover {
            opacity: 0.9;
        }
    }

    .buynow {
        height: 40px;
        padding: 0 20px;
    }

    &.overseas {
        gap: 10px;

        .buy-btn {
            flex: 1 1 0;
            min-width: 0;
            height: 48px;
            margin-left: 0;
            padding: 0 20px;
            border-radius: 4px;
            font-size: 16px;
            font-weight: 700;

            :deep(span) {
                font-weight: 700;
            }
        }

        .buy-btn:nth-child(2) {
            --el-button-disabled-text-color: #fff;
            --el-button-disabled-bg-color: #1a1a1a;
            --el-button-disabled-border-color: #1a1a1a;
            background-color: #1a1a1a;
            border-color: #1a1a1a;
            color: #fff;
        }

        .buy-btn:nth-child(3) {
            --el-button-disabled-text-color: #1a1a1a;
            --el-button-disabled-bg-color: #fff;
            --el-button-disabled-border-color: #1a1a1a;
            background-color: #fff;
            border-color: #1a1a1a;
            color: #1a1a1a;
        }

        .buy-btn.is-disabled {
            opacity: 0.5;
        }
    }
}
</style>
