<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="table-container">
                <SelectTime v-model:dateType="filterParams.dateType" v-model:dateValue="filterParams.startEndTime" @onOK="getRankingData"></SelectTime>
                <a-spin :spinning="loading">
                    <el-table :data="filterState" row-key="productId" :total="total" @sort-change="onSortChange">
                        <el-table-column :label="$t('排名')" :fixed="true" width="68">
                            <template #default="{ row, $index }">
                                <div>
                                    <div>{{ $index + 1 }}</div>
                                </div>
                            </template>
                        </el-table-column>
                        <el-table-column :label="$t('商品')" prop="productName" :fixed="true" min-width="200">
                            <template #default="{ row }">
                                <div class="flex flex-align-center">
                                    <div>
                                        <Image :src="row.picThumb" fit="contain" style="height: 50px; width: 50px; margin-right: 10px" />
                                    </div>
                                    <div>
                                        <div>{{ row.productName || "--" }}</div>
                                        <div class="sn">{{ $t('货号：{productSn}', { productSn: row.productSn || "--" }) }}</div>
                                    </div>
                                </div>
                            </template>
                        </el-table-column>
                        <el-table-column :label="$t('成交金额')" prop="totalProductMoney" sortable="custom" width="200">
                            <template #default="{ row }">
                                <div>
                                    <div>{{ priceFormat(row.totalProductMoney) || "--" }}</div>
                                </div>
                            </template>
                        </el-table-column>
                        <el-table-column :label="$t('支出佣金')" prop="commissionExpenses" width="200">
                            <template #default="{ row }">
                                <div>
                                    <div>{{ priceFormat(row.commissionExpenses) || "--" }}</div>
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
import { AnalysisFilterParams, AnalysisFormState } from "@/types/salesman/overview.d";
import { getProductAnalysis } from "@/api/salesman/overview";
import { priceFormat, imageFormat } from "@/utils/format";
import { Image } from "@/components/image";
const config: any = useConfigStore();
import { useListRequest } from "@/hooks/useListRequest";
const {
    listData: filterState,
    loading,
    total,
    filterParams,
    loadData: getRankingData,
    onSortChange
} = useListRequest<AnalysisFormState, AnalysisFilterParams>({
    apiFunction: getProductAnalysis,
    idKey: "articleId",
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
.sn {
    font-size: 12px;
    color: #999;
}
</style>
