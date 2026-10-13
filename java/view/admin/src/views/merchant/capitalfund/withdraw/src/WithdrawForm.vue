<template>
    <div class="withdraw-form">
        <el-form v-if="!loading" ref="formRef" :model="formState" class="form-body" label-width="auto">
            <div class="dual-balance-bar">
                <div class="balance-item">
                    <span class="balance-label">{{ source === "online" ? $t("线上余额") : $t("线下余额") }}</span>
                    <span class="balance-value">{{ priceFormat(currentBalance) }}</span>
                </div>
            </div>

            <el-form-item
                v-if="source === 'offline'"
                :rules="[{ required: true, message: $t('请选择提现账户!') }]"
                :label="$t('提现账户：')"
                prop="merchantAccountId"
            >
                <el-select
                    v-model="formState.merchantAccountId"
                    :disabled="isAccountSelectionDisabled(currentWithdrawEnabled)"
                    :placeholder="$t('请选择提现账户')"
                    style="width: 240px"
                    clearable
                    @change="handleChange"
                >
                    <el-option v-for="item in cardList" :key="item.accountId" :label="`${item.accountNo}(${item.accountTypeText})`" :value="item.accountId" />
                </el-select>
                <router-link v-if="currentWithdrawEnabled && !formState.merchantAccountId" :to="{ path: '/account/list' }">
                    <el-button style="margin-left: 10px" type="primary" link>{{ $t("去添加") }}</el-button>
                </router-link>
            </el-form-item>
            <el-form-item v-else :label="$t('提现账户：')">
                <el-input :model-value="onboardingAccountDisplay" disabled :placeholder="$t('暂无进件结算账户')" style="width: 320px" />
                <div v-if="!onlineAccountReady" class="extra online-account-hint">{{ $t("请先完成进件并填写结算银行账户") }}</div>
            </el-form-item>

            <el-form-item v-if="source !== 'online'" :label="$t('提现手续费率')">
                <div>
                    <TigInput type="integer" v-model="formState.storeWithdrawalFeeRate" width="240px" disabled v-if="getShopType() === 1">
                        <template #append>%</template>
                    </TigInput>
                    <TigInput type="integer" v-model="formState.storefrontWithdrawalFeeRate" width="240px" disabled v-if="getShopType() === 2">
                        <template #append>%</template>
                    </TigInput>
                </div>
            </el-form-item>
            <el-form-item :label="$t('提现金额：')" :rules="moneyRules" prop="amount">
                <div>
                    <div>
                        <TigInput
                            v-model="formState.amount"
                            type="decimal"
                            width="240px"
                            :placeholder="$t('提现金额')"
                            :disabled="currentBalance <= 0 || !currentWithdrawEnabled"
                            clearable
                        >
                            <template #prepend>{{ config.config.dollarSign }}</template>
                        </TigInput>
                        <el-button
                            v-if="currentWithdrawEnabled"
                            style="margin-left: 10px"
                            type="primary"
                            link
                            @click="formState.amount = currentBalance"
                            :disabled="currentBalance <= 0"
                            >{{ $t("全部提现") }}</el-button
                        >
                    </div>
                    <div class="extra">
                        {{ $t("可提现余额") }} <span style="color: #333">{{ priceFormat(currentBalance) }}</span>
                    </div>
                </div>
            </el-form-item>
            <el-form-item :label="$t('备注：')" prop="remark">
                <TigInput
                    v-model="formState.remark"
                    :disabled="!currentWithdrawEnabled"
                    :rows="4"
                    :placeholder="$t('输入提现用途(选填)')"
                    type="textarea"
                    width="300px"
                />
            </el-form-item>
            <el-form-item label=" ">
                <el-button type="primary" @click="onSubmit" :disabled="!currentWithdrawEnabled || submitting || (source === 'online' && !onlineAccountReady)" :loading="submitting">{{ $t("提交") }}</el-button>
                <div v-if="!currentWithdrawEnabled" class="extra ml10">{{ $t("平台提现功能已关闭") }}</div>
            </el-form-item>
        </el-form>
    </div>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { onMounted, reactive, ref, computed, shallowRef } from "vue";
import { message } from "ant-design-vue";
import { useConfigStore } from "@/store/config";
import { getAccountList } from "@/api/merchant/capitalfund/account";
import { AccountFilterState, AccountFilterParams } from "@/types/merchant/capitalfund/account.d";
import { createWithdraw, getDualBalance } from "@/api/merchant/capitalfund/withdraw";
import { WithdrawFormState, accountForm } from "@/types/merchant/capitalfund/withdraw.d";
import { priceFormat } from "@/utils/format";
import { getShopAccount } from "@/api/merchant/capitalfund/dashboard";
import { AccountFormState } from "@/types/merchant/capitalfund/dashboard.d";
import { getProfitsharingConfig } from "@/api/finance/profitsharingSetting";
import { getCurrentMerchantApplication } from "@/api/finance/merchantApplication";
import type { MerchantApplicationItem } from "@/types/finance/merchantApplication.d";
import { getShopType } from "@/utils/storage";
import { isAccountSelectionDisabled } from "./withdrawForm";

import { useI18n } from "vue-i18n";
const { t } = useI18n();
const props = defineProps<{
    source?: "offline" | "online";
}>();
const source = computed(() => props.source || "offline");
const config: any = useConfigStore();
const loading = ref<boolean>(false);
const formRef = shallowRef();
const formState = ref<WithdrawFormState>({
    merchantAccountId: "",
    amount: "",
    remark: "",
    storeWithdrawalFeeRate: 0
});
const withdrawSettingVO = config.get("withdrawSettingVO");
const onlineBalance = ref<number>(0);
const onlineChannel = ref<string | undefined>();

const offlineWithdrawEnabled = computed(() => Number(withdrawSettingVO?.withdrawalEnabled) === 1);
const onlineWithdrawEnabled = computed(() => Number(withdrawSettingVO?.onlineWithdrawEnabled) === 1);
const currentWithdrawEnabled = computed(() => (source.value === "offline" ? offlineWithdrawEnabled.value : onlineWithdrawEnabled.value));
const currentBalance = computed(() => (source.value === "offline" ? Number(accountData.value.shopMoney) : Number(onlineBalance.value ?? 0)));
const filterParams = reactive<AccountFilterParams>({
    page: 1,
    size: config.get("pageSize"),
    sortField: "",
    sortOrder: ""
});
const accountData = ref<AccountFormState>({
    shopMoney: 0
});
const cardList = ref<AccountFilterState[]>([]);
const onboardingAccount = ref<{ accountName: string; accountNo: string; bankName: string; bankBranch: string; typeText: string } | null>(null);
const onboardingFinished = ref(false);
const onboardingAccountDisplay = computed(() => {
    const acc = onboardingAccount.value;
    if (!acc?.accountNo && !acc?.accountName) return "";
    const bank = acc.bankName ? `（${acc.bankName}）` : "";
    const typeText = acc.typeText ? `${acc.typeText} ` : "";
    return `${typeText}${acc.accountName || ""} ${acc.accountNo || ""}${bank}`.trim();
});
/** 与后端 create 一致：只接受该渠道 FINISH 进件上的结算卡。 */
const onlineAccountReady = computed(() => Boolean(onboardingAccountDisplay.value && onboardingFinished.value));

const parseSnapshot = (raw: MerchantApplicationItem["requestSnapshot"]) => {
    if (!raw) return undefined;
    if (typeof raw === "object") return raw as Record<string, any>;
    try {
        return JSON.parse(raw) as Record<string, any>;
    } catch {
        return undefined;
    }
};

const pickOnboardingBankAccount = (app?: MerchantApplicationItem | null) => {
    const snapshot = parseSnapshot(app?.requestSnapshot);
    const accountName = String(app?.bankAccountName || snapshot?.bankAccountName || "").trim();
    const accountNo = String(app?.bankAccountNumber || snapshot?.bankAccountNumber || "").trim();
    const bankName = String(app?.bankName || snapshot?.bankName || "").trim();
    const bankBranch = String(app?.bankBranchName || snapshot?.bankBranchName || "").trim();
    const typeCode = String(app?.bankAccountType || snapshot?.bankAccountType || "");
    if (!accountName && !accountNo) return null;
    return {
        accountName,
        accountNo,
        bankName,
        bankBranch,
        typeText: typeCode === "74" ? t("对公") : typeCode === "75" ? t("对私") : ""
    };
};

const fillDefaultAccount = () => {
    const first = cardList.value[0];
    if (!first?.accountId) {
        formState.value.merchantAccountId = "";
        formState.value.accountData = {} as accountForm;
        return;
    }
    formState.value.merchantAccountId = first.accountId;
    formState.value.accountData = first as accountForm;
};

const loadFilter = async () => {
    loading.value = true;
    try {
        const result1 = await getShopAccount();
        accountData.value = result1;
        if (source.value !== "online") {
            const result2 = await getProfitsharingConfig();
            getShopType() === 1
                ? (formState.value.storeWithdrawalFeeRate = result2.storeWithdrawalFeeRate)
                : (formState.value.storefrontWithdrawalFeeRate = result2.storefrontWithdrawalFeeRate);
            const result = await getAccountList({ ...filterParams });
            cardList.value = result.records;
            fillDefaultAccount();
        }
        try {
            const dual = await getDualBalance();
            const channels = dual?.channelBalances || {};
            onlineChannel.value = dual?.onlineChannel && channels[dual.onlineChannel] !== undefined
                ? dual.onlineChannel
                : Object.keys(channels)[0];
            onlineBalance.value = Number(channels[onlineChannel.value || ""] ?? 0);
            if (dual?.shopMoney !== undefined && dual?.shopMoney !== null) {
                accountData.value.shopMoney = Number(dual.shopMoney);
            }
        } catch {
            onlineChannel.value = undefined;
            onlineBalance.value = 0;
        }
        if (source.value === "online") {
            try {
                const application = await getCurrentMerchantApplication(
                    onlineChannel.value ? { payChannel: onlineChannel.value } : undefined
                );
                onboardingAccount.value = pickOnboardingBankAccount(application);
                onboardingFinished.value = String(application?.status || "").toUpperCase() === "FINISH";
            } catch {
                onboardingAccount.value = null;
                onboardingFinished.value = false;
            }
        }
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};
onMounted(() => {
    loadFilter();
});

const handleChange = (e: any) => {
    formState.value.accountData = cardList.value.find((card: any) => card.accountId === e) as accountForm;
};

const moneyRules = computed(() => {
    return [
        { required: true, message: t("请输入提现金额!"), trigger: "blur" },
        {
            validator: (rule: any, value: any, callback: any) => {
                if (value > currentBalance.value) {
                    callback(new Error(t("提现金额不得大于可用余额")));
                    return;
                }
                if (value <= 0) {
                    callback(new Error(t("提现金额不得小于0")));
                    return;
                }
                callback();
            },
            trigger: "blur"
        }
    ];
});
const submitting = ref<boolean>(false);
const onSubmit = async () => {
    if (submitting.value) return;
    submitting.value = true;
    try {
        await formRef.value.validate();
    } catch {
        submitting.value = false;
        return;
    }
    try {
        const withdrawType = source.value === "online" ? 2 : 1;
        const payload: Record<string, any> = { ...formState.value, withdrawType };
        if (withdrawType === 2) {
            if (!onlineAccountReady.value || !onlineChannel.value) {
                message.error(t("请先完成进件并填写结算银行账户"));
                return;
            }
            payload.payChannel = onlineChannel.value;
            delete payload.merchantAccountId;
            delete payload.accountData;
        }
        await createWithdraw(payload);
        await formRef.value.resetFields();
        message.success(t("操作成功"));
        formState.value.amount = "";
        formState.value.merchantAccountId = "";
        formState.value.accountData = {} as accountForm;
        loadFilter();
    } catch (error: any) {
        message.error(error.message);
    } finally {
        submitting.value = false;
    }
};
</script>
<style lang="less" scoped>
.withdraw-form {
    margin-top: 10px;
    .tips {
        padding: 10px;
        background-color: #f5f5f5;
        margin-bottom: 20px;
    }
    .dual-balance-bar {
        display: flex;
        gap: 16px;
        margin-bottom: 20px;
        .balance-item {
            flex: 0 0 auto;
            min-width: 160px;
            padding: 14px 18px;
            background: #f5f7fa;
            border-radius: 6px;
            display: flex;
            flex-direction: column;
            gap: 6px;
            .balance-label {
                color: #999;
                font-size: 12px;
            }
            .balance-value {
                color: #f5222d;
                font-size: 20px;
                font-weight: bold;
            }
        }
    }
    .online-account-hint {
        display: inline-block;
        margin-left: 12px;
        vertical-align: middle;
    }
}
</style>
