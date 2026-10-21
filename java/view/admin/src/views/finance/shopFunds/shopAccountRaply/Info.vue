/**
 * @file 店铺提现申请详情/审核页
 * @description 查看店铺提现申请详情并进行审核
 * @features
 * - 显示申请时间/金额/状态/收款账户信息
 * - 审核通过/拒绝操作
 * - 填写审核备注和凭证
 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form v-if="!loading" ref="formRef" :model="formState" label-width="auto">
                    <el-form-item prop="addTime" :label="$t('申请时间')">
                        <p>{{ info.addTime }}</p>
                    </el-form-item>
                    <el-form-item prop="amount" :label="$t('申请金额')">
                        <p>{{ priceFormat(info.amount) }}</p>
                    </el-form-item>
                    <el-form-item prop="accountData.accountName" :label="$t('状态')">
                        <template v-if="info.status == 0 || info.status == 4">
                            <StatusDot color="blue" :flicker="true"></StatusDot>
                        </template>
                        <template v-if="info.status == 2">
                            <StatusDot color="red" :flicker="true"></StatusDot>
                        </template>
                        <template v-if="info.status == 3">
                            <StatusDot color="green" :flicker="true"></StatusDot>
                        </template>
                        <span>{{ info.statusText }}</span>
                    </el-form-item>
                    <el-form-item prop="accountData.accountName" :label="$t('收款人')">
                        <p>{{ info.accountData?.accountName }}</p>
                    </el-form-item>
                    <el-form-item prop="accountData.accountNo" :label="$t('收款账号')">
                        <p>{{ info.accountData?.accountNo }}</p>
                    </el-form-item>
                    <el-form-item prop="paymentVoucher" :label="$t('打款凭证')" v-if="info.paymentVoucher && info.paymentVoucher !== null">
                        <Image :src="info.paymentVoucher" fit="contain" style="height: 80px; width: 80px" />
                    </el-form-item>
                    <el-form-item v-if="info.status == 0" prop="status" :label="$t('审核结果')" :rules="[{ required: true, message: '请选择审核结果!' }]">
                        <el-radio-group v-model="formState.status">
                            <el-radio :value="4">{{ $t('审核通过') }}</el-radio>
                            <el-radio :value="2">{{ $t('审核不通过') }}</el-radio>
                        </el-radio-group>
                    </el-form-item>
                    <el-form-item
                        v-if="info.status == 0 && formState.status == 2"
                        prop="auditRemark"
                        :label="$t('拒绝原因')"
                        :rules="[{ required: true, message: '拒绝原因不能为空!' }]"
                    >
                        <TigInput classType="tig-form-input" type="textarea" :row="2" v-model="formState.auditRemark" />
                    </el-form-item>
                    <el-form-item v-if="info.status == 4" prop="paymentVoucher" :label="$t('打款凭证')" :rules="[{ required: true, message: '打款凭证不能为空!' }]">
                        <FormAddGallery v-model:photo="formState.paymentVoucher"></FormAddGallery>
                    </el-form-item>
                    <el-form-item v-show="!props.isDialog" :wrapper-col="{ offset: 4, span: 16 }">
                        <el-button ref="submitBtn" class="form-submit-btn" type="primary" @click="onSubmit">{{ $t('提交') }}</el-button>
                    </el-form-item>
                </el-form>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { FormAddGallery } from "@/components/gallery";
import { Image } from "@/components/image";
import { ref, shallowRef, onMounted } from "vue";
import StatusDot from "@/components/form/src/StatusDot.vue";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
import { priceFormat } from "@/utils/format";
import { WithdrawFormState } from "@/types/merchant/capitalfund/withdraw.d";
import { auditWithdraw, getWithdrawDetail, uploadPayVoucher } from "@/api/merchant/capitalfund/withdraw";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);

const props = defineProps({
    id: {
        type: Number,
        default: 0
    },
    act: {
        type: String,
        default: ""
    },
    isDialog: Boolean
});
const loading = ref<boolean>(false);
const query = useRouter().currentRoute.value.query;
const action = ref<string>(props.isDialog ? props.act : String(query.act));
const id = ref<number>(props.isDialog ? props.id : Number(query.id));
const operation = action.value === "add" ? "create" : "update";
const formRef = shallowRef();
const formState = ref<WithdrawFormState>({
    remark:""
});
const info = ref<any>({});
onMounted(() => {
    // 获取详情数据
    fetchWithdrawDetail();
});
const fetchWithdrawDetail = async () => {
    try {
        const result = await getWithdrawDetail({ id: id.value });
        Object.assign(info.value, result);
    } catch (error: any) {
        message.error(error.message);
        emit("close");
    } finally {
        loading.value = false;
    }
};

// 表单通过验证后提交
const onSubmit = async () => {
    await formRef.value.validate();
    try {
        emit("update:confirmLoading", true);
        if (info.value.status == 0) {
            const result = await auditWithdraw({ id: id.value, ...formState.value });
            message.success(t('操作成功'));
        }
        if (info.value.status == 4) {
            await uploadPayVoucher({ id: id.value, ...formState.value });
            message.success(t('操作成功'));
        }
        emit("submitCallback", true);
    } catch (error: any) {
        message.error(error.message);
    } finally {
        emit("update:confirmLoading", false);
    }
};
// 表单提交
const onFormSubmit = () => {
    onSubmit();
};

defineExpose({ onFormSubmit });
</script>
