<template>
    <el-dialog
        v-model="dialogVisible"
        :title="dialogTitle"
        width="700px"
        :close-on-click-modal="false"
        @closed="handleClosed"
    >
        <!-- 搜索栏 -->
        <div class="search-bar">
            <el-input
                v-model="keyword"
                :placeholder="$t('姓名/手机号')"
                clearable
                style="width: 200px"
                @keyup.enter="handleSearch"
            >
                <template #prefix>
                    <el-icon><Search /></el-icon>
                </template>
            </el-input>
            <el-button type="primary" @click="handleSearch">{{ $t('搜索') }}</el-button>
        </div>

        <!-- 人员表格 -->
        <el-table
            :data="tableData"
            :loading="loading"
            highlight-current-row
            @current-change="handleCurrentChange"
            style="margin-top: 16px"
        >
            <el-table-column :label="$t('选择')" width="60" align="center">
                <template #default="{ row }">
                    <el-radio
                        :model-value="selectedId"
                        :label="row.id"
                        :disabled="row.serviceStatus === 2"
                        @change="handleSelect(row)"
                    >
                        <span></span>
                    </el-radio>
                </template>
            </el-table-column>

            <el-table-column :label="$t('头像')" width="80" align="center">
                <template #default="{ row }">
                    <el-avatar :src="row.avatar" :size="40">
                        <el-icon><User /></el-icon>
                    </el-avatar>
                </template>
            </el-table-column>

            <el-table-column prop="name" :label="$t('姓名')" width="120" />
            <el-table-column prop="phone" :label="$t('手机号')" width="130" />

            <el-table-column :label="$t('状态')" width="100" align="center">
                <template #default="{ row }">
                    <el-tag :type="getServiceStatusType(row.serviceStatus)" size="small">
                        {{ getServiceStatusText(row.serviceStatus) }}
                    </el-tag>
                </template>
            </el-table-column>

            <el-table-column :label="$t('今日已完成')" width="110" align="center">
                <template #default="{ row }">
                    {{ $t('{todayCount}单', { todayCount: row.todayCount || 0 }) }}</template>
            </el-table-column>

            <el-table-column :label="$t('距离')" width="80" align="center" v-if="showDistance">
                <template #default="{ row }">
                    {{ formatDistance(row.distance) }}
                </template>
            </el-table-column>
        </el-table>

        <!-- 空状态 -->
        <el-empty v-if="!loading && tableData.length === 0" :description="$t('暂无可用人员')" />

        <template #footer>
            <span class="dialog-footer">
                <el-button @click="dialogVisible = false">{{ $t('取消') }}</el-button>
                <el-button
                    type="primary"
                    :disabled="!selectedId"
                    :loading="submitting"
                    @click="handleConfirm"
                > {{ $t('确认派单') }} </el-button>
            </span>
        </template>
    </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from "vue";
import { ElMessage } from "element-plus";
import { Search, User } from "@element-plus/icons-vue";
import type { AvailableStaffItem } from "@/types/organize/dispatch";
import type { StaffType } from "@/types/organize/staff";
import { getAvailableStaff } from "@/api/organize/dispatch";

/**
 * 人员选择弹窗组件
 * 功能：用于订单派单时选择配送员或服务员
 */

import { useI18n } from "vue-i18n"
const { t } = useI18n()
interface Props {
    /** 显示状态 */
    modelValue: boolean;
    /** 人员类型 */
    staffType: StaffType;
    /** 门店ID（筛选） */
    shopId?: number;
    /** 订单ID（用于计算距离） */
    orderId?: number;
}

const props = withDefaults(defineProps<Props>(), {
    shopId: undefined,
    orderId: undefined
});

const emit = defineEmits<{
    "update:modelValue": [value: boolean];
    confirm: [staffId: number, staffInfo: AvailableStaffItem];
}>();

// ===== 响应式状态 =====
const dialogVisible = computed({
    get: () => props.modelValue,
    set: (val) => emit("update:modelValue", val)
});

const keyword = ref("");
const loading = ref(false);
const submitting = ref(false);
const tableData = ref<AvailableStaffItem[]>([]);
const selectedId = ref<number | null>(null);
const selectedStaff = ref<AvailableStaffItem | null>(null);

// ===== 计算属性 =====
const dialogTitle = computed(() => {
    return props.staffType === "deliveryman" ? t('选择配送员') : t('选择服务员');
});

const showDistance = computed(() => {
    return props.orderId !== undefined;
});

// ===== 方法 =====

/**
 * 获取服务状态类型
 */
const getServiceStatusType = (status: number): string => {
    const statusMap: Record<number, string> = {
        0: "success", // 空闲中
        1: "warning", // 配送中/服务中
        2: "info" // 已下班
    };
    return statusMap[status] || "info";
};

/**
 * 获取服务状态文本
 */
const getServiceStatusText = (status: number): string => {
    const statusMap: Record<number, string> = {
        0: t('空闲中'),
        1: props.staffType === "deliveryman" ? t('配送中') : t('服务中'),
        2: t('已下班')
    };
    return statusMap[status] || t('未知');
};

/**
 * 格式化距离
 */
const formatDistance = (distance?: number): string => {
    if (!distance) return "-";
    if (distance < 1000) {
        return `${distance}m`;
    }
    return `${(distance / 1000).toFixed(1)}km`;
};

/**
 * 加载可用人员列表
 */
const loadStaffList = async () => {
    if (!props.shopId) {
        ElMessage.warning(t('请先选择门店'));
        return;
    }

    loading.value = true;
    try {
        const result = await getAvailableStaff({
            shopId: props.shopId,
            staffType: props.staffType,
            keyword: keyword.value || undefined,
            orderId: props.orderId
        });
        tableData.value = result.records || [];
        
        // 重置选中状态
        selectedId.value = null;
        selectedStaff.value = null;
    } catch (error: any) {
        ElMessage.error(error.message || t('加载人员列表失败'));
        tableData.value = [];
    } finally {
        loading.value = false;
    }
};

/**
 * 搜索
 */
const handleSearch = () => {
    loadStaffList();
};

/**
 * 选择人员
 */
const handleSelect = (row: AvailableStaffItem) => {
    selectedId.value = row.id;
    selectedStaff.value = row;
};

/**
 * 当前行变化
 */
const handleCurrentChange = (row: AvailableStaffItem | null) => {
    if (row) {
        selectedId.value = row.id;
        selectedStaff.value = row;
    }
};

/**
 * 确认派单
 */
const handleConfirm = () => {
    if (!selectedId.value || !selectedStaff.value) {
        ElMessage.warning(t('请选择人员'));
        return;
    }

    if (selectedStaff.value.serviceStatus === 2) {
        ElMessage.warning(t('该人员已下班，无法派单'));
        return;
    }

    emit("confirm", selectedId.value, selectedStaff.value);
};

/**
 * 弹窗关闭后重置
 */
const handleClosed = () => {
    keyword.value = "";
    selectedId.value = null;
    selectedStaff.value = null;
    tableData.value = [];
};

// ===== 监听 =====
watch(
    () => props.modelValue,
    (visible) => {
        if (visible && props.shopId) {
            loadStaffList();
        }
    },
    { immediate: true }
);
</script>

<style lang="less" scoped>
.search-bar {
    display: flex;
    align-items: center;
    gap: 12px;
}

.dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
}

:deep(.el-table) {
    .el-table__row {
        cursor: pointer;

        &.disabled-row {
            opacity: 0.5;
        }
    }
}
</style>
