<template>
    <div class="container" style="position: relative; height: 100%">
        <div class="content_wrapper">
            <div class="" style="display: flex; flex-direction: column; width: 100%; height: 100%; position: absolute">
                <div class="list-table-tool lyecs-search-warp" style="flex: 0">
                    <el-form :model="filterParams" name="form" @finish="searchSubmit" @keyup.enter="searchSubmit" @submit.prevent>
                        <div class="list-table-tool-row" style="margin-bottom: 5px">
                            <div class="list-table-tool-col">
                                <el-space>
                                    <TigInput width="230px" v-model="filterParams.keyword" clearable :placeholder="$t('请输入页面名称')" @clear="searchSubmit" />
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
                                row-key="decorateId"
                                @select="selectRow"
                                @select-all="handleSelectAll"
                                @selection-change="onSelectChange"
                            >
                                <el-table-column :selectable="isSelectable" :reserve-selection="true" type="selection" width="32" />
                                <el-table-column :label="props.type === 'bindCustom' ? $t('PC端页面') : $t('移动端页面')" prop="decorateTitle">
                                    <template #default="{ row }">
                                        <div class="flex">
                                            <div>
                                                <p>{{ row.decorateTitle }}</p>
                                            </div>
                                        </div>
                                    </template>
                                </el-table-column>
                                <el-table-column :label="$t('更新时间')" prop="updateTime" :min-width="150"></el-table-column>
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
import { onMounted, ref, nextTick } from "vue";
import { message } from "ant-design-vue";
import { Pagination } from "@/components/list";
import type { FilterParams, PcCustomPageFilterState } from "@/types/decorate/pcCustomPage.d";
import { useConfigStore } from "@/store/config";
import { getH5DecorateList, getCustomDecorateList } from "@/api/decorate/pcCustomPage";

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
    type: {
        type: String,
        default: "bindH5" // bindH5, bindCustom
    },
    bindedH5Id: {
        type: Number,
        default: 0
    }
});
const emit = defineEmits(["submitCallback", "callback", "okType", "update:modelValue"]);
emit("okType", false);

const selectedIds = ref<number[]>([]);
const loading = ref(true); //列表加载中
const total = ref<number>(0);
const config = useConfigStore();
const filterState = ref<PcCustomPageFilterState[]>([]);
// 选中的数据
const selectionList = ref<PcCustomPageFilterState[]>([]);

const filterParams = ref<FilterParams>({
    page: 1,
    size: config.get("pageSize")
});

// 获取列表的查询结果
const loadFilter = async () => {
    loading.value = true;
    try {
        const fetchApi = props.type === "bindCustom" ? getCustomDecorateList : getH5DecorateList;
        const result = await fetchApi(filterParams.value);
        filterState.value = result.records;
        total.value = result.total;
        console.log(filterState.value);
        
        // 如果存在 bindedH5Id，默认勾选匹配的项
        if (props.bindedH5Id && props.bindedH5Id > 0) {
            nextTick(() => {
                const matchedRow = filterState.value.find((item: any) => item.decorateId === props.bindedH5Id || item.id === props.bindedH5Id);
                if (matchedRow && tableRef.value) {
                    tableRef.value.toggleRowSelection(matchedRow, true);
                    // 更新选中状态
                    const matchedId = (matchedRow as any).decorateId || (matchedRow as any).id;
                    if (props.isMultiple) {
                        selectedIds.value = [matchedId];
                        emit("okType", true);
                    } else {
                        selectedIds.value = [matchedId];
                        emit("update:modelValue", matchedRow);
                        emit("okType", true);
                    }
                }
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
            idsSet.push(item.decorateId);
        });
        selectedIds.value = idsSet;
        emit("okType", selectedIds.value.length > 0);
    }
};

const selectRow = (selection: any, val: any) => {
    // 获取选中的数据
    selectionList.value = selection;

    if (!props.isMultiple) {
        // 单选
        tableRef.value.clearSelection();
        if (selection.length == 0) {
            selectedIds.value = []; // 清空data中绑定的selectedRow
            return;
        }
        tableRef.value.toggleRowSelection(val, true);
        selectedIds.value.length = 0;
        selectedIds.value.push(val.decorateId);
        emit("okType", selectedIds.value.length > 0);
    }
    emit("update:modelValue", val);
};

//监听权选
const handleSelectAll = (selection: any) => {
    // 获取选中的数据
    selectionList.value = selection;
};

const isSelectable = (row: any, index: number) => {
    // 排除重复项
    return !props.selectedIds?.includes(row.decorateId);
};

// 弹窗回调
const onFormSubmit = () => {
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

:deep(.hide-checkbox .el-table__header-wrapper .el-table__header .el-checkbox) {
    display: none;
}
</style>
