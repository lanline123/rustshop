<template>
    <tig-layout :title="$t('我的订单')">
        <view class="order-info">
            <template v-if="Object.keys(orderInfo).length">
                <template v-if="!productPresale?.saleMode">
                    <view class="order-info-steps">
                        <uni-steps active-color="var(--general)" :options="orderInfo.stepStatus.steps" :active="orderInfo.stepStatus.current" />
                    </view>
                </template>
                <template v-else>
                    <presaleCard :presale-info="productPresale" :total="orderInfo" />
                </template>

                <view class="order-info-content">
                    <view class="order-card card-space">
                        <view class="order-card-item justify-between">
                            <view class="item-left">
                                <text class="order-item-label">{{ $t("订单编号") }}：</text>
                                <text>{{ orderInfo.orderSn }}</text>
                            </view>
                        </view>
                        <view class="order-card-item justify-between">
                            <view class="item-right">
                                <text class="order-item-label">{{ $t("订单状态") }}：</text>
                                <text class="order-item-value">{{ orderInfo.orderStatusName }}</text>
                            </view>
                        </view>
                        <view class="order-card-item">
                            <text class="order-item-label">{{ $t("下单时间") }}：</text>
                            <text>{{ orderInfo.addTime }}</text>
                        </view>
                        <template v-if="orderInfo.autoDeliveryDays">
                            <view class="order-card-item">{{ $t("订单将在") }}{{ orderInfo.autoDeliveryDays }}{{ $t("天后自动收货") }}</view>
                        </template>
                        <template v-else>
                            <view class="order-card-item">{{ oStatusInfo[orderInfo.orderStatus] }}</view>
                        </template>
                    </view>
                    <template
                        v-if="
                            orderInfo.orderId &&
                            (orderInfo.orderStatus === 2 || orderInfo.orderStatus === 5) &&
                            orderInfo.isCycleOrder !== 1 &&
                            orderInfo.shippingTypeId !== 3
                        "
                    >
                        <view class="expressage-card card-space">
                            <template v-if="shippingInfo.length === 0">
                                <view class="shippingInfo-title"> {{ $t("物流信息") }} </view>
                            </template>

                            <view class="expressage-box">
                                <view class="expressage-info">
                                    <view class="">{{ orderInfo.logisticsName }}</view>
                                    <view class="">{{ orderInfo.trackingNo }}</view>
                                </view>
                                <view v-if="orderInfo.logisticsName !== '无需配送'" class="copy-btn" @click="handleCopy(orderInfo.trackingNo)">{{
                                    $t("复制")
                                }}</view>
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
                                        }}<text class="iconfont-h5 icon-arrowdown btn-icon" :class="{ rotate: showMore }" style="font-size: 32rpx; color: #999"
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


                    <view class="pay-card card-space">
                        <template v-if="orderInfo.userAddress && orderInfo.consignee && orderInfo.mobile">
                            <view class="pay-card-item">
                                <text class="order-item-label">{{ $t("收货信息") }}：</text>
                                <text class="address-text">{{ orderInfo.userAddress + " " + orderInfo.consignee + " " + orderInfo.mobile }}</text>
                            </view>
                        </template>

                        <template v-if="orderInfo.shippingTypeName">
                            <view class="pay-card-item">
                                <text class="order-item-label">{{ $t("配送方式") }}：</text>
                                <text>{{ orderInfo.shippingTypeName }}</text>
                            </view>
                        </template>
                        <view class="pay-card-item">
                            <text class="order-item-label">{{ $t("支付方式") }}：</text>
                            <text v-if="isGiftCardExchangeOrder">{{ $t("礼品卡兑换") }}</text>
                            <text v-else>{{ orderInfo.payTypeId === 1 ? $t("在线支付") : orderInfo.payTypeId === 2 ? $t("货到付款") : $t("线下支付") }}</text>
                        </view>
                        <template v-if="showService">
                            <view class="pay-card-item im-btn">
                                <service
                                    ref="serviceRef"
                                    :phone="orderInfo.shop && orderInfo.shop.kefuPhone ? orderInfo.shop.kefuPhone : ''"
                                    :order-id="orderInfo.orderId"
                                    :shop-id="orderInfo.shopId"
                                >
                                    <tig-button :plain="true" color="#333" :custom-style="{ height: '55rpx' }" @click="handleService">
                                        {{ $t("联系客服") }}
                                    </tig-button>
                                </service>
                            </view>
                        </template>
                    </view>

                    <view class="product-card card-space">
                        <template v-for="(items, index) in itemsGroup" :key="index">
                            <template v-if="items[0].shopId === 0">
                                <view class="shop-info">
                                    <text class="text"> {{ $t("自营") }} </text>
                                </view>
                            </template>
                            <template v-else-if="items[0]?.shopTitle">
                                <view class="shop-info" @click.stop="handleToShop(items[0].shopId)">
                                    <text class="text">
                                        {{ items[0]?.shopTitle }}
                                    </text>
                                    <text class="iconfont icon-xiangyou" />
                                </view>
                            </template>

                            <template v-for="item in items" :key="item.productId">
                                <view class="item-box">
                                    <view class="product-card-item">
                                        <view class="product-card-item-left" @click="handleLink(item.productId, orderInfo.shopId)">
                                            <view class="left-img">
                                                <tig-image :src="item.picThumb" />
                                                <template v-if="item.isGift === 1">
                                                    <view class="gift-text">{{ $t("赠品") }}</view>
                                                </template>
                                            </view>
                                        </view>
                                        <view class="product-card-item-right" @click="handleLink(item.productId, orderInfo.shopId)">
                                            <view class="right-title line2 presale-box">
                                                <template v-if="productPresale?.saleMode && productPresale?.saleMode > 0">
                                                    <text class="presale-tag">{{ $t("预售") }}</text>
                                                    <text class="separator">|</text>
                                                </template>
                                                <text>{{ item.productName }}</text>
                                            </view>
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
                                            <view class="right-price">
                                                <format-price
                                                    :show-text="false"
                                                    class="right-price-pricenum"
                                                    :decimals-style="{
                                                        fontSize: '23rpx',
                                                        fontWeight: 'bold'
                                                    }"
                                                    :currency-style="{
                                                        fontSize: '21rpx',
                                                        fontWeight: 'bold'
                                                    }"
                                                    :price-data="item.price"
                                                />
                                                <view class="right-price-quantity">x {{ item.quantity }}</view>
                                            </view>
                                        </view>
                                        <template v-if="orderInfo.payStatus === 2 && item.isGift === 0 && !pickupAftersalesDisabled">
                                            <view class="item-btn">
                                                <template v-if="item.aftersalesItem">
                                                    <tig-button
                                                        :plain="true"
                                                        color="#333"
                                                        :custom-style="{ height: '55rpx' }"
                                                        @click="handleAfterSaleDetail(item.aftersalesItem.aftersaleId)"
                                                    >
                                                        {{ $t("售后详情") }}
                                                    </tig-button>
                                                </template>
                                                <template
                                                    v-else-if="
                                                        orderInfo.availableActions.toAftersales &&
                                                        !isGiftCardOrder &&
                                                        !isGiftCardExchangeOrder &&
                                                        orderInfo.orderSource !== 'pos' &&
                                                        !(orderInfo.cycleInfo && Object.keys(orderInfo.cycleInfo)?.length > 0)
                                                    "
                                                >
                                                    <tig-button
                                                        :plain="true"
                                                        color="#333"
                                                        :custom-style="{ height: '55rpx' }"
                                                        @click="handleAfterSale(item.itemId)"
                                                    >
                                                        {{ $t("申请售后") }}
                                                    </tig-button>
                                                </template>
                                            </view>
                                        </template>
                                    </view>
                                    <!-- <template v-if="orderInfo.orderType === 8 && item.eCard.length > 0">
                                        <view class="virtualsample-box">
                                            <template v-for="subItem in item.eCard" :key="subItem">
                                                <view>
                                                    <view class="virtualsample-item">
                                                        <view class="virtualsample-item-con">
                                                            <view class="label">{{ $t("卡号") }}：</view>
                                                            <view class="value"> {{ subItem.cardNumber }}</view>
                                                        </view>
                                                        <view class="virtualsample-item-con">
                                                            <view class="label">{{ $t("卡密") }}：</view>
                                                            <view class="value"> {{ subItem.cardPwd }}</view>
                                                        </view>
                                                    </view>
                                                    <view class="virtualsample-btn">
                                                        <view>
                                                            <tig-button
                                                                :plain="true"
                                                                color="#333"
                                                                :custom-style="{ height: '55rpx' }"
                                                                @click="handleCopy(`卡号：${subItem.cardNumber}  卡密：${subItem.cardPwd}`)"
                                                            >
                                                                {{ $t("复制") }}
                                                            </tig-button>
                                                        </view>
                                                    </view>
                                                </view>
                                            </template>
                                        </view>
                                    </template> -->
                                </view>
                            </template>
                        </template>
                    </view>

                    <view v-if="orderInfo.orderType === 8" class="product-card card-space">
                        <template v-for="item in orderInfo.items" :key="item.productId">
                            <view class="item-box">
                                <template v-if="orderInfo.orderType === 8 && item.eCard.length > 0">
                                    <view class="virtualsample-box">
                                        <view class="virtualsample-header">
                                            <view class="virtualsample-title">{{ $t("券码信息") }}</view>
                                            <view class="virtualsample-count">{{ $t("(共") }}{{ item.eCard.length }}{{ $t("张)") }}</view>
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
                        <view class="money-card-item">
                            <text class="order-item-label">{{ $t("商品总价") }}：</text>
                            <format-price :show-text="false" :is-bottom="false" :price-data="orderInfo.productAmount" />
                        </view>
                        <view v-if="orderInfo.serviceFee && Number(orderInfo.serviceFee) > 0" class="money-card-item">
                            <text class="order-item-label">{{ $t("附加费用") }}：</text>
                            <format-price :show-text="false" :is-bottom="false" :price-data="orderInfo.serviceFee" />
                        </view>
                        <!-- 运费：除到店自提外，其它类型订单均需展示；运费=0 显示对应文案 -->
                        <view v-if="showShippingFeeRow" class="money-card-item">
                            <text class="order-item-label">{{ $t(shippingFeeTitleKey) }}：</text>
                            <template v-if="shippingFeeAmount > 0">
                                <format-price :show-text="false" :is-bottom="false" :price-data="shippingFeeAmount" />
                            </template>
                            <template v-else>
                                <text>{{ $t(shippingFeeFreeTextKey) }}</text>
                            </template>
                        </view>
                        <view v-if="orderInfo.cycleCardInfo && orderInfo.cycleCardInfo?.unitPrice" class="money-card-item">
                            <text class="order-item-label">{{ $t("次卡抵扣") }}：</text>
                            <view class="order-item-price">
                                <text>-</text>
                                <format-price :show-text="false" :is-bottom="false" :price-data="orderInfo.cycleCardInfo?.unitPrice" />
                            </view>
                        </view>
                        <view v-if="orderInfo.balance && orderInfo.balance > 0" class="money-card-item">
                            <text class="order-item-label">{{ $t("使用余额") }}：</text>
                            <format-price :show-text="false" :is-bottom="false" :price-data="orderInfo.balance" />
                        </view>
                        <view v-if="orderInfo.pointsAmount && orderInfo.pointsAmount > 0" class="money-card-item">
                            <text class="order-item-label">{{ `${$t("使用")}${configStore.integralName}` }}：</text>
                            <format-price :show-text="false" :is-bottom="false" :price-data="orderInfo.pointsAmount" />
                        </view>
                        <view v-if="orderInfo.couponAmount && orderInfo.couponAmount > 0" class="money-card-item">
                            <text class="order-item-label">{{ $t("优惠券") }}：</text>
                            <format-price :show-text="false" :is-bottom="false" :price-data="orderInfo.couponAmount" />
                        </view>
                        <view v-if="orderInfo.discountAmount && orderInfo.discountAmount > 0" class="money-card-item">
                            <text class="order-item-label">{{ $t("其他优惠") }}：</text>
                            <format-price :show-text="false" :is-bottom="false" :price-data="orderInfo.discountAmount" />
                        </view>
                        <view v-if="orderInfo.unpaidAmount && orderInfo.unpaidAmount > 0" class="money-card-item">
                            <text class="order-item-label">{{ $t("待支付金额") }}：</text>
                            <format-price :show-text="false" :is-bottom="false" :price-data="orderInfo.unpaidAmount" />
                        </view>
                        <view v-if="orderInfo.payStatus == 2" class="money-card-item">
                            <text class="order-item-label">{{ $t("实付款") }}：</text>
                            <view class="order-item-price">
                                <format-price :show-text="false" :is-bottom="false" :price-data="orderInfo.totalAmount" />
                            </view>
                        </view>
                    </view>
                </view>
                <!--                orderType 14为抽奖商品订单-->
                <!--                orderType 14为抽奖商品订单-->
                <template v-if="showPresaleFinalPay">
                    <tig-fixed-placeholder height="100rpx" background-color="#fff">
                        <view class="order-info-btn presale-final-pay">
                            <view class="presale-final-left">
                                <view class="presale-final-text">{{ $t("尾款") }}</view>
                                <format-price
                                    class="presale-final-amount"
                                    :font-style="{ fontWeight: 'bold', fontSize: '34rpx' }"
                                    :decimals-style="{ fontSize: '24rpx', fontWeight: 'bold' }"
                                    :currency-style="{ fontSize: '23rpx', fontWeight: 'bold' }"
                                    :price-data="productPresale?.finalPaymentAmount"
                                />
                            </view>
                            <view class="presale-final-btn-box"
                                ><tig-button class="btn presale-final-btn" @click="handlePay(orderInfo.orderId)">{{ $t("去支付") }}</tig-button></view
                            >
                        </view>
                    </tig-fixed-placeholder>
                </template>
                <template
                    v-else-if="
                        (orderInfo.availableActions.cancelOrder ||
                            orderInfo.availableActions.toPay ||
                            orderInfo.availableActions.confirmReceipt ||
                            (orderInfo.availableActions.toAftersales && !isGiftCardOrder && !isGiftCardExchangeOrder && !pickupAftersalesDisabled)) &&
                        orderInfo.orderSource !== 'pos'
                    "
                >
                    <tig-fixed-placeholder height="100rpx" background-color="#fff">
                        <view class="order-info-btn">
                            <view class="order-info-btn-box">
                                <tig-button
                                    v-if="orderInfo.availableActions.cancelOrder"
                                    class="btn"
                                    color="#333"
                                    :plain="true"
                                    @click="handleCancelOrder(orderInfo.orderId)"
                                >
                                    {{ $t("取消订单") }}
                                </tig-button>
                                <tig-button
                                    v-if="orderInfo.availableActions.toPay"
                                    class="btn"
                                    color="#333"
                                    :plain="true"
                                    @click="handlePay(orderInfo.orderId)"
                                >
                                    {{ $t("去付款") }}
                                </tig-button>
                                <tig-button
                                    v-if="orderInfo.availableActions.confirmReceipt"
                                    color="#333"
                                    class="btn"
                                    :plain="true"
                                    @click="handleConfirmReceipt(orderInfo.orderId)"
                                >
                                    {{ $t("确认已收货") }}
                                </tig-button>
                                <tig-button
                                    v-if="
                                        orderInfo.availableActions.toAftersales &&
                                        !isGiftCardOrder &&
                                        !isGiftCardExchangeOrder &&
                                        orderInfo.orderSource !== 'pos' &&
                                        !pickupAftersalesDisabled
                                    "
                                    color="#333"
                                    class="btn"
                                    :plain="true"
                                    @click="handleAfterSale(null)"
                                >
                                    {{ $t("整单售后") }}
                                </tig-button>
                                <tig-button
                                    v-if="orderInfo.grouponPromotionRecordId"
                                    class="btn"
                                    :plain="true"
                                    @click="handleToGrouponDetail(orderInfo.grouponPromotionRecordId)"
                                >
                                    {{ $t("拼团详情") }}
                                </tig-button>
                            </view>
                        </view>
                    </tig-fixed-placeholder>
                </template>
            </template>
        </view>
    </tig-layout>

</template>

<script setup lang="ts">
/**
 * 订单详情页
 * - 展示订单状态流程、基本信息、物流信息
 * - 展示商品列表，支持单品售后和整单售后
 * - 虚拟商品订单展示券码信息（支持查看/隐藏）
 * - 根据订单状态显示不同操作按钮（取消/付款/确认收货/售后）
 */
import service from "@/components/service/index.vue";
import presaleCard from "./src/presaleCard.vue";
import { onShow, onLoad } from "@dcloudio/uni-app";
import { getOrder, cancelOrder, confirmReceipt, getShippingInfo, orderBuyAgain } from "@/api/user/order";
import type { OrderInfoResponseItem, ShippingInfoTrace } from "@/types/user/order";
import { ref, computed } from "vue";
import { useI18n } from "vue-i18n";
import { useUserStore } from "@/store/user";
import { useConfigStore } from "@/store/config";
import { copy, redirect, getSkuText, hasSkuText } from "@/utils";

const configStore = useConfigStore();

const serviceRef = ref<InstanceType<typeof service> | null>(null);
const handleService = () => {
    if (serviceRef.value) {
        serviceRef.value.handleClick();
    }
};

const userStore = useUserStore();

const { t } = useI18n();

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
// 预售信息（存在时用于替换顶部步骤条、以及尾款支付入口）
const productPresale = computed(() => {
    const info: any = orderInfo.value as any;
    return info?.presaleInfo || null;
});
const showPresaleFinalPay = computed(() => {
    const p: any = productPresale.value;
    return Number(p?.saleMode) === 2 && Number(p?.presaleStatus) === 1 && !!(orderInfo.value as any)?.availableActions?.toPay;
});
const showMore = ref(false);
const isGiftCardExchangeOrder = computed(() => {
    return orderInfo.value.orderType === 12;
});
const isGiftCardOrder = computed(() => {
    return orderInfo.value.orderType === 11;
});
// 自提订单确认收货（orderStatus>=3 即已完成/已取消/已关闭）后禁止售后
const pickupAftersalesDisabled = computed(() => {
    return !!orderInfo.value.isPickup && orderInfo.value.orderStatus >= 3;
});
const cardViewStates = ref<Record<string | number, boolean>>({});


const orderFlowType = computed(() => {
    const flat: any = orderInfo.value as any;
    return Number(flat?.orderType) || 0;
});

/**
 * 订单详情页缺少结算页的 deliveryType 字段，这里做一个兼容映射：
 * - 自提：isPickup=true -> 2
 * - 同城配送：shippingTypeName 包含“同城” -> 3
 * - 默认快递配送 -> 1
 */
const orderDeliveryType = computed(() => {
    if (orderInfo.value?.isPickup) return 2;
    const name = String((orderInfo.value as any)?.shippingTypeName ?? "");
    if (name.includes(t("同城"))) return 3;
    return 1;
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

const handleCancelOrder = (id: number) => {
    uni.showModal({
        title: t("提示"),
        content: t("确认要取消该订单吗？"),
        confirmText: t("确定"),
        cancelText: t("取消"),
        success: async (res) => {
            if (res.confirm) {
                try {
                    await cancelOrder({ id });
                    uni.showToast({
                        title: t("订单已取消成功"),
                        icon: "none"
                    });
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

const handlePay = (id: number) => {
    uni.navigateTo({
        url: `/pages/order/pay?orderId=${id}`
    });
};

const handleToShop = (id: number) => {
    uni.navigateTo({
        url: `/pages/store/index?id=${id}`
    });
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
    margin-bottom: 20rpx;
}

.virtualsample-box {
    // margin-top: 20rpx;
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
            font-size: 26rpx;
            line-height: 40rpx;
            flex: 1;
            padding-right: 140rpx;

            .label {
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
    padding: 20rpx;
    border-radius: 20rpx;
    background-color: #fff;
    margin-top: 20rpx;
}
.address-text {
    width: calc(100% - 160rpx);
    text-align: end;
}
.phone-link {
    color: var(--general);
    text-decoration: none;
}
.phone-link {
    color: var(--general);
    text-decoration: none;
}
.order-item-label {
    color: #999;
    // max-width: 160rpx;
}

.order-info-steps {
    padding: 25rpx;
    background-color: #fff;
    border-radius: 0 0 20rpx 20rpx;
    overflow: hidden;
}

.order-info-content {
    padding: 10rpx 20rpx;
    .order-card {
        .order-card-item {
            align-items: center;
            display: flex;
            font-size: 24rpx;
            padding: 12rpx 0;
            .item-left {
                display: flex;
                .order-item-label {
                    flex-shrink: 0;
                }
            }
            .item-right {
                display: flex;
                flex-wrap: nowrap;
                .order-item-label {
                    flex-shrink: 0;
                }
                .order-item-value {
                    white-space: nowrap;
                }
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
        .product-card-item {
            display: flex;
            position: relative;
            margin-bottom: 20rpx;
            &:last-child {
                margin-bottom: 0;
            }

            .product-card-item-left {
                .left-img {
                    width: 160rpx;
                    height: 160rpx;
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

                .right-title {
                    font-weight: bold;
                    margin-bottom: 10rpx;
                    &.presale-box {
                        .presale-tag {
                            color: var(--general);
                            font-weight: bold;
                        }

                        .separator {
                            color: #333;
                            margin: 0 8rpx;
                        }
                    }
                }

                .right-price {
                    display: flex;
                    font-size: 28rpx;
                    padding-top: 10rpx;
                    .right-price-pricenum {
                        font-weight: bold;
                        color: var(--general);
                        padding-right: 10rpx;
                    }

                    .right-price-quantity {
                        color: #999;
                    }
                }
            }
            .item-btn {
                position: absolute;
                right: 0;
                bottom: 0;
                display: flex;
                justify-content: center;
                align-items: center;
                column-gap: 10rpx;
            }
        }
    }

    .virtualsample {
        display: flex;
        width: 100%;
        column-gap: 20rpx;
        font-size: 24rpx;
        .virtualsample-label {
            color: #999;
        }
        .virtualsample-value {
            flex: 1;
            overflow: hidden;

            img {
                width: 100%;
                height: auto;
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
    &.presale-final-pay {
        justify-content: space-between;
        align-items: center;
        .presale-final-left {
            display: flex;
            align-items: baseline;
            column-gap: 10rpx;
            .presale-final-text {
                color: #333;
                font-size: 30rpx;
            }
            .presale-final-amount {
                color: var(--general);
                font-weight: 700;
            }
        }
        .presale-final-btn-box {
            height: 100%;
            .presale-final-btn {
                width: 220rpx;
                height: 100%;
            }
        }
    }
    .order-info-btn-box {
        display: flex;
        align-items: center;
        .btn {
            margin-right: 10rpx;
            height: 100%;
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
</style>
