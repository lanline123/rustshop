/**
 * @file 保证金顶部总览条
 * @description 商家类型 / 经营类目 / 所属商户 / 保证金状态 / 开店时间（只读）
 */
<template>
    <div class="overview-bar">
        <div class="overview-item">
            <span class="label">{{ $t('商家类型') }}</span>
            <span class="value">{{ detail.subjectTypeText || "--" }}</span>
        </div>
        <div class="overview-item">
            <span class="label">{{ $t('经营类目') }}</span>
            <span class="value">{{ detail.businessCategory || "--" }}</span>
        </div>
        <div class="overview-item">
            <span class="label">{{ $t('所属商户') }}</span>
            <span class="value">{{ detail.merchantName || "--" }}</span>
        </div>
        <div class="overview-item">
            <span class="label">{{ $t('保证金状态') }}</span>
            <span class="value">{{ detail.depositStatusText || "--" }}</span>
        </div>
        <div class="overview-item">
            <span class="label">{{ $t('开店时间') }}</span>
            <span class="value">{{ detail.openTime || "--" }}</span>
        </div>
    </div>
</template>

<script lang="ts" setup>
import type { PropType } from "vue";
import type { DepositQueryDetail } from "@/types/finance/deposit.d";

defineProps({
    detail: {
        type: Object as PropType<DepositQueryDetail>,
        default: () => ({})
    }
});
</script>

<style lang="less" scoped>
.overview-bar {
    display: flex;
    flex-wrap: wrap;
    gap: 16px 32px;
    background-color: #f5f6fa;
    padding: 13px 18px;
    border-radius: 3px;
    margin-bottom: 16px;
    .overview-item {
        flex: 1;
        min-width: 0;
        display: flex;
        flex-direction: column;
        gap: 9px;
        .label {
            color: #a4adb3;
        }
        .value {
            color: #333;
            line-height: 1.5;
        }
    }
}
</style>
