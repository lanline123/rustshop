/** * @file 保证金交易明细详情 * @description 只读详情 + 凭证大图预览 */
<template>
    <a-spin class="transaction-detail" :spinning="loading">
        <div class="notice-warp">
            <p class="notice-title">{{ $t('交易信息') }}</p>
            <hr style="border: 0.5px solid #dddddd" />
            <p>
                <span class="label">{{ $t('组织名称：') }}</span><span class="value">{{ detail.shopTitle || detail.ownerName || "--" }}</span>
            </p>
            <p>
                <span class="label">{{ $t('主体类型：') }}</span><span class="value">{{ detail.subjectTypeText || "--" }}</span>
            </p>
            <p>
                <span class="label">{{ $t('交易类型：') }}</span><span class="value">{{ detail.typeText || "--" }}</span>
            </p>
            <p>
                <span class="label">{{ $t('交易金额：') }}</span><span class="value">{{ priceFormat(detail.changeAmount) }}</span>
            </p>
            <p>
                <span class="label">{{ $t('支付方式：') }}</span><span class="value">{{ detail.payTypeText || detail.payeeAccountTypeText || "--" }}</span>
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
                <span class="label">{{ $t('交易备注：') }}</span><span class="value">{{ remarkText || "--" }}</span>
            </p>
            <p>
                <span class="label">{{ $t('交易时间：') }}</span><span class="value">{{ detail.addTime || "--" }}</span>
            </p>
        </div>
    </a-spin>
</template>

<script lang="ts" setup>
import { computed, onMounted, ref } from "vue";
import { message } from "ant-design-vue";
import { priceFormat } from "@/utils/format";
import { getDepositTransaction } from "@/api/finance/deposit";
import { DepositTxType } from "@/types/finance/deposit.d";
import type { DepositTransactionDetail } from "@/types/finance/deposit.d";

const props = defineProps({
    id: { type: Number, default: 0 },
    isDialog: Boolean
});

const toImgList = (voucher?: string) => (voucher ? voucher.split(",").filter(Boolean) : []);

const loading = ref<boolean>(true);
const detail = ref<DepositTransactionDetail>({ shopDepositLogId: props.id, type: 1 });
const voucherList = computed(() => toImgList(detail.value.payVoucher));
const remarkText = computed(() =>
    detail.value.type === DepositTxType.Recharge || detail.value.type === DepositTxType.Refund ? detail.value.relatedRemark : detail.value.remark
);

const loadDetail = async () => {
    try {
        const result = await getDepositTransaction({ id: props.id });
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
.transaction-detail {
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
