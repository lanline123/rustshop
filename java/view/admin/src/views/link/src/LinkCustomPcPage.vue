<template>
    <div class="container" style="position: relative; height: 100%">
        <div class="content_wrapper">
            <div class="" style="position: absolute; top: 0; bottom: 0; overflow-y: auto; width: 100%">
                <div class="list-table-tool lyecs-search-warp" style="position: absolute; top: 0">
                    <el-form :model="filterParams" name="form" @submit.prevent>
                        <div class="list-table-tool-row" style="margin-bottom: 5px">
                            <div class="list-table-tool-col">
                                <el-space>
                                    <TigInput v-model="filterParams.keyword" :placeholder="$t('请输入搜索关键字')" @keyup.enter="searchSubmit" />
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
                                @select="selectRow"
                                @selection-change="onSelectChange"
                                :total="total"
                                :loading="loading"
                            >
                                <el-table-column :selectable="isSelectable" type="selection" width="32" />
                                <el-table-column :label="$t('装修页面名称')" prop="decorateTitle"></el-table-column>
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

<script setup lang="ts">
import "@/style/css/list.less";
import { nextTick, onMounted, reactive, ref } from "vue";
import { message } from "ant-design-vue";
import { Pagination } from "@/components/list";
import { getDecorateList } from "@/api/decorate/mobileDecorate";
import { FilterState, FilterParams } from "@/types/decorate/mobileDecorate.d";
import { useConfigStore } from "@/store/config";

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
const emit = defineEmits(["submitCallback", "okType"]);
emit("okType", false);
const linkSelectData = defineModel("linkSelectData");

// 基本参数定义
const loading = ref(true); //列表加载中
const selectedIds = ref<number[]>([]); //钩选项
const total = ref<number>(0);
const config: any = useConfigStore();
const filterState = ref<FilterState[]>([]);
const filterParams = reactive<FilterParams>({
    page: 1,
    size: config.get("pageSize"),
    decorateType: "2,3", // PC自定义装修页类型
    status: 1, // 只查询已发布的数据
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
    const currentRow = filterState.value.find((item) => item.decorateId === currentId);
    if (currentRow) {
        tableRef.value?.clearSelection();
        tableRef.value?.toggleRowSelection(currentRow, true);
    }
};

// 获取列表的查询结果
const loadFilter = async () => {
    loading.value = true;
    try {
        const result = await getDecorateList({ ...filterParams });
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

const onSelectChange = (e: FilterState[]) => {
    if (props.isMultiple) {
        selectedIds.value = e.map((item) => item.decorateId);
        emit("okType", selectedIds.value.length > 0);
    }
};

const tableRef: any = ref();
const selectRow = (selection: FilterState[], val: FilterState) => {
    if (!props.isMultiple) {
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
    linkSelectData.value = {
        path: `activity?id=${val.decorateId}`,
        label: t('自定义装修页'),
        name: val.decorateTitle,
        id: val.decorateId,
        data: {
            id: val.decorateId,
            name: val.decorateTitle
        }
    };
};

const isSelectable = (row: FilterState) => {
    // 排除重复项
    return !props.selectedIds.includes(row.decorateId);
};

// 弹窗回调
const onFormSubmit = () => {
    // 弹窗确认按钮提交
    emit("submitCallback", selectedIds.value);
};

defineExpose({
    onFormSubmit
});
</script>

<style lang="less" scoped>
.pagination-con {
    background: #fff;
}

.content_wrapper {
    :deep(.el-table__header-wrapper .el-table-column--selection .el-checkbox) {
        display: none !important;
    }
}
</style>
