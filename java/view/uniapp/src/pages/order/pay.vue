<template>
    <tig-layout>
        <tig-navbar :title="navbarTitle" />
        <template v-if="loading">
            <view class="page-loading">
                <view class="ico" />
            </view>
        </template>
        <template v-if="order">
            <view class="order-pay-warp">
                <view class="order_infos">
                    <view class="order_amount">
                        <template v-if="typeText !== 'recharge'">
                            <format-price :is-bootom="false" :price-data="order.unpaidAmount" />
                        </template>
                        <template v-else>
                            <format-price :is-bootom="false" :price-data="order.amount" />
                        </template>
                    </view>
                    <template v-if="typeText !== 'recharge'">
                        <view class="order_sn">
                            {{ $t("订单号") }}：
                            <text>{{ order.orderSn }}</text>
                        </view>
                    </template>
                </view>

                <template v-if="order.payTypeId !== 3 && paymentList.length > 0">
                    <payment
                        v-model="paymentType"
                        :payment-list="paymentList"
                        :balance="userStore.userInfo.balance || 0"
                    />
                </template>

                <template v-if="order.payTypeId === 3">
                    <offline :offline-payment-list="offlinePaymentList" />
                </template>

                <template v-if="typeText !== 'recharge' && typeText !== 'premiumMember'">
                    <view class="other-info">
                        <view class="tit">{{ $t("其它信息") }}</view>
                        <view class="item">
                            {{ $t("支付方式：") }}
                            <text>{{ $t(payTypeText) }}</text>
                        </view>
                        <view class="item">
                            {{ $t("配送方式") }}：
                            <text>{{ order.shippingTypeName || $t("专线物流") }}</text>
                        </view>
                        <view class="item">
                            {{ $t("下单时间") }}：
                            <text>{{ order.addTime }}</text>
                        </view>
                    </view>
                </template>

                <template v-if="order.payTypeId !== 3">
                    <tig-fixed-placeholder background-color="#fff">
                        <view class="btn-box">
                            <tig-button :disabled="paymentDisabled" @click="handlePay"> {{ $t("立即支付") }}</tig-button>
                        </view>
                    </tig-fixed-placeholder>
                </template>
            </view>
        </template>
        <!-- #ifdef H5 -->
        <div ref="formContainer" />
        <!-- #endif -->
    </tig-layout>
</template>

<script setup lang="ts">
/**
 * 订单支付页
 * - 展示订单金额、订单号、支付方式、配送方式等信息
 * - 支持多种支付方式：微信、支付宝、线下支付等
 * - 支持订单支付和充值支付两种场景
 * - 轮询检查支付状态，成功后跳转结果页
 */
import payment from "./src/payment.vue";
import offline from "./src/offline.vue";
import { onLoad } from "@dcloudio/uni-app";
import { computed, onBeforeUnmount, ref } from "vue";
import { orderPayInfo, creatPay, checkPayStatus, rechargeOrderPay, rechargeOrderCreate, checkRechargePayStatus, createDepositPayment } from "@/api/order/pay";
import type { Order, OfflinePaymentList } from "@/types/order/pay";
import { useConfigStore } from "@/store/config";
import { useUserStore } from "@/store/user";
import { useI18n } from "vue-i18n";
import { redirect, isOverseas } from "@/utils";
import { openExternalPaymentUrl } from "@/utils/payment";
import { launchApplePay } from "@/utils/applePay";
import type { OrderPresaleInfo } from "@/types/presale/presale";

const { t } = useI18n();

const configStore = useConfigStore();
const userStore = useUserStore();

const navbarTitle = ref(t("订单支付"));

const loading = ref(false);
const paymentList = ref<string[]>([]);
const paymentDisabled = ref(false);
const offlinePaymentList = ref<OfflinePaymentList>({} as OfflinePaymentList);
const paymentType = ref("");
const order = ref<Order>();
const presaleInfo = ref<OrderPresaleInfo | null>(null);
const formContainer = ref<HTMLElement | null>(null);
const typeText = ref("");
const payTypeText = ref("");
const isPremiumMember = ref(false);

const orderId = ref<number | null>(null);
onLoad((options) => {
    if (options && options.orderId) {
        if (!options.type) {
            loadOrderPayInfo(options.orderId);
        } else {
            typeText.value = options.type;
            navbarTitle.value = t("充值支付");
            getRechargeOrderPay(options.orderId);
        }
    }
});

/**
 * @description 加载订单支付信息
 * 获取订单详情、可用支付方式、线下支付配置等
 * @param id 订单ID
 */
const loadOrderPayInfo = async (id: number) => {
    loading.value = true;
    try {
        const result = await orderPayInfo(id);
        order.value = result.order;
        paymentList.value = result.paymentList;
        paymentType.value = paymentList.value[0];
        offlinePaymentList.value = result.offlinePaymentList;
        orderId.value = result.order.orderId;
        payTypeText.value = result.order.payTypeId === 3 ? t("线下支付") : t("在线支付");

        if (result.order.presaleInfo) {
            presaleInfo.value = result.order.presaleInfo;
        }
    } catch (error: any) {
        uni.showToast({
            title: error.message,
            duration: 1500,
            icon: "none"
        });
        setTimeout(function () {
            redirect({
                url: "/pages/user/order/list",
                mode: "redirectTo"
            });
        }, 1500);
    } finally {
        loading.value = false;
    }
};

/**
 * @description 加载充值订单支付信息
 * @param id 充值订单ID
 */
const getRechargeOrderPay = async (id: number) => {
    try {
        const result = await rechargeOrderPay({ orderId: id });
        order.value = result.order;
        // Apple Pay 充值为二期，一期充值场景过滤隐藏 applepay（后端充值 create 不处理）
        let rechargePaymentList = result.paymentList || [];
        rechargePaymentList = rechargePaymentList.filter((item: string) => item !== "applepay");
        paymentList.value = rechargePaymentList;
        paymentType.value = paymentList.value[0];
        orderId.value = result.order.orderId;
    } catch (error: any) {
        uni.showToast({
            title: error.message,
            duration: 1500,
            icon: "none"
        });
        setTimeout(function () {
            uni.navigateTo({
                url: "/pages/user/account/index"
            });
        }, 1500);
    } finally {
        loading.value = false;
    }
};


let pollingTimer: ReturnType<typeof setTimeout> | null = null;
let pollingGeneration = 0;
let paymentSettled = false;

const stopPaymentPolling = () => {
    pollingGeneration++;
    if (pollingTimer) {
        clearTimeout(pollingTimer);
        pollingTimer = null;
    }
};

const settlePayment = (callback: () => void) => {
    if (paymentSettled) return;
    paymentSettled = true;
    stopPaymentPolling();
    callback();
};

const redirectAfterPaymentSuccess = () => {
    if (isPremiumMember.value) {
        uni.showToast({ title: t("支付完成"), icon: "success", duration: 2000 });
        setTimeout(() => redirect({ url: "/pages/member/premiumMember/index", mode: "redirectTo" }), 2000);
        return;
    }
    uni.showToast({ title: t("支付成功"), duration: 1500, icon: "none" });
    setTimeout(() => {
        if (typeText.value === "recharge") {
            redirect({ url: "/pages/user/account/index", mode: "redirectTo" });
        } else if (isPremiumMember.value) {
            redirect({ url: "/pages/member/premiumMember/index", mode: "redirectTo" });
        } else {
            redirect({
                url: `/pages/order/payStatus?id=${orderId.value}${
                    presaleInfo.value?.saleMode === 2 && presaleInfo.value?.presaleStatus !== 1 ? "&saleMode=1" : ""
                }`,
                mode: "redirectTo"
            });
        }
    }, 1500);
};

const startPaymentPolling = () => {
    stopPaymentPolling();
    paymentSettled = false;
    const generation = pollingGeneration;
    let count = 0;

    const poll = async () => {
        if (paymentSettled || generation !== pollingGeneration) return;
        count++;
        const paid = typeText.value === "recharge" ? await __checkRechargePayStatus() : await __checkPayStatus();
        if (paymentSettled || generation !== pollingGeneration) return;
        if (paid) {
            settlePayment(redirectAfterPaymentSuccess);
            return;
        }
        if (count >= 50) {
            settlePayment(() => {
                uni.showToast({ title: t("支付已超时"), duration: 1500, icon: "none" });
                redirect({
                    url:
                        typeText.value === "recharge"
                            ? "/pages/user/account/index"
                            : isPremiumMember.value
                              ? "/pages/member/premiumMember/index"
                              : "/pages/user/order/list",
                    mode: "redirectTo"
                });
            });
            return;
        }
        pollingTimer = setTimeout(poll, 2000);
    };

    pollingTimer = setTimeout(poll, 2000);
};

// #ifdef H5
/**
 * @description 拉起 Apple Pay 内联支付（仅 H5，不跳中间页）
 * 支付成功后跳转支付结果页；取消静默、失败提示
 */
const startApplePay = () => {
    if (!orderId.value) return;
    const isDeposit = presaleInfo.value?.saleMode === 2 && presaleInfo.value?.presaleStatus !== 1;
    launchApplePay({
        id: orderId.value,
        isDeposit,
        amount: order.value?.unpaidAmount ?? 0,
        label: order.value?.orderSn || "Apple Pay",
        onSuccess: () => {
            uni.showToast({ title: t("支付成功"), duration: 1500 });
            setTimeout(() => {
                redirect({
                    url: `/pages/order/payStatus?id=${orderId.value}${isDeposit ? "&saleMode=1" : ""}`,
                    mode: "redirectTo"
                });
            }, 1500);
        },
        onFail: (msg?: string) => {
            uni.showToast({ title: msg || t("支付失败"), duration: 1500, icon: "none" });
        }
    });
};
// #endif

/**
 * @description 发起支付
 * 根据选择的支付方式调用对应的支付接口：
 * - 支付宝：H5表单提交/App原生支付
 * - 微信：H5跳转/小程序支付/公众号JSAPI/App原生支付
 * - 第三方支付：雅班/PayPal/云支付等
 * 启动轮询检查支付状态，超时50次（100秒）视为失败
 */
const handlePay = async () => {
    paymentDisabled.value = true;
    stopPaymentPolling();
    paymentSettled = false;

    if (paymentType.value === "") {
        uni.showToast({
            title: t("请选择支付方式"),
            duration: 1500,
            icon: "none"
        });
        paymentDisabled.value = false;
        return;
    }

    // #ifdef H5
    // Apple Pay：H5 内联 ApplePaySession 流程（必须在点击手势内同步拉起）
    if (paymentType.value === "applepay") {
        paymentDisabled.value = false;
        startApplePay();
        return;
    }
    // #endif

    try {
        if (orderId.value) {
            let result;
            if (typeText.value === "recharge") {
                // 充值
                result = await __rechargeOrderCreate();
            } else {
                    // 订单
                    result = await __orderCreatPay();
            }
            if (result) {
                let launched = false;
                // 支付宝（含汇付支付宝 huifuAlipay、拉卡拉支付宝 lakalaAlipay，复用现有 alipay 通道）
                if (
                    paymentType.value === "alipay"
                    || paymentType.value === "huifuAlipay"
                    || paymentType.value === "lakalaAlipay"
                    || paymentType.value === "yeepayAlipay"
                ) {
                    if (configStore.XClientType === "h5") {
                        alipay(result.payInfo.html);
                        launched = true;
                    }
                    if (configStore.XClientType === "app") {
                        appPay(result.payInfo);
                        launched = true;
                    }
                }
                // 微信（含汇付微信 huifuWechat、拉卡拉微信 lakalaWechat，复用现有 wechat 通道；拉卡拉微信 H5 走聚合收银台 url）
                if (
                    paymentType.value === "wechat" ||
                    paymentType.value === "yunpayWechat"
                    || paymentType.value === "huifuWechat"
                    || paymentType.value === "lakalaWechat"
                    || paymentType.value === "yeepayWechat"
                ) {
                    if (configStore.XClientType === "h5") {
                        window.open(result.payInfo.url, "_blank");
                        launched = true;
                    }
                    if (configStore.XClientType === "miniProgram") {
                        miniProgramPay(result.payInfo);
                        launched = true;
                    }
                    if (configStore.XClientType === "wechat") {
                        onBridgeReady(result.payInfo);
                        launched = true;
                    }
                    if (configStore.XClientType === "app") {
                        appPay(result.payInfo);
                        launched = true;
                    }
                }

                const urlPayTypes = ["yabanpayWechat", "yabanpayAlipay", "paypal"];
                const codeUrlPayTypes = ["yunpayAlipay", "yunpayYunshanfu"];
                if (urlPayTypes.includes(paymentType.value) || codeUrlPayTypes.includes(paymentType.value)) {
                    const externalUrl = urlPayTypes.includes(paymentType.value) ? result.payInfo?.url : result.payInfo?.codeUrl;
                    launched = openExternalPaymentUrl(externalUrl, {
                        clientType: configStore.XClientType,
                        onFail: () => {
                            settlePayment(() => uni.showToast({ title: t("无法打开支付页面"), icon: "none" }));
                        }
                    });
                }

                if (launched) {
                    startPaymentPolling();
                } else {
                    uni.showToast({ title: t("当前客户端暂不支持该支付方式"), icon: "none" });
                }
            }
        }
    } catch (error: any) {
        uni.showToast({
            title: error.message,
            duration: 1500,
            icon: "none"
        });
    } finally {
        paymentDisabled.value = false;
    }
};

/**
 * @description 创建充值支付信息
 * 微信小程序需先获取登录code
 */
const __rechargeOrderCreate = async () => {
    let result;
    // #ifdef APP-PLUS || H5 || MP-QQ || MP-TOUTIAO || MP-BAIDU || MP-ALIPAY
    result = await __creatRechargePay();
    return result;
    // #endif
    // #ifdef MP-WEIXIN
    return new Promise((resolve, reject) => {
        wx.login({
            success: (res: any) => {
                __creatRechargePay(res.code)
                    .then((result: any) => {
                        resolve(result);
                    })
                    .catch((err: any) => {
                        reject(err);
                    });
            },
            fail: (err: any) => {
                reject(err);
            }
        });
    });
    // #endif
};

/**
 * @description 创建订单支付信息
 * 微信小程序需先获取登录code
 */
const __orderCreatPay = async () => {
    let result;
    // #ifdef APP-PLUS || H5 || MP-QQ || MP-TOUTIAO || MP-BAIDU || MP-ALIPAY
    result = await __creatPay();
    return result;
    // #endif
    // #ifdef MP-WEIXIN
    return new Promise((resolve, reject) => {
        wx.login({
            success: (res: any) => {
                __creatPay(res.code)
                    .then((result: any) => {
                        resolve(result);
                    })
                    .catch((err: any) => {
                        reject(err);
                    });
            },
            fail: (err: any) => {
                reject(err);
            }
        });
    });
    // #endif
};

type TData = {
    id: number;
    type: string;
    code?: number;
};
const __creatPay = async (code?: number) => {
    let result;
    try {
        const data: TData = {
            id: orderId.value!,
            type: paymentType.value
        };
        if (code) {
            data.code = code;
        }
        // 定金预售：在“付定金”阶段调用定金支付接口；尾款阶段/普通订单走普通支付
        result = presaleInfo.value?.saleMode === 2 && presaleInfo.value?.presaleStatus !== 1 ? await createDepositPayment(data) : await creatPay(data);
    } catch (error: any) {
        uni.showToast({
            title: error.message,
            duration: 1500,
            icon: "none"
        });
    } finally {
        return result;
    }
};

const __creatRechargePay = async (code?: number) => {
    let result;
    try {
        const data: TData = {
            id: orderId.value!,
            type: paymentType.value
        };
        if (code) {
            data.code = code;
        }
        result = await rechargeOrderCreate(data);
    } catch (error: any) {
        uni.showToast({
            title: error.message,
            duration: 1500,
            icon: "none"
        });
    } finally {
        return result;
    }
};


/**
 * @description 轮询检查订单支付状态
 * 支付成功则跳转支付结果页
 */
const __checkPayStatus = async (): Promise<boolean> => {
    try {
        const result = await checkPayStatus({ id: orderId.value });
        return result == 1;
    } catch (error) {
        return false;
    }
};

/**
 * @description 轮询检查充值订单支付状态
 * 支付成功则跳转账户中心
 */
const __checkRechargePayStatus = async (): Promise<boolean> => {
    try {
        const result = await checkRechargePayStatus({ id: orderId.value });
        return result == 1;
    } catch (error) {
        return false;
    }
};

/**
 * @description H5支付宝支付
 * 通过表单提交方式跳转支付宝支付页
 * @param html 支付宝返回的表单HTML
 */
const alipay = (html: string) => {
    if (formContainer.value) {
        formContainer.value.innerHTML = html;
        // 提交表单
        const forms = document.forms;
        forms && forms[0].submit();
    }
};

/**
 * @description 微信小程序支付
 * @param payInfo 支付参数
 */
const miniProgramPay = (payInfo: any) => {
    wx.requestPayment({
        timeStamp: String(payInfo.timeStamp),
        nonceStr: payInfo.nonceStr,
        package: payInfo.package,
        signType: payInfo.signType,
        paySign: payInfo.paySign,
        success(res: any) {
            settlePayment(redirectAfterPaymentSuccess);
        },
        fail(res: any) {
            settlePayment(() => {
                uni.showToast({ title: t("支付失败"), duration: 1500, icon: "none" });
                setTimeout(() => {
                    redirect({
                        url: typeText.value === "recharge" ? "/pages/user/account/index" : "/pages/user/order/list",
                        mode: "redirectTo"
                    });
                }, 1500);
            });
        }
    });
};

/**
 * @description 微信公众号JSAPI支付
 * @param payInfo 支付参数
 */
const onBridgeReady = (payInfo: any) => {
    WeixinJSBridge.invoke(
        "getBrandWCPayRequest",
        {
            ...payInfo
        },
        function (res: any) {
            if (res.errMsg == "getBrandWcpayRequest:ok") {
                // 使用以上方式判断前端返回,微信团队郑重提示：
                //res.err_msg将在用户支付成功后返回ok，但并不保证它绝对可靠。
            }
        }
    );
};

/**
 * @description App端原生支付
 * 支持微信支付和支付宝支付
 * @param payInfo 支付参数
 */
const appPay = (payInfo: any) => {
    //微信App支付独立结构
    if (
        paymentType.value === "wechat"
        || paymentType.value === "huifuWechat"
        || paymentType.value === "lakalaWechat"
        || paymentType.value === "yeepayWechat"
    ) {
        uni.requestPayment({
            provider: "wxpay",
            orderInfo: {
                appid: payInfo.appId,
                noncestr: payInfo.nonceStr,
                package: payInfo.package,
                partnerid: String(payInfo.partnerId),
                prepayid: payInfo.prepayId,
                timestamp: payInfo.timeStamp,
                sign: payInfo.sign
            },
            success() {
                uni.showToast({
                    title: t("支付成功"),
                    duration: 1500,
                    icon: "none"
                });
            },
            fail() {
                uni.showToast({
                    title: t("支付失败"),
                    duration: 1500,
                    icon: "none"
                });
                settlePayment(() => {});
            },
            complete() {}
        });
        return;
    }
    uni.requestPayment({
        provider: payInfo.provider,
        orderInfo: payInfo.orderInfo,
        success() {
            uni.showToast({
                title: t("支付成功"),
                duration: 1500,
                icon: "none"
            });
        },
        fail() {
            uni.showToast({
                title: t("支付失败"),
                duration: 1500,
                icon: "none"
            });
            settlePayment(() => {});
        },
        complete() {}
    });
};

onBeforeUnmount(() => {
    typeText.value = "";
    stopPaymentPolling();
});
</script>

<style lang="scss" scoped>
.btn-box {
    padding: 25rpx;
}
.order-pay-warp {
    padding-bottom: 100rpx;
}
.order_infos {
    text-align: center;
    background: #fff;
    padding: 50rpx 0 60rpx;
    border-radius: 0 0 18rpx 18rpx;
}
.order_infos .order_amount {
    font-size: 46rpx;
    color: var(--general);
    font-weight: bold;
    :deep(.util) {
        font-size: 30rpx;
        font-weight: normal;
        position: relative;
        top: -5rpx;
    }
}

.other-info {
    background: #fff;
    padding: 30rpx 36rpx 30rpx;
    border-radius: 18rpx;
    margin-top: 20rpx;
    margin-bottom: 40rpx;
}
.other-info .tit {
    font-size: 22rpx;
    font-weight: bold;
}
.other-info .item {
    color: #888;
    font-size: 24rpx;
    padding-top: 20rpx;
}
.other-info .item text {
    color: #333;
}

.pay-btn-warp {
    position: fixed;
    display: block;
    background: #fff;
    bottom: 0;
    left: 0;
    width: 100%;
}
.pay-btn-warp .pay-btn {
    text-align: center;
    height: 80rpx;
    line-height: 80rpx;
    padding: 0;
    display: block;
    margin: 20rpx 30rpx;
}
.offline-warp {
    border-radius: 20rpx;
    padding: 10rpx;
    background-color: #fff;
    margin: 20rpx 0 0;
    overflow: hidden;

    .offline-menu {
        height: 100rpx;
        width: 100%;
        background-color: #fff;
        display: flex;
        align-items: center;
        box-sizing: content-box;
        margin-bottom: 15rpx;

        .offline-menu-item {
            flex: 1;
            height: 100%;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 28rpx;
            color: #999;
            position: relative;

            &.active {
                color: #333;
                font-weight: bold;

                &:after {
                    content: "";
                    position: absolute;
                    bottom: 0;
                    height: 5rpx;
                    width: 80rpx;
                    background-color: var(--general);
                    border-radius: 10rpx;
                }
            }
        }
    }
}
.offline-content {
    padding: 20rpx;
    line-height: 45rpx;
}
</style>
