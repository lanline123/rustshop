<template>
    <div class="staff-info-card">
        <!-- 左侧：头像、名称、联系方式 -->
        <div class="left-section">
            <el-avatar :src="info.avatar" :size="50">
                <i class="el-icon-user"></i>
            </el-avatar>
            <div class="staff-basic">
                <h3 class="name">{{ info.name }}</h3>
                <StaffMobileCard class="phone" :id="info.id" :mobile="info.phone" />
            </div>
        </div>

        <!-- 右侧：订单统计 -->
        <div class="right-section">
            <div class="stat-item">
                <div class="stat-value">
                    <span class="number">{{ info.completedOrderCount || 0 }}</span>
                    <span class="unit">{{ $t('单') }}</span>
                </div>
                <div class="stat-label">{{ $t('{orderLabel}订单数', { orderLabel }) }}</div>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item">
                <div class="stat-value">
                    <span class="number">{{ formatAmount(info.totalIncome) }}</span>
                    <span class="unit">{{ $t('元') }}</span>
                </div>
                <div class="stat-label">{{ staffType === "serviceman" ? $t('服务费') : $t('配送费') }}</div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed } from "vue";
import type { ServiceStaffVO } from "@/api/organize/serviceStaff";
import StaffMobileCard from "@/components/organize/StaffMobileCard.vue";

/**
 * 人员信息卡片组件
 * 功能：展示配送员/服务员的基本信息和订单统计
 */

// ===== Props =====
import { useI18n } from "vue-i18n"
const { t } = useI18n()
interface Props {
    info: ServiceStaffVO;
    staffType: "deliveryman" | "serviceman";
}

const props = defineProps<Props>();

// ===== 计算属性 =====

/** 根据类型显示不同的订单标签 */
const orderLabel = computed(() => {
    return props.staffType === "deliveryman" ? t('已配送') : t('已服务');
});

// ===== 方法 =====

/**
 * 格式化金额
 */
const formatAmount = (amount: number | undefined): string => {
    if (amount === undefined || amount === null) return "0.00";
    return amount.toFixed(2);
};
</script>

<style lang="less" scoped>
.staff-info-card {
    display: flex;
    justify-content: space-between;
    align-items: center;
    background-color: #f5f7fa;
    border-radius: 8px;
    padding: 24px 32px 24px 24px;
    margin-bottom: 15px;

    // 左侧区域
    .left-section {
        display: flex;
        align-items: center;
        gap: 16px;

        .staff-basic {
            .name {
                font-size: 18px;
                font-weight: bold;
                color: #333;
                margin: 0 0 8px 0;
            }

            .phone {
                font-size: 14px;
                color: #666;
                margin: 0;
            }
        }
    }

    // 右侧区域
    .right-section {
        display: flex;
        align-items: center;
        gap: 40px;

        .stat-item {
            text-align: center;

            .stat-value {
                margin-bottom: 15px;

                .number {
                    font-size: 28px;
                    font-weight: 600;
                    color: var(--tig-primary);
                }

                .unit {
                    font-size: 14px;
                    color: var(--tig-primary);
                    margin-left: 4px;
                }
            }

            .stat-label {
                font-size: 14px;
                color: #333;
            }
        }

        // 竖线分割
        .stat-divider {
            width: 1px;
            height: 30px;
            background-color: #dcdfe6;
        }
    }
}

// 响应式适配
@media (max-width: 768px) {
    .staff-info-card {
        flex-direction: column;
        gap: 24px;
        padding: 20px;

        .left-section {
            width: 100%;
            justify-content: center;
        }

        .right-section {
            width: 100%;
            justify-content: center;
            gap: 24px;

            .stat-divider {
                height: 40px;
            }
        }
    }
}
</style>
