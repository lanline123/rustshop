<template>
    <tig-layout :title="$t('账户安全')">
        <view class="profile-edit-main">
            <view class="profile-edit-content">
                <view v-if="hasMobile" class="item">
                    <view class="title">
                        <view>{{ $t("请输入") }}</view>
                        <view class="info">{{ mobile.replace(/(\d{2})\d*(\d{4})$/, "$1*****$2") }}</view>
                        <view>{{ $t("收到的短信验证码") }}</view>
                    </view>
                </view>
                <template v-else>
                    <view class="item">
                        <view class="title">
                            <view>{{ $t("您还未绑定手机号，请输入手机号绑定") }}</view>
                        </view>
                    </view>
                    <view class="item special-item line">
                        <view class="mobile-input-wrap">
                            <uni-easyinput
                                :input-border="false"
                                :value="mobile"
                                :focus="!hasMobile"
                                class="uni-input item-input"
                                :placeholder="$t('请输入手机号')"
                                primary-color="rgb(192, 196, 204)"
                                @input="inputMobile"
                            />
                        </view>
                    </view>
                </template>

                <view class="flex align-center">
                    <view class="item line">
                        <uni-easyinput
                            :input-border="false"
                            :value="mobileCode"
                            :focus="hasMobile"
                            class=""
                            :placeholder="$t('手机短信验证码')"
                            primary-color="rgb(192, 196, 204)"
                            @input="inputMobileCode"
                        />
                    </view>
                    <VerificationCode
                        v-if="hasMobile"
                        v-model:is-checked="isChecked"
                        v-model:verify-token-data="verifyToken"
                        :mobile="mobileForRequest"
                        style="height: 65rpx"
                        :request-api="sendMobileCodeByMobileValidate"
                        :need-mobile="false"
                        @mobile-error-callback="mobileErrorCallback"
                    />

                    <VerificationCode
                        v-else
                        v-model:is-checked="isChecked"
                        v-model:verify-token-data="verifyToken"
                        :mobile="mobileForRequest"
                        style="height: 65rpx"
                        :request-api="sendMobileCodeByMobileMobile"
                        @mobile-error-callback="mobileErrorCallback"
                    />
                </view>

                <view class="item" style="height: 60rpx">
                    <tig-button :disabled="isRegisterDisabled" class="next" @click="onNext">{{ $t("下一步") }}</tig-button>
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
import { useI18n } from "vue-i18n";

/**
 * 手机号绑定/换绑页
 * - 已绑定：验证原手机号短信验证码，跳转换绑新手机
 * - 未绑定：直接输入新手机号并验证绑定
 */
import { computed, ref } from "vue";
import VerificationCode from "@/components/verificationCode/index.vue";
import { mobileMobile, mobileValidate, sendMobileCodeByMobileMobile, sendMobileCodeByMobileValidate } from "@/api/user/security";
import { onLoad } from "@dcloudio/uni-app";
import { isOverseas, redirect } from "@/utils";
import { useConfigStore } from "@/store/config";

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
const hasMobile = ref(false);
onLoad((options: any) => {
    if (options.mobile) {
        hasMobile.value = true;
        mobile.value = options.mobile;
    } else {
        hasMobile.value = false;
    }
});
const isRegisterDisabled = computed(() => {
    return !mobileCode.value || !verifyToken.value;
});

const mobileForRequest = computed(() => {
    return !hasMobile.value && isOverseas() && configStore.isOpenMobileAreaCode ? mobileAreaCode.value + mobile.value : mobile.value;
});

const mobileErrorCallback = (msg: string) => {
    uni.showToast({
        title: msg,
        duration: 1500,
        icon: "none"
    });
};

const onNext = async () => {
    try {
        let temp = {
            mobile: mobile.value,
            code: mobileCode.value
        };
        if (hasMobile.value) {
            await mobileValidate({ code: mobileCode.value });
            redirect({
                url: "/pages/user/security/newPhone"
            });
        } else {
            await mobileMobile({ ...temp, mobile: mobileForRequest.value });
            uni.showToast({
                title: t("绑定成功"),
                icon: "none"
            });
            redirect({
                url: "/pages/member/index"
            });
        }
    } catch (error: any) {
        uni.showToast({
            title: error.message,
            icon: "none"
        });
    }
};
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

            &.special-item {
                width: 600rpx;
            }

            .mobile-input-wrap {
                display: flex;
                align-items: center;
                width: 100%;
            }

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

        .line {
            padding-bottom: 10rpx;
            border-bottom: 1px solid #dfdfdf;
            width: 390rpx;
        }
    }
}
</style>
