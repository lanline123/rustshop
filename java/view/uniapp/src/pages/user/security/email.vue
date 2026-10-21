<template>
    <tig-layout :title="$t('账户安全')">
        <view class="profile-edit-main">
            <view class="profile-edit-content">
                <template v-if="isBind">
                    <view class="item special-item line">
                        <uni-easyinput
                            :input-border="false"
                            :value="emailNew"
                            :focus="true"
                            class="uni-input item-input"
                            :placeholder="$t('请输入新邮箱')"
                            primary-color="rgb(192, 196, 204)"
                            @input="inputEmailNew"
                        />
                    </view>
                    <view class="flex align-center">
                        <view class="item line">
                            <uni-easyinput
                                :input-border="false"
                                :value="emailCodeNew"
                                class=""
                                :placeholder="$t('新邮箱验证码')"
                                primary-color="rgb(192, 196, 204)"
                                @input="inputMobileCodeNew"
                            />
                        </view>
                        <VerificationCode
                            v-model:is-checked="isChecked"
                            v-model:email="emailNew"
                            v-model:verify-token-data="verifyToken"
                            style="height: 65rpx"
                            :request-api="sendEmailCodeByModifyEmail"
                            @mobile-error-callback="mobileErrorCallback"
                        />
                    </view>
                </template>
                <template v-else>
                    <view v-if="hasEmail" class="item">
                        <view class="title">
                            <view>{{ $t("请输入") }}</view>
                            <view class="info">{{ email.replace(/(\w{2})[\w.-]*@/, "$1*****@") }}</view>
                            <view>{{ $t("收到的短信验证码") }}</view>
                        </view>
                    </view>
                    <template v-else>
                        <view class="item">
                            <view class="title">
                                <view>{{ $t("您还未绑定邮箱，请输入邮箱绑定") }}</view>
                            </view>
                        </view>
                        <view class="item special-item line">
                            <uni-easyinput
                                :input-border="false"
                                :value="email"
                                class="uni-input item-input"
                                :placeholder="$t('请输入邮箱')"
                                primary-color="rgb(192, 196, 204)"
                                @input="inputEmail"
                            />
                        </view>
                    </template>

                    <view class="flex align-center">
                        <view class="item line">
                            <uni-easyinput
                                :input-border="false"
                                :value="emailCode"
                                class=""
                                :placeholder="$t('邮箱验证码')"
                                primary-color="rgb(192, 196, 204)"
                                @input="inputMobileCode"
                            />
                        </view>
                        <VerificationCode
                            v-if="hasEmail"
                            v-model:is-checked="isChecked"
                            v-model:email="email"
                            v-model:verify-token-data="verifyToken"
                            style="height: 65rpx"
                            :request-api="sendEmailCodeByEmailValidate"
                            @mobile-error-callback="mobileErrorCallback"
                        />

                        <VerificationCode
                            v-else
                            v-model:is-checked="isChecked"
                            v-model:email="email"
                            v-model:verify-token-data="verifyToken"
                            style="height: 65rpx"
                            :request-api="sendEmailCodeByModifyEmail"
                            @mobile-error-callback="mobileErrorCallback"
                        />
                    </view>
                </template>
                <template v-if="!hasEmail">
                    <view class="item" style="height: 60rpx">
                        <tig-button :disabled="isRegisterDisabled" class="next" @click="onBinding">{{ $t("下一步") }}</tig-button>
                    </view>
                </template>
                <template v-else>
                    <view class="item" style="height: 60rpx">
                        <tig-button :disabled="isRegisterDisabled" class="next" @click="onNext">{{ $t("下一步") }}</tig-button>
                    </view>
                </template>
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
 * 邮箱绑定/换绑页
 * - 已绑定：先验证原邮箱，再输入新邮箱完成换绑
 * - 未绑定：直接输入邮箱验证绑定
 */
import { computed, ref } from "vue";
import VerificationCode from "@/components/verificationCode/index.vue";
import { emailEmailValidate, emailValidate, sendEmailCodeByEmailValidate, sendEmailCodeByModifyEmail } from "@/api/user/security";
import { onLoad } from "@dcloudio/uni-app";

const { t } = useI18n();

const inputMobileCode = (e: any) => {
    emailCode.value = e;
};

const inputEmail = (e: any) => {
    email.value = e;
};
const inputMobileCodeNew = (e: any) => {
    emailCodeNew.value = e;
};

const inputEmailNew = (e: any) => {
    emailNew.value = e;
};

const email = ref("");
const emailNew = ref("");
const emailCodeNew = ref("");
const emailCode = ref("");
const verifyToken = ref("");
const isChecked = ref(true);
const hasEmail = ref(false);
const isBind = ref(false);
onLoad((options: any) => {
    if (options.email) {
        hasEmail.value = true;
        email.value = options.email;
    } else {
        hasEmail.value = false;
    }
});
const isRegisterDisabled = computed(() => {
    return !emailCode.value || !verifyToken.value;
});

const mobileErrorCallback = (msg: string) => {
    uni.showToast({
        title: msg,
        duration: 1500,
        icon: "none"
    });
};
const onBinding = async () => {
    try {
        let temp = {
            email: email.value,
            code: emailCode.value
        };
        await emailEmailValidate({ ...temp });
        uni.showToast({
            title: t("绑定成功"),
            icon: "none"
        });
        let url = "/pages/member/index";
        uni.navigateTo({
            url
        });
    } catch (error: any) {
        uni.showToast({
            title: error.message,
            icon: "none"
        });
    }
};

const onEmailValidateNew = async () => {
    try {
        await emailValidate({
            email: email.value,
            code: emailCode.value
        });

        isBind.value = true;
    } catch (error: any) {
        uni.showToast({
            title: error.message,
            icon: "none"
        });
        console.error("Email validation failed:", error);
    }
};

const onNext = async () => {
    if (!isBind.value) {
        return await onEmailValidateNew();
    }
    try {
        let temp = {
            email: !isBind.value ? email.value : emailNew.value,
            code: !isBind.value ? emailCode.value : emailCodeNew.value
        };
        await emailEmailValidate({ ...temp });
        uni.showToast({
            title: t("绑定成功"),
            icon: "none"
        });
        let url = "/pages/member/index";
        uni.navigateTo({
            url
        });
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
