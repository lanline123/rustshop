/**
 * @file 增票资质申请列表页
 * @description 管理会员增值税专用发票资质申请
 * @features
 * - 按会员/公司名称/状态筛选
 * - 显示公司信息/税号/审核状态
 * - 支持审核通过/拒绝操作
 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <el-form :model="filterParams">
                        <div class="advanced-search-warp list-table-tool-row">
                            <div class="simple-form-warp">
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('申请信息：') }}</span></label>
                                        <div class="control-container">
                                            <TigInput
                                                v-model="filterParams.keyword"
                                                name="keyword"
                                                :placeholder="$t('输入申请会员/公司名称')"
                                                @keyup.enter="onSearchSubmit"
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
                                        <label class="control-label"><span>{{ $t('申请状态：') }}</span></label>
                                        <div class="control-container">
                                            <el-select clearable :placeholder="$t('请选择状态')" v-model="filterParams.status" @change="onSearchSubmit">
                                                <el-option v-for="item in statusList" :label="item.label" :value="item.value"></el-option>
                                            </el-select>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <el-button type="primary" plain @click="onSearchSubmit">{{ $t('搜索') }}</el-button>
                                </div>
                                <div class="simple-form-field">
                                    <el-button plain @click="resetParams">{{ $t('重置') }}</el-button>
                                </div>
                            </div>
                        </div>
                        <div class="list-table-tool-row">
                            <div class="list-table-tool-col">
                                <el-space>
                                    <el-popconfirm :title="$t('您确认要批量删除所选数据吗？')" @confirm="onBatchSubmit('del')">
                                        <template #reference>
                                            <el-button :disabled="selectedIds.length === 0">{{ $t('批量删除') }}</el-button>
                                        </template>
                                    </el-popconfirm>
                                    <span v-if="selectedIds.length > 0"> {{ $t('已选择：') }}<b>{{ selectedIds.length }}</b> {{ $t('项') }} </span>
                                </el-space>
                            </div>
                        </div>
                    </el-form>
                </div>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table
                            :data="filterState"
                            :loading="loading"
                            :total="total"
                            row-key="invoiceId"
                            @selection-change="onSelectChange"
                            @sort-change="onSortChange"
                        >
                            <el-table-column type="selection" width="32" />
                            <el-table-column :label="$t('申请会员')" prop="username">
                                <template #default="{ row }">
                                    {{ row.username || "--" }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('公司名称')" prop="companyName"></el-table-column>
                            <el-table-column :label="$t('申请日期')" prop="addTime"></el-table-column>
                            <el-table-column :label="$t('申请状态')" prop="statusName">
                                <template #default="{ row }">
                                    <span :style="{ color: row.status === 1 ? 'green' : row.status === 2 ? 'black' : 'red' }">{{ row.statusName }}</span>
                                </template>
                            </el-table-column>
                            <el-table-column :width="100" fixed="right" :label="$t('操作')">
                                <template #default="{ row }">
                                    <DialogForm
                                        :params="{ act: 'detail', id: row.invoiceId }"
                                        isDrawer
                                        path="finance/userInvoice/Info"
                                        :title="$t('编辑增票资质申请')"
                                        width="600px"
                                        @okCallback="loadFilter"
                                    >
                                        <a class="btn-link">{{ $t('编辑') }}</a>
                                    </DialogForm>
                                    <el-divider direction="vertical" />
                                    <DeleteRecord :params="{ id: row.invoiceId }" :requestApi="delUserInvoice" @afterDelete="loadFilter">{{ $t('删除') }} </DeleteRecord>
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
import { DialogForm } from "@/components/dialog";
import { ref } from "vue";
import { DeleteRecord, Pagination } from "@/components/list";
import { useConfigStore } from "@/store/config";
import { UserInvoiceFilterParams, UserInvoiceFilterState } from "@/types/finance/userInvoice";
import { batchSubmit, delUserInvoice, getUserInvoiceList } from "@/api/finance/userInvoice";
import { useListRequest } from "@/hooks/useListRequest";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const config: any = useConfigStore();
const statusList = ref([
    { value: 1, label: t('审核通过') },
    { value: 2, label: t('待审核') },
    { value: 3, label: t('审核未通过') }
]);
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
} = useListRequest<UserInvoiceFilterState, UserInvoiceFilterParams>({
    apiFunction: getUserInvoiceList,
    idKey: "invoiceId",
    defaultParams: {
        page: 1,
        size: config.get("pageSize"),
        sortField: "",
        sortOrder: "",
        keyword: "",
        status: "",
    }
});
// 批量操作
const onBatchSubmit = async (action: string) => {
    await onBatchAction(action, batchSubmit);
};

// 初始化加载
loadFilter();

</script>
