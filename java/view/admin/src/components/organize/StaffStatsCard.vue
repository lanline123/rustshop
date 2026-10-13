<template>
    <div class="stats-grid">
        <div class="stat-card">
            <div class="stat-value">{{ stats.completedOrderCount ?? 0 }}</div>
            <div class="stat-label">{{ $t('已完成{staffTypedeliveryman}', { staffTypedeliveryman: staffType === "deliveryman" ? "配送" : "服务" }) }}
            </div>
        </div>
        <div class="stat-card">
            <div class="stat-value">¥{{ formatAmount(stats.completedOrderAmount ?? 0) }}</div>
            <div class="stat-label">{{ $t('已完成金额') }}</div>
        </div>
    </div>
</template>

<script setup lang="ts">
import type { ServiceStaffVO } from "@/api/organize/serviceStaff";

/**
 * 统计数据卡片组件
 * 功能：展示配送员/服务员的统计数据
 */

// ===== Props =====
interface Props {
    stats: ServiceStaffVO;
    staffType: "deliveryman" | "serviceman";
}

defineProps<Props>();

// ===== 方法 =====

/**
 * 格式化金额
 */
const formatAmount = (amount: number): string => {
    return amount.toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ",");
};
</script>

<style lang="less" scoped>
.stats-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 20px;
    margin-bottom: 20px;

    .stat-card {
        background: #f8f9fa;
        border-radius: 8px;
        padding: 20px;
        text-align: center;
        transition: all 0.3s ease;

        &:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
        }

        .stat-value {
            font-size: 24px;
            font-weight: bold;
            color: var(--tig-primary, #1456f0);
            margin-bottom: 8px;
        }

        .stat-label {
            font-size: 14px;
            color: #666;
        }
    }
}

// 响应式适配
@media (max-width: 576px) {
    .stats-grid {
        grid-template-columns: 1fr;
    }
}
</style>

