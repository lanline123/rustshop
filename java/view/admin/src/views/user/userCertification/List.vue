/** 客户认证申请列表 - 审核客户的个人/企业认证申请（B2B模式） */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <el-form :model="filterParams" @submit.native.prevent="onSearchSubmit">
                        <div class="list-table-tool-row">
                            <div class="simple-form-warp">
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <div class="control-container">
                                            <TigInput
                                                v-model="filterParams.username"
                                                name="username"
                                                :placeholder="$t('输入客户名称')"
                                                clearable
                                                @clear="onSearchSubmit"
                                            >
                                                <template #append>
                                                    <el-button @click="onSearchSubmit"><span class="iconfont icon-chakan1"></span> </el-button>
                                                </template>
                                            </TigInput>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <SelectConfig @callback="onChangeFilter"></SelectConfig>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </el-form>
                </div>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table :data="filterState" :loading="loading" :total="total" row-key="id" @sort-change="onSortChange">
                            <el-table-column :width="250" :label="$t('申请信息')" prop="companyName">
                                <template #default="{ row }">
                                    <template v-if="row.type == 1">
                                        <Tag :transparent="true" :text="$t('个人')"></Tag>
                                    </template>
                                    <template v-else>
                                        <Tag color="#409EFF" :transparent="false" :text="$t('企业')"></Tag>
                                    </template>
                                    {{ row.type == 1 ? "" + row.companyData.corporateName : "" + row.companyData.companyName }}
                                </template>
                            </el-table-column>
                            <el-table-column :width="200" :label="$t('申请客户名称')" prop="username">
                                <template #default="{ row }">
                                    <UserCard v-if="row.username" :user="row.username"></UserCard>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('联系人姓名')" prop="companyData.corporateName">
                                <template #default="{ row }">
                                    {{ row.companyData.corporateName }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('联系人电话')" prop="companyData.contactPhone">
                                <template #default="{ row }">
                                    <MobileCard :mobile="row.companyData.contactPhone"></MobileCard>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('申请状态')" prop="statusText">
                                <template #default="{ row }">
                                    <template v-if="row.status == 1">
                                        <StatusDot :flicker="true"></StatusDot>
                                    </template>
                                    <template v-if="row.status == 2">
                                        <StatusDot color="#52c41a" :flicker="true"></StatusDot>
                                    </template>
                                    <template v-if="row.status == 3">
                                        <StatusDot color="#f5222d" :flicker="false"></StatusDot>
                                    </template>
                                    {{ row.statusText }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('申请日期')" prop="addTime" sortable="custom"></el-table-column>
                            <el-table-column :width="110" fixed="right" :label="$t('操作')">
                                <template #default="{ row }">
                                    <DialogForm
                                        :showClose="false"
                                        :showOnOk="false"
                                        :params="{ act: 'detail', id: row.id }"
                                        isDrawer
                                        path="user/userCertification/Info"
                                        :title="$t('申请详情')"
                                        width="600px"
                                        @okCallback="loadFilter"
                                    >
                                        <a class="btn-link">{{ $t("详情") }}</a>
                                    </DialogForm>
                                    <el-divider v-if="row.status == 3" direction="vertical" />
                                    <DeleteRecord v-if="row.status == 3" :params="{ id: row.id }" :requestApi="delUserCompany" @afterDelete="loadFilter">{{
                                        $t("删除")
                                    }}</DeleteRecord>
                                </template>
                            </el-table-column>
                            <template #empty>
                                <div class="empty-warp">
                                    <div v-if="!loading" class="empty-bg">{{ $t("暂无数据") }}</div>
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
                                >{{ $t("已选择：") }}<b>{{ selectedIds.length }}</b> {{ $t("项") }}</span
                            >
                            <el-popconfirm :title="$t('您确认要批量删除所选数据吗？')" @confirm="onBatchSubmit('del')">
                                <template #reference>
                                    <el-button>{{ $t("批量删除") }}</el-button>
                                </template>
                            </el-popconfirm>
                        </el-space>
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
import { DeleteRecord, Pagination } from "@/components/list";
import { message } from "ant-design-vue";
import StatusDot from "@/components/form/src/StatusDot.vue";
import { useConfigStore } from "@/store/config";
import { UserCompanyFilterParams, UserCompanyFilterState } from "@/types/user/userCertification.d";
import { batchSubmit, delUserCompany, getUserCompanyList } from "@/api/user/userCertification";
import { Tag } from "@/components/form";
import MobileCard from "@/components/list/src/MobileCard.vue";
import UserCard from "@/components/list/src/UserCard.vue";
import SelectConfig from "./src/SelectConfig.vue";
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
} = useListRequest<UserCompanyFilterState, UserCompanyFilterParams>({
    apiFunction: getUserCompanyList,
    idKey: "id",
    defaultParams: {
        sortField: "",
        sortOrder: "",
        username: "",
        status: 0,
        page: 1,
        size: config.get("pageSize")
    }
});
/**
 * @description 批量操作处理，支持批量删除等操作
 * @param action 操作类型：'del'-批量删除
 */
const onBatchSubmit = async (action: string) => {
    await onBatchAction(action, batchSubmit);
};

// 初始化加载认证申请列表
loadFilter();

/**
 * @description 筛选条件变更处理，重置分页并重新加载列表
 * @param value 认证状态：1-待审核 2-已通过 3-已拒绝
 */
const onChangeFilter = (value: any) => {
    filterParams.status = value;
    filterParams.page = 1;
    loadFilter();
};
</script>
