<template>
    <view>
        <tig-layout>
            <tig-navbar background="transparent" />
            <view class="bg" />
            <view class="container">
                <view class="card-title">
                    <text class="title">{{ $t("礼品卡兑换") }}</text>
                </view>
                <view class="card-subtitle">
                    <view class="title">
                        <text>{{ $t("一卡在手，好礼不愁") }}</text>
                    </view>
                </view>
                <view class="gift-card">
                    <view class="card-box exchange-form">
                        <view class="card-edit-content">
                            <uni-forms ref="formRef" :model-value="formState" label-width="170rpx" label-position="top">
                                <uni-forms-item :label="$t('卡号')" name="cardNumber">
                                    <uni-easyinput
                                        v-model="formState.cardNumber"
                                        :input-border="false"
                                        :placeholder="$t('请输入卡号')"
                                        primary-color="rgb(192, 196, 204)"
                                    />
                                </uni-forms-item>
                                <uni-forms-item :label="$t('卡密')" name="cardPassword">
                                    <uni-easyinput
                                        v-model="formState.cardPassword"
                                        type="password"
                                        :input-border="false"
                                        :placeholder="$t('请输入卡密')"
                                        primary-color="rgb(192, 196, 204)"
                                    />
                                </uni-forms-item>
                            </uni-forms>
                            <view class="action">
                                <tig-button class="btn" :loading="loading" @click="handleExchange">{{ $t("立即兑换") }}</tig-button>
                            </view>
                        </view>
                    </view>
                    <view class="card-box">
                        <view class="card-content">
                            <view class="title">{{ $t("兑换说明") }}</view>
                            <view class="tips">
                                <view>{{ $t("1. 储值卡输入卡密进行兑换将自动充值到余额") }}</view>
                                <view>{{ $t("2. 兑换卡输入卡密进行兑换后将跳转至该商品详情页进行兑换") }}</view>
                            </view>
                        </view>
                    </view>
                </view>
            </view>
        </tig-layout>
    </view>
</template>
<script setup lang="ts">
import { computed, nextTick, ref } from "vue";
import { onLoad, onShow } from "@dcloudio/uni-app";
import { useI18n } from "vue-i18n";
import { useUserStore } from "@/store/user";
import { redirect, staticResource } from "@/utils";
import { giftCardExchange } from "@/api/giftCard/giftCard";
import type { GiftCardExchangeParams } from "@/types/giftCard/giftCard";
import { handleLogin } from "@/utils/handleLogin";

const { t } = useI18n();
const userStore = useUserStore();
const formState = ref<GiftCardExchangeParams>({
    cardNumber: "",
    cardPassword: ""
});

const hasToken = computed(() => {
    return userStore.token || uni.getStorageSync("token");
});
const loading = ref(false);
const formRef = ref();
const backgroundImg = computed(() => {
    return `url(${staticResource("giftCard/exchange_bg.png")}) center no-repeat`;
});

const handleExchange = async () => {
    formRef.value
        .validate()
        .then(() => {
            exchange();
        })
        .catch((err: any) => {
            console.log("表单错误信息：", err);
        });
};

/**
 * 礼品卡兑换
 */
const exchange = async () => {
    try {
        if (loading.value) {
            return;
        }
        loading.value = true;
        const result = await giftCardExchange(formState.value);
        // 兑换礼品卡跳转到订单确认页
        if (result.exchangeProductToCart?.flowType) {
            redirect({ url: `/pages/order/check?flowType=${result.exchangeProductToCart?.flowType}` });
        } else {
            // 储值卡跳转到兑换成功页面
            redirect({ url: "/pages/giftCard/order/payStatus" });
        }
    } catch (error: any) {
        uni.showToast({
            title: t(error.message),
            icon: "none"
        });
        console.error(error);
    } finally {
        loading.value = false;
    }
};

const rules = {
    cardNumber: {
        rules: [{ required: true, errorMessage: t("请输入卡号") }]
    },
    cardPassword: {
        rules: [{ required: true, errorMessage: t("请输入卡密") }]
    }
};

onShow(() => {
    nextTick(() => {
        formRef.value.setRules(rules);
    });
});

// 页面加载
onLoad(() => {
    // 需要登录
    if (!hasToken.value) {
        handleLogin();
        return;
    }
    // if (options?.cardNumber) {
    //     formState.value.cardNumber = options.cardNumber;
    // }
});
</script>

<style scoped lang="scss">
.bg {
    height: 617rpx;
    background: v-bind(backgroundImg); // url("@/static/images/giftCard/exchange_bg.png") center no-repeat;
    background-size: 100% 100%;
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    z-index: 0;
}
.container {
    position: relative;
    z-index: 1;
}
.card-title {
    padding-left: 33rpx;
    .title {
        // 字体颜色渐变
        background: linear-gradient(90deg, #975204 30%, #5c2b00);
        font-weight: bold;
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        font-size: 55rpx;
        letter-spacing: 20rpx;
    }
}
.card-subtitle {
    display: flex;
    margin-top: 23rpx;
    .title {
        border: 1rpx solid #fff;
        border-top-right-radius: 30rpx;
        border-bottom-right-radius: 30rpx;
        overflow: hidden;
        color: #eb9655;
        background: #ffe6d4;
        width: 458rpx;
        height: 63rpx;
        display: flex;
        font-size: 28rpx;
        line-height: 28rpx;
        align-items: center;
        padding-left: 50rpx;
        letter-spacing: 9rpx;
    }
}
.gift-card {
    position: relative;
    z-index: 1;
    padding: 20rpx;
    display: flex;
    flex-direction: column;
    gap: 20rpx;
    margin-top: 16rpx;
}
.card-box {
    border-radius: 16rpx;
    overflow: hidden;
    .card-edit-content {
        display: flex;
        flex-direction: column;
        gap: 42rpx;
        padding: 70rpx 30rpx 50rpx;
        :deep(.uni-forms-item__label) {
            font-size: 32rpx;
            font-weight: bold;
            color: #2a2a2a;
            padding-bottom: 20rpx;
        }
        :deep(.uni-easyinput__content) {
            background-color: #f4f4f4 !important;
            border-color: #f4f4f4 !important;
            .uni-input-placeholder {
                color: #bfbfbf;
                font-size: 28rpx;
            }
            .uni-easyinput__content-input {
                background-color: #f4f4f4 !important;
                border-color: #f4f4f4 !important;
                height: 90rpx;
                font-size: 28rpx;
            }
        }
        .form-item {
            .form-label {
                font-size: 32rpx;
                font-weight: bold;
                color: #2a2a2a;
                padding-bottom: 20rpx;
            }
        }
        .paste-action {
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 10rpx;
            color: #fc6f4e;
            font-size: 28rpx;
            cursor: pointer;
            padding: 10rpx 0;
            .iconfont-h5 {
                font-size: 32rpx;
            }
        }
        .action {
            padding-top: 30rpx;
            .btn {
                height: 90rpx;
                border-radius: 8rpx;
                color: #fff;
                background: linear-gradient(90deg, #fd4962, #fc6f4e);
                border-color: transparent !important;
                // display: flex;
                // align-items: center;
                // justify-content: center;
                // color: #fff;
                // position: relative;
                // &::before {
                //     content: "";
                //     position: absolute;
                //     inset: 0;
                //     background: #000;
                //     opacity: 0;
                // }
                // &--active {
                //     &::before {
                //         opacity: 0.15;
                //     }
                // }
            }
        }
    }
    .card-content {
        display: flex;
        flex-direction: column;
        gap: 20rpx;
        padding: 30rpx;
        .title {
            color: #262626;
            font-size: 32rpx;
        }
        .tips {
            font-size: 28rpx;
            color: #262626;
            line-height: 44rpx;
            padding-right: 60rpx;
        }
    }
    .input {
        // flex: 1;
        height: 90rpx;
        background: #f4f4f4;
        border-radius: 8rpx;
        box-sizing: border-box;
        padding: 0 30rpx;
        font-size: 28rpx;

        .input-placeholder {
            color: #bfbfbf;
        }
    }
}

.exchange-form {
    background: linear-gradient(180deg, #fff5ed, #ffffff 30%);
    box-shadow: 0rpx 0rpx 0rpx 1.5rpx #ffffff;
}
</style>
