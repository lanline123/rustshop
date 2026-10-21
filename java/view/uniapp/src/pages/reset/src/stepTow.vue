<template>
    <view class="item">
        <input
            :type="passwordType[0]"
            :placeholder="$t('请输入新密码')"
            placeholder-class="placeholder"
            class="text password-text"
            :value="fristPassword"
            @input="changeFristPassword"
        />
        <view :class="'password_show_hide iconfont ' + (passwordType[0] == 'text' ? 'icon-xianshi' : 'icon-yincang')" @click="passwordTypeChange(0)" />
    </view>
    <view class="item">
        <input
            :type="passwordType[1]"
            :placeholder="$t('请再次确认新密码')"
            placeholder-class="placeholder"
            class="text password-text"
            :value="secondPassword"
            @input="changeSecondPassword"
        />
        <view :class="'password_show_hide iconfont ' + (passwordType[1] == 'text' ? 'icon-xianshi' : 'icon-yincang')" @click="passwordTypeChange(1)" />
    </view>
    <view class="btn2-css3">
        <tig-button :loading-text="$t('确 认')" :loading="isLoading" :disabled="isDisabled" @click="handleResetPassword">
            {{ $t("确 认") }}
        </tig-button>
    </view>
    <view class="login-text" @click="handleToLogin">{{ $t("立即登录") }}</view>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { useI18n } from "vue-i18n";
import { forgetPassword } from "@/api/reset/reset";
const { t } = useI18n();

const mobileKey = defineModel("mobileKey");
const step = defineModel("step");

const passwordType = ref(["password", "password"]);
const passwordTypeChange = (index: number) => {
    passwordType.value[index] = passwordType.value[index] == "password" ? "text" : "password";
};

const fristPassword = ref("");
const changeFristPassword = (e: any) => {
    fristPassword.value = e.detail.value;
};

const secondPassword = ref("");
const changeSecondPassword = (e: any) => {
    secondPassword.value = e.detail.value;
};

const isLoading = ref(false);
const isDisabled = computed(() => {
    return !fristPassword.value || !secondPassword.value || isLoading.value;
});
const validatePassword = (value: string) => {
    if (!value) {
        return t("密码不能为空");
    } else if (value.length > 20 || value.length < 6) {
        return t("密码应为6-20位字符");
    } else if (/\s/.test(value)) {
        return t("密码中不允许有空格");
    } else if (/^\d+$/.test(value)) {
        return t("密码不能全为数字");
    } else if (!/^(?=.*[\d\W]).+$/.test(value)) {
        return t("密码不能全为字母");
    } else if (!/^(?=.*[a-zA-Z0-9]).+$/.test(value)) {
        return t("密码不能全为符号");
    } else if (!/^(?!([a-zA-Z0-9])\1*$).+$/.test(value)) {
        return t("密码不能全为相同字符或数字");
    }
};
const handleResetPassword = async () => {
    if (validatePassword(fristPassword.value)) {
        return uni.showToast({
            title: validatePassword(fristPassword.value),
            duration: 1500,
            icon: "none"
        });
    }
    if (fristPassword.value != secondPassword.value) {
        return uni.showToast({
            title: t("两次密码输入不一致"),
            duration: 1500,
            icon: "none"
        });
    }
    try {
        isLoading.value = true;
        const result = await forgetPassword({
            mobileKey: mobileKey.value,
            password: fristPassword.value //新密码
        });
        uni.showToast({
            title: t("重置密码成功"),
            duration: 1500,
            icon: "none"
        });
        setTimeout(() => {
            uni.navigateTo({
                url: "/pages/login/index?loginType=password"
            });
        }, 1500);
    } catch (error: any) {
        console.error(error);
        uni.showToast({
            title: error.message,
            duration: 1500,
            icon: "none"
        });
        step.value = 1;
        mobileKey.value = "";
        fristPassword.value = "";
        secondPassword.value = "";
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
.password_show_hide {
    position: absolute;
    color: #666;
    right: 8rpx;
    top: 10rpx;
    font-size: 40rpx;
    padding: 10rpx;
    z-index: 88;
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
</style>
