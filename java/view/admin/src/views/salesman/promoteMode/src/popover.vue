<template>
    <el-form ref="formRef" :rules="rules" :model="conditionsOpts" label-width="auto">
        <el-form-item prop="conditionsData">
            <el-popover ref="popoverRef" placement="right" :title="$t('升级设置')" :width="350" trigger="click">
                <template #reference>
                    <slot></slot>
                </template>
                <template #default>
                    <div class="tips mb10">{{ $t('以下条件至少选择一个，选择多个需同时满足才可升级。自购金额、推广金额需交易完成后才会统计。') }}</div>
                    <div class="check-list">
                        <el-form-item prop="selfBuyAmount" :rules="rules.selfBuyAmount">
                            <div class="item flex flex-align-center flex-justify-between">
                                <div class="check">
                                    <el-checkbox
                                        v-model="conditionsOpts.selfBuyAmount.checked"
                                        :label="conditionsOpts.selfBuyAmount.title"
                                        :disabled="conditionsOpts.selfBuyAmount.disabled"
                                        size="large"
                                    />
                                </div>
                                <div class="inp flex flex-align-center">
                                    <p class="mr10">{{ $t('满') }}</p>
                                    <TigInput
                                        type="decimal"
                                        v-model="conditionsOpts.selfBuyAmount.value"
                                        :disabled="!conditionsOpts.selfBuyAmount.checked"
                                        width="130px"
                                        :min="0"
                                    >
                                        <template #append>{{ conditionsOpts.selfBuyAmount.unit }}</template>
                                    </TigInput>
                                </div>
                            </div>
                        </el-form-item>
                        <el-form-item prop="salesAmount" :rules="rules.salesAmount">
                            <div class="item flex flex-align-center flex-justify-between">
                                <div class="check">
                                    <el-checkbox
                                        v-model="conditionsOpts.salesAmount.checked"
                                        :label="conditionsOpts.salesAmount.title"
                                        :disabled="conditionsOpts.salesAmount.disabled"
                                        size="large"
                                    />
                                </div>
                                <div class="inp flex flex-align-center">
                                    <p class="mr10">{{ $t('满') }}</p>
                                    <TigInput
                                        type="decimal"
                                        v-model="conditionsOpts.salesAmount.value"
                                        :disabled="!conditionsOpts.salesAmount.checked"
                                        width="130px"
                                        :min="0"
                                    >
                                        <template #append>{{ conditionsOpts.salesAmount.unit }}</template>
                                    </TigInput>
                                </div>
                            </div>
                        </el-form-item>
                        <el-form-item prop="salesInviteUsers" :rules="rules.salesInviteUsers">
                            <div class="item flex flex-align-center flex-justify-between">
                                <div class="check">
                                    <el-checkbox
                                        v-model="conditionsOpts.salesInviteUsers.checked"
                                        :label="conditionsOpts.salesInviteUsers.title"
                                        :disabled="conditionsOpts.salesInviteUsers.disabled"
                                        size="large"
                                    />
                                </div>
                                <div class="inp flex flex-align-center">
                                    <p class="mr10">{{ $t('满') }}</p>
                                    <TigInput
                                        type="integer"
                                        v-model="conditionsOpts.salesInviteUsers.value"
                                        :disabled="!conditionsOpts.salesInviteUsers.checked"
                                        width="130px"
                                        :min="0"
                                    >
                                        <template #append>{{ conditionsOpts.salesInviteUsers.unit }}</template>
                                    </TigInput>
                                </div>
                            </div>
                        </el-form-item>
                        <!-- <el-form-item prop="inviteSales" :rules="rules.inviteSales">
                            <div class="item flex flex-align-center flex-justify-between">
                                <div class="check">
                                    <el-checkbox v-model="conditionsOpts.inviteSales.checked" :label="conditionsOpts.inviteSales.title" size="large" />
                                </div>
                                <div class="inp flex flex-align-center">
                                    <p class="mr10">满</p>
                                    <TigInput type="integer" v-model="conditionsOpts.inviteSales.value" :disabled="!conditionsOpts.inviteSales.checked" style="width: 120px">
                                        <template #append>{{conditionsOpts.inviteSales.unit}}</template>
                                    </TigInput>
                                </div>
                            </div>
                        </el-form-item> -->
                    </div>
                    <div class="btn-box">
                        <div class="tips">
                            <span v-if="!isVerified" class="red">{{ $t('请设置升级条件') }}</span>
                        </div>
                        <div>
                            <el-button @click="popoverRef.hide()">{{ $t('取消') }}</el-button>
                            <el-button type="primary" @click="handleSubmit">{{ $t('确认') }}</el-button>
                        </div>
                    </div>
                </template>
            </el-popover>
        </el-form-item>
    </el-form>
</template>
<script lang="ts" setup>
import { Plus, QuestionFilled, Edit } from "@element-plus/icons-vue";
import { onMounted, reactive, ref, watch, nextTick, PropType } from "vue";
import type { FormRules } from "element-plus";
import { priceFormat } from "@/utils/format";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
import { useConfigStore } from "@/store/config";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const emit = defineEmits(["submitCallback"]);
const config = useConfigStore();
export interface conditionsInterface {
    selfBuyAmount: { checked: boolean; disabled: boolean; value: number; title: string; unit: string };
    salesAmount: { checked: boolean; disabled: boolean; value: number; title: string; unit: string };
    salesInviteUsers: { checked: boolean; disabled: boolean; value: number; title: string; unit: string };
}
const props = defineProps({
    conditionsData: {
        type: Object,
        default: () => ({})
    },
    superiorData: {
        type: Object,
        default: null
    },
    index: {
        type: Number,
        default: 0
    }
});
const formRef = ref<any>(null);
const popoverRef = ref<any>(null);
const conditionsOpts = reactive<conditionsInterface>({
    selfBuyAmount: { checked: false, disabled: false, value: 0.0, title: t('自购金额'), unit: t('{get}', { get: config.get("amountUnit") ?? t('元') }) },
    salesAmount: { checked: false, disabled: false, value: 0.0, title: t('推广金额'), unit: t('{get}', { get: config.get("amountUnit") ?? t('元') }) },
    salesInviteUsers: { checked: false, disabled: false, value: 0, title: t('发展客户数'), unit: t('人') }
    // inviteSales: { checked: false, value: 0, title: "邀请分销员", unit: "人" }
});
const isVerified = ref(false);
const handleSubmit = async () => {
    await formRef.value.validate();
    // 设置初始值为 true，假设所有条件都为 false
    let allUnchecked = true;
    for (let key in conditionsOpts) {
        let strKey: keyof typeof conditionsOpts = key as keyof typeof conditionsOpts;
        if (conditionsOpts[strKey].checked) {
            allUnchecked = false;
            break;
        }
    }
    // 如果所有的条件都未勾选，则不进行后续操作
    if (allUnchecked) {
        console.log("No conditions selected. Verification failed.");
        return;
    }
    // 如果有条件选中，则认为验证通过
    isVerified.value = true;
    emit("submitCallback", { conditionsOpts, index: props.index });
    popoverRef.value.hide();
};
const validateSelfBuyAmount = (rule: any, value: any, callback: any) => {
    console.log(props.superiorData == null);
    if (value.checked && Number(value.value) <= 0) {
        callback(new Error(t('请输入自购金额')));
        return;
    }
    if (props.superiorData != null) {
        if (value.checked && Number(value.value) <= Number(props.superiorData.selfBuyAmount.value)) {
            callback(new Error(t('需大于上一等级金额({value})', { value: props.superiorData.selfBuyAmount.value })));
        } else {
            callback();
        }
    } else {
        callback();
    }
};
const validateSalesAmount = (rule: any, value: any, callback: any) => {
    if (value.checked && Number(value.value) <= 0) {
        callback(new Error(t('请输入推广金额')));
        return;
    }
    if (props.superiorData != null) {
        if (value.checked && Number(value.value) <= Number(props.superiorData.salesAmount.value)) {
            callback(new Error(t('需大于上一等级金额({value})', { value: props.superiorData.salesAmount.value })));
        } else {
            callback();
        }
    } else {
        callback();
    }
    callback();
};
const validateSalesInviteUsers = (rule: any, value: any, callback: any) => {
    if (value.checked && Number(value.value) <= 0) {
        callback(new Error(t('请输入发展客户数')));
        return;
    }
    if (props.superiorData != null) {
        if (value.checked && Number(value.value) <= Number(props.superiorData.salesInviteUsers.value)) {
            callback(new Error(t('需大于上一等级客户数({value})', { value: props.superiorData.salesInviteUsers.value })));
        } else {
            callback();
        }
    } else {
        callback();
    }
    callback();
};
// const validateInviteSales = (rule: any, value: any, callback: any) => {
//     if(value.checked && Number(value.value) <= 0){
//         callback(new Error('请输入邀请分销员数量'));
//         return
//     }
//     callback();
// }
interface RuleForm {
    selfBuyAmount: string;
    salesAmount: string;
    salesInviteUsers: string;
    // inviteSales: string;
}
const rules = reactive<FormRules<RuleForm>>({
    selfBuyAmount: [{ required: true, validator: validateSelfBuyAmount, trigger: "blur" }],
    salesAmount: [{ required: true, validator: validateSalesAmount, trigger: "blur" }],
    salesInviteUsers: [{ required: true, validator: validateSalesInviteUsers, trigger: "blur" }]
    // inviteSales: [{ required: true, validator: validateInviteSales, trigger: "blur" }]
});
const dataProcessing = () => {
    const conditionsData = props.conditionsData || {};
    const superiorData = props.superiorData || {};
    const superiorSelfBuyAmount = (superiorData as any).selfBuyAmount;
    const superiorSalesAmount = (superiorData as any).salesAmount;
    const superiorSalesInviteUsers = (superiorData as any).salesInviteUsers;
    if (superiorSelfBuyAmount?.checked) {
        conditionsData.selfBuyAmount.checked = true;
        conditionsData.selfBuyAmount.disabled = true;
    } else {
        conditionsData.selfBuyAmount.disabled = false;
    }
    if (superiorSalesAmount?.checked) {
        conditionsData.salesAmount.checked = true;
        conditionsData.salesAmount.disabled = true;
    } else {
        conditionsData.salesAmount.disabled = false;
    }
    if (superiorSalesInviteUsers?.checked) {
        conditionsData.salesInviteUsers.checked = true;
        conditionsData.salesInviteUsers.disabled = true;
    } else {
        conditionsData.salesInviteUsers.disabled = false;
    }
    Object.assign(conditionsOpts, conditionsData);
};
onMounted(() => {
    if (props.superiorData) {
        nextTick(() => {
            dataProcessing();
        });
    } else {
        Object.assign(conditionsOpts, props.conditionsData || {});
    }
});
watch(
    () => props.superiorData,
    () => {
        dataProcessing();
    },
    { deep: true }
);
</script>
<style lang="less" scoped>
.check-list {
    border-top: 1px solid #f0f0f0;
    :deep(.el-checkbox.el-checkbox--large .el-checkbox__inner) {
        width: 16px;
        height: 16px;
    }
    :deep(.el-form-item) {
        margin-bottom: 0;
    }
    .item {
        width: 100%;
        border-bottom: 1px solid #f0f0f0;
        padding: 12px 0;
    }
    :deep(.el-input-group__append, .el-input-group__prepend) {
        padding: 0 10px !important;
    }
    :deep(.el-form-item__error) {
        top: 47px;
        left: 196px;
    }
}
.btn-box {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 20px;
}
</style>
