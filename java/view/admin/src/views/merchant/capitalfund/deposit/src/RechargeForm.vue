/** * @file 店铺端 保证金充值弹窗 * @description 收款账户 + 金额 + 凭证 + 备注；对齐 DepositRechargeApplyDTO { payeeAccountId, amount, payVoucher, remark } */
<template>
    <a-spin :spinning="loading">
        <el-form ref="formRef" :model="formState" label-width="90px">
            <el-form-item :label="$t('充值方式')">
                <el-radio-group v-model="accountType" @change="onAccountTypeChange">
                    <el-radio-button v-for="item in accountTypeList" :key="item.accountType" :value="item.accountType">
                        {{ item.accountTypeText }}
                    </el-radio-button>
                </el-radio-group>
            </el-form-item>
            <el-form-item :label="$t('充值账户')" prop="payeeAccountId" :rules="[{ required: true, message: '请选择账户进行充值' }]">
                <el-select v-model="formState.payeeAccountId" :placeholder="$t('请选择账户进行充值')" style="width: 330px" clearable>
                    <el-option v-for="item in payeeAccounts" :key="item.id" :label="formatAccountLabel(item)" :value="item.id" />
                </el-select>
            </el-form-item>
            <el-form-item :label="$t('充值金额')" prop="amount" :rules="amountRules">
                <TigInput v-model="formState.amount" type="decimal" width="330px" allowEmptyDecimal :decimal-places="2" :placeholder="$t('请输入您的充值金额')" />
            </el-form-item>
            <el-form-item :label="$t('充值凭证')" required>
                <div>
                    <FormAddGallery isMultiple v-model:photos="voucherPhotos" />
                    <div class="extra">
                        <p>{{ $t('1、凭证为保证金转账、银行汇款、线上付款截图，需清晰展示付款金额、付款时间、收款账户、付款方信息；') }}</p>
                        <p>{{ $t('2、支持 JPG、PNG、PDF 格式，单张图片大小不超过 {imageUploadMaxSize}MB，可上传多张附件；', { imageUploadMaxSize: configStore.imageUploadMaxSize }) }}</p>
                        <p>{{ $t('3、凭证信息需和实际充值金额一致，信息模糊、篡改的凭证不予审核通过。') }}</p>
                    </div>
                </div>
            </el-form-item>
            <el-form-item :label="$t('备注')" prop="remark">
                <el-input v-model="formState.remark" type="textarea" :rows="3" :maxlength="200" show-word-limit :placeholder="$t('请输入备注（选填）')" />
            </el-form-item>
        </el-form>
    </a-spin>
</template>

<script lang="ts" setup>
import { onMounted, ref, shallowRef } from "vue";
import { message } from "ant-design-vue";
import { FormAddGallery } from "@/components/gallery";
import { getRechargePayeeAccountOptions, submitShopDepositRecharge } from "@/api/merchant/capitalfund/deposit";
import { PayAccountType, type AccountTypeList } from "@/types/finance/deposit.d";
import type { PayeeAccountOption } from "@/types/merchant/capitalfund/deposit.d";
import { useConfigStore } from "@/store/config";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const configStore = useConfigStore();
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);
defineProps({ isDialog: Boolean });

const formRef = shallowRef();
const loading = ref(false);
const voucherPhotos = ref<any[]>([]);
const payeeAccounts = ref<PayeeAccountOption[]>([]);
const accountType = ref(PayAccountType.Bank);
const accountTypeList = ref<AccountTypeList[]>([
    { accountType: PayAccountType.Bank, accountTypeText: t('银行卡') },
    { accountType: PayAccountType.Alipay, accountTypeText: t('支付宝') },
    { accountType: PayAccountType.Wechat, accountTypeText: t('微信') }
]);

const formState = ref({
    payeeAccountId: undefined as number | undefined,
    amount: undefined as number | undefined,
    remark: ""
});

const formatAccountLabel = (item: PayeeAccountOption) => {
    const typeText = item.accountTypeText || "";
    return `${item.accountNo}(${typeText})`;
};

const amountRules = [
    { required: true, message: t('请输入您的充值金额') },
    {
        validator: (rule: any, value: any, callback: any) => {
            if (!value || Number(value) <= 0) return callback(new Error(t('充值金额必须大于 0')));
            callback();
        }
    }
];

const loadPayeeAccounts = async () => {
    loading.value = true;
    try {
        payeeAccounts.value = await getRechargePayeeAccountOptions({ accountType: accountType.value });
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};

const onAccountTypeChange = () => {
    formState.value.payeeAccountId = undefined;
    loadPayeeAccounts();
};

const onSubmit = async () => {
    try {
        await formRef.value.validate();
    } catch {
        return;
    }
    const voucher = voucherPhotos.value.map((item: any) => item.picUrl);
    if (voucher.length === 0) {
        message.error(t('请上传充值凭证'));
        return;
    }
    try {
        emit("update:confirmLoading", true);
        const result = await submitShopDepositRecharge({
            payeeAccountId: Number(formState.value.payeeAccountId),
            amount: Number(formState.value.amount),
            payVoucher: voucher.join(","),
            remark: formState.value.remark || undefined
        });
        message.success(t('提交成功，等待平台审核'));
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
    loadPayeeAccounts();
});
</script>

<style lang="less" scoped></style>
