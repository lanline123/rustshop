<template>
    <tig-layout>
        <tig-navbar
            :title="isMeituanDelivery ? $t('我的订单') : orderInfo.orderStatusName || $t('我的订单')"
            :background="navbarBackground"
            :border-bottom="false"
        >
            <template v-if="!isMeituanDelivery && showService" #right>
                <view class="icon-box">
                    <service
                        :phone="orderInfo.shop && orderInfo.shop.kefuPhone ? orderInfo.shop.kefuPhone : ''"
                        :order-id="orderInfo.orderId"
                        :shop-id="orderInfo.shopId"
                    >
                        <text class="iconfont-h5 icon-a-21" />
                    </service>
                </view>
            </template>
        </tig-navbar>
        <view class="order-info" :class="{ 'order-info--meituan': isMeituanDelivery }">
            <template v-if="Object.keys(orderInfo).length">
                <view v-if="!isMeituanDelivery" class="order-info-steps">
                    <template v-if="showPresaleCard">
                        <presaleCard :presale-info="productPresale" :total="orderInfo" />
                    </template>
                    <template v-else>
                        <view class="order-info-header">
                            <view class="status-title">{{ orderInfo.orderStatusName }}</view>
                            <view v-if="showPayCountdown || statusSubText" class="status-sub">
                                <template v-if="showPayCountdown">
                                    {{ $t("还剩") }}
                                    <tig-countdown class="status-countdown" :end-time="payEndTime" :font-size="13" color="#cc0c1c" splitor-color="#cc0c1c" />
                                    {{ $t("订单自动取消") }}
                                </template>
                                <template v-else>{{ statusSubText }}</template>
                            </view>
                        </view>
                    </template>

                    <template v-if="showAddressBox && !isMeituanDelivery">
                        <view class="address-box" :style="{ marginTop: showPresaleCard ? '0' : '40rpx' }">
                            <view class="icon">
                                <text class="iconfont-h5 icon-dizhi" />
                            </view>
                            <view class="info">
                                <view class="title">
                                    <view class="name">{{ orderInfo.consignee }}</view>
                                    <view class="mobile">{{ orderInfo.mobile }}</view>
                                </view>
                                <view class="address-txt">{{ orderInfo.userAddress }}</view>
                            </view>
                        </view>
                    </template>
                <view v-if="orderInfo.cancelRemark?.trim()" class="cancel-reason card-space">
                    {{ $t("取消原因") }}：{{ orderInfo.cancelRemark }}
                </view>
                </view>

                <view class="order-info-content">

                    <view class="product-card card-space">
                        <view class="product-card-title" @click.stop="productCardShopId != null && handleToShop(productCardShopId)">
                            <view class="shop-info">
                                <text class="text">{{ productCardShopTitle }}</text>
                            </view>
                            <view class="product-card-title-right">{{ $t("订单商品") }}({{ itemsTotalQuantity }})</view>
                        </view>
                        <template v-for="(items, index) in itemsGroup" :key="index">
                            <view class="product-card-group">
                                <template v-if="itemsGroup.length > 1 && (items[0].shopId === 0 || items[0]?.shopTitle)">
                                    <view class="product-card-group-title">
                                        <view class="shop-info" @click.stop="handleToShop(items[0].shopId)">
                                            <text class="text">
                                                {{ items[0].shopId === 0 ? items[0]?.shopTitle || $t("自营") : items[0]?.shopTitle }}
                                            </text>
                                            <text class="iconfont icon-xiangyou" />
                                        </view>
                                    </view>
                                </template>

                                <template v-for="item in items" :key="item.productId">
                                    <view class="item-box">
                                        <view class="product-card-item">
                                            <view class="product-card-item-left" @click="handleLink(item.productId, orderInfo.shopId)">
                                                <view class="left-img">
                                                    <tig-image :src="pickSkuPic(item)" />
                                                    <template v-if="item.isGift === 1">
                                                        <view class="gift-text">{{ $t("赠品") }}</view>
                                                    </template>
                                                </view>
                                            </view>
                                            <view class="product-card-item-right" @click="handleLink(item.productId, orderInfo.shopId)">
                                                <view class="product-info-box">
                                                    <view class="right-title line2">
                                                        <template v-if="showPresaleCard">
                                                            <text class="presale-tag">{{ $t("预售") }}</text>
                                                        </template>
                                                        <text>{{ item.productName }}</text>
                                                    </view>
                                                    <format-price
                                                        :show-text="false"
                                                        :font-style="amountStyle"
                                                        :currency-style="amountStyle"
                                                        :decimals-style="amountStyle"
                                                        :price-data="item.price"
                                                        class="product-price"
                                                    />
                                                </view>
                                                <view v-if="hasSkuText(item.skuData)" class="sku-card">
                                                    <view class="sku-item line1">{{ getSkuText(item.skuData) }}</view>
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
                                                                        <text class="content-text">{{ attr.attrValue }}</text>
                                                                        <format-price :show-text="false" class="price" :price-data="attr.attrPrice" />
                                                                        <view class="num">x{{ item.quantity }}</view>
                                                                    </view>
                                                                </view>
                                                            </template>
                                                        </view>
                                                    </view>
                                                </template>
                                                <view class="right-price">
                                                    <view class="right-price-quantity">
                                                        x
                                                        <text>{{ item.quantity }}</text>
                                                    </view>
                                                </view>
                                            </view>
                                            <template
                                                v-if="
                                                    !isMeituanDelivery &&
                                                    orderInfo.payStatus === 2 &&
                                                    item.isGift === 0 &&
                                                    !pickupAftersalesDisabled &&
                                                    !isLotteryOrder
                                                "
                                            >
                                                <view class="item-btn">
                                                    <template
                                                        v-if="
                                                            orderInfo.availableActions.toAftersales &&
                                                            canItemApplyAftersales(item) &&
                                                            !isGiftCardExchangeOrder &&
                                                            orderInfo.orderSource !== 'pos' &&
                                                            !isLotteryOrder &&
                                                            !(orderInfo.cycleInfo && Object.keys(orderInfo.cycleInfo)?.length > 0) &&
                                                            item.quantity > (item.aftersalesNumber || 0)
                                                        "
                                                    >
                                                        <tig-button2 class="btn" @click="handleAfterSale(item.itemId)">
                                                            {{ $t("申请售后") }}
                                                        </tig-button2>
                                                    </template>
                                                    <template v-else-if="item.aftersalesItem">
                                                        <tig-button2 class="btn" @click="handleAfterSaleDetail(item.aftersalesItem.aftersaleId)">
                                                            {{ $t("售后详情") }}
                                                        </tig-button2>
                                                    </template>
                                                </view>
                                            </template>
                                        </view>
                                    </view>
                                </template>
                            </view>
                        </template>
                    </view>

                    <view v-if="hasECardInfo" class="product-card1 card-space">
                        <template v-for="item in orderInfo.items" :key="item.productId">
                            <view v-if="item.eCard?.length > 0" class="item-box">
                                <template v-if="orderInfo.orderType === 8 && item.eCard?.length > 0">
                                    <view class="virtualsample-box">
                                        <view class="virtualsample-header">
                                            <view class="virtualsample-title">{{ $t("券码信息") }}</view>
                                            <view class="virtualsample-count">({{ $t("共{0}张", [item.eCard?.length || 0]) }})</view>
                                        </view>
                                        <template v-for="(subItem, cardIndex) in item.eCard" :key="`${item.itemId || item.productId}-${cardIndex}`">
                                            <view class="virtualsample-card">
                                                <view class="virtualsample-item">
                                                    <view class="virtualsample-item-con">
                                                        <view class="label">{{ $t("卡号") }}：</view>
                                                        <view class="value">
                                                            {{ getMaskedCardNumber(subItem.cardNumber, `${item.itemId || item.productId}-${cardIndex}`) }}
                                                        </view>
                                                    </view>
                                                    <view
                                                        class="virtualsample-view-btn"
                                                        @click="toggleCardView(`${item.itemId || item.productId}-${cardIndex}`)"
                                                    >
                                                        <text
                                                            class="iconfont-h5"
                                                            :class="
                                                                cardViewStates[`${item.itemId || item.productId}-${cardIndex}`]
                                                                    ? 'icon-xianshi'
                                                                    : 'icon-buxianshi'
                                                            "
                                                        />
                                                        <text class="view-text">{{ $t("查看券码") }}</text>
                                                    </view>
                                                </view>
                                                <view class="virtualsample-item">
                                                    <view class="virtualsample-item-con">
                                                        <view class="label">{{ $t("卡密") }}：</view>
                                                        <view class="value">
                                                            {{ getMaskedCardPassword(subItem.cardPwd, `${item.itemId || item.productId}-${cardIndex}`) }}
                                                        </view>
                                                    </view>
                                                </view>
                                            </view>
                                        </template>
                                    </view>
                                </template>
                            </view>
                        </template>
                    </view>

                    <template v-if="orderInfo.orderType === 6 && orderInfo.items[0].virtualSample">
                        <view class="virtualsample card-space">
                            <view class="virtualsample-label">{{ $t("虚拟内容") }}：</view>
                            <view class="virtualsample-value">
                                <rich-text class="desc-text-item" :nodes="orderInfo.items[0].virtualSample" />
                            </view>
                        </view>
                    </template>

                    <view class="money-card card-space">
                        <view class="money-title-box" @click="showUp = !showUp">
                            <view class="money-title">
                                {{ $t("订单总价") }}
                            </view>
                            <view class="money-price">
                                <format-price
                                    :show-text="false"
                                    :is-bottom="false"
                                    :font-style="totalStyle"
                                    :currency-style="totalStyle"
                                    :decimals-style="totalStyle"
                                    :price-data="orderInfo.totalAmount"
                                    style="margin-right: 10rpx"
                                />
                                <text v-if="showUp" class="iconfont-h5 icon-up" />
                                <text v-else class="iconfont-h5 icon-arrowdown" />
                            </view>
                        </view>
                        <template v-if="showUp">
                            <view class="money-card-item">
                                <text class="order-item-label bold">{{ $t("商品总价") }}</text>
                                <format-price
                                    :show-text="false"
                                    :is-bottom="false"
                                    :font-style="amountStyle"
                                    :currency-style="amountStyle"
                                    :decimals-style="amountStyle"
                                    :price-data="orderInfo.productAmount"
                                />
                            </view>
                            <view v-if="orderInfo.unpaidAmount && orderInfo.unpaidAmount > 0" class="money-card-item">
                                <text class="order-item-label bold">{{ $t("待支付金额") }}</text>
                                <format-price
                                    :show-text="false"
                                    :is-bottom="false"
                                    :font-style="amountStyle"
                                    :currency-style="amountStyle"
                                    :decimals-style="amountStyle"
                                    :price-data="orderInfo.unpaidAmount"
                                />
                            </view>
                            <view v-if="orderInfo.serviceFee && Number(orderInfo.serviceFee) > 0" class="money-card-item">
                                <text class="order-item-label">{{ $t("附加费用") }}</text>
                                <format-price
                                    :show-text="false"
                                    :is-bottom="false"
                                    :font-style="amountStyle"
                                    :currency-style="amountStyle"
                                    :decimals-style="amountStyle"
                                    :price-data="orderInfo.serviceFee"
                                />
                            </view>
                            <!-- 运费：除到店自提外，其它类型订单均需展示；运费=0 显示对应文案 -->
                            <view v-if="showShippingFeeRow" class="money-card-item">
                                <text class="order-item-label">{{ $t(shippingFeeTitleKey) }}</text>
                                <template v-if="shippingFeeAmount > 0">
                                    <format-price
                                        :show-text="false"
                                        :is-bottom="false"
                                        :font-style="amountStyle"
                                        :currency-style="amountStyle"
                                        :decimals-style="amountStyle"
                                        :price-data="shippingFeeAmount"
                                    />
                                </template>
                                <template v-else>
                                    <text class="free-text">{{ $t(shippingFeeFreeTextKey) }}</text>
                                </template>
                            </view>
                            <view v-if="orderInfo.cycleCardInfo && orderInfo.cycleCardInfo?.unitPrice" class="money-card-item">
                                <text class="order-item-label">{{ $t("次卡抵扣") }}</text>
                                <view class="order-item-price">
                                    <text>-</text>
                                    <format-price
                                        :show-text="false"
                                        :is-bottom="false"
                                        :font-style="discountStyle"
                                        :currency-style="discountStyle"
                                        :decimals-style="discountStyle"
                                        :price-data="orderInfo.cycleCardInfo?.unitPrice"
                                    />
                                </view>
                            </view>
                            <view v-if="orderInfo.balance && orderInfo.balance > 0" class="money-card-item">
                                <text class="order-item-label">{{ $t("使用余额") }}</text>
                                <format-price
                                    base
                                    :show-text="false"
                                    :is-bottom="false"
                                    :font-style="discountStyle"
                                    :currency-style="discountStyle"
                                    :decimals-style="discountStyle"
                                    :price-data="orderInfo.balance"
                                />
                            </view>
                            <view v-if="orderInfo.pointsAmount && orderInfo.pointsAmount > 0" class="money-card-item">
                                <text class="order-item-label">{{ `${$t("使用")}${configStore.integralName}` }}</text>
                                <format-price
                                    :show-text="false"
                                    :is-bottom="false"
                                    :font-style="discountStyle"
                                    :currency-style="discountStyle"
                                    :decimals-style="discountStyle"
                                    :price-data="orderInfo.pointsAmount"
                                />
                            </view>
                            <view v-if="orderInfo.couponAmount && orderInfo.couponAmount > 0" class="money-card-item">
                                <text class="order-item-label">{{ $t("优惠券") }}</text>
                                <format-price
                                    :show-text="false"
                                    :is-bottom="false"
                                    :font-style="discountStyle"
                                    :currency-style="discountStyle"
                                    :decimals-style="discountStyle"
                                    :price-data="orderInfo.couponAmount"
                                />
                            </view>
                            <view v-if="orderInfo.discountAmount && orderInfo.discountAmount > 0" class="money-card-item">
                                <text class="order-item-label">{{ $t("其他优惠") }}</text>
                                <format-price
                                    :show-text="false"
                                    :is-bottom="false"
                                    :font-style="discountStyle"
                                    :currency-style="discountStyle"
                                    :decimals-style="discountStyle"
                                    :price-data="orderInfo.discountAmount"
                                />
                            </view>
                            <view v-if="orderInfo.payStatus == 2" class="money-card-item">
                                <text class="order-item-label bold">{{ $t("实付款") }}</text>
                                <format-price
                                    :show-text="false"
                                    :is-bottom="false"
                                    :font-style="amountStyle"
                                    :currency-style="amountStyle"
                                    :decimals-style="amountStyle"
                                    :price-data="orderInfo.totalAmount"
                                />
                            </view>
                        </template>
                    </view>

                    <view v-if="!isMeituanDelivery" class="pay-card card-space">
                        <template v-if="orderInfo.shippingTypeName">
                            <view class="pay-card-item">
                                <text class="order-item-label">{{ $t("配送方式") }}</text>
                                <text>{{ $t(orderInfo.shippingTypeName) }}</text>
                            </view>
                        </template>
                        <view class="pay-card-item">
                            <text class="order-item-label">{{ $t("支付方式") }}：</text>
                            <text v-if="isGiftCardExchangeOrder">{{ $t("礼品卡兑换") }}</text>
                            <text v-else>{{ orderInfo.payTypeId === 1 ? $t("在线支付") : orderInfo.payTypeId === 2 ? $t("货到付款") : $t("线下支付") }}</text>
                        </view>
                    </view>

                    <view v-if="!isMeituanDelivery" class="order-card card-space">
                        <view class="order-card-item justify-between">
                            <text class="order-item-label">{{ $t("订单编号") }}</text>
                            <text>{{ orderInfo.orderSn }}</text>
                        </view>
                        <view class="order-card-item justify-between">
                            <text class="order-item-label">{{ $t("下单时间") }}</text>
                            <text>{{ orderInfo.addTime }}</text>
                        </view>
                        <template v-if="orderInfo.autoDeliveryDays">
                            <view class="order-card-item">{{ $t("订单将在{0}天后自动收货", [orderInfo.autoDeliveryDays]) }}</view>
                        </template>
                        <template v-else>
                            <view class="order-card-item">{{ oStatusInfo[orderInfo.orderStatus] }}</view>
                        </template>
                    </view>

                    <template
                        v-if="
                            !isMeituanDelivery &&
                            orderInfo.orderId &&
                            (orderInfo.orderStatus === 2 || orderInfo.orderStatus === 5) &&
                            orderInfo.isCycleOrder !== 1 &&
                            orderInfo.shippingTypeId !== 3
                        "
                    >
                        <view class="expressage-card card-space">
                            <template v-if="shippingInfo.length === 0">
                                <view class="shippingInfo-title">{{ $t("物流信息") }}</view>
                            </template>

                            <view class="expressage-box">
                                <view class="expressage-info">
                                    <view class="">{{ orderInfo.logisticsName }}</view>
                                    <view class="">{{ orderInfo.trackingNo }}</view>
                                </view>
                                <view v-if="orderInfo.logisticsName !== '无需配送'" class="copy-btn" @click="handleCopy(orderInfo.trackingNo)">
                                    {{ $t("复制") }}
                                </view>
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
                                    <view class="shippingInfo-btn" @click="showMore = !showMore">
                                        {{ showMore ? $t("收起") : $t("展开") }}
                                        <text v-if="showMore" class="iconfont-h5 icon-up" />
                                        <text v-else class="iconfont-h5 icon-arrowdown" />
                                    </view>
                                </view>
                            </template>
                            <template v-else>
                                <view class="emptyText">
                                    {{ emptyText }}
                                </view>
                            </template>
                        </view>
                    </template>

                </view>

                <!-- orderType 14 为抽奖商品订单：禁展示取消/付款/确认收货/售后底栏 -->
                <template
                    v-if="
                        (orderInfo.availableActions.cancelOrder ||
                            orderInfo.availableActions.toPay ||
                            orderInfo.availableActions.confirmReceipt ||
                            (orderInfo.availableActions.toAftersales &&
                                !orderInfo.afterSalesIng &&
                                !hasItemAftersales &&
                                !giftCardAftersalesBlocked &&
                                !isGiftCardExchangeOrder &&
                                !pickupAftersalesDisabled &&
                                !isLotteryOrder)) &&
                        orderInfo.orderSource !== 'pos' &&
                        !isLotteryOrder
                    "
                >
                    <tig-fixed-placeholder height="100rpx" background-color="#fff">
                        <view class="order-info-btn">
                            <view class="order-info-btn-box" :class="{ 'presale-btn-box': showPresaleFinalPayBtn }">
                                <tig-button2 v-if="orderInfo.availableActions.cancelOrder" class="btn" @click="handleCancelOrder(orderInfo.orderId)">
                                    {{ $t("取消订单") }}
                                </tig-button2>
                                <tig-button2
                                    v-if="orderInfo.availableActions.toPay"
                                    theme
                                    style="height: 100%"
                                    :class="{ 'presale-final-btn': showPresaleFinalPayBtn }"
                                    :disabled="isFinalPaymentBlocked"
                                    @click="handlePay(orderInfo.orderId)"
                                >
                                    <template v-if="showPresaleFinalPayBtn">
                                        <view class="presale-final-btn-content">
                                            <view class="presale-final-btn-amount">
                                                <text class="presale-final-btn-text">{{ $t("尾款") }}</text>
                                                <format-price
                                                    :is-bottom="false"
                                                    :price-data="productPresale?.finalPaymentAmount"
                                                    :font-style="{ fontSize: '26rpx', fontWeight: 'bold' }"
                                                />
                                            </view>
                                            <text class="presale-final-btn-text">{{ $t("去支付") }}</text>
                                        </view>
                                    </template>
                                    <template v-else>
                                        {{ isFinalPaymentBlocked ? $t("尾款未到支付时间") : $t("去支付") }}
                                    </template>
                                </tig-button2>
                                <tig-button2 v-if="orderInfo.availableActions.confirmReceipt" class="btn" @click="handleConfirmReceipt(orderInfo.orderId)">
                                    {{ $t("确认已收货") }}
                                </tig-button2>
                                <tig-button2
                                    v-if="
                                        orderInfo.availableActions.toAftersales &&
                                        !orderInfo.afterSalesIng &&
                                        !hasItemAftersales &&
                                        !giftCardAftersalesBlocked &&
                                        !isGiftCardExchangeOrder &&
                                        orderInfo.orderSource !== 'pos' &&
                                        !pickupAftersalesDisabled &&
                                        !isLotteryOrder
                                    "
                                    class="btn"
                                    @click="handleAfterSale(null)"
                                >
                                    {{ $t("整单售后") }}
                                </tig-button2>
                                <tig-button2
                                    v-if="orderInfo.grouponPromotionRecordId"
                                    class="btn"
                                    :plain="true"
                                    @click="handleToGrouponDetail(orderInfo.grouponPromotionRecordId)"
                                >
                                    {{ $t("拼团详情") }}
                                </tig-button2>
                            </view>
                        </view>
                    </tig-fixed-placeholder>
                </template>
            </template>
        </view>
    </tig-layout>

    <CancelOrderReasonPopup ref="cancelOrderReasonPopupRef" @success="__getOrder" />
</template>

<script setup lang="ts">
/**
 * 订单详情页
 * - 展示订单状态流程、收货信息、物流信息
 * - 展示商品列表，支持单品售后和整单售后
 * - 虚拟商品订单展示券码信息（支持查看/隐藏）
 * - 根据订单状态显示不同操作按钮（取消/付款/确认收货/售后）
 */
import service from "@/components/service/index.vue";
import presaleCard from "./src/presaleCard.vue";
import { onShow, onLoad, onUnload } from "@dcloudio/uni-app";
import { getOrder, confirmReceipt, getShippingInfo } from "@/api/user/order";
import type { OrderInfoItemElement, OrderInfoResponseItem, ShippingInfoTrace } from "@/types/user/order";
import { ref, computed } from "vue";
import { useI18n } from "vue-i18n";
import { useUserStore } from "@/store/user";
import { useConfigStore } from "@/store/config";
import { useScrollTop } from "@/hooks";
import { copy, getSkuText, hasSkuText, toStoreOrShopHome, isOverseas } from "@/utils";
import CancelOrderReasonPopup from "@/components/order/CancelOrderReasonPopup.vue";
import { pickSkuPic } from "@/utils/format";
import { canGiftCardItemApplyAftersales, isGiftCardAftersalesBlocked } from "@/utils/giftCard";

const configStore = useConfigStore();

/** 海外皮肤：用于增值税等专属展示 */
const isOverseasSkin = isOverseas();

const userStore = useUserStore();

const { t } = useI18n();

/** 金额行统一字号/字色（对齐跨境扁平卡）；优惠类金额用红色 */
const amountStyle = { fontSize: "26rpx", color: "#1a1a1a", fontWeight: "bold" };
const discountStyle = { fontSize: "26rpx", color: "#cc0c1c", fontWeight: "bold" };
const totalStyle = { fontSize: "30rpx", color: "#1a1a1a", fontWeight: "bold" };

interface IoStatusInfo {
    [key: number]: string;
}

const oStatusInfo = ref<IoStatusInfo>({
    0: t("您的订单已提交成功，请尽快完成付款哦！"),
    1: t("感谢您在本店的购物，您的订单已提交成功，我们会尽快为您发货！"),
    2: t("您的订单已经发货，请注意查收，祝您购物愉快！"),
    3: t('您的订单已被取消，您可以通过"再次购买"，重新购买订单中的商品。'),
    4: t('您的订单被认定为无效订单，订单已被关闭。您可以通过"再次购买"，重新购买订单中的商品。给您造成的不便请多多谅解！'),
    5: t("您的订单已经确认收货，感谢您在本店购物，祝您生活愉快！")
});

const orderInfo = ref<OrderInfoResponseItem>({} as OrderInfoResponseItem);
const hasECardInfo = computed(() => {
    if (orderInfo.value.orderType !== 8 || !Array.isArray(orderInfo.value.items)) return false;
    return orderInfo.value.items.some((item) => Array.isArray(item.eCard) && item.eCard.length > 0);
});
// 预售信息（存在时用于替换顶部步骤条、以及尾款支付入口）
const productPresale = computed(() => {
    const info: any = orderInfo.value as any;
    return info?.presaleInfo || null;
});
/** 是否展示预售流程卡（有则替换状态文案区） */
const showPresaleCard = computed(() => {
    const mode = Number(productPresale.value?.saleMode ?? 0);
    return mode > 0 && !!productPresale.value;
});
/** 定金预售待付尾款阶段（saleMode=2 且 presaleStatus=1） */
const isFinalPaymentPhase = computed(() => {
    const p: any = productPresale.value;
    return Number(p?.saleMode) === 2 && Number(p?.presaleStatus) === 1;
});
/** 尾款未到支付时间：尾款阶段但后端未放行支付 */
const isFinalPaymentBlocked = computed(() => isFinalPaymentPhase.value && productPresale.value?.canPayFinalPayment === false);
/** 底栏支付按钮是否走「尾款 ¥x 去支付」的宽按钮形态 */
const showPresaleFinalPayBtn = computed(() => isFinalPaymentPhase.value && !!productPresale.value && !isFinalPaymentBlocked.value);

const payEndTime = computed(() => String((orderInfo.value as any)?.payEndTime || ""));
const isPendingPay = computed(() => !!orderInfo.value?.availableActions?.toPay);
const payEndTimeMs = computed(() => {
    const raw = payEndTime.value;
    if (!raw) return 0;
    const ms = Date.parse(raw.replace(/-/g, "/"));
    return Number.isFinite(ms) ? ms : 0;
});
const showPayCountdown = computed(() => isPendingPay.value && payEndTimeMs.value > Date.now());

let countdownTimer: ReturnType<typeof setInterval> | null = null;
const stopCountdownWatch = () => {
    if (countdownTimer) {
        clearInterval(countdownTimer);
        countdownTimer = null;
    }
};
const startCountdownWatch = () => {
    stopCountdownWatch();
    if (!showPayCountdown.value) return;
    countdownTimer = setInterval(() => {
        if (payEndTimeMs.value && payEndTimeMs.value - Date.now() <= 0) {
            stopCountdownWatch();
            __getOrder();
        }
    }, 1000);
};
onUnload(stopCountdownWatch);

const { scrollTop } = useScrollTop();
const NAVBAR_SCROLL_THRESHOLD = 50;
const navbarBackground = computed(() => {
    if (!isMeituanDelivery.value) return "#fff";
    const opacity = Math.min(Math.max(scrollTop.value / NAVBAR_SCROLL_THRESHOLD, 0), 1);
    return `rgba(255, 255, 255, ${opacity})`;
});

const isMeituanDelivery = computed(() => {
    let delivery: any = null;
    return !!delivery && Number(delivery.carrierType) === 2;
});
const showMore = ref(false);
// 金额明细默认展开
const showUp = ref(true);
const isGiftCardExchangeOrder = computed(() => {
    return orderInfo.value.orderType === 12;
});
/** 礼品卡订单：实体卡按实体商品放开售后，仅含电子卡时禁止售后 */
const giftCardAftersalesBlocked = computed(() => isGiftCardAftersalesBlocked(orderInfo.value));
const canItemApplyAftersales = (item: OrderInfoItemElement) => canGiftCardItemApplyAftersales(orderInfo.value, item);
// 自提订单确认收货（orderStatus>=3 即已完成/已取消/已关闭）后禁止售后
/** 抽奖商品订单，不支持售后 */
const isLotteryOrder = computed(() => {
    return orderInfo.value.orderType === 14;
});
const hasItemAftersales = computed(() => {
    return orderInfo.value.items?.some((item) => Boolean(item.aftersalesItem)) ?? false;
});
const pickupAftersalesDisabled = computed(() => {
    return !!orderInfo.value.isPickup && orderInfo.value.orderStatus >= 3;
});
// 收货信息齐全时才在状态区展示地址块（虚拟/卡密类订单无地址）
const showAddressBox = computed(() => {
    return !!(orderInfo.value.userAddress && orderInfo.value.consignee && orderInfo.value.mobile);
});

/** 顶部状态副文案（对齐跨境：tip / 自动收货 / oStatusInfo） */
const statusSubText = computed(() => {
    if (showPayCountdown.value) return "";
    if (isPendingPay.value) return t("请尽快完成支付，以免订单被自动取消");
    const tip = String((orderInfo.value as any)?.orderStatusTip || "").trim();
    if (tip) return tip;
    const status = Number(orderInfo.value?.orderStatus ?? -1);
    const autoDays = Number(orderInfo.value?.autoDeliveryDays ?? 0);
    if ((status === 1 || status === 2) && autoDays > 0) {
        return t("订单将在{0}天后自动收货", [autoDays]);
    }
    if (status in oStatusInfo.value) return oStatusInfo.value[status];
    return "";
});

/** 订单商品总件数（对齐跨境卡头「订单商品(N)」） */
const itemsTotalQuantity = computed(() => {
    const items = orderInfo.value?.items;
    if (!Array.isArray(items)) return 0;
    return items.reduce((sum, p) => sum + Number(p.quantity || 0), 0);
});
/** 商品卡头店名：订单 shop > 单店商品标题 > 多店留空（分组标题展示）> 官方自营店 */
const productCardShopTitle = computed(() => {
    if (orderInfo.value?.shop?.shopTitle) return orderInfo.value.shop.shopTitle;
    const items = (orderInfo.value?.items || []) as any[];
    const shopIds = [...new Set(items.map((i) => i.shopId))];
    if (shopIds.length > 1) return "";
    if (shopIds.length === 1) {
        const first = items[0];
        if (first?.shopId === 0) return first?.shopTitle || t("官方自营店");
        if (first?.shopTitle) return first.shopTitle;
    }
    return t("官方自营店");
});
/** 卡头可跳转的店铺 id；多店不跳 */
const productCardShopId = computed(() => {
    if (orderInfo.value?.shop?.shopId != null) return orderInfo.value.shop.shopId;
    const items = (orderInfo.value?.items || []) as any[];
    const shopIds = [...new Set(items.map((i) => i.shopId))];
    if (shopIds.length === 1) return shopIds[0] ?? null;
    return null;
});
const cardViewStates = ref<Record<string | number, boolean>>({});


const orderFlowType = computed(() => {
    const flat: any = orderInfo.value as any;
    return Number(flat?.orderType) || 0;
});

const shippingFeeAmount = computed(() => Number((orderInfo.value as any)?.shippingFee ?? 0));


// 优先按商品类型判断：虚拟商品=2，卡密商品=3，付费内容=4，电子礼品卡=8（这些都无需配送，不展示运费）
const noDeliveryProductTypes = new Set([2, 3, 4, 8]);
/**
 * - 自提：不显示
 * - 预约（orderType=9/10）：上门服务费；0元显示“免费上门”
 * - 同城配送（有配送员信息 / 配送方式含“同城”）：配送费；0元显示“免配送费”
 * - 其它：快递；0元显示“包邮”
 */
const isPickupOrder = computed(() => {
    const shippingTypeName = String(orderInfo.value?.shippingTypeName || "");
    // 优先用后端返回的配送方式名称判断
    if (shippingTypeName.includes(t("自提"))) return true;
    // 兜底：存在自提信息且不是预约上门时，视为自提订单
    return !!orderInfo.value?.isPickup && !orderInfo.value?.isReservationHome && !!orderInfo.value?.pickupDetail;
});
const isReserveOrder = computed(() => orderInfo.value?.orderType === 9 || orderInfo.value?.orderType === 10);
const isLocalDeliveryOrder = computed(() => {
    const shippingTypeName = String(orderInfo.value?.shippingTypeName || "");
    return shippingTypeName.includes("同城");
});
const isNoDeliveryOrder = computed(() => {
    const items = ((orderInfo.value as any)?.items || []) as any[];
    const hasNoDeliveryProductType = items.some((item) => noDeliveryProductTypes.has(Number(item?.productType)));
    if (hasNoDeliveryProductType) return true;

    // 兜底：配送方式名称为“无需配送”
    const shippingTypeName = String((orderInfo.value as any)?.shippingTypeName || "");
    return shippingTypeName.includes(t("无需配送"));
});

// 运费行展示规则：
// - 到店自提：不展示
// - 同城配送/预约上门：仍展示（即使为 0）
// - 其它：若“无需配送”则不展示，否则展示（即使为 0）
const showShippingFeeRow = computed(() => {
    // 自提：不显示
    if (isPickupOrder.value) return false;
    // 预约/同城：始终显示（0元显示对应文案）
    if (isReserveOrder.value || isLocalDeliveryOrder.value) return true;
    // 其它订单：若无需配送则不显示
    if (isNoDeliveryOrder.value) return false;
    // 默认（快递等）：显示（0元显示“包邮”）
    return true;
});

const shippingFeeTitleKey = computed(() => {
    if (isReserveOrder.value) return t("上门服务费");
    if (isLocalDeliveryOrder.value) return t("配送费");
    return t("快递");
});

const shippingFeeFreeTextKey = computed(() => {
    if (isReserveOrder.value) return t("免费上门");
    if (isLocalDeliveryOrder.value) return t("免配送费");
    return t("包邮");
});



const id = ref<null | number>();
const isFirstLoad = ref(true);
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
onShow(() => {
    if (isFirstLoad.value) {
        isFirstLoad.value = false;
        return;
    }
    if (id.value) {
        Promise.all([__getOrder(), __getShippingInfo()]);
    }
});

const itemsGroup = ref<any[]>([]);

/**
 * 获取订单详情
 * 将商品按店铺分组展示，支持多店铺订单场景
 */
const __getOrder = async () => {
    try {
        const result = await getOrder({ id: id.value });
        orderInfo.value = result;
        startCountdownWatch();

        // 按店铺ID分组商品，用于多店铺订单展示
        const itemsByShop = new Map<number, typeof result.items>();

        result.items.forEach((item) => {
            if (!itemsByShop.has(item.shopId)) {
                itemsByShop.set(item.shopId, []);
            }
            itemsByShop.get(item.shopId)!.push(item);
        });

        itemsGroup.value = Array.from(itemsByShop.values());
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

const cancelOrderReasonPopupRef = ref<{ open: (id: number) => void }>();
const handleCancelOrder = (id: number) => {
    cancelOrderReasonPopupRef.value?.open(id);
};

const handlePay = (id: number) => {
    uni.navigateTo({
        url: orderInfo.value.payTypeId === 3 ? `/pages/order/pay?orderId=${id}` : `/pages/order/cashier?orderId=${id}&from=orderInfo`
    });
};

const handleToShop = (id: number) => {
    toStoreOrShopHome(id, { navType: "navigateTo" });
};

const handleConfirmReceipt = (id: number) => {
    uni.showModal({
        title: t("提示"),
        content: t("确认收货吗？"),
        cancelText: t("取消"),
        confirmText: t("确认"),
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

/**
 * 申请售后
 * @param itemId 商品项ID，传入则为单品售后，不传则为整单售后
 */
const handleAfterSale = (itemId: null | number) => {
    if (itemId) {
        uni.navigateTo({
            url: `/pages/user/afterSale/edit?itemId=${itemId}&orderId=${orderInfo.value.orderId}`
        });
    } else {
        uni.navigateTo({
            url: `/pages/user/afterSale/edit?orderId=${orderInfo.value.orderId}`
        });
    }
};

const handleAfterSaleDetail = (id: number) => {
    uni.navigateTo({
        url: `/pages/user/afterSale/info?id=${id}`
    });
};

const handleLink = (id: number, shopId?: number) => {
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

const handleToGrouponDetail = (grouponPromotionRecordId: number) => {
    uni.navigateTo({
        url: `/pages/product/groupBuying?grouponPromotionRecordId=${grouponPromotionRecordId}`
    });
};

const handleCallPhone = (phone: string) => {
    if (!phone) return;
    uni.makePhoneCall({
        phoneNumber: phone,
        fail: () => {
            uni.showToast({
                title: t("拨打电话失败"),
                icon: "none"
            });
        }
    });
};

/**
 * 判断是否显示客服入口
 * - 优先使用店铺配置的客服入口（kefuInlet包含2表示开启）
 * - 无店铺配置时使用全局客服配置
 */
const showService = computed(() => {
    if (orderInfo.value.shop && Object.keys(orderInfo.value.shop).length > 0) {
        // kefuInlet=2 表示在订单详情页显示客服入口
        if (orderInfo.value.shop.kefuInlet && orderInfo.value.shop.kefuInlet.length > 0 && orderInfo.value.shop.kefuInlet.includes(2)) {
            return true;
        }
        return false;
    } else {
        return userStore.serviceConfig.show;
    }
});

/**
 * 切换卡券显示/隐藏状态
 * 用于虚拟商品订单的券码信息保护
 */
const toggleCardView = (cardKey: string | number) => {
    cardViewStates.value[cardKey] = !cardViewStates.value[cardKey];
};

const getMaskedCardNumber = (cardNumber: string, cardKey: string | number) => {
    if (cardViewStates.value[cardKey]) {
        return cardNumber;
    }
    if (!cardNumber || cardNumber.length <= 5) {
        return cardNumber;
    }
    const firstChar = cardNumber[0];
    const lastFour = cardNumber.slice(-4);
    const maskedLength = cardNumber.length - 5;
    return firstChar + "*".repeat(maskedLength) + lastFour;
};

const getMaskedCardPassword = (cardPwd: string, cardKey: string | number) => {
    if (cardViewStates.value[cardKey]) {
        return cardPwd;
    }
    if (!cardPwd || cardPwd.length <= 5) {
        return cardPwd;
    }
    const firstChar = cardPwd[0];
    const lastFour = cardPwd.slice(-4);
    const maskedLength = cardPwd.length - 5;
    return firstChar + "*".repeat(maskedLength) + lastFour;
};
</script>

<style lang="scss" scoped>
.phone-link {
    color: var(--general);
    text-decoration: none;
}

.sku-card {
    background-color: #fff !important;
    padding: 0;
}

.extraskudata-box {
    display: flex;
    .extraskudata {
        padding: 0;
        border-radius: 10rpx;
        margin-top: 5rpx;
        color: #969799;
        display: flex;
        flex-direction: column;
        .extraskudata-item {
            font-size: 22rpx;
            display: flex;
            line-height: 40rpx;

            .title {
                flex-shrink: 0;
            }

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
                    white-space: normal;
                    word-break: break-all;
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
    padding-bottom: 20rpx;

    &:last-child {
        padding-bottom: 0;
    }
}

.shop-info {
    display: flex;
    align-items: center;
    font-size: 26rpx;
    font-weight: bold;
}

.product-card-group {
    & + .product-card-group {
        margin-top: 20rpx;
        padding-top: 20rpx;
        border-top: 0.5px solid #e0e1e5;
    }
}

.product-card-group-title {
    margin: 0 20rpx 20rpx 0;
}

.virtualsample-box {
    padding-top: 0;

    .virtualsample-header {
        display: flex;
        align-items: baseline;
        margin-bottom: 12rpx;

        .virtualsample-title {
            font-size: 28rpx;
            font-weight: 600;
            color: #333;
            margin-right: 10rpx;
        }

        .virtualsample-count {
            font-size: 24rpx;
            color: #999;
        }
    }

    .virtualsample-card {
        background-color: #fff;
        padding: 12rpx 0 0 0;
        margin-bottom: 20rpx;
        display: flex;
        flex-direction: column;
        position: relative;
        border-top: 1px solid #f2f2f2;

        &:last-child {
            margin-bottom: 0;
        }
    }

    .virtualsample-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 16rpx;

        &:last-child {
            margin-bottom: 0;
        }

        .virtualsample-item-con {
            display: flex;
            font-size: 24rpx;
            line-height: 40rpx;
            flex: 1;
            padding-right: 140rpx;

            .label {
                flex-shrink: 0;
                color: #999;
                margin-right: 8rpx;
            }

            .value {
                color: #333;
            }
        }

        .virtualsample-view-btn {
            display: flex;
            align-items: center;
            gap: 6rpx;
            cursor: pointer;
            flex-shrink: 0;
            position: absolute;
            right: 24rpx;
            top: 50%;
            transform: translateY(-50%);

            .view-text {
                font-size: 26rpx;
                line-height: 1;
                color: #333;
            }
        }
    }
}

.card-space {
    padding: 20rpx 0;
    background-color: #fff;
    margin-top: 15rpx;
}

.delivery-address-card {
    display: flex;
    align-items: flex-start;
    padding: 30rpx 24rpx;

    .delivery-address-label {
        flex-shrink: 0;
        margin-right: 30rpx;
        color: #999;
        font-size: 24rpx;
        line-height: 36rpx;
    }

    .delivery-address-main {
        flex: 1;
        min-width: 0;

        .delivery-address-detail {
            color: #1a1a1a;
            font-size: 24rpx;
            line-height: 36rpx;
        }

        .delivery-address-contact {
            margin-top: 20rpx;
            color: #333;
            font-size: 24rpx;
            line-height: 36rpx;

            .delivery-address-name {
                margin-right: 24rpx;
            }
        }
    }
}

.address-text {
    width: calc(100% - 160rpx);
    text-align: end;
}

.order-item-label {
    color: #1a1a1a;
}

.bold {
    font-weight: bold;
}

.free-text {
    color: #269636;
    font-size: 25rpx;
    font-weight: 700;
    line-height: 32rpx;
}

.icon-box {
    text-align: right;
    // #ifdef MP-WEIXIN
    // 小程序端 navbar 右侧已预留胶囊宽度，图标整体左移避让胶囊按钮
    text-align: left;
    transform: translateX(-40rpx);
    // #endif
    .iconfont-h5 {
        font-size: 36rpx;
    }
}

.order-info-steps {
    padding: 25rpx;
    background-color: #fff;
    overflow: hidden;

    .order-info-header {
        .status-title {
            font-size: 40rpx;
            font-weight: 700;
            color: #1a1a1a;
            line-height: 1.2;
        }

        .status-sub {
            margin-top: 16rpx;
            font-size: 26rpx;
            color: #8b8e97;
            display: flex;
            align-items: center;
            flex-wrap: wrap;

            .status-countdown {
                margin: 0 8rpx;
            }
        }
    }

    .address-box {
        display: flex;
        margin-top: 40rpx;
        color: #1a1a1a;

        // 有预售步骤时去掉上间距（与跨境一致）；剩余空隙来自 presale-box / presale-flow 的 padding-bottom
        &.address-box--presale {
            margin-top: 0;
        }

        .icon {
            width: 16px;
            margin-right: 8px;
            margin-top: 2rpx;
            .iconfont-h5 {
                font-size: 16px;
            }
        }
        .info {
            flex: 1;
            font-size: 26rpx;
            .title {
                display: flex;
                align-items: center;
                gap: 10rpx;
                .name {
                    font-weight: bold;
                }
            }
            .address-txt {
                margin-top: 10rpx;
            }
        }
    }
}

.order-info {
    &.order-info--meituan {
        .order-info-content {
            position: relative;
            z-index: 2;
        }
    }

    .bg {
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: calc(var(--nav-height) + 220rpx);
        background: linear-gradient(90deg, #f4f4f4, #fff7f8);
        z-index: 1;
    }

    .meituan-status {
        position: relative;
        z-index: 2;
        padding: 30rpx 30rpx 20rpx;

        .order-info-content-status-title {
            color: #ee0a24;
            font-size: 36rpx;
            font-weight: 800;
        }

        .order-info-content-status-desc {
            margin-top: 10rpx;
            color: #6a6a6a;
            font-size: 28rpx;
            font-weight: 500;
        }
    }
}

.order-info-content {
    .order-card {
        padding: 20rpx;
        .order-card-item {
            align-items: center;
            display: flex;
            font-size: 24rpx;
            padding: 12rpx 0;
        }
    }

    .expressage-card {
        padding: 20rpx;
    }

    .pay-card {
        font-size: 24rpx;
        padding: 0 20rpx 20rpx;
        .pay-card-item {
            padding: 20rpx 0;
            display: flex;
            justify-content: space-between;

            &:last-child {
                border-bottom: none;
                padding-bottom: 0;
            }
        }
    }

    .product-card {
        padding: 0 0 30rpx 20rpx;
        .product-card-title {
            font-size: 28rpx;
            font-weight: 700;
            line-height: 38rpx;
            border-bottom: 0.5px solid #e0e1e5;
            padding: 10rpx 0;
            margin-bottom: 20rpx;
            display: flex;
            align-items: center;
            justify-content: space-between;

            .product-card-title-right {
                display: flex;
                align-items: center;
                margin-right: 20rpx;
            }
        }

        .product-card-item {
            display: flex;
            position: relative;
            margin-bottom: 20rpx;
            padding-right: 20rpx;
            &:last-child {
                margin-bottom: 0;
            }

            .product-card-item-left {
                .left-img {
                    width: 140rpx;
                    height: 140rpx;
                    border-radius: 10rpx;
                    overflow: hidden;
                    margin-right: 20rpx;
                    box-shadow: 0px 2px 8px 0px rgba(0, 0, 0, 0.04);
                    position: relative;
                }
            }

            .product-card-item-right {
                font-size: 24rpx;
                flex: 1;
                min-width: 0;

                .product-info-box {
                    display: flex;
                    justify-content: space-between;
                    align-items: flex-start;

                    .right-title {
                        flex: 1;
                        width: auto;
                        min-width: 0;
                        font-weight: bold;
                        margin-bottom: 10rpx;

                        .presale-tag {
                            display: inline;
                            background-color: #e47200;
                            color: #fff;
                            padding: 4rpx 12rpx;
                            border-radius: 4rpx;
                            overflow: hidden;
                            font-size: 24rpx;
                            margin-right: 10rpx;
                        }
                    }

                    .product-price {
                        flex-shrink: 0;
                        white-space: nowrap;
                        margin-left: 8rpx;
                    }
                }

                .right-price {
                    display: flex;
                    font-size: 28rpx;
                    padding-top: 10rpx;

                    .right-price-quantity {
                        color: #888b94;
                        font-size: 20rpx;
                        text {
                            font-size: 26rpx;
                        }
                    }
                }
            }
            .item-btn {
                position: absolute;
                right: 20rpx;
                bottom: 0;
                display: flex;
                justify-content: center;
                align-items: center;
                column-gap: 10rpx;

                .btn {
                    color: #1a1a1a;
                    border-color: #888b94;
                }
            }
        }

        .meituan-item-btn {
            display: flex;
            justify-content: flex-end;
            align-items: center;
            column-gap: 20rpx;
            margin: 24rpx 20rpx 0 0;
        }
    }

    .product-card1 {
        padding: 20rpx;
    }

    .virtualsample {
        display: flex;
        width: 100%;
        column-gap: 20rpx;
        font-size: 24rpx;
        .virtualsample-label {
            padding-left: 20rpx;
            color: #999;
        }
        .virtualsample-value {
            flex: 1;
            padding-right: 20rpx;
            overflow: hidden;

            img {
                width: 100%;
                height: auto;
            }
        }
    }

    .money-card {
        font-size: 24rpx;
        padding: 20rpx 0;
        padding-bottom: 20rpx;
        .money-title-box {
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 10rpx 20rpx;
            font-weight: bold;
            text {
                font-weight: 500;
            }
            .money-price {
                display: flex;
                align-items: center;
            }
            .iconfont-h5 {
                font-size: 24rpx;
                color: #8b8e97;
            }
            .icon-arrowdown {
                display: inline-block;
                transform: translateY(2rpx);
            }
        }
        .money-card-item {
            display: flex;
            justify-content: space-between;
            padding: 20rpx 20rpx;
            &:last-child {
                padding-bottom: 0;
                border-bottom: none;
            }

            .order-item-price {
                font-weight: bold;
                color: var(--general);
                font-size: 28rpx;
                :deep(.util) {
                    font-size: 22rpx;
                    font-weight: normal;
                }
            }
        }
    }
}

.cycle-plan-entry {
    align-items: center;
}
.cycle-plan-right {
    display: flex;
    align-items: center;
    flex: 1;
    justify-content: flex-end;
    gap: 10rpx;
}
.cycle-plan-text {
    width: auto;
    max-width: calc(100% - 100rpx);
    display: flex;
    flex-direction: column;
    gap: 10rpx;
    .cycle-text-line {
        white-space: nowrap;
    }
}
.cycle-plan-arrow {
    font-size: 28rpx;
    color: #999;
}

.order-info-btn {
    height: 100%;
    width: 100%;
    display: flex;
    justify-content: flex-end;
    padding: 20rpx;
    border-top: 1rpx solid rgba(0, 0, 0, 0.12);

    .order-info-btn-box {
        display: flex;
        align-items: center;
        gap: 10rpx;

        &.presale-btn-box {
            flex: 1;
        }

        .btn {
            height: 100%;
            color: #1a1a1a;
            border-color: #888b94;
            background: none;
        }

        .presale-final-btn {
            flex: 1;
            padding: 0 20rpx;
            margin-right: 0;

            .presale-final-btn-content {
                display: flex;
                align-items: center;
                justify-content: center;
                gap: 20rpx;
                width: 100%;
                line-height: 1;

                :deep(.price),
                :deep(.currency),
                :deep(.decimals) {
                    line-height: 1;
                    padding: 0 !important;
                }
            }

            .presale-final-btn-amount {
                display: flex;
                align-items: baseline;
                gap: 6rpx;
                line-height: 1;
            }

            .presale-final-btn-text {
                font-size: 26rpx;
                font-weight: bold;
                line-height: 1;
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
    align-items: center;

    .expressage-info {
        display: flex;
        column-gap: 15rpx;
        align-items: baseline;
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

        .iconfont-h5 {
            font-size: 32rpx;
            color: rgb(153, 153, 153);
            display: inline-block;
            margin-left: 2rpx;
        }
        .icon-arrowdown {
            transform: translateY(8rpx);
        }
    }
}

.emptyText {
    padding-top: 15rpx;
    font-size: 24rpx;
}
</style>
