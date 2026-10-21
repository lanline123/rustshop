<template>
    <view class="invoice-list-page">
        <view class="invoice-list">
            <template v-for="item in invoiceList" :key="item.orderId">
                <view class="invoice-list-item">
                    <view class="invoice-list-item-header">
                        <text class="special-text"
                            >{{ $t("订单编号") }}：<text class="special-color">{{ item.orderSn }}</text>
                        </text>
                        <text v-if="item.invoiceData">{{ formatText(item.invoiceData.status) }}</text>
                        <text v-else>{{ configStore.canInvoice === 1 ? $t("可申请") : $t("不可申请") }}</text>
                    </view>
                    <view class="invoice-list-item-content">
                        <view class="item-content-product">
                            <template v-for="subItem in item.items" :key="subItem.itemId">
                                <navigator :url="'/pages/product/index?id=' + subItem.productId" hover-class="navigator-hover">
                                    <view class="item-content-product-item">
                                        <view class="item-content-product-img">
                                            <tig-image :src="pickPic(subItem)" />
                                        </view>
                                        <view class="item-content-product-info">
                                            <view class="product-item-title">
                                                {{ subItem.productName }}
                                            </view>
                                            <view class="product-item-price">
                                                <format-price
                                                    :show-text="false"
                                                    :decimals-style="{
                                                        fontSize: '25rpx'
                                                    }"
                                                    :currency-style="{
                                                        fontSize: '23rpx'
                                                    }"
                                                    class="product-item-pricenum"
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
                            <view class="item-content-pay-box">
                                <view class="pay-item total-money"
                                    >{{ $t("总额") }}：<format-price :show-text="false" :is-bottom="false" :price-data="item.totalAmount"
                                /></view>
                            </view>
                        </view>
                    </view>
                    <view class="invoice-list-item-btn">
                        <view class="item-btn-box">
                            <template v-if="item.invoiceData">
                                <tig-button
                                    :custom-style="{ width: '150rpx', height: '60rpx' }"
                                    :plain="true"
                                    :plain-main-color="true"
                                    @click="handleShowInfo(item.invoiceData, item.orderSn)"
                                >
                                    {{ $t("查看") }}
                                </tig-button>
                            </template>
                            <tig-button
                                v-else-if="configStore.canInvoice === 1"
                                :plain="true"
                                :custom-style="{ width: '150rpx', height: '60rpx' }"
                                :plain-main-color="true"
                                @click="handleApply(item.orderId)"
                            >
                                {{ $t("申请") }}
                            </tig-button>
                        </view>
                    </view>
                </view>
            </template>
            <view v-if="!isLoading && invoiceList.length === 0" class="invoice-empty">
                <empty-box center-content height="100%" mode="list" background="#f5f5f5" :text="$t('暂无申请')" />
            </view>
        </view>
    </view>
</template>

<script setup lang="ts">
import { pickPic } from "@/utils/format";
import { ref, reactive, onMounted, computed } from "vue";
import { getOrderList } from "@/api/user/order";
import type { OrderListFilterResult, OrderListInvoiceData } from "@/types/user/order";
import { useI18n } from "vue-i18n";
import { useConfigStore } from "@/store/config";

const configStore = useConfigStore();
const { t } = useI18n();

const invoiceList = ref<OrderListFilterResult[]>([]);

const filterParams = reactive({
    page: 1,
    size: 10,
    orderStatus: 5
});
const loaded = ref(false);
const total = ref(0);
const isLoading = ref(true);
const __getOrderList = async () => {
    isLoading.value = true;
    if (filterParams.page > 1) {
        loaded.value = true;
    } else {
        uni.showLoading({
            title: t("加载中")
        });
    }
    try {
        const result = await getOrderList(filterParams);
        invoiceList.value = [...invoiceList.value, ...result.records];
        total.value = result.total;
    } catch (error) {
        console.error(error);
    } finally {
        uni.hideLoading();
        loaded.value = false;
        isLoading.value = false;
    }
};

const formatText = (status: number) => {
    switch (status) {
        case 0:
            return t("待审核");
        case 1:
            return t("通过");
        default:
            return t("失败/作废");
    }
};

const handleApply = (id: number) => {
    uni.navigateTo({
        url: `/pages/user/invoiceManagement/applyInvoice?orderId=${id}`
    });
};

const handleShowInfo = (data: OrderListInvoiceData, orderSn: string) => {
    data.orderSn = orderSn;
    const invoiceData = encodeURIComponent(JSON.stringify(data));
    uni.navigateTo({
        url: "/pages/user/invoiceManagement/invoiceInfo?invoiceData=" + invoiceData
    });
};

onMounted(() => {
    __getOrderList();
});

defineExpose({
    __getOrderList,
    filterParams,
    total
});
</script>

<style lang="scss" scoped>
.invoice-list-page,
.invoice-list {
    flex: 1;
    display: flex;
    flex-direction: column;
}

.invoice-empty {
    flex: 1;
    min-height: 300rpx;
}

.invoice-list {
    padding: 10rpx 10rpx;

    .invoice-list-item {
        background-color: #fff;
        border-radius: 10rpx;
        margin-bottom: 20rpx;
        padding: 20rpx;
        font-size: 24rpx;

        &:last-child {
            margin-bottom: 0;
        }

        .invoice-list-item-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding-bottom: 20rpx;
            .special-text {
                font-size: 26rpx;
                font-weight: bold;
            }
            .special-color {
                color: #999;
                font-weight: normal;
            }
        }

        .invoice-list-item-content {
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
                }
                .item-content-product-info {
                    width: 495rpx;
                    padding-left: 25rpx;

                    .product-item-title {
                        font-weight: bold;
                        overflow: hidden;
                        white-space: nowrap;
                        text-overflow: ellipsis;
                    }

                    .product-item-price {
                        display: flex;
                        padding-top: 25rpx;
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

        .invoice-list-item-btn {
            display: flex;
            justify-content: flex-end;
            padding-top: 25rpx;

            .item-btn-box {
                display: flex;
            }
        }
    }
}
</style>
