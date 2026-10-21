<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <Alert :message="$t('这里管理当前登录主体已绑定的物流公司。发货与默认物流仅会使用此处已绑定的数据。')" type="info" show-icon closable />
                <div class="list-table-tool lyecs-search-warp">
                    <div class="list-table-tool-row">
                        <div class="simple-form-warp">
                            <div class="simple-form-field">
                                <div class="form-group">
                                    <div class="control-container">
                                        <TigInput v-model="filterParams.keyword" name="keyword" :placeholder="$t('输入物流公司名称')"
                                            @keyup.enter="onSearchSubmit" clearable @clear="onSearchSubmit">
                                            <template #append>
                                                <el-button @click="onSearchSubmit"><span
                                                        class="iconfont icon-chakan1"></span></el-button>
                                            </template>
                                        </TigInput>
                                    </div>
                                </div>
                            </div>
                            <div class="simple-form-field">
                                <div class="form-group">
                                    <DialogForm :params="{ act: 'add' }" isDrawer
                                        path="setting/logisticsCompanyConfig/Info" :title="$t('添加物流公司')" width="600px"
                                        @okCallback="loadFilter">
                                        <el-button type="primary">{{ $t('添加物流公司') }}</el-button>
                                    </DialogForm>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table :data="filterState" :loading="loading" :total="total" row-key="logisticsId"
                            @sort-change="onSortChange">
                            <el-table-column :label="$t('物流公司名称')" min-width="220">
                                <template #default="{ row }">
                                    <div class="flex flex-align-center" style="gap: 8px; flex-wrap: wrap;">
                                        <span>{{ row.logisticsName }}</span>
                                        <el-tag v-if="row.isDefault === 1" size="small" effect="plain"
                                            type="primary">{{ $t('默认') }}</el-tag>
                                    </div>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('编号')" min-width="120">
                                <template #default="{ row }">
                                    {{ isKdniao ? (row.apiLogisticsCode || row.logisticsCode || "--") : (row.kuaidi100Code || row.logisticsCode || "--") }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('物流公司描述')" prop="logisticsDesc" min-width="220"
                                show-overflow-tooltip />
                            <template v-if="isKdniao">
                                <el-table-column :label="$t('客户编码')" prop="customerName" min-width="140" show-overflow-tooltip>
                                    <template #default="{ row }">
                                        {{ row.customerName || "--" }}
                                    </template>
                                </el-table-column>
                                <el-table-column :label="$t('月结号')" prop="monthCode" min-width="140" show-overflow-tooltip>
                                    <template #default="{ row }">
                                        {{ row.monthCode || "--" }}
                                    </template>
                                </el-table-column>
                                <el-table-column :label="$t('网点编码')" prop="sendSite" min-width="140" show-overflow-tooltip>
                                    <template #default="{ row }">
                                        {{ row.sendSite || "--" }}
                                    </template>
                                </el-table-column>
                                <el-table-column :label="$t('网点名称')" prop="sendStaff" min-width="140" show-overflow-tooltip>
                                    <template #default="{ row }">
                                        {{ row.sendStaff || "--" }}
                                    </template>
                                </el-table-column>
                            </template>
                            <template v-else>
                                <el-table-column :label="$t('partnerId')" prop="kuaidi100PartnerId" min-width="140" show-overflow-tooltip>
                                    <template #default="{ row }">
                                        {{ row.kuaidi100PartnerId || "--" }}
                                    </template>
                                </el-table-column>
                                <el-table-column :label="$t('收件网点名称')" prop="kuaidi100Net" min-width="140" show-overflow-tooltip>
                                    <template #default="{ row }">
                                        {{ row.kuaidi100Net || "--" }}
                                    </template>
                                </el-table-column>
                                <el-table-column :label="$t('面单模板ID')" prop="kuaidi100TempId" min-width="140" show-overflow-tooltip>
                                    <template #default="{ row }">
                                        {{ row.kuaidi100TempId || "--" }}
                                    </template>
                                </el-table-column>
                            </template>
                            <el-table-column :label="$t('是否启用')" prop="isEnabled" sortable="custom" width="120">
                                <template #default="{ row }">
                                    <Switch v-model:checked="row.isEnabled"
                                        :params="{ id: row.configId, field: 'isEnabled' }"
                                        :requestApi="updateLogisticsCompanyConfigField" @callback="loadFilter" />
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('排序')" prop="sortOrder" sortable="custom" width="120">
                                <template #default="{ row }">
                                    <PopForm v-model:org-value="row.sortOrder"
                                        :params="{ id: row.logisticsId, field: 'sortOrder' }"
                                        :requestApi="updateLogisticsCompanyConfigField" :extra="$t('默认值为50，数值越小，排序越靠前')"
                                        :label="$t('排序')" type="integer">
                                        <div>{{ row.sortOrder }}</div>
                                    </PopForm>
                                </template>
                            </el-table-column>
                            <el-table-column :width="220" fixed="right" :label="$t('操作')">
                                <template #default="{ row }">
                                    <DialogForm :params="{ act: 'detail', id: row.configId }" isDrawer
                                        path="setting/logisticsCompanyConfig/Info" :title="$t('编辑物流公司')" width="600px"
                                        @okCallback="loadFilter">
                                        <a class="btn-link">{{ $t('编辑') }}</a>
                                    </DialogForm>
                                    <template v-if="row.isDefault !== 1">
                                        <el-divider direction="vertical" />
                                        <DeleteRecord :params="{ id: row.configId }"
                                            :requestApi="setDefaultLogisticsCompanyConfig" :title="$t('确认将该物流公司设为默认吗？')"
                                            :message="$t('已设为默认物流公司')" @afterDelete="loadFilter"> {{ $t('设为默认') }} </DeleteRecord>
                                    </template>
                                    <el-divider direction="vertical" />
                                    <DeleteRecord :params="{ id: row.configId }" :requestApi="delLogisticsCompanyConfig"
                                        :title="$t('确认解绑该物流公司吗？')" :message="$t('解绑成功')" @afterDelete="loadFilter"> {{ $t('解绑') }} </DeleteRecord>
                                </template>
                            </el-table-column>
                            <template #empty>
                                <div class="empty-warp">.
                                    <div v-if="!loading" class="empty-bg">{{ $t('暂无数据') }}</div>
                                </div>
                            </template>
                        </el-table>
                    </a-spin>
                    <div v-if="total > 0" class="pagination-con">
                        <Pagination v-model:page="filterParams.page" v-model:size="filterParams.size" :total="total"
                            @callback="loadFilter" />
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { computed, onMounted, ref } from "vue";
import { Alert } from "ant-design-vue";
import { DialogForm } from "@/components/dialog";
import { PopForm } from "@/components/pop-form";
import { DeleteRecord, Pagination, Switch } from "@/components/list";
import { useConfigStore } from "@/store/config";
import { useListRequest } from "@/hooks/useListRequest";
import { getLogisticsProvider, LOGISTICS_PROVIDER_KDNIAO } from "@/api/setting/baseLogistics";
import type { LogisticsProvider } from "@/types/setting/baseLogistics.d";
import type { LogisticsCompanyConfigFilterParams, LogisticsCompanyConfigFilterState } from "@/types/setting/logisticsCompanyConfig";
import {
    delLogisticsCompanyConfig,
    getLogisticsCompanyConfigList,
    setDefaultLogisticsCompanyConfig,
    updateLogisticsCompanyConfigField,
} from "@/api/setting/logisticsCompanyConfig";

const config: any = useConfigStore();
const provider = ref<LogisticsProvider>(LOGISTICS_PROVIDER_KDNIAO);
const isKdniao = computed(() => provider.value !== 2);

const {
    listData: filterState,
    loading,
    total,
    filterParams,
    loadData: loadFilter,
    onSearchSubmit,
    onSortChange,
} = useListRequest<LogisticsCompanyConfigFilterState, LogisticsCompanyConfigFilterParams>({
    apiFunction: getLogisticsCompanyConfigList,
    idKey: "logisticsId",
    defaultParams: {
        sortField: "sortOrder",
        sortOrder: "asc",
        keyword: "",
        page: 1,
        size: config.get("pageSize"),
    },
});

// 先拿到全局服务商再请求列表，避免先发一次不带 providerType 的请求
onMounted(async () => {
    provider.value = await getLogisticsProvider();
    filterParams.providerType = provider.value;
    loadFilter();
});
</script>
