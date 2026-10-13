<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <el-form :model="filterParams">
                        <div class="list-table-tool-row">
                            <div class="list-table-tool-col">
                                <el-space>
                                    <el-button v-if="filterParams.parentId > 0" @click="backToParentCat"
                                        ><i class="iconfont-admin icon-diaotou" style="font-size: 14px; padding-right: 5px"></i> {{ $t("返回上一级") }}
                                    </el-button>
                                    <DialogForm
                                        :params="{ act: 'add', regionId: filterParams.parentId }"
                                        isDrawer
                                        path="setting/region/Info"
                                        :title="$t('添加地区')"
                                        width="580px"
                                        @okCallback="loadFilter"
                                    >
                                        <el-button type="primary">{{ $t("添加地区") }}</el-button>
                                    </DialogForm>

                                    <el-popconfirm :title="$t('您确认要批量删除所选数据吗？')" @confirm="onBatchSubmit('del')">
                                        <template #reference>
                                            <el-button :disabled="selectedIds.length === 0">{{ $t("批量删除") }}</el-button>
                                        </template>
                                    </el-popconfirm>
                                    <span v-if="selectedIds.length > 0">
                                        {{ $t("已选择：") }}<b>{{ selectedIds.length }}</b> {{ $t("项") }}
                                    </span>
                                </el-space>
                            </div>
                        </div>
                    </el-form>
                </div>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table
                            :data="filterState"
                            :loading="loading"
                            :total="total"
                            row-key="regionId"
                            @selection-change="onSelectChange"
                            @sort-change="onSortChange"
                        >
                            <el-table-column type="selection" width="32" />
                            <el-table-column :width="200" :label="$t('地区名称')" prop="regionId">
                                <template #default="{ row }">
                                    <div style="position: relative">
                                        <PopForm
                                            v-model:org-value="row.regionName"
                                            :max="200"
                                            :params="{ id: row.regionId, field: 'regionName' }"
                                            :requestApi="updateRegionField"
                                            :label="$t('地区名称')"
                                            type="textarea"
                                        >
                                            <div>{{ row.regionName }}</div>
                                        </PopForm>
                                    </div>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('首字母')">
                                <template #default="{ row }">
                                    <PopForm
                                        v-model:org-value="row.firstWord"
                                        :len="1"
                                        :params="{ id: row.regionId, field: 'firstWord' }"
                                        :requestApi="updateRegionField"
                                        :label="$t('首字母')"
                                        type="input"
                                    >
                                        <div>{{ row.firstWord || "-" }}</div>
                                    </PopForm>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('是否热门')">
                                <template #default="{ row }">
                                    <Switch v-model:checked="row.isHot" :params="{ id: row.regionId, field: 'isHot' }" :requestApi="updateRegionField" />
                                </template>
                            </el-table-column>
                            <el-table-column :width="320" fixed="right" :label="$t('操作')">
                                <template #default="{ row }">
                                    <DialogForm
                                        :params="{ act: 'detail', id: row.regionId }"
                                        isDrawer
                                        path="setting/region/Info"
                                        :title="$t('编辑地区')"
                                        width="580px"
                                        @okCallback="loadFilter"
                                    >
                                        <a class="btn-link">{{ $t("编辑") }}</a>
                                    </DialogForm>
                                    <el-divider direction="vertical" />
                                    <a class="btn-link" @click="onSearch(row.parentId, row.regionId, row.regionName)">{{ $t("子地区") }}</a>
                                    <el-divider direction="vertical" />
                                    <DialogForm
                                        :params="{ act: 'add', regionId: row.regionId }"
                                        isDrawer
                                        path="setting/region/Info"
                                        :title="$t('添加子地区')"
                                        width="600"
                                        @okCallback="loadFilter"
                                    >
                                        <a class="btn-link">{{ $t("添加子地区") }}</a>
                                    </DialogForm>
                                    <el-divider direction="vertical" />
                                    <DeleteRecord :params="{ id: row.regionId }" :requestApi="delRegion" @afterDelete="loadData(row.parentId)"
                                        >{{ $t("删除") }}
                                    </DeleteRecord>
                                </template>
                            </el-table-column>
                            <template #empty>
                                <div class="empty-warp">
                                    <div v-if="!loading" class="empty-bg">{{ $t("暂无数据") }}</div>
                                </div>
                            </template>
                        </el-table>
                    </a-spin>
                    <div v-if="total > 0" class="pagination-con">
                        <Pagination v-model:page="filterParams.page" v-model:size="filterParams.size" :total="total" @callback="onChangePage" />
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { DialogForm } from "@/components/dialog";
import { PopForm } from "@/components/pop-form";
import { onMounted, reactive, ref } from "vue";
import { DeleteRecord, Pagination, Switch } from "@/components/list";
import { useConfigStore } from "@/store/config";
import type { RegionFilterParams, RegionFilterState } from "@/types/setting/region";
import { batchSubmit, delRegion, getRegionList, updateRegionField } from "@/api/setting/region";
import { useAppStore } from "@/store/app";
import { isOverseas } from "@/utils/version";
const appStore = useAppStore();
const config: any = useConfigStore();
import { useListRequest } from "@/hooks/useListRequest";
const {
    listData: filterState,
    loading,
    total,
    selectedIds,
    filterParams,
    loadData: loadFilter,
    onSortChange,
    onSelectChange,
    onBatchAction,
    resetParams
} = useListRequest<RegionFilterState, RegionFilterParams>({
    apiFunction: getRegionList,
    idKey: "regionId",
    defaultParams: {
        sortField: "",
        sortOrder: "",
        keyword: "",
        parentId: 0,
        page: 1,
        size: config.get("pageSize")
    }
});
// 批量操作
const onBatchSubmit = async (action: string) => {
    await onBatchAction(action, batchSubmit);
};

// 初始化加载
loadFilter();
const loadData = (parentId?: number | object) => {
    if (parentId && typeof parentId === "number") {
        filterParams.parentId = parentId;
    }
    loadFilter();
};
const parentIds = ref<Array<number>>([]);
const parentNames = ref<Array<string>>([]);
const onChangePage = () => {
    loadFilter();
};
const regionName = ref<string>("");
const onSearch = (pid: number, id: number, name: string) => {
    parentIds.value.push(pid);
    parentNames.value.push(name);
    filterParams.parentId = id;
    regionName.value = name;
    loadFilter();
};

const backToParentCat = () => {
    filterParams.parentId = parentIds.value[parentIds.value.length - 1];
    regionName.value = parentNames.value[parentNames.value.length - 1];
    parentIds.value.pop();
    parentNames.value.pop();
    loadFilter();
};
</script>
