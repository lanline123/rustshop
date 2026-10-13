<template>
    <tig-layout :title="$t('注册')">
        <view class="register-container">
            <view class="register-warp">
                <view class="h1_tit">{{ $t("会员注册") }}</view>
            </view>
            <view class="profile-edit-content">
                <uni-forms ref="formRef" :model-value="formState" :rules="formRules" label-align="right">
                    <template v-if="formState.registType === 'email'">
                        <uni-forms-item label="" name="email">
                            <view class="input mobileareacode-box">
                                <uni-easyinput
                                    v-model="formState.email"
                                    :input-border="false"
                                    focus
                                    class="uni-input item-input"
                                    placeholder-style="font-size: 28rpx;"
                                    placeholder-class="placeholder"
                                    :placeholder="$t('请输入邮箱')"
                                    primary-color="rgb(192, 196, 204)"
                                />
                            </view>
                        </uni-forms-item>

                        <uni-forms-item label="" name="emailCode">
                            <view class="item-one">
                                <uni-easyinput
                                    v-model="formState.emailCode"
                                    :input-border="false"
                                    class="uni-input item-input input"
                                    placeholder-style="font-size: 28rpx;"
                                    placeholder-class="placeholder"
                                    :placeholder="$t('邮箱验证码')"
                                    primary-color="rgb(192, 196, 204)"
                                />
                                <VerificationCode
                                    v-model:is-checked="isChecked"
                                    v-model:verify-token-data="verifyToken"
                                    style="height: 65rpx; width: 200rpx; flex-shrink: 0; margin-left: 20rpx"
                                    :email="formState.email"
                                    :request-api="sendEmailCode"
                                    @mobile-error-callback="mobileErrorCallback"
                                />
                            </view>
                        </uni-forms-item>
                    </template>
                    <template v-else>
                        <uni-forms-item label="" name="mobile">
                            <view class="input mobileareacode-box">
                                <template v-if="configStore.isOpenMobileAreaCode">
                                    <tig-mobile-areacode
                                        v-model="mobileAreaCode"
                                        :label-style="{ fontSize: '28rpx' }"
                                        :name-style="{ fontSize: '24rpx', padding: '0 2rpx 0 10rpx' }"
                                        :box-style="{ paddingRight: '0' }"
                                        :icon-style="{ fontSize: '22rpx' }"
                                    />
                                </template>
                                <uni-easyinput
                                    v-model="formState.mobile"
                                    :input-border="false"
                                    class="uni-input item-input"
                                    focus
                                    placeholder-style="font-size: 28rpx;"
                                    placeholder-class="placeholder"
                                    :placeholder="$t('请输入手机号')"
                                    primary-color="rgb(192, 196, 204)"
                                />
                            </view>
                        </uni-forms-item>

                        <uni-forms-item label="" name="mobileCode">
                            <view class="item-one">
                                <uni-easyinput
                                    v-model="formState.mobileCode"
                                    :input-border="false"
                                    class="uni-input item-input input"
                                    placeholder-style="font-size: 28rpx;"
                                    placeholder-class="placeholder"
                                    :placeholder="$t('手机验证码')"
                                    primary-color="rgb(192, 196, 204)"
                                />
                                <VerificationCode
                                    v-model:is-checked="isChecked"
                                    v-model:verify-token-data="verifyToken"
                                    style="height: 65rpx; width: 200rpx; flex-shrink: 0; margin-left: 20rpx"
                                    :mobile="configStore.isOpenMobileAreaCode ? mobileAreaCode + formState.mobile : formState.mobile"
                                    :request-api="sendMobileCode"
                                    @mobile-error-callback="mobileErrorCallback"
                                />
                            </view>
                        </uni-forms-item>
                    </template>

                    <uni-forms-item label="" name="password">
                        <view class="input">
                            <uni-easyinput
                                v-model="formState.password"
                                :clearable="false"
                                :input-border="false"
                                :trim="true"
                                type="password"
                                class="uni-input item-input"
                                placeholder-style="font-size: 28rpx;"
                                placeholder-class="placeholder"
                                :placeholder="$t('请输入密码')"
                                primary-color="rgb(192, 196, 204)"
                            />
                        </view>
                    </uni-forms-item>
                    <uni-forms-item label="" name="">
                        <view class="btn">
                            <template v-if="configStore.openEmailRegister === 1">
                                <view class="change-btn" @click="registerTypeChange(formState.registType === 'email' ? 'mobile' : 'email')">
                                    {{ formState.registType === "email" ? $t("切换为手机号注册") : $t("切换为邮箱注册") }}
                                </view>
                            </template>
                            <view class="btn-text" @click.stop="handleLink('/pages/login/index')">
                                <view>{{ $t("已有账号？") }}</view>
                            </view>
                        </view>
                    </uni-forms-item>
                </uni-forms>
                <view class="form-con">
                    <tig-button :disabled="isRegisterDisabled" :loading="loginLoading" class="btn2-css3" @click="onRegister"> {{ $t("注 册") }} </tig-button>
                </view>
            </view>
            <view class="agreement-btn">
                <tig-checkbox v-model:checked="isChecked" />
                <text class="tip-txt" @click="isChecked = !isChecked">{{ $t("我已阅读并同意") }}</text>
                <text class="agreement-text special-color" @click.stop="handleLink('/pages/register/agreement')">{{ $t("《注册协议》") }} </text>
                <text class="special-color" @click.stop="handleLink('/pages/login/mallAgreement?articleSn=fwxy')">《{{ $t("服务协议") }}》</text>
                <text class="special-color" @click.stop="handleLink('/pages/login/mallAgreement?articleSn=ysxy')">《{{ $t("隐私协议") }}》</text>
            </view>
        </view>
    </tig-layout>
</template>

<script lang="ts" setup>
import { computed, ref, shallowRef, nextTick } from "vue";
import { onShow } from "@dcloudio/uni-app";
import { sendMobileCode, sendEmailCode } from "@/api/login/login";
import VerificationCode from "@/components/verificationCode/index.vue";
import { userRegist } from "@/api/user/regist";
import { useI18n } from "vue-i18n";
import { useConfigStore } from "@/store/config";
import { useResettableRef } from "@/hooks";
import { redirect } from "@/utils";

const configStore = useConfigStore();

const mobileAreaCode = ref("86");

const [formState, reset] = useResettableRef({
    mobile: "",
    mobileCode: "",
    password: "",
    registType: "",
    email: "",
    emailCode: ""
});

const { t } = useI18n();

const isChecked = ref(false);
const verifyToken = ref("");
const loginLoading = ref(false);
const isRegisterDisabled = computed(() => {
    return formState.value.registType === "email"
        ? !formState.value.email || !formState.value.emailCode || !formState.value.password || !verifyToken.value
        : !formState.value.mobile || !formState.value.mobileCode || !formState.value.password || !verifyToken.value;
});

const registerTypeChange = (type: string) => {
    reset();
    formState.value.registType = type;
};

const validateMobile = (rule: any, value: any, data: any, callback: any) => {
    if (!value) {
        return callback(t("手机号不能为空"));
    } else if (/^(?=.*\D).+$/.test(value)) {
        return callback(t("格式错误，请输入正确的手机号码"));
    } else {
        callback();
    }
};

const validateCode = (rule: any, value: any, data: any, callback: any) => {
    if (!value) {
        return callback(t("验证码不能为空"));
    } else if (value.length != 6) {
        return callback(t("请输入6位验证码"));
    } else {
        callback();
    }
};

const validatePassword = (rule: any, value: any, data: any, callback: any) => {
    if (!value) {
        return callback(t("密码不能为空"));
    } else if (value.length > 20 || value.length < 6) {
        return callback(t("密码应为6-20位字符"));
    } else if (/\s/.test(value)) {
        return callback(t("密码中不允许有空格"));
    } else if (/^\d+$/.test(value)) {
        return callback(t("密码不能全为数字"));
    } else if (!/^(?=.*[\d\W]).+$/.test(value)) {
        return callback(t("密码不能全为字母"));
    } else if (!/^(?=.*[a-zA-Z0-9]).+$/.test(value)) {
        return callback(t("密码不能全为符号"));
    } else if (!/^(?!([a-zA-Z0-9])\1*$).+$/.test(value)) {
        return callback(t("密码不能全为相同字符或数字"));
    } else {
        callback();
    }
};

const formRules = {
    mobile: {
        rules: [
            { required: true, errorMessage: t("请输入手机号") },
            {
                validateFunction: validateMobile
            }
        ]
    },
    mobileCode: {
        rules: [
            { required: true, errorMessage: t("请输入验证码") },
            {
                validateFunction: validateCode
            }
        ]
    },
    password: {
        rules: [
            { required: true, errorMessage: t("请输入密码") },
            {
                validateFunction: validatePassword
            }
        ]
    }
};

const formRef = shallowRef();

const onRegister = async () => {
    formRef.value
        .validate()
        .then(async () => {
            try {
                loginLoading.value = true;
                const bindSalesmanId = uni.getStorageSync("bindSalesmanId");
                const data: any = {
                    ...formState.value
                };

                if (bindSalesmanId) {
                    data.salesmanId = bindSalesmanId;
                }

                if (formState.value.mobile) {
                    data.mobile = configStore.isOpenMobileAreaCode ? mobileAreaCode.value + formState.value.mobile : formState.value.mobile;
                }

                await userRegist(data);
                uni.removeStorageSync("bindSalesmanId");
                uni.showToast({
                    title: t("注册成功"),
                    duration: 1500,
                    icon: "none"
                });
                setTimeout(() => {
                    // 一秒后执行这里的代码
                    uni.redirectTo({
                        url: "/pages/login/index"
                    });
                }, 1000);
            } catch (error: any) {
                formState.value.emailCode = "";
                formState.value.mobileCode = "";
                uni.showToast({
                    title: error.message,
                    duration: 1500,
                    icon: "none"
                });
            } finally {
                loginLoading.value = false;
            }
        })
        .catch((err: any) => {
            console.error("表单错误信息：", err);
        });
};

onShow(() => {
    if (uni.getStorageSync("token")) {
        uni.reLaunch({
            url: "/pages/index/index"
        });
    }
    nextTick(() => {
        formRef.value.setRules(formRules);
    });
});

const mobileErrorCallback = (msg: string) => {
    uni.showToast({
        title: msg,
        duration: 1500,
        icon: "none"
    });
};

const handleLink = (url: string) => {
    if (!url) return;
    redirect({
        url: url
    });
};
</script>
<style>
page {
    background: #fff !important;
}
</style>
<style lang="scss" scoped>
.mobileareacode-box {
    display: flex;
    align-items: center;

    .tem-input {
        flex: 1;
    }
}

.change-btn {
    display: flex;
    justify-content: center;
    cursor: pointer;
    color: var(--general);
    font-size: 24rpx;
}

.register-container {
    height: 100%;

    .register-warp {
        display: flex;
        flex-direction: column;
        padding-left: 20rpx;

        .h1_tit {
            font-size: 36rpx;
            padding: 40rpx;
            font-weight: bold;
            color: #333333;
        }
    }

    .tab-row {
        display: flex;
        align-items: end;
        gap: 80rpx;
        color: #999999;
        font-size: 34rpx;
        margin-bottom: 40rpx;
        margin: 0 60rpx;
        padding: 20rpx 0;

        & .active {
            color: #333333;
            font-size: 44rpx;
            font-weight: bold;
        }
    }

    .profile-edit-content {
        margin: 0 60rpx;
        border-radius: 15rpx;
        padding: 20rpx 0;
        display: flex;
        flex-direction: column;
        gap: 16rpx;

        .item-one {
            display: flex;
            justify-content: space-between;
            flex-direction: row;
            align-items: center;
        }

        .input {
            flex: 1;
            width: auto;
            padding-bottom: 10rpx;
            border-bottom: 1px solid #dfdfdf;
        }

        .btn {
            display: flex;
            flex-direction: row;
            align-items: center;

            .btn-text {
                margin-left: auto;
                font-size: 24rpx;
                cursor: pointer;
            }
        }
    }
}

:deep(.is-input-border) {
    border: none; /* 首先清除所有边框 */
    border-radius: 0;
    border-bottom: 0.05rem solid;
}

.special-color {
    color: var(--general);
}

.placeholder {
    font-size: 28rpx;
    font-weight: normal;
}

.agreement-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    padding-top: 30rpx;
    font-size: 24rpx;
    flex-wrap: wrap;

    .tip-txt {
        white-space: nowrap;
    }
}
</style>
