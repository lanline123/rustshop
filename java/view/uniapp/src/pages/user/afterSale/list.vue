<template>
    <tig-layout :title="$t('申请列表')">
        <view class="after-sale-list">
            <view class="order-list">
                <block v-for="item in afterSaleList" :key="item.orderId">
                    <view class="order-list-item">
                        <view class="order-list-item-header">
                            <text class="special-text"
                                >{{ $t("售后编号") }}：<text class="special-color">{{ item.aftersalesSn }}</text>
                            </text>
                            <text>{{ item.statusName }}</text>
                        </view>
                        <view class="order-list-item-content">
                            <view class="item-content-product">
                                <block v-for="subItem in item.aftersalesItems" :key="subItem.aftersalesItemId">
                                    <navigator :url="getProductUrl(subItem.productId, item.shopId)" hover-class="navigator-hover">
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
                                                        :price-data="getDisplayPrice(subItem)"
                                                    />
                                                    <view class="product-item-quantity">x {{ subItem.number }}</view>
                                                </view>
                                            </view>
                                        </view>
                                    </navigator>
                                </block>
                            </view>
                        </view>
                        <view class="order-list-item-btn">
                            <view class="item-btn-box">
                                <view class="base-item-btn detail" @click="handleAfterSaleDetail(item.aftersaleId)"> {{ $t("售后详情") }} </view>
                            </view>
                        </view>
                    </view>
                </block>
            </view>
            <loading-box v-model="loaded" :page="filterParams.page" :length="afterSaleList.length" />
        </view>
    </tig-layout>
</template>

<script setup lang="ts">
import { pickPic } from "@/utils/format";
/**
 * 售后申请列表页
 * - 展示用户所有售后申请记录
 * - 支持分页加载和触底加载更多
 * - 点击可进入售后详情页
 */
import { reactive, ref } from "vue";
import { aftersalesRecord } from "@/api/user/afterSale";
import type { AfterSaleListFilterResult } from "@/types/user/afterSale";
import { onShow, onReachBottom } from "@dcloudio/uni-app";
import { useI18n } from "vue-i18n";

const { t } = useI18n();

const filterParams = reactive({
    page: 1,
    size: 10
});
const loaded = ref(false);
const afterSaleList = ref<AfterSaleListFilterResult[]>([]);
const total = ref(0);
/**
 * 获取商品显示价格
 * @param item 商品信息
 * @returns 商品显示价格 （商品价格 + 附加价）
 */
const getDisplayPrice = (item: any) => {
    const rawExtraSkuData = item?.extraSkuData ?? [];
    let extraSkuData = rawExtraSkuData;
    if (typeof rawExtraSkuData === "string") {
        try {
            extraSkuData = JSON.parse(rawExtraSkuData);
        } catch {
            extraSkuData = [];
        }
    }
    if (!Array.isArray(extraSkuData)) {
        extraSkuData = [];
    }
    const extraPrice = extraSkuData.reduce((sum: number, extra: any) => {
        const raw = extra?.totalAttrPrice ?? extra?.attrPrice ?? 0;
        return sum + Number(raw || 0);
    }, 0);
    return Number(item?.price || 0) + extraPrice;
};

const getList = async () => {
    if (filterParams.page > 1) {
        loaded.value = true;
    } else {
        uni.showLoading({
            title: t("加载中")
        });
    }
    try {
        const result = await aftersalesRecord(filterParams);
        afterSaleList.value = [...afterSaleList.value, ...result.records];
        total.value = result.total;
    } catch (error) {
        console.error(error);
    } finally {
        uni.hideLoading();
        loaded.value = false;
    }
};

const handleAfterSaleDetail = (id: number) => {
    uni.navigateTo({
        url: `/pages/user/afterSale/info?id=${id}`
    });
};

const getProductUrl = (productId: number, shopId?: number) => {
    let url = `/pages/product/index?id=${productId}`;
    if (shopId && shopId > 0) {
        url += `&shopId=${shopId}`;
    }
    return url;
};

onReachBottom(() => {
    if (filterParams.page < Math.ceil(total.value / filterParams.size)) {
        filterParams.page++;
        getList();
    }
});

onShow(() => {
    filterParams.page = 1;
    afterSaleList.value = [];
    getList();
});
</script>

<style lang="scss" scoped>
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

        .order-list-item-btn {
            display: flex;
            justify-content: flex-end;
            padding-top: 25rpx;

            .item-btn-box {
                display: flex;
                .base-item-btn {
                    padding: 10rpx 25rpx;
                    border: 1px solid #ddd;
                    border-radius: 30rpx;
                    margin-left: 10rpx;
                    &.detail {
                        border: 1px solid var(--general);
                        color: var(--general);
                    }

                    &:active {
                        opacity: 0.6;
                    }
                }
            }
        }
    }
}
</style>
