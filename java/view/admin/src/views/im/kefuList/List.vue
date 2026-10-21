/**
 * @file 客服列表页面
 * @description 客服人员查询与账号状态维护
 * @features
 * - 客服人员列表展示（头像/姓名、员工身份、手机号、账号状态）
 * - 搜索、筛选、分页和空数据展示
 * - 启用/停用客服
 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <el-form :model="filterParams" @keyup.enter="onSearchSubmit">
                        <div class="advanced-search-warp list-table-tool-row">
                            <div class="simple-form-warp">
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>姓名/手机号：</span></label>
                                        <div class="control-container">
                                            <TigInput
                                                v-model="filterParams.keyword"
                                                name="keyword"
                                                placeholder="输入姓名/手机号"
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
                                        <label class="control-label"><span>账号状态：</span></label>
                                        <div class="control-container">
                                            <el-select v-model="filterParams.isUsing" clearable placeholder="全部" @change="onSearchSubmit">
                                                <el-option :value="0" label="启用" />
                                                <el-option :value="1" label="停用" />
                                            </el-select>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>添加时间：</span></label>
                                        <div class="control-container">
                                            <el-date-picker
                                                v-model="dateRange"
                                                type="daterange"
                                                range-separator="至"
                                                start-placeholder="开始日期"
                                                end-placeholder="结束日期"
                                                value-format="YYYY-MM-DD"
                                                @change="onDateChange"
                                            />
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-warp">
                                    <div class="simple-form-field">
                                        <label class="control-label"></label>
                                        <div class="control-container">
                                            <el-button type="primary" plain @click="onSearchSubmit">搜索</el-button>
                                            <el-button plain @click="onReset">重置</el-button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="list-table-tool-row customer-staff-maintenance-tip">新增、编辑或删除人员，请前往员工管理。</div>
                    </el-form>
                </div>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table
                            :data="filterState"
                            :loading="loading"
                            :total="total"
                            row-key="id"
                            @sort-change="onSortChange"
                        >
                            <el-table-column label="ID" prop="adminId" width="120">
                                <template #default="{ row }">
                                    {{ row.adminId || "--" }}
                                </template>
                            </el-table-column>
                            <el-table-column label="头像/姓名" prop="username" min-width="180">
                                <template #default="{ row }">
                                    <div class="avatar-username">
                                        <el-avatar :size="30" :src="avatarFormat(row.avatar || row.adminUser?.avatar || '')" />
                                        <span>{{ row.username || row.adminUser?.username || "--" }}</span>
                                    </div>
                                </template>
                            </el-table-column>
                            <el-table-column label="员工身份" prop="roleName">
                                <template #default="{ row }">
                                    <span>{{ row.role?.roleName || "客服人员" }}</span>
                                </template>
                            </el-table-column>
                            <el-table-column label="手机号" prop="mobile">
                                <template #default="{ row }">
                                    <span v-if="row.adminUser?.mobile || row.mobile">
                                        <MobileCard :mobile="row.adminUser?.mobile || row.mobile" />
                                    </span>
                                    <span v-else>--</span>
                                </template>
                            </el-table-column>
                            <el-table-column label="添加时间" prop="addTime">
                                <template #default="{ row }">
                                    {{ row.addTime || "--" }}
                                </template>
                            </el-table-column>
                            <el-table-column label="账号状态" prop="isUsing" width="100">
                                <template #default="{ row }">
                                    <Switch
                                        v-model:checked="row.isUsing"
                                        :switchingCode="[1, 0]"
                                        :params="{ id: row.id, field: 'isUsing' }"
                                        :requestApi="updateAdminUserKefuField"
                                    />
                                </template>
                            </el-table-column>
                            <template #empty>
                                <div class="empty-warp">
                                    <div v-if="!loading" class="empty-bg">暂无数据</div>
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
import { Pagination, Switch } from "@/components/list";
import MobileCard from "@/components/list/src/MobileCard.vue";
import { onMounted, ref } from "vue";
import { useConfigStore } from "@/store/config";
import { avatarFormat } from "@/utils/format";
import { useListRequest } from "@/hooks/useListRequest";
import type { AdminUserKefuFilterParams, AdminUserKefuFilterState } from "@/types/im/kefu";
import { getAdminUserKefuList, updateAdminUserKefuField } from "@/api/im/adminUserKefu";

const config: any = useConfigStore();
const {
    listData: filterState,
    loading,
    total,
    filterParams,
    loadData: loadFilter,
    onSearchSubmit,
    onSortChange,
    resetParams
} = useListRequest<AdminUserKefuFilterState, AdminUserKefuFilterParams>({
    apiFunction: getAdminUserKefuList,
    idKey: "id",
    defaultParams: {
        page: 1,
        size: config.get("pageSize"),
        sortField: "",
        sortOrder: "",
        keyword: "",
        isUsing: "",
        searchStartTime: "",
        searchEndTime: ""
    }
});

const dateRange = ref<[string, string] | null>(null);
const onDateChange = (val: [string, string] | null) => {
    filterParams.searchStartTime = val?.[0] || "";
    filterParams.searchEndTime = val?.[1] || "";
    onSearchSubmit();
};

const onReset = () => {
    dateRange.value = null;
    resetParams();
};

onMounted(() => {
    loadFilter();
});
</script>
<style lang="less" scoped>
.avatar-username {
    display: flex;
    align-items: center;
    gap: 8px;
}

.customer-staff-maintenance-tip {
    color: #999;
}
</style>
