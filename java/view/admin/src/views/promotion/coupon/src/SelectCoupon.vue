<template>
    <div class="container" style="position: relative; height: 100%">
        <div class="content_wrapper">
            <div class="" style="position: absolute; top: 0; bottom: 0; overflow-y: auto; width: 100%">
                <div class="list-table-tool lyecs-search-warp" style="position: absolute; top: 0">
                    <el-form :model="filterParams" name="form" @finish="searchSubmit">
                        <div class="list-table-tool-row" style="margin-bottom: 5px">
                            <div class="list-table-tool-col">
                                <el-space>
                                    <TigInput v-model="filterParams.keyword" :placeholder="$t('请输入搜索优惠券')" />
                                    <el-button @click="searchSubmit">{{ $t('搜索') }}</el-button>
                                </el-space>
                            </div>
                            <div class="list-table-tool-col"></div>
                        </div>
                    </el-form>
                </div>
                <perfect-scrollbar class="table-container" style="position: absolute; top: 50px; bottom: 50px; overflow-y: auto; width: 100%">
                    <a-spin :spinning="loading">
                        <div style="padding-right: 15px; box-sizing: border-box">
                            <el-table
                                :class="isMultiple ? '' : 'hide-checkbox'"
                                ref="tableRef"
                                class="result-table"
                                :data="filterState"
                                row-key="couponId"
                                @select="selectRow"
                                @selection-change="onSelectChange"
                                :total="total"
                                :loading="loading"
                            >
                                <el-table-column :selectable="isSelectable" type="selection" width="32" />
                                <el-table-column :label="$t('优惠券名称')" prop="couponName"></el-table-column>
                                <template #empty>
                                    <div class="empty-warp">
                                        <div v-if="!loading" class="empty-bg">{{ $t('暂无数据') }}</div>
                                    </div>
                                </template>
                            </el-table>
                        </div>
                    </a-spin>
                </perfect-scrollbar>
                <div
                    class="pagination-con"
                    v-if="total > 0"
                    style="position: absolute; bottom: 0; justify-content: right; display: flex; width: 100%; margin: 0"
                >
                    <Pagination v-model:page="filterParams.page" v-model:size="filterParams.size" :total="total" @callback="loadFilter" />
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import "@/style/css/list.less";
import { nextTick, onMounted, reactive, ref } from "vue";
import { Modal, message, type TableColumnsType } from "ant-design-vue";
import { imageFormat } from "@/utils/format";
import { Pagination } from "@/components/list";
import { CouponFilterParams, CouponFilterState } from "@/types/promotion/coupon.d";
import { useConfigStore } from "@/store/config";
import { getCouponList } from "@/api/promotion/coupon";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const props = defineProps({
    // 已选择的项，用于排除重复，禁止选择
    selectedIds: { type: Array, default: [] },
    isMultiple: {
        type: Boolean,
        default: true
    }
});

const linkSelectData = defineModel("linkSelectData");
const emit = defineEmits(["submitCallback", "okType"]);
emit("okType", false);
// 基本参数定义
const columns: TableColumnsType = [{ title: t('商品信息'), key: "couponId", sorter: true }];
const activeKey = ref(1);
const loading = ref(true); //列表加载中

const selectedIds = ref<number[]>([]); //钩选项
const total = ref<number>(0);
const config: any = useConfigStore();
const filterState = ref<CouponFilterState[]>([]);
const filterParams = reactive<CouponFilterParams>({
    page: 1,
    size: config.get("pageSize"),
    sortField: "",
    sortOrder: "",
    keyword: ""
});
const restoreSelection = async () => {
    const currentId = Number(linkSelectData.value?.id || 0);
    if (!currentId) {
        return;
    }
    selectedIds.value = [currentId];
    emit("okType", true);
    await nextTick();
    const currentRow = filterState.value.find((item) => item.couponId === currentId);
    if (currentRow) {
        tableRef.value?.clearSelection();
        tableRef.value?.toggleRowSelection(currentRow, true);
    }
};

// 获取列表的查询结果
const loadFilter = async () => {
    loading.value = true;
    try {
        const result = await getCouponList({ ...filterParams });
        filterState.value = result.records;
        total.value = result.total;
        await restoreSelection();
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};
onMounted(() => {
    loadFilter();
});
// 参数查询
const searchSubmit = (values: any) => {
    loadFilter();
};

const onSelectChange = (e: any) => {
    if (props.isMultiple) {
        selectedIds.value = e.map((item: CouponFilterState) => item.couponId);
        emit("okType", selectedIds.value.length > 0);
    }
};
const tableRef: any = ref();
const selectRow = (selection: any, val: any) => {
    if (!props.isMultiple) {
        tableRef.value.clearSelection();
        if (selection.length == 0) {
            selectedIds.value = []; // 清空data中绑定的selectedRow
            return;
        }
        tableRef.value.toggleRowSelection(val, true);
        selectedIds.value.length = 0;
        selectedIds.value.push(val.couponId);
        emit("okType", selectedIds.value.length > 0);
        linkSelectData.value = {
            path: "coupon",
            label: t('优惠券'),
            name: val.couponName,
            id: val.couponId,
            data: {
                id: val.couponId,
            }
        };
    }
};
const isSelectable = (row: CouponFilterState) => {
    // 排除重复项
    return !props.selectedIds.includes(row.couponId); // Column configuration not to be checked
};
// 弹窗回调
const onFormSubmit = () => {
    // 弹窗确认按钮提交
    console.log(selectedIds.value);
    emit("submitCallback", selectedIds.value);
};

// 修改排序

defineExpose({
    onFormSubmit
});
</script>
<style lang="less" scoped>
.pagination-con {
    background: #fff;
}

</style>
