<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="tabs-box">
                    <el-tabs v-model="activeTab" tab-position="top">
                        <el-tab-pane v-if="isKdniao" name="kdniao" :label="$t('快递鸟')"></el-tab-pane>
                        <el-tab-pane v-else name="kuaidi100" :label="$t('快递100')"></el-tab-pane>
                    </el-tabs>
                </div>
                <Alert
                    :message="$t('这里维护的是平台级物流公司基础资料。启用后，各登录主体才能在“物流设置”中绑定并用于发货。')"
                    type="warning"
                    show-icon
                    closable
                />
                <div class="list-table-tool lyecs-search-warp">
                    <div class="list-table-tool-row">
                        <div class="simple-form-warp">
                            <div class="simple-form-field">
                                <div class="form-group">
                                    <div class="control-container">
                                        <TigInput
                                            v-model="filterParams.keyword"
                                            name="keyword"
                                            :placeholder="$t('输入物流公司名称')"
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
                                    <DialogForm
                                        :params="{ act: 'add' }"
                                        isDrawer
                                        path="setting/logisticsCompany/Info"
                                        :title="$t('添加物流公司')"
                                        width="600px"
                                        @okCallback="loadFilter"
                                    >
                                        <el-button type="primary">{{ $t('添加物流公司') }}</el-button>
                                    </DialogForm>
                                </div>
                            </div>
                            <div class="simple-form-field">
                                <div class="form-group">
                                    <el-space>
                                        <DeleteRecord
                                            :disabled="selectedIds.length === 0"
                                            :params="{ ids: selectedIds }"
                                            :requestApi="handleBatchDelete"
                                            :title="$t('您确认要批量删除所选数据吗？')"
                                            @afterDelete="loadFilter"
                                        >
                                            <el-button :disabled="selectedIds.length === 0">{{ $t('批量删除') }}</el-button>
                                        </DeleteRecord>
                                        <DeleteRecord
                                            :params="{}"
                                            :requestApi="handleSync"
                                            :title="$t('更新后将覆盖您当前的物流公司数据，请确认是否更新？')"
                                            @afterDelete="loadFilter"
                                        >
                                            <el-button>
                                                <el-icon style="margin-right: 4px"><Refresh /></el-icon>
                                                {{ isKdniao ? $t('同步快递鸟数据') : $t('同步快递100数据') }}
                                            </el-button>
                                        </DeleteRecord>
                                        <span v-if="selectedIds.length > 0"> {{ $t('已选择：') }}<b>{{ selectedIds.length }}</b> {{ $t('项') }} </span>
                                        <span v-if="syncSummary" class="sync-summary">{{ syncSummary }}</span>
                                    </el-space>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table
                            :data="filterState"
                            :loading="loading"
                            :total="total"
                            row-key="logisticsId"
                            @selection-change="onSelectChange"
                            @sort-change="onSortChange"
                        >
                            <el-table-column type="selection" width="32" />
                            <el-table-column :label="$t('物流公司名称')">
                                <template #default="{ row }">
                                    <PopForm
                                        v-model:org-value="row.logisticsName"
                                        :max="10"
                                        :params="{ id: row.logisticsId, field: 'logisticsName' }"
                                        :requestApi="updateLogisticsCompanyField"
                                        :label="$t('物流公司名称')"
                                        type="input"
                                    >
                                        <div>{{ row.logisticsName }}</div>
                                    </PopForm>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('编号')" :prop="isKdniao ? 'apiLogisticsCode' : 'kuaidi100Code'">
                                <template #default="{ row }">
                                    <PopForm
                                        v-if="!isKdniao"
                                        v-model:org-value="row.kuaidi100Code"
                                        :max="32"
                                        :params="{ id: row.logisticsId, field: 'kuaidi100Code' }"
                                        :requestApi="updateLogisticsCompanyField"
                                        :label="$t('编号')"
                                        type="input"
                                    >
                                        <div>{{ row.kuaidi100Code }}</div>
                                    </PopForm>
                                    <PopForm
                                        v-else
                                        v-model:org-value="row.apiLogisticsCode"
                                        :max="10"
                                        :params="{ id: row.logisticsId, field: 'apiLogisticsCode' }"
                                        :requestApi="updateLogisticsCompanyField"
                                        :label="$t('编号')"
                                        type="input"
                                    >
                                        <div>{{ row.apiLogisticsCode || row.logisticsCode }}</div>
                                    </PopForm>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('物流公司描述')" prop="logisticsDesc">
                                <template #default="{ row }">
                                    <PopForm
                                        v-model:org-value="row.logisticsDesc"
                                        :max="100"
                                        :params="{ id: row.logisticsId, field: 'logisticsDesc' }"
                                        :requestApi="updateLogisticsCompanyField"
                                        :label="$t('物流公司描述')"
                                        type="textarea"
                                    >
                                        <div>{{ row.logisticsDesc }}</div>
                                    </PopForm>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('是否启用')" prop="isEnabled" sortable="custom">
                                <template #header>
                                    <span>{{ $t('是否启用') }}</span>
                                    <el-tooltip :content="$t('启用后该物流公司才能在各主体的物流设置中绑定使用')" placement="top">
                                        <el-icon style="margin-left: 4px; cursor: help; color: #909399">
                                            <QuestionFilled />
                                        </el-icon>
                                    </el-tooltip>
                                </template>
                                <template #default="{ row }">
                                    <Switch
                                        v-model:checked="row.isEnabled"
                                        :params="{ id: row.logisticsId, field: 'isEnabled' }"
                                        :requestApi="updateLogisticsCompanyField"
                                        @callback="loadFilter"
                                    />
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('排序')" prop="sortOrder" sortable="custom">
                                <template #default="{ row }">
                                    <PopForm
                                        v-model:org-value="row.sortOrder"
                                        :params="{ id: row.logisticsId, field: 'sortOrder' }"
                                        :requestApi="updateLogisticsCompanyField"
                                        :extra="$t('默认值为50，数值越小，排序越靠前')"
                                        :label="$t('排序')"
                                        type="integer"
                                    >
                                        <div>{{ row.sortOrder }}</div>
                                    </PopForm>
                                </template>
                            </el-table-column>
                            <el-table-column :width="150" fixed="right" :label="$t('操作')">
                                <template #default="{ row }">
                                    <DialogForm
                                        :params="{ act: 'detail', id: row.logisticsId }"
                                        isDrawer
                                        path="setting/logisticsCompany/Info"
                                        :title="$t('编辑物流公司')"
                                        width="600px"
                                        @okCallback="loadFilter"
                                    >
                                        <a class="btn-link">{{ $t('编辑') }}</a>
                                    </DialogForm>
                                    <el-divider direction="vertical" />
                                    <DeleteRecord :params="{ id: row.logisticsId }" :requestApi="delLogisticsCompany" @afterDelete="loadFilter"
                                        >{{ $t('删除') }} </DeleteRecord>
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
import { PopForm } from "@/components/pop-form";
import { DeleteRecord, Pagination, Switch } from "@/components/list";
import { Alert, message } from "ant-design-vue";
import { QuestionFilled, Refresh } from "@element-plus/icons-vue";
import { computed, onMounted, ref } from "vue";
import { useConfigStore } from "@/store/config";
import { LogisticsCompanyFilterParams, LogisticsCompanyFilterState } from "@/types/setting/logisticsCompany";
import { batchSubmit, delLogisticsCompany, getLogisticsCompanyList, updateLogisticsCompanyField, syncKdniaoData, syncKuaidi100Data } from "@/api/setting/logisticsCompany";
import { getLogisticsProvider, LOGISTICS_PROVIDER_KDNIAO } from "@/api/setting/baseLogistics";
import type { LogisticsProvider } from "@/types/setting/baseLogistics.d";
import { useI18n } from "vue-i18n";
import { useListRequest } from "@/hooks/useListRequest";

const { t } = useI18n();
const config: any = useConfigStore();
// 物流服务商全局单选，决定 tab、编号列取值与同步接口
const provider = ref<LogisticsProvider>(LOGISTICS_PROVIDER_KDNIAO);
const isKdniao = computed(() => provider.value !== 2);
const activeTab = ref("kdniao");
const syncSummary = ref("");

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
} = useListRequest<LogisticsCompanyFilterState, LogisticsCompanyFilterParams>({
    apiFunction: getLogisticsCompanyList,
    idKey: "logisticsId",
    defaultParams: {
        // 默认：启用排在前面
        sortField: "isEnabled",
        sortOrder: "desc",
        keyword: "",
        page: 1,
        size: config.get("pageSize")
    }
});

// 批量删除处理函数
const handleBatchDelete = async (params: any) => {
    return await batchSubmit("del", params);
};

// 同步物流公司数据，按当前服务商走对应接口
const handleSync = async (params: any) => {
    if (isKdniao.value) {
        return await syncKdniaoData(params);
    }
    const result = await syncKuaidi100Data({ overwriteName: true, defaultIsEnabled: 1 });
    const conflicted = Number(result?.conflicted || 0);
    syncSummary.value = t("本次同步：共{remoteTotal}条，新增{inserted}条，更新{updated}条，冲突{conflicted}条", {
        remoteTotal: Number(result?.remoteTotal || 0),
        inserted: Number(result?.inserted || 0),
        updated: Number(result?.updated || 0),
        conflicted
    });
    if (conflicted > 0) {
        // 冲突由后端跳过，不做任何自动合并，只提示人工核对
        message.warning(t("存在{conflicted}条冲突数据未同步，请人工核对物流公司列表", { conflicted }));
    }
    return result;
};

// 初始化加载：先拿到全局服务商再请求列表，避免先发一次不带 providerType 的请求
onMounted(async () => {
    provider.value = await getLogisticsProvider();
    activeTab.value = isKdniao.value ? "kdniao" : "kuaidi100";
    filterParams.providerType = provider.value;
    loadFilter();
});
</script>
<style lang="less" scoped>
.sync-summary {
    color: #909399;
}
</style>
