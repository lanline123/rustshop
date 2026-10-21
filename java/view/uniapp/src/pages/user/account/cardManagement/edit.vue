<template>
    <tig-layout :title="navbarTitle">
        <view class="card-edit-main">
            <uni-forms ref="formRef" :model-value="formState" label-width="170rpx">
                <view class="card-edit-content">
                    <uni-forms-item v-if="!id" :label="$t('卡片类型')" name="accountType" required>
                        <picker :range="accountTypeList" :value="selectedAccountType" mode="selector" @change="acTypeChange">
                            <view class="card-idx picker-arrow">{{ accountTypeList[selectedAccountType] }}</view>
                        </picker>
                    </uni-forms-item>
                    <uni-forms-item v-else :label="$t('卡片类型')" name="accountType" required>
                        <uni-easyinput :input-border="false" :placeholder="accountPlaceholder" disabled />
                    </uni-forms-item>
                    <uni-forms-item :label="$t('真实姓名')" name="accountName" required>
                        <uni-easyinput
                            v-model="formState.accountName"
                            :input-border="false"
                            :placeholder="$t('请输入真实姓名')"
                            primary-color="rgb(192, 196, 204)"
                        />
                    </uni-forms-item>
                    <uni-forms-item :label="$t('身份证号')" name="identity" required>
                        <uni-easyinput
                            v-model="formState.identity"
                            :input-border="false"
                            :placeholder="$t('请输入身份证号码')"
                            primary-color="rgb(192, 196, 204)"
                        />
                    </uni-forms-item>
                    <uni-forms-item :label="$t('{0}号', [accountPlaceholder])" name="accountNo" required>
                        <uni-easyinput
                            v-model="formState.accountNo"
                            :input-border="false"
                            :placeholder="$t('请输入{0}账号', [accountPlaceholder])"
                            primary-color="rgb(192, 196, 204)"
                        />
                    </uni-forms-item>
                    <uni-forms-item v-if="formState.accountType === 1" :label="$t('银行卡详情')" name="bankName">
                        <uni-easyinput
                            v-model="formState.bankName"
                            :input-border="false"
                            :placeholder="$t('请输入银行卡详情')"
                            primary-color="rgb(192, 196, 204)"
                        />
                    </uni-forms-item>
                </view>
            </uni-forms>
        </view>
        <tig-fixed-placeholder background-color="#fff">
            <view class="btn-box">
                <tig-button class="btn" @click="onSubmit"> {{ id ? $t("保存修改") : $t("新增账号") }} </tig-button>
            </view>
        </tig-fixed-placeholder>
    </tig-layout>
</template>

<script setup lang="ts">
import { ref, shallowRef, computed, nextTick } from "vue";
import { onLoad, onShow } from "@dcloudio/uni-app";
import { getAccount, updateAccount } from "@/api/user/account";
import type { AccountData } from "@/types/user/account";
import { useI18n } from "vue-i18n";

const { t } = useI18n();

const navbarTitle = ref(t("添加账号"));

const id = ref();
const selectedAccountType = ref(0);
const accountTypeList = ref([t("银行卡"), t("支付宝"), t("微信")]);
const acTypeChange = (e: any) => {
    selectedAccountType.value = e.detail.value;
    formState.value.accountType = e.detail.value + 1;
    resetForm();
};
const resetForm = () => {
    formState.value = {
        accountName: "",
        identity: "",
        accountNo: "",
        accountType: Number(selectedAccountType.value) + 1,
        bankName: ""
    };
};

const formState = ref<AccountData>({
    accountType: 1
});
const accountPlaceholder = computed(() => {
    switch (formState.value.accountType) {
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

const __getAccount = async () => {
    uni.showLoading({
        title: t("加载中")
    });
    try {
        const result = await getAccount({ accountId: id.value });
        selectedAccountType.value = result.accountType - 1;
        Object.assign(formState.value, result);
    } catch (error: any) {
        console.error(error.message);
    } finally {
        uni.hideLoading();
    }
};

const rules = {
    accountName: {
        rules: [{ required: true, errorMessage: t("请输入真实姓名") }]
    },
    identity: {
        rules: [
            { required: true, errorMessage: t("请输入身份证号码") },
            {
                validateFunction: function (rule: any, value: any, data: any, callback: any) {
                    const regex = /^[1-9]\d{5}[1-9]\d{3}((0\d)|(1[0-2]))(([0|1|2]\d)|3[0-1])\d{3}([0-9]|X)$/;
                    const status = regex.test(value);
                    if (!status) {
                        callback(t("请输入正确的身份证号码"));
                    }
                    return true;
                }
            }
        ]
    },
    accountNo: {
        rules: [
            { required: true, errorMessage: t("请输入正确的{0}账号", [accountPlaceholder.value]) },
            {
                validateFunction: function (rule: any, value: any, data: any, callback: any) {
                    const regex = /^\d{6,}$/;
                    const status = regex.test(value);
                    if (!status && formState.value.accountType === 1) {
                        callback(t("请输入正确的{0}账号", [accountPlaceholder.value]));
                    }
                    return true;
                }
            }
        ]
    }
};

const formRef = shallowRef();
const onSubmit = (values: any) => {
    formRef.value
        .validate()
        .then(() => {
            if (id.value) {
                edit();
            } else {
                add();
            }
        })
        .catch((err: any) => {
            console.error("表单错误信息：", err);
        });
};

const add = async () => {
    try {
        const result = await updateAccount({ ...formState.value }, "createAccount");
        uni.showToast({
            title: t("添加成功"),
            icon: "success",
            duration: 1000
        });
        setTimeout(() => {
            uni.navigateBack({
                success: function (res) {
                    uni.$emit("refreshData", { type: formState.value.accountType }); // 发送刷新信号
                }
            });
        }, 1000);
    } catch (error: any) {
        uni.showToast({
            title: error.message,
            icon: "none",
            duration: 1000
        });
    }
};

const edit = async () => {
    try {
        const result = await updateAccount({ ...formState.value }, "updateAccount");
        if (result.message) {
            uni.showToast({
                title: result.message,
                icon: "success",
                duration: 1000
            });
        }
        setTimeout(() => {
            uni.navigateBack({
                success: function (res) {
                    uni.$emit("refreshData", { type: formState.value.accountType }); // 发送刷新信号
                }
            });
        }, 1000);
    } catch (error: any) {
        uni.showToast({
            title: error.message,
            icon: "none",
            duration: 1000
        });
    }
};

onShow(() => {
    nextTick(() => {
        formRef.value.setRules(rules);
    });
});

onLoad((option: any) => {
    if (option.id) {
        id.value = option.id;
        navbarTitle.value = t("编辑账号");
        __getAccount();
    } else if (option.accountType) {
        const type = Number(option.accountType);
        selectedAccountType.value = type - 1;
        formState.value.accountType = type;
    }
});
</script>

<style lang="scss" scoped>
:deep(.uni-forms-item) {
    margin-bottom: 30rpx;
}

:deep(.uni-forms-item:last-child) {
    margin-bottom: 0;
}

:deep(.uni-forms-item__label) {
    font-size: 26rpx;
}

:deep(.uni-forms-item__error) {
    top: 90%;
    right: 18rpx;
    text-align: right;
}

:deep(.uni-input-input) {
    text-align: right;
}

:deep(.uni-easyinput__placeholder-class) {
    font-size: 26rpx;
    text-align: right;
}

:deep(.is-disabled) {
    background-color: #fff !important;
}

:deep(.icon-calendar) {
    display: none;
}

:deep(.uni-date__x-input) {
    text-align: right;
    color: #333;
}

.card-edit-main {
    padding: 20rpx;
    .card-edit-content {
        background-color: #fff;
        border-radius: 10rpx;
        padding: 20rpx;
        margin-bottom: 30rpx;
        .card-idx {
            line-height: 78rpx;
            text-align: right;
        }
        .picker-arrow {
            position: relative;
            padding-right: 28rpx;
            &::after {
                content: "";
                position: absolute;
                top: 50%;
                right: 6rpx;
                width: 14rpx;
                height: 14rpx;
                border-top: 2rpx solid currentColor;
                border-right: 2rpx solid currentColor;
                transform: translateY(-50%) rotate(45deg);
            }
        }
    }
}

.card-edit-btn {
    background: var(--general);
    color: #fff;
    &::after {
        border: 0;
    }
}

.btn-box {
    padding: 25rpx;

    .btn {
        font-size: 28rpx;
    }
}
</style>
