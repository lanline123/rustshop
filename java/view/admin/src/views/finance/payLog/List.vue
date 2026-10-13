/** * @file 支付日志列表页 * @description 展示订单支付流水记录 * @features * - 按支付状态和订单号筛选 * - 显示支付方式/金额/交易号/时间 * -
区分已支付和未支付状态 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <el-form :model="filterParams">
                        <div class="list-table-tool-row">
                            <div class="list-table-tool-col">
                                <el-space>
                                    <el-select v-model="filterParams.payStatus" clearable :placeholder="$t('请选择状态')" @change="onSearchSubmit">
                                        <el-option :value="0" :label="$t('未支付')" />
                                        <el-option :value="2" :label="$t('已支付')" />
                                    </el-select>
                                    <TigInput
                                        v-model="filterParams.keyword"
                                        name="keyword"
                                        :placeholder="$t('输入订单号')"
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
                    </el-form>
                </div>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table :data="filterState" :loading="loading" :total="total" row-key="paylogId" @sort-change="onSortChange">
                            <el-table-column :label="$t('订单编号')" prop="orderSn"></el-table-column>
                            <el-table-column :label="$t('订单收件人')" prop="consignee">
                                <template #default="{ row }">
                                    {{ row.consignee || "--" }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('支付方式')" prop="payName"></el-table-column>
                            <el-table-column :label="$t('支付金额')" prop="payAmount"></el-table-column>
                            <!-- <el-table-column label="已退款金额" prop="refundAmount"></el-table-column> -->
                            <el-table-column :width="160" :label="$t('支付日期')" prop="addTime" sortable="custom"></el-table-column>
                            <el-table-column :label="$t('支付状态')" prop="payStatusName">
                                <template #default="{ row }">
                                    <span :style="{ color: row.payStatus === 2 ? 'green' : 'red' }">{{ row["payStatusName"] }}</span>
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
import { Pagination } from "@/components/list";
import { useConfigStore } from "@/store/config";
import { PayLogFilterParams, PayLogFilterState } from "@/types/finance/payLog";
import { getPayLogList } from "@/api/finance/payLog";
import { useListRequest } from "@/hooks/useListRequest";
const config: any = useConfigStore();
const {
    listData: filterState,
    loading,
    total,
    filterParams,
    loadData: loadFilter,
    onSearchSubmit,
    onSortChange
} = useListRequest<PayLogFilterState, PayLogFilterParams>({
    apiFunction: getPayLogList,
    idKey: "paylogId",
    defaultParams: {
        page: 1,
        size: config.get("pageSize"),
        sortField: "",
        sortOrder: "",
        keyword: "",
        payStatus: 2
    }
});
// 初始化加载
loadFilter();
</script>
