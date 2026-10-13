/**
 * @file 商品分组列表页面
 * @description 商品分组管理列表，支持添加、编辑、删除商品分组。
 * @features
 * - 分组名称搜索
 * - 添加商品分组
 * - 编辑分组信息
 * - 批量删除分组
 * - 分组别名和描述展示
 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <el-form :model="filterParams" @submit.native.prevent="onSearchSubmit">
                        <div class="list-table-tool-row">
                            <div class="simple-form-warp">
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <div class="control-container">
                                            <TigInput
                                                v-model="filterParams.keyword"
                                                name="keyword"
                                                :placeholder="$t('输入分组名称')"
                                                clearable
                                                @clear="onSearchSubmit"
                                            >
                                                <template #append>
                                                    <el-button @click="onSearchSubmit"><span class="iconfont icon-chakan1"></span> </el-button>
                                                </template>
                                            </TigInput>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <DialogForm
                                            :params="{ act: 'add' }"
                                            isDrawer
                                            path="product/productGroup/Info"
                                            :title="$t('添加分组')"
                                            width="600px"
                                            @okCallback="loadFilter"
                                        >
                                            <el-button type="primary">{{ $t('添加分组') }}</el-button>
                                        </DialogForm>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <el-space>
                                            <el-popconfirm :title="$t('您确认要批量删除所选数据吗？')" @confirm="onBatchSubmit('del')">
                                                <template #reference>
                                                    <el-button :disabled="selectedIds.length === 0">{{ $t('批量删除') }}</el-button>
                                                </template>
                                            </el-popconfirm>
                                            <span v-if="selectedIds.length > 0"> {{ $t('已选择：') }}<b>{{ selectedIds.length }}</b> {{ $t('项') }} </span>
                                        </el-space>
                                    </div>
                                </div>
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
                            row-key="productGroupId"
                            @selection-change="onSelectChange"
                            show-overflow-tooltip
                            @sort-change="onSortChange"
                        >
                            <el-table-column type="selection" width="32" />
                            <el-table-column :label="$t('分组名称')" prop="productGroupName"> </el-table-column>
                            <el-table-column :label="$t('分组别名')" prop="productGroupSn"> </el-table-column>
                            <el-table-column prop="productGroupDescription" :label="$t('分组描述')"> </el-table-column>
                            <el-table-column :label="$t('添加时间')" prop="addTime" sortable="custom"> </el-table-column>
                            <el-table-column :width="150" fixed="right" :label="$t('操作')">
                                <template #default="{ row }">
                                    <div style="position: relative">
                                        <DialogForm
                                            :params="{ act: 'detail', id: row.productGroupId }"
                                            isDrawer
                                            path="product/productGroup/Info"
                                            :title="$t('编辑分组')"
                                            width="600px"
                                            @okCallback="loadFilter"
                                        >
                                            <a class="btn-link">{{ $t('编辑') }}</a>
                                        </DialogForm>
                                        <el-divider direction="vertical" />
                                        <DeleteRecord :params="{ id: row.productGroupId }" :requestApi="delProductGroup" @afterDelete="loadFilter"
                                            >{{ $t('删除') }}</DeleteRecord
                                        >
                                    </div>
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
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { DialogForm } from "@/components/dialog";
import { DeleteRecord, Pagination } from "@/components/list";
import { useConfigStore } from "@/store/config";
import { useListRequest } from "@/hooks/useListRequest";
import { ProductGroupFilterParams, ProductGroupFilterState } from "@/types/product/productGroup";
import { batchSubmit, delProductGroup, getProductGroupList } from "@/api/product/productGroup";
const config: any = useConfigStore();
const {
    listData: filterState,
    loading,
    total,
    selectedIds,
    filterParams,
    loadData: loadFilter,
    onSearchSubmit,
    onSortChange,
    onSelectChange,
    onBatchAction
} = useListRequest<ProductGroupFilterState, ProductGroupFilterParams>({
    apiFunction: getProductGroupList,
    idKey: "productGroupId",
    defaultParams: {
        sortField: "",
        sortOrder: "",
        keyword: "",
        page: 1,
        size: config.get("pageSize")
    }
});
/**
 * @description 批量操作处理
 * @param action 操作类型：'del'-批量删除
 */
const onBatchSubmit = async (action: string) => {
    await onBatchAction(action, batchSubmit);
};

// 初始化加载
loadFilter();
</script>
