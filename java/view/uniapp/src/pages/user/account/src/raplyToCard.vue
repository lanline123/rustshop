<template>
    <view v-if="tabs.length > 0" class="custom-tabs">
        <view
            v-for="(tab, index) in tabs"
            :key="index"
            :class="{ 'active-tab': activeTab === tab.accountType }"
            class="tab"
            @click="actionClick(tab.accountType)"
        >
            <view>{{ $t(tab.title) }}</view>
        </view>
    </view>
    <view v-if="tabs.length === 0" class="empty-method">{{ $t("暂无可用提现方式") }}</view>
    <view v-if="tabs.length > 0" class="reply-main">
        <view class="reply-content">
            <up-form ref="formRef" :model="formState" label-width="170rpx" error-type="toast">
                <up-form-item :label="$t('提现至')">
                    <up-picker :show="show" :columns="dataList" key-name="text" @confirm="confirm" @cancel="cancel" />
                    <up-button style="justify-content: flex-start" @click="show = true">
                        <view class="account-picker-trigger">
                            <text class="account-picker-text">{{ selectedData.text }}</text>
                            <text :class="['iconfont-h5', 'icon-a-gengduo20', { 'is-open': show }]" />
                        </view>
                    </up-button>
                </up-form-item>
                <view class="tips"
                    >{{ $t("当前") }}{{ accountPlaceholder }}{{ $t("账号已不用") }}？<navigator
                        url="/pages/user/account/cardManagement/list"
                        class="font-color"
                        >{{ $t("点击这里") }}</navigator
                    >{{ $t("去添加新的") }}{{ accountPlaceholder }}{{ $t("账号") }}。</view
                >
                <up-form-item :label="$t('提现姓名')" prop="accountData.accountName">
                    <up-input
                        v-model="formState.accountData.accountName"
                        :placeholder="$t('请选择') + accountPlaceholder + $t('账号')"
                        disabled
                        font-size="12"
                    />
                </up-form-item>
                <up-form-item v-if="formState.accountData.accountType === 1" :label="$t('银行详情')" prop="accountData.bankName">
                    <up-textarea v-model="formState.accountData.bankName" :placeholder="$t('请选择银行卡账号')" disabled font-size="12" />
                </up-form-item>
                <up-form-item :required="true" :label="$t('提现金额')" prop="amount">
                    <up-input v-model="formState.amount" type="number" :placeholder="$t('请输入提现金额')" clearable font-size="12" />
                </up-form-item>
            </up-form>
        </view>
    </view>
    <tig-fixed-placeholder background-color="#fff">
        <view class="btn-box">
            <tig-button class="btn" plain @click="backDetail"> {{ $t("返回列表") }} </tig-button>
            <tig-button v-if="tabs.length > 0" class="btn" @click="onSubmit"> {{ $t("提交申请") }} </tig-button>
        </view>
    </tig-fixed-placeholder>
</template>

<script lang="ts" setup>
/**
 * 余额提现组件
 * - 支持银行卡、支付宝、微信三种提现方式
 * - 选择已绑定的账号进行提现
 * - 输入提现金额提交申请
 */
import { ref, shallowRef, computed, nextTick, onBeforeMount } from "vue";
import { onShow } from "@dcloudio/uni-app";
import { getAccountNoList, updateWithdrawApply, updateCommissionWithdrawApply } from "@/api/user/account";
import type { AccountFormState, AccountInfo } from "@/types/user/account";
import { useI18n } from "vue-i18n";
import { isDemo } from "@/utils";
import { storeToRefs } from "pinia";
import { useConfigStore } from "@/store/config";

const { t } = useI18n();

const props = defineProps({
    balance: {
        type: [Number, String],
        default: 0
    },
    // D1107：钱包类型，balance=余额提现 commission=佣金提现
    wallet: {
        type: String,
        default: "balance"
    }
});
// 按钱包类型选择提交接口
const submitApi = props.wallet === "commission" ? updateCommissionWithdrawApply : updateWithdrawApply;

// D1107：提现到账方式按后台「提现设置」勾选项展示（余额/佣金共用该配置），未配置则全显示
const { memberWithdrawalReceiptMethod } = storeToRefs(useConfigStore());
const allTabs = [
    { title: t("银行卡"), accountType: 1 },
    { title: t("支付宝"), accountType: 2 },
    { title: t("微信"), accountType: 3 }
];
const tabs = computed(() => {
    // 佣金提现为分销核心能力，不受后台「余额提现方式」勾选限制，展示全部账户类型（D1107）
    if (props.wallet === "commission") return allTabs;
    const enabled = memberWithdrawalReceiptMethod.value;
    // 配置字段缺失（后端未下发）才兜底全显示；空数组=后台全部取消勾选=一个都不展示
    if (!Array.isArray(enabled)) return allTabs;
    return allTabs.filter((t) => enabled.includes(t.accountType));
});
const formRef = shallowRef();
const activeTab = ref(tabs.value[0]?.accountType ?? 1);
const actionClick = async (value: number) => {
    if (value === activeTab.value) return;
    activeTab.value = value;
    formState.value.accountData.accountType = value;
    yhkList.value = [];
    zfbList.value = [];
    wxList.value = [];
    resetForm();
    __getAccountNoList(value);
};
const resetForm = () => {
    selectedData.value = {
        id: "",
        text: t("请选择")
    };
    formState.value = {
        accountData: {
            accountNo: "",
            accountName: "",
            accountType: activeTab.value,
            bankName: ""
        },
        amount: ""
    };
};

const show = ref(false);
const selectedData = ref({
    id: "",
    text: t("请选择")
});
const cancel = () => {
    show.value = false;
};
const confirm = (e: any) => {
    show.value = false;
    selectedData.value.id = e.value[0].id;
    selectedData.value.text = e.value[0].text;
    upAcData(e.value[0].id);
};

const upAcData = (value: any) => {
    const list: any = formState.value.accountData.accountType === 1 ? yhkList : formState.value.accountData.accountType === 2 ? zfbList : wxList;
    let selectedItem: any = list.value[0].find((item: any) => item.accountId === value);
    formState.value.accountData.accountNo = selectedItem?.accountNo;
    formState.value.accountData.accountName = selectedItem?.accountName;
    formState.value.accountData.bankName = selectedItem?.bankName;
};

const yhkList = ref<AccountInfo[]>([]);
const zfbList = ref<AccountInfo[]>([]);
const wxList = ref<AccountInfo[]>([]);
const __getAccountNoList = async (type: number) => {
    uni.showLoading({
        title: t("加载中")
    });
    try {
        let temp: any = {
            accountType: type
        };
        const result = await getAccountNoList({ ...temp });
        let arr: any = [];
        if (type == 1) {
            arr = result.records.map((item: any) => ({
                id: item.accountId,
                text: item.accountNo,
                ...item
            }));
            yhkList.value.push(arr);
        } else if (type === 2) {
            arr = result.records.map((item: any) => ({
                id: item.accountId,
                text: item.accountNo,
                ...item
            }));

            zfbList.value.push(arr);
        } else {
            arr = result.records.map((item: any) => ({
                id: item.accountId,
                text: item.accountNo,
                ...item
            }));

            wxList.value.push(arr);
        }
    } catch (error: any) {
        console.error(error.message);
    } finally {
        uni.hideLoading();
    }
};
onBeforeMount(() => {
    __getAccountNoList(activeTab.value);
});

const formState = ref<AccountFormState>({
    accountData: {
        accountNo: "",
        accountName: "",
        accountType: activeTab.value,
        bankName: ""
    },
    amount: ""
});

const accountPlaceholder = computed(() => {
    switch (formState.value.accountData.accountType) {
        case 1:
            return t("银行卡");
        case 2:
            return t("支付宝");
        case 3:
            return t("微信");
        default:
            return "";
    }
});

const dataList = computed(() => {
    switch (formState.value.accountData.accountType) {
        case 1:
            return yhkList.value;
        case 2:
            return zfbList.value;
        case 3:
            return wxList.value;
        default:
            return [];
    }
});

const rules = {
    amount: {
        required: true,
        validator: (rule: any, value: any, callback: any) => {
            if (value === "" || value === null || value === undefined) {
                return false;
            }
            if (value == 0) {
                uni.$u.toast(t("提现金额必须大于0"));
            }
            return value > 0;
        },
        message: t("请输入提现金额"),
        trigger: ["blur"]
    }
};

onShow(() => {
    nextTick(() => {
        formRef.value.setRules(rules);
    });
});

const onSubmit = async () => {
    if (isDemo()) return uni.showToast({ title: t("权限不足"), icon: "none" });
    if (selectedData.value.id.length === 0) {
        uni.showToast({
            title: t("请选择需要提现的账号"),
            icon: "none",
            duration: 1000
        });
        return;
    }
    formRef.value
        .validate()
        .then(async (valid: boolean) => {
            if (valid) {
                try {
                    const result = await submitApi({ ...formState.value });
                    uni.showToast({
                        title: t("操作成功"),
                        icon: "success",
                        duration: 1000
                    });
                    setTimeout(() => {
                        backDetail(true);
                    }, 1500);
                } catch (error: any) {
                    console.error(error);
                    uni.$u.toast(error.message);
                }
            }
        })
        .catch(() => {});
};

const emit = defineEmits(["backDetail"]);
const backDetail = (needRefresh = false) => {
    emit("backDetail", needRefresh);
};
</script>
<style lang="scss" scoped>
.empty-method {
    text-align: center;
    color: #999;
    font-size: 26rpx;
    padding: 80rpx 40rpx;
}
.custom-tabs {
    display: flex;
    justify-content: center;
    flex-direction: row;
    gap: 100rpx;
    margin: 20rpx;
    .tab {
        padding: 10px;
        width: 100%;
        font-size: 28rpx;
        border-bottom: none;
        color: black;
        text-align: center;
    }
    & .active-tab {
        position: relative;
        color: var(--general);
        &::before {
            content: "";
            position: absolute;
            width: 30%;
            height: 2px;
            background-color: var(--general);
            left: 50%;
            transform: translateX(-50%);
            bottom: 0;
        }
    }
}

.reply-main {
    padding: 20rpx;
    .reply-content {
        background-color: #fff;
        border-radius: 10rpx;
        padding: 20rpx;
        margin-bottom: 30rpx;
        .tips {
            font-size: 12px;
            color: #a5a5a5;
            padding-left: 170rpx;
            navigator {
                display: inline;
            }
        }
    }
}

.account-picker-trigger {
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: space-between;

    .icon-a-gengduo20 {
        font-size: 24rpx;
        color: #999;
        transform: rotate(90deg);
        transition: transform 0.2s ease;
    }

    .is-open {
        transform: rotate(270deg);
    }
}

.account-picker-text {
    flex: 1;
    text-align: left;
}

:deep(.u-textarea__field) {
    font-size: 24rpx;
}

.btn-box {
    padding: 25rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    height: 100%;
    gap: 20rpx;

    .btn {
        width: 48%;
        font-size: 28rpx;
    }
}
</style>
