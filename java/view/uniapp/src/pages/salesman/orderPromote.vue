<template>
    <tig-layout>
        <timeFilter v-model:params="filterParams" @change="timeFilterChange" />
        <view class="orderpromote-box">
            <view class="orderpromote-list">
                <block v-for="item in list" :key="item.orderId">
                    <view class="orderpromote-list-item">
                        <view class="orderpromote-list-item-status">
                            <view v-if="item.userOrder && item.userOrder.consignee" class="orderpromote-list-item-name"
                                >{{ $t("买家：") }}{{ item.userOrder.consignee }}</view
                            >
                            <view
                                class="orderpromote-list-item-statustext"
                                :style="[{ color: item.statusText === '已失效' && !item.userOrderRefund ? '#969799' : '' }]"
                                >{{ item.userOrderRefund ? $t("已退款") : item.statusText }}</view
                            >
                        </view>
                        <view class="orderpromote-list-item-time">{{ item.addTime }}</view>
                        <view class="orderpromote-list-item-product">
                            <view class="item-product-left">
                                <view class="item-product-left-img">
                                    <tig-image :src="pickPic(item.userOrderItem)" />
                                </view>
                            </view>
                            <view class="item-product-right">
                                <view class="item-product-right-name line2">{{ item.userOrderItem.productName }}</view>
                                <!-- <view class="item-product-right-ratio">{{ $t('佣金比例：') }}{{  }}</view> -->
                                <view class="item-product-right-num">x{{ item.userOrderItem.quantity }}</view>
                            </view>
                        </view>
                        <view class="orderpromote-list-item-orderamount"> {{ $t("实付金额：") }}<format-price base :price-data="item.orderAmount" /> </view>
                        <view class="orderpromote-list-item-amount">
                            {{ $t("商品佣金：") }}<format-price base :font-style="{ color: '#ff720d', 'font-weight': 700 }" :price-data="item.amount" />
                        </view>
                        <!-- <view class="orderpromote-list-item-btn">
                            <view @click="handleLink(item.itemId)">
                                <tig-button :customStyle="{ 'border-color': '#ebedf0' }" :plain="true" color="#333" style="height: 64rpx"
                                    >结算详情</tig-button
                                ></view
                            >
                        </view> -->
                    </view>
                </block>
            </view>
        </view>
        <empty v-if="!loading && total === 0" :styles="{ width: '320rpx', height: '320rpx' }" />
    </tig-layout>
</template>

<script lang="ts" setup>
import { pickPic } from "@/utils/format";
/**
 * 推广订单列表页
 * - 展示分销员推广产生的订单
 * - 支持按时间筛选和搜索
 * - 显示订单佣金信息
 */
import { reactive, ref } from "vue";
import empty from "@/pages/salesman/src/empty.vue";
import timeFilter from "@/pages/salesman/src/timeFilter.vue";
import { salesmanOrderList } from "@/api/salesman/salesman";
import type { OrderListFilterResult } from "@/types/salesman/salesman";
import { onLoad, onReachBottom } from "@dcloudio/uni-app";
const filterParams = ref({
    page: 1,
    size: 10,
    keyword: "",
    orderTimeStart: "",
    orderTimeEnd: ""
});
const loading = ref(false);
const list = ref<OrderListFilterResult[]>([]);
const total = ref(0);
const getSalesmanOrderList = async () => {
    loading.value = true;
    try {
        const result = await salesmanOrderList(filterParams.value);
        list.value = result.records;
        total.value = result.total;
    } catch (error) {
        console.log(error);
    } finally {
        loading.value = false;
    }
};

const timeFilterChange = () => {
    filterParams.value.page = 1;
    getSalesmanOrderList();
};

const handleLink = (id: number) => {
    uni.navigateTo({
        url: `/pages/salesman/settlementDetail?id=${id}`
    });
};

onLoad(() => {
    getSalesmanOrderList();
});
onReachBottom(() => {
    if (filterParams.value.page < Math.ceil(total.value / filterParams.value.size)) {
        filterParams.value.page++;
        getSalesmanOrderList();
    }
});
</script>

<style lang="scss" scoped>
.orderpromote-list {
    .orderpromote-list-item {
        background-color: #fff;
        padding: 24rpx 24rpx 0;
        margin-top: 24rpx;
        border-radius: 8rpx;

        .orderpromote-list-item-status {
            display: flex;
            justify-content: space-between;
            align-items: center;
            font-size: 28rpx;
            height: 36rpx;

            .orderpromote-list-item-name {
                color: #323233;
            }
            .orderpromote-list-item-statustext {
                color: #ff720d;
            }
        }

        .orderpromote-list-item-time {
            font-size: 24rpx;
            line-height: 32rpx;
            color: #969799;
            margin: 16rpx 0;
        }

        .orderpromote-list-item-product {
            padding: 24rpx;
            display: flex;
            .item-product-left {
                margin-right: 16rpx;
                .item-product-left-img {
                    width: 160rpx;
                    height: 160rpx;
                }
            }

            .item-product-right {
                min-height: 176rpx;
                display: flex;
                flex-direction: column;
                justify-content: space-between;

                .item-product-right-name {
                    margin-bottom: 12rpx;
                    line-height: 40rpx;
                    font-size: 28rpx;
                }

                .item-product-right-num {
                    margin-top: 16rpx;
                    line-height: 40rpx;
                    display: flex;
                    justify-content: flex-end;
                    color: #969799;
                }
            }
        }
        .orderpromote-list-item-orderamount {
            display: flex;
            justify-content: flex-end;
            height: 64rpx;
            font-size: 24rpx;
            align-items: center;
            color: #969799;
        }

        .orderpromote-list-item-amount {
            display: flex;
            justify-content: flex-end;
            align-items: center;
            height: 64rpx;
            font-size: 24rpx;
        }

        .orderpromote-list-item-btn {
            height: 96rpx;
            display: flex;
            justify-content: flex-end;
            align-items: center;
            border-top: 2rpx solid #ebedf0;
        }
    }
}
</style>
