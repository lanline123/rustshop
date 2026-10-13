/** * @file 充值审核详情/审核弹窗 * @description 只读详情 + 去审核（通过/驳回，驳回备注必填） */
<template>
    <a-spin class="recharge-audit" :spinning="loading">
        <div class="notice-warp">
            <p class="notice-title">{{ $t('充值信息') }}</p>
            <hr style="border: 0.5px solid #dddddd" />
            <p>
                <span class="label">{{ $t('{shopLabel}名称：', { shopLabel }) }}</span><span class="value">{{ detail.shopName || detail.ownerName || "--" }}</span>
            </p>
            <p>
                <span class="label">{{ $t('主体类型：') }}</span><span class="value">{{ detail.subjectTypeText || "--" }}</span>
            </p>
            <p>
                <span class="label">{{ $t('交易金额：') }}</span><span class="value">{{ priceFormat(detail.amount) }}</span>
            </p>
            <p>
                <span class="label">{{ $t('收款账户：') }}</span><span class="value">{{ formatPayeeAccount(detail) }}</span>
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
                <span class="label">{{ $t('交易备注：') }}</span><span class="value">{{ detail.remark || "--" }}</span>
            </p>
            <p>
                <span class="label">{{ $t('提交时间：') }}</span><span class="value">{{ detail.addTimeText || "--" }}</span>
            </p>
            <p>
                <span class="label">{{ $t('审核状态：') }}</span><span class="value">{{ statusText }}</span>
            </p>
            <p v-if="detail.status !== 0">
                <span class="label">{{ $t('审核备注：') }}</span><span class="value">{{ detail.auditRemark || "--" }}</span>
            </p>
        </div>

        <el-form v-if="isAudit" ref="formRef" :model="formState" label-width="90px" class="audit-form">
            <el-form-item :label="$t('审核结果')" prop="result" :rules="[{ required: true, message: '请选择审核结果' }]">
                <el-radio-group v-model="formState.result">
                    <el-radio :value="1">{{ $t('通过') }}</el-radio>
                    <el-radio :value="2">{{ $t('驳回') }}</el-radio>
                </el-radio-group>
            </el-form-item>
            <el-form-item :label="$t('审核备注')" prop="remark" :rules="formState.result === 2 ? [{ required: true, message: '请输入审核备注' }] : []">
                <el-input v-model="formState.remark" type="textarea" :rows="3" :maxlength="200" show-word-limit :placeholder="$t('请输入审核备注')" />
            </el-form-item>
        </el-form>
    </a-spin>
</template>

<script lang="ts" setup>
import { computed, onMounted, ref, shallowRef } from "vue";
import { message } from "ant-design-vue";
import { priceFormat } from "@/utils/format";
import { auditRecharge, getRechargeAuditDetail } from "@/api/finance/deposit";
import type { RechargeAuditDetail } from "@/types/finance/deposit.d";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const toImgList = (voucher?: string) => (voucher ? voucher.split(",").filter(Boolean) : []);

const formatPayeeAccount = (row: RechargeAuditDetail) => {
    if (row.payeeAccountName) return row.payeeAccountNo || "--";
    if (row.payeeBankAccount) return row.payeeBankAccount;
    return "--";
};

const props = defineProps({
    id: { type: Number, default: 0 },
    mode: { type: String, default: "view" },
    isDialog: Boolean
});

const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);

const loading = ref<boolean>(true);
const formRef = shallowRef();
const detail = ref<RechargeAuditDetail>({ shopDepositRechargeId: props.id, amount: 0, status: 0 });
const shopLabel = computed(() => detail.value.ownerTypeText || (detail.value.ownerType === 2 ? t('供应商') : t('店铺')));
const isAudit = computed(() => props.mode === "audit");
const voucherList = computed(() => toImgList(detail.value.payVoucher));

const statusText = computed(() => {
    if (detail.value.status === 0) return t('待审核');
    if (detail.value.status === 1) return t('已通过');
    if (detail.value.status === 2) return t('未通过');
    return detail.value.statusText || "--";
});

const formState = ref({
    result: 1,
    remark: ""
});

const loadDetail = async () => {
    try {
        const result = await getRechargeAuditDetail({ id: props.id });
        detail.value = result;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};

const onSubmit = async () => {
    if (!isAudit.value) return;
    try {
        await formRef.value.validate();
    } catch {
        return;
    }
    try {
        emit("update:confirmLoading", true);
        const result = await auditRecharge({ id: props.id, pass: formState.value.result === 1, auditRemark: formState.value.remark });
        message.success(t('操作成功'));
        emit("submitCallback", result);
    } catch (error: any) {
        message.error(error.message);
    } finally {
        emit("update:confirmLoading", false);
    }
};

const onFormSubmit = () => {
    onSubmit();
};

defineExpose({ onFormSubmit });

onMounted(() => {
    loadDetail();
});
</script>

<style lang="less" scoped>
.recharge-audit {
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
.audit-form {
    margin-top: 20px;
}
</style>
