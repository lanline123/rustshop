<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <div class="list-table-tool-row">
                        <div class="simple-form-warp">
                            <div class="simple-form-field">
                                <div class="form-group">
                                    <div class="control-container">
                                        <TigInput
                                            v-model="filterParams.username"
                                            name="username"
                                            :placeholder="$t('输入会员名称')"
                                            @keyup.enter="onSearchSubmit"
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
                    <div v-if="filterParams.status == 20" class="list-table-tool-row mt20">
                        <div class="list-table-tool-col">
                            <el-space>
                                <el-popconfirm :title="$t('您确认要批量删除所选数据吗？')" @confirm="onBatchSubmit('del')">
                                    <template #reference>
                                        <el-button :disabled="selectedIds.length === 0 || filterParams.status != 20">{{ $t('批量删除') }}</el-button>
                                    </template>
                                </el-popconfirm>
                                <span v-if="selectedIds.length > 0"> {{ $t('已选择：') }}<b>{{ selectedIds.length }}</b> {{ $t('项') }} </span>
                            </el-space>
                        </div>
                    </div>
                </div>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table
                            :data="filterState"
                            :loading="loading"
                            :total="total"
                            row-key="merchantApplyId"
                            @selection-change="onSelectChange"
                            @sort-change="onSortChange"
                        >
                            <el-table-column v-if="filterParams.status == 20" type="selection" width="32" />
                            <el-table-column :label="$t('申请信息')">
                                <template #default="{ row }">
                                    <div class="ssdw">
                                        <template v-if="row.type == 1">
                                            <Tag :transparent="true" :text="$t('个人')"></Tag>
                                        </template>
                                        <template v-else>
                                            <Tag color="#409EFF" :transparent="false" :text="$t('企业')"></Tag>
                                        </template>
                                        {{ row.type == 1 ? "" + row.corporateName : "" + row.companyName }}
                                    </div>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('申请会员名称(ID)')">
                                <template #default="{ row }">
                                    <div v-if="row.user">{{ row.user.username || "" }}({{ row.user.userId || "" }})</div>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('联系人姓名')" prop="userName">
                                <template #default="{ row }">
                                    {{ row.merchantData.contactName }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('联系人电话')" prop="contactMobile">
                                <template #default="{ row }">
                                    <MobileCard :mobile="row.merchantData.contactPhone"></MobileCard>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('申请状态')" prop="statusText">
                                <template #default="{ row }">
                                    <template v-if="row.status == 1">
                                        <StatusDot :flicker="true"></StatusDot>
                                    </template>
                                    <template v-if="row.status == 10">
                                        <StatusDot color="#52c41a" :flicker="true"></StatusDot>
                                    </template>
                                    <template v-if="row.status == 20">
                                        <StatusDot color="#f5222d" :flicker="false"></StatusDot>
                                    </template>
                                    {{ row.statusText }}
                                </template>
                            </el-table-column>
                            <el-table-column :width="160" :label="$t('申请日期')" prop="addTime" sortable="custom"></el-table-column>
                            <el-table-column :width="160" :label="$t('审核时间')" prop="auditTime">
                                <template #default="{ row }">
                                    {{ row.status != 1 && row.auditTime ? row.auditTime : "-" }}
                                </template>
                            </el-table-column>
                            <el-table-column :width="120" :label="$t('审核人')" prop="auditAdminName">
                                <template #default="{ row }">
                                    {{ row.status != 1 && row.auditAdminName ? row.auditAdminName : "-" }}
                                </template>
                            </el-table-column>
                            <el-table-column :width="150" fixed="right" :label="$t('操作')">
                                <template #default="{ row }">
                                    <DialogForm
                                        :showClose="false"
                                        :showOnOk="false"
                                        :params="{ act: 'detail', id: row.merchantApplyId }"
                                        isDrawer
                                        path="adminMerchant/apply/Info"
                                        :title="$t('申请详情')"
                                        width="600px"
                                        @okCallback="loadFilter"
                                    >
                                        <a class="btn-link">{{ $t('详情') }}</a>
                                    </DialogForm>
                                    <el-divider v-if="row.status == 20" direction="vertical" />
                                    <DeleteRecord
                                        v-if="row.status == 20"
                                        :params="{ id: row.merchantApplyId }"
                                        :requestApi="delApply"
                                        @afterDelete="loadFilter"
                                        >{{ $t('删除') }}</DeleteRecord
                                    >
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
import { onMounted, reactive, ref } from "vue";
import { DeleteRecord, Pagination } from "@/components/list";
import { message } from "ant-design-vue";
import { useConfigStore } from "@/store/config";
import { ApplyFilterParams, ApplyFilterState } from "@/types/adminMerchant/apply";
import { batchSubmit, delApply, getApplyList } from "@/api/adminMerchant/apply";
import SelectConfig from "@/views/adminMerchant/apply/src/SelectConfig.vue";
import { Tag } from "@/components/form";
import StatusDot from "@/components/form/src/StatusDot.vue";
import MobileCard from "@/components/list/src/MobileCard.vue";
import { useListRequest } from '@/hooks/useListRequest';
const config: any = useConfigStore();
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
} = useListRequest<ApplyFilterState, ApplyFilterParams>({
  apiFunction: getApplyList,
  idKey: 'merchantApplyId',
  defaultParams: {
      sortField: '',
      sortOrder: '',
      username: '',
      page: 1,
      size: config.get("pageSize"),
  }
});
// 批量操作
const onBatchSubmit = async (action: string) => {
  await onBatchAction(action, batchSubmit);
};

// 初始化加载
loadFilter();

const onChangeFilter = (value: any) => {
    filterParams.status = value;
    filterParams.page = 1
    loadFilter();
};
</script>
<style lang="less" scoped>
.ssdw {
    display: flex;
    align-items: center;
    gap: 4px;
}
</style>
