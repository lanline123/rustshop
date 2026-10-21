<template>
    <tig-layout :title="$t('个人资料')" :border-bottom="true">
        <view class="profile-edit-main">
            <uni-forms ref="formRef" :model-value="form" label-width="300rpx">
                <view class="profile-edit-content">
                    <uni-forms-item :label="$t('用户ID')" name="username">
                        <uni-easyinput v-model="form.dimUsername" :input-border="false" disabled primary-color="rgb(192, 196, 204)" />
                    </uni-forms-item>
                    <uni-forms-item :label="$t('昵称')" name="nickname">
                        <uni-easyinput
                            v-model="form.nickname"
                            :input-border="false"
                            :placeholder="$t('请输入昵称')"
                            primary-color="rgb(192, 196, 204)"
                            type="nickname"
                        />
                    </uni-forms-item>
                    <uni-forms-item :label="$t('出生日期')" name="birthday">
                        <view class="el-input-id" @click="handleShowDatetime">
                            {{ form.birthday === "0000-00-00" ? $t("请选择出生日期") : form.birthday }}
                            <view class="iconfont icon-xiangyou" />
                        </view>
                    </uni-forms-item>
                </view>
                <view class="profile-edit-content">
                    <uni-forms-item :label="$t('登录密码')" @tap="goPages('/pages/user/security/password' + '?mobile=' + form.mobile, 'password')">
                        <view class="el-input-id">
                            {{ $t("修改") }}
                            <view class="iconfont icon-xiangyou" />
                        </view>
                    </uni-forms-item>
                    <uni-forms-item :label="$t('手机号码')" @tap="goPages('/pages/user/security/phone' + '?mobile=' + form.mobile)">
                        <view class="el-input-id">
                            {{ form.showMobile || $t("去绑定") }}
                            <view class="iconfont icon-xiangyou" />
                        </view>
                    </uni-forms-item>
                    <template v-if="configStore.openEmailRegister === 1">
                        <uni-forms-item :label="$t('邮箱')">
                            <view class="el-input-id" @tap="goPages('/pages/user/security/email' + '?email=' + form.email)">
                                {{ form.email ? form.email : $t("去绑定") }}
                                <view class="iconfont icon-xiangyou" />
                            </view>
                        </uni-forms-item>
                    </template>
                    <!-- #ifdef H5 -->
                    <template v-if="configStore.XClientType == 'wechat'">
                        <uni-forms-item :label="$t('公众号绑定')">
                            <template v-if="form.isBindWechat">
                                <view class="el-input-id" @click.stop="unbindWechatAct">
                                    {{ $t("解绑") }}
                                    <view class="iconfont icon-xiangyou" />
                                </view>
                            </template>
                            <template v-else>
                                <view class="el-input-id" @click.stop="wechatLogin">
                                    {{ $t("绑定") }}
                                    <view class="iconfont icon-xiangyou" />
                                </view>
                            </template>
                        </uni-forms-item>
                    </template>
                    <!-- #endif -->
                    <uni-forms-item :label="$t('意见建议')" @tap="goPages('/pages/user/suggest/index' + '?mobile=' + form.mobile)">
                        <view class="el-input-id">
                            <view class="iconfont icon-xiangyou" />
                        </view>
                    </uni-forms-item>
                    <uni-forms-item :label="$t('关于')" @tap="goPages('/pages/user/about/index' + '?mobile=' + form.mobile)">
                        <view class="el-input-id">
                            <view class="iconfont icon-xiangyou" />
                        </view>
                    </uni-forms-item>
                </view>
            </uni-forms>
        </view>

        <!-- #ifdef APP-PLUS -->
        <view class="write-off-box" @click="handleWriteOff">
            <view class="">{{ $t("注销账号") }}</view>
            <view class="write-off-desc">
                <text class="">{{ $t("注销后账号无法恢复，请谨慎操作") }}</text>
                <text class="iconfont icon-xiangyou" />
            </view>
        </view>
        <!-- #endif -->

        <tig-copyright />

        <tig-fixed-placeholder background-color="#fff">
            <view class="btn-box">
                <tig-button class="btn" plain @click="onLogout"> {{ $t("退出登录") }} </tig-button>
                <tig-button class="btn" @click="onSubmit"> {{ $t("保存修改") }} </tig-button>
            </view>
        </tig-fixed-placeholder>
        <up-datetime-picker
            ref="datetimePickerRef"
            :cancel-text="$t('取消')"
            :confirm-text="$t('确定')"
            :show="showDatetime"
            :model-value="Date.now()"
            mode="date"
            :formatter="formatter"
            format="YYYY-MM-DD"
            confirm-color="var(--general)"
            :min-date="1"
            @cancel="showDatetime = false"
            @confirm="getBirthday"
        />
    </tig-layout>
</template>

<script lang="ts" setup>
/**
 * 个人资料编辑页
 * - 编辑昵称、出生日期等基本信息
 * - 修改登录密码、绑定/换绑手机号、绑定邮箱
 * - H5微信公众号环境支持绑定/解绑微信
 * - APP端支持账号注销
 */
import { nextTick, reactive, ref } from "vue";
import { onLoad, onShow } from "@dcloudio/uni-app";
import { getProfile, updateProfile } from "@/api/user/profile";
import { useUserStore } from "@/store/user";
import { getOauthUrl, bindWechat, unbindWechat } from "@/api/login/login";
import { userClose } from "@/api/user/user";
import { useConfigStore } from "@/store/config";
import { useI18n } from "vue-i18n";
import { redirect } from "@/utils";

const { t } = useI18n();

const userStore = useUserStore();

const configStore = useConfigStore();

const form = reactive<any>({
    nickname: "",
    birthday: "",
    mobile: ""
});

const goPages = (url: string, type?: string) => {
    if (type == "password" && form.mobile == "") {
        uni.showToast({
            title: t("未绑定手机号，请先绑定手机号再修改密码"),
            icon: "none"
        });
        return;
    }
    uni.navigateTo({
        url
    });
};

const handleUerClose = async () => {
    try {
        await userClose();
        uni.showToast({
            title: t("账号已注销"),
            icon: "none",
            duration: 1000
        });
        setTimeout(() => {
            userStore.logout();
        }, 500);
    } catch (error) {
        console.error(error);
    }
};

const handleWriteOff = () => {
    uni.showModal({
        title: t("温馨提示"),
        content: t("注销账号后会删除相关账号数据，您是否要确认注销?"),
        confirmText: t("确定"),
        cancelText: t("取消"),
        success: (res) => {
            if (res.confirm) {
                handleUerClose();
            }
        }
    });
};

const rules = {
    nickname: {
        rules: [{ required: true, errorMessage: t("请您填写用户昵称") }]
    }
};

const formRef = ref();

const onSubmit = () => {
    formRef.value
        .validate()
        .then(() => {
            edit();
        })
        .catch((err: any) => {
            console.log("表单错误信息：", err);
        });
};

const onLogout = () => {
    userStore.logout();
};

const edit = async () => {
    try {
        await updateProfile({ ...form });

        uni.showToast({
            title: t("修改成功"),
            icon: "none",
            duration: 1000
        });

        setTimeout(() => {
            uni.navigateBack({
                success: function () {
                    uni.$emit("refreshData");
                }
            });
        }, 1000);
    } catch (error: any) {
        console.error(error);
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
        // 微信小程序需要用此写法
        datetimePickerRef.value.setFormatter(formatter);
    });
    if (configStore.XClientType === "wechat" && uni.getStorageSync("token")) {
        const params = getUlParams(location.href);
        if (params.code && params.code.length > 12) {
            //授权
            bindWechatAct(params.code);
        }
    }
});

/**
 * 绑定微信公众号
 * - 授权成功后根据是否有 flowType 决定跳转目标
 * - flowType 存在时跳转订单结算页（从下单绑定微信场景进入）
 */
const bindWechatAct = async (code: string) => {
    uni.showLoading({
        title: t("授权中") + "..."
    });
    try {
        await bindWechat({ code: code });
        uni.showToast({
            title: t("授权成功"),
            icon: "none",
            duration: 1000
        });

        let url = "/pages/user/profile/index";

        if (flowType.value) {
            url = `/pages/order/check?flowType=${flowType.value}`;
        }

        setTimeout(() => {
            redirect({
                url,
                mode: "redirectTo",
                success: () => {
                    if (flowType.value) {
                        uni.removeStorageSync("bindWechatFlowType");
                    }
                }
            });
        }, 1000);
    } catch (error: any) {
        uni.showToast({
            title: error.message,
            icon: "none",
            duration: 1000
        });
    } finally {
        uni.hideLoading();
    }
};
//解绑
const unbindWechatAct = async () => {
    uni.showModal({
        title: t("提示"),
        content: t("确定取消绑定吗？"),
        confirmText: t("确定"),
        cancelText: t("取消"),
        success: async (res) => {
            if (res.confirm) {
                uni.showLoading({
                    title: t("正在解绑...")
                });
                try {
                    await unbindWechat();
                    uni.hideLoading();
                    uni.showToast({
                        title: t("解绑成功"),
                        icon: "none",
                        duration: 1000
                    });
                    setTimeout(() => {
                        __getProfile();
                    }, 500);
                } catch (error: any) {
                    console.error(error);
                    uni.showToast({
                        title: error.message,
                        icon: "none",
                        duration: 1000
                    });
                } finally {
                    uni.hideLoading();
                }
            }
        }
    });
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

const flowType = ref(uni.getStorageSync("bindWechatFlowType") || null);

onLoad(async (options) => {
    if (options && options.bindWechat) {
        await wechatLogin();
    }
    __getProfile();
});

const __getProfile = async () => {
    uni.showLoading({
        title: t("加载中")
    });
    try {
        const result = await getProfile();
        let temp: any = result.mobile;
        result.showMobile = temp.replace(/(\d{2})\d*(\d{4})$/, "$1*****$2");
        Object.assign(form, result);
    } catch (error) {
        console.error(error);
    } finally {
        uni.hideLoading();
    }
};

const formatter = (type: string, value: string) => {
    if (type === "year") {
        return `${value}${t("年")}`;
    }
    if (type === "month") {
        return `${value}${t("月")}`;
    }
    if (type === "day") {
        return `${value}${t("日")}`;
    }
    return value;
};

const datetimePickerRef = ref();
const showDatetime = ref(false);
const handleShowDatetime = () => {
    showDatetime.value = true;
};
const getBirthday = (val?: any) => {
    if (val && val.value) {
        form.birthday = uni.$u.timeFormat(val.value, "yyyy-mm-dd");
    }
    showDatetime.value = false;
};

//微信授权登录
const wechatLogin = async () => {
    const result = await getOauthUrl({ url: location.origin + location.pathname });
    window.location.href = result.url;
};
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
    left: 18rpx;
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

.write-off-box {
    background-color: #fff;
    padding: 20rpx;
    margin-bottom: 20rpx;
    font-size: 26rpx;
    display: flex;
    align-items: center;
    justify-content: space-between;
    color: #606266;

    .write-off-desc {
        font-size: 24rpx;
        color: #999999;
        display: flex;
        align-items: center;
    }
}

.profile-edit-main {
    .profile-edit-content {
        background-color: #fff;
        padding: 20rpx;
        margin-bottom: 20rpx;

        .el-input-id {
            height: 100%;
            color: #999999;
            font-size: 24rpx;
            display: flex;
            align-items: center;
            justify-content: flex-end;
        }
    }

    :deep(.uni-calendar-item__weeks-box .uni-calendar-item--checked) {
        background-color: var(--general);
    }
    :deep(.uni-datetime-picker--btn) {
        background-color: var(--general);
    }
}
.birthday-input {
    display: flex;
    width: 100%;
    align-items: center;
    justify-content: end;
    height: 100%;
    font-size: 23rpx;
    color: #999;
}

.btn-box {
    padding: 25rpx 15rpx;
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 100%;

    .btn {
        width: 48%;
        font-size: 28rpx;
        height: 100%;
    }
}
</style>
