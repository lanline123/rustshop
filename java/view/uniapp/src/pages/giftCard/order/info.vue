<template>
    <tig-layout>
        <giftCardNav v-model="scrollTop" :event="handleScrollTo" :title="$t('订单详情')" />
        <view class="bg" />
        <view class="order-info">
            <template v-if="Object.keys(orderInfo).length">
                <view class="order-info-content">
                    <view class="order-info-content-status">
                        <view class="order-info-content-status-title">{{ orderInfo?.orderStatusName }}</view>
                        <view class="order-info-content-status-desc">{{
                            $t("为了您的财产安全，请妥善保管卡密信息，切勿将卡号、卡密、二维码截图等任何信息发送他人，谨防上当受骗！")
                        }}</view>
                    </view>
                    <view class="product-card card-space">
                        <view class="product-card-shop-title" @click.stop="handleToShop(orderInfo.shop?.shopId)">
                            <text class="iconfont-h5 icon-dianpu" />
                            <view>{{ orderInfo.shopId == 0 ? $t("自营") : orderInfo.shop?.shopTitle }}</view>
                        </view>
                        <template v-for="(item, itemIndex) in orderInfo.items" :key="item.productId">
                            <view class="item-box">
                                <view class="product-card-item">
                                    <view class="product-card-content">
                                        <view class="product-card-item-left" @click="handleLink(item.productId)">
                                            <view class="left-img">
                                                <tig-image :src="pickPic(item)" />
                                            </view>
                                        </view>
                                        <view class="product-card-item-right" @click="handleLink(item.productId)">
                                            <view class="product-info">
                                                <view class="product-top">
                                                    <view class="right-title line2">
                                                        <text>{{ item.productName }}</text>
                                                    </view>
                                                    <view class="right-price">
                                                        <format-price
                                                            :show-text="false"
                                                            class="right-price-pricenum"
                                                            :decimals-style="{
                                                                fontSize: '24rpx'
                                                            }"
                                                            :currency-style="{
                                                                fontSize: '20rpx'
                                                            }"
                                                            :price-data="item.price"
                                                        />
                                                    </view>
                                                </view>
                                                <view class="product-bottom">
                                                    <view>
                                                        <view class="sku-card" :class="{ nodata: !hasSkuText(item.skuData) }">
                                                            <view v-if="hasSkuText(item.skuData)" class="sku-item line1">{{ getSkuText(item.skuData) }}</view>
                                                        </view>
                                                        <template v-if="item.extraSkuData && item.extraSkuData.length">
                                                            <view class="extraskudata-box">
                                                                <view class="extraskudata">
                                                                    <template v-for="attr in item.extraSkuData" :key="attr">
                                                                        <view class="extraskudata-item">
                                                                            <view class="title">{{ attr.attrName }}</view>
                                                                            <view class="line-box">
                                                                                <view class="line" />
                                                                            </view>
                                                                            <view class="content">
                                                                                <text class="line1 content-text">{{ attr.attrValue }}</text>
                                                                                <format-price :show-text="false" class="price" :price-data="attr.attrPrice" />
                                                                                <view class="num">x{{ item.quantity }}</view>
                                                                            </view>
                                                                        </view>
                                                                    </template>
                                                                </view>
                                                            </view>
                                                        </template>
                                                    </view>
                                                    <view class="right-price-quantity">x {{ item.quantity }}</view>
                                                </view>
                                            </view>
                                        </view>
                                    </view>
                                    <template v-if="orderInfo.payStatus === 2 && item.isGift === 0">
                                        <view class="action">
                                            <view class="item-btn">
                                                <!-- <tig-button :plain="true" :custom-style="{ height: '55rpx' }" @click="handleService()">
                                                    {{ $t("联系客服") }}
                                                </tig-button> -->
                                                <template v-if="showService">
                                                    <view class="pay-card-item im-btn">
                                                        <service
                                                            ref="serviceRef"
                                                            :phone="orderInfo.shop && orderInfo.shop.kefuPhone ? orderInfo.shop.kefuPhone : ''"
                                                            :order-id="orderInfo.orderId"
                                                            :shop-id="orderInfo.shopId"
                                                        >
                                                            <tig-button
                                                                :plain="true"
                                                                :custom-style="{ height: '55rpx' }"
                                                                @click.stop="handleService(itemIndex)"
                                                            >
                                                                {{ $t("联系客服") }}
                                                            </tig-button>
                                                        </service>
                                                    </view>
                                                </template>
                                            </view>
                                        </view>
                                    </template>
                                </view>
                            </view>
                        </template>
                    </view>
                    <view v-if="showGiftCards && orderInfo.giftCardOrder?.num" class="gift-card-box card-space">
                        <view class="gift-card-header">
                            <text class="title">{{ $t("卡片信息") }}</text>
                            <text class="count">（{{ $t("共") + orderInfo.giftCardOrder?.num + $t("张") }}）</text>
                        </view>
                        <view class="gift-card-list">
                            <view v-for="item in orderInfo.giftCardOrder?.giftCardOrderInfo" :key="item.giftCardsId" class="gift-card-item">
                                <giftCard :card-info="item" card-size="large" />
                            </view>
                        </view>
                    </view>
                    <view class="order-card card-space">
                        <view class="order-card-item flex justify-between">
                            <view class="item-left">
                                <text class="order-item-label">{{ $t("订单编号") }}：</text>
                                <text>{{ orderInfo.orderSn }}</text>
                            </view>
                            <view class="item-right">
                                <tig-button
                                    class="btn-copy"
                                    color="#8C8C8C"
                                    :custom-style="{ height: '28rpx', width: '64rpx' }"
                                    :plain="true"
                                    @click="handleCopy(orderInfo.orderSn)"
                                >
                                    {{ $t("复制") }}
                                </tig-button>
                            </view>
                        </view>
                        <view class="order-card-item">
                            <text class="order-item-label">{{ $t("创建时间") }}：</text>
                            <text>{{ orderInfo.addTime }}</text>
                        </view>
                        <view class="order-card-item">
                            <text class="order-item-label">{{ $t("付款时间") }}：</text>
                            <text>{{ orderInfo.addTime }}</text>
                        </view>
                        <view class="pay-card-item">
                            <text class="order-item-label">{{ $t("支付方式") }}：</text>
                            <text v-if="isGiftCardExchangeOrder">{{ $t("礼品卡兑换") }}</text>
                            <text v-else>{{ orderInfo.payTypeId === 1 ? $t("在线支付") : orderInfo.payTypeId === 2 ? $t("货到付款") : $t("线下支付") }}</text>
                        </view>
                        <view class="order-card-item flex justify-between">
                            <view class="item-left">
                                <text class="order-item-label">{{ $t("交易单号") }}：</text>
                                <text>{{ orderInfo.orderSn }}</text>
                            </view>
                            <view class="item-right">
                                <tig-button
                                    class="btn-copy"
                                    color="#8C8C8C"
                                    :custom-style="{ height: '28rpx', width: '64rpx' }"
                                    :plain="true"
                                    @click="handleCopy(orderInfo.orderSn)"
                                >
                                    {{ $t("复制") }}
                                </tig-button>
                            </view>
                        </view>
                        <view v-if="orderInfo.buyerNote" class="order-card-item">
                            <text class="order-item-label">{{ $t("订单备注") }}：</text>
                            <text>{{ orderInfo.buyerNote }}</text>
                        </view>
                    </view>
                    <template v-if="orderInfo.orderId && (orderInfo.orderStatus === 2 || orderInfo.orderStatus === 5) && orderInfo.shippingMethod == 1">
                        <view class="expressage-card card-space">
                            <template v-if="shippingInfo.length === 0">
                                <view class="shippingInfo-title"> {{ $t("物流信息") }} </view>
                            </template>

                            <view class="expressage-box">
                                <view class="expressage-info">
                                    <view class="">{{ orderInfo.logisticsName }}</view>
                                    <view class="">{{ orderInfo.trackingNo }}</view>
                                </view>
                                <view v-if="orderInfo?.trackingNo" class="copy-btn" @click="handleCopy(orderInfo.trackingNo)">{{ $t("复制") }}</view>
                            </view>
                            <template v-if="shippingInfo.length > 0">
                                <view class="shippingInfo-box">
                                    <view class="shippingInfo-box-content" :class="{ dim: !showMore, 'more-height': showMore }">
                                        <up-steps :current="0" dot active-color="var(--general)" direction="column">
                                            <up-steps-item
                                                v-for="(item, index) in shippingInfo"
                                                :key="index"
                                                :title="item.acceptStation"
                                                :desc="item.acceptTime"
                                            />
                                        </up-steps>
                                    </view>
                                    <view class="shippingInfo-btn" @click="showMore = !showMore"
                                        >{{ showMore ? $t("收起") : $t("展开")
                                        }}<uni-icons class="btn-icon" :class="{ rotate: showMore }" type="down" size="18" color="#999"
                                    /></view>
                                </view>
                            </template>
                            <template v-else>
                                <view class="emptyText">
                                    {{ emptyText }}
                                </view>
                            </template>
                        </view>
                    </template>
                    <view class="money-card card-space">
                        <view class="money-card-item">
                            <text class="order-item-label">{{ $t("订单金额") }}：</text>
                            <format-price :show-text="false" :is-bottom="false" :price-data="orderInfo.productAmount" />
                        </view>
                        <!-- <view v-if="orderInfo.serviceFee && orderInfo.serviceFee > 0" class="money-card-item">
                            <text class="order-item-label">{{ $t("附加费用") }}：</text>
                            <format-price :show-text="false" :is-bottom="false" :price-data="orderInfo.serviceFee" />
                        </view> -->
                        <view class="money-card-item">
                            <text class="order-item-label">{{ $t("优惠券") }}：</text>
                            <format-price :show-text="false" :is-bottom="false" :price-data="orderInfo.couponAmount" />
                        </view>
                        <!-- <view class="money-card-item">
                            <text class="order-item-label">{{ $t("快递") }}：</text>
                            <text v-if="Number(orderInfo.shippingFee || 0) === 0">{{ $t("包邮") }}</text>
                            <format-price v-else :show-text="false" :is-bottom="false" :price-data="orderInfo.shippingFee" />
                        </view> -->
                        <!-- <view v-if="orderInfo.discountAmount && orderInfo.discountAmount > 0" class="money-card-item">
                            <text class="order-item-label">{{ $t("其他优惠") }}：</text>
                            <format-price :show-text="false" :is-bottom="false" :price-data="orderInfo.discountAmount" />
                        </view> -->
                    </view>
                </view>
                <template v-if="orderInfo.availableActions.confirmReceipt || showExchange">
                    <tig-fixed-placeholder height="100rpx" background-color="#fff">
                        <view class="order-info-btn">
                            <view class="order-info-btn-box">
                                <tig-button
                                    v-if="orderInfo.availableActions.confirmReceipt"
                                    color="#333"
                                    class="btn"
                                    :plain="true"
                                    @click="handleConfirmReceipt(orderInfo.orderId)"
                                >
                                    {{ $t("确认已收货") }}
                                </tig-button>
                                <tig-button v-if="unUseGiftCards?.length" class="btn" :plain="true" @click="handleGiftCardSend">
                                    <text decode>&emsp;{{ $t("赠送") }}&emsp;</text>
                                </tig-button>
                                <tig-button v-if="showExchange" class="btn" @click="handleGiftCardExchange">
                                    {{ $t("立即兑换") }}
                                </tig-button>
                            </view>
                        </view>
                    </tig-fixed-placeholder>
                </template>
            </template>
            <giftCardShare v-model="showShare" :link-url="transferLinkUrl" />
            <giftCardSelect v-model="showCardSelect" :cards="unUseGiftCards" @ok-call-back="handleGiftCardSelectOk" />
        </view>
    </tig-layout>
</template>

<script setup lang="ts">
import { pickPic } from "@/utils/format";
import { onLoad } from "@dcloudio/uni-app";
import { confirmReceipt, getOrder, getShippingInfo } from "@/api/user/order";
import type { OrderInfoResponseItem, ShippingInfoTrace } from "@/types/user/order";
import { ref, computed, getCurrentInstance } from "vue";
import { useI18n } from "vue-i18n";
import { copy, getSkuText, hasSkuText, isO2o, toStoreOrShopHome } from "@/utils";
import giftCardNav from "../src/giftCardNav.vue";
import { useScrollTop } from "@/hooks";
import service from "@/components/service/index.vue";
import giftCard from "../src/giftCard.vue";
import giftCardShare from "../src/giftCardShare.vue";
import { giftCardTransfer } from "@/api/giftCard/giftCard";
import { useUserStore } from "@/store/user";
import giftCardSelect from "../src/giftCardSelect.vue";
import type { GiftCardInfo } from "@/types/giftCard/giftCard";

const { scrollTop } = useScrollTop();
const userStore = useUserStore();
const instance = getCurrentInstance()?.proxy;

const handleScrollTo = (module: string) => {
    const query = uni.createSelectorQuery().in(instance);
    query.select(module).boundingClientRect();
    query.selectViewport().scrollOffset(() => {});
    query.exec((res) => {
        uni.pageScrollTo({
            scrollTop: res[0].top + res[1].scrollTop - 50,
            duration: 0
        });
    });
};

const { t } = useI18n();

const orderInfo = ref<OrderInfoResponseItem>({} as OrderInfoResponseItem);
const showMore = ref(false);
const showExchange = computed(() => {
    return !(orderInfo.value.orderStatus == 0 || orderInfo.value.orderStatus == 3);
});

const id = ref<null | number>();
onLoad((option: any) => {
    if (option && option.id) {
        id.value = option.id;
        try {
            uni.showLoading({
                title: t("加载中")
            });
            Promise.all([__getOrder(), __getShippingInfo()]);
        } catch (error) {
            console.error(error);
        } finally {
            // uni.hideLoading();
        }
    }
});

const __getOrder = async () => {
    try {
        const result = await getOrder({ id: id.value });
        orderInfo.value = result;
    } catch (error) {
        console.error(error);
    }
};

const shippingInfo = ref<ShippingInfoTrace[]>([]);
const emptyText = ref<string>("");
const __getShippingInfo = async () => {
    try {
        const result = await getShippingInfo({ id: id.value });
        if (result) {
            if (Array.isArray(result)) {
                shippingInfo.value = result;
            } else {
                shippingInfo.value = result.traces ?? ([] as ShippingInfoTrace[]);
            }
        }
    } catch (error) {
        console.error(error);
    } finally {
        uni.hideLoading();
    }
};

const handleConfirmReceipt = (id: number) => {
    uni.showModal({
        title: t("提示"),
        content: t("确认收货吗？"),
        confirmText: t("确定"),
        cancelText: t("取消"),
        success: async (res) => {
            if (res.confirm) {
                try {
                    await confirmReceipt({ id });
                    __getOrder();
                } catch (error: any) {
                    uni.showToast({
                        title: error.message,
                        icon: "none"
                    });
                }
            }
        }
    });
};

const handleToShop = (shopId?: string | number) => {
    if (shopId === undefined || shopId === null || shopId === "") return;
    if (String(shopId) === "0") return;
    // 触发关店提示的店铺状态码：4 停业（枚举：1开业 4停业 10关店）
    if ((orderInfo.value?.shop as { status?: number } | undefined)?.status === 4) {
        uni.showToast({
            title: isO2o() ? t("该门店已停业") : t("该店铺已停业"),
            icon: "none"
        });
        return;
    }
    toStoreOrShopHome(String(shopId), { navType: "navigateTo" });
};

const handleLink = (id: number) => {
    const shopId = (orderInfo.value as any)?.shopId;
    uni.navigateTo({
        url: `/pages/product/index?id=${id}${shopId != null ? `&shopId=${shopId}` : ""}`
    });
};

const handleCopy = (data: any) => {
    copy(data, () => {
        uni.showToast({
            title: t("复制成功"),
            icon: "none"
        });
    });
};
const showService = computed(() => {
    if (orderInfo.value.shop && Object.keys(orderInfo.value.shop).length > 0) {
        if (orderInfo.value.shop.kefuInlet && orderInfo.value.shop.kefuInlet.length > 0 && orderInfo.value.shop.kefuInlet.includes(2)) {
            return true;
        }
        return false;
    } else {
        return userStore.serviceConfig.show;
    }
});
const serviceRef = ref<InstanceType<typeof service>[]>([]);
const handleService = (index: number) => {
    const target = serviceRef.value[index];
    if (target && typeof target.handleClick === "function") {
        target.handleClick();
    }
};

const showShare = ref(false);
const showCardSelect = ref(false);
const cardSelectType = ref<"exchange" | "send">();
const selectedGiftCard = ref<GiftCardInfo>();
const transferLinkUrl = ref<string>("");
const unUseGiftCards = computed(() => {
    return orderInfo.value.giftCardOrder?.giftCardOrderInfo.filter((item: GiftCardInfo) => item.status == 0);
});
// 电子礼品卡才展示卡片信息
const showGiftCards = computed(() => {
    return orderInfo.value.items[0].productType === 8;
});

// 礼品卡兑换按钮点击事件
const handleGiftCardExchange = () => {
    // showCardSelect.value = true;
    // cardSelectType.value = "exchange";
    giftCardExchagne();
};
// 礼品卡赠送按钮点击事件
const handleGiftCardSend = async () => {
    showCardSelect.value = true;
    cardSelectType.value = "send";
};
const giftCardExchagne = () => {
    uni.navigateTo({
        url: "/pages/giftCard/exchange"
    });
};
const giftCardSend = async (giftCardSn: string) => {
    try {
        const res = await giftCardTransfer({ giftCardSn, isFrom: "1" });
        // 从接口响应中获取转赠链接
        // H5_DOMAIN 末尾带 `/` 时，后端拼接会生成 `/mobile//pages/...`，复制前兼容清理。
        transferLinkUrl.value = (res || "").replace(/\/mobile\/{2,}/g, "/mobile/");
        if (transferLinkUrl.value) {
            showShare.value = true;
        }
    } catch (error: any) {
        console.error(error);
        uni.showToast({
            title: error.message,
            icon: "none"
        });
    }
};
const handleGiftCardSelectOk = async (giftCard: GiftCardInfo) => {
    selectedGiftCard.value = giftCard;
    // 兑换
    if (cardSelectType.value == "exchange") {
        // giftCardExchagne(giftCard.cardNumber);
    } else if (cardSelectType.value == "send") {
        // 赠送
        giftCardSend(giftCard.giftCardSn);
    }
};
</script>

<style lang="scss" scoped>
.bg {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 423rpx;
    background: linear-gradient(90deg, #f4f4f4, #fff7f8);
}
.header-back {
    height: 100%;
    border-radius: 50%;
    background: rgba(0, 0, 0, 0.3);
    display: flex;
    align-items: center;
    justify-content: center;
    box-sizing: content-box;
    margin-right: 20rpx;

    .icon-a-gengduo20 {
        font-size: 28rpx;
        color: #fff;
        transform: rotate(180deg);
    }
}

.extraskudata-box {
    display: flex;
    .extraskudata {
        background-color: #f7f8fa;
        padding: 10rpx 15rpx;
        border-radius: 10rpx;
        margin-top: 5rpx;
        color: #969799;
        display: flex;
        flex-direction: column;
        .extraskudata-item {
            overflow: hidden;
            font-size: 22rpx;
            display: flex;
            line-height: 40rpx;
            .content {
                flex: 1;
                display: flex;
                align-items: center;
                overflow: hidden;
                .price {
                    color: #555;
                    padding-left: 10rpx;
                }
                .content-text {
                    flex: 1;
                }
                .num {
                    padding-left: 10rpx;
                }
            }

            .line-box {
                width: 25rpx;
                position: relative;
                .line {
                    width: 1rpx;
                    height: 15rpx;
                    background-color: #c0c0c0;
                    position: absolute;
                    top: 50%;
                    left: 50%;
                    transform: translate(-50%, -50%);
                }
            }
        }
    }
}

.item-box {
    // padding-bottom: 20rpx;

    &:last-child {
        padding-bottom: 0;
    }
    .shop-info {
        display: flex;
        align-items: center;
        font-size: 26rpx;
        font-weight: bold;
        margin-bottom: 20rpx;
    }
}
.virtualsample-box {
    border-top: 1px solid #f2f2f2;
    margin-top: 20rpx;
    padding-top: 20rpx;

    .virtualsample-item {
        .virtualsample-item-con {
            display: flex;
            font-size: 26rpx;
            line-height: 40rpx;
            .label {
                color: #999;
            }
            .value {
                color: #333;
            }
        }
    }
    .virtualsample-btn {
        display: flex;
        justify-content: flex-end;
        margin-top: 20rpx;
    }
}

.card-space {
    padding: 20rpx;
    border-radius: 20rpx;
    background-color: #fff;
    margin-top: 20rpx;
}
.address-text {
    width: calc(100% - 160rpx);
    text-align: end;
}
.order-item-label {
    color: #999;
    // max-width: 160rpx;
}

.order-info-content {
    padding: 10rpx 20rpx;
    .order-info-content-status {
        margin-top: 10rpx;
        margin-bottom: 46rpx;
        padding: 20rpx;
        .order-info-content-status-title {
            color: #ee0a24;
            font-size: 36rpx;
            font-weight: 800;
        }
        .order-info-content-status-desc {
            margin-top: 24rpx;
            color: #6a6a6a;
            font-size: 28rpx;
            line-height: 42rpx;
            font-weight: 500;
        }
    }
    .order-card {
        .order-card-item {
            align-items: center;
            display: flex;
            font-size: 24rpx;
            padding: 12rpx 0;

            .btn-copy {
                font-size: 16rpx;
            }
        }
    }

    .pay-card {
        font-size: 24rpx;
        .pay-card-item {
            padding: 20rpx 0;
            display: flex;
            justify-content: space-between;

            &.im-btn {
                justify-content: flex-end;
            }

            &:last-child {
                border-bottom: none;
                padding-bottom: 0;
            }
        }
    }

    .product-card {
        .product-card-shop-title {
            font-weight: bold;
            font-size: 32rpx;
            margin-bottom: 30rpx;
            display: flex;
            align-items: center;
            gap: 16rpx;
            .icon-dianpu {
                font-size: 30rpx;
            }
        }
        .product-card-item {
            // display: flex;
            position: relative;
            margin-bottom: 20rpx;
            &:last-child {
                margin-bottom: 0;
            }
            .product-card-content {
                display: flex;
            }

            .product-card-item-left {
                .left-img {
                    width: 185rpx;
                    height: 185rpx;
                    border-radius: 4rpx;
                    overflow: hidden;
                    margin-right: 20rpx;
                    // box-shadow: 0px 2px 8px 0px rgba(0, 0, 0, 0.04);
                    position: relative;
                    cursor: pointer;
                }
            }

            .product-card-item-right {
                font-size: 24rpx;
                flex: 1;
                display: flex;
                .product-info {
                    flex: 1;
                    display: flex;
                    flex-direction: column;
                    justify-content: space-between;
                }
                .product-top {
                    display: flex;
                    justify-content: space-between;
                    align-items: baseline;
                }
                .product-bottom {
                    display: flex;
                    align-items: flex-end;
                    justify-content: space-between;
                }

                .right-title {
                    cursor: pointer;
                    font-weight: bold;
                    font-size: 32rpx;
                    margin-bottom: 10rpx;
                    padding-right: 50rpx;
                }

                .right-price {
                    flex-shrink: 0;
                    display: flex;
                    font-size: 32rpx;
                    // align-items: baseline;
                    // padding-top: 10rpx;
                    .right-price-pricenum {
                        color: #8c8c8c;
                    }
                }
                .sku-card {
                    margin-bottom: 0;
                }
                .right-price-quantity {
                    color: #8c8c8c;
                    font-size: 28rpx;
                }
            }
            .action {
                display: flex;
                padding: 34rpx 0 14rpx 0;
                justify-content: flex-end;
                .item-btn {
                    // position: absolute;
                    // right: 0;
                    // bottom: 0;
                    // display: flex;
                    justify-content: center;
                    align-items: center;
                    column-gap: 10rpx;
                }
            }
        }
    }

    .money-card {
        font-size: 24rpx;
        .money-card-item {
            display: flex;
            justify-content: space-between;
            padding: 20rpx 0;
            &:last-child {
                padding-bottom: 0;
                border-bottom: none;
            }

            .order-item-price {
                font-weight: bold;
                color: var(--general);
                // padding-right: 10rpx;
                font-size: 28rpx;
                :deep(.util) {
                    font-size: 22rpx;
                    font-weight: normal;
                }
            }
        }
    }

    .contact-card {
        .contact-card-header {
            display: flex;
            flex-direction: row;
            align-items: center;
            margin-bottom: 20rpx;
            .contact-card-header-info {
                flex: 1;
                width: 0;
                display: flex;
                flex-direction: column;
                justify-content: space-between;
                align-items: flex-start;
                gap: 20rpx;
                .contact-card-header-info-content {
                    display: flex;
                    flex-direction: row;
                    align-items: center;
                    color: #1a1a1a;
                    .icon-zitidian {
                        font-size: 30rpx;
                        color: #1a1a1a;
                    }
                    .contact-card-header-info-content-name {
                        margin-left: 10rpx;
                    }
                }
                .time {
                    width: 100%;
                    color: #595959;
                    font-size: 24rpx;
                    white-space: nowrap;
                    overflow: hidden;
                    text-overflow: ellipsis;
                }
                .address {
                    font-size: 24rpx;
                }
            }
            .contact-card-header-view {
                width: 58rpx;
                height: 58rpx;
                border: 1px solid #f0f0f0;
                border-radius: 29rpx;
                display: flex;
                justify-content: center;
                align-items: center;
                margin-right: 22rpx;
                color: #9a9a9a;

                &:last-child {
                    margin-right: 0;
                }
            }
        }

        .contact-card-footer {
            display: flex;
            flex-direction: row;
            align-items: center;
            padding-top: 20rpx;
            padding-bottom: 20rpx;
            &:nth-child(2) {
                border-top: 1px solid #f4f4f4;
            }
            .icon-lianxiren {
                font-size: 30rpx;
            }
            .contact-card-footer-phone {
                margin-left: 10rpx;
                color: #1a1a1a;
                font-size: 28rpx;
                font-weight: bold;
            }
            .icon-a-pending-payment {
                font-size: 30rpx;
            }
        }
        .contact-card-footer-code {
            display: flex;
            flex-direction: row;
            justify-content: space-between;
            align-items: center;
            padding: 30rpx 20rpx;
            background-color: #f7f7f7;
            border-radius: 8rpx;

            .contact-card-footer-code-left {
                flex: 1;
                display: flex;
                flex-direction: row;
                align-items: center;
                .contact-card-footer-code-left-qr {
                    width: 58rpx;
                    height: 58rpx;
                    background: linear-gradient(45deg, #ee0a24, #ff7b70);
                    border-radius: 50%;
                    margin-right: 18rpx;
                    display: flex;
                    justify-content: center;
                    align-items: center;
                    color: #fff;
                }
                .contact-card-footer-code-left-name {
                    color: #1a1a1a;
                    font-size: 28rpx;
                    font-weight: bold;
                    margin-right: 18rpx;
                }
                .contact-card-footer-code-left-number {
                    color: #ee0a24;
                    font-weight: bold;
                    font-size: 38rpx;
                }
            }
        }
    }
}
.order-info {
    position: relative;
    z-index: 1;
}

.order-info-btn {
    height: 100%;
    display: flex;
    justify-content: flex-end;
    padding: 20rpx;
    .order-info-btn-box {
        display: flex;
        align-items: center;
        .btn {
            margin-right: 10rpx;
            height: 100%;
            flex-basis: content;
            &:last-child {
                margin-right: 0;
            }
        }
    }
}

.shippingInfo-title {
    font-size: 28rpx;
    padding-bottom: 20rpx;
}

.expressage-box {
    display: flex;
    font-size: 26rpx;
    justify-content: space-between;

    .expressage-info {
        display: flex;
        column-gap: 15rpx;
    }

    .copy-btn {
        color: #b8b8b8;
        transition: color 0.2s;

        &:active {
            color: #333;
        }
    }
}

.shippingInfo-box {
    padding: 20rpx 0;
    .shippingInfo-box-content {
        height: 400rpx;
        overflow: hidden;
        position: relative;

        &.dim::after {
            width: 100%;
            height: 200rpx;
            content: "";
            position: absolute;
            left: 0;
            bottom: 0rpx;
            background-image: linear-gradient(-180deg, rgba(255, 255, 255, 0) 0%, rgb(255, 255, 255) 80%);
        }

        &.more-height {
            height: auto;
        }
    }

    .shippingInfo-btn {
        display: flex;
        flex-direction: row;
        justify-content: center;

        .btn-icon {
            &.rotate {
                transition: all 0.3s;
                transform: rotate(180deg);
            }
        }
    }
}

.emptyText {
    padding-top: 15rpx;
    font-size: 24rpx;
}

.icon-size {
    font-size: 40rpx;
    padding-right: 10rpx;
}
.gift-card-box {
    padding: 0 20rpx;
    .gift-card-header {
        display: flex;
        align-items: flex-end;
        gap: 0rpx;
        padding: 20rpx 0;
        border-bottom: 1px solid #f4f4f4;
        .title {
            font-size: 32rpx;
            font-weight: bold;
        }
        .count {
            font-size: 24rpx;
            color: #6a6a6a;
        }
    }

    .gift-card-list {
        padding: 32rpx 0;
        display: flex;
        flex-direction: column;
        gap: 30rpx;

        .gift-card-item {
            width: 668rpx;
            height: 389rpx;
        }
    }
}
</style>
