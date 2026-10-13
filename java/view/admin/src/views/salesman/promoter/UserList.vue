<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table :data="filterState" :total="total" row-key="salesmanId" @selection-change="onSelectChange" @sort-change="onSortChange">
                            <el-table-column :label="$t('会员名称')" prop="user" :width="160" fixed="left">
                                <template #default="{ row }">
                                    <div class="flex flex-align-center" style="gap: 10px;">
                                        <el-avatar :size="30" :src="imageFormat(row.avatar)" />
                                        <div style="width: 100px;" class="line1">{{ row.nickname || row.username || "--" }}</div>
                                    </div>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('会员手机')" :width="140" fixed="left">
                                <template #default="{ row }">
                                    {{ row.mobile || "--" }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('会员等级')">
                                <template #default="{ row }">
                                    <div class="rank-container" v-if="row.rankName">
                                        <el-image v-if="row.rankLogo" :src="imageFormat(row.rankLogo)" class="image" fit="contain" />
                                        <div class="text">{{ row.rankName }}</div>
                                    </div>
                                    <span v-else>--</span>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('注册日期')">
                                <template #default="{ row }">
                                    {{ row.regTime || "--" }}
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
                <div v-if="selectedIds.length > 0" class="selected-action-warp selected-warp-left">
                    <div class="selected-action">
                        <el-space>
                            <span
                                >{{ $t('已选择：') }}<b>{{ selectedIds.length }}</b> {{ $t('项') }}</span
                            >
                            <el-button>{{ $t('设置等级') }}</el-button>
                            <el-button>{{ $t('分组') }}</el-button>
                            <el-button>{{ $t('清退') }}</el-button>
                        </el-space>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { imageFormat } from "@/utils/format";
import { Pagination } from "@/components/list";
import { useConfigStore } from "@/store/config";
import { salesmanCustomerFilterParams, salesmanCustomerFilterState } from "@/types/salesman/promoter.d";
import { getsalesmanCustomerList } from "@/api/salesman/promoter";
const config: any = useConfigStore();
import { useListRequest } from "@/hooks/useListRequest";
const props = defineProps({
    id: {
        type: Number,
        default: 0
    }
});
const {
    listData: filterState,
    loading,
    total,
    selectedIds,
    filterParams,
    loadData: loadFilter,
    onSortChange,
    onSelectChange
} = useListRequest<salesmanCustomerFilterState, salesmanCustomerFilterParams>({
    apiFunction: getsalesmanCustomerList,
    idKey: "salesmanId",
    defaultParams: {
        page: 1,
        size: config.get("pageSize"),
        salesmanId: props.id
    }
});
// 初始化加载
loadFilter();
</script>
<style lang="less" scoped>
.list-table-tool {
    margin-bottom: 0;
}
.rank-container {
    display: flex;
    align-items: center; /* 垂直居中 */

    .image {
        width: 18px;
        height: 18px;
    }

    .text {
        margin-left: 3px;
    }
}
</style>
