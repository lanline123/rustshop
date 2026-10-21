/** * @file 保证金账户 添加/编辑表单（平台收款账户） * @description 独立实现，照搬 merchant/capitalfund/account/Info，接口走 deposit/payeeAccount/* */
<template>
    <el-form v-if="!loading" ref="formRef" :model="formState" class="form-body" label-suffix="：" label-width="auto">
        <el-form-item v-show="page" :rules="[{ required: true, message: '请选择收款方式!' }]" :label="$t('收款方式')">
            <div>
                <el-radio-group v-model="formState.accountType" @change="onChange">
                    <el-radio-button v-for="item in accountTypeList" :value="item.accountType">{{ item.accountTypeText }}</el-radio-button>
                </el-radio-group>
                <div class="extra">{{ $t('用于平台收取{isStore}/供应商保证金的收款账户。', { isStore: isStore() ? "门店" : "店铺" }) }}</div>
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
import { ref, computed, onMounted } from "vue";
import { message } from "ant-design-vue";
import { useRouter } from "vue-router";
import type { AccountFormState, AccountTypeList } from "@/types/finance/deposit.d";
import { updateDepositAccount } from "@/api/finance/deposit";
import { isStore } from "@/utils/version";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);
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
    row: {
        type: Object,
        default: () => ({})
    },
    isDialog: Boolean
});
const loading = ref<boolean>(true);
const query = useRouter().currentRoute.value.query;
const action = ref<string>(props.isDialog ? props.act : String(query.act));
const id = ref<number>(props.isDialog ? props.row?.id ?? props.id : Number(query.id));
const operation = action.value === "add" ? "create" : "update";
const formRef = ref();
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
const accountNoRules = computed(() => {
    return [
        { required: true, message: t('请输入') + accountPlaceholder.value + t('号') + "!", trigger: "blur" },
        {
            validator: (rule: any, value: any, callback: any) => {
                const regExp = /^\d{6,}$/;
                if (!regExp.test(value) && formState.value.accountType === 1) {
                    callback(new Error(t('请输入正确的银行卡号')));
                } else {
                    callback();
                }
            },
            trigger: "blur"
        }
    ];
});

const onSubmit = async () => {
    await formRef.value.validate();
    try {
        emit("update:confirmLoading", true);
        const { status, sortOrder, ...payload } = formState.value;
        const result = await updateDepositAccount(operation, { id: id.value, ...payload });
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
const onFormSubmit = () => {
    onSubmit();
};
const fetchInfo = () => {
    Object.assign(formState.value, props.row);
    loading.value = false;
};
const accountTypeList = ref<AccountTypeList[]>([
    { accountType: 1, accountTypeText: t('银行卡') },
    { accountType: 2, accountTypeText: t('支付宝') },
    { accountType: 3, accountTypeText: t('微信') }
]);
onMounted(() => {
    if (action.value === "detail") {
        fetchInfo();
    } else {
        loading.value = false;
    }
});
defineExpose({ onFormSubmit });
</script>
<style lang="less" scoped>
.extra {
    color: #999;
    font-size: 12px;
    margin-top: 4px;
}
</style>
