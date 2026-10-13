<template>
    <CommonPageHeader></CommonPageHeader>
    <CommonHeader title="支付订单"></CommonHeader>
    <CommonPageContainer v-if="!loading">
        <div class="success_txt">{{ $t("您的订单已成功提交，请付款") }}！</div>
        <div class="price_box">
            <div class="num_box">
                <div class="tit">{{ $t("需支付") }}：</div>
                <FormatPrice class="price" v-model="order.unpaidAmount" :showText="false"></FormatPrice>
            </div>
            <div v-if="!isPremiumOrder || premiumOrderCreated" class="order_num">
                <div class="tit">{{ $t("订单号") }}：</div>
                <div class="num" :class="{ 'hand-pointer': !isPremiumOrder }">
                    <NuxtLink v-if="!isPremiumOrder" :to="'/member/order/info?id=' + order.orderId" target="_blank">{{ order.orderSn }}</NuxtLink>
                    <span v-else>{{ order.orderSn }}</span>
                </div>
            </div>
        </div>
        <div
            class="pay_type_box payment-methods-box"
            v-if="
                order.payTypeId < 3 &&
                (!isPremiumOrder || !premiumPaymentStarted) &&
                (paymentMethods.length > 0 || (isPremiumOrder && ((premiumDraft && premiumBalanceEnabled) || Number(order.balanceAmount || 0) > 0)))
            "
        >
            <div v-if="paymentMethods.length > 0" class="payment-section platform-payment-box">
                <div class="tit_row">
                    <h3>{{ $t("平台支付") }}</h3>
                    <p>{{ $t("支持所有银行卡或信用卡，更迅速、安全") }}</p>
                </div>
                <div class="type_tab">
                    <el-radio-group v-model="payType">
                        <el-radio v-for="item in paymentMethods" :key="item.type" class="tab flex align-center" :value="item.type">
                            <div class="cont"><img :src="item.icon" /></div>
                        </el-radio>
                    </el-radio-group>
                </div>
            </div>
        </div>
        <div class="pay_type_box" v-if="!isPremiumOrder && order.payTypeId >= 3">
            <el-tabs type="border-card" v-model="activeName">
                <el-tab-pane v-if="'thirdPartyPaymentTransfer' in offlinePaymentList" :label="$t('第三方支付转账')" :name="3">
                    <div v-html="offlinePaymentList.thirdPartyPaymentTransfer"></div>
                </el-tab-pane>
                <el-tab-pane v-if="'offlinePayBank' in offlinePaymentList" :label="$t('银行汇款')" :name="1">
                    <div v-html="offlinePaymentList.offlinePayBank"></div>
                </el-tab-pane>
                <el-tab-pane v-if="'offlinePayCompany' in offlinePaymentList" :label="$t('企业汇款')" :name="2">
                    <div v-html="offlinePaymentList.offlinePayCompany"></div>
                </el-tab-pane>
                <el-tab-pane v-if="'otherPaymentRemittance' in offlinePaymentList" :label="$t('其他汇款方式')" :name="4">
                    <div v-html="offlinePaymentList.otherPaymentRemittance"></div>
                </el-tab-pane>
            </el-tabs>
        </div>
        <div class="btn_box" v-if="order.payTypeId < 3 && (!isPremiumOrder || !premiumPaymentStarted)">
            <NuxtLink :to="payLink" target="_blank" @click="handlePayClick">
                <div class="btn" :class="{ 'btn-loading': paying || applePaying }">
                    <span v-if="!paying && !applePaying">{{ $t("去付款") }}</span>
                    <span v-else>{{ $t("支付中") }}...</span>
                    <i v-if="!paying && !applePaying" class="iconfont-pc icon-yuanjiaojiantouyou"></i>
                </div>
            </NuxtLink>
        </div>
    </CommonPageContainer>
    <CommonPageFooter></CommonPageFooter>
</template>
<script lang="ts" setup>
/**
 * @description 订单支付选择页面
 * - 展示待支付订单信息
 * - 提供多种支付方式选择（微信、支付宝、银行卡等）
 * - 选择支付方式后跳转到对应的支付页面
 */
import { reactive, ref, shallowRef, onMounted, onUnmounted, computed, watch, nextTick } from "vue";
import { orderPayInfo, creatPay, createDepositPayment } from "~/api/order/pay";
import { isOverseas } from "@/utils/util";
import iconWechat from "~/assets/images/extra/payment_method_wechat.jpg";
import iconAlipay from "~/assets/images/extra/payment_method_alipay.jpg";
import iconPaypal from "~/assets/images/extra/payment_method_paypal.jpg";
import iconYunshanfu from "~/assets/images/extra/payment_method_yunpay_yunshanfu.jpg";
import iconApplePay from "~/assets/images/extra/payment_method_applepay.svg";
const activeName = ref<number>(1);
const route = useRoute();
const id = ref(route.query.id);
const payType = ref("wechat");
const premiumOrderCreated = ref(false);
const premiumBalanceEnabled = ref(false);
const premiumDraftToken = ref("");
const premiumDraft = ref<any>();
const loading = ref(false);
const paying = ref(false); // 支付中状态
const order = ref<any>({});
const payment = ref<string[]>([]);
const offlinePaymentList = ref<any>({});
const type = ref<string>("");
const { t } = useI18n();
const isPremiumOrder = computed(() => route.query.orderType === "premiumMember" || route.query.premium === "1");
const premiumPaymentStarted = ref(false);
const premiumQrCode = ref("");
const premiumFormContainer = ref<HTMLDivElement | null>(null);

// 支付方式标识对应的图标
const PAYMENT_ICON_MAP: Record<string, string> = {
    wechat: iconWechat,
    alipay: iconAlipay,
    paypal: iconPaypal,
    yabanpayWechat: iconWechat,
    yabanpayAlipay: iconAlipay,
    yunpayWechat: iconWechat,
    yunpayAlipay: iconAlipay,
    yunpayYunshanfu: iconYunshanfu,
    huifuWechat: iconWechat,
    huifuAlipay: iconAlipay,
    lakalaWechat: iconWechat,
    lakalaAlipay: iconAlipay,
    yeepayWechat: iconWechat,
    yeepayAlipay: iconAlipay,
    applepay: iconApplePay
};

// Apple Pay：内联流程封装 + 能力检测（仅苹果 Safari 可见可用）
const { launchApplePay, paying: applePayingSource } = useApplePay();
const applePayAvailable = ref(false);
onMounted(() => {
    applePayAvailable.value = canUseApplePay();
});
// applePaying：启用 Apple Pay 时同步内联支付状态，否则恒为 false
const applePaying = ref(false);
watch(applePayingSource, (v) => (applePaying.value = v), { immediate: true });

const getPaymentIcon = (type: string) => {
    if (PAYMENT_ICON_MAP[type]) return PAYMENT_ICON_MAP[type];
    const normalized = type.toLowerCase();
    if (normalized.includes("alipay")) return iconAlipay;
    if (normalized.includes("wechat")) return iconWechat;
    return iconWechat;
};

// 按接口返回的 paymentList 顺序生成可展示的支付方式列表
const paymentMethods = computed(() =>
    payment.value
        .filter((key) => {
            if (isPremiumOrder.value) {
                return !["offline", "balance", "cash", "transfer"].includes(key.toLowerCase());
            }
            if (!PAYMENT_ICON_MAP[key]) return false;
            // Apple Pay 仅在支持的浏览器（苹果 Safari）中展示
            if (key === "applepay") return applePayAvailable.value;
            return true;
        })
        .map((key) => ({ type: key, icon: getPaymentIcon(key) }))
);

const checkType = (item: any) => {
    payType.value = item.type;
};

/**
 * @description 加载订单支付信息，包括订单详情、可用支付方式和线下支付说明
 * @returns 无返回值，设置order、payment、offlinePaymentList等状态
 */
const loadOrderPayInfo = async () => {
    loading.value = true;
    try {
        const result = await orderPayInfo({
            id: id.value
        });
        order.value = result.order;
        payment.value = result.paymentList;
        offlinePaymentList.value = result.offlinePaymentList;
        // 默认选中接口返回的第一个可用支付方式
        if (paymentMethods.value.length > 0 && !paymentMethods.value.some((item) => item.type === payType.value)) {
            payType.value = paymentMethods.value[0].type;
        }
        // 定金预售且处于“非尾款阶段”时，标记为定金支付
        if ((result.order as any)?.presaleInfo?.saleMode === 2 && (result.order as any)?.presaleInfo?.presaleStatus !== 1) {
            type.value = "saleMode";
        } else {
            type.value = "";
        }
        // 根据已启用的线下支付方式，默认选中第一个
        if ("thirdPartyPaymentTransfer" in offlinePaymentList.value) {
            activeName.value = 3;
        } else if ("offlinePayBank" in offlinePaymentList.value) {
            activeName.value = 1;
        } else if ("offlinePayCompany" in offlinePaymentList.value) {
            activeName.value = 2;
        } else if ("otherPaymentRemittance" in offlinePaymentList.value) {
            activeName.value = 4;
        }
        loading.value = false;
    } catch (error: any) {
        message.error(error.message);
        navigateTo("/member/order/list/");
    } finally {
    }
};
loadOrderPayInfo();

/**
 * @description 根据选择的支付方式生成对应的支付页面链接（重定向类支付除外）
 * @returns 支付页面URL，包含订单ID参数
 */
const payLink = computed(() => {
    if (payment.value.length === 0) {
        return "";
    }


    // 与多商户版本一致：用 type 传递预售定金场景标记（普通订单会带 &type= 为空）
    const orderTypeParam = `&type=${type.value}`;

    switch (payType.value) {
        case "wechat":
            return "/order/wechat/?id=" + id.value + "&type=" + type.value;
        case "alipay":
            return "/order/alipay/?id=" + id.value + "&type=" + type.value;
        case "paypal":
            return "/order/paypal/?id=" + id.value + "&type=" + type.value;
        case "yabanpayWechat":
            return "/order/yabanpay/?type=wechat&id=" + id.value;
        case "yabanpayAlipay":
            return "/order/yabanpay/?type=alipay&id=" + id.value;
        case "yunpayWechat":
            return "/order/yunpay/?type=wechat&id=" + id.value;
        case "yunpayAlipay":
            return "/order/yunpay/?type=alipay&id=" + id.value;
        case "yunpayYunshanfu":
            return "/order/yunpay/?type=yunshanfu&id=" + id.value;
        case "huifuWechat":
            return "/order/huifu/?payCode=huifuWechat&id=" + id.value + orderTypeParam;
        case "huifuAlipay":
            return "/order/huifu/?payCode=huifuAlipay&id=" + id.value + orderTypeParam;
        case "lakalaWechat":
            return "/order/lakala/?payCode=lakalaWechat&id=" + id.value + orderTypeParam;
        case "lakalaAlipay":
            return "/order/lakala/?payCode=lakalaAlipay&id=" + id.value + orderTypeParam;
        case "yeepayWechat":
            return "/order/yeepay/?payCode=yeepayWechat&id=" + id.value + orderTypeParam;
        case "yeepayAlipay":
            return "/order/yeepay/?payCode=yeepayAlipay&id=" + id.value + orderTypeParam;
        default:
            return "";
    }
});



/**
 * @description 拉起 Apple Pay 内联支付（不跳中间页）
 * 支付成功后弹窗提示并跳转订单列表；取消静默、失败提示
 */
const startApplePay = () => {
    launchApplePay({
        id: id.value,
        isDeposit: type.value === "saleMode",
        amount: order.value.unpaidAmount,
        label: order.value.shopName || order.value.orderSn || "Apple Pay",
        onSuccess: () => {
            ElMessageBox.confirm(t("支付完成"), t("提示"), {
                confirmButtonText: t("查看订单"),
                showClose: false,
                showCancelButton: false,
                closeOnClickModal: false,
                type: "success"
            })
                .then(() => {
                    navigateTo("/member/order/list");
                })
                .catch(() => {});
        },
        onFail: (msg?: string) => message.error(msg || t("支付失败"))
    });
};

// 处理支付按钮点击
const handlePayClick = (e: Event) => {
    // applepay：走内联支付流程，不做 NuxtLink 跳转
    if (payType.value === "applepay") {
        e.preventDefault();
        startApplePay();
        return;
    }
    if (!payType.value) {
        e.preventDefault();
        message.error(t("请选择支付方式"));
        return;
    }

};

</script>
<style lang="less" scoped>
.container {
    margin-top: 30px;
    :deep(.el-tabs--border-card > .el-tabs__header .el-tabs__item.is-active) {
        color: #333;
    }
    :deep(.el-tabs--border-card > .el-tabs__header .el-tabs__item:not(.is-disabled):hover) {
        color: #333;
    }
    .success_txt {
        color: #239700;
        font-size: 14px;
        margin-bottom: 15px;
    }

    .price_box {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 10px;
        .num_box {
            display: flex;
            align-items: flex-end;
            .tit {
                color: #333;
                font-size: 18px;
                font-weight: bold;
                line-height: 26px;
            }
            :deep(.price) {
                vertical-align: bottom;
                .util {
                    color: var(--price);
                    font-size: 16px;
                    line-height: 28px;
                }
                .num,
                .decimals {
                    color: var(--price);
                    font-size: 28px;
                }
            }
        }
        .order_num {
            display: flex;
            align-items: center;
            a {
                color: var(--general);
            }
        }
    }
    .pay_type_box {
        // border: 1px solid #ccc;
        background: #fff;
        border-radius: 8px;
        padding: 30px 30px;
        .tit_row {
            display: flex;
            align-items: flex-end;
            h3 {
                color: #333;
                font-size: 18px;
                font-weight: 400;
                margin-right: 10px;
            }
            p {
                color: #999;
            }
        }
        .type_tab {
            display: flex;
            .tab {
                border: 1px solid #d9d9d9;
                cursor: pointer;
                height: 39px;
                padding: 4px 5px 4px 15px;
                background: #fff;
                overflow: hidden;
                width: 146px;
                margin-right: 20px;
                border-radius: 8px;
                margin-top: 25px;
                .cont {
                    height: 35px;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    img {
                        max-height: 35px;
                        max-width: 100%;
                        object-fit: contain;
                    }
                }
                .check {
                    width: 10px;
                    height: 10px;
                    border: 1px solid #333;
                    border-radius: 100px;
                    margin-right: 5px;
                    text-align: center;
                    line-height: 10px;
                    span {
                        display: inline-block;
                        width: 7px;
                        height: 7px;
                        border-radius: 100px;
                    }
                }
            }
            .active {
                border: 1px solid #ed3714;
                .check {
                    border: 1px solid #0075ff;
                    span {
                        background-color: #0075ff;
                    }
                }
            }

            .balance-tab {
                width: 180px;
                padding: 4px 12px;

                .balance-payment {
                    display: flex;
                    flex-direction: column;
                    align-items: flex-start;
                    justify-content: center;
                    gap: 2px;
                    line-height: 1.2;

                    &__title {
                        color: #333;
                        font-size: 14px;
                    }

                    &__amount {
                        display: inline-flex;
                        align-items: center;
                        gap: 4px;
                        color: #999;
                        font-size: 12px;

                        :deep(.util),
                        :deep(.num),
                        :deep(.decimals) {
                            color: #999;
                            font-size: 12px;
                        }
                    }
                }
            }
        }
    }

    .premium-balance-box {
        .tit_row h3 {
            margin: 0 10px 0 0;
            line-height: 26px;
        }

        .balance-tab {
            display: inline-flex;
            align-items: center;
            min-height: 39px;
            min-width: 270px;
            margin-top: 25px;
            padding: 4px 14px;
            box-sizing: border-box;
            white-space: nowrap;
            border: 1px solid #d9d9d9;
            border-radius: 8px;
            background: #fff;

            .balance-payment__title {
                color: #333;
                font-size: 14px;
            }

            .balance-payment__amount {
                display: inline-flex;
                align-items: center;
                gap: 4px;
                margin-left: 12px;
                color: #999;
                font-size: 12px;
            }
        }
    }

    .payment-methods-box {
        .payment-section + .payment-section {
            margin-top: 20px;
            padding-top: 20px;
            border-top: 1px solid #f0f0f0;
        }

        .platform-payment-box .tit_row {
            align-items: flex-end;
        }
    }

    .premium-payment-result {
        margin-top: 20px;
        padding: 24px;
        border-radius: 8px;
        background: #fff;
        text-align: center;

        .premium-payment-qrcode {
            h3 {
                margin: 0 0 12px;
                color: #333;
                font-size: 18px;
                font-weight: 400;
            }

            img {
                display: block;
                width: 300px;
                height: 300px;
                margin: 0 auto;
            }

            p {
                margin: 12px 0 0;
                color: #8a919f;
            }
        }
    }
    .btn_box {
        display: flex;
        flex-direction: column;
        align-items: flex-end;
        margin-top: 20px;
        .btn {
            display: flex;
            align-items: center;
            background-color: var(--general);
            border: 1px solid var(--general);
            color: var(--main-text);
            cursor: pointer;
            border-radius: 2px;
            height: 16px;
            line-height: 16px;
            text-align: center;
            margin-right: 20px;
            width: 80px;
            padding: 10px 12px;
            font-size: 16px;
            span {
                display: inline-block;
                margin-left: 10px;
            }
            i {
                font-size: 12px;
                margin-left: 5px;
            }
            &.btn-loading {
                opacity: 0.7;
                cursor: not-allowed;
            }
        }
        .tips {
            display: flex;
            margin-right: 16px;
            margin-top: 10px;
            cursor: pointer;
            p {
                margin-left: 5px;
                span {
                    color: #b2002e;
                    margin-left: 5px;
                }
            }
        }
    }
}
.cont {
    width: 100px;
    display: flex;
    align-items: center;
    justify-content: center;
    & > img {
        max-width: 100%;
        max-height: 100%;
        object-fit: contain;
    }
}
</style>
