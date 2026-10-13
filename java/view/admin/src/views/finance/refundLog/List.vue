/**
 * @file 退款日志列表页
 * @description 展示已完成的退款记录
 * @features
 * - 按退款方式和售后编号筛选
 * - 显示退款金额/方式/时间
 * - 区分原路返回/余额退回/线下退回
 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <div class="list-table-tool-row">
                        <div class="list-table-tool-col">
                            <el-space>
                                <el-select v-model="filterParams.type" clearable :placeholder="$t('请选择状态')" @change="onSearchSubmit">
                                    <el-option :value="1" :label="$t('原路返回')" />
                                    <el-option :value="2" :label="$t('余额退回')" />
                                    <el-option :value="3" :label="$t('线下退回')" />
                                </el-select>
                                <TigInput
                                    v-model="filterParams.keyword"
                                    name="keyword"
                                    :placeholder="$t('输入售后编号')"
                                    @keyup.enter="onSearchSubmit"
                                    clearable
                                    @clear="onSearchSubmit"
                                >
                                    <template #append>
                                        <el-button @click="onSearchSubmit"><span class="iconfont icon-chakan1"></span> </el-button>
                                    </template>
                                </TigInput>
                            </el-space>
                        </div>
                    </div>
                </div>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table :data="filterState" :loading="loading" :total="total" row-key="paylogId" @sort-change="onSortChange">
                            <el-table-column :label="$t('退款信息')" prop="aftersalesSn">
                                <template #default="{ row }">
                                    <div style="display: flex; justify-content: space-between; align-items: center">
                                        <DialogForm
                                            :params="{ act: 'detail', id: row.refund?.aftersaleId, type: 2 }"
                                            isDrawer
                                            path="order/aftersales/Info"
                                            :title="$t('售后详情 ') + row.refund?.aftersales.aftersalesSn"
                                            width="800px"
                                            @okCallback="loadFilter"
                                            :showClose="false"
                                            :showOnOk="false"
                                        > {{ $t('售后编号：') }}<a>{{ row.refund?.aftersales.aftersalesSn }}</a>
                                        </DialogForm>
                                    </div>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('退款方式')" prop="refundTypeText"></el-table-column>
                            <el-table-column :label="$t('退款金额')" prop="refundAmount"></el-table-column>
                            <el-table-column :width="160" :label="$t('退款时间')" prop="addTime" sortable="custom"></el-table-column>
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
import { onMounted, reactive, ref } from "vue";
import { Pagination } from "@/components/list";
import { message } from "ant-design-vue";
import { useConfigStore } from "@/store/config";
import { RedundLogFilterParams, RedundLogFilterState } from "@/types/finance/refundLog";
import { getRedundLogList } from "@/api/finance/refundLog";
import { useListRequest } from "@/hooks/useListRequest";
const config: any = useConfigStore();
const {
    listData: filterState,
    loading,
    total,
    filterParams,
    loadData: loadFilter,
    onSearchSubmit,
    onSortChange,
} = useListRequest<RedundLogFilterState, RedundLogFilterParams>({
    apiFunction: getRedundLogList,
    idKey: "paylogId",
    defaultParams: {
        page: 1,
        size: config.get("pageSize"),
        sortField: "",
        sortOrder: "",
        keyword: "",
        type: 1
    }
});
// 初始化加载
loadFilter();
</script>
