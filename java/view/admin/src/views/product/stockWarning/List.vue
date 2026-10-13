/**
 * @file 库存预警列表
 * @description 按商品/SKU 配置库存预警上下限，替代原先写死的统一阈值
 * @features
 * - 库存预警通知总开关
 * - 库存状态 Tab：全部、低库存、高库存、正常库存
 * - 关键词（商品名称/商品编码/SKU编码）与预警开关筛选
 * - 单个设置与批量设置预警规则
 * - 导入库存预警（模板三列：规格编码 / 上限 / 下限）
 * - 批量导出勾选行（ids=productId_skuId）
 * - 相关商品数量经 history.state 跳转商品列表
 */
<template>
    <div class="container">
        <div class="content_wrapper warning-switch-block">
            <div class="switch-line">
                <span class="switch-label">{{ $t('库存预警通知') }}</span>
                <el-switch v-model="warningEnabled" size="small" :active-value="1" :inactive-value="0" :loading="configLoading" :disabled="!canUpdateStockWarning" @change="onWarningEnabledChange" />
                <span class="switch-desc">{{ $t('开启后，达到库存上/下限的商品将会自动推送消息预警') }}</span>
            </div>
        </div>
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="section-header">
                    <div class="section-title">{{ $t('商品预警设置') }}</div>
                    <div class="tabs-box">
                        <el-tabs v-model="activeKey" tab-position="top" @tab-change="onTabChange">
                            <el-tab-pane v-for="item in stockStatusTabs" :key="item.value" :name="item.value" :label="item.label"></el-tab-pane>
                        </el-tabs>
                    </div>
                </div>
                <div class="list-table-tool lyecs-search-warp">
                    <div class="advanced-search-warp list-table-tool-row">
                        <div class="simple-form-warp">
                            <div class="simple-form-field">
                                <div class="form-group">
                                    <label class="control-label"><span>{{ $t('关键词：') }}</span></label>
                                    <div class="control-container">
                                        <TigInput
                                            v-model="filterParams.keyword"
                                            name="keyword"
                                            :placeholder="$t('商品名称/商品编码/SKU编码')"
                                            clearable
                                            @keyup.enter="onSearchSubmit"
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
                                    <label class="control-label"><span>{{ $t('库存预警：') }}</span></label>
                                    <div class="control-container">
                                        <el-select v-model="filterParams.warnEnabled" @change="onSearchSubmit">
                                            <el-option :value="-1" :label="$t('全部')" />
                                            <el-option :value="1" :label="$t('已开启')" />
                                            <el-option :value="0" :label="$t('已关闭')" />
                                        </el-select>
                                    </div>
                                </div>
                            </div>
                            <div class="simple-form-warp">
                                <div class="simple-form-field">
                                    <label class="control-label"></label>
                                    <div class="control-container">
                                        <el-button type="primary" plain @click="onSearchSubmit">{{ $t('搜索') }}</el-button>
                                        <el-button plain @click="onResetParams">{{ $t('重置') }}</el-button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="list-table-tool-row">
                    <div class="list-table-tool-col">
                        <el-space>
                            <DialogForm
                                v-if="canUpdateStockWarning"
                                :disabled="!selectedRows.length"
                                :params="batchDialogParams"
                                :title="$t('批量设置库存预警')"
                                :okText="$t('保存')"
                                path="product/stockWarning/SetWarning"
                                width="600px"
                                is-drawer
                                @okCallback="onSetCallback"
                            >
                                <el-button :disabled="!selectedRows.length">{{ $t('设置库存预警') }}</el-button>
                            </DialogForm>
                            <DialogForm
                                v-if="canUpdateStockWarning"
                                :maskClose="false"
                                :title="$t('导入库存预警')"
                                path="product/stockWarning/ImportWarning"
                                width="600px"
                                is-drawer
                                :showClose="false"
                                :showOnOk="false"
                                @callback="onImported"
                                @closeCallback="onImportClose"
                            >
                                <el-button>{{ $t('导入库存预警') }}</el-button>
                            </DialogForm>
                            <el-button :loading="exportLoading" :disabled="!selectedRows.length" @click="handleExport">{{ $t('批量导出') }}</el-button>
                            <span class="selected-total">{{ $t('共{total}条', { total }) }}<template v-if="selectedRows.length">{{ $t('，已选择{length}条', { length: selectedRows.length }) }}</template></span>
                        </el-space>
                    </div>
                </div>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table :data="filterState" :row-key="getRowKey" @selection-change="onTableSelectChange" @sort-change="onSortChange">
                            <el-table-column type="selection" width="40" align="center" />
                            <el-table-column :label="$t('商品名称')" min-width="400">
                                <template #default="{ row }">
                                    <div class="product-info">
                                        <div v-if="productThumb(row)" class="product-image">
                                            <Image :src="productThumb(row)" :width="64" :height="64" fit="contain" />
                                        </div>
                                        <div class="product-text">
                                            <div class="product-name">{{ row.productName }}</div>
                                            <div class="product-sn">{{ row.productSn || "-" }}</div>
                                        </div>
                                    </div>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('规格')" prop="productSkuName" min-width="180" :formatter="emptyText" />
                            <el-table-column :label="$t('相关商品')" min-width="90">
                                <template #default="{ row }">
                                    <a v-if="canJumpRelated(row)" class="btn-link" @click="onRelatedProductClick(row)">{{ row.relatedProductCount }}</a>
                                    <span v-else>-</span>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('实物库存')" prop="currentStock" min-width="110" sortable="custom" />
                            <el-table-column :label="$t('在途库存')" prop="transitStock" min-width="130" sortable="custom">
                                <template #header>
                                    <div class="header-with-tip">
                                        <div>{{ $t("在途库存") }}</div>
                                        <el-tooltip class="box-item" effect="light" placement="top" show-after="300">
                                            <template #content>
                                                <div style="padding: 5px 10px">{{ $t("采购未入库的数量") }}</div>
                                            </template>
                                            <el-icon style="margin-left: 5px" size="14" color="#999" @click.stop>
                                                <QuestionFilled />
                                            </el-icon>
                                        </el-tooltip>
                                    </div>
                                </template>
                                <template #default="{ row }">{{ formatLimit(row.transitStock) }}</template>
                            </el-table-column>
                            <el-table-column :label="$t('库存下限')" prop="lowerLimit" min-width="110" sortable="custom">
                                <template #default="{ row }">{{ formatLimit(row.lowerLimit) }}</template>
                            </el-table-column>
                            <el-table-column :label="$t('库存上限')" prop="upperLimit" min-width="110" sortable="custom">
                                <template #default="{ row }">{{ formatLimit(row.upperLimit) }}</template>
                            </el-table-column>
                            <el-table-column :label="$t('库存状态')" min-width="100">
                                <template #default="{ row }">
                                    <span :class="stockStatusClass(row)">{{ stockStatusLabel(row) }}</span>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('库存预警')" min-width="90" align="center">
                                <template #default="{ row }">{{ warnEnabledLabel(row.warnEnabled) }}</template>
                            </el-table-column>
                            <el-table-column v-if="canUpdateStockWarning" :label="$t('操作')" width="90" fixed="right">
                                <template #default="{ row }">
                                    <DialogForm
                                        :params="{
                                            targets: [{ productId: row.productId, skuId: row.skuId }],
                                            warnEnabled: row.warnEnabled,
                                            lowerLimit: row.lowerLimit,
                                            upperLimit: row.upperLimit
                                        }"
                                        :maskClose="false"
                                        :title="$t('库存预警设置')"
                                        :okText="$t('保存')"
                                        path="product/stockWarning/SetWarning"
                                        width="600px"
                                        is-drawer
                                        @okCallback="onSetCallback"
                                    >
                                        <a class="btn-link">{{ $t('设置') }}</a>
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
import { computed, ref } from "vue";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
import { useI18n } from "vue-i18n";
import { QuestionFilled } from "@element-plus/icons-vue";
import { Image } from "@/components/image";
import { Pagination } from "@/components/list";
import { DialogForm } from "@/components/dialog";
import { useConfigStore } from "@/store/config";
import { useUserStore } from "@/store/user";
import { useListRequest } from "@/hooks/useListRequest";
import { productImage } from "@/utils/format";
import requestExport from "@/utils/export";
import { exportStockWarning, getStockWarningSetting, getStockWarningList, saveStockWarningSetting } from "@/api/product/stockWarning";
import type { StockWarningFilterParams, StockWarningFilterState } from "@/types/product/stockWarning";

const { t } = useI18n();
const router = useRouter();
const config: any = useConfigStore();
const userStore = useUserStore();
const canUpdateStockWarning = computed(() => {
    const authList = userStore.userInfo?.authList || [];
    return authList.includes("all") || authList.includes("stockWarningUpdateManage");
});

// 状态取值对齐后端：不传为全部，页面上用 -1 表达
const STOCK_STATUS_ALL = -1;
const STOCK_STATUS_NORMAL = 0;
const STOCK_STATUS_LOW = 1;
const STOCK_STATUS_HIGH = 2;

const stockStatusTabs = computed(() => [
    { value: STOCK_STATUS_ALL, label: t("全部") },
    { value: STOCK_STATUS_LOW, label: t("低库存") },
    { value: STOCK_STATUS_HIGH, label: t("高库存") },
    { value: STOCK_STATUS_NORMAL, label: t("正常库存") }
]);

const activeKey = ref<number>(STOCK_STATUS_ALL);
const warningEnabled = ref<0 | 1>(0);
const configLoading = ref<boolean>(false);
const exportLoading = ref<boolean>(false);
const selectedRows = ref<StockWarningFilterState[]>([]);

const {
    listData: filterState,
    loading,
    total,
    filterParams,
    loadData: loadFilter,
    onSearchSubmit,
    onSortChange,
    resetParams
} = useListRequest<StockWarningFilterState, StockWarningFilterParams>({
    apiFunction: getStockWarningList,
    defaultParams: {
        page: 1,
        size: config.get("pageSize"),
        keyword: "",
        stockStatus: STOCK_STATUS_ALL,
        warnEnabled: -1,
        sortField: "",
        sortOrder: ""
    }
});

const selectedTargets = computed(() => selectedRows.value.map((item) => ({ productId: item.productId, skuId: item.skuId })));

const batchDialogParams = computed(() => ({
    targets: selectedTargets.value,
    warnEnabled: 1,
    lowerLimit: null,
    upperLimit: null
}));

// 列表按 SKU 成行，主键是 productId + skuId 组合
const getRowKey = (row: StockWarningFilterState) => `${row.productId}-${row.skuId}`;

// 接口只下发缩略图，仍走统一取图函数拼接 storageUrl
const productThumb = (row: StockWarningFilterState) => productImage({ picThumb: row.productPicThumb });

const EMPTY_TEXT = "-";

const emptyText = (row: any, column: any, cellValue: any) => cellValue || EMPTY_TEXT;

const formatLimit = (value: number | null) => (value === 0 || value ? value : EMPTY_TEXT);

// 未配置规则的行没有开关值，与上下限一样留空
const warnEnabledLabel = (value: 0 | 1 | null) => {
    if (value === null || value === undefined) return EMPTY_TEXT;
    return value === 1 ? t("开启") : t("关闭");
};

// 未开启预警的行状态无意义（后端会给「未配置」），统一留空
const isWarnEnabled = (row: StockWarningFilterState) => row.warnEnabled === 1;

const stockStatusLabel = (row: StockWarningFilterState) => {
    if (!isWarnEnabled(row)) return EMPTY_TEXT;
    const labelMap: Record<number, string> = {
        [STOCK_STATUS_LOW]: t("低库存"),
        [STOCK_STATUS_HIGH]: t("高库存"),
        [STOCK_STATUS_NORMAL]: t("正常库存")
    };
    return labelMap[row.stockStatus] || EMPTY_TEXT;
};

const stockStatusClass = (row: StockWarningFilterState) => {
    if (!isWarnEnabled(row)) return "";
    if (row.stockStatus === STOCK_STATUS_LOW) return "red";
    return "";
};

const canJumpRelated = (row: StockWarningFilterState) => row.relatedProductCount > 0 && !!row.relatedProductIds?.length;

const onRelatedProductClick = (row: StockWarningFilterState) => {
    router.push({
        name: "productManage",
        state: { relatedProductIds: row.relatedProductIds.map((id) => Number(id)) }
    });
};

const loadConfig = async () => {
    try {
        const result = await getStockWarningSetting();
        warningEnabled.value = result?.notifyEnabled === 1 ? 1 : 0;
    } catch (error: any) {
        message.error(error.message || t("请求失败"));
    }
};

const onWarningEnabledChange = async (value: any) => {
    if (!canUpdateStockWarning.value) {
        warningEnabled.value = value === 1 ? 0 : 1;
        return;
    }
    const previous: 0 | 1 = value === 1 ? 0 : 1;
    configLoading.value = true;
    try {
        await saveStockWarningSetting({ notifyEnabled: value });
        message.success(t("操作成功"));
    } catch (error: any) {
        warningEnabled.value = previous;
        message.error(error.message || t("操作失败"));
    } finally {
        configLoading.value = false;
    }
};

// 只导勾选行：后端按 productId_skuId 定位，筛选条件不参与
const handleExport = async () => {
    if (!selectedRows.value.length) {
        message.error(t("请先选择要导出的数据"));
        return;
    }
    exportLoading.value = true;
    try {
        const ids = selectedRows.value.map((item) => `${item.productId}_${item.skuId}`).join(",");
        const result = await exportStockWarning({ ids });
        requestExport(result, t("库存预警导出"));
    } catch (error: any) {
        message.error(error.message || t("操作失败"));
    } finally {
        exportLoading.value = false;
    }
};

const onTabChange = (value: number) => {
    filterParams.stockStatus = value;
    onSearchSubmit();
};

const onResetParams = () => {
    activeKey.value = STOCK_STATUS_ALL;
    resetParams();
};

const onTableSelectChange = (rows: StockWarningFilterState[]) => {
    selectedRows.value = rows;
};

const onSetCallback = () => {
    selectedRows.value = [];
    loadFilter();
};

// 导入弹窗调用过导入接口才通知，点「完成」和点关闭都收敛到关闭时刷新一次
const imported = ref<boolean>(false);

const onImported = (result: any) => {
    if (result?.imported) imported.value = true;
};

const onImportClose = () => {
    if (!imported.value) return;
    imported.value = false;
    loadFilter();
};

loadConfig();
loadFilter();
</script>

<style lang="less" scoped>
.container {
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.warning-switch-block {
    padding: 20px;
}

.switch-line {
    display: flex;
    align-items: center;
    gap: 10px;
}

.switch-label {
    font-weight: bold;
    font-size: 16px;
}

.switch-desc {
    color: #333;
    font-size: 14px;
}

.section-header {
    .section-title {
        font-weight: bold;
        font-size: 16px;
        line-height: 24px;
        margin-bottom: 10px;
    }
}

.tabs-box {
    :deep(.el-tabs__header) {
        margin-bottom: 0;
    }
}

.selected-total {
    color: #9598a6;
}

.header-with-tip {
    display: inline-flex;
    align-items: center;
}

.product-info {
    display: flex;

    .product-image {
        width: 64px;
        height: 64px;
        flex-shrink: 0;
    }

    .product-text {
        margin-left: 8px;
        min-width: 0;
        display: flex;
        flex-direction: column;
        justify-content: space-between;
    }

    .product-sn {
        color: #9598a6;
        font-size: 12px;
    }
}
</style>
