<template>
    <div class="lyecs-table-list-warp">
        <div class="list-table-tool lyecs-search-warp">
            <el-form :model="filterParams">
                <div class="list-table-tool-row">
                    <div class="simple-form-warp">
                        <div class="simple-form-field">
                            <div class="form-group">
                                <div class="control-container">
                                    <el-select :empty-values="[null, undefined]" v-model="filterParams.status" clearable @clear="onSearchSubmit" @change="onSearchSubmit">
                                        <el-option v-for="(item, key) in buyShowStatus" :key="key" :value="item.value" :label="item.label" />
                                    </el-select>
                                </div>
                            </div>
                        </div>
                        <div class="simple-form-field">
                            <div class="form-group">
                                <SelectTimeInterval
                                    v-model:end-date="filterParams.endTime"
                                    v-model:start-date="filterParams.startTime"
                                    :clearable="false"
                                    type="date"
                                    value-format="YYYY-MM-DD"
                                    @callback="loadFilter"
                                ></SelectTimeInterval>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="list-table-tool-row">
                    <div class="list-table-tool-col">
                        <el-space>
                            <slot></slot>
                            <el-popconfirm :title="$t('您确认要批量通过所选数据吗？')" @confirm="onBatchSubmit('audit', {value: 1})">
                                <template #reference>
                                    <div>
                                        <el-button :disabled="selectedIds.length === 0">{{ $t('批量通过') }}</el-button>
                                    </div>
                                </template>
                            </el-popconfirm>
                            <el-popconfirm :title="$t('您确认要批量拒绝所选数据吗？')" @confirm="onBatchSubmit('audit',  {value: 2})">
                                <template #reference>
                                    <div>
                                        <el-button :disabled="selectedIds.length === 0">{{ $t('批量拒绝') }}</el-button>
                                    </div>
                                </template>
                            </el-popconfirm>
                            <span v-if="selectedIds.length > 0"> {{ $t('已选择：') }}<b>{{ selectedIds.length }}</b> {{ $t('项') }} </span>
                        </el-space>
                    </div>
                </div>
            </el-form>
        </div>
        <CommentTable v-model:filterState="filterState" v-model:filterParams="filterParams" v-model:total="total" v-model:loading="loading" @okCallback="loadFilter" @onSelectChange="onSelectChange"></CommentTable>
    </div>
</template>

<script setup lang="ts">
import "@/style/css/list.less";
import { ref } from "vue";
import { useListRequest } from "@/hooks/useListRequest";
import {  buyerShowCommentFilterState, buyerShowCommentFilterParams } from "@/types/promotion/buyerShowComment";
import { getBuyerShowEvaluationList, commentInfoBatch } from "@/api/promotion/buyShow";
import { useConfigStore } from "@/store/config";
import { SelectTimeInterval } from "@/components/select";
import CommentTable from './src/CommentTable.vue'

import { useI18n } from "vue-i18n"
const { t } = useI18n()
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
} = useListRequest<buyerShowCommentFilterState, buyerShowCommentFilterParams>({
    apiFunction: getBuyerShowEvaluationList,
    idKey: "buyerShowCommentId",
    defaultParams: {
        page: 1,
        size: config.get("pageSize"),
        sortField: "",
        sortOrder: "",
        keyword: "",
        status: '',
        startTime: '',
        endTime: '',
    }
});

// 批量操作
const onBatchSubmit = async (action: string, data: object) => {
    await onBatchAction(action, commentInfoBatch, data);
};

const buyShowStatus = ref([
    {
        value: '',
        label: t('全部状态')
    },
    {
        value: -1,
        label: t('待审核')
    },
    {
        value: 0,
        label: t('隐藏')
    },
    {
        value: 1,
        label: t('公开')
    }
]);

// 初始化加载
loadFilter();
</script>

<style scoped lang="less">

</style>
