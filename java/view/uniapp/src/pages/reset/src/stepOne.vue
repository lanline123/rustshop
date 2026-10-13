<template>
    <view v-if="formState.registType === 'mobile'">
        <view class="item">
            <view class="text mobileareacode-box">
                <template v-if="configStore.isOpenMobileAreaCode">
                    <tig-mobile-areacode v-model="mobileAreaCode" />
                </template>
                <input type="text" :value="mobile" :placeholder="$t('请输入手机号')" placeholder-class="placeholder" class="mobile-text" @input="mobileInput" />
            </view>
        </view>
        <view class="item mobile_code_text">
            <view style="flex: 1; margin-right: 10rpx">
                <input
                    type="text"
                    :placeholder="$t('手机短信验证码')"
                    placeholder-class="placeholder"
                    :value="mobileCode"
                    name="mobile_code"
                    class="text"
                    @input="mobileCodeInput"
                />
            </view>
            <verificationCode
                v-model:is-checked="isChecked"
                v-model:verify-token-data="verifyToken"
                style="height: 65rpx"
                :mobile="configStore.isOpenMobileAreaCode ? mobileAreaCode + mobile : mobile"
                event="forgetPassword"
                :request-api="sendMobileCode"
                @mobile-error-callback="mobileErrorCallback"
            />
        </view>
    </view>
    <view v-else>
        <view class="item">
            <view class="text mobileareacode-box">
                <input
                    type="text"
                    :placeholder="$t('请输入邮箱')"
                    placeholder-class="placeholder"
                    :value="email"
                    name="mobile_code"
                    class="mobile-text"
                    @input="inputEmail"
                />
            </view>
        </view>
        <view class="item mobile_code_text">
            <view style="flex: 1; margin-right: 10rpx">
                <input
                    type="text"
                    :placeholder="$t('邮箱验证码')"
                    placeholder-class="placeholder"
                    :value="emailCode"
                    name="mobile_code"
                    class="text"
                    @input="inputMobileCode"
                />
            </view>
            <verificationCode
                v-model:is-checked="isChecked"
                v-model:email="email"
                v-model:verify-token-data="verifyToken"
                event="forgetPassword"
                style="height: 65rpx"
                :request-api="sendEmailCode"
                @mobile-error-callback="mobileErrorCallback"
            />
        </view>
    </view>
    <view class="btn2-css3">
        <tig-button :loading-text="$t('下 一 步')" :loading="isLoading" @click="handleNext">
            {{ $t("下 一 步") }}
        </tig-button>
    </view>
    <view :class="['bottom-bar', { 'bottom-bar-single': configStore.openEmailRegister !== 1 }]">
        <template v-if="configStore.openEmailRegister === 1">
            <view class="change-btn" @click="registerTypeChange(formState.registType === 'email' ? 'mobile' : 'email')">
                {{ formState.registType === "email" ? $t("手机号找回") : $t("邮箱找回") }}
            </view>
        </template>
        <view class="login-text" @click="handleToLogin">{{ $t("立即登录") }}</view>
    </view>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { useI18n } from "vue-i18n";
import { useConfigStore } from "@/store/config";
import { sendMobileCode, checkMobile, sendEmailCode, checkEmail } from "@/api/login/login";
import VerificationCode from "@/components/verificationCode/index.vue";

const mobileKey = defineModel("mobileKey");
const step = defineModel("step");
const configStore = useConfigStore();
const { t } = useI18n();

const mobileAreaCode = ref("86");
const isChecked = ref(true);
const verifyToken = ref("");
const email = ref("");
const emailCode = ref("");
const inputEmail = (e: any) => {
    email.value = e.detail.value;
};
const inputMobileCode = (e: any) => {
    emailCode.value = e.detail.value;
};

const mobile = ref("");
const mobileInput = (e: any) => {
    mobile.value = e.detail.value;
};
const mobileCode = ref("");
const mobileCodeInput = (e: any) => {
    mobileCode.value = e.detail.value;
};
const mobileErrorCallback = (msg: string) => {
    uni.showToast({
        title: msg,
        duration: 1500,
        icon: "none"
    });
};
const registerTypeChange = (type: string) => {
    formState.value.registType = type;
    if (type === "email") {
        email.value = "";
        emailCode.value = "";
    } else {
        mobile.value = "";
        mobileCode.value = "";
        mobileAreaCode.value = "86"; // 重置手机号区号为默认值
    }
};
const formState = ref({
    registType: "mobile" // 默认是手机号找回
});
const isLoading = ref(false);
const isDisabled = computed(() => {
    return !mobile.value || !mobileCode.value || isLoading.value;
});

const handleNext = async () => {
    if (formState.value.registType === "email") {
        if (!email.value) {
            return uni.showToast({
                title: t("请输入邮箱"),
                duration: 1500,
                icon: "none"
            });
        }
        if (!emailCode.value) {
            return uni.showToast({
                title: t("请输入邮箱验证码"),
                duration: 1500,
                icon: "none"
            });
        }
    } else {
        if (!mobile.value) {
            return uni.showToast({
                title: t("请输入手机号"),
                duration: 1500,
                icon: "none"
            });
        }
        if (!mobileCode.value) {
            return uni.showToast({
                title: t("请输入手机验证码"),
                duration: 1500,
                icon: "none"
            });
        }
    }
    isLoading.value = true;
    try {
        let temp = {
            mobile: configStore.isOpenMobileAreaCode ? mobileAreaCode.value + mobile.value : mobile.value,
            code: mobileCode.value
        };
        let tempEmail = {
            email: email.value,
            code: emailCode.value
        };
        // 这里根据类型调用不同接口
        let result;
        if (formState.value.registType === "email") {
            result = await checkEmail(tempEmail);
        } else {
            result = await checkMobile(temp);
        }
        mobileKey.value = formState.value.registType === "email" ? result : result;
        step.value = 2;
    } catch (error: any) {
        console.error(error);
        uni.showToast({
            title: error.message,
            duration: 1500,
            icon: "none"
        });
    } finally {
        isLoading.value = false;
    }
};

const handleToLogin = () => {
    uni.navigateTo({
        url: "/pages/login/index"
    });
};
</script>

<style lang="scss" scoped>
.login-text {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 100rpx;
    color: #999;
}
.mobileareacode-box {
    display: flex;
    align-items: center;
    .mobile-text {
        flex: 1;
    }
}
.item {
    margin-bottom: 70rpx;
    position: relative;
    .text {
        border: 0;
        border-radius: 0;
        background-color: transparent;
        padding-left: 0rpx;
        height: 88rpx;
        line-height: normal;
        font-size: 28rpx;
        color: #252525;
        padding: 0;
        font-weight: normal;
        border-bottom: 1rpx solid #eee;
    }
    .get-mobile {
        background-color: transparent;
        padding-left: 0rpx;
        height: 88rpx;
        line-height: normal;
        font-size: 28rpx;
        color: #252525;
        padding: 0;
        font-weight: normal;
        position: absolute;
        top: 0;
        width: 100%;
        z-index: 88;
    }
    .parent {
        position: relative;
    }

    &.mobile_code_text {
        width: 100%;
        /* padding-right: 200rpx; */
        align-items: center;
        display: flex;
    }
}
.change-btn {
    display: flex;
    justify-content: center;
    cursor: pointer;
    color: var(--general);
}
.bottom-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20rpx;
    // background-color: #f9f9f9;
    border-top: 1rpx solid #eee;
}

.bottom-bar-single {
    justify-content: center;
}
</style>
