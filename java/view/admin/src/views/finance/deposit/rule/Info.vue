/** * @file 保证金规则 添加/编辑表单 * @description DialogForm 子页，复用 SelectCategoryPanel 选择适用类目 */
<template>
    <div class="deposit-rule-form">
        <el-form ref="formRef" :model="formState" label-width="110px">
            <el-form-item :label="$t('规则名称')" prop="ruleName" :rules="[{ required: true, message: '请输入规则名称' }]">
                <el-input v-model="formState.ruleName" :placeholder="$t('请输入规则名称')" style="width: 400px" maxlength="50" clearable />
            </el-form-item>

            <el-form-item :label="$t('主体类型')" required>
                <div class="form-block">
                    <el-checkbox-group v-model="formState.merchantTypes">
                        <el-checkbox :value="MerchantType.Personal">{{ $t('个人') }}</el-checkbox>
                        <el-checkbox :value="MerchantType.Enterprise">{{ $t('企业') }}</el-checkbox>
                    </el-checkbox-group>
                    <div class="extra">{{ $t('限定规则适用商户资质，勾选类型的商户按本规则缴保') }}</div>
                </div>
            </el-form-item>

            <el-form-item :label="$t('适用组织')" required>
                <div class="form-block">
                    <el-radio-group v-model="orgScopeSingle">
                        <el-radio :value="OrgScope.Shop">{{ isStore() ? $t('门店') : $t('店铺') }}</el-radio>
                        <el-radio v-if="showSupplier" :value="OrgScope.Supplier">{{ $t('供应商') }}</el-radio>
                    </el-radio-group>
                    <div class="extra">{{ $t('限定规则适用商户身份，店铺/供应商可单选或多选') }}</div>
                </div>
            </el-form-item>

            <el-form-item :label="$t('适用类目')" required>
                <SelectCategoryPanel v-model="formState.categoryIds" />
            </el-form-item>

            <el-form-item :label="$t('保证金金额')" prop="depositAmount" :rules="[{ required: true, message: '请输入保证金金额' }]">
                <TigInput v-model="formState.depositAmount" type="decimal" width="200px" :min="0.01" :decimal-places="2" :placeholder="$t('请输入保证金金额')">
                    <template #append>{{ $t('元') }}</template>
                </TigInput>
            </el-form-item>

            <el-form-item :label="$t('生效时间')" required>
                <div class="form-block">
                    <el-radio-group v-model="formState.effectiveMode" class="effective-mode-group">
                        <div class="effective-row">
                            <el-radio :value="1">{{ $t('区间有效') }}</el-radio>
                            <SelectTimeInterval
                                type="datetime"
                                value-format="YYYY-MM-DD HH:mm"
                                format="YYYY-MM-DD HH:mm"
                                :end-default-time="endDefaultTime"
                                v-model:startDate="formState.startDate"
                                v-model:endDate="formState.endDate"
                                :disabled="formState.effectiveMode !== 1"
                                separator="~"
                            />
                        </div>
                        <div class="effective-row">
                            <el-radio :value="2">{{ $t('长期有效') }}</el-radio>
                            <div>
                                <el-date-picker
                                    v-model="longStartDate"
                                    type="datetime"
                                    value-format="YYYY-MM-DD HH:mm"
                                    format="YYYY-MM-DD HH:mm"
                                    :placeholder="$t('开始日期')"
                                    style="width: 200px"
                                    :editable="false"
                                    :clearable="false"
                                    :disabled="formState.effectiveMode !== 2"
                                />
                                <span class="effective-sep">~</span>
                                <span class="effective-forever">{{ $t('长期') }}</span>
                            </div>
                        </div>
                    </el-radio-group>
                </div>
            </el-form-item>

            <el-form-item :label="$t('规则状态')">
                <el-switch v-model="formState.status" :active-value="1" :inactive-value="0" />
            </el-form-item>
        </el-form>
    </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, ref, shallowRef, watch } from "vue";
import { message } from "ant-design-vue";
import dayjs from "dayjs";
import { SelectTimeInterval } from "@/components/select";
import { isStore, isS2b2c } from "@/utils/version";
import { getDepositRule, updateDepositRule } from "@/api/finance/deposit";
import { MerchantType, OrgScope } from "@/types/finance/deposit.d";
import type { DepositRuleFormState } from "@/types/finance/deposit.d";
import SelectCategoryPanel from "@/views/finance/categoryServiceFeeRule/components/SelectCategoryPanel.vue";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const props = defineProps({
    act: { type: String, default: "add" },
    id: { type: Number, default: 0 },
    isDialog: { type: Boolean, default: false }
});

const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);

const formRef = shallowRef();
const formState = ref<DepositRuleFormState>({
    ruleName: "",
    merchantTypes: [],
    shopTypes: [OrgScope.Shop],
    categoryIds: [],
    depositAmount: undefined,
    effectiveMode: 1,
    startDate: "",
    endDate: "",
    status: 1
});

// 长期有效模式的开始时间，与区间有效模式独立，避免两边互相回显
const longStartDate = ref("");

watch(
    () => formState.value.effectiveMode,
    (mode) => {
        if (mode === 1) {
            longStartDate.value = "";
        } else {
            formState.value.startDate = "";
            formState.value.endDate = "";
        }
    }
);

// 区间有效：结束时间选中日期后默认 23:59
const endDefaultTime = new Date(2000, 0, 1, 23, 59, 0);

const showSupplier = computed(() => isS2b2c());

const orgScopeSingle = computed({
    get: () => formState.value.shopTypes[0],
    set: (val) => {
        formState.value.shopTypes = val != null ? [val] : [];
    }
});

const loadDetail = async () => {
    if (props.act !== "edit" || !props.id) return;
    try {
        const detail = await getDepositRule("detail", { id: props.id });
        formState.value.shopDepositRuleId = detail.shopDepositRuleId;
        formState.value.ruleName = detail.ruleName || "";
        formState.value.merchantTypes = detail.merchantTypes || [];
        formState.value.shopTypes = detail.shopTypes || [];
        formState.value.categoryIds = detail.categoryIds || [];
        formState.value.depositAmount = detail.depositAmount;
        formState.value.status = detail.status;
        const startText = detail.startTime ? dayjs.unix(detail.startTime).format("YYYY-MM-DD HH:mm") : "";
        formState.value.effectiveMode = detail.endTime ? 1 : 2;
        if (formState.value.effectiveMode === 2) {
            longStartDate.value = startText;
            formState.value.startDate = "";
            formState.value.endDate = "";
        } else {
            formState.value.startDate = startText;
            formState.value.endDate = detail.endTime ? dayjs.unix(detail.endTime).format("YYYY-MM-DD HH:mm") : "";
            longStartDate.value = "";
        }
    } catch (error: any) {
        message.error(error.message);
    }
};

const validateBusiness = (): boolean => {
    if (formState.value.merchantTypes.length === 0) {
        message.error(t('请选择主体类型'));
        return false;
    }
    if (formState.value.shopTypes.length === 0) {
        message.error(t('请选择适用组织'));
        return false;
    }
    if (formState.value.categoryIds.length === 0) {
        message.error(t('请选择适用类目'));
        return false;
    }
    if (formState.value.effectiveMode === 2) {
        if (!longStartDate.value) {
            message.error(t('请选择生效时间'));
            return false;
        }
        return true;
    }
    if (!formState.value.startDate) {
        message.error(t('请选择生效时间'));
        return false;
    }
    if (!formState.value.endDate) {
        message.error(t('请选择生效结束时间'));
        return false;
    }
    if (formState.value.startDate >= formState.value.endDate) {
        message.error(t('生效开始时间必须早于结束时间'));
        return false;
    }
    return true;
};

const onSubmit = async () => {
    try {
        await formRef.value.validate();
    } catch {
        return;
    }
    if (!validateBusiness()) return;

    const { startDate, endDate, effectiveMode } = formState.value;
    const effectiveStart = effectiveMode === 2 ? longStartDate.value : startDate;
    const payload: Record<string, unknown> = {
        ruleName: formState.value.ruleName,
        merchantTypes: formState.value.merchantTypes,
        shopTypes: formState.value.shopTypes,
        categoryIds: formState.value.categoryIds,
        depositAmount: formState.value.depositAmount,
        status: formState.value.status,
        startTime: effectiveStart ? dayjs(effectiveStart).unix() : 0,
        endTime: effectiveMode === 2 ? 0 : endDate ? dayjs(endDate).unix() : 0
    };
    if (props.act === "edit" && props.id) {
        payload.shopDepositRuleId = props.id;
    }

    try {
        emit("update:confirmLoading", true);
        const operation = props.act === "edit" && props.id ? "update" : "create";
        const result = await updateDepositRule(operation, payload);
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
.deposit-rule-form {
    padding-right: 12px;
}
.form-block {
    width: 100%;
}
.effective-mode-group {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
}
.effective-row {
    display: flex;
    align-items: center;
    gap: 12px;
    font-size: var(--el-font-size-base, 14px);
}
.effective-row :deep(.el-radio) {
    width: 90px;
    margin-right: 0;
}
.effective-sep {
    padding: 0 10px;
}
.effective-forever {
    color: #333;
}
.extra {
    color: #999;
    font-size: 12px;
    margin-top: 4px;
}
</style>
