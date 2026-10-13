<template>
    <tig-layout :title="$t('账户安全')">
        <view class="profile-edit-main">
            <view class="profile-edit-content">
                <view class="item input">
                    <view class="mobile-input-wrap">
                        <uni-easyinput
                            :input-border="false"
                            :value="mobile"
                            class="uni-input item-input"
                            :placeholder="$t('请输入新手机号')"
                            primary-color="rgb(192, 196, 204)"
                            @input="inputMobile"
                        />
                    </view>
                </view>
                <view class="flex align-center">
                    <view class="item line">
                        <uni-easyinput
                            :input-border="false"
                            :value="mobileCode"
                            class="uni-input item-input"
                            focus
                            :placeholder="$t('新手机短信验证码')"
                            primary-color="rgb(192, 196, 204)"
                            @input="inputMobileCode"
                        />
                    </view>
                    <VerificationCode
                        v-model:is-checked="isChecked"
                        v-model:verify-token-data="verifyToken"
                        :mobile="mobileForRequest"
                        style="height: 65rpx"
                        :request-api="sendMobileCodeByMobileMobile"
                        @mobile-error-callback="mobileErrorCallback"
                    />
                </view>
                <view class="item" style="height: 60rpx">
                    <tig-button :disabled="isRegisterDisabled" class="next" @click="onsubmit">{{ $t("提交") }}</tig-button>
                </view>
            </view>
        </view>
    </tig-layout>
</template>
<script lang="ts" setup>
/**
 * 绑定新手机号页
 * - 验证原手机后跳转到此页
 * - 输入新手机号并验证短信验证码
 * - 绑定成功后跳转个人资料页
 */
import { computed, ref } from "vue";
import VerificationCode from "@/components/verificationCode/index.vue";
import { mobileMobile, sendMobileCodeByMobileMobile } from "@/api/user/security";
import { useI18n } from "vue-i18n";
import { useConfigStore } from "@/store/config";
import { isOverseas } from "@/utils";

const { t } = useI18n();
const configStore = useConfigStore();

const inputMobileCode = (e: any) => {
    mobileCode.value = e;
};

const inputMobile = (e: any) => {
    mobile.value = e;
};

const mobileCode = ref("");
const mobile = ref("");
const mobileAreaCode = ref("86");
const verifyToken = ref("");
const isChecked = ref(true);

const isRegisterDisabled = computed(() => {
    return !mobile.value || !mobileCode.value;
});

const mobileForRequest = computed(() => {
    return isOverseas() && configStore.isOpenMobileAreaCode ? mobileAreaCode.value + mobile.value : mobile.value;
});

const mobileErrorCallback = (msg: string) => {
    uni.showToast({
        title: msg,
        duration: 1500,
        icon: "none"
    });
};

const onsubmit = async () => {
    try {
        let temp = {
            mobile: mobile.value,
            code: mobileCode.value
        };
        await mobileMobile({ ...temp, mobile: mobileForRequest.value });

        uni.showToast({
            title: t("修改成功"),
            icon: "none"
        });

        setTimeout(() => {
            // 一秒后执行这里的代码
            uni.redirectTo({
                url: "/pages/user/profile/index"
            });
        }, 1000);
    } catch (error: any) {
        if (error.message) {
            uni.showToast({
                title: error.message,
                icon: "none"
            });
        }
    }
};
</script>
<style lang="scss" scoped>
.line {
    padding-bottom: 10rpx;
    border-bottom: 1px solid #dfdfdf;
    width: 390rpx;
}
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

            .mobile-input-wrap {
                display: flex;
                align-items: center;
                width: 100%;
            }

            .next {
                width: 100%;
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

        .btn-item {
            display: flex;
            flex-direction: column;
            margin: 20rpx;
            font-size: 24rpx;
            gap: 8rpx;

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
