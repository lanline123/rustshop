/**
 * @file 管理员操作日志列表
 * @description 后台管理员操作日志记录
 * @features
 * - 日志列表展示（操作者、IP地址、操作时间、操作记录）
 * - 关键词和IP地址搜索
 * - 按操作日期排序
 * - 查看操作详情
 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <div class="list-table-tool-row">
                        <div class="list-table-tool-col">
                            <el-space>
                                <TigInput
                                    v-model="filterParams.keyword"
                                    class="width240"
                                    name="keyword"
                                    :placeholder="$t('输入关键词或IP地址')"
                                    @keyup.enter="onSearchSubmit"
                                    clearable
                                    @clear="onSearchSubmit"
                                >
                                    <template #append>
                                        <el-button @click="onSearchSubmit"><span class="iconfont icon-chakan1"></span></el-button>
                                    </template>
                                </TigInput>
                            </el-space>
                        </div>
                    </div>
                </div>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table :data="filterState" :loading="loading" :total="total" row-key="logId" @sort-change="onSortChange">
                            <el-table-column :label="$t('编号')" prop="logId"></el-table-column>
                            <el-table-column :label="$t('操作者')" prop="username"></el-table-column>
                            <el-table-column :label="$t('IP地址')" prop="ipAddress"></el-table-column>
                            <el-table-column :label="$t('操作日期')" prop="logTime" sortable="custom"></el-table-column>
                            <el-table-column :label="$t('操作记录')" prop="logInfo">
                                <template #default="{ row }">
                                    <DialogForm
                                        v-if="row.requestLog && row.requestLog != null"
                                        :params="{ data: row.requestLog }"
                                        isDrawer
                                        path="authority/adminLog/Info"
                                        :title="$t('操作详情')"
                                        width="500px"
                                        :showClose="false"
                                        :showOnOk="false"
                                        @okCallback="loadFilter"
                                    >
                                        <div class="flex flex-align-center">
                                            <el-icon><Document /></el-icon>
                                            <p>&nbsp;&nbsp;{{ row.logInfo }}</p>
                                        </div>
                                    </DialogForm>
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
import { Document } from "@element-plus/icons-vue";
import { DialogForm } from "@/components/dialog";
import { Pagination } from "@/components/list";
import { useConfigStore } from "@/store/config";
import type { AdminLogFilterParams, AdminLogFilterState } from "@/types/authority/adminLog";
import { getAdminLogList } from "@/api/authority/adminLog";
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
} = useListRequest<AdminLogFilterState, AdminLogFilterParams>({
    apiFunction: getAdminLogList,
    idKey: "articleId",
    defaultParams: {
        page: 1,
        size: config.get("pageSize"),
        sortField: "logTime",
        sortOrder: "desc",
        keyword: ""
    }
});
// 初始化加载
loadFilter();
</script>
