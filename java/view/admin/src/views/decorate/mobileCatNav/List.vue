<template>
    <div class="container">
        <div class="content_wrapper">
            <!-- <el-tabs v-model="activeKey" class="lyecs-tabs" tab-position="top" @tab-change="onTabChange">
                <el-tab-pane :name="1" label="主导航"></el-tab-pane>
                <el-tab-pane :name="2" label="顶部小导航"></el-tab-pane>
                <el-tab-pane :name="3" label="底部导航"></el-tab-pane>
                <el-tab-pane :name="4" label="侧边导航"></el-tab-pane>
            </el-tabs> -->
            <div class="container">
                <div class="lyecs-table-list-warp">
                    <div class="list-table-tool lyecs-search-warp">
                        <el-form :model="filterParams">
                            <div class="list-table-tool-row">
                                <div class="list-table-tool-col">
                                    <el-space>
                                        <DialogForm
                                            :params="{ act: 'add' }"
                                            :title="$t('添加首页分类栏')"
                                            isDrawer
                                            path="decorate/mobileCatNav/Info"
                                            width="600px"
                                            @okCallback="loadFilter"
                                        >
                                            <el-button type="primary">{{ $t('添加首页分类栏') }}</el-button>
                                        </DialogForm>
                                        <el-popconfirm :title="$t('您确认要批量删除所选数据吗？')" @confirm="onBatchSubmit('del')">
                                            <template #reference>
                                                <el-button :disabled="selectedIds.length === 0">{{ $t('批量删除') }}</el-button>
                                            </template>
                                        </el-popconfirm>
                                        <span v-if="selectedIds.length > 0"> {{ $t('已选择：') }}<b>{{ selectedIds.length }}</b> {{ $t('项') }} </span>
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
                                row-key="mobileCatNavId"
                                @selection-change="onSelectChange"
                                @sort-change="onSortChange"
                            >
                                <el-table-column type="selection" width="32" />
                                <el-table-column :width="200" :label="$t('首页分类栏名称')" prop="categoryName"></el-table-column>
                                <el-table-column :width="100" :label="$t('分类颜色')" prop="categoryName" a>
                                    <template #default="{ row }">
                                        <div class="color_card" :style="{ backgroundColor: row.catColor }"></div>
                                    </template>
                                </el-table-column>
                                <el-table-column :label="$t('是否显示')" prop="isShow">
                                    <template #default="{ row }">
                                        <Switch
                                            v-model:checked="row.isShow"
                                            :params="{ id: row.mobileCatNavId, field: 'isShow' }"
                                            :requestApi="updateMobileCatNavFiled"
                                        />
                                    </template>
                                </el-table-column>
                                <el-table-column :label="$t('排序')" prop="sortOrder" sortable="custom">
                                    <template #default="{ row }">
                                        <PopForm
                                            v-model:org-value="row.sortOrder"
                                            :params="{ id: row.mobileCatNavId, field: 'sortOrder' }"
                                            :requestApi="updateMobileCatNavFiled"
                                            :extra="$t('默认值为50，数值越小，排序越靠前')"
                                            :label="$t('排序')"
                                            type="integer"
                                        >
                                            <div>{{ row.sortOrder }}</div>
                                        </PopForm>
                                    </template>
                                </el-table-column>
                                <el-table-column :width="150" fixed="right" :label="$t('操作')">
                                    <template #default="{ row }">
                                        <DialogForm
                                            :params="{ act: 'detail', id: row.mobileCatNavId }"
                                            :title="$t('编辑首页分类栏')"
                                            isDrawer
                                            path="decorate/mobileCatNav/Info"
                                            width="600px"
                                            @okCallback="loadFilter"
                                        >
                                            <a class="btn-link">{{ $t('编辑') }}</a>
                                        </DialogForm>
                                        <el-divider direction="vertical" />
                                        <DeleteRecord :params="{ id: row.mobileCatNavId }" :requestApi="delMobileCatNav" @afterDelete="loadFilter"
                                            >{{ $t('删除') }} </DeleteRecord>
                                    </template>
                                </el-table-column>
                                <template #empty>
                                    <div class="empty-warp">
                                        <div v-if="!loading" class="empty-bg">{{ $t('暂无数据') }}</div>
                                    </div>
                                </template>
                            </el-table>
                        </a-spin>
                        <div v-if="total > 0" class="pagination-con">
                            <Pagination v-model:page="filterParams.page" v-model:size="filterParams.size" :total="total" @callback="loadFilter" />
                        </div>
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
import { DeleteRecord, Pagination, Switch } from "@/components/list";
import { useConfigStore } from "@/store/config";
import { MobileCatNavFilterParams, MobileCatNavFilterState } from "@/types/decorate/mobileCatNav.d";
import { batchSubmit, delMobileCatNav, getMobileCatNavList, updateMobileCatNavFiled } from "@/api/decorate/mobileCatNav";
import { useListRequest } from "@/hooks/useListRequest";
const config: any = useConfigStore();
const {
    listData: filterState,
    loading,
    total,
    selectedIds,
    filterParams,
    loadData: loadFilter,
    onSortChange,
    onSelectChange,
    onBatchAction
} = useListRequest<MobileCatNavFilterState, MobileCatNavFilterParams>({
    apiFunction: getMobileCatNavList,
    // idKey: "id",
    idKey: "mobileCatNavId",
    defaultParams: {
        sortField: "",
        sortOrder: "",
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
</script>
<style scoped>
.color_card {
    width: 20px;
    height: 20px;
    border-radius: 3px;
}
</style>
