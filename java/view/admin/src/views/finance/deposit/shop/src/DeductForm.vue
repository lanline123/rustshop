/** * @file 扣除保证金弹窗 * @description 顶部总览条（只读）+ 扣除表单（金额 ≤ 已缴余额 paid 前端校验） */
<template>
    <a-spin class="deduct-form" :spinning="loading">
        <DepositOverviewBar :detail="detail" />

        <el-form ref="formRef" :model="formState" label-width="110px">
            <el-form-item :label="$t('保证金扣除')" prop="amount" :rules="deductAmountRules">
                <div>
                    <TigInput v-model="formState.amount" type="decimal" width="180px" allowEmptyDecimal :decimal-places="2" :placeholder="$t('请输入扣除金额')">
                        <template #append>{{ currencySign }}</template>
                    </TigInput>
                    <div class="extra">{{ $t('当前类目保证金：{priceFormat}，保证金余额：{priceFormat1}', { priceFormat: priceFormat(detail.frozen), priceFormat1: priceFormat(detail.available) }) }}</div>
                </div>
            </el-form-item>
            <el-form-item :label="$t('扣除原因')" prop="reason" :rules="[{ required: true, message: '请输入扣除原因' }]">
                <el-input
                    v-model="formState.reason"
                    type="textarea"
                    :rows="6"
                    :maxlength="200"
                    show-word-limit
                    :placeholder="$t('请输入扣除原因')"
                    style="width: 100%"
                />
            </el-form-item>
        </el-form>
    </a-spin>
</template>

<script lang="ts" setup>
import { computed, onMounted, ref, shallowRef } from "vue";
import { message } from "ant-design-vue";
import { priceFormat } from "@/utils/format";
import { deductDeposit, getDepositQueryDetail } from "@/api/finance/deposit";
import { useConfigStore } from "@/store/config";
import type { DepositQueryDetail } from "@/types/finance/deposit.d";
import DepositOverviewBar from "./DepositOverviewBar.vue";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const props = defineProps({
    ownerType: { type: Number, default: undefined },
    ownerId: { type: Number, default: undefined },
    shopId: { type: Number, default: 0 },
    isDialog: Boolean
});

const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);

const config = useConfigStore();
const currencySign = computed(() => config.get("dollarSign") ?? "¥");

const shopId = ref<number>(props.shopId);
const ownerType = ref<number | undefined>(props.ownerType);
const ownerId = ref<number | undefined>(props.ownerId);
const loading = ref<boolean>(true);
const formRef = shallowRef();
const detail = ref<DepositQueryDetail>({ shopId: props.shopId });

const formState = ref({
    amount: undefined as number | undefined,
    reason: ""
});

const deductAmountRules = computed(() => [
    { required: true, message: t('请输入扣除金额') },
    {
        validator: (_rule: unknown, value: unknown, callback: (error?: Error) => void) => {
            const amount = Number(value);
            if (!value || amount <= 0) return callback(new Error(t('扣除金额必须大于 0')));
            const paid = Number(detail.value.paid || 0);
            if (amount > paid) return callback(new Error(t('扣除金额不能超过已缴余额')));
            callback();
        }
    }
]);

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
    try {
        await formRef.value.validate();
    } catch {
        return;
    }
    try {
        emit("update:confirmLoading", true);
        const result = await deductDeposit({
            ownerType: ownerType.value,
            ownerId: ownerId.value,
            shopId: shopId.value,
            amount: Number(formState.value.amount),
            reason: formState.value.reason
        });
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
.deduct-form {
    padding: 0 6px;
}
.extra {
    color: #999;
    font-size: 12px;
    margin-top: 4px;
}
</style>
