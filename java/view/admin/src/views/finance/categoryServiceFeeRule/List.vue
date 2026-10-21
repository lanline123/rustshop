/** * @file 类目服务费率规则列表页 * @description 管理店铺/门店与供应商的类目服务费率规则 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <div class="category-service-tabs">
                        <el-tabs v-model="activeTab" @tab-change="onTabChange">
                            <el-tab-pane :label="storeTabLabel" name="store" />
                        </el-tabs>
                    </div>
                    <div class="list-table-tool-row">
                        <div class="list-table-tool-col">
                            <el-space wrap>
                                <TigInput
                                    v-model="filterParams.keyword"
                                    name="keyword"
                                    :placeholder="$t('请输入规则名称')"
                                    clearable
                                    @keyup.enter="onSearchSubmit"
                                    @clear="onSearchSubmit"
                                >
                                    <template #append>
                                        <el-button @click="onSearchSubmit"><span class="iconfont icon-chakan1"></span></el-button>
                                    </template>
                                </TigInput>
                                <el-select v-model="filterParams.status" :placeholder="$t('请选择规则状态')" clearable @change="onSearchSubmit">
                                    <el-option :label="$t('启用')" :value="1" />
                                    <el-option :label="$t('禁用')" :value="0" />
                                </el-select>
                                <el-button @click="resetParams">{{ $t('重置') }}</el-button>
                            </el-space>
                        </div>
                        <div class="list-table-tool-col">
                            <DialogForm
                                :params="{ act: 'add', tabKey: activeTab }"
                                isDrawer
                                :title="$t('新增规则')"
                                width="1000px"
                                path="finance/categoryServiceFeeRule/Info"
                                @okCallback="loadFilter"
                            >
                                <el-button type="primary">{{ $t('新增规则') }}</el-button>
                            </DialogForm>
                        </div>
                    </div>
                </div>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table :data="filterState" :loading="loading" row-key="id" @sort-change="onSortChange">
                            <el-table-column :label="$t('费率名称')" min-width="180" prop="ruleName" />
                            <el-table-column :label="$t('适用类目')" min-width="160" prop="categoryCount">
                                <template #default="{ row }">
                                    {{ row.categoryCount ? `${row.categoryCount}个类目` : "--" }}
                                </template>
                            </el-table-column>
                            <el-table-column min-width="150" prop="serviceFeeRate" sortable="custom">
                                <template #header>
                                    <div class="table-header-with-tip">
                                        <span>{{ $t('服务费率') }}</span>
                                        <el-tooltip placement="top" effect="light">
                                            <template #content>
                                                <div class="service-rate-rule-tip">
                                                    <div>{{ $t('1、优先执行指定{storeObjectLabel}+ 精确匹配品类的专属规则；', { storeObjectLabel }) }}</div>
                                                    <div>{{ $t('2、若无专属规则，则执行全部{storeObjectLabel}+ 精确匹配品类的通用规则；', { storeObjectLabel }) }}</div>
                                                    <div>{{ $t('3、以上均未匹配时，自动采用系统全局默认费率兜底。') }}</div>
                                                </div>
                                            </template>
                                            <el-icon class="header-tip-icon"><QuestionFilled /></el-icon>
                                        </el-tooltip>
                                    </div>
                                </template>
                                <template #default="{ row }"> {{ row.serviceFeeRate }}% </template>
                            </el-table-column>
                            <el-table-column :label="$t('规则状态')" width="120" align="center">
                                <template #default="{ row }">
                                    <el-switch
                                        :model-value="row.status"
                                        :active-value="1"
                                        :inactive-value="0"
                                        @change="(value: unknown) => onStatusChange(row, value as CategoryFeeRuleStatus)"
                                    />
                                </template>
                            </el-table-column>
                            <el-table-column fixed="right" :label="$t('操作')" width="140">
                                <template #default="{ row }">
                                    <DialogForm
                                        :params="{ act: 'edit', id: row.id, tabKey: activeTab }"
                                        isDrawer
                                        :title="$t('编辑规则')"
                                        width="1000px"
                                        path="finance/categoryServiceFeeRule/Info"
                                        @okCallback="loadFilter"
                                    >
                                        <a class="btn-link">{{ $t('编辑') }}</a>
                                    </DialogForm>
                                    <el-divider direction="vertical" />
                                    <el-popconfirm :title="$t('删除后规则将无法恢复，请确认是否删除该规则？')" @confirm="onDelete(row)">
                                        <template #reference>
                                            <a class="btn-link">{{ $t('删除') }}</a>
                                        </template>
                                    </el-popconfirm>
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
import { computed, onMounted, ref } from "vue";
import { QuestionFilled } from "@element-plus/icons-vue";
import { message } from "ant-design-vue";
import { useRoute } from "vue-router";
import { DialogForm } from "@/components/dialog";
import { Pagination } from "@/components/list";
import { useConfigStore } from "@/store/config";
import { useListRequest } from "@/hooks/useListRequest";
import { isStore, isS2b2c, isOverseas } from "@/utils/version";
import { delCategoryFeeRule, getCategoryFeeRuleList, updateCategoryFeeRuleStatus } from "@/api/finance/categoryFeeRule";
import type {
    CategoryFeeRuleFilterParams,
    CategoryFeeRuleFilterState,
    CategoryFeeRuleStatus,
    CategoryFeeRuleTargetType
} from "@/types/finance/categoryFeeRule";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
type FeeRuleTabKey = "store" | "supplier";

const config = useConfigStore();
const route = useRoute();
const activeTab = ref<FeeRuleTabKey>("store");
const storeObjectLabel = computed(() => (isStore() ? t('门店') : t('店铺')));
const storeTabLabel = computed(() => t('{value}服务费', { value: storeObjectLabel.value }));

const resolveTargetType = (tabKey: FeeRuleTabKey): CategoryFeeRuleTargetType => (tabKey === "store" ? 1 : 2);
const resolveTabKey = (tab: unknown): FeeRuleTabKey => (tab === "supplier" ? "supplier" : "store");

const {
    listData: filterState,
    loading,
    total,
    filterParams,
    loadData: loadFilter,
    onSearchSubmit,
    onSortChange,
    resetParams
} = useListRequest<CategoryFeeRuleFilterState, CategoryFeeRuleFilterParams>(
    {
        apiFunction: getCategoryFeeRuleList,
        idKey: "id",
        defaultParams: {
            page: 1,
            size: config.get("pageSize"),
            sortField: "",
            sortOrder: "",
            keyword: "",
            status: "",
            targetType: resolveTargetType(activeTab.value)
        }
    },
    ["targetType"]
);

const onTabChange = () => {
    filterParams.page = 1;
    filterParams.targetType = resolveTargetType(activeTab.value);
    loadFilter();
};

const onDelete = async (row: CategoryFeeRuleFilterState) => {
    try {
        await delCategoryFeeRule({ ids: [row.id] });
        message.success(t('操作成功'));
        if (filterState.value.length <= 1 && Number(filterParams.page) > 1) {
            filterParams.page = Number(filterParams.page) - 1;
        }
        await loadFilter();
    } catch (error: any) {
        message.error(error.message);
    }
};

const showMutationSuccess = (disabledRuleNames?: string[]) => {
    if (disabledRuleNames?.length) {
        message.success(t('操作成功，已自动禁用：{join}', { join: disabledRuleNames.join("、") }));
        return;
    }
    message.success(t('操作成功'));
};

const onStatusChange = async (row: CategoryFeeRuleFilterState, nextStatus: CategoryFeeRuleStatus) => {
    const previousStatus = row.status;
    row.status = nextStatus;
    try {
        const result = await updateCategoryFeeRuleStatus({ id: row.id, status: nextStatus });
        showMutationSuccess(result);
        await loadFilter();
    } catch (error: any) {
        row.status = previousStatus;
        message.error(error.message);
    }
};

onMounted(() => {
    activeTab.value = resolveTabKey(route.query.tab);
    filterParams.targetType = resolveTargetType(activeTab.value);
    loadFilter();
});
</script>

<style lang="less" scoped>
.category-service-tabs {
    margin-bottom: 12px;
}

.table-header-with-tip {
    display: inline-flex;
    align-items: center;
    gap: 4px;
}

.header-tip-icon {
    color: #999;
    cursor: help;
}

.service-rate-rule-tip {
    line-height: 1.6;
}
</style>
