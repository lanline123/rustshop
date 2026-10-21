<template>
    <!-- 海外版属性下拉面板（layoutStyle=2） -->
    <view class="brand-box" @click.stop>
        <view class="brand-box-content">
            <view class="brand-title">{{ $t(attrItem.name) }}</view>
            <view class="brand-list">
                <template v-for="(item, index) in attrItem.values" :key="index">
                    <view class="brand-item" :class="{ active: selectedValues.includes(getOptionText(item)) }" @click="emit('change', getOptionText(item))">
                        <text class="line1">{{ getOptionText(item) }}</text>
                    </view>
                </template>
            </view>
        </view>

        <view class="brand-btn">
            <tig-button2 class="btn" style="height: 63rpx" :loading="isLoading" @click="emit('attrReset')">
                {{ $t("重置") }}
            </tig-button2>
            <tig-button2 class="btn" style="height: 63rpx" theme @click="emit('close')">
                <text>{{ $t("显示") }} {{ Number(total) || 0 }} {{ $t("个结果") }}</text>
            </tig-button2>
        </view>
    </view>
</template>

<script setup lang="ts">
/**
 * 搜索结果页 - 海外版属性面板
 * 选中态由父级 filterParams.attrs 决定，与国内抽屉共用同一份选中数据。
 */
import type { PropType } from "vue";
import type { attrsListData } from "@/types/search/search";
import { getAttrOptionText } from "../composables";

defineProps({
    attrItem: {
        type: Object as PropType<attrsListData>,
        default: () => ({ name: "", values: [] })
    },
    /** 当前属性已选中的值 */
    selectedValues: {
        type: Array as PropType<string[]>,
        default: () => []
    },
    total: {
        type: Number,
        default: 0
    },
    isLoading: {
        type: Boolean,
        default: false
    }
});

const emit = defineEmits<{
    (e: "change", value: string): void;
    (e: "attrReset"): void;
    (e: "close"): void;
}>();

const getOptionText = getAttrOptionText;
</script>

<style lang="scss" scoped>
.brand-box {
    position: relative;
    top: calc(var(--safe-top) * -1);

    .brand-box-content {
        padding: 21rpx 28rpx 120rpx;
        // #ifdef MP-WEIXIN
        padding-top: calc(var(--safe-top, 21rpx));
        // #endif
        border-bottom-left-radius: 21rpx;
        border-bottom-right-radius: 21rpx;
        background-color: #fff;
        max-height: 600rpx;
        overflow: hidden;
        overflow-y: auto;
    }

    .brand-title {
        padding-bottom: 32rpx;
        font-weight: 700;
        font-size: 28rpx;
    }

    .brand-list {
        display: flex;
        flex-wrap: wrap;
        gap: 25rpx 14rpx;

        .brand-item {
            background: #f5f6fa;
            padding: 9rpx 21rpx;
            font-size: 25rpx;
            border-radius: 7rpx;
            color: #505259;
            cursor: pointer;
            border: 1rpx solid #f5f6fa;
            overflow: hidden;
            max-width: 317rpx;
            outline: none;
            display: flex;
            align-items: center;
            transition:
                background-color 0.2s ease,
                color 0.2s ease;

            &.active {
                outline: 3rpx solid #1a1a1a;
                outline-offset: -2rpx;
                color: #1a1a1a;
                background-color: #f7f8fc;
            }
        }
    }

    .brand-btn {
        position: absolute;
        bottom: 0;
        left: 0;
        right: 0;
        display: flex;
        padding: 21rpx 28rpx;
        gap: 14rpx;
        background-color: #fff;
        border-bottom-left-radius: 21rpx;
        border-bottom-right-radius: 21rpx;
        overflow: hidden;

        .btn {
            flex: 1;
        }
    }
}
</style>
