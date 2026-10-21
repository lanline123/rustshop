<template>
    <div class="container" style="position: relative; height: 100%">
        <div class="content_wrapper" style="width: 100%; height: 100%; position: absolute">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp" style="flex: 0; padding-right: 15px">
                    <el-form :model="filterParams" name="form" @finish="onSearchSubmit">
                        <div class="advanced-search-warp list-table-tool-row">
                            <div class="list-table-tool-col">
                                <el-space>
                                    <TigInput
                                        width="230px"
                                        v-model="filterParams.keyword"
                                        :placeholder="$t('优惠券名称')"
                                        @keyup.enter="onSearchSubmit"
                                    />
                                    <el-button @click="onSearchSubmit">{{ $t('搜索') }}</el-button>
                                </el-space>
                            </div>
                        </div>
                    </el-form>
                </div>
                <perfect-scrollbar class="table-container" style="flex: 1">
                    <a-spin :spinning="loading">
                        <div style="padding-right: 15px; box-sizing: border-box">
                            <el-table
                                ref="tableRef"
                                :class="isMultiple ? '' : 'hide-checkbox'"
                                :data="filterState"
                                :loading="loading"
                                :total="total"
                                class="result-table"
                                :row-key="(row: CouponFilterState) => row.couponId!"
                                @select="selectRow"
                                @selection-change="onSelectionChange"
                            >
                                <el-table-column type="selection" width="32" />
                                <el-table-column :label="$t('优惠券名称')" prop="couponName" min-width="180">
                                    <template #default="{ row }">
                                        <span>{{ row.couponName }}</span>
                                    </template>
                                </el-table-column>
                                <el-table-column :label="$t('发放类型')" width="100">
                                    <template #default="{ row }">
                                        {{ row.couponType == 1 ? $t('满减券') : $t('折扣券') }}
                                    </template>
                                </el-table-column>
                                <el-table-column :label="$t('优惠券金额(折扣)')" width="160">
                                    <template #default="{ row }">
                                        <span v-if="row.couponType == 1">
                                            <template v-if="row.reduceType === 2 || !Number(row.minOrderAmount)">{{ $t('无门槛减{couponMoney}', { couponMoney: row.couponMoney || 0 }) }}</template>
                                            <template v-else>{{ $t('满{minOrderAmount}减{couponMoney}', { minOrderAmount: row.minOrderAmount, couponMoney: row.couponMoney || 0 }) }}</template>
                                        </span>
                                        <span v-else>{{ $t('{couponDiscount}折', { couponDiscount: row.couponDiscount || 0 }) }}</span>
                                    </template>
                                </el-table-column>
                                <template #empty>
                                    <div class="empty-warp">
                                        <div v-if="!loading" class="empty-bg">{{ $t('暂无数据') }}</div>
                                    </div>
                                </template>
                            </el-table>
                        </div>
                    </a-spin>
                </perfect-scrollbar>
                <div v-if="total > 0" class="pagination-con" style="flex-shrink: 0">
                    <Pagination
                        v-model:page="filterParams.page"
                        v-model:size="filterParams.size"
                        :total="total"
                        @callback="onPageChanged"
                        layout="slot ,prev, pager, next"
                        :background="false"
                    />
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { onMounted, reactive, ref, nextTick, PropType } from "vue";
import { message } from "ant-design-vue";
import { Pagination } from "@/components/list";
import { getCouponList } from "@/api/promotion/coupon";
import { useConfigStore } from "@/store/config";
import { useCouponInfoMap } from "@/hooks/useCouponInfoMap";
import { CouponFilterParams, CouponFilterState } from "@/types/promotion/coupon";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const props = defineProps({
    selectedIds: {
        type: Array as PropType<number[]>,
        default: () => []
    },
    isMultiple: {
        type: Boolean,
        default: true
    },
    isDialog: { type: Boolean, default: false },
    visible: { type: Boolean, default: false }
});

const emit = defineEmits(["submitCallback", "okType", "close", "callback"]);
emit("okType", false);

const config: any = useConfigStore();
const { couponInfoMap, fetchCouponInfo } = useCouponInfoMap();

const tableRef = ref<any>(null);
const filterState = ref<CouponFilterState[]>([]);
const loading = ref(true);
const total = ref(0);
const isPaginationChange = ref(false);
const selectedRows = ref<CouponFilterState[]>([]);

const filterParams = reactive<CouponFilterParams & { isShow?: number }>({
    page: 1,
    size: config.get("pageSize"),
    sortField: "",
    sortOrder: "",
    keyword: "",
    isShow: 1
});

const loadFilter = async () => {
    loading.value = true;
    try {
        const result: any = await getCouponList({ ...filterParams });
        filterState.value = result.records || [];
        total.value = result.total || 0;
    } catch (error: any) {
        message.error(error?.message || t('获取优惠券列表失败'));
    } finally {
        initChecked();
        loading.value = false;
    }
};

const initChecked = () => {
    isPaginationChange.value = true;
    nextTick(() => {
        if (selectedRows.value.length > 0 && tableRef.value) {
            filterState.value.forEach((row) => {
                const matched = selectedRows.value.some((s) => s.couponId === row.couponId);
                if (matched) {
                    tableRef.value.toggleRowSelection(row, true);
                }
            });
        }
        isPaginationChange.value = false;
    });
};

const onPageChanged = async () => {
    await loadFilter();
};

const onSearchSubmit = () => {
    filterParams.page = 1;
    loadFilter();
};

const onSelectionChange = (selection: CouponFilterState[]) => {
    if (isPaginationChange.value) return;
    if (!props.isMultiple) return;
    const currentPageIds = filterState.value.map((r) => r.couponId);
    selectedRows.value = selectedRows.value.filter((r) => !currentPageIds.includes(r.couponId));
    selectedRows.value.push(...selection);
    emit("okType", selectedRows.value.length > 0);
};

const selectRow = (selection: any, val: CouponFilterState) => {
    if (props.isMultiple) return;
    tableRef.value.clearSelection();
    if (selection.length === 0) {
        selectedRows.value = [];
        emit("okType", false);
        return;
    }
    tableRef.value.toggleRowSelection(val, true);
    selectedRows.value = [val];
    emit("okType", true);
};

const initSelectedFromProps = async () => {
    if (!props.selectedIds?.length) {
        emit("okType", false);
        return;
    }
    await fetchCouponInfo(props.selectedIds);
    selectedRows.value = props.selectedIds
        .map((id) => couponInfoMap.value.get(id))
        .filter((v): v is CouponFilterState => Boolean(v));
    emit("okType", selectedRows.value.length > 0);
};

onMounted(async () => {
    await initSelectedFromProps();
    loadFilter();
});

const onFormSubmit = () => {
    emit("submitCallback", selectedRows.value);
};

defineExpose({ onFormSubmit });
</script>

<style lang="less" scoped>
.pagination-con {
    background: #fff;
}
</style>
