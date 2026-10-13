<template>
    <tig-layout :title="$t('绑定')">
        <view class="bindMobilePhone">
            <view class="bindMobilePhone-title">{{ $t("手机号绑定") }}</view>
            <up-form ref="formRef" label-position="left" :model="form" :rules="rules" error-type="toast">
                <up-form-item label="" prop="mobile">
                    <up-input v-model="form.mobile" :placeholder="$t('请输入手机号')" font-size="28rpx" border="bottom" />
                </up-form-item>
                <up-form-item label="" prop="">
                    <view class="flex flex1">
                        <up-input v-model="form.mobileCode" :placeholder="$t('请输入验证码')" font-size="28rpx" border="bottom">
                            <template #suffix />
                        </up-input>
                        <verificationCode
                            v-model:mobile="form.mobile"
                            v-model:is-checked="isChecked"
                            v-model:verify-token-data="verifyToken"
                            style="height: 65rpx; margin-left: 15rpx"
                            :request-api="sendMobileCode"
                            @mobile-error-callback="mobileErrorCallback"
                        />
                    </view>
                </up-form-item>
                <up-form-item label="" prop="">
                    <up-input v-model="form.password" :placeholder="$t('请输入密码')" font-size="28rpx" password border="bottom" />
                </up-form-item>
            </up-form>
            <tig-button :loading="isLoading" :disabled="isloginDisabled" @click="handleSubmit"> {{ $t("确 认") }} </tig-button>
        </view>
        <!-- #ifdef H5 -->
        <Verify ref="verify" mode="pop" captcha-type="blockPuzzle" :img-size="{ width: '310px', height: '155px' }" @ok-callback="okCallback" />
        <!-- #endif -->
    </tig-layout>
</template>

<script setup lang="ts">
/**
 * 第三方登录后绑定手机号页
 * - 输入手机号、验证码、密码完成绑定
 * - 绑定成功后设置Token并跳转会员中心
 * - 支持滑动验证码二次校验
 */
import VerificationCode from "@/components/verificationCode/index.vue";
import Verify from "@/components/verifition/Verify.vue";
import { reactive, ref, computed } from "vue";
import { sendMobileCode } from "@/api/login/login";
import { bindMobilePhone } from "@/api/user/bindMobilePhone";
import { useUserStore } from "@/store/user";
import { useWholesaleStore } from "@/store/wholesale";
import { redirect } from "@/utils";
import { useI18n } from "vue-i18n";

const { t } = useI18n();

const userStore = useUserStore();

const formRef = ref();

const verify = ref();

const rules = {
    mobile: {
        validator: (rule: any, value: any, callback: any) => {
            const regex = /^(?:(?:\+|00)86)?1[3-9]\d{9}$/;
            const status = regex.test(value);
            if (!status) {
                callback(t("请输入正确的手机号码"));
            }
            return true;
        },
        message: t("请输入正确的手机号码!"),
        // 触发器可以同时用blur和change
        trigger: ["change", "blur"]
    }
};
const verifyToken = ref("");
const isChecked = ref(true);
const form = reactive({
    mobile: "",
    mobileCode: "",
    password: "",
    openData: {}
});
const isLoading = ref(false);

const mobileErrorCallback = (msg: string) => {
    uni.showToast({
        title: msg,
        duration: 1500,
        icon: "none"
    });
};

const isloginDisabled = computed(() => {
    return !(form.mobile && form.mobileCode && form.password);
});

const handleSubmit = async () => {
    isLoading.value = true;
    try {
        await formRef.value.validate();
        form.openData = uni.getStorageSync("openData");
        const result = await bindMobilePhone(form);
        userStore.setToken(result);
        userStore.getUserInfo();
        userStore.setAuthType("");
        // 登录后强制刷新批发身份，避免沿用启动时未登录态缓存导致详情页误判为零售
        useWholesaleStore().loadIdentity(true);
        uni.showToast({
            title: t("绑定成功"),
            duration: 1500,
            icon: "success"
        });
        setTimeout(() => {
            uni.setStorageSync("openData", "");
            redirect({
                url: "/pages/member/index"
            });
        }, 1500);
    } catch (error: any) {
        console.error(error);
        if (error.code == 1002 && verify.value) {
            verify.value.show();
        } else if (error.code > 0) {
            uni.showToast({
                title: error.message,
                duration: 1500,
                icon: "none"
            });
            verifyToken.value = "";
        }
    } finally {
        isLoading.value = false;
    }
};

const okCallback = (e: any) => {
    verifyToken.value = e.verifyToken;
    handleSubmit();
};
</script>
<style>
page {
    background-color: #fff;
}
</style>
<style lang="scss" scoped>
.flex1 {
    flex: 1;
}
.bindMobilePhone {
    padding: 30rpx;

    .bindMobilePhone-title {
        font-size: 22px;
        padding-left: 6px;
        font-weight: bold;
        color: #333333;
        margin-bottom: 30rpx;
    }
    :deep(.u-form-item__body) {
        margin-bottom: 15rpx;
    }
}
</style>
