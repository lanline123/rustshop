<template>
    <tig-layout>
        <tig-navbar title="收银台" :custom-back="true" @back="onClickBack" />

        <template v-if="order">
            <view class="cashier">
                <!-- 金额与倒计时 -->
                <view class="cashier-head">
                    <view class="cashier-amount">
                        <format-price
                            :is-bottom="false"
                            :price-data="payAmount"
                            :currency-style="{ fontWeight: '500', fontSize: '34rpx' }"
                            :font-style="{ fontWeight: '500', fontSize: '64rpx', lineHeight: '1' }"
                            :decimals-style="{ fontWeight: '500', fontSize: '40rpx' }"
                        />
                    </view>
                    <view v-if="countdownText" class="cashier-countdown">
                        {{ $t("请在") }}<text class="countdown-time">{{ countdownText }}</text
                        >{{ $t("内付款") }}
                    </view>
                    <view v-else-if="payExpired" class="cashier-countdown cashier-expired">{{ $t("支付已过期") }}</view>
                </view>

                <!-- 支付方式 -->
                <view class="cashier-panel">
                    <view class="panel-title">{{ $t("支付方式") }}</view>
                    <view class="channel-list">
                        <view v-for="channel in displayPaymentList" :key="channel" class="channel-item" @click="selectChannel(channel)">
                            <view class="channel-info">
                                <image lazy-load class="channel-icon" :src="staticResource('payment/pay_' + channel + '.png')" />
                                <view class="channel-text">
                                    <view class="channel-name">{{ $t(PAYMENT_TEXT_MAP[channel] || channel) }}</view>
                                </view>
                            </view>
                            <view class="radio-circle" :class="{ checked: paymentType === channel }" />
                        </view>
                    </view>
                </view>

                <!-- 底部重新支付 -->
                <tig-fixed-placeholder background-color="#fff">
                    <view class="cashier-footer">
                        <tig-button
                            class="pay-btn"
                            :disabled="paymentDisabled || payExpired || !paymentType"
                            :loading="paymentDisabled"
                            loading-text="支付中..."
                            @click="handlePay"
                        >
                            <view class="pay-btn-inner">
                                <text>{{ $t(isRecharge ? "立即支付" : "重新支付") }}</text>
                                <format-price
                                    :is-bottom="false"
                                    :price-data="payAmount"
                                    :currency-style="{ color: '#fff', fontSize: '26rpx', fontWeight: 'bold' }"
                                    :font-style="{ color: '#fff', fontSize: '30rpx', fontWeight: 'bold' }"
                                    :decimals-style="{ color: '#fff', fontSize: '26rpx', fontWeight: 'bold' }"
                                />
                            </view>
                        </tig-button>
                    </view>
                </tig-fixed-placeholder>
            </view>
        </template>

        <!-- 放弃支付挽留弹窗 -->
        <abandonPayDialog
            v-model:show="showAbandon"
            :amount="payAmount"
            :payment-type="paymentType"
            :reward-points="willSendPoints"
            :discount-amount="memberDiscountAmount"
            @abandon="confirmAbandon"
            @confirm="continuePay"
            @change="onChangePay"
        />

        <tig-loadingpage v-model="loading" />
    </tig-layout>
</template>

<script setup lang="ts">
/**
 * @description 收银台落地页
 * - 结算页直选在线支付发起后的落地/返回页：展示应付金额、付款倒计时、支付方式，支持重新支付
 * - 返回时弹出放弃支付挽留：放弃/关闭弹窗跳待付款订单，立即支付重新发起
 * - 订单已支付时直接跳支付结果页，避免重复支付
 * - 支付执行统一委托 usePayment
 */
import { computed, onUnmounted, ref } from "vue";
import { onLoad, onBackPress } from "@dcloudio/uni-app";
import { orderPayInfo, rechargeOrderPay } from "@/api/order/pay";
import type { Order } from "@/types/order/pay";
import { usePayment, PAYMENT_TEXT_MAP } from "@/hooks/usePayment";
import { isOverseas, staticResource, redirect } from "@/utils";
import { useI18n } from "vue-i18n";
import abandonPayDialog from "./src/abandonPayDialog.vue";

const { t } = useI18n();
const { startPay, stopPolling } = usePayment();
/** 外部支付返回会重新加载收银台，用订单维度的短期缓存恢复用户选择 */
const PAYMENT_SELECTION_CACHE_KEY = "cashierPaymentSelection";
const PAYMENT_SELECTION_CACHE_TTL = 30 * 60 * 1000;

interface PaymentSelectionCache {
    orderId: number;
    paymentType: string;
    ts: number;
    entryFrom?: string;
}

const loading = ref(false);
const order = ref<Order>();
const paymentList = ref<string[]>([]);
const paymentType = ref("");
const paymentDisabled = ref(false);
const presaleInfo = ref<any>(null);
const orderId = ref<number | null>(null);
/** 进入收银台前已选择的支付渠道，渠道仍可用时优先恢复 */
const initialPaymentType = ref("");
/** 充值支付：type=recharge */
const isRecharge = ref(false);
/**
 * 进入来源：settle=结算页直选支付（放弃后去待付款列表）；
 * 其余（订单详情/列表/充值）通过 navigateTo 进入，放弃后返回上一页
 */
const entryFrom = ref("");

// 跨境支付渠道仅在跨境运行环境展示，避免普通版误显示或选择。
const displayPaymentList = computed(() =>
    paymentList.value.filter((channel) => {
        return true;
    })
);

const getCachedPaymentType = (id: number): string => {
    const cache = uni.getStorageSync(PAYMENT_SELECTION_CACHE_KEY) as PaymentSelectionCache | undefined;
    if (cache && Number(cache.orderId) === id && cache.paymentType && Date.now() - Number(cache.ts) < PAYMENT_SELECTION_CACHE_TTL) {
        if (!entryFrom.value && cache.entryFrom) entryFrom.value = cache.entryFrom;
        return cache.paymentType;
    }
    if (cache) uni.removeStorageSync(PAYMENT_SELECTION_CACHE_KEY);
    return "";
};

const cachePaymentType = () => {
    if (!orderId.value || !paymentType.value) return;
    uni.setStorageSync(PAYMENT_SELECTION_CACHE_KEY, {
        orderId: orderId.value,
        paymentType: paymentType.value,
        ts: Date.now(),
        entryFrom: entryFrom.value
    });
};

const clearCachedPaymentType = () => {
    const cache = uni.getStorageSync(PAYMENT_SELECTION_CACHE_KEY) as PaymentSelectionCache | undefined;
    if (!cache || Number(cache.orderId) === orderId.value) {
        uni.removeStorageSync(PAYMENT_SELECTION_CACHE_KEY);
    }
};

const resolvePaymentType = (id: number) => {
    // 缓存可能是用户进入收银台后重新选择的渠道，应优先于入口 URL 中的初始渠道
    const preferredPaymentType = getCachedPaymentType(id) || initialPaymentType.value;
    paymentType.value = displayPaymentList.value.includes(preferredPaymentType) ? preferredPaymentType : displayPaymentList.value[0] || "";
    cachePaymentType();
};

const showAbandon = ref(false);
/** 立即支付 / 更换方式关闭弹窗时，跳过「放弃」跳转 */
const skipAbandonRedirect = ref(false);
/** 本单赠送积分（挽留弹窗） */
const willSendPoints = ref<number | string>(0);
/** 会员优惠立减金额（挽留弹窗） */
const memberDiscountAmount = ref<number | string>(0);

/** 应付金额：充值单取 amount，普通订单取 unpaidAmount */
const payAmount = computed<string | number>(() => {
    if (!order.value) return "";
    if (isRecharge.value) return order.value.amount ?? order.value.unpaidAmount ?? "";
    return order.value.unpaidAmount ?? "";
});

// ----- 付款倒计时 -----
const remainSeconds = ref(0);
const payExpired = ref(false);
let countdownTimer: any = null;

const countdownText = computed(() => {
    if (remainSeconds.value <= 0) return "";
    const h = Math.floor(remainSeconds.value / 3600);
    const m = Math.floor((remainSeconds.value % 3600) / 60);
    const s = remainSeconds.value % 60;
    const pad = (n: number) => (n < 10 ? "0" + n : String(n));
    return `${pad(h)}:${pad(m)}:${pad(s)}`;
});

/** 服务端时间转时间戳（兼容秒/毫秒时间戳与 iOS 的 yyyy-MM-dd HH:mm:ss） */
const parseTime = (value?: string | number): number => {
    if (value === undefined || value === null || value === "") return 0;
    if (typeof value === "number") return value < 1e12 ? value * 1000 : value;
    const str = String(value);
    if (/^\d+$/.test(str)) {
        const timestamp = Number(str);
        return timestamp < 1e12 ? timestamp * 1000 : timestamp;
    }
    const [datePart, timePart = "00:00:00"] = str.split(" ");
    const [y, mo, d] = datePart.split("-").map(Number);
    const [h, mi, se] = timePart.split(":").map(Number);
    return new Date(y, (mo || 1) - 1, d || 1, h || 0, mi || 0, se || 0).getTime();
};

const startCountdown = () => {
    stopCountdown();
    remainSeconds.value = 0;
    payExpired.value = false;

    // 充值订单后端没有截止时间，不在前端伪造 30 分钟有效期。
    const endTsFromServer = parseTime(order.value?.payEndTime);
    if (!endTsFromServer) return;
    const serverNow = parseTime(order.value?.serverTime);
    const endTs = serverNow ? Date.now() + Math.max(0, endTsFromServer - serverNow) : endTsFromServer;
    const tick = () => {
        const diff = Math.floor((endTs - Date.now()) / 1000);
        remainSeconds.value = diff > 0 ? diff : 0;
        if (remainSeconds.value <= 0) {
            payExpired.value = true;
            stopCountdown();
        }
    };
    tick();
    countdownTimer = setInterval(tick, 1000);
};

const stopCountdown = () => {
    if (countdownTimer) {
        clearInterval(countdownTimer);
        countdownTimer = null;
    }
};

onLoad((options) => {
    if (!options || !options.orderId) {
        redirect({ url: "/pages/user/order/list", mode: "redirectTo" });
        return;
    }
    entryFrom.value = options.from || "";
    initialPaymentType.value = options.paymentType || "";
    if (options.type === "recharge") {
        isRecharge.value = true;
        loadRechargeOrderPay(Number(options.orderId));
    } else {
        loadOrderPayInfo(Number(options.orderId));
    }
});

/**
 * @description 加载订单支付信息，初始化金额、渠道、倒计时
 * @param id 订单ID
 */
const loadOrderPayInfo = async (id: number) => {
    loading.value = true;
    try {
        const result = await orderPayInfo(id);
        order.value = result.order;
        paymentList.value = result.paymentList || [];
        orderId.value = result.order.orderId;
        presaleInfo.value = result.order.presaleInfo ?? null;
        resolvePaymentType(result.order.orderId);
        willSendPoints.value = result.willSendPoints ?? result.order?.willSendPoints ?? 0;
        memberDiscountAmount.value = result.memberDiscountAmount ?? result.order?.memberDiscountAmount ?? 0;

        // 拼团订单跳拼团详情（与支付页一致）
        if (result?.grouponPromotionRecordId) {
            redirect({
                url: `/pages/product/groupBuying?grouponPromotionRecordId=${result.grouponPromotionRecordId}`,
                mode: "redirectTo"
            });
            return;
        }

        // 订单已支付：直接跳支付结果页，避免重复支付
        if (result.order.payStatus === 2) {
            clearCachedPaymentType();
            redirect({ url: `/pages/order/payStatus?id=${id}`, mode: "redirectTo" });
            return;
        }

        startCountdown();
    } catch (error: any) {
        uni.showToast({ title: error.message, duration: 1500, icon: "none" });
        setTimeout(() => {
            redirect({ url: "/pages/user/order/list", mode: "redirectTo" });
        }, 1500);
    } finally {
        loading.value = false;
    }
};

/**
 * @description 加载充值订单支付信息
 * @param id 充值订单ID
 */
const loadRechargeOrderPay = async (id: number) => {
    loading.value = true;
    try {
        const result = await rechargeOrderPay({ orderId: id });
        order.value = result.order;
        paymentList.value = result.paymentList || [];
        orderId.value = result.order.orderId;
        resolvePaymentType(result.order.orderId);
        startCountdown();
    } catch (error: any) {
        uni.showToast({ title: error.message, duration: 1500, icon: "none" });
        setTimeout(() => {
            redirect({ url: "/pages/user/account/index", mode: "redirectTo" });
        }, 1500);
    } finally {
        loading.value = false;
    }
};

const selectChannel = (channel: string) => {
    if (paymentDisabled.value) return;
    paymentType.value = channel;
    cachePaymentType();
};

/**
 * @description 发起支付（重新支付 / 自动发起共用）
 * 各端各渠道的收银台拉起 / 表单跳转统一由 usePayment 处理
 */
const handlePay = async () => {
    if (paymentDisabled.value) return;
    if (payExpired.value) {
        uni.showToast({ title: t("订单支付已过期"), duration: 1500, icon: "none" });
        return;
    }
    if (!orderId.value || !paymentType.value) {
        uni.showToast({ title: t("请选择支付方式"), duration: 1500, icon: "none" });
        return;
    }
    cachePaymentType();
    paymentDisabled.value = true;
    try {
        const saleModeQuery = presaleInfo.value?.saleMode === 2 ? "&saleMode=1" : "";
        const started = await startPay({
            orderId: orderId.value,
            paymentType: paymentType.value,
            presaleInfo: presaleInfo.value,
            isRecharge: isRecharge.value,
            // H5 微信用当前页跳转，返回落回收银台页
            h5WechatMode: "href",
            onSuccess: () => {
                clearCachedPaymentType();
                if (isRecharge.value) {
                    redirect({ url: "/pages/user/account/index", mode: "redirectTo" });
                } else {
                    redirect({ url: `/pages/order/payStatus?id=${orderId.value}${saleModeQuery}`, mode: "redirectTo" });
                }
            },
            onFail: () => {
                // 取消/失败：留在收银台，可重新支付
                paymentDisabled.value = false;
            },
            onTimeout: () => {
                paymentDisabled.value = false;
            }
        });
        if (!started) paymentDisabled.value = false;
    } catch (error: any) {
        uni.showToast({ title: error.message, duration: 1500, icon: "none" });
        paymentDisabled.value = false;
    }
};

/** 点击返回：弹出放弃支付挽留 */
const onClickBack = () => {
    if (isRecharge.value) {
        confirmAbandon();
        return;
    }
    showAbandon.value = true;
};

// 拦截系统/硬件返回，改为弹出挽留
onBackPress((e: any) => {
    if (e && e.from === "backbutton") {
        if (isRecharge.value) {
            confirmAbandon();
            return true;
        }
        if (!showAbandon.value) {
            showAbandon.value = true;
        }
        return true;
    }
    return false;
});

/** 放弃支付 / 关闭挽留弹窗 */
const confirmAbandon = () => {
    showAbandon.value = false;
    if (skipAbandonRedirect.value) {
        skipAbandonRedirect.value = false;
        return;
    }
    clearCachedPaymentType();
    // 结算页进入：无上一页可回，去待付款列表 / 充值兜底账户页
    if (entryFrom.value === "settle") {
        if (isRecharge.value) {
            redirect({ url: "/pages/user/account/index", mode: "redirectTo" });
        } else {
            redirect({ url: "/pages/user/order/list?type=awaitPay", mode: "redirectTo" });
        }
        return;
    }
    // 订单详情 / 列表 / 充值：有历史栈则返回上一页
    const pages = getCurrentPages();
    if (pages.length > 1) {
        uni.navigateBack();
        return;
    }
    // 无历史栈兜底
    if (isRecharge.value) {
        redirect({ url: "/pages/user/account/index", mode: "redirectTo" });
    } else {
        redirect({ url: "/pages/user/order/list?type=awaitPay", mode: "redirectTo" });
    }
};

/** 立即支付：关闭挽留并重新发起 */
const continuePay = () => {
    skipAbandonRedirect.value = true;
    showAbandon.value = false;
    handlePay();
};

/** 更换方式：关闭挽留，回到收银台自行切换支付渠道 */
const onChangePay = () => {
    skipAbandonRedirect.value = true;
    showAbandon.value = false;
};

onUnmounted(() => {
    stopCountdown();
    stopPolling();
});
</script>

<style lang="scss" scoped>
.cashier {
    padding-bottom: 120rpx;
}

.cashier-head {
    text-align: center;
    padding: 60rpx 0 50rpx;

    .cashier-amount {
        display: flex;
        align-items: flex-end;
        justify-content: center;
        color: var(--general);
    }

    .cashier-countdown {
        margin-top: 20rpx;
        font-size: 26rpx;
        color: #2a2a2a;

        .countdown-time {
            color: var(--general);
            margin: 0 6rpx;
        }
    }

    .cashier-expired {
        color: #f04438;
    }
}

.cashier-panel {
    background: #fff;
    border-radius: 18rpx;
    margin: 0 20rpx;
    padding: 30rpx 32rpx 10rpx;

    .panel-title {
        font-size: 32rpx;
        color: #262626;
    }

    .channel-list {
        margin-top: 10rpx;

        .channel-item {
            height: 120rpx;
            display: flex;
            align-items: center;
            justify-content: space-between;
            border-bottom: 1rpx solid #eee;

            &:last-child {
                border-bottom: 0;
            }
        }

        .channel-info {
            display: flex;
            align-items: center;

            .channel-icon {
                width: 42rpx;
                height: 42rpx;
            }

            .channel-text {
                padding-left: 18rpx;

                .channel-name {
                    font-size: 32rpx;
                    color: #262626;
                }
            }
        }
    }
}

.radio-circle {
    width: 36rpx;
    height: 36rpx;
    border-radius: 50%;
    border: 2rpx solid #ccc;
    box-sizing: border-box;
    position: relative;
    flex-shrink: 0;

    &.checked {
        border-color: var(--general);
        background-color: var(--general);

        &::after {
            content: "";
            position: absolute;
            left: 50%;
            top: 50%;
            width: 16rpx;
            height: 9rpx;
            border-left: 3rpx solid #fff;
            border-bottom: 3rpx solid #fff;
            transform: translate(-50%, -65%) rotate(-45deg);
        }
    }
}

.cashier-footer {
    padding: 20rpx 30rpx;
    padding-bottom: calc(20rpx + constant(safe-area-inset-bottom));
    padding-bottom: calc(20rpx + env(safe-area-inset-bottom));

    .pay-btn {
        width: 100%;
    }

    .pay-btn-inner {
        display: flex;
        align-items: center;
        justify-content: center;
        column-gap: 8rpx;
    }
}
</style>
