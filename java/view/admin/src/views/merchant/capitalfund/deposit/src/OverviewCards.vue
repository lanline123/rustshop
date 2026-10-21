/**
 * @file 店铺端 保证金总览三卡（公式形态）
 * @description 可用余额 = 总余额 - 冻结金额，供退还申请、可用明细等弹窗复用
 */
<template>
    <div class="overview-cards">
        <div class="card highlight">
            <div class="card-label">{{ $t('保证金可用余额') }}</div>
            <div class="card-num">{{ priceFormat(overview.withdrawable, false) }}<span class="unit">{{ $t('元') }}</span></div>
        </div>
        <div class="operator">=</div>
        <div class="card">
            <div class="card-label">{{ $t('保证金总余额') }}</div>
            <div class="card-num">{{ priceFormat(overview.paid, false) }}<span class="unit">{{ $t('元') }}</span></div>
        </div>
        <div class="operator">-</div>
        <div class="card outline">
            <div class="card-label">{{ $t('冻结金额') }}</div>
            <div class="card-num">{{ priceFormat(overview.frozen, false) }}<span class="unit">{{ $t('元') }}</span></div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { priceFormat } from "@/utils/format";
import type { ShopDepositOverview } from "@/types/merchant/capitalfund/deposit.d";

defineProps<{ overview: ShopDepositOverview }>();
</script>

<style lang="less" scoped>
.overview-cards {
    display: flex;
    align-items: center;
    gap: 6px;
    margin-bottom: 24px;
    .card {
        flex: 1;
        background-color: #f7f8fb;
        border: 1px solid transparent;
        border-radius: 8px;
        padding: 20px;
        text-align: left;
        box-sizing: border-box;
        display: flex;
        flex-direction: column;
        justify-content: center;
        &.highlight {
            background-color: #3d80ff;
            .card-label {
                color: rgba(255, 255, 255, 0.85);
            }
            .card-num {
                color: #fff;
            }
        }
        &.outline {
            border-color: #2f74ff;
        }
        .card-label {
            margin-bottom: 8px;
        }
        .card-num {
            font-size: 20px;
            font-weight: bold;
            color: #303133;
            line-height: 1.2;
            .unit {
                margin-left: 4px;
                font-size: 12px;
                font-weight: normal;
                color: inherit;
            }
        }
    }
    .operator {
        font-size: 18px;
        font-weight: 500;
        user-select: none;
    }
}
</style>
