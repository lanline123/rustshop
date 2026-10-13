<template>
    <div class="category-fee-rule-form">
        <el-form ref="formRef" :model="formState" label-width="110px" require-asterisk-position="right">
            <el-form-item :label="$t('规则名称')" prop="ruleName" :rules="[{ required: true, message: '请输入规则名称' }]">
                <el-input v-model="formState.ruleName" :placeholder="$t('请输入规则名称')" style="width: 400px" maxlength="50" clearable />
            </el-form-item>

            <el-form-item :label="$t('选择类目')" required>
                <SelectCategoryPanel v-model="selectedCategoryIds" />
            </el-form-item>

            <el-form-item :label="$t('适用{organizationLabel}', { organizationLabel })" required>
                <div class="form-block">
                    <el-radio-group v-model="organizationMode" class="organization-mode-group">
                        <el-radio value="ALL">{{ $t('全部{organizationLabel}', { organizationLabel }) }}</el-radio>
                        <el-radio value="INCLUDE">{{ $t('以下{organizationLabel}', { organizationLabel }) }}</el-radio>
                        <el-radio value="EXCLUDE">{{ $t('不含以下{organizationLabel}', { organizationLabel }) }}</el-radio>
                    </el-radio-group>
                    <SelectOrganizationPanel v-if="organizationMode !== 'ALL'" v-model="selectedOrganizations" :tab-key="currentTabKey" />
                </div>
            </el-form-item>

            <el-form-item :label="$t('服务费率')" prop="serviceFeeRate" :rules="[{ required: true, message: '请输入服务费率' }]">
                <TigInput
                    v-model="formState.serviceFeeRate"
                    type="decimal"
                    width="180px"
                    :min="0"
                    :max="100"
                    :decimal-places="2"
                    :fixed-decimal="false"
                    :allow-empty-decimal="true"
                    :placeholder="$t('请输入服务费率')"
                >
                    <template #append>%</template>
                </TigInput>
            </el-form-item>

            <el-form-item :label="$t('备注')">
                <el-input v-model="formState.remark" type="textarea" :placeholder="$t('请输入备注')" :maxlength="200" :rows="3" show-word-limit style="width: 400px" />
            </el-form-item>

            <el-form-item :label="$t('规则状态')">
                <el-switch v-model="formState.status" :active-value="1" :inactive-value="0" />
            </el-form-item>
        </el-form>
    </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, ref, shallowRef } from "vue";
import { message } from "ant-design-vue";
import { isStore } from "@/utils/version";
import {
    createCategoryFeeRule,
    getCategoryFeeRule,
    updateCategoryFeeRule
} from "@/api/finance/categoryFeeRule";
import type {
    CategoryFeeRuleCreateParams,
    CategoryFeeRuleScopeType,
    CategoryFeeRuleStatus,
    CategoryFeeRuleTargetType
} from "@/types/finance/categoryFeeRule";
import SelectCategoryPanel from "./components/SelectCategoryPanel.vue";
import SelectOrganizationPanel from "./components/SelectOrganizationPanel.vue";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
type FeeRuleTabKey = "store" | "supplier";
type OrganizationMode = "ALL" | "INCLUDE" | "EXCLUDE";

interface OrganizationOption {
    id: number;
    name: string;
}

const props = defineProps({
    act: { type: String, default: "add" },
    id: { type: Number, default: 0 },
    tabKey: { type: String as () => FeeRuleTabKey, default: "store" },
    isDialog: { type: Boolean, default: false }
});

const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);

const formRef = shallowRef();
const selectedCategoryIds = ref<number[]>([]);
const selectedOrganizations = ref<OrganizationOption[]>([]);
const organizationMode = ref<OrganizationMode>("ALL");
const formState = ref({
    ruleName: "",
    serviceFeeRate: undefined as number | undefined,
    remark: "",
    status: 1 as CategoryFeeRuleStatus
});

const currentTabKey = computed<FeeRuleTabKey>(() => props.tabKey || "store");
const organizationLabel = computed(() => {
    if (currentTabKey.value === "supplier") return t('供应商');
    return isStore() ? t('门店') : t('店铺');
});

const resolveTargetType = (tabKey: FeeRuleTabKey): CategoryFeeRuleTargetType => (tabKey === "store" ? 1 : 2);

const resolveScopeType = (mode: OrganizationMode): CategoryFeeRuleScopeType => {
    if (mode === "INCLUDE") return 2;
    if (mode === "EXCLUDE") return 3;
    return 1;
};

const resolveOrganizationMode = (scopeType?: number): OrganizationMode => {
    if (scopeType === 2) return "INCLUDE";
    if (scopeType === 3) return "EXCLUDE";
    return "ALL";
};

const loadDetail = async () => {
    if (props.act !== "edit" || !props.id) return;
    try {
        const detail = await getCategoryFeeRule({ id: props.id });
        formState.value.ruleName = detail.ruleName || "";
        formState.value.serviceFeeRate = detail.serviceFeeRate;
        formState.value.remark = detail.remark || "";
        formState.value.status = detail.status ?? 1;
        organizationMode.value = resolveOrganizationMode(detail.scopeType);
        selectedCategoryIds.value = (detail.categories || []).map((item) => Number(item.categoryId)).filter((id) => Number.isFinite(id));
        selectedOrganizations.value = (detail.targets || []).map((item) => ({
            id: Number(item.targetId),
            name: item.targetName || String(item.targetId)
        }));
    } catch (error: any) {
        message.error(error.message);
    }
};

const buildPayload = (): CategoryFeeRuleCreateParams | null => {
    const categoryIds = selectedCategoryIds.value.map((id) => Number(id)).filter((id) => Number.isFinite(id));
    if (categoryIds.length === 0) {
        message.error(t('请选择商品类目'));
        return null;
    }

    const targetIds = selectedOrganizations.value.map((item) => Number(item.id)).filter((id) => Number.isFinite(id));
    const scopeType = resolveScopeType(organizationMode.value);
    if (scopeType !== 1 && targetIds.length === 0) {
        message.error(t('请选择{value}', { value: organizationLabel.value }));
        return null;
    }

    if (formState.value.serviceFeeRate === undefined || formState.value.serviceFeeRate === null) {
        message.error(t('请输入服务费率'));
        return null;
    }

    return {
        ruleName: formState.value.ruleName.trim(),
        targetType: resolveTargetType(currentTabKey.value),
        scopeType,
        serviceFeeRate: Number(formState.value.serviceFeeRate),
        remark: formState.value.remark.trim() || undefined,
        status: formState.value.status,
        categoryIds,
        targetIds: scopeType === 1 ? undefined : targetIds
    };
};

const submitRule = async (payload: CategoryFeeRuleCreateParams) => {
    if (props.act === "edit" && props.id) {
        return updateCategoryFeeRule({ id: props.id, ...payload });
    }
    return createCategoryFeeRule(payload);
};

const showMutationSuccess = (disabledRuleNames?: string[]) => {
    if (disabledRuleNames?.length) {
        message.success(t('操作成功，已自动禁用：{join}', { join: disabledRuleNames.join("、") }));
        return;
    }
    message.success(t('操作成功'));
};

const onSubmit = async () => {
    try {
        await formRef.value.validate();
    } catch {
        return;
    }
    const payload = buildPayload();
    if (!payload) return;

    try {
        emit("update:confirmLoading", true);
        const result = await submitRule(payload);
        showMutationSuccess(result);
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
.category-fee-rule-form {
    padding-right: 12px;
}

.form-block {
    width: 100%;
}

.organization-mode-group {
    margin-bottom: 12px;
}

</style>
