/**
 * @file 商品服务列表页面
 * @description 商品服务管理列表，如"正品保障"、"7天无理由退换"等商品服务标签。
 * @features
 * - 服务名称搜索
 * - 添加商品服务
 * - 行内编辑（服务名称、简介、排序）
 * - 默认显示状态开关
 * - 批量删除服务
 * - 服务图标展示
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
                                                :placeholder="$t('输入服务名称')"
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
                                            path="product/productServices/Info"
                                            :title="$t('添加商品服务')"
                                            width="600px"
                                            @okCallback="loadFilter"
                                        >
                                            <el-button type="primary">{{ $t('添加商品服务') }}</el-button>
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
                            row-key="productServiceId"
                            @selection-change="onSelectChange"
                            @sort-change="onSortChange"
                        >
                            <el-table-column type="selection" width="32" />
                            <el-table-column :width="300" :label="$t('服务名称')" prop="productServiceId">
                                <template #default="{ row }">
                                    <div style="position: relative">
                                        <PopForm
                                            :label="$t('服务名称')"
                                            type="textarea"
                                            :requestApi="updateProductServicesFiled"
                                            v-model:org-value="row.productServiceName"
                                            :params="{ id: row.productServiceId, field: 'productServiceName' }"
                                            :max="100"
                                        >
                                            <div>{{ row.productServiceName }}</div>
                                        </PopForm>
                                    </div>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('图标')">
                                <template #default="{ row }">
                                    <Image :src="row.icoImg" fit="contain" style="height: 25px" />
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('简介')" prop="productServiceDesc">
                                <template #default="{ row }">
                                    <PopForm
                                        :max="100"
                                        :label="$t('简介')"
                                        type="input"
                                        :requestApi="updateProductServicesFiled"
                                        v-model:org-value="row.productServiceDesc"
                                        :params="{ id: row.productServiceId, field: 'productServiceDesc' }"
                                    >
                                        <div>{{ row.productServiceDesc }}</div>
                                    </PopForm>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('默认显示')" prop="defaultOn" sortable="custom">
                                <template #default="{ row }">
                                    <Switch
                                        v-model:checked="row.defaultOn"
                                        :requestApi="updateProductServicesFiled"
                                        :params="{ id: row.productServiceId, field: 'defaultOn' }"
                                    />
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('排序')" prop="sortOrder" sortable="custom">
                                <template #default="{ row }">
                                    <PopForm
                                        :label="$t('排序')"
                                        type="integer"
                                        :requestApi="updateProductServicesFiled"
                                        v-model:org-value="row.sortOrder"
                                        :params="{ id: row.productServiceId, field: 'sortOrder' }"
                                        :extra="$t('默认值为50，数值越小，排序越靠前')"
                                    >
                                        <div>{{ row.sortOrder }}</div>
                                    </PopForm>
                                </template>
                            </el-table-column>
                            <el-table-column :width="150" fixed="right" :label="$t('操作')">
                                <template #default="{ row }">
                                    <DialogForm
                                        :params="{ act: 'detail', id: row.productServiceId }"
                                        isDrawer
                                        path="product/productServices/Info"
                                        :title="$t('编辑商品服务')"
                                        width="600px"
                                        @okCallback="loadFilter"
                                    >
                                        <a class="btn-link">{{ $t('编辑') }}</a>
                                    </DialogForm>
                                    <el-divider direction="vertical" />
                                    <DeleteRecord :params="{ id: row.productServiceId }" :requestApi="delProductServices" @afterDelete="loadFilter"
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
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { DialogForm } from "@/components/dialog";
import { PopForm } from "@/components/pop-form";
import { DeleteRecord, Switch, Pagination } from "@/components/list";
import { Image } from "@/components/image";
import { useConfigStore } from "@/store/config";
import { ProductServicesFilterParams, ProductServicesFilterState } from "@/types/product/productServices";
import { batchSubmit, delProductServices, getProductServicesList, updateProductServicesFiled } from "@/api/product/productServices";
import { useListRequest } from "@/hooks/useListRequest";
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
    onBatchAction,
    resetParams
} = useListRequest<ProductServicesFilterState, ProductServicesFilterParams>({
    apiFunction: getProductServicesList,
    idKey: "productServiceId",
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
