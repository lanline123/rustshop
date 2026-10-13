/**
 * @file 商品分组选择器
 * @description 用于选择商品分组的弹窗组件
 * @features
 * - 分组名称搜索
 * - 支持单选和多选模式
 * - 显示分组商品数量
 * - 支持买家秀等场景回显已选项
 */
<template>
    <div class="container" style="position: relative; height: 100%">
        <div class="content_wrapper">
            <div class="" style="display: flex; flex-direction: column; width: 100%; height: 100%; position: absolute">
                <div class="list-table-tool lyecs-search-warp" style="flex: 0">
                    <el-form :model="filterParams" name="form" @finish="searchSubmit">
                        <div class="list-table-tool-row" style="margin-bottom: 5px">
                            <div class="list-table-tool-col">
                                <el-space>
                                    <TigInput v-model="filterParams.keyword" :placeholder="$t('商品分组名称')" />
                                    <el-button @click="searchSubmit">{{ $t('搜索') }}</el-button>
                                </el-space>
                            </div>
                            <div class="list-table-tool-col"></div>
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
                                row-key="productGroupId"
                                @select="selectRow"
                                @selection-change="onSelectChange"
                            >
                                <el-table-column :selectable="isSelectable" type="selection" width="32" />
                                <el-table-column :label="$t('分组名称')" prop="productGroupName"> </el-table-column>
                                <el-table-column :label="$t('分组商品数量')" prop="productIds" align="center">
                                    <template #default="{ row }">
                                        {{ $t('{length}个', { length: row.productIds.length }) }}</template>
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
                        @callback="loadFilter"
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
import { nextTick, onMounted, reactive, ref } from "vue";
import { message } from "ant-design-vue";
import { Pagination } from "@/components/list";
import { useConfigStore } from "@/store/config";
import { getProductGroupList } from "@/api/product/productGroup";
import { ProductGroupFilterParams, ProductGroupFilterState } from "@/types/product/productGroup";
const props = defineProps({
    // 已选择的项，用于排除重复，禁止选择
    selectedIds: { type: Array, default: [] },
    modelValue: {
        type: Object,
        default: {}
    },
    isMultiple: {
        type: Boolean,
        default: true
    },
    index: {
        type: Number,
        default: 0
    },
    buyShowSelectedIds: {
        type: Array,
        default: []
    }
});
const emit = defineEmits(["submitCallback", "okType", "update:modelValue"]);
emit("okType", false);
const selectedIds = ref<number[]>([]);
const loading = ref(true); //列表加载中
const total = ref<number>(0);
const config = useConfigStore();
const filterState = ref<ProductGroupFilterState[]>([]);
const filterParams = reactive<ProductGroupFilterParams>({
    page: 1,
    size: (config as any).get("pageSize"),
    sortField: "",
    sortOrder: "",
    keyword: ""
});

// 获取列表的查询结果
const loadFilter = async () => {
    loading.value = true;
    try {
        const result = await getProductGroupList({ ...filterParams });
        filterState.value = result.records;
        total.value = result.total;
        // 如果 buyShowSelectedIds 有值，则自动勾选对应行
        if (props.buyShowSelectedIds && props.buyShowSelectedIds.length > 0) {
            const buyShowSelectedIds = props.buyShowSelectedIds?.map(item => item?.productGroupId || item)
            // 等待 DOM 更新后执行勾选操作
            nextTick(() => {
                filterState.value.forEach(row => {
                    if (buyShowSelectedIds.includes(row.productGroupId)) {
                        tableRef.value.toggleRowSelection(row, true);
                    }
                });
            });
        }
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
const searchSubmit = () => {
    loadFilter();
};
const tableRef: any = ref();

const onSelectChange = (e: any) => {
    if (props.isMultiple) {
        let idsSet: any = [];
        e.forEach((item: any) => {
            idsSet.push(item.productGroupId);
        });
        selectedIds.value = idsSet;
        emit("okType", selectedIds.value.length > 0);
    }
};
const selectRow = (selection: any, val: any) => {
    //手动触发该事件
    if (!props.isMultiple) {
        // 单选
        tableRef.value.clearSelection();
        if (selection.length == 0) {
            selectedIds.value = []; // 清空data中绑定的selectedRow
            return;
        }
        tableRef.value.toggleRowSelection(val, true);
        selectedIds.value.length = 0;
        selectedIds.value.push(val.productGroupId);
        emit("okType", selectedIds.value.length > 0);
    }
    emit("update:modelValue", val);
};
const isSelectable = (row: any, index: number) => {
    // 买家秀修改商品分组，回显已选择的商品，不禁用选择
    if(props.buyShowSelectedIds?.length) {
        return true;
    } else {
        // 排除重复项
        return !props.selectedIds?.includes(row.productGroupId); // Column configuration not to be checked
    }
};

// 弹窗回调
const onFormSubmit = () => {
    emit("submitCallback", { ids: selectedIds.value, index: props.index });
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

:deep(.hide-checkbox .el-table__header-wrapper .el-table__header .el-checkbox) {
    display: none;
}
</style>
