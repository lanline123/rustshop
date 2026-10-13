<template>
    <el-form v-if="!loading" ref="formRef" :model="formState" class="form-body" label-suffix="：" label-width="auto">
        <el-form-item v-show="page" :rules="[{ required: true, message: '请选择收款方式!' }]" :label="$t('收款方式')">
            <div>
                <el-radio-group v-model="formState.accountType" @change="onChange">
                    <el-radio-button v-for="item in accountTypeList" :value="item.accountType">{{ item.accountTypeText }}</el-radio-button>
                </el-radio-group>
                <div class="extra">
                    {{ withdrawSettingVO.withdrawalDescription }}
                </div>
            </div>
        </el-form-item>
        <el-form-item :rules="[{ required: true, message: '请输入真实姓名!' }]" :label="$t('真实姓名')" prop="accountName">
            <TigInput classType="tig-form-input" v-model="formState.accountName" :placeholder="$t('请输入真实姓名')" />
        </el-form-item>
        <el-form-item :label="accountPlaceholder + $t('号')" :rules="accountNoRules" prop="accountNo">
            <TigInput classType="tig-form-input" v-model="formState.accountNo" :placeholder="$t('请输入') + accountPlaceholder" />
        </el-form-item>
        <el-form-item v-if="formState.accountType === 1" :label="$t('分行名称')" prop="bankBranch">
            <TigInput classType="tig-form-input" v-model="formState.bankBranch" :placeholder="$t('请输入分行名称')" />
        </el-form-item>
        <el-form-item v-if="formState.accountType === 1" :label="$t('银行卡详情')" prop="bankName">
            <TigInput classType="tig-form-input" v-model="formState.bankName" :rows="4" :placeholder="$t('请输入银行卡详情')" type="textarea" />
        </el-form-item>
    </el-form>
</template>
<script lang="ts" setup>
import { ref, computed, shallowRef, onMounted } from "vue";
import { message } from "ant-design-vue";
import { useRouter } from "vue-router";
import type { AccountFormState, AccountTypeList } from "@/types/merchant/capitalfund/account.d";
import { getAccount, updateAccount, getAccountConfig } from "@/api/merchant/capitalfund/account";
import { useConfigStore } from "@/store/config";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const config: any = useConfigStore();
const withdrawSettingVO = config.get("withdrawSettingVO");
// 父组件回调
const emit = defineEmits([
    "submitCallback", //确认后回调
    "update:confirmLoading", //确认按钮的loading状态
    "close" //关闭弹窗
]);
const props = defineProps({
    act: {
        type: String,
        default: ""
    },
    id: {
        type: Number,
        default: 0
    },
    type: {
        type: Number,
        default: 1
    },
    page: {
        type: Number,
        default: 1
    },
    isDialog: Boolean
});
const loading = ref<boolean>(true);
const query = useRouter().currentRoute.value.query;
const action = ref<string>(props.isDialog ? props.act : String(query.act));
const id = ref<number>(props.isDialog ? props.id : Number(query.id));
const operation = action.value === "add" ? "create" : "update";
const formRef = shallowRef();
const formState = ref<AccountFormState>({
    accountType: 1
});
const accountPlaceholder = computed(() => {
    switch (formState.value.accountType) {
        case 1:
            return t('银行卡');
        case 2:
            return t('支付宝');
        case 3:
            return t('微信');
        default:
            return "";
    }
});
const onChange = async () => {
    await formRef.value.resetFields();
};
const identityRules = computed(() => {
    return [
        { required: true, message: t('请输入身份证号'), trigger: "blur" }, // 必填验证
        {
            validator: (rule: any, value: any, callback: any) => {
                const regExp = /^[1-9]\d{5}[1-9]\d{3}((0\d)|(1[0-2]))(([0|1|2]\d)|3[0-1])\d{3}([0-9]|X)$/;
                if (!regExp.test(value)) {
                    callback(new Error(t('请输入正确的身份证号码')));
                } else {
                    callback();
                }
            },
            trigger: "blur" // 触发方式，可以是 blur、change等
        }
    ];
});
const accountNoRules = computed(() => {
    return [
        { required: true, message: t('请输入') + accountPlaceholder.value + t('号') + "!", trigger: "blur" }, // 必填验证
        {
            validator: (rule: any, value: any, callback: any) => {
                const regExp = /^\d{6,}$/;
                if (!regExp.test(value) && formState.value.accountType === 1) {
                    callback(new Error(t('请输入正确的银行卡号')));
                } else {
                    callback();
                }
            },
            trigger: "blur" // 触发方式，可以是 blur、change等
        }
    ];
});

const onSubmit = async () => {
    await formRef.value.validate();
    try {
        emit("update:confirmLoading", true);
        const result = await updateAccount(operation, { id: id.value, ...formState.value });
        await formRef.value.resetFields();
        message.success(t('操作成功'));
        formState.value.accountType = 1;
        emit("submitCallback", result);
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
const fetchInfo = async () => {
    try {
        loading.value = true;
        const result = await getAccount(action.value, { id: id.value });
        Object.assign(formState.value, result);
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};
const accountTypeList = ref<AccountTypeList[]>([]);
const fetchAccountConfig = async () => {
    try {
        const result = await getAccountConfig();
        accountTypeList.value = result;
    } catch (error: any) {
        message.error(error.message);
    }
};
onMounted(() => {
    if (action.value === "detail") {
        // 获取详情数据
        fetchInfo();
    } else {
        loading.value = false;
    }
    fetchAccountConfig();
});
defineExpose({ onFormSubmit });
</script>
<style lang="less" scoped></style>
