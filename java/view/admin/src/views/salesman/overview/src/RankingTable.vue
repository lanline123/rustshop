<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="table-container">
                <SelectTime v-model:dateType="filterParams.dateType" v-model:dateValue="filterParams.startEndTime" @onOK="getRankingData"></SelectTime>
                <a-spin :spinning="loading">
                    <el-table :data="filterState" :total="total" @sort-change="onSortChange">
                        <el-table-column :label="$t('排名')" :fixed="true" width="68">
                            <template #default="{ row, $index }">
                                <div style="position: relative">
                                    <div>{{ $index + 1 }}</div>
                                </div>
                            </template>
                        </el-table-column>
                        <el-table-column :label="$t('分销员昵称')" prop="nickname" :fixed="true">
                            <template #default="{ row, $index }">
                                <div>
                                    <div>{{ row.nickname || row.username }}</div>
                                </div>
                            </template>
                        </el-table-column>
                        <el-table-column :label="`${$t('新增销售额')}(${$t(config.config.dollarSignCn)})`" prop="totalSaleAmount" sortable="custom">
                            <template #default="{ row, $index }">
                                <div>
                                    <div>{{ priceFormat(row.totalSaleAmount) }}</div>
                                </div>
                            </template>
                        </el-table-column>
                        <el-table-column :label="$t('新增客户数')" prop="totalCustomers" sortable="custom">
                            <template #default="{ row, $index }">
                                <div>
                                    <div>{{ row.totalCustomers }}</div>
                                </div>
                            </template>
                        </el-table-column>
                        <el-table-column :label="$t('支付客户数')" prop="totalPayCustomers" sortable="custom">
                            <template #default="{ row, $index }">
                                <div>
                                    <div>{{ row.totalPayCustomers }}</div>
                                </div>
                            </template>
                        </el-table-column>
                        <el-table-column :label="$t('开单邀请数')" prop="orderNum" sortable="custom">
                            <template #default="{ row, $index }">
                                <div>
                                    <div>{{ row.orderNum || "--" }}</div>
                                </div>
                            </template>
                        </el-table-column>
                        <!-- <el-table-column label="操作" fixed="right">
                    <template #default="{ row }">
                        <div style="position: relative">
                            <div>{{ row.promotionName || "--" }}</div>
                        </div>
                    </template>
                </el-table-column> -->
                        <template #empty>
                            <div class="empty-warp">
                                <div v-if="!loading" class="empty-bg">{{ $t('暂无数据') }}</div>
                            </div>
                        </template>
                    </el-table>
                </a-spin>
                <div class="pagination-con" v-if="total > 0">
                    <Pagination v-model:page="filterParams.page" v-model:size="filterParams.size" :total="total" @callback="getRankingData" />
                </div>
            </div>
        </div>
    </div>
</template>
<script setup lang="ts">
import "@/style/css/list.less";
import { Pagination } from "@/components/list";
import { useConfigStore } from "@/store/config";
import SelectTime from "./SelectTime.vue";
import { getDays } from "@/utils/util";
import { formattedDate } from "@/utils/time";
import { RankingFormState, RankingFilterParams } from "@/types/salesman/overview.d";
import { getSalesmanRanking } from "@/api/salesman/overview";
import { priceFormat } from "@/utils/format";
const config: any = useConfigStore();
import { useListRequest } from "@/hooks/useListRequest";
const {
    listData: filterState,
    loading,
    total,
    filterParams,
    loadData: getRankingData,
    onSortChange
} = useListRequest<RankingFormState, RankingFilterParams>({
    apiFunction: getSalesmanRanking,
    idKey: "id",
    defaultParams: {
        page: 1,
        size: config.get("pageSize"),
        sortField: "",
        sortOrder: "",
        dateType: 3,
        startEndTime: formattedDate(getDays(0, "sub"), "YYYY-MM-DD")
    }
});

// 初始化加载
getRankingData();
</script>
<style lang="less" scoped>
.table-container {
    margin-bottom: 20px;
}
.filtrate-menu {
    display: flex;

    .mr-10 {
        margin-right: 5px;
    }
}
</style>
