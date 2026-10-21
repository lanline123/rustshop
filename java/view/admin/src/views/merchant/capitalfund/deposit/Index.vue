/** * @file 店铺/商家后台 保证金首页 * @description 顶部总览三卡 + 操作按钮 + 下方保证金流水列表（供应商端复用） */
<template>
    <div class="container">
        <a-spin :spinning="pageLoading">
            <div class="deposit-page">
                <!-- 保证金总览卡片 -->
                <div class="content_wrapper">
                    <div class="title">{{ $t('保证金总览') }}</div>
                    <div class="balance-box flex-wrap">
                        <div class="label">
                            <div class="title"> {{ $t('保证金可用余额') }} <el-tooltip placement="bottom" effect="light">
                                    <template #content
                                        ><div class="tooltip-width">{{ $t('{shopLabel}超出平台类目要求标准、可自由申请提现的多缴保证金金额。', { shopLabel }) }}</div></template
                                    >
                                    <el-icon><QuestionFilled /></el-icon>
                                </el-tooltip>
                            </div>
                            <div class="num-box">
                                <span class="num">{{ priceFormat(overview.withdrawable) }}</span>
                                <DialogForm isDrawer :title="$t('保证金余额')" width="800px" path="merchant/capitalfund/deposit/src/AvailableDetail" :showOnOk="false">
                                    <el-button type="primary" link class="detail-link">{{ $t('明细') }}</el-button>
                                </DialogForm>
                            </div>
                        </div>
                        <div class="label">
                            <div class="title"> {{ $t('冻结金额') }} <el-tooltip placement="bottom" effect="light">
                                    <template #content>
                                        <div class="tooltip-width">{{ $t('平台依据{shopLabel}经营类目担保规则锁定的最低保障保证金金额，常规状态下不可提现转出；当类目保证金额度下调时，减少的冻结资金会自动释放至可用余额。', { shopLabel }) }}</div>
                                    </template>
                                    <el-icon><QuestionFilled /></el-icon>
                                </el-tooltip>
                            </div>
                            <div class="num-box">
                                <span class="num frozen">{{ priceFormat(overview.frozen) }}</span>
                                <DialogForm isDrawer :title="$t('保证金余额')" width="900px" path="merchant/capitalfund/deposit/src/AvailableDetail" :showOnOk="false">
                                    <el-button type="primary" link class="detail-link">{{ $t('冻结明细') }}</el-button>
                                </DialogForm>
                            </div>
                        </div>
                        <div class="label">
                            <div class="title"> {{ $t('待补缴金额') }} <el-tooltip placement="bottom" effect="light">
                                    <template #content>
                                        <div class="tooltip-width">{{ $t('当前{shopLabel}保证金总额未达到平台对应类目担保核定标准，需要补足的资金差额；需在规定缓冲期限内完成补缴，逾期未足额缴存将限制{shopLabel1}上新、接单等经营权限。', { shopLabel, shopLabel1: shopLabel }) }}</div>
                                    </template>
                                    <el-icon><QuestionFilled /></el-icon>
                                </el-tooltip>
                            </div>
                            <div class="num-box">
                                <span class="num red">{{ priceFormat(overview.pending) }}</span>
                            </div>
                        </div>
                    </div>

                    <div class="action-box">
                        <DialogForm
                            isDrawer
                            :title="$t('保证金充值')"
                            width="800px"
                            path="merchant/capitalfund/deposit/src/RechargeForm"
                            :disabled="!isOpenDeposit"
                            @okCallback="reload"
                        >
                            <el-button type="primary" :disabled="!isOpenDeposit">{{ $t('充值') }}</el-button>
                        </DialogForm>
                        <DialogForm
                            isDrawer
                            :title="$t('保证金退还申请')"
                            width="600px"
                            path="merchant/capitalfund/deposit/src/RefundForm"
                            :disabled="!refundable"
                            @okCallback="reload"
                        >
                            <el-button :disabled="!refundable">{{ $t('退还') }}</el-button>
                        </DialogForm>
                        <DialogForm
                            v-if="overview.hasProtocol"
                            isDrawer
                            :title="$t('保证金协议')"
                            width="800px"
                            path="merchant/capitalfund/deposit/src/Agreement"
                            :showOnOk="false"
                            :showClose="false"
                        >
                            <el-button link type="primary">{{ $t('《保证金协议》') }}</el-button>
                        </DialogForm>
                    </div>
                </div>

                <!-- 保证金流水 / 款项审核 卡片 -->
                <div class="content_wrapper deposit-content">
                    <el-tabs v-model="activeTab" class="deposit-tabs">
                        <!-- 保证金流水 -->
                        <el-tab-pane :label="$t('保证金流水')" name="log">
                            <div class="lyecs-table-list-warp">
                                <div class="list-table-tool lyecs-search-warp">
                                    <el-form :model="filterParams">
                                        <div class="list-table-tool-row advanced-search-warp">
                                            <div class="simple-form-warp">
                                                <div class="simple-form-field">
                                                    <div class="form-group">
                                                        <label class="control-label"><span>{{ $t('变动类型：') }}</span></label>
                                                        <div class="control-container">
                                                            <el-select v-model="filterParams.type" :placeholder="$t('全部')" clearable @change="handleSearchSubmit">
                                                                <el-option
                                                                    v-for="item in logTypeOptions"
                                                                    :key="item.value"
                                                                    :label="item.label"
                                                                    :value="item.value"
                                                                />
                                                            </el-select>
                                                        </div>
                                                    </div>
                                                </div>
                                                <div class="simple-form-field">
                                                    <div class="form-group">
                                                        <label class="control-label"><span>{{ $t('时间筛选：') }}</span></label>
                                                        <SelectTimeInterval type="date" v-model:startDate="startDate" v-model:endDate="endDate" clearable />
                                                    </div>
                                                </div>
                                                <div class="simple-form-field">
                                                    <el-radio-group v-model="quickRange" @change="onQuickRange">
                                                        <el-radio-button v-for="item in quickOptions" :key="item.value" :value="item.value">{{
                                                            item.label
                                                        }}</el-radio-button>
                                                    </el-radio-group>
                                                </div>
                                                <div class="simple-form-warp">
                                                    <div class="simple-form-field">
                                                        <label class="control-label"></label>
                                                        <el-button type="primary" plain @click="handleSearchSubmit">{{ $t('搜索') }}</el-button>
                                                        <el-button plain @click="onReset">{{ $t('重置') }}</el-button>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </el-form>
                                </div>
                                <div class="table-container">
                                    <a-spin :spinning="tableLoading">
                                        <el-table :data="filterState" row-key="shopDepositLogId">
                                            <el-table-column :label="$t('变动类型')" prop="typeText" min-width="100" />
                                            <el-table-column :label="$t('变动原因')" min-width="220">
                                                <template #default="{ row }">{{ row.remark || "--" }}</template>
                                            </el-table-column>
                                            <el-table-column :label="$t('收支金额(元)')" min-width="120">
                                                <template #default="{ row }">{{ priceFormat(row.changeAmount) }}</template>
                                            </el-table-column>
                                            <el-table-column :label="$t('现金总余额(元)')" min-width="120">
                                                <template #default="{ row }">{{ priceFormat(row.afterPaid) }}</template>
                                            </el-table-column>
                                            <el-table-column :label="$t('来源账户/去向账户')" min-width="220">
                                                <template #default="{ row }">
                                                    <template v-if="row.sourceAccount || row.destAccount">
                                                        <div v-if="row.sourceAccount">{{ $t('来源：{sourceAccount}', { sourceAccount: row.sourceAccount }) }}</div>
                                                        <div v-if="row.destAccount">{{ $t('去向：{destAccount}', { destAccount: row.destAccount }) }}</div>
                                                    </template>
                                                    <span v-else>--</span>
                                                </template>
                                            </el-table-column>
                                            <el-table-column :label="$t('变动时间')" prop="addTimeText" min-width="160" />
                                            <template #empty>
                                                <div class="empty-warp"><div v-if="!tableLoading" class="empty-bg">{{ $t('暂无数据') }}</div></div>
                                            </template>
                                        </el-table>
                                        <div v-if="total > 0" class="pagination-con">
                                            <Pagination
                                                v-model:page="filterParams.page"
                                                v-model:size="filterParams.size"
                                                :total="total"
                                                @callback="loadFilter"
                                            />
                                        </div>
                                    </a-spin>
                                </div>
                            </div>
                        </el-tab-pane>

                        <!-- 款项审核 -->
                        <el-tab-pane :label="$t('款项审核')" name="audit">
                            <div class="lyecs-table-list-warp">
                                <div class="list-table-tool lyecs-search-warp">
                                    <el-form :model="auditParams">
                                        <div class="list-table-tool-row advanced-search-warp">
                                            <div class="simple-form-warp">
                                                <div class="simple-form-field">
                                                    <div class="form-group">
                                                        <label class="control-label"><span>{{ $t('审核类型：') }}</span></label>
                                                        <div class="control-container">
                                                            <el-select
                                                                v-model="auditParams.auditType"
                                                                :placeholder="$t('请选择审核类型')"
                                                                clearable
                                                                @change="handleAuditSearch"
                                                            >
                                                                <el-option
                                                                    v-for="item in auditTypeOptions"
                                                                    :key="item.value"
                                                                    :label="item.label"
                                                                    :value="item.value"
                                                                />
                                                            </el-select>
                                                        </div>
                                                    </div>
                                                </div>
                                                <div class="simple-form-field">
                                                    <div class="form-group">
                                                        <label class="control-label"><span>{{ $t('审核状态：') }}</span></label>
                                                        <div class="control-container">
                                                            <el-select
                                                                v-model="auditParams.status"
                                                                :placeholder="$t('请选择审核状态')"
                                                                clearable
                                                                @change="handleAuditSearch"
                                                            >
                                                                <el-option
                                                                    v-for="item in auditStatusOptions"
                                                                    :key="item.value"
                                                                    :label="item.label"
                                                                    :value="item.value"
                                                                />
                                                            </el-select>
                                                        </div>
                                                    </div>
                                                </div>
                                                <div class="simple-form-field">
                                                    <div class="form-group">
                                                        <label class="control-label"><span>{{ $t('时间筛选：') }}</span></label>
                                                        <SelectTimeInterval
                                                            type="date"
                                                            v-model:startDate="auditStartDate"
                                                            v-model:endDate="auditEndDate"
                                                            clearable
                                                        />
                                                    </div>
                                                </div>
                                                <div class="simple-form-field">
                                                    <el-radio-group v-model="auditQuickRange" @change="onAuditQuickRange">
                                                        <el-radio-button v-for="item in quickOptions" :key="item.value" :value="item.value">{{
                                                            item.label
                                                        }}</el-radio-button>
                                                    </el-radio-group>
                                                </div>
                                                <div class="simple-form-warp">
                                                    <div class="simple-form-field">
                                                        <label class="control-label"></label>
                                                        <el-button type="primary" plain @click="handleAuditSearch">{{ $t('搜索') }}</el-button>
                                                        <el-button plain @click="onAuditReset">{{ $t('重置') }}</el-button>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </el-form>
                                </div>
                                <div class="table-container">
                                    <a-spin :spinning="auditTableLoading">
                                        <el-table :data="auditState" :row-key="auditRowKey">
                                            <el-table-column :label="$t('审核类型')" prop="auditTypeText" min-width="120" />
                                            <el-table-column :label="$t('交易金额')" min-width="120">
                                                <template #default="{ row }">{{ priceFormat(row.amount) }}</template>
                                            </el-table-column>
                                            <el-table-column :label="$t('审核状态')" prop="statusText" min-width="100" />
                                            <el-table-column :label="$t('备注')" min-width="160" show-overflow-tooltip>
                                                <template #default="{ row }">{{ row.remark || "--" }}</template>
                                            </el-table-column>
                                            <el-table-column :label="$t('提交时间')" prop="addTimeText" min-width="160" />
                                            <el-table-column :label="$t('关联凭证')" min-width="100" align="center">
                                                <template #default="{ row }">
                                                    <el-image
                                                        v-if="toImgList(row.payVoucher).length"
                                                        :src="toImgList(row.payVoucher)[0]"
                                                        :preview-src-list="toImgList(row.payVoucher)"
                                                        :preview-teleported="true"
                                                        fit="cover"
                                                        style="width: 40px; height: 40px"
                                                    />
                                                    <span v-else>--</span>
                                                </template>
                                            </el-table-column>
                                            <el-table-column :label="$t('操作')" min-width="80" fixed="right">
                                                <template #default="{ row }">
                                                    <DialogForm
                                                        :params="{ auditType: row.auditType, bizId: row.bizId }"
                                                        isDrawer
                                                        :title="$t('款项详情')"
                                                        width="700px"
                                                        path="merchant/capitalfund/deposit/src/PaymentAuditDetail"
                                                        :showOnOk="false"
                                                    >
                                                        <el-button type="primary" link>{{ $t('详细') }}</el-button>
                                                    </DialogForm>
                                                </template>
                                            </el-table-column>
                                            <template #empty>
                                                <div class="empty-warp"><div v-if="!auditTableLoading" class="empty-bg">{{ $t('暂无数据') }}</div></div>
                                            </template>
                                        </el-table>
                                        <div v-if="auditTotal > 0" class="pagination-con">
                                            <Pagination
                                                v-model:page="auditParams.page"
                                                v-model:size="auditParams.size"
                                                :total="auditTotal"
                                                @callback="loadAudit"
                                            />
                                        </div>
                                    </a-spin>
                                </div>
                            </div>
                        </el-tab-pane>
                    </el-tabs>
                </div>
            </div>
        </a-spin>
    </div>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { computed, onMounted, ref, watch } from "vue";
import dayjs from "dayjs";
import { message } from "ant-design-vue";
import { QuestionFilled } from "@element-plus/icons-vue";
import { DialogForm } from "@/components/dialog";
import { Pagination } from "@/components/list";
import { SelectTimeInterval } from "@/components/select";
import { useConfigStore } from "@/store/config";
import { useListRequest } from "@/hooks/useListRequest";
import { priceFormat } from "@/utils/format";
import { formattedDate } from "@/utils/time";
import { getDays } from "@/utils/util";
import { getShopDepositAuditList, getShopDepositLogList, getShopDepositOverview } from "@/api/merchant/capitalfund/deposit";
import { DepositTxType } from "@/types/finance/deposit.d";
import { getShopTypeName } from "@/utils/storage";
import type {
    ShopDepositAuditFilterParams,
    ShopDepositAuditState,
    ShopDepositLogFilterParams,
    ShopDepositLogState,
    ShopDepositOverview
} from "@/types/merchant/capitalfund/deposit.d";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const shopLabel = getShopTypeName();

const config = useConfigStore();
const pageLoading = ref(true);
const overview = ref<ShopDepositOverview>({ required: 0, paid: 0, pending: 0, withdrawable: 0, frozen: 0 });

// 当前选中的 Tab：log 保证金流水 / audit 款项审核
const activeTab = ref<string>("log");

const quickRange = ref<string>("");
const startDate = ref("");
const endDate = ref("");

// 关联凭证多图拆分（逗号分隔的 URL 串）
const toImgList = (voucher?: string) => (voucher ? voucher.split(",").filter(Boolean) : []);

// 款项审核筛选下拉项
const auditTypeOptions = [
    { value: 1, label: t('保证金充值') },
    { value: 2, label: t('保证金退还') }
];
const auditStatusOptions = [
    { value: 0, label: t('待审核') },
    { value: 1, label: t('已通过') },
    { value: 2, label: t('未通过') }
];

// 可用余额（可提现）大于 0 时允许发起退还
const refundable = computed(() => Number(overview.value.withdrawable || 0) > 0);

// 平台保证金功能开启时允许充值
const isOpenDeposit = computed(() => Number(config.get("isOpenDeposit") ?? 0) === 1);

const quickOptions = [
    { value: "today", label: t('今天') },
    { value: "yesterday", label: t('昨天') },
    { value: "7days", label: t('近7天') },
    { value: "30days", label: t('近30天') },
    { value: "halfYear", label: t('近半年') }
];

const logTypeOptions = [
    { value: DepositTxType.Recharge, label: t('充值入账') },
    { value: DepositTxType.Withdraw, label: t('提现出账') },
    { value: DepositTxType.Deduct, label: t('平台扣除') }
];

const {
    listData: filterState,
    loading,
    total,
    filterParams,
    loadData: loadFilter,
    onSearchSubmit,
    resetParams
} = useListRequest<ShopDepositLogState, ShopDepositLogFilterParams>({
    apiFunction: getShopDepositLogList,
    idKey: "shopDepositLogId",
    defaultParams: {
        page: 1,
        size: config.get("pageSize"),
        type: undefined,
        startTime: undefined,
        endTime: undefined
    }
});

const syncTimeParams = () => {
    filterParams.startTime = startDate.value ? dayjs(startDate.value).startOf("day").unix() : undefined;
    filterParams.endTime = endDate.value ? dayjs(endDate.value).endOf("day").unix() : undefined;
};

const handleSearchSubmit = () => {
    syncTimeParams();
    onSearchSubmit();
};

// 表格 loading：搜索/翻页时展示，首屏整页 loading 期间不重复展示
const tableLoading = computed(() => loading.value && !pageLoading.value);

// 款项审核合并列表（充值 + 退还）
const {
    listData: auditState,
    loading: auditLoading,
    total: auditTotal,
    filterParams: auditParams,
    loadData: loadAudit,
    onSearchSubmit: onAuditSearchSubmit,
    resetParams: resetAuditParams
} = useListRequest<ShopDepositAuditState, ShopDepositAuditFilterParams>({
    apiFunction: getShopDepositAuditList,
    idKey: "bizId",
    defaultParams: {
        page: 1,
        size: config.get("pageSize"),
        auditType: undefined,
        status: undefined,
        startTime: undefined,
        endTime: undefined
    }
});

// 充值/退还混合列表，bizId 可能跨类型重复，故以「类型-单号」作为行唯一键
const auditRowKey = (row: ShopDepositAuditState) => `${row.auditType}-${row.bizId}`;

const auditQuickRange = ref<string>("");
const auditStartDate = ref("");
const auditEndDate = ref("");

const auditTableLoading = computed(() => auditLoading.value);

const syncAuditTimeParams = () => {
    auditParams.startTime = auditStartDate.value ? dayjs(auditStartDate.value).startOf("day").unix() : undefined;
    auditParams.endTime = auditEndDate.value ? dayjs(auditEndDate.value).endOf("day").unix() : undefined;
};

const handleAuditSearch = () => {
    syncAuditTimeParams();
    onAuditSearchSubmit();
};

const loadOverview = async () => {
    try {
        const result = await getShopDepositOverview();
        overview.value = result;
    } catch (error: any) {
        message.error(error.message);
    }
};

const reload = () => {
    loadOverview();
    loadFilter();
    auditParams.page = 1;
    loadAudit();
};

// 根据快捷选项计算起止日期（YYYY-MM-DD）
const computeQuickRange = (value: string): [string, string] => {
    const fmtDate = (d: Date) => formattedDate(d, "YYYY-MM-DD");
    const today = new Date();
    switch (value) {
        case "today":
            return [fmtDate(today), fmtDate(today)];
        case "yesterday":
            return [fmtDate(getDays(1, "sub")), fmtDate(getDays(1, "sub"))];
        case "7days":
            return [fmtDate(getDays(7, "sub")), fmtDate(today)];
        case "30days":
            return [fmtDate(getDays(30, "sub")), fmtDate(today)];
        case "halfYear":
            return [fmtDate(getDays(180, "sub")), fmtDate(today)];
        default:
            return ["", ""];
    }
};

const onQuickRange = (value: string) => {
    [startDate.value, endDate.value] = computeQuickRange(value);
    handleSearchSubmit();
};

const onReset = () => {
    quickRange.value = "";
    startDate.value = "";
    endDate.value = "";
    resetParams();
};

const onAuditQuickRange = (value: string) => {
    [auditStartDate.value, auditEndDate.value] = computeQuickRange(value);
    handleAuditSearch();
};

const onAuditReset = () => {
    auditQuickRange.value = "";
    auditStartDate.value = "";
    auditEndDate.value = "";
    resetAuditParams();
};

// 切换 Tab 时按当前筛选条件刷新对应表格
watch(activeTab, (value) => {
    if (value === "log") {
        loadFilter();
    } else if (value === "audit") {
        loadAudit();
    }
});

onMounted(async () => {
    try {
        await Promise.all([loadOverview(), loadFilter()]);
    } finally {
        pageLoading.value = false;
    }
});
</script>

<style lang="less" scoped>
.deposit-page {
    display: flex;
    flex-direction: column;
    gap: 12px;
}

:deep(.ant-spin-nested-loading),
:deep(.ant-spin-container) {
    width: 100%;
}

.content_wrapper {
    & > .title {
        font-weight: bold;
        padding-bottom: 20px;
        font-size: 16px;
        line-height: 24px;
    }
    &.deposit-content {
        padding-top: 10px;
    }
}

.balance-box {
    display: flex;
    background-color: #f5f6fa;
    padding: 20px;
    margin-bottom: 20px;
    flex-wrap: wrap;
    gap: 16px 0;

    .label {
        flex: 0 0 20%;
        max-width: 20%;
        min-width: 280px;
        box-sizing: border-box;

        .title {
            font-size: 14px;
            color: #101010;
            margin-bottom: 12px;
            display: flex;
            align-items: center;
            gap: 6px;

            :deep(.el-icon) {
                color: #c8c9cc;
                font-size: 14px;
                cursor: pointer;
            }
        }

        .num-box {
            display: flex;
            align-items: baseline;
            flex-wrap: nowrap;
            gap: 12px;
            white-space: nowrap;

            :deep(.dialog-link) {
                display: inline-flex;
                flex-shrink: 0;
            }

            .num {
                font-size: 20px;
                line-height: 28px;
                font-weight: bold;
                color: #303133;
                flex-shrink: 0;

                &.red {
                    color: #fb0606;
                }

                &.frozen {
                    color: #a4adb3;
                }
            }

            .detail-link {
                font-size: 13px;
                padding: 0;
                height: auto;
                font-weight: normal;
                flex-shrink: 0;
            }
        }
    }

    @media (max-width: 992px) {
        .label {
            flex: 0 0 50%;
            max-width: 50%;
            min-width: calc(50% - 1px);
        }
    }

    @media (max-width: 576px) {
        padding: 16px;

        .label {
            flex: 0 0 100%;
            max-width: 100%;
            min-width: 0;

            .title {
                margin-bottom: 8px;
            }

            .num-box .num {
                font-size: 18px;
                line-height: 26px;
            }
        }
    }
}

.action-box {
    display: flex;
    align-items: center;
    margin-top: 16px;
    gap: 12px;
    flex-wrap: wrap;

    .el-button {
        font-size: 14px;
    }
}

.tooltip-width {
    width: 250px;
    white-space: normal;
    word-break: break-all;
}

.deposit-tabs {
    :deep(.el-tabs__item) {
        font-size: 16px;
        font-weight: bold;
    }
}
</style>
