/**
 * @file 款项审核列表页
 * @description 主双 tab（退还审核 / 充值审核）+ 状态 tab 联动 filterParams.status
 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <el-tabs v-model="mainTab" class="main-tabs" @tab-change="onMainTabChange">
                <el-tab-pane :label="$t('退还审核')" name="refund" />
                <el-tab-pane :label="$t('充值审核')" name="recharge" />
            </el-tabs>

            <div class="lyecs-table-list-warp">
                <TigTabs v-model="filterParams.status" :tabs="statusTabs" @onTabChange="onStatusTabChange" />

                <div class="list-table-tool lyecs-search-warp">
                    <el-form :model="filterParams">
                        <div class="list-table-tool-row advanced-search-warp">
                            <div class="simple-form-warp">
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('组织类型：') }}</span></label>
                                        <div class="control-container">
                                            <el-select v-model="filterParams.ownerType" :placeholder="$t('请选择组织类型')" clearable @change="onSearchSubmit">
                                                <el-option :label="$t('店铺')" :value="OrgScope.Shop" />
                                                <el-option :label="$t('供应商')" :value="OrgScope.Supplier" />
                                            </el-select>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('组织名称：') }}</span></label>
                                        <div class="control-container">
                                            <TigInput
                                                v-model="filterParams.ownerName"
                                                :placeholder="$t('请输入组织名称')"
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
                                        <label class="control-label"><span>{{ $t('主体类型：') }}</span></label>
                                        <div class="control-container">
                                            <el-select v-model="filterParams.subjectType" :placeholder="$t('请选择主体类型')" clearable @change="onSearchSubmit">
                                                <el-option :label="$t('个人')" :value="MerchantType.Personal" />
                                                <el-option :label="$t('企业')" :value="MerchantType.Enterprise" />
                                            </el-select>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field" v-if="mainTab === 'recharge'">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('支付方式：') }}</span></label>
                                        <div class="control-container">
                                            <el-select v-model="filterParams.payeeAccountType" :placeholder="$t('请选择支付方式')" clearable @change="onSearchSubmit">
                                                <el-option :label="$t('银行卡')" :value="1" />
                                                <el-option :label="$t('支付宝')" :value="2" />
                                                <el-option :label="$t('微信')" :value="3" />
                                            </el-select>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field" v-else>
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('退还类型：') }}</span></label>
                                        <div class="control-container">
                                            <el-select v-model="filterParams.type" :placeholder="$t('请选择退还类型')" clearable @change="onSearchSubmit">
                                                <el-option :label="$t('部分退还')" :value="1" />
                                                <el-option :label="$t('全额退还')" :value="2" />
                                            </el-select>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('申请时间：') }}</span></label>
                                        <SelectTimeInterval
                                            type="date"
                                            v-model:startDate="filterParams.startDate"
                                            v-model:endDate="filterParams.endDate"
                                            clearable
                                        />
                                    </div>
                                </div>
                                <div class="simple-form-warp">
                                    <div class="simple-form-field">
                                        <label class="control-label"></label>
                                        <el-button type="primary" plain @click="onSearchSubmit">{{ $t('搜索') }}</el-button>
                                        <el-button plain @click="resetParams">{{ $t('重置') }}</el-button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </el-form>
                </div>

                <div class="table-container">
                    <a-spin :spinning="loading">
                        <!-- 充值审核 -->
                        <el-table v-if="mainTab === 'recharge'" :data="filterState" row-key="shopDepositRechargeId">
                            <el-table-column :label="$t('组织名称')" min-width="180">
                                <template #default="{ row }">{{ row.shopName || row.ownerName || "--" }}</template>
                            </el-table-column>
                            <el-table-column :label="$t('主体类型')" prop="subjectTypeText" min-width="100">
                                <template #default="{ row }">{{ row.subjectTypeText || "--" }}</template>
                            </el-table-column>
                            <el-table-column :label="$t('交易金额')" min-width="150">
                                <template #default="{ row }">{{ priceFormat(row.amount) }}</template>
                            </el-table-column>
                            <el-table-column :label="$t('收款账户')" min-width="220">
                                <template #default="{ row }">{{ formatPayeeAccount(row) }}</template>
                            </el-table-column>
                            <el-table-column :label="$t('支付方式')" prop="payeeAccountTypeText" min-width="100" />
                            <el-table-column :label="$t('审核状态')" prop="statusText" min-width="100" />
                            <el-table-column :label="$t('交易备注')" prop="remark" min-width="120" show-overflow-tooltip />
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
                            <el-table-column fixed="right" :label="$t('操作')" width="130">
                                <template #default="{ row }">
                                    <DialogForm :params="{ id: row.shopDepositRechargeId, mode: 'view' }" isDrawer :title="$t('充值详情')" width="700px" path="finance/deposit/audit/src/RechargeAuditInfo" :showOnOk="false">
                                        <a class="btn-link">{{ $t('详细') }}</a>
                                    </DialogForm>
                                    <template v-if="row.status === 0">
                                        <el-divider direction="vertical" />
                                        <DialogForm :params="{ id: row.shopDepositRechargeId, mode: 'audit' }" isDrawer :title="$t('充值审核')" width="700px" path="finance/deposit/audit/src/RechargeAuditInfo" @okCallback="loadFilter">
                                            <a class="btn-link">{{ $t('去审核') }}</a>
                                        </DialogForm>
                                    </template>
                                </template>
                            </el-table-column>
                            <template #empty>
                                <div class="empty-warp"><div v-if="!loading" class="empty-bg">{{ $t('暂无数据') }}</div></div>
                            </template>
                        </el-table>

                        <!-- 退还审核 -->
                        <el-table v-else :data="filterState" row-key="shopDepositRefundId">
                            <el-table-column :label="$t('组织名称')" min-width="120">
                                <template #default="{ row }">{{ row.shopName || row.ownerName || "--" }}</template>
                            </el-table-column>
                            <el-table-column :label="$t('主体类型')" prop="subjectTypeText" min-width="100">
                                <template #default="{ row }">{{ row.subjectTypeText || "--" }}</template>
                            </el-table-column>
                            <el-table-column :label="$t('退还类型')" prop="typeText" min-width="100" />
                            <el-table-column :label="$t('申请金额')" min-width="110">
                                <template #default="{ row }">{{ priceFormat(row.amount) }}</template>
                            </el-table-column>
                            <el-table-column :label="$t('审核状态')" prop="statusText" min-width="100" />
                            <el-table-column :label="$t('申请时间')" prop="addTimeText" min-width="160" />
                            <el-table-column fixed="right" :label="$t('操作')" width="130">
                                <template #default="{ row }">
                                    <DialogForm :params="{ id: row.shopDepositRefundId, mode: 'view' }" isDrawer :title="$t('退还详情')" width="700px" path="finance/deposit/audit/src/RefundAuditInfo" :showOnOk="false">
                                        <a class="btn-link">{{ $t('详细') }}</a>
                                    </DialogForm>
                                    <template v-if="row.status === 0">
                                        <el-divider direction="vertical" />
                                        <DialogForm :params="{ id: row.shopDepositRefundId, mode: 'audit' }" isDrawer :title="$t('退还审核')" width="700px" path="finance/deposit/audit/src/RefundAuditInfo" @okCallback="loadFilter">
                                            <a class="btn-link">{{ $t('去审核') }}</a>
                                        </DialogForm>
                                    </template>
                                </template>
                            </el-table-column>
                            <template #empty>
                                <div class="empty-warp"><div v-if="!loading" class="empty-bg">{{ $t('暂无数据') }}</div></div>
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
import { DialogForm } from "@/components/dialog";
import { Pagination } from "@/components/list";
import { SelectTimeInterval } from "@/components/select";
import { useConfigStore } from "@/store/config";
import { useListRequest } from "@/hooks/useListRequest";
import { priceFormat } from "@/utils/format";
import { getRechargeAuditList, getRefundAuditList } from "@/api/finance/deposit";
import { MerchantType, OrgScope } from "@/types/finance/deposit.d";
import type { PaymentAuditFilterParams, PaymentAuditFilterState, RechargeAuditDetail } from "@/types/finance/deposit.d";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
type MainTab = "refund" | "recharge";

const config = useConfigStore();
const mainTab = ref<MainTab>("refund");

const rechargeStatusTabs = [
    { value: "", label: t('全部'), count: 0, isShow: true },
    { value: "0", label: t('待审核'), count: 0, isShow: true },
    { value: "1", label: t('已通过'), count: 0, isShow: true },
    { value: "2", label: t('未通过'), count: 0, isShow: true }
];
const refundStatusTabs = [
    { value: "", label: t('全部'), count: 0, isShow: true },
    { value: "0", label: t('待审核'), count: 0, isShow: true },
    { value: "1", label: t('已退还'), count: 0, isShow: true },
    { value: "2", label: t('审核驳回'), count: 0, isShow: true }
];

const toImgList = (voucher?: string) => (voucher ? voucher.split(",").filter(Boolean) : []);

const formatPayeeAccount = (row: RechargeAuditDetail) => {
    if (row.payeeAccountName) return row.payeeAccountNo || "--";
    if (row.payeeBankAccount) return row.payeeBankAccount;
    return "--";
};

const statusTabs = computed(() => (mainTab.value === "recharge" ? rechargeStatusTabs : refundStatusTabs));

// 充值/退还共用一套筛选参数，按当前主 tab 转发到对应接口
const apiFunction = computed(
    () => (params: PaymentAuditFilterParams) => (mainTab.value === "recharge" ? getRechargeAuditList(params) : getRefundAuditList(params))
);

const {
    listData: filterState,
    loading,
    total,
    filterParams,
    loadData: loadFilter,
    onSearchSubmit,
    resetParams
} = useListRequest<PaymentAuditFilterState, PaymentAuditFilterParams>(
    {
        apiFunction,
        defaultParams: {
            page: 1,
            size: config.get("pageSize"),
            ownerType: "",
            ownerName: "",
            subjectType: "",
            payeeAccountType: "",
            type: "",
            status: "",
            startDate: "",
            endDate: ""
        }
    },
    ["status"]
);

const onMainTabChange = () => {
    filterParams.page = 1;
    // 切换主 tab 时重置下方所有筛选项
    filterParams.status = "";
    filterParams.ownerType = "";
    filterParams.ownerName = "";
    filterParams.subjectType = "";
    filterParams.payeeAccountType = "";
    filterParams.type = "";
    filterParams.startDate = "";
    filterParams.endDate = "";
    loadFilter();
};

const onStatusTabChange = (value: string) => {
    filterParams.page = 1;
    filterParams.status = value;
    loadFilter();
};

onMounted(() => {
    loadFilter();
});
</script>

<style lang="less" scoped>
.main-tabs {
    margin-bottom: 8px;
}
</style>
