/** 积分变动日志列表 - 查看会员积分变动记录（增加/减少） */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <el-form :model="filterParams" @submit.native.prevent="onSearchSubmit">
                        <div class="list-table-tool-row">
                            <div class="list-table-tool-col">
                                <el-space>
                                    <TigInput
                                        v-model="filterParams.keyword"
                                        name="keyword"
                                        :placeholder="$t('输入会员名称/手机号/邮箱')"
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
                        <el-table :data="filterState" :loading="loading" :total="total" row-key="id">
                            <el-table-column :label="$t('操作时间')" prop="changeTime"></el-table-column>
                            <el-table-column :label="$t('会员名称')" prop="id">
                                <template #default="{ row }">
                                    <div style="position: relative">
                                        <div>{{ row.username }}</div>
                                    </div>
                                </template>
                            </el-table-column>
                            <el-table-column :width="200" :label="$t('操作备注')" prop="changeDesc"></el-table-column>
                            <el-table-column :label="$t('积分')">
                                <template #default="{ row }">
                                    <p v-if="row.changeType == 1" style="color: rgb(0, 0, 255)">＋{{ row.points }}</p>
                                    <p v-else style="color: rgb(255, 0, 0)">－{{ row.points }}</p>
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
import { IntegralLogFilterParams, IntegralLogFilterState } from "@/types/user/integralLog";
import { getIntegralLogList } from "@/api/user/integralLog";
import { useListRequest } from "@/hooks/useListRequest";
const config: any = useConfigStore();
const {
    listData: filterState,
    loading,
    total,
    filterParams,
    loadData: loadFilter,
    onSearchSubmit,
} = useListRequest<IntegralLogFilterState, IntegralLogFilterParams>({
    apiFunction: getIntegralLogList,
    idKey: "id",
    defaultParams: {
        page: 1,
        size: config.get("pageSize"),
        sortField: "",
        sortOrder: "",
        keyword: ""
    }
});
// 初始化加载
loadFilter();
</script>
