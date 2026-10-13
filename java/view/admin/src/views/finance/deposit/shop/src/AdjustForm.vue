/** * @file 调整保证金弹窗 * @description 顶部总览条（只读）+ 双 tab（调整保证金 / 调整记录） */
<template>
    <a-spin class="adjust-form" :spinning="loading">
        <DepositOverviewBar :detail="detail" />
        <el-tabs v-model="activeTab" @tab-change="onTabChange">
            <el-tab-pane :label="$t('调整保证金')" name="adjust">
                <el-form ref="formRef" :model="formState" label-width="110px">
                    <el-form-item :label="$t('保证金调整')" prop="adjustType" :rules="[{ required: true, message: '请选择调整方式' }]">
                        <div class="adjust-options">
                            <div class="radio-line">
                                <el-radio v-model="formState.adjustType" :value="1">{{ $t('按经营类目最高缴纳') }}</el-radio>
                            </div>
                            <div class="radio-line">
                                <el-radio v-model="formState.adjustType" :value="2">{{ $t('手动调整') }}</el-radio>
                                <el-form-item prop="requiredManual" :rules="manualAmountRules" class="manual-amount-item" label-width="0">
                                    <TigInput
                                        v-model="formState.requiredManual"
                                        type="decimal"
                                        width="160px"
                                        allowEmptyDecimal
                                        :decimal-places="2"
                                        :disabled="formState.adjustType !== 2"
                                        :placeholder="$t('请输入')"
                                        @focus="formState.adjustType = 2"
                                    >
                                        <template #append>{{ currencySign }}</template>
                                    </TigInput>
                                </el-form-item>
                            </div>
                            <div class="tips">
                                {{ $t('{shopLabel}保证金额度手动调整后，该{shopLabel1}将固定采用调整后金额作为缴存标准，不再受{shopLabel2}经营类目保证金规则约束。', { shopLabel, shopLabel1: shopLabel, shopLabel2: shopLabel }) }}</div>
                        </div>
                    </el-form-item>
                    <el-form-item :label="$t('备注')" prop="reason">
                        <el-input v-model="formState.reason" type="textarea" :rows="6" :maxlength="200" :placeholder="$t('请输入备注')" style="width: 100%" />
                    </el-form-item>
                </el-form>
            </el-tab-pane>
            <el-tab-pane :label="$t('调整记录')" name="log">
                <AdjustLogTimeline ref="logRef" :owner-type="ownerType" :owner-id="ownerId" />
            </el-tab-pane>
        </el-tabs>
    </a-spin>
</template>

<script lang="ts" setup>
import { computed, nextTick, onMounted, ref, shallowRef, watch } from "vue";
import { message } from "ant-design-vue";
import { adjustDeposit, getDepositQueryDetail } from "@/api/finance/deposit";
import { useConfigStore } from "@/store/config";
import { OrgScope } from "@/types/finance/deposit.d";
import type { AdjustDepositParams, DepositQueryDetail } from "@/types/finance/deposit.d";
import AdjustLogTimeline from "./AdjustLogTimeline.vue";
import DepositOverviewBar from "./DepositOverviewBar.vue";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const config = useConfigStore();
const currencySign = computed(() => config.get("dollarSign") ?? "¥");

const props = defineProps({
    ownerType: { type: Number, default: undefined },
    ownerId: { type: Number, default: undefined },
    shopId: { type: Number, default: 0 },
    isDialog: Boolean
});

const emit = defineEmits(["submitCallback", "update:confirmLoading", "close", "okType"]);

const shopId = ref<number>(props.shopId);
const ownerType = ref<number | undefined>(props.ownerType);
const ownerId = ref<number | undefined>(props.ownerId);
const activeTab = ref<string>("adjust");
const loading = ref<boolean>(true);
const formRef = shallowRef();
const logRef = ref();
const detail = ref<DepositQueryDetail>({ shopId: props.shopId });

// 当前组织称谓
const shopLabel = computed(() => {
    if (detail.value.ownerTypeText) {
        return detail.value.ownerTypeText;
    }
    if ((detail.value.ownerType ?? ownerType.value) === OrgScope.Supplier) {
        return t('供应商');
    }
    if (detail.value.shopType === 2) {
        return t('门店');
    }
    return t('店铺');
});

const formState = ref({
    adjustType: 1,
    requiredManual: undefined as number | undefined,
    reason: ""
});

const manualAmountRules = computed(() =>
    formState.value.adjustType === 2
        ? [
              { required: true, message: t('请输入手动调整金额') },
              {
                  validator: (_rule: unknown, value: unknown, callback: (error?: Error) => void) => {
                      if (!value || Number(value) <= 0) return callback(new Error(t('手动调整金额必须大于 0')));
                      callback();
                  }
              }
          ]
        : []
);

watch(
    () => formState.value.adjustType,
    async (type) => {
        if (type !== 2) {
            formState.value.requiredManual = undefined;
            await nextTick();
            formRef.value?.clearValidate("requiredManual");
        }
    }
);

const onTabChange = (name: string) => {
    // 切换到"调整记录"时禁用确认按钮，避免误提交
    emit("okType", name === "adjust");
    if (name === "log") {
        logRef.value?.loadData();
    }
};

const loadDetail = async () => {
    try {
        const result = await getDepositQueryDetail({ ownerType: ownerType.value, ownerId: ownerId.value, shopId: shopId.value });
        detail.value = result;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};

const onSubmit = async () => {
    if (activeTab.value !== "adjust") return;
    try {
        await formRef.value.validate();
    } catch {
        return;
    }
    const payload: AdjustDepositParams = {
        ownerType: ownerType.value,
        ownerId: ownerId.value,
        shopId: shopId.value,
        adjustType: formState.value.adjustType,
        requiredManual: formState.value.adjustType === 2 ? formState.value.requiredManual : undefined,
        reason: formState.value.reason
    };
    try {
        emit("update:confirmLoading", true);
        const result = await adjustDeposit(payload);
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
.adjust-form {
    padding: 0 6px;
}
.adjust-options {
    display: flex;
    flex-direction: column;
    gap: 12px;
    width: 100%;
}
.radio-line {
    display: flex;
    align-items: center;
    gap: 12px;
    :deep(.el-radio) {
        margin-right: 0;
    }
}
.manual-amount-item {
    margin-bottom: 0;
    :deep(.el-form-item__content) {
        line-height: normal;
    }
}
.tips {
    color: #999;
    font-size: 12px;
    line-height: 1.6;
}
</style>
