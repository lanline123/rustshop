<template>
    <tig-layout :title="$t('账户安全')">
        <view class="profile-edit-main">
            <view class="profile-edit-content">
                <view class="item">
                    <view class="title">
                        <view>{{ $t("请输入") }}</view>
                        <view class="info">{{ mobile.replace(/(\d{2})\d*(\d{4})$/, "$1*****$2") }}</view>
                        <view>{{ $t("收到的短信验证码") }}</view>
                    </view>
                </view>
                <uni-forms ref="formRef" :label-width="80" :model-value="formState" :rules="formRules">
                    <uni-forms-item :label="$t('验证码')" name="mobileCode" required>
                        <view class="flex align-center">
                            <view class="item-one input">
                                <uni-easyinput
                                    :input-border="false"
                                    :value="formState.mobileCode"
                                    class="uni-input item-input"
                                    focus
                                    :placeholder="$t('手机短信验证码')"
                                    primary-color="rgb(192, 196, 204)"
                                    @input="inputMobileCode"
                                />
                            </view>
                            <VerificationCode
                                v-model:is-checked="isChecked"
                                v-model:mobile="mobile"
                                v-model:verify-token-data="verifyToken"
                                style="height: 65rpx"
                                :request-api="sendMobileCodeByMobilePassword"
                                :need-mobile="false"
                                @mobile-error-callback="mobileErrorCallback"
                            />
                        </view>
                    </uni-forms-item>
                    <uni-forms-item :label="$t('新密码')" name="password" required>
                        <view class="input">
                            <uni-easyinput
                                :clearable="false"
                                :trim="true"
                                type="password"
                                :input-border="false"
                                :value="formState.password"
                                class="uni-input item-input"
                                :placeholder="$t('请输入新密码')"
                                primary-color="rgb(192, 196, 204)"
                                @input="inputPassword"
                            />
                        </view>
                    </uni-forms-item>
                    <uni-forms-item :label="$t('确认密码')" name="confirmPassword" required>
                        <view class="input">
                            <uni-easyinput
                                :clearable="false"
                                :trim="true"
                                type="password"
                                :input-border="false"
                                :value="formState.confirmPassword"
                                class="uni-input item-input"
                                :placeholder="$t('请再次输入新密码')"
                                primary-color="rgb(192, 196, 204)"
                                @input="inputConfirmPassword"
                            />
                        </view>
                    </uni-forms-item>
                </uni-forms>
                <view class="item">
                    <tig-button :disabled="isRegisterDisabled" class="next" @click="onNext()">{{ $t("提交") }}</tig-button>
                </view>
            </view>
            <view class="profile-edit-content">
                <view class="btn-item">
                    <view class="title">
                        <view class="big-title">{{ $t("为什么要进行身份验证") }}？</view>
                    </view>
                    <view class="info">1. {{ $t("为保障您的账户信息安全，在变更账户中的重要信息时需要进行身份验证，感谢您的理解和支持") }}。</view>
                    <view class="info">2. {{ $t("验证身份遇到问题？您可以通知我们的客服，我们将尽快联系您") }}。</view>
                </view>
            </view>
        </view>
    </tig-layout>
</template>
<script lang="ts" setup>
/**
 * 修改登录密码页
 * - 通过手机验证码验证身份
 * - 设置新密码（6-20位，不能纯数字/纯字母/纯符号）
 * - 修改成功后自动退出登录
 */
import { nextTick, ref, computed } from "vue";
import { onShow } from "@dcloudio/uni-app";
import VerificationCode from "@/components/verificationCode/index.vue";
import { checkModifyPasswordMobileCode, sendMobileCodeByMobilePassword } from "@/api/user/security";
import { onLoad } from "@dcloudio/uni-app";
import { useI18n } from "vue-i18n";
import { useResettableRef } from "@/hooks";
import { useUserStore } from "@/store/user";

const { t } = useI18n();

const userStore = useUserStore();

const mobile = ref("");

const [formState, reset] = useResettableRef({
    mobileCode: "",
    password: "",
    confirmPassword: ""
});

const inputMobileCode = (e: any) => {
    formState.value.mobileCode = e;
};
const inputPassword = (e: any) => {
    formState.value.password = e;
};
const inputConfirmPassword = (e: any) => {
    formState.value.confirmPassword = e;
};

const isRegisterDisabled = computed(() => {
    return !mobile.value || !formState.value.mobileCode || !formState.value.password || !formState.value.confirmPassword || !verifyToken.value;
});

const formRules = {
    mobileCode: { rules: [{ required: true, message: t("请输入验证码"), trigger: "change" }] },
    password: {
        rules: [
            { required: true, errorMessage: t("请输入新密码") },
            {
                validateFunction: function (rule: any, value: any, data: any, callback: any) {
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
                    }
                    return true;
                }
            }
        ]
    },
    confirmPassword: {
        rules: [
            { required: true, errorMessage: t("请输入确认密码") },
            {
                validateFunction: function (rule: any, value: any, data: any, callback: any) {
                    if (!value) {
                        return callback(t("请再次输入密码"));
                    } else if (value != formState.value.password) {
                        return callback(t("两次密码不一致"));
                    }
                    return true;
                }
            }
        ]
    }
};

const verifyToken = ref("");

const isChecked = ref(true);

onLoad((options: any) => {
    mobile.value = options.mobile;
});

const mobileErrorCallback = (msg: string) => {
    uni.showToast({
        title: msg,
        duration: 1500,
        icon: "none"
    });
};

const formRef = ref();

const onNext = async () => {
    formRef.value
        .validate()
        .then(async () => {
            try {
                await checkModifyPasswordMobileCode({
                    password: formState.value.password,
                    code: formState.value.mobileCode
                });

                uni.showToast({
                    title: t("修改成功"),
                    icon: "none",
                    duration: 1000
                });

                setTimeout(() => {
                    userStore.logout();
                }, 1000);
            } catch (error: any) {
                reset();
                if (error.message) {
                    uni.showToast({
                        title: error.message,
                        icon: "none"
                    });
                }
            } finally {
            }
        })
        .catch((err: any) => {
            console.log("表单错误信息：", err);
        });
};

onShow(() => {
    nextTick(() => {
        formRef.value.setRules(formRules);
    });
});
</script>
<style lang="scss" scoped>
.profile-edit-main {
    padding: 30rpx;

    .profile-edit-content {
        background-color: #fff;
        border-radius: 15rpx;
        padding: 20rpx;
        margin-bottom: 30rpx;
        display: flex;
        flex-direction: column;
        gap: 16rpx;

        .item {
            display: flex;
            justify-content: space-between;
            flex-direction: row;
            margin: 20rpx;
            height: 60rpx;

            .next {
                width: 100%;
                height: 100%;
            }

            .title {
                font-size: 24rpx;
                display: flex;
                gap: 16rpx;

                .info {
                    color: #999999;
                }
            }
        }

        .item-one {
            display: flex;
            justify-content: space-between;
            flex-direction: row;
        }

        .btn-item {
            display: flex;
            flex-direction: column;
            margin: 20rpx;
            font-size: 24rpx;
            gap: 16rpx;

            .big-title {
                font-size: 28rpx;
            }

            .info {
                color: #999999;
            }
        }

        .input {
            padding-bottom: 10rpx;
            border-bottom: 1px solid #dfdfdf;
        }
    }
}
</style>
