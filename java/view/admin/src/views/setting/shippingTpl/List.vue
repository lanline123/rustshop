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
                                            :placeholder="$t('输入模板名称')"
                                            @keyup.enter="onSearchSubmit"
                                            clearable
                                            @clear="onSearchSubmit"
                                        >
                                            <template #append>
                                                <el-button @click="onSearchSubmit"><span class="iconfont icon-chakan1"></span></el-button>
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
                                        path="setting/shippingTpl/Info"
                                        :title="$t('添加运费模板')"
                                        width="900px"
                                        @okCallback="loadFilter"
                                    >
                                        <el-button type="primary">{{ $t('添加运费模板') }}</el-button>
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
                </div>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table
                            :data="filterState"
                            :loading="loading"
                            :total="total"
                            row-key="shippingTplId"
                            @selection-change="onSelectChange"
                            @sort-change="onSortChange"
                        >
                            <el-table-column type="selection" width="32" />
                            <el-table-column :label="$t('模板名称')">
                                <template #default="{ row }">
                                    <PopForm
                                        v-model:org-value="row.shippingTplName"
                                        :max="10"
                                        :params="{ id: row.shippingTplId, field: 'shippingTplName' }"
                                        :requestApi="updateShippingTplField"
                                        :label="$t('模板名称')"
                                        type="input"
                                    >
                                        <div class="flex flex-align-center" style="gap: 8px; flex-wrap: wrap; white-space: nowrap">
                                            <span>{{ row.shippingTplName }}</span>
                                            <el-tag v-if="row.isDefault === 1" size="small" effect="plain" type="primary">{{ $t('默认模版') }}</el-tag>
                                        </div>
                                    </PopForm>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('是否包邮')" prop="isFree">
                                <template #default="{ row }">
                                    {{ row.isFree === 0 ? $t('自定义运费') : $t('卖家承担运费') }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('计价方式')" prop="isSupportCod">
                                <template #default="{ row }">
                                    {{ row.pricingType === 1 ? $t('按件数') : $t('按重量') }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('发货时间')" prop="shippingTime" sortable="custom"></el-table-column>
                            <el-table-column :label="$t('是否默认')" prop="isSupportCod">
                                <template #default="{ row }">
                                    {{ row.isDefault === 1 ? $t('是') : "--" }}
                                </template>
                            </el-table-column>
                            <el-table-column :width="210" fixed="right" :label="$t('操作')">
                                <template #default="{ row }">
                                    <DialogForm
                                        :params="{ act: 'detail', id: row.shippingTplId }"
                                        isDrawer
                                        path="setting/shippingTpl/Info"
                                        :title="$t('编辑运费模板')"
                                        width="900px"
                                        @okCallback="loadFilter"
                                    >
                                        <a class="btn-link">{{ $t('编辑') }}</a>
                                    </DialogForm>
                                    <el-divider direction="vertical" />
                                    <DeleteRecord
                                        :disabled="row.isDefault === 1"
                                        :params="{ id: row.shippingTplId }"
                                        :requestApi="setDefault"
                                        :title="$t('确认将该模版设为默认运费模板吗？')"
                                        :message="$t('已设为默认模版')"
                                        @afterDelete="loadFilter"
                                    > {{ $t('设为默认') }} </DeleteRecord>
                                    <el-divider direction="vertical" />
                                    <DeleteRecord :params="{ id: row.shippingTplId }" :requestApi="delShippingTpl" @afterDelete="loadFilter"
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
import { DeleteRecord, Pagination } from "@/components/list";
import { useConfigStore } from "@/store/config";
import type { ShippingTplFilterParams, ShippingTplFilterState } from "@/types/setting/shippingTpl";
import { batchSubmit, delShippingTpl, getShippingTplList, setDefault, updateShippingTplField } from "@/api/setting/shippingTpl";
// shippingTplId
const config: any = useConfigStore();
import { useListRequest } from "@/hooks/useListRequest";
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
} = useListRequest<ShippingTplFilterState, ShippingTplFilterParams>({
    apiFunction: getShippingTplList,
    idKey: "shippingTplId",
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
