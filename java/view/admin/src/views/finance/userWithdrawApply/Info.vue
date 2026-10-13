/**
 * @file 会员提现申请详情/审核页
 * @description 查看提现申请详情或进行审核处理
 * @features
 * - 添加模式：选择会员/填写金额/选择收款方式
 * - 详情模式：显示申请信息/收款账户
 * - 审核通过/拒绝/确认打款操作
 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form v-if="!loading" ref="formRef" :model="formState" label-width="auto">
                    <template v-if="action === 'add'">
                        <el-form-item :rules="[{ required: true, message: '会员名称不能为空!' }]" :label="$t('会员名称')" prop="userId">
                            <SelectUser :multiple="false" v-model:userId="formState.userId"></SelectUser>
                        </el-form-item>
                        <el-form-item :label="$t('金额')" prop="amount">
                            <TigInput classType="tig-form-input" type="decimal" v-model="formState.amount" />
                        </el-form-item>
                        <el-form-item :label="$t('管理员备注')" prop="postscript">
                            <TigInput classType="tig-form-input" v-model="formState.postscript" :row="2" type="textarea" />
                        </el-form-item>
                        <el-form-item :label="$t('到款状态')" prop="status">
                            <el-radio-group v-model="formState.status">
                                <el-radio :value="0">{{ $t('待处理') }}</el-radio>
                                <el-radio :value="1" v-if="action != 'add'">{{ $t('已完成') }}</el-radio>
                                <el-radio :value="2" v-if="action != 'add'">{{ $t('拒绝申请') }}</el-radio>
                            </el-radio-group>
                        </el-form-item>
                        <el-form-item :label="$t('支付方式')" prop="accountType">
                            <el-select v-model="formState.accountData.accountType" style="width: 100%">
                                <el-option :key="1" :label="$t('银行卡')" :value="1" />
                                <el-option :key="2" :label="$t('支付宝')" :value="2" />
                                <el-option :key="3" :label="$t('微信')" :value="3" />
                            </el-select>
                        </el-form-item>
                        <el-form-item :label="$t('姓名')" prop="accountName">
                            <TigInput classType="tig-form-input" v-model="formState.accountData.accountName" />
                        </el-form-item>
                        <el-form-item :rules="[{ required: true, message: '银行卡号或账号不能为空!' }]" :label="$t('银行卡号或账号')" prop="accountData.accountNo">
                            <TigInput classType="tig-form-input" v-model="formState.accountData.accountNo" />
                        </el-form-item>
                        <el-form-item :label="$t('身份证号')" prop="identity">
                            <TigInput classType="tig-form-input" v-model="formState.accountData.identity" />
                        </el-form-item>
                        <el-form-item :label="$t('银行名称')" prop="bankName">
                            <TigInput classType="tig-form-input" v-model="formState.accountData.bankName" />
                        </el-form-item>
                        <el-form-item v-show="!props.isDialog" :wrapper-col="{ offset: 4, span: 16 }">
                            <el-button ref="submitBtn" class="form-submit-btn" type="primary" @click="onSubmit">{{ $t('提交') }}</el-button>
                        </el-form-item>
                    </template>
                    <template v-else>
                        <div class="notice-warp">
                            <p>{{ $t('会员金额信息') }}</p>
                            <hr style="border: 0.5px solid #dddddd" />
                            <p>{{ $t('会员名称：{username}', { username: formState.username }) }}</p>
                            <p>{{ $t('提现方式：{accountType2}', { accountType2: formState.accountData.accountType === 1 ? "银行卡" : formState.accountData.accountType === 2 ? "支付宝" : "微信" }) }}
                            </p>
                            <p>{{ $t('提现姓名：{accountName}', { accountName: formState.accountData.accountName }) }}</p>
                            <p>{{ $t('提现账号：{accountNo}', { accountNo: formState.accountData.accountNo }) }}</p>
                            <p v-if="formState.accountData.accountType === 1">{{ $t('开户行：{bankName}', { bankName: formState.accountData.bankName }) }}</p>
                            <p>{{ $t('金额：{priceFormat}', { priceFormat: priceFormat(formState.amount) }) }}</p>

                            <p>{{ $t('手续费：{priceFormat}', { priceFormat: priceFormat(formState.handlingFee) }) }}</p>
                            <p>{{ $t('手续费率：{handlingRate0}', { handlingRate0: formState.handlingRate ? formState.handlingRate : 0 }) }}<span>%</span></p>

                            <p>{{ $t('申请日期：{addTime}', { addTime: formState.addTime }) }}</p>
                        </div>
                        <el-form-item :rules="[{ required: true, message: '管理员备注不能为空!' }]" :label="$t('管理员备注')" prop="postscript">
                            <TigInput classType="tig-form-input" v-model="formState.postscript" :row="2" type="textarea" />
                        </el-form-item>
                        <el-form-item :label="$t('到款状态')" prop="status">
                            <el-radio-group v-model="formState.status">
                                <el-radio :value="1">{{ $t('已完成') }}</el-radio>
                                <el-radio :value="2">{{ $t('拒绝申请') }}</el-radio>
                            </el-radio-group>
                        </el-form-item>
                    </template>
                </el-form>
                <a-spin :spinning="loading" style="width: 100%; margin-top: 100px" />
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { onMounted, ref, shallowRef } from "vue";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
import type { UserWithdrawApplyFormState } from "@/types/finance/userWithdrawApply.d";
import { getUserWithdrawApply, updateUserWithdrawApply } from "@/api/finance/userWithdrawApply";
import { SelectUser } from "@/components/select";
import { priceFormat } from "@/utils/format";
// 父组件回调
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
const loading = ref<boolean>(true);
const query = useRouter().currentRoute.value.query;
const action = ref<string>(props.isDialog ? props.act : String(query.act));
const id = ref<number>(props.isDialog ? props.id : Number(query.id));
const operation = action.value === "add" ? "create" : "update";
const formRef = shallowRef();
const formState = ref<UserWithdrawApplyFormState>({
    accountData: {},
    amount: 0,
    status: 0
});
const fetchUserWithdrawApply = async () => {
    try {
        const result = await getUserWithdrawApply(action.value, { id: id.value });
        Object.assign(formState.value, result);
    } catch (error: any) {
        message.error(error.message);
        emit("close");
    } finally {
        loading.value = false;
    }
};

onMounted(() => {
    if (action.value === "detail") {
        // 获取详情数据
        fetchUserWithdrawApply();
    } else {
        loading.value = false;
    }
});

// 表单通过验证后提交
const onSubmit = async () => {
    await formRef.value.validate();
    try {
        emit("update:confirmLoading", true);
        const result = await updateUserWithdrawApply(operation, { id: id.value, ...formState.value });
        emit("submitCallback", result);
        message.success(t('操作成功'));
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
<style scoped lang="less">
.notice-warp {
    font-weight: 500;
    color: black;
    background-color: #eef2ff;
    border-radius: 9px;
    padding: 15px;
    margin-bottom: 20px;
    line-height: 24px;
}
</style>
