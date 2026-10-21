/** * @file 店铺账单明细页 * @description 展示店铺资金账单流水明细 * @features * - 按入账/下单时间筛选 * - 按账单类型/订单号筛选 * -
显示订单金额/平台服务费/实际结算金额 * - 账单汇总统计 */
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
                                        <label class="control-label"><span>{{ $t('账单时间：') }}</span></label>
                                        <div class="control-container">
                                            <el-select clearable :placeholder="$t('请选择状态')" v-model="filterParams.timeType" @change="onSearchSubmit">
                                                <el-option :value="1" :label="$t('入账时间')" />
                                                <el-option :value="2" :label="$t('下单时间')" />
                                            </el-select>
                                        </div>
                                    </div>
                                </div>
                                <div v-if="showShopSelect" class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"
                                            ><span>{{ source === "vendor" ? $t('供应商') : shopTypeName }}：</span></label
                                        >
                                        <div class="control-container">
                                            <SelectVendor v-if="source === 'vendor'" v-model:vendorId="filterParams.vendorId" @onChange="onSearchSubmit" />
                                            <SelectStore v-else-if="isMultiStore" v-model:storeId="filterParams.shopId" @onChange="onSearchSubmit" />
                                            <SelectShop v-else v-model:shopId="filterParams.shopId" @onChange="onSearchSubmit"></SelectShop>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('筛选时间：') }}</span></label>
                                        <SelectTimeInterval
                                            type="date"
                                            v-model:start-date="filterParams.startDateTime"
                                            v-model:end-date="filterParams.endDateTime"
                                            @callback="changeDateType(-1)"
                                        ></SelectTimeInterval>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <el-radio-group class="itemWidth" v-model="dateType" @change="changeDateType">
                                            <el-radio-button :value="0">{{ $t('今天') }}</el-radio-button>
                                            <el-radio-button :value="1">{{ $t('昨天') }}</el-radio-button>
                                            <el-radio-button :value="2">{{ $t('近7天') }}</el-radio-button>
                                            <el-radio-button :value="3">{{ $t('近30天') }}</el-radio-button>
                                            <el-radio-button :value="4">{{ $t('近半年') }}</el-radio-button>
                                        </el-radio-group>
                                    </div>
                                </div>

                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('入账类型：') }}</span></label>
                                        <div class="control-container">
                                            <el-select clearable :placeholder="$t('请选择入账类型')" v-model="filterParams.accountType" @change="onSearchSubmit">
                                                <el-option v-for="item in configData.accountType" :value="item.code" :label="item.description" />
                                            </el-select>
                                        </div>
                                    </div>
                                </div>

                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('类型：') }}</span></label>
                                        <div class="control-container">
                                            <el-select clearable :placeholder="$t('请选择类型')" v-model="filterParams.type" @change="onSearchSubmit">
                                                <el-option :value="0" :label="$t('全部')" />
                                                <el-option v-for="item in configData.statementType" :value="item.code" :label="item.description" />
                                            </el-select>
                                        </div>
                                    </div>
                                </div>

                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('支付方式：') }}</span></label>
                                        <div class="control-container">
                                            <el-select clearable :placeholder="$t('请选择支付方式：')" v-model="filterParams.paymentType" @change="onSearchSubmit">
                                                <el-option v-for="item in configData.payMethodType" :value="item.code" :label="item.description" />
                                            </el-select>
                                        </div>
                                    </div>
                                </div>

                                <div class="simple-form-field mr10">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('单号：') }}</span></label>
                                        <div class="control-container">
                                            <TigInput
                                                v-model="filterParams.recordSn"
                                                name="orderSn"
                                                :placeholder="$t('请输入单号')"
                                                @keyup.enter="onSearchSubmit"
                                                clearable
                                                @clear="onSearchSubmit"
                                            ></TigInput>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-warp">
                                    <div class="simple-form-field">
                                        <label class="control-label"></label>
                                        <div class="control-container">
                                            <el-button type="primary" plain @click="onSearchSubmit">{{ $t('搜索') }}</el-button>
                                            <el-button plain @click="handleResetParams">{{ $t('重置') }}</el-button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="list-table-tool-row">
                            <div class="list-table-tool-col">
                                <el-button type="primary" @click="orderExport" :loading="Exportloading">{{ $t('导出报表') }}</el-button>
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
                            <el-table-column :label="$t('类型')" prop="typeName">
                                <template #default="{ row }">
                                    {{ row.typeName || "--" }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('入账时间')" prop="settlementTime">
                                <template #default="{ row }">
                                    {{ row.settlementTime || "--" }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="shopTypeName + $t('名称')" prop="shopName" v-if="source === 'shop'">
                                <template #default="{ row }">
                                    {{ row.shopName || "--" }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('供应商名称')" prop="vendorName" v-if="source === 'vendor'">
                                <template #default="{ row }">
                                    {{ row.vendorName || "--" }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('业务单号')" prop="recordSn">
                                <template #default="{ row }">
                                    {{ row.recordSn || "--" }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('入账类型')" prop="accountTypeName">
                                <template #default="{ row }">
                                    {{ row.accountTypeName || "--" }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="`${$t('金额')}（${config.config.dollarSignCn}）`" prop="amount">
                                <template #default="{ row }">
                                    {{ row.amount || "--" }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="`${$t('余额')}（${config.config.dollarSignCn}）`" prop="accountBalance">
                                <template #default="{ row }">
                                    {{ row.accountBalance || "--" }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('支付方式')" prop="paymentType">
                                <template #default="{ row }">
                                    {{ row.paymentType || "--" }}
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
import { Pagination } from "@/components/list";
import { useConfigStore } from "@/store/config";
import type { StatementListFilterParams, StatementListItem } from "@/types/finance/shopFunds.d";
import { getStatementList, exportStatement } from "@/api/finance/shopFunds";
import { getStatementConfig } from "@/api/finance/statementDownload";
import { useListRequest } from "@/hooks/useListRequest";
import { SelectShop, SelectTimeInterval, SelectVendor, SelectStore } from "@/components/select";
import { getAdminType } from "@/utils/storage";
import { isMerchant, isStore } from "@/utils/version";
import { getDays } from "@/utils/util";
import { formattedDate } from "@/utils/time";
import { message } from "ant-design-vue";
import requestExport from "@/utils/export";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const props = defineProps({
    source: {
        type: String,
        default: "shop"
    },
    startDateTime: {
        type: String,
        default: ""
    },
    endDateTime: {
        type: String,
        default: ""
    }
});

const showShopSelect = computed(() => {
    // 仅平台（admin）可按组织筛选；非平台（shop/vendor/store等）隐藏
    return getAdminType() === "admin";
});

// 用于决定是否展示“门店”筛选（多门店版本）
const isMultiStore = computed(() => !isMerchant() && isStore());
const shopTypeName = computed(() => (isMerchant() ? t('店铺') : isStore() ? t('门店') : t('店铺')));
const config: any = useConfigStore();
const dateType = ref(-1);
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
} = useListRequest<StatementListItem, StatementListFilterParams>(
    {
        apiFunction: getStatementList,
        idKey: "invoiceId",
        defaultParams: {
            page: 1,
            size: config.get("pageSize"),
            sortField: "",
            sortOrder: "",
            keyword: "",
            startDateTime: props.startDateTime,
            endDateTime: props.endDateTime,
            timeType: 1,
            accountType: 0,
            type: 0,
            paymentType: "",
            recordSn: "",
            shopId: undefined,
            vendorId: undefined,
            source: props.source
        }
    },
    ["accountType"]
);
const configData = ref<any>({});
const _getStatementConfig = async () => {
    try {
        const result = await getStatementConfig();
        configData.value = result;
        filterParams.accountType = result.accountType[0].code;
        await loadFilter();
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};

_getStatementConfig();
const changeDateType = (event: number) => {
    if (event === -1) {
        dateType.value = event;
        return;
    }
    filterParams.startDateTime = formattedDate(new Date(), "YYYY-MM-DD");
    if (event === 0) {
        //今天
        filterParams.endDateTime = formattedDate(new Date(), "YYYY-MM-DD");
    }
    if (event === 1) {
        //昨天
        filterParams.startDateTime = formattedDate(getDays(1, "sub"), "YYYY-MM-DD");
        filterParams.endDateTime = formattedDate(getDays(1, "sub"), "YYYY-MM-DD");
    }
    if (event === 2) {
        //近七天
        filterParams.startDateTime = formattedDate(getDays(7, "sub"), "YYYY-MM-DD");
        filterParams.endDateTime = formattedDate(new Date(), "YYYY-MM-DD");
    }
    if (event === 3) {
        //近30天
        filterParams.startDateTime = formattedDate(getDays(30, "sub"), "YYYY-MM-DD");
        filterParams.endDateTime = formattedDate(new Date(), "YYYY-MM-DD");
    }
    if (event === 4) {
        //近半年
        filterParams.startDateTime = formattedDate(getDays(180, "sub"), "YYYY-MM-DD");
        filterParams.endDateTime = formattedDate(new Date(), "YYYY-MM-DD");
    }
    loadFilter();
};

const Exportloading = ref<boolean>(false);
// 导出订单
const orderExport = async () => {
    Exportloading.value = true;
    try {
        const result = await exportStatement(filterParams);
        Exportloading.value = false;
        requestExport(result, t('导出报表'));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        Exportloading.value = false;
    }
};

const handleResetParams = async () => {
    await _getStatementConfig();
    resetParams();
};
</script>
