<!--
  @file 款项审核详情（店铺端/供应商端）
  @description 充值/退还合并详情只读展示，owner 由登录态自动锁定，调用对应端 paymentAuditDetail 接口
-->
<template>
    <a-spin class="payment-audit-detail" :spinning="loading">
        <div class="notice-warp">
            <p class="notice-title">{{ detail.auditTypeText || $t('款项信息') }}</p>
            <hr style="border: 0.5px solid #dddddd" />
            <p>
                <span class="label">{{ $t('审核类型：') }}</span><span class="value">{{ detail.auditTypeText || "--" }}</span>
            </p>
            <p>
                <span class="label">{{ $t('交易金额：') }}</span><span class="value">{{ priceFormat(detail.amount) }}</span>
            </p>
            <p v-if="detail.auditType === 2">
                <span class="label">{{ $t('退还类型：') }}</span><span class="value">{{ detail.refundTypeText || "--" }}</span>
            </p>
            <p>
                <span class="label">{{ detail.auditType === 2 ? $t('退还原因') : $t('申请备注') }}：</span><span class="value">{{ detail.remark || "--" }}</span>
            </p>
            <p>
                <span class="label">{{ $t('关联凭证：') }}</span>
                <span v-if="voucherList.length" class="voucher-list">
                    <el-image
                        v-for="(img, index) in voucherList"
                        :key="index"
                        :src="img"
                        :preview-src-list="voucherList"
                        :initial-index="index"
                        :preview-teleported="true"
                        fit="cover"
                        style="width: 80px; height: 80px; margin-right: 8px"
                    />
                </span>
                <span v-else class="value">--</span>
            </p>
            <p>
                <span class="label">{{ $t('提交时间：') }}</span><span class="value">{{ detail.addTimeText || "--" }}</span>
            </p>
            <p>
                <span class="label">{{ $t('审核状态：') }}</span><span class="value">{{ detail.statusText || statusText }}</span>
            </p>
            <template v-if="detail.status !== 0">
                <p>
                    <span class="label">{{ $t('审核时间：') }}</span><span class="value">{{ detail.auditTimeText || "--" }}</span>
                </p>
                <p>
                    <span class="label">{{ $t('审核备注：') }}</span><span class="value">{{ detail.auditRemark || "--" }}</span>
                </p>
            </template>
        </div>
    </a-spin>
</template>

<script lang="ts" setup>
import { computed, onMounted, ref } from "vue";
import { message } from "ant-design-vue";
import { priceFormat } from "@/utils/format";
import { getShopDepositAuditDetail } from "@/api/merchant/capitalfund/deposit";
import type { ShopDepositAuditState } from "@/types/merchant/capitalfund/deposit.d";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const props = defineProps({
    auditType: { type: Number, default: 1 },
    bizId: { type: Number, default: 0 },
    isDialog: Boolean
});

const loading = ref<boolean>(true);
const detail = ref<ShopDepositAuditState>({ auditType: props.auditType, bizId: props.bizId, amount: 0, status: 0 });

const toImgList = (voucher?: string) => (voucher ? voucher.split(",").filter(Boolean) : []);
const voucherList = computed(() => toImgList(detail.value.payVoucher));

const statusText = computed(() => {
    if (detail.value.status === 0) return t('待审核');
    if (detail.value.status === 1) return t('已通过');
    if (detail.value.status === 2) return t('未通过');
    return "--";
});

const loadDetail = async () => {
    try {
        const result = await getShopDepositAuditDetail({ auditType: props.auditType, bizId: props.bizId });
        detail.value = result;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};

onMounted(() => {
    loadDetail();
});
</script>

<style lang="less" scoped>
.payment-audit-detail {
    padding: 0 6px;
}
.notice-warp {
    font-weight: 500;
    color: black;
    background-color: #eef2ff;
    border-radius: 9px;
    padding: 15px;
    margin-bottom: 20px;
    line-height: 24px;
    p {
        display: flex;
        align-items: flex-start;
    }
    .notice-title {
        display: block;
    }
    .label {
        flex-shrink: 0;
        white-space: nowrap;
    }
    .value {
        flex: 1;
        min-width: 0;
        word-break: break-all;
    }
}
.voucher-list {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-wrap: wrap;
    row-gap: 8px;
}
</style>
