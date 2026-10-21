/** * @file 退还审核详情/审核弹窗 * @description 一步式：通过须上传打款凭证（否则确认置灰），驳回须备注；退款金额只读展示 */
<template>
    <a-spin class="refund-audit" :spinning="loading">
        <div class="notice-warp">
            <p class="notice-title">{{ $t('退还申请信息') }}</p>
            <hr style="border: 0.5px solid #dddddd" />
            <p>
                <span class="label">{{ $t('组织名称：') }}</span><span class="value">{{ detail.shopName || detail.ownerName || "--" }}</span>
            </p>
            <p>
                <span class="label">{{ $t('主体类型：') }}</span><span class="value">{{ detail.subjectTypeText || "--" }}</span>
            </p>
            <p>
                <span class="label">{{ $t('退还类型：') }}</span><span class="value">{{ detail.typeText || "--" }}</span>
            </p>
            <p>
                <span class="label">{{ $t('申请金额：') }}</span><span class="value">{{ priceFormat(detail.amount) }}</span>
            </p>
            <p>
                <span class="label">{{ $t('申请时间：') }}</span><span class="value">{{ detail.addTimeText || "--" }}</span>
            </p>
            <p>
                <span class="label">{{ $t('退还说明：') }}</span><span class="value">{{ detail.reason || "--" }}</span>
            </p>
            <p>
                <span class="label">{{ $t('审核状态：') }}</span><span class="value">{{ detail.statusText || "--" }}</span>
            </p>
            <p v-if="detail.status !== 0">
                <span class="label">{{ $t('审核备注：') }}</span><span class="value">{{ detail.auditRemark || "--" }}</span>
            </p>
            <p v-if="voucherList.length">
                <span class="label">{{ $t('打款凭证：') }}</span>
                <span class="voucher-list">
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
            </p>
        </div>

        <el-form v-if="isAudit" ref="formRef" :model="formState" label-width="110px" class="audit-form">
            <el-form-item :label="$t('审核结果')" prop="result" :rules="[{ required: true, message: '请选择审核结果' }]">
                <el-radio-group v-model="formState.result">
                    <el-radio :value="1">{{ $t('通过') }}</el-radio>
                    <el-radio :value="2">{{ $t('驳回') }}</el-radio>
                </el-radio-group>
            </el-form-item>

            <template v-if="formState.result === 1">
                <el-form-item :label="$t('退款金额')">
                    <span>{{ priceFormat(detail.amount) }}</span>
                </el-form-item>
                <el-form-item :label="$t('上传附件')" required>
                    <div>
                        <FormAddGallery isMultiple v-model:photos="payVoucherPhotos" />
                        <div class="extra">
                            <p>{{ $t('1、凭证为保证金转账、银行汇款、线上付款截图，需清晰展示付款金额、付款时间、收款账户、付款方信息；') }}</p>
                            <p>{{ $t('2、支持 JPG、PNG、PDF 格式，单张图片大小不超过 {imageUploadMaxSize}MB，可上传多张附件；', { imageUploadMaxSize: configStore.imageUploadMaxSize }) }}</p>
                            <p>{{ $t('3、凭证信息需和实际退款金额一致，信息模糊、篡改的凭证不予审核通过。') }}</p>
                        </div>
                    </div>
                </el-form-item>
            </template>

            <el-form-item :label="$t('审核备注')" prop="remark" :rules="formState.result === 2 ? [{ required: true, message: '驳回时必须填写审核备注' }] : []">
                <el-input v-model="formState.remark" type="textarea" :rows="3" :maxlength="200" show-word-limit :placeholder="$t('请输入审核备注')" />
            </el-form-item>
        </el-form>
    </a-spin>
</template>

<script lang="ts" setup>
import { computed, onMounted, ref, shallowRef, watch } from "vue";
import { message } from "ant-design-vue";
import { FormAddGallery } from "@/components/gallery";
import { priceFormat } from "@/utils/format";
import { auditRefund, getRefundAuditDetail } from "@/api/finance/deposit";
import type { RefundAuditDetail } from "@/types/finance/deposit.d";
import { useConfigStore } from "@/store/config";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const configStore = useConfigStore();
const props = defineProps({
    id: { type: Number, default: 0 },
    mode: { type: String, default: "view" },
    isDialog: Boolean
});

const emit = defineEmits(["submitCallback", "update:confirmLoading", "close", "okType"]);

const loading = ref<boolean>(true);
const formRef = shallowRef();
const detail = ref<RefundAuditDetail>({ shopDepositRefundId: props.id, amount: 0, type: 1, status: 0 });
const isAudit = computed(() => props.mode === "audit");
const toImgList = (voucher?: string) => (voucher ? voucher.split(",").filter(Boolean) : []);
const voucherList = computed(() => toImgList(detail.value.payVoucher));

const payVoucherPhotos = ref<any[]>([]);

const formState = ref({
    result: 1,
    remark: ""
});

const canConfirm = computed(() => {
    if (!isAudit.value) return true;
    if (formState.value.result === 2) return true;
    return payVoucherPhotos.value.length > 0;
});

watch(
    canConfirm,
    (val) => {
        emit("okType", val);
    },
    { immediate: true }
);

const loadDetail = async () => {
    try {
        const result = await getRefundAuditDetail({ id: props.id });
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
    const payVoucher = payVoucherPhotos.value.map((item: any) => item.picUrl).join(",");
    const pass = formState.value.result === 1;
    if (pass && !payVoucher) {
        message.error(t('请上传打款凭证'));
        return;
    }
    try {
        emit("update:confirmLoading", true);
        const result = await auditRefund({
            id: props.id,
            pass,
            payVoucher: pass ? payVoucher : undefined,
            auditRemark: formState.value.remark
        });
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
.refund-audit {
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
.audit-form {
    margin-top: 20px;
}
.voucher-list {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-wrap: wrap;
    row-gap: 8px;
}
</style>
