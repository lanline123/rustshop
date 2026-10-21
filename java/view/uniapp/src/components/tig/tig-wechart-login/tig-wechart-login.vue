<template>
    <!-- 层级需高于商品规格等业务弹窗，否则弹窗内触发登录时会被遮挡 -->
    <tig-popup v-model:show="show" :z-index="10080" @close="close">
        <template v-if="show">
            <view class="wechart-login-content" :style="isWechatH5 ? { height: '19vh' } : {}">
                <view class="wechart-login-box">
                    <!-- #ifdef MP-WEIXIN -->
                    <template v-if="isChecked">
                        <tig-button
                            :custom-style="{ 'border-radius': '20rpx', height: '95rpx' }"
                            open-type="getPhoneNumber"
                            shape="square"
                            :loading="loginLoading"
                            @get-phone-number="getPhoneNumber"
                            >{{ $t("手机号快捷登入") }}</tig-button
                        >
                    </template>
                    <template v-else>
                        <tig-button :custom-style="{ 'border-radius': '20rpx', height: '95rpx' }" shape="square" @click="mobileLogin">{{
                            $t("手机号快捷登入")
                        }}</tig-button>
                    </template>
                    <!-- #endif -->
                    <!-- #ifndef MP-WEIXIN -->
                    <template v-if="isChecked">
                        <tig-button :custom-style="{ 'border-radius': '20rpx', height: '95rpx' }" shape="square" :loading="loginLoading" @click="wechatLogin">{{
                            $t("微信授权")
                        }}</tig-button>
                    </template>
                    <template v-else>
                        <tig-button :custom-style="{ 'border-radius': '20rpx', height: '95rpx' }" shape="square" @click="mobileLogin">{{
                            $t("微信授权")
                        }}</tig-button>
                    </template>
                    <!-- #endif -->

                    <view class="rule-text">
                        <tig-checkbox v-model:checked="isChecked" />
                        <view class="rule-xieyi">
                            <text @click="isChecked = !isChecked">{{ $t("登录即为同意") }}</text>
                            <text class="special-color" @click="showAgreement">{{ $t("《商城用户服务协议》") }}</text>
                        </view>
                    </view>

                    <view v-if="!isWechatH5" class="other-login">
                        <view class="mobile-login" @click="handleLink('mobile')">{{ $t("手机登录") }}</view>
                        <view class="account-login" @click="handleLink('password')">{{ $t("账号登录") }}</view>
                    </view>
                </view>
            </view>
        </template>
    </tig-popup>
</template>

<script setup lang="ts">
import { ref, computed } from "vue";
import { getOauthInfo, getOauthUrl, getWechartMobile, setMiniProgramOpenid } from "@/api/login/login";
import { useUserStore } from "@/store/user";
import { useConfigStore } from "@/store/config";
import { useWholesaleStore } from "@/store/wholesale";
import { onShow } from "@dcloudio/uni-app";
import { useI18n } from "vue-i18n";
const { t } = useI18n();

const emit = defineEmits(["loginSuccess"]);

const userStore = useUserStore();
const configStore = useConfigStore();

const isWechatH5 = computed(() => configStore.XClientType === "wechat");

const show = computed({
    get() {
        return !!userStore.authType;
    },
    set(val) {
        userStore.setAuthType("");
    }
});

const isChecked = ref(false);
const loginLoading = ref(false);
const showAgreement = () => {
    userStore.setAuthType(""); // 关闭弹窗
    uni.navigateTo({
        url: "/pages/login/mallAgreement?articleSn=fwxy&from=loginPopup"
    });
};
const mobileLogin = () => {
    if (!isChecked.value) {
        return uni.showToast({
            title: t("请先同意用户协议"),
            duration: 1500,
            icon: "none"
        });
    }
};

//微信授权登录
const wechatLogin = async () => {
    // const result = await getOauthUrl({ url: location.origin + location.pathname });
    const result = await getOauthUrl({ url: location.origin + location.pathname + location.search });
    window.location.href = result.url;
};

//获取用户信息
const getWechatInfo = async (code: string) => {
    try {
        const result = await getOauthInfo({ code: code });
        if (result.type == 1) {
            // 已注册过，直接登录
            userStore.setToken(result.token);
            await userStore.getUserInfo();
            // 登录后强制刷新批发身份，避免沿用启动时未登录态缓存导致详情页误判为零售
            useWholesaleStore().loadIdentity(true);
            emit("loginSuccess");
            userStore.setAuthType("");
            return;
        }
        if (result.type == 2) {
            // 需要绑定手机号：存 openData 并跳转绑定页（是否需要绑定由后端判断）
            uni.setStorageSync("openData", result.openData);
            uni.navigateTo({
                url: "/pages/user/bindMobilePhone/index"
            });
            return;
        }
    } catch (error: any) {
        uni.showToast({
            title: error?.message || t("微信授权失败，请重试"),
            duration: 1500,
            icon: "none"
        });
    }
};

const handleLink = (type: string) => {
    userStore.setAuthType("");
    uni.navigateTo({
        url: `/pages/login/index?loginType=${type}`
    });
};

const getPhoneNumber = async (e: any) => {
    try {
        const result = await getWechartMobile({ code: e.detail.code });
        userStore.setToken(result);
        userStore.getUserInfo();
        // 登录后强制刷新批发身份，避免沿用启动时未登录态缓存导致详情页误判为零售
        useWholesaleStore().loadIdentity(true);
        emit("loginSuccess");
        userStore.setAuthType("");
        // #ifdef MP-WEIXIN
        updateUserOpenId();
        // #endif
    } catch (error: any) {
        console.error(error);
        uni.showToast({
            title: error.message,
            icon: "none"
        });
        userStore.setAuthType("");
    }
};
const updateUserOpenId = async () => {
    wx.login({
        success: async (res: any) => {
            await setMiniProgramOpenid({
                code: res.code
            });
        },
        fail: (err: any) => {
            console.log(err);
        }
    });
};

const close = () => {
    userStore.setAuthType("");
};

const getUlParams = (url: string) => {
    const params: Record<string, string> = {};
    const urlParams = url.split("?")[1];
    if (urlParams) {
        urlParams.split("&").forEach((item: string) => {
            const [key, value] = item.split("=");
            params[key] = value;
        });
    }
    return params;
};

onShow(() => {
    if (configStore.XClientType === "wechat" && !uni.getStorageSync("token")) {
        const params = getUlParams(location.href);
        if (params.code && params.code.length > 12) {
            //授权
            getWechatInfo(params.code);
        }
    }
});
</script>

<style lang="scss" scoped>
.wechart-login-content {
    height: 25vh;
    box-sizing: content-box;
    padding: 30rpx;
    .wechart-login-box {
        margin-top: 80rpx;
    }
}

.other-login {
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 28rpx;
    color: #999;
    margin-top: 50rpx;
    height: 30rpx;
    font-size: 24rpx;
    width: 100%;
    .mobile-login {
        padding-right: 25rpx;
        position: relative;
        &::after {
            content: "";
            display: inline-block;
            width: 1rpx;
            height: 28rpx;
            background: #e5e5e5;
            position: absolute;
            right: 0;
        }
    }

    .account-login {
        padding-left: 25rpx;
    }
}

.btn2-css3 {
    width: 100%;
    height: 90rpx;
    line-height: 90rpx;
    padding: 0;
    font-size: 36rpx;
    font-weight: normal;
}

.rule-text {
    font-size: 26rpx;
    color: #999;
    margin-top: 10rpx;
    display: flex;
    align-items: center;
    justify-content: center;
}
.rule-xieyi {
    display: flex;
    align-items: center;
    margin-left: 8rpx;
}
.special-color {
    color: var(--general);
}
</style>
