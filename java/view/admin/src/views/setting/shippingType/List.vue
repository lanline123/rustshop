<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <div class="list-table-tool-row">
                        <div class="simple-form-warp">
                            <div class="simple-form-field">
                                <div class="form-group">
                                    <div class="control-container">
                                        <TigInput
                                            v-model="filterParams.keyword"
                                            name="keyword"
                                            :placeholder="$t('输入配送类型')"
                                            @keyup.enter="onSearchSubmit"
                                            clearable
                                            @clear="onSearchSubmit"
                                        >
                                            <template #append>
                                                <el-button @click="onSearchSubmit"><span
                                                    class="iconfont icon-chakan1"></span></el-button>
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
                                        path="setting/shippingType/Info"
                                        :title="$t('添加配送类型')"
                                        width="580px"
                                        @okCallback="loadFilter"
                                    >
                                        <el-button type="primary">{{ $t('添加配送方式') }}</el-button>
                                    </DialogForm>
                                </div>
                            </div>
                            <div class="simple-form-field">
                                <div class="form-group">
                                    <el-space>
                                        <el-popconfirm :title="$t('您确认要批量删除所选数据吗？')"
                                                       @confirm="onBatchSubmit('del')">
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
                </div>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table
                            :data="filterState"
                            :loading="loading"
                            :total="total"
                            row-key="shippingTypeId"
                            @selection-change="onSelectChange"
                            @sort-change="onSortChange"
                        >
                            <el-table-column type="selection" width="32" />
                            <el-table-column :label="$t('分类名称')">
                                <template #default="{ row }">
                                    <PopForm
                                        v-model:org-value="row.shippingTypeName"
                                        :max="10"
                                        :params="{ id: row.shippingTypeId, field: 'shippingTypeName' }"
                                        :requestApi="updateShippingTypeField"
                                        :label="$t('分类名称')"
                                        type="input"
                                    >
                                        <div>{{ row.shippingTypeName }}</div>
                                    </PopForm>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('分类描述')" prop="shippingTypeDesc">
                                <template #default="{ row }">
                                    <PopForm
                                        v-model:org-value="row.shippingTypeDesc"
                                        :max="0"
                                        :params="{ id: row.shippingTypeId, field: 'shippingTypeDesc' }"
                                        :requestApi="updateShippingTypeField"
                                        :label="$t('分类描述')"
                                        type="input"
                                    >
                                        <div>{{ row.shippingTypeDesc }}</div>
                                    </PopForm>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('默认物流')" prop="logisticsName"></el-table-column>
                            <el-table-column :label="$t('货到付款')" prop="isSupportCod" sortable="custom">
                                <template #default="{ row }">
                                    <Switch
                                        v-model:checked="row.isSupportCod"
                                        :params="{ id: row.shippingTypeId, field: 'isSupportCod' }"
                                        :requestApi="updateShippingTypeField"
                                    />
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('排序')" prop="sortOrder" sortable="custom">
                                <template #default="{ row }">
                                    <PopForm
                                        v-model:org-value="row.sortOrder"
                                        :params="{ id: row.shippingTypeId, field: 'sortOrder' }"
                                        :requestApi="updateShippingTypeField"
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
                                        :params="{ act: 'detail', id: row.shippingTypeId }"
                                        isDrawer
                                        path="setting/shippingType/Info"
                                        :title="$t('编辑配送类型')"
                                        width="580px"
                                        @okCallback="loadFilter"
                                    >
                                        <a class="btn-link">{{ $t('编辑') }}</a>
                                    </DialogForm>
                                    <el-divider direction="vertical" />
                                    <DeleteRecord :params="{ id: row.shippingTypeId }" :requestApi="delShippingType"
                                                  @afterDelete="loadFilter"
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
                        <Pagination v-model:page="filterParams.page" v-model:size="filterParams.size" :total="total"
                                    @callback="loadFilter" />
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
import { message } from "ant-design-vue";
import { useConfigStore } from "@/store/config";
import type { ShippingTypeFilterParams, ShippingTypeFilterState } from "@/types/setting/shippingType";
import { batchSubmit, delShippingType, getShippingTypeList, updateShippingTypeField } from "@/api/setting/shippingType";
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
} = useListRequest<ShippingTypeFilterState, ShippingTypeFilterParams>({
    apiFunction: getShippingTypeList,
    idKey: "shippingTypeId",
    defaultParams: {
        sortField: "",
        sortOrder: "",
        keyword: "",
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
