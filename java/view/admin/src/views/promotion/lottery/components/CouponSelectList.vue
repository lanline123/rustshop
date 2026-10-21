<template>
    <div class="coupon-select-list">
        <div class="search-bar">
            <TigInput
                v-model="filterParams.keyword"
                name="keyword"
                :placeholder="$t('输入优惠券名称')"
                clearable
                @clear="onSearchSubmit"
                @keyup.enter="onSearchSubmit"
            >
                <template #append>
                    <el-button @click="onSearchSubmit"><span class="iconfont icon-chakan1"></span></el-button>
                </template>
            </TigInput>
        </div>
        <div class="table-container">
            <a-spin :spinning="loading">
                <el-table 
                    ref="tableRef"
                    :data="filterState" 
                    :loading="loading"
                    @select="handleSelectChange"
                    @select-all="handleSelectAll"
                    row-key="couponId"
                    class="single-select-table"
                >
                    <el-table-column type="selection" width="50" />
                    <el-table-column :label="$t('优惠券名称')" prop="couponName" min-width="150">
                        <template #default="{ row }">
                            {{ row.couponName }}
                        </template>
                    </el-table-column>
                    <el-table-column :label="$t('发放类型')" width="120">
                        <template #default="{ row }">
                            {{ row.couponType == 1 ? $t('满减券') : $t('折扣券') }}
                        </template>
                    </el-table-column>
                    <el-table-column :label="$t('优惠券金额(折扣)')" width="140">
                        <template #default="{ row }">
                            <span v-if="row.couponType == 1">{{ row.couponMoney || "0" }}</span>
                            <span v-else>{{ $t('{couponDiscount}折', { couponDiscount: row.couponDiscount || "0" }) }}</span>
                        </template>
                    </el-table-column>
                    <template #empty>
                        <div class="empty-warp">
                            <div v-if="!loading" class="empty-bg">{{ $t('暂无数据') }}</div>
                        </div>
                    </template>
                </el-table>
            </a-spin>
            <div class="pagination-con" v-if="total > 0">
                <Pagination v-model:page="filterParams.page" v-model:size="filterParams.size" :total="total" @callback="loadFilter" />
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, nextTick } from 'vue';
import { Pagination } from "@/components/list";
import { getValidCouponList } from "@/api/promotion/coupon";
import { useConfigStore } from "@/store/config";
import { useListRequest } from "@/hooks/useListRequest";
import { CouponFilterState, CouponFilterParams } from "@/types/promotion/coupon.d";
import { ElMessage } from 'element-plus';

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const config: any = useConfigStore();

const props = defineProps({
    selectedCouponId: {
        type: Number,
        default: 0
    },
    isDialog: {
        type: Boolean,
        default: false
    },
    visible: {
        type: Boolean,
        default: false
    }
});

const emit = defineEmits(['submitCallback', 'callback', 'close']);

const confirmLoading = defineModel('confirmLoading', {
    type: Boolean,
    default: false
});

const tableRef = ref<any>(null);
const selectedRow = ref<CouponFilterState | null>(null);

const {
    listData: filterState,
    loading,
    total,
    filterParams,
    loadData: loadFilter,
    onSearchSubmit
} = useListRequest<CouponFilterState, CouponFilterParams>({
    apiFunction: getValidCouponList,
    idKey: "couponId",
    defaultParams: {
        sortField: "",
        sortOrder: "",
        keyword: "",
        page: 1,
        size: config.get("pageSize")
    }
});

// 单选处理
const handleSelectChange = (selection: CouponFilterState[], row: CouponFilterState) => {
    // 清除其他选中，只保留当前行
    tableRef.value?.clearSelection();
    if (selection.length > 0) {
        tableRef.value?.toggleRowSelection(row, true);
        selectedRow.value = row;
    } else {
        selectedRow.value = null;
    }
};

// 禁止全选
const handleSelectAll = (selection: CouponFilterState[]) => {
    tableRef.value?.clearSelection();
    if (selectedRow.value) {
        const row = filterState.value.find((item: CouponFilterState) => item.couponId === selectedRow.value?.couponId);
        if (row) {
            tableRef.value?.toggleRowSelection(row, true);
        }
    }
};

// 回显已选中的优惠券
const restoreSelection = () => {
    if (props.selectedCouponId && filterState.value.length > 0) {
        const row = filterState.value.find((item: CouponFilterState) => item.couponId === props.selectedCouponId);
        if (row) {
            nextTick(() => {
                tableRef.value?.toggleRowSelection(row, true);
                selectedRow.value = row;
            });
        }
    }
};

// 监听数据加载完成后回显
watch(() => filterState.value, () => {
    restoreSelection();
}, { immediate: true });

// 表单提交方法（供DialogForm组件调用）
const onFormSubmit = async () => {
    if (!selectedRow.value) {
        ElMessage.warning(t('请选择一个优惠券'));
        return Promise.reject('未选择优惠券');
    }
    emit('submitCallback', {
        couponId: selectedRow.value.couponId,
        couponName: selectedRow.value.couponName,
        couponType: selectedRow.value.couponType,
        couponMoney: selectedRow.value.couponMoney,
        couponDiscount: selectedRow.value.couponDiscount
    });
};

onMounted(() => {
    loadFilter();
});

defineExpose({
    onFormSubmit
});
</script>

<style scoped lang="less">
.coupon-select-list {
    padding: 20px;
    
    .search-bar {
        margin-bottom: 15px;
        max-width: 300px;
    }
    
    .table-container {
        margin-top: 10px;
    }
    
    .pagination-con {
        margin-top: 15px;
    }
    
    // 隐藏表头的全选复选框，实现单选效果
    .single-select-table {
        :deep(.el-table__header-wrapper .el-table__header .el-checkbox) {
            display: none;
        }
    }
}
</style>

