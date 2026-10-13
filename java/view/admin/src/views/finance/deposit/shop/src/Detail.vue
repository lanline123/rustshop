/** * @file 店铺保证金详情抽屉 * @description 顶部总览 + 3 个 tab：基本信息 / 缴纳记录 / 调整记录 */
<template>
    <a-spin class="deposit-detail" :spinning="loading">
        <DepositOverviewBar :detail="detail" />

        <el-tabs v-model="activeTab">
            <el-tab-pane :label="$t('基本信息')" name="basic">
                <div class="basic-info-list">
                    <div class="info-item">
                        <div class="label">{{ $t('{shopLabel}类型：', { shopLabel }) }}</div>
                        <div class="value">{{ detail.subjectTypeText || "--" }}</div>
                    </div>
                    <div class="info-item">
                        <div class="label">{{ $t('经营类目：') }}</div>
                        <div class="value">{{ detail.businessCategory || "--" }}</div>
                    </div>
                    <div class="info-item">
                        <div class="label">{{ $t('开店时间：') }}</div>
                        <div class="value">{{ detail.openTime || "--" }}</div>
                    </div>
                    <div class="info-item">
                        <div class="label">{{ $t('所属商户：') }}</div>
                        <div class="value">{{ detail.merchantName || "--" }}</div>
                    </div>
                    <div class="info-item">
                        <div class="label">{{ $t('{shopLabel}状态：', { shopLabel }) }}</div>
                        <div class="value">{{ ownerStatusText }}</div>
                    </div>
                    <div class="info-item">
                        <div class="label">{{ $t('保证金状态：') }}</div>
                        <div class="value">{{ detail.depositStatusText || "--" }}</div>
                    </div>
                    <div class="info-item">
                        <div class="label">{{ $t('应缴金额：') }}</div>
                        <div class="value">{{ priceFormat(detail.required) }}</div>
                    </div>
                    <div class="info-item">
                        <div class="label">{{ $t('已缴金额：') }}</div>
                        <div class="value">{{ priceFormat(detail.paid) }}</div>
                    </div>
                    <div class="info-item">
                        <div class="label">{{ $t('待缴金额：') }}</div>
                        <div class="value">{{ priceFormat(detail.pending) }}</div>
                    </div>
                </div>
            </el-tab-pane>

            <el-tab-pane :label="$t('缴纳记录')" name="payment">
                <a-spin :spinning="paymentLoading">
                <el-table :data="paymentList">
                    <el-table-column :label="$t('交易类型')" prop="typeText" min-width="100" />
                    <el-table-column :label="$t('交易金额')" min-width="120">
                        <template #default="{ row }">{{ priceFormat(row.changeAmount) }}</template>
                    </el-table-column>
                    <el-table-column :label="$t('支付方式')" min-width="100">
                        <template #default="{ row }">{{ row.payeeAccountTypeText || "--" }}</template>
                    </el-table-column>
                    <el-table-column :label="$t('交易备注')" min-width="140" show-overflow-tooltip>
                        <template #default="{ row }">{{ remarkText(row) || "--" }}</template>
                    </el-table-column>
                    <el-table-column :label="$t('交易时间')" prop="addTime" min-width="160" />
                    <el-table-column :label="$t('交易凭证')" min-width="120">
                        <template #default="{ row }">
                            <el-image
                                v-if="row.payVoucher"
                                :src="row.payVoucher"
                                :preview-src-list="[row.payVoucher]"
                                :preview-teleported="true"
                                fit="cover"
                                style="width: 50px; height: 50px"
                            />
                            <span v-else>--</span>
                        </template>
                    </el-table-column>
                    <template #empty>
                        <div class="empty-warp"><div v-if="!paymentLoading" class="empty-bg">{{ $t('暂无数据') }}</div></div>
                    </template>
                </el-table>
                </a-spin>
                <div v-if="paymentTotal > 0" class="pagination-con">
                    <Pagination v-model:page="paymentParams.page" v-model:size="paymentParams.size" :total="paymentTotal" @callback="loadPaymentLog" />
                </div>
            </el-tab-pane>

            <el-tab-pane :label="$t('调整记录')" name="adjust">
                <AdjustLogTimeline :owner-type="ownerType" :owner-id="ownerId" />
            </el-tab-pane>
        </el-tabs>
    </a-spin>
</template>

<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from "vue";
import { message } from "ant-design-vue";
import { useRouter } from "vue-router";
import { Pagination } from "@/components/list";
import { useConfigStore } from "@/store/config";
import { priceFormat } from "@/utils/format";
import { getDepositPaymentLog, getDepositQueryDetail } from "@/api/finance/deposit";
import { OrgScope, DepositTxType } from "@/types/finance/deposit.d";
import type { DepositQueryDetail, PaymentLogState } from "@/types/finance/deposit.d";
import AdjustLogTimeline from "./AdjustLogTimeline.vue";
import DepositOverviewBar from "./DepositOverviewBar.vue";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const props = defineProps({
    ownerType: { type: Number, default: undefined },
    ownerId: { type: Number, default: undefined },
    shopId: { type: Number, default: 0 },
    isDialog: Boolean
});

const config: any = useConfigStore();
const query = useRouter().currentRoute.value.query;
const shopId = ref<number>(props.isDialog ? props.shopId : Number(query.shopId));
const ownerType = ref<number | undefined>(props.isDialog ? props.ownerType : query.ownerType ? Number(query.ownerType) : undefined);
const ownerId = ref<number | undefined>(props.isDialog ? props.ownerId : query.ownerId ? Number(query.ownerId) : undefined);
const activeTab = ref<string>("basic");
const loading = ref<boolean>(true);
const detail = ref<DepositQueryDetail>({ shopId: shopId.value });

const shopLabel = computed(() => {
    if (detail.value.ownerTypeText) {
        return detail.value.ownerTypeText;
    }
    const type = detail.value.ownerType ?? ownerType.value;
    if (type === OrgScope.Supplier) {
        return t('供应商');
    }
    return t('店铺');
});

const ownerStatusText = computed(() => {
    const type = detail.value.ownerType ?? ownerType.value;
    if (type === OrgScope.Supplier) {
        return detail.value.vendorStatusText || "--";
    }
    return detail.value.shopStatusText || "--";
});

const paymentList = ref<PaymentLogState[]>([]);
const paymentTotal = ref<number>(0);
const paymentLoading = ref<boolean>(false);
const paymentParams = reactive({ page: 1, size: config.get("pageSize") });

// 充值/退还展示关联单备注，其余展示流水备注
const remarkText = (row: PaymentLogState) =>
    row.type === DepositTxType.Recharge || row.type === DepositTxType.Refund ? row.relatedRemark : row.remark;

const loadDetail = async () => {
    try {
        const result = await getDepositQueryDetail({ ownerType: ownerType.value, ownerId: ownerId.value, shopId: shopId.value });
        detail.value = result;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};

const loadPaymentLog = async () => {
    paymentLoading.value = true;
    try {
        const result = await getDepositPaymentLog({
            ownerType: ownerType.value,
            ownerId: ownerId.value,
            shopId: shopId.value,
            page: paymentParams.page,
            size: paymentParams.size
        });
        paymentList.value = result.records;
        paymentTotal.value = result.total;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        paymentLoading.value = false;
    }
};

onMounted(() => {
    loadDetail();
    loadPaymentLog();
});
</script>

<style lang="less" scoped>
.basic-info-list {
    padding: 16px 0;
    .info-item {
        display: flex;
        align-items: flex-start;
        margin-bottom: 16px;
        font-size: 14px;
        line-height: 1.5;
        gap: 10px;
        .label {
            width: 90px;
            font-weight: 500;
            text-align: right;
            flex-shrink: 0;
        }
        .value {
            flex: 1;
            word-break: break-all;
        }
    }
}
</style>
