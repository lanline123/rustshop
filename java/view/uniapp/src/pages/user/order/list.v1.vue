<template>
    <tig-layout :title="isOverseasSkin ? '' : $t('我的订单')" background="#f5f5f5">
        <view class="order">
            <view class="order-menu badgecolor">
                <up-tabs
                    v-model:current="currentIndex"
                    line-color="var(--general)"
                    :list="menuTypeList"
                    :active-style="{ padding: '0 10rpx', color: '#1A1A1A', fontWeight: 800 }"
                    :inactive-style="{ padding: '0 10rpx' }"
                    :scrollable="false"
                    key-name="value"
                    @change="changeMenu"
                />
            </view>
            <view class="placeholder" />
            <view class="order-list">
                <template v-for="item in orderList" :key="item.orderId">
                    <view class="order-list-item" @click="handleOrederDetail(item)">
                        <view class="order-list-item-header">
                            <text class="order-special-text">{{ $t("订单编号") }}：{{ item.orderSn }} </text>
                            <text class="copy-text" @click.stop="handleCopy(item.orderSn)"> {{ $t("复制") }} </text>
                            <text class="order-status">{{ item.orderStatusName }}</text>
                        </view>
                        <view class="order-list-item-content">
                            <view v-if="false" class="item-content-text">
                                <view> {{ $t("收件人") }}：{{ item.consignee }} </view>
                                <view class="special-color">{{ item.addTime }}</view>
                            </view>
                            <view class="item-content-product">
                                <template v-for="subItem in item.items" :key="subItem.itemId">
                                    <navigator>
                                        <view class="item-content-product-item">
                                            <view class="item-content-product-img">
                                                <tig-image :src="subItem.picThumb" />
                                                <template v-if="subItem.isGift === 1">
                                                    <view class="gift-text">{{ $t("赠品") }}</view>
                                                </template>
                                            </view>
                                            <view class="item-content-product-info">
                                                <view class="product-item-title">
                                                    <view v-if="item.joinStatus" class="groupon-tag">
                                                        <text class="groupon-tag-text">{{ $t("拼") }}</text>
                                                    </view>
                                                    <template v-if="item.isPresale">
                                                        <text class="presale-tag">{{ $t("预售") }}</text>
                                                        <text class="separator">|</text>
                                                    </template>
                                                    <text>{{ subItem.productName }}</text>
                                                </view>

                                                <view class="sku-card" :class="{ nodata: !hasSkuText(subItem.skuData) }">
                                                    <view v-if="hasSkuText(subItem.skuData)" class="sku-item line1">{{ getSkuText(subItem.skuData) }}</view>
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
                                                                        <text class="line1 content-text">{{ attr.attrValue }}</text>
                                                                        <format-price :show-text="false" class="price" :price-data="attr.attrPrice" />
                                                                        <view class="num">x{{ subItem.quantity }}</view>
                                                                    </view>
                                                                </view>
                                                            </template>
                                                        </view>
                                                    </view>
                                                </template>
                                                <view class="product-item-price">
                                                    <format-price
                                                        :show-text="false"
                                                        class="product-item-pricenum"
                                                        :decimals-style="{
                                                            fontSize: '25rpx'
                                                        }"
                                                        :currency-style="{
                                                            fontSize: '23rpx'
                                                        }"
                                                        :price-data="subItem.price"
                                                    />
                                                    <view class="product-item-quantity">x {{ subItem.quantity }}</view>
                                                </view>
                                            </view>
                                        </view>
                                    </navigator>
                                </template>
                            </view>

                            <view class="item-content-pay">
                                <template v-if="item.payStatus !== 0 && item.orderReservation && !(item.isCycleOrder === 1 && !item.parentOrderId)">
                                    <view class="reservation">
                                        <text class="iconfont-h5 icon-riqi" /> {{ item.orderReservation.reservationDate }}
                                        {{ item.orderReservation.startTime }}～{{ item.orderReservation.endTime }}
                                    </view>
                                </template>

                                <view class="item-content-pay-box">
                                    <view class="pay-item pay-type">
                                        {{ item.payTypeId === 1 ? $t("在线支付") : item.payTypeId === 2 ? $t("货到付款") : t("线下支付") }}
                                    </view>
                                    <view class="pay-item total-money"
                                        >{{ $t("总额") }}：<format-price :show-text="false" :is-bottom="false" :price-data="item.totalAmount"
                                    /></view>
                                    <view v-if="item.orderStatus === 0 && Number(item.unpaidAmount) > 0" class="pay-item">
                                        {{ $t("应付") }}：<format-price :show-text="false" :is-bottom="false" :price-data="item.unpaidAmount" />
                                    </view>
                                </view>
                            </view>
                        </view>
                        <view class="order-list-item-btn">
                            <view class="autodeliverydays">
                                <template v-if="item.autoDeliveryDays && !item.isCardOrder">
                                    <view class="autodeliverydays-text line1">{{ $t("订单将在") }}{{ item.autoDeliveryDays }}{{ $t("天后自动收货") }} </view>
                                </template>
                            </view>
                            <view class="item-btn-box" @click.stop>
                                <tig-button
                                    class="btn-margin"
                                    :custom-style="{ height: '55rpx' }"
                                    :plain="true"
                                    :plain-main-color="true"
                                    @click.stop.prevent="handleOrederDetail(item)"
                                >
                                    {{ $t("订单详情") }}
                                </tig-button>
                                <!--                                pickupDetail：自提信息-->
                                <tig-button
                                    v-if="
                                        item.isPickup &&
                                        item.orderStatus === 2 &&
                                        item.orderType !== 10 &&
                                        item.orderType !== 9 &&
                                        !item.afterSalesIng &&
                                        item.pickupDetail
                                    "
                                    class="btn-margin"
                                    color="#333"
                                    :custom-style="{ height: '55rpx' }"
                                    :plain="true"
                                    @click.stop="redirect({ url: `/pages/store/order/voucher?id=${item.orderId}` })"
                                >
                                    {{ $t("提货凭证") }}
                                </tig-button>
                                <tig-button
                                    v-if="item.availableActions.toPay"
                                    class="btn-margin"
                                    color="#333"
                                    :custom-style="{ height: '55rpx' }"
                                    :plain="true"
                                    @click.stop="handlePay(item.orderId)"
                                >
                                    {{ $t("去付款") }}
                                </tig-button>
                                <tig-button
                                    v-if="item.availableActions.cancelOrder"
                                    color="#333"
                                    class="btn-margin"
                                    :custom-style="{ height: '55rpx' }"
                                    :plain="true"
                                    @click.stop="handleCancelOrder(item.orderId)"
                                >
                                    {{ $t("取消订单") }}
                                </tig-button>
                                <tig-button
                                    v-if="item.availableActions.delOrder"
                                    color="#333"
                                    class="btn-margin"
                                    :custom-style="{ height: '55rpx' }"
                                    :plain="true"
                                    @click.stop="handleDelOrder(item.orderId)"
                                >
                                    {{ $t("删除订单") }}
                                </tig-button>
                                <!--周期购、预约-->
                                <tig-button
                                    v-if="item.isCycleOrder === 1 || isSubCardMainOrder(item) || [9, 10].includes(item.orderType)"
                                    color="#333"
                                    class="btn-margin"
                                    :custom-style="{ height: '55rpx' }"
                                    :plain="true"
                                    @click.stop="handleLink(item?.items?.[0]?.productId)"
                                >
                                    {{ [9, 10].includes(item.orderType) ? $t("再次预约") : $t("再次购买") }}
                                </tig-button>
                                <!-- 次卡子订单不允许再次购买 -->
                                <tig-button
                                    v-else-if="(item.availableActions.rebuy || item.orderType === 2) && !isSubCard(item)"
                                    color="#333"
                                    class="btn-margin"
                                    :custom-style="{ height: '55rpx' }"
                                    :plain="true"
                                    @click.stop="handleBuyAgain(item)"
                                >
                                    {{ $t("再次购买") }}
                                </tig-button>
                                <tig-button
                                    v-if="item.availableActions.toComment"
                                    color="#333"
                                    class="btn-margin"
                                    :custom-style="{ height: '55rpx' }"
                                    :plain="true"
                                    @click.stop="handleEvaluate(item.orderId)"
                                >
                                    {{ $t("去评价") }}
                                </tig-button>
                                <tig-button
                                    v-if="item.availableActions.confirmReceipt && !item.isPickup && !item.isCardOrder"
                                    color="#333"
                                    class="btn-margin"
                                    :custom-style="{ height: '55rpx' }"
                                    :plain="true"
                                    @click.stop="handleConfirmReceipt(item.orderId)"
                                >
                                    {{ $t("确认收货") }}
                                </tig-button>
                            </view>
                        </view>
                    </view>
                </template>
            </view>
            <loading-box v-model="isLoadMore" :page="filterParams.page" :length="orderList.length" />

            <view v-show="!isLoading && total === 0">
                <emptyBox mode="list" background="#f5f5f5" :text="$t('暂无订单')" />
            </view>
        </view>
        <template v-if="isLoading">
            <tig-loadingpage :model-value="isLoading" bg-color="'#f4f4f4'" />
        </template>
    </tig-layout>
</template>

<script setup lang="ts">
/**
 * 订单列表页
 * - 支持按状态筛选：全部/待支付/待发货/待收货/待评价
 * - 支持订单操作：取消/删除/付款/再次购买/评价/确认收货
 * - 支持查看提货凭证（到店自提订单）
 * - 根据订单类型跳转不同详情页（普通/自提/预约）
 */
import emptyBox from "@/components/empty-box/empty-box.vue";
import { reactive, ref } from "vue";
import { getOrderList, cancelOrder, delOrder, orderBuyAgain, confirmReceipt } from "@/api/user/order";
import type { OrderListFilterParams, OrderListFilterResult } from "@/types/user/order";
import { onLoad, onShow, onUnload } from "@dcloudio/uni-app";
import { redirect, copy, getSkuText, hasSkuText } from "@/utils";
import { useList } from "@/hooks";
import { useI18n } from "vue-i18n";
import { getJoiningActivityDetail } from "@/api/groupon/groupon";
import dayjs from "dayjs";

const { t } = useI18n();

interface MenuType {
    type: string;
    value: string;
    badge: any;
}
const menuTypeList = reactive<MenuType[]>([
    { type: "all", value: t("全部"), badge: { value: 0 } },
    { type: "awaitPay", value: t("待支付"), badge: { value: 0 } },
    { type: "awaitShipping", value: t("待发货"), badge: { value: 0 } },
    { type: "awaitReceived", value: t("待收货"), badge: { value: 0 } },
    { type: "awaitComment", value: t("待评价"), badge: { value: 0 } }
]);
const currentType = ref<string>("all");
const filterParams = reactive<OrderListFilterParams>({
    page: 1,
    size: 10,
    orderStatus: -1,
    commentStatus: -1
});

const currentIndex = ref(0);
const isFirstShow = ref(true);


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

const handleCancelOrder = async (id: number) => {
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
            await orderBuyAgain({ id: item.orderId });
            redirect({ url: "/pages/cart/index" });
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

const handlePay = (id: number) => {
    redirect({
        url: `/pages/order/pay?orderId=${id}`
    });
};

/**
 * 跳转订单详情页
 * 根据订单类型跳转不同页面：
 * - orderType=9 预约到店服务 -> 预约详情页
 * - orderType=10 预约上门服务 -> 预约详情页
 * - orderType=2 且 isPickup 无提货信息 -> 预约详情页
 * - isPickup 有提货信息 -> 自提订单详情页
 * - 其他 -> 普通订单详情页
 */
const handleOrederDetail = (item: OrderListFilterResult) => {
    let url = `/pages/user/order/info?id=${item.orderId}`;
    if (item.items[0].productType === 8) {
        uni.navigateTo({
            url: `/pages/giftCard/order/info?id=${item.orderId}`
        });
        return;
    } else if (item.orderType === 9 || item.orderType === 10 || (item.orderType === 2 && item.isPickup && !item.pickupDetail)) {
        url = `/pages/store/order/reserveInfo?id=${item.orderId}`;
    } else if (item.isPickup) {
        url = `/pages/store/order/info?id=${item.orderId}`;
    }


    redirect({
        url
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
                        url: "/pages/user/order/list?type=awaitComment"
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

const handleToShop = (id: number) => {
    uni.navigateTo({
        url: `/pages/store/index?id=${id}`
    });
};

const handleCopy = (data: string) => {
    copy(data, () => {
        uni.showToast({
            title: t("复制成功"),
            icon: "none"
        });
    });
};

onLoad((options) => {
    if (options && options.type) {
        currentIndex.value = menuTypeList.findIndex((item) => item.type === options.type);

        if (options.type === "awaitComment") {
            filterParams.commentStatus = 0;
        } else {
            filterParams.orderStatus = formatOrderStatus(options.type);
        }
        currentType.value = options.type;
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
.order-menu {
    background-color: #f4f4f4 !important;
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

.placeholder {
    height: 88rpx;
}

.badgecolor {
    :deep(.u-badge--error) {
        background-color: var(--general);
    }
}

.order-menu {
    background-color: #fff;
    margin-bottom: 30rpx;
    position: fixed;
    width: 100%;
    z-index: 88;

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
    padding: 10rpx 20rpx;

    .order-list-item {
        background-color: #fff;
        border-radius: 10rpx;
        margin-bottom: 20rpx;
        padding: 20rpx;
        font-size: 24rpx;

        &:last-child {
            margin-bottom: 0;
        }

        .order-list-item-header {
            display: flex;
            align-items: baseline;
            padding-bottom: 20rpx;

            .special-text {
                font-size: 26rpx;
                font-weight: bold;
                display: flex;
                align-items: center;
                .icon {
                    transform: translateY(1px);
                    display: inline-block;
                }
            }

            .order-special-text {
                font-size: 24rpx;
                color: #000000;
            }

            .copy-text {
                color: #8c8c8c;
                border-radius: 14rpx;
                border: 1px solid #8c8c8c;
                padding: 2rpx 8rpx;
                font-size: 20rpx;
                margin-left: 12rpx;
            }

            .order-status {
                margin-left: auto;
                font-weight: bold;
                font-size: 24rpx;
                color: #cc1d31;
            }
        }

        .order-list-item-content {
            .item-content-text {
                display: flex;
                justify-content: space-between;
                align-items: center;
                padding-bottom: 10rpx;

                .special-color {
                    color: #999;
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
                    // border-radius: 10rpx;
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
                    flex: 1;
                    padding-left: 25rpx;
                    overflow: hidden;

                    .product-item-title {
                        font-weight: bold;
                        overflow: hidden;
                        white-space: nowrap;
                        text-overflow: ellipsis;
                        padding-bottom: 10rpx;
                        .groupon-tag {
                            display: inline-flex;
                            align-items: center;
                            justify-content: center;
                            padding-top: 4.5rpx;
                            margin-right: 5rpx;

                            .groupon-tag-text {
                                box-sizing: border-box;
                                padding: 4rpx;
                                border-radius: 4rpx;
                                border: 1px solid var(--primary-light-3);
                                font-weight: 500;
                                font-size: 20rpx;
                                line-height: 20rpx;
                                color: var(--primary-light-3);
                            }
                        }
                    }

                    .product-item-price {
                        display: flex;
                        padding-top: 5rpx;

                        .product-item-pricenum {
                            font-size: 30rpx;
                        }

                        .product-item-quantity {
                            font-size: 28rpx;
                            color: #999;
                            padding-left: 15rpx;
                        }
                    }
                }
            }

            .item-content-pay {
                display: flex;
                justify-content: flex-end;
                width: 100%;

                .reservation {
                    flex: 1;
                    color: #9e9e9e;
                    display: flex;
                    align-items: center;
                    column-gap: 6rpx;

                    .icon-riqi {
                        padding-top: 2rpx;
                        font-size: 16rpx;
                    }
                }

                .item-content-pay-box {
                    display: flex;

                    .pay-item {
                        padding: 0 10rpx;

                        &.pay-type {
                            color: #999;
                        }

                        &.total-money {
                            .price-content {
                                color: var(--general);
                            }
                        }
                    }
                }
            }
        }

        .order-list-item-btn {
            display: flex;
            justify-content: flex-end;
            padding-top: 25rpx;

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
                    margin-right: 10rpx;

                    &:last-child {
                        margin-right: 0;
                    }
                }
            }
        }
    }
    .presale-tag {
        color: var(--general);
    }

    .separator {
        color: #333;
        margin: 0 8rpx;
    }
}

:deep(.u-tabs__wrapper__nav__item) {
    flex: 1 !important;
    padding: 0 !important;
}
</style>
