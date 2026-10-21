<template>
    <!-- overflow=false：tig-layout 默认给内容区加 overflow:hidden，会使其成为 sticky 的滚动容器导致吸顶失效 -->
    <!-- 标题栏放进 sticky 流内（对齐 vortmall）：下拉时头部+tabs 一起动，避免 tig-layout fixed 导航栏与 tabs 脱节 -->
    <tig-layout :overflow="false">
        <view class="order">
            <view class="order-sticky-header">
                <tig-navbar v-if="!isOverseasSkin" :title="$t('我的订单')" :fixed="false" :need-placeholder="false" />
                <view class="order-menu badgecolor" :class="{ 'tabs-equal': !shouldTabsScrollable }">
                    <up-tabs
                        v-model:current="currentIndex"
                        line-color="var(--general)"
                        :list="visibleMenuTypeList"
                        :active-style="tabTextStyle"
                        :inactive-style="tabTextStyle"
                        :scrollable="shouldTabsScrollable"
                        key-name="value"
                        @change="changeMenu"
                    />
                </view>
            </view>
            <view class="order-list">
                <template v-for="item in orderList" :key="item.orderId">
                    <view class="order-list-item">
                        <view class="order-list-item-top">
                            <view class="status-name">
                                {{ item.orderStatusName }}
                            </view>
                            <view v-if="item.cancelRemark?.trim()" class="cancel-reason"> {{ $t("取消原因") }}：{{ item.cancelRemark }} </view>
                            <view class="right-wrap" @click="handleOrederDetail(item)">
                                <view class="more">
                                    <format-price
                                        :font-style="{ fontWeight: '600', fontSize: '24rpx' }"
                                        :show-text="false"
                                        :is-bottom="false"
                                        :price-data="item.totalAmount"
                                    />
                                    <view class="icon iconfont icon-xiangyou" />
                                </view>
                            </view>
                        </view>
                        <view class="order-list-item-content">
                            <view v-if="item.items.length === 1" class="item-content-product">
                                <template v-for="subItem in item.items" :key="subItem.itemId">
                                    <navigator :url="getOrderDetailUrl(item)" hover-class="navigator-hover">
                                        <view class="item-content-product-item-one">
                                            <view class="item-content-product-img">
                                                <tig-image :src="pickSkuPic(subItem)" />
                                                <template v-if="subItem.isGift === 1">
                                                    <view class="gift-text">{{ $t("赠品") }}</view>
                                                </template>
                                            </view>
                                            <view class="item-content-product-info">
                                                <view class="product-item-title">
                                                    <text v-if="item.isPresale" class="presale-tag">
                                                        <text class="presale-tag-text">{{ $t("预售") }}</text>
                                                    </text>
                                                    <text v-if="item.joinStatus" class="groupon-tag">
                                                        <text class="groupon-tag-text">{{ $t("拼团") }}</text>
                                                    </text>
                                                    {{ subItem.productName }}
                                                </view>
                                                <view class="product-item-detail">
                                                    <view class="sku-wrap" :class="{ nodata: !hasSkuText(subItem.skuData) }">
                                                        <text v-if="hasSkuText(subItem.skuData)" class="sku-item">{{ getSkuText(subItem.skuData) }}</text>
                                                    </view>
                                                    <view class="product-item-quantity">× {{ subItem.quantity }}</view>
                                                </view>
                                                <template v-if="subItem.extraSkuData && subItem.extraSkuData.length">
                                                    <view class="extraskudata-box">
                                                        <view class="extraskudata">
                                                            <template v-for="attr in subItem.extraSkuData" :key="attr">
                                                                <view class="extraskudata-item">
                                                                    <view class="title">{{ attr.attrName }}</view>
                                                                    <view class="line-box">
                                                                        <view class="line" />
                                                                    </view>
                                                                    <view class="content">
                                                                        <text class="content-text">{{ attr.attrValue }}</text>
                                                                        <format-price :show-text="false" class="price" :price-data="attr.attrPrice" />
                                                                        <view class="num">x{{ subItem.quantity }}</view>
                                                                    </view>
                                                                </view>
                                                            </template>
                                                        </view>
                                                    </view>
                                                </template>
                                            </view>
                                        </view>
                                    </navigator>
                                </template>
                            </view>
                            <view v-if="item.items.length > 1" class="item-content-product">
                                <scroll-view scroll-x="true" enable-flex="true" style="display: flex">
                                    <template v-for="subItem in item.items" :key="subItem.itemId">
                                        <navigator class="item-content-product-nav" :url="getOrderDetailUrl(item)" hover-class="navigator-hover">
                                            <view class="item-content-product-item">
                                                <view class="item-content-product-img">
                                                    <tig-image :src="subItem.picThumb" />
                                                    <template v-if="subItem.isGift === 1">
                                                        <view class="gift-text">{{ $t("赠品") }}</view>
                                                    </template>
                                                </view>
                                            </view>
                                        </navigator>
                                    </template>
                                </scroll-view>
                            </view>

                            <!-- <view class="item-content-pay">
                                <view class="item-content-pay-box">
                                    <view class="pay-item pay-type">
                                        {{ item.payTypeId === 1 ? $t("在线支付") : item.payTypeId === 2 ? $t("货到付款") : t("线下支付") }}</view
                                    >
                                    <view class="pay-item total-money"
                                        >{{ $t("总额") }}：<format-price :show-text="false" :is-bottom="false" :price-data="item.totalAmount"
                                    /></view>
                                    <view v-if="item.orderStatus === 0 && Number(item.unpaidAmount) > 0" class="pay-item">
                                        {{ $t("应付") }}：<format-price :show-text="false" :is-bottom="false" :price-data="item.unpaidAmount" />
                                    </view>
                                </view>
                            </view> -->

                            <template v-if="item.payStatus !== 0 && item.orderReservation && !(item.isCycleOrder === 1 && !item.parentOrderId)">
                                <view class="item-content-reservation">
                                    <text class="iconfont-h5 icon-riqi" /> {{ item.orderReservation.reservationDate }} {{ item.orderReservation.startTime }}～{{
                                        item.orderReservation.endTime
                                    }}
                                </view>
                            </template>
                        </view>
                        <view class="order-list-item-btn">
                            <view class="autodeliverydays">
                                <template v-if="item.autoDeliveryDays && !item.isCardOrder">
                                    <view class="autodeliverydays-text line1">{{ $t("订单将在{0}天后自动收货", [item.autoDeliveryDays]) }}</view>
                                </template>
                            </view>
                            <view class="item-btn-box">
                                <tig-button2
                                    v-if="item.availableActions.cancelOrder"
                                    class="btn-margin"
                                    style="border-color: #505259"
                                    @click="handleCancelOrder(item.orderId)"
                                >
                                    {{ $t("取消订单") }}
                                </tig-button2>
                                <tig-button2
                                    v-if="item.availableActions.delOrder"
                                    class="btn-margin"
                                    style="border-color: #505259"
                                    @click="handleDelOrder(item.orderId)"
                                >
                                    {{ $t("删除订单") }}
                                </tig-button2>
                                <tig-button2
                                    v-if="item.availableActions.toComment && commentEnabled"
                                    class="btn-margin"
                                    style="border-color: #505259"
                                    @click="handleEvaluate(item.orderId)"
                                >
                                    {{ $t("去评价") }}
                                </tig-button2>
                                <!--周期购、预约-->
                                <tig-button2
                                    v-if="item.isCycleOrder === 1 || isSubCardMainOrder(item) || [9, 10].includes(item.orderType)"
                                    class="btn-margin"
                                    theme
                                    @click="handleLink(item?.items?.[0]?.productId)"
                                >
                                    {{ [9, 10].includes(item.orderType) ? $t("再次预约") : $t("再次购买") }}
                                </tig-button2>
                                <!-- 次卡子订单不允许再次购买 -->
                                <tig-button2
                                    v-else-if="(item.availableActions.rebuy || item.orderType === 2) && !isSubCard(item)"
                                    class="btn-margin"
                                    theme
                                    @click="handleBuyAgain(item)"
                                >
                                    {{ $t("再次购买") }}
                                </tig-button2>
                                <tig-button2
                                    v-if="item.availableActions.toPay"
                                    class="btn-margin"
                                    theme
                                    :disabled="isFinalPaymentBlocked(item)"
                                    @click="handlePay(item.orderId, item.payTypeId)"
                                >
                                    {{ isFinalPaymentBlocked(item) ? $t("尾款未到支付时间") : $t("去付款") }}
                                </tig-button2>
                                <tig-button2
                                    v-if="item.availableActions.confirmReceipt && !item.isPickup && !item.isCardOrder"
                                    class="btn-margin"
                                    theme
                                    @click="handleConfirmReceipt(item.orderId)"
                                >
                                    {{ $t("确认收货") }}
                                </tig-button2>
                            </view>
                        </view>
                    </view>
                </template>
            </view>
            <loading-box v-model="isLoadMore" :page="filterParams.page" :length="orderList.length" />

            <view v-show="!isLoading && total === 0" class="order-empty">
                <emptyBox center-content height="100%" mode="list" background="#f5f5f5" :text="$t('暂无订单')" />
            </view>
        </view>
        <template v-if="isLoading">
            <tig-loadingpage :model-value="isLoading" bg-color="'#f4f4f4'" />
        </template>
        <CancelOrderReasonPopup ref="cancelOrderReasonPopupRef" @success="refreshAfterCancel" />
        <specification
            ref="buyAgainSpecificationRef"
            v-model="buyAgainProductId"
            v-model:sku-id="buyAgainSkuId"
            :groupon-enable="false"
            @load-end="openBuyAgainSpecification"
            @close="resetBuyAgainPopup"
        />
    </tig-layout>
</template>

<script setup lang="ts">
import { pickSkuPic } from "@/utils/format";
/**
 * 订单列表页
 * - 支持按状态筛选：全部/待支付/待发货/待收货/待评价
 * - 支持订单操作：取消/删除/付款/再次购买/评价/确认收货
 * - 支持查看提货凭证（到店自提订单）
 * - 根据订单类型跳转不同详情页（普通/自提/预约）
 */
import emptyBox from "@/components/empty-box/empty-box.vue";
import specification from "@/components/product/specification.vue";
import { computed, reactive, ref } from "vue";
import { getOrderList, delOrder, orderBuyAgain, confirmReceipt } from "@/api/user/order";
import type { OrderListFilterParams, OrderListFilterResult } from "@/types/user/order";
import { onLoad, onShow, onUnload } from "@dcloudio/uni-app";
import { redirect, getSkuText, hasSkuText, isO2o } from "@/utils";
import { useList } from "@/hooks";
import { useI18n } from "vue-i18n";
import { getJoiningActivityDetail } from "@/api/groupon/groupon";
import dayjs from "dayjs";
import { useConfigStore } from "@/store/config";
import CancelOrderReasonPopup from "@/components/order/CancelOrderReasonPopup.vue";

const { t, locale } = useI18n();
const configStore = useConfigStore();
const commentEnabled = computed(() => configStore.productCommentEnabled === 1);
const buyAgainProductId = ref(0);
const buyAgainSkuId = ref(0);
const buyAgainSpecificationRef = ref<InstanceType<typeof specification>>();
const resetBuyAgainPopup = () => {
    buyAgainProductId.value = 0;
    buyAgainSkuId.value = 0;
};
const openBuyAgainSpecification = () => buyAgainSpecificationRef.value?.handleShowPopup();
const openSingleProductBuyAgain = (item: OrderListFilterResult) => {
    buyAgainSkuId.value = Number(item.items[0]?.skuId || 0);
    buyAgainProductId.value = Number(item.items[0]?.productId || 0);
};
const isNormalSingleProductOrder = (item: OrderListFilterResult) => item.items.length === 1 && !item.isPresale && Number(item.items[0]?.productType) === 1;
let isOverseasSkin = false;

interface MenuType {
    type: string;
    value: string;
    badge: any;
}
const menuTypeList = reactive<MenuType[]>([
    { type: "all", value: t("全部订单"), badge: { value: 0 } },
    { type: "awaitPay", value: t("待支付"), badge: { value: 0 } },
    { type: "awaitShipping", value: t("待发货"), badge: { value: 0 } },
    { type: "awaitReceived", value: t("待收货"), badge: { value: 0 } },
    { type: "awaitComment", value: t("待评价"), badge: { value: 0 } }
]);
const visibleMenuTypeList = computed(() => menuTypeList.filter((item) => item.type !== "awaitComment" || commentEnabled.value));
const currentType = ref<string>("all");
const filterParams = reactive<OrderListFilterParams>({
    page: 1,
    size: 10,
    orderStatus: -1,
    commentStatus: -1,
    sortField: "addTime",
    sortOrder: "desc"
});

const currentIndex = ref(0);
const isFirstShow = ref(true);

const windowWidth = uni.getSystemInfoSync().windowWidth || 375;
// 非中文语种标签更长，需要开启横向滚动避免挤压
const isNonChinese = computed(
    () =>
        !String(locale.value || "zh")
            .toLowerCase()
            .includes("zh")
);
const shouldTabsScrollable = computed(() => isNonChinese.value || windowWidth <= 390);
const tabTextStyle = computed(() => {
    const padding = windowWidth <= 375 ? "0 4rpx" : windowWidth <= 390 ? "0 8rpx" : "0 12rpx";
    return { padding };
});

// 次卡子订单
const isSubCard = (item: OrderListFilterResult) => {
    let result = false;
    return result;
};
// 次卡主订单
const isSubCardMainOrder = (item: OrderListFilterResult) => {
    let result = false;
    return result;
};

/** 定金预售待付尾款阶段（saleMode=2 且 presaleStatus=1）但后端未放行支付 */
const isFinalPaymentBlocked = (item: OrderListFilterResult): boolean => {
    const presale = item.presaleInfo;
    return presale?.saleMode === 2 && presale?.presaleStatus === 1 && presale?.canPayFinalPayment === false;
};

/**
 * 切换订单状态筛选
 * - 待评价状态使用 commentStatus 筛选
 * - 其他状态使用 orderStatus 筛选
 */
const changeMenu = (data: any) => {
    if (currentType.value === data.type) return;
    currentType.value = data.type;
    filterParams.page = 1;
    orderList.value = [];
    // 待评价状态需要单独用 commentStatus 字段筛选
    if (data.type === "awaitComment") {
        filterParams.commentStatus = 0;
        filterParams.orderStatus = -1;
    } else {
        filterParams.orderStatus = formatOrderStatus(data.type);
        filterParams.commentStatus = -1;
    }
    getList();
};

const {
    getList,
    data: orderList,
    isLoadMore,
    isLoading,
    total
} = useList<OrderListFilterResult>(getOrderList, {
    params: filterParams,
    path: {
        dataKey: "records"
    }
});

const cancelOrderReasonPopupRef = ref<{ open: (id: number) => void }>();
const handleCancelOrder = (id: number) => {
    cancelOrderReasonPopupRef.value?.open(id);
};
const refreshAfterCancel = () => {
    filterParams.page = 1;
    orderList.value = [];
    getList();
};

const handleDelOrder = async (id: number) => {
    uni.showModal({
        title: t("提示"),
        content: t("确认要删除该订单吗？"),
        confirmText: t("确定"),
        cancelText: t("取消"),
        success: async (res) => {
            if (res.confirm) {
                try {
                    await delOrder({ id });
                    uni.showToast({
                        title: t("订单已删除成功"),
                        icon: "none"
                    });
                    filterParams.page = 1;
                    orderList.value = [];
                    getList();
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
 * @description 再次购买，将订单商品重新加入购物车
 * 拼团订单特殊处理：如果活动未结束则跳转商品详情页重新参团，否则加入普通购物车
 * @param item 订单数据
 */
const handleBuyAgain = async (item: OrderListFilterResult) => {
    try {
        // 如果是拼团订单
        if (item.grouponPromotionRecordId) {
            // 获取可用的拼团活动
            const result = await getJoiningActivityDetail({
                productId: item.items[0].productId
            });
            // 拼团活动没结束
            if (result && result.endTime > dayjs().format("YYYY-MM-DD HH:mm:ss")) {
                let url = "/pages/product/index?id=" + result.productId;
                // 如果有skuId,就传skuId过去
                if (item.items[0].skuId) {
                    url = url + "&skuId=" + item.items[0].skuId;
                }
                redirect({ url });
            } else {
                await orderBuyAgain({ id: item.orderId });
                redirect({ url: "/pages/cart/index" });
            }
        }
        else {
            if (isNormalSingleProductOrder(item)) {
                openSingleProductBuyAgain(item);
            } else {
                await orderBuyAgain({ id: item.orderId });
                redirect({ url: "/pages/cart/index" });
            }
        }
    } catch (error: any) {
        uni.showToast({
            title: error.message,
            icon: "none"
        });
    }
};

const handleLink = (id: number) => {
    uni.navigateTo({
        url: `/pages/product/index?id=${id}`
    });
};

const handlePay = (id: number, payTypeId: number) => {
    uni.navigateTo({
        url: payTypeId === 3 ? `/pages/order/pay?orderId=${id}` : `/pages/order/cashier?orderId=${id}&from=orderList`
    });
};

/**
 * 解析订单详情页地址
 * 根据订单类型跳转不同页面：
 * - 周期购次卡 -> 门店订单详情页
 * - productType=8 电子礼品卡 -> 礼品卡订单详情页
 * - orderType=9 预约到店服务 -> 预约详情页
 * - orderType=10 预约上门服务 -> 预约详情页
 * - orderType=2 且 isPickup 无提货信息 -> 预约详情页
 * - isPickup 有提货信息 -> 自提订单详情页
 * - 其他 -> 普通订单详情页
 */
const resolveOrderDetailUrl = (item: OrderListFilterResult): string => {
    if (item.items[0].productType === 8) {
        return `/pages/giftCard/order/info?id=${item.orderId}`;
    }

    let url = `/pages/user/order/info?id=${item.orderId}`;


    return url;
};

/** 商品区 navigator 用的详情地址 */
const getOrderDetailUrl = (item: OrderListFilterResult) => resolveOrderDetailUrl(item);

const handleOrederDetail = (item: OrderListFilterResult) => {
    redirect({
        url: resolveOrderDetailUrl(item)
    });
};

const handleEvaluate = (id: number) => {
    redirect({
        url: `/pages/user/comment/info?id=${id}`
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
                    uni.redirectTo({
                        url: commentEnabled.value ? "/pages/user/order/list?type=awaitComment" : "/pages/user/order/list"
                    });
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
 * 将状态类型转换为接口需要的 orderStatus 值
 * -1=全部, 0=待付款, 1=待发货, 2=待收货
 */
const formatOrderStatus = (status: string) => {
    switch (status) {
        case "all":
            return -1;
        case "awaitPay":
            return 0;
        case "awaitShipping":
            return 1;
        case "awaitReceived":
            return 2;
        default:
            return -1;
    }
};

onLoad(async (options) => {
    // 等待配置初始化后再计算可见标签
    await configStore.whenConfigReady();
    if (options && options.type) {
        // 兼容下划线形式的历史链接；不可见标签回到全部订单
        const requestedType = options.type === "await_comment" ? "awaitComment" : options.type;
        const type = requestedType === "awaitComment" && !commentEnabled.value ? "all" : requestedType;
        const menuIndex = visibleMenuTypeList.value.findIndex((item) => item.type === type);
        currentIndex.value = menuIndex >= 0 ? menuIndex : 0;

        const currentMenuType = menuIndex >= 0 ? type : "all";
        if (currentMenuType === "awaitComment") {
            filterParams.commentStatus = 0;
            filterParams.orderStatus = -1;
        } else {
            filterParams.commentStatus = -1;
            filterParams.orderStatus = formatOrderStatus(currentMenuType);
        }
        currentType.value = currentMenuType;
    }
    getList();
    // 监听评论完成事件
    uni.$on("commentFinished", handleCommentFinished);
});

onShow(() => {
    if (isFirstShow.value) {
        isFirstShow.value = false;
        return;
    }
    filterParams.page = 1;
    orderList.value = [];
    getList();
});

// 处理评论完成事件
const handleCommentFinished = () => {
    getList();
};

// 页面卸载时移除事件监听
onUnload(() => {
    uni.$off("commentFinished", handleCommentFinished);
});
</script>

<style lang="scss" scoped>
.order {
    min-height: calc(100vh - var(--safe-bottom, 0px));
    display: flex;
    flex-direction: column;
}

.order-empty {
    flex: 1;
    min-height: 300rpx;
}

.order-sticky-header {
    position: sticky;
    position: -webkit-sticky;
    top: 0;
    z-index: 600;
    background-color: #fff;
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

.attr-list {
    padding-bottom: 10rpx;

    .attr-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        height: 45rpx;
        line-height: 45rpx;

        .sub-title {
            padding-right: 30rpx;
        }

        .item-content {
            flex: 1;
            display: flex;
            align-items: center;
            justify-content: flex-end;
            overflow: hidden;

            .content-text {
                flex: 1;
                text-align: end;
            }

            .right {
                display: flex;
                padding-left: 10rpx;
                color: #242424;

                .num {
                    padding-left: 10rpx;
                }
            }
        }
    }
}

.badgecolor {
    :deep(.u-badge--error) {
        background-color: var(--general);
    }
}

.order-menu {
    background-color: #fff;
    margin-bottom: 20rpx;
    position: static;
    z-index: 88;
    border-top: 1px solid #f0f1f2;

    .order-menu-item {
        flex: 1;
        display: flex;
        align-items: center;
        justify-content: center;
        height: 88rpx;
        position: relative;

        .order-menu-item-text {
            padding: 0 10rpx 0 0;
        }

        &.active {
            color: #333;
            font-weight: bold;

            &:after {
                content: "";
                position: absolute;
                bottom: 0;
                height: 5rpx;
                width: 60rpx;
                background-color: var(--general);
                border-radius: 10rpx;
            }
        }
    }
}

.order-list {
    padding-bottom: 10rpx;

    .order-list-item {
        background-color: #fff;
        margin-bottom: 20rpx;
        padding: 30rpx;
        padding-top: 0;
        font-size: 24rpx;

        &:last-child {
            margin-bottom: 0;
        }

        .order-list-item-top {
            display: flex;
            align-items: center;
            justify-content: space-between;
            height: 78rpx;
            border-bottom: 1px solid #f0f1f2;
            margin-bottom: 10rpx;

            .status-name {
                font-size: 25rpx;
                color: #1a1a1a;
                font-weight: 600;
                white-space: nowrap;
                max-width: 45%;
                overflow: hidden;
                text-overflow: ellipsis;
            }

            .right-wrap {
                display: flex;
                align-items: center;
                text-transform: none;
                font-size: 25rpx;

                .more {
                    display: flex;
                    align-items: center;

                    .icon {
                        margin-left: 15rpx;
                        font-weight: 600;
                        font-size: 20rpx;
                        color: #888b94;
                    }
                }
            }
        }

        .order-list-item-content {
            .item-content-product-nav {
                margin-right: 15rpx;

                &:last-child {
                    margin-right: 0;
                }
            }

            .item-content-product-item {
                padding-bottom: 15rpx;
                display: flex;

                &:first-child {
                    padding-top: 15rpx;
                }

                .item-content-product-img {
                    width: 140rpx;
                    height: 140rpx;
                    overflow: hidden;
                    position: relative;

                    .gift-text {
                        position: absolute;
                        top: 0;
                        left: 0;
                        font-size: 23rpx;
                        padding: 3rpx 8rpx;
                        background-color: var(--main-bg);
                        color: var(--main-text);
                        border-radius: 0 var(--tag-round-border-radius, 999px) var(--tag-round-border-radius, 999px) 0;
                    }
                }
            }

            .item-content-product {
                .item-content-product-item-one {
                    padding-top: 15rpx;
                    display: flex;

                    .item-content-product-img {
                        width: 148rpx;
                        height: 148rpx;
                        overflow: hidden;
                        position: relative;

                        .gift-text {
                            position: absolute;
                            top: 0;
                            left: 0;
                            font-size: 23rpx;
                            padding: 3rpx 8rpx;
                            background-color: var(--main-bg);
                            color: var(--main-text);
                            border-radius: 0 var(--tag-round-border-radius, 999px) var(--tag-round-border-radius, 999px) 0;
                        }
                    }

                    .item-content-product-info {
                        display: flex;
                        flex-direction: column;
                        justify-content: center;
                        flex: 1;
                        padding-left: 16rpx;
                        overflow: hidden;

                        .product-item-title {
                            font-size: 28rpx;
                            line-height: 35rpx;
                            max-height: 70rpx;
                            overflow: hidden;
                            text-overflow: ellipsis;
                            display: -webkit-box;
                            -webkit-line-clamp: 2;
                            line-clamp: 2;
                            -webkit-box-orient: vertical;
                            word-break: break-word;
                        }

                        .product-item-detail {
                            display: flex;
                            align-items: center;
                            margin-top: 15rpx;

                            .sku-wrap {
                                white-space: nowrap;
                                overflow: hidden;
                                text-overflow: ellipsis;
                                display: flex;
                                align-items: baseline;

                                .sku-item {
                                    font-size: 26rpx;
                                    color: #888b94;
                                    white-space: nowrap;
                                    overflow: hidden;
                                    text-overflow: ellipsis;
                                    line-height: 30rpx;
                                }
                            }

                            .product-item-quantity {
                                flex-shrink: 0;
                                font-size: 26rpx;
                                color: #999;
                                padding-left: 15rpx;
                            }
                        }
                    }
                }
            }

            .item-content-reservation {
                display: flex;
                align-items: center;
                column-gap: 6rpx;
                padding-top: 16rpx;
                color: #9e9e9e;

                .icon-riqi {
                    padding-top: 2rpx;
                    font-size: 16rpx;
                }
            }

            :deep(.uni-scroll-view-content) {
                display: flex;
            }
        }

        .order-list-item-btn {
            display: flex;
            justify-content: flex-end;
            padding-top: 24rpx;

            .autodeliverydays {
                flex: 1;
                display: flex;
                align-items: center;
                overflow: hidden;

                .autodeliverydays-text {
                    width: 100%;
                    color: var(--main-bg);
                }
            }

            .item-btn-box {
                display: flex;

                .btn-margin {
                    height: 60rpx;
                    margin-right: 16rpx;

                    &:last-child {
                        margin-right: 0;
                    }
                }
            }
        }
    }
}

.groupon-tag-text {
    display: inline;
    background: #ee7700;
    color: #fff;
    padding: 4rpx 12rpx;
    border-radius: 4rpx;
    font-size: 24rpx;
}

.presale-tag-text {
    display: inline;
    background-color: #e47200;
    color: #fff;
    padding: 4rpx 12rpx;
    border-radius: 4rpx;
    font-size: 24rpx;
    margin-right: 6rpx;
}

// 非滚动模式下 5 个状态页签等分整行；滚动模式（英文/窄屏）交回 up-tabs 自身宽度
.tabs-equal {
    :deep(.u-tabs__wrapper__nav__item) {
        flex: 1 !important;
        padding: 0 !important;
    }
}
</style>
