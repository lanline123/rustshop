<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-alert type="warning" show-icon :closable="false" style="margin-bottom: 16px">
                    <template #title>
                        <span>{{ $t('提示：充值附赠赠送余额时，强烈建议关闭会员余额提现功能，避免产生资金套利风险。') }}</span>
                        <a class="btn-link" style="margin-left: 8px" @click="goCloseWithdrawal">{{ $t('去关闭') }}</a>
                    </template>
                </el-alert>
                <el-form v-if="!loading" ref="formRef" :model="formState" label-width="auto">
                    <el-form-item prop="money" :label="$t('充值金额')" :rules="[{ required: true, validator: validateMoney, trigger: 'blur' }]">
                        <TigInput classType="tig-form-input" type="decimal" v-model="formState.money" :min="0" />
                    </el-form-item>
                    <el-form-item prop="discountMoney" :label="$t('赠送金额')" :rules="[{ required: true, validator: validateMoney, trigger: 'blur' }]">
                        <TigInput classType="tig-form-input" type="decimal" v-model="formState.discountMoney" :min="0" />
                    </el-form-item>
                    <el-form-item prop="sortOrder" :label="$t('排序')">
                        <TigInput classType="tig-form-input" type="integer" v-model="formState.sortOrder" />
                    </el-form-item>
                    <el-form-item prop="isShow" :label="$t('是否显示')">
                        <el-radio-group v-model="formState.isShow">
                            <el-radio :value="1">{{ $t('是') }}</el-radio>
                            <el-radio :value="0">{{ $t('否') }}</el-radio>
                        </el-radio-group>
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
import { ref, shallowRef, onMounted } from "vue";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
import { FormAddGallery } from "@/components/gallery";
import { RechargeSettingFormState } from "@/types/promotion/rechargeSetting.d";
import { getRechargeSetting, updateRechargeSetting } from "@/api/promotion/rechargeSetting";

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
const router = useRouter();
// 跳转至会员提现设置（关闭会员余额提现，避免赠送余额套利，D1107）
const goCloseWithdrawal = () => {
    router.push({ path: "/finance/user-withdraw-apply/list", query: { tab: "setting" } });
};
const query = useRouter().currentRoute.value.query;
const action = ref<string>(props.isDialog ? props.act : String(query.act));
const id = ref<number>(props.isDialog ? props.id : Number(query.id));
const operation = action.value === "add" ? "create" : "update";
const formRef = shallowRef();
const formState = ref<RechargeSettingFormState>({
    discountMoney: "",
    money: "",
    sortOrder: 1,
    isShow: 1
});

onMounted(() => {
    if (action.value === "detail") {
        // 获取详情数据
        fetchRechargeSetting();
    } else {
        loading.value = false;
    }
});
const fetchRechargeSetting = async () => {
    try {
        const result = await getRechargeSetting(action.value, { id: id.value });
        Object.assign(formState.value, result);
    } catch (error: any) {
        message.error(error.message);
        emit("close");
    } finally {
        loading.value = false;
    }
};
const validateMoney = (rule: any, value: any, callback: any) => {
    console.log(value);
    if (Number(value) === 0 || value === "") {
        callback(new Error(t('金额不能为0或者为空！')));
        return
    } else if (Number(value) < 0) {
        callback(new Error(t('金额必须大于0！')));
        return
    } else {
        callback();
    }
};
// 表单通过验证后提交
const onSubmit = async () => {
    await formRef.value.validate();
    try {
        emit("update:confirmLoading", true);
        const result = await updateRechargeSetting(operation, { id: id.value, ...formState.value });
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
<style scoped>
/* 提示条背景左边从 label 起、右边与输入框右边对齐（宽度=label 78px + tig-form-input 450px）；
   正常宽度单行显示，窄屏自动换行，避免「去关闭」被挤出裁切或被右侧元素遮挡 */
:deep(.el-alert) {
    align-items: center;
    width: 650px;
    max-width: 100%;
    box-sizing: border-box;
}
:deep(.el-alert__title) {
    font-size: 13px;
    line-height: 1.6;
    white-space: normal;
    word-break: break-word;
}
:deep(.el-alert__title .btn-link) {
    font-size: 13px;
    white-space: nowrap;
}
</style>
