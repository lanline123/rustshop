<template>
    <tig-layout>
        <groupon-nav />
        <view class="groupon">
            <view class="groupon-menu">
                <view class="order-menu badgecolor">
                    <up-tabs
                        v-model:current="currentIndex"
                        line-color="var(--general)"
                        :list="menuList"
                        line-width="50rpx"
                        line-height="6rpx"
                        :style="{
                            fontSize: '28rpx'
                        }"
                        :active-style="{ padding: '0 10rpx', fontWeight: 'bold', color: '#1A1A1A' }"
                        :inactive-style="{ padding: '0 10rpx' }"
                        :scrollable="false"
                        key-name="value"
                        @change="handleMenuType"
                    />
                </view>
            </view>
            <view class="placeholder" />
            <view class="groupon-list">
                <view v-for="(item, index) in grouponRecordList" :key="index" class="groupon-item" @click="handleToDetail(item.grouponPromotionRecordId)">
                    <view class="groupon-item__wrap">
                        <view class="groupon-content">
                            <view class="groupon-img-box">
                                <tig-image class="groupon-img" :src="productImage(item)" mode="aspectFill" />
                            </view>
                            <view class="groupon-info">
                                <view class="flex flex-column main-info">
                                    <view class="groupon-tit line2">
                                        {{ item.productName }}
                                    </view>
                                    <view class="detail-group">
                                        <text class="btn">{{ item.minGroupNum }}{{ $t("人团") }}</text>
                                        <text class="num">{{ $t("已有") + item.joinNumCount + $t("人参团") }}</text>
                                    </view>
                                </view>
                                <view class="flex align-end">
                                    <view class="group-price">
                                        <text class="price-text">{{ $t("拼团价") }}:</text>
                                        <format-price
                                            :currency-style="{
                                                fontSize: '23rpx'
                                            }"
                                            :price-data="item.grouponPrice"
                                        />
                                    </view>
                                    <view class="original-price">
                                        <format-price
                                            :font-style="{ 'text-decoration': 'line-through' }"
                                            :currency-style="{
                                                fontSize: '23rpx'
                                            }"
                                            :price-data="item.productPrice"
                                        />
                                    </view>
                                </view>
                            </view>
                        </view>
                        <view class="groupon-footer">
                            <view class="groupon-desc">
                                <view v-if="item.joinStatus === 1" class="text">
                                    <text>{{ $t("拼团中") }}</text> ，
                                    <text>{{ $t("还差") }}</text>
                                    <text class="num">{{ item.minGroupNum - item.joinNumCount }}</text>
                                    <text>{{ $t("人") }}</text>
                                </view>
                                <view v-if="item.joinStatus === 2">
                                    <text class="text">{{ $t("拼团成功") }}</text>
                                </view>
                                <view v-if="item.joinStatus === 3">
                                    <text class="text">{{ $t("拼团失败") }}</text>
                                </view>
                            </view>
                            <view class="btns" @click.stop>
                                <tig-button
                                    v-if="item.joinStatus === 1"
                                    color="var(--main-bg)"
                                    :custom-style="{ height: '58rpx' }"
                                    :plain="true"
                                    @click.stop.prevent="handleShare(item)"
                                >
                                    {{ $t("邀请好友") }}
                                </tig-button>
                                <tig-button
                                    v-if="item.joinStatus === 2 || item.joinStatus === 3"
                                    color="#9A9A9A"
                                    :custom-style="{ height: '58rpx' }"
                                    :plain="true"
                                    @click.stop.prevent="handleToDetail(item.grouponPromotionRecordId)"
                                >
                                    {{ $t("查看详情") }}
                                </tig-button>
                            </view>
                        </view>
                    </view>
                </view>
            </view>
            <template v-if="!isLoading && total === 0">
                <view class="empty-box">
                    <up-empty :icon="staticResource('salesman/no_order.png')" :text="$t('暂无数据')" />
                </view>
            </template>
            <grouponShare
                v-model="showShare"
                :product-id="Number(productId)"
                :product-info="product"
                :groupon-promotion-id="grouponPromotionId"
                :groupon-promotion-record-id="grouponPromotionRecordId"
                :product-price="productPrice"
            />
            <loading-box v-model="isLoadMore" :page="filterParams.page" :length="grouponRecordList.length" />
        </view>
        <tig-loadingpage v-model="isLoading" />
    </tig-layout>
</template>

<script setup lang="ts">
/**
 * 我的拼团列表页
 * - 展示用户参与的拼团记录
 * - 按状态筛选（全部/进行中/成功/失败）
 * - 提供分享邀请好友参团功能
 */
import { reactive, ref } from "vue";
import { useUserStore } from "@/store/user";
import grouponNav from "./src/grouponNav.vue";
import grouponShare from "@/pages/groupon/src/grouponShare.vue";
import type { GrouponRecordItem, GrouponRecordListParams } from "@/types/groupon/groupon";
import type { ProductItem } from "@/types/product/product";
import { getGrouponRecordList, getGrouponRecordDetail } from "@/api/groupon/groupon";
import { getProductDetail } from "@/api/product/product";
import { productImage } from "@/utils/format";
import { redirect, staticResource } from "@/utils";
import { useI18n } from "vue-i18n";
import { useList } from "@/hooks";
import { onLoad } from "@dcloudio/uni-app";
const currentIndex = ref<number>(0);
const currentType = ref<number>(1);
const showShare = ref(false);
const { t } = useI18n();
const userStore = useUserStore();

const menuList = reactive<any>([
    { type: 1, value: t("我发起的团"), num: 0 },
    { type: 2, value: t("我参与的团"), num: 0 }
]);
const filterParams: GrouponRecordListParams = {
    page: 1,
    size: 10,
    joinType: currentType.value,
    isMine: true,
    dateType: 0,
    keyword: ""
};

const productId = ref(0);
const product = ref<ProductItem>({
    productId: 0,
    productStock: 0
});
const productPrice = ref<string>("0");
const grouponPromotionId = ref(0);
const grouponPromotionRecordId = ref(0);

const {
    data: grouponRecordList,
    getList,
    isLoadMore,
    isLoading,
    total
} = useList<GrouponRecordItem>(getGrouponRecordList, {
    params: filterParams,
    path: {
        dataKey: "records"
    },
    needReachBottom: true
    // immediate: true
});

/**
 * 处理分享
 */
const handleShare = async (item: GrouponRecordItem) => {
    if (!userStore.token) {
        uni.navigateTo({
            url: "/pages/login/index"
        });
        return;
    }
    try {
        // 拿到记录详情
        const grouponRecord = await getGrouponRecordDetail(item.grouponPromotionRecordId);
        if (!grouponRecord) {
            return;
        }
        // 拿到商品详情
        const productResult = await getProductDetail(grouponRecord.productId);
        grouponPromotionId.value = grouponRecord.grouponPromotionId;
        grouponPromotionRecordId.value = item.grouponPromotionRecordId;
        productId.value = productResult.item.productId;
        product.value = productResult.item;
        productPrice.value = item.grouponPrice.toString();
        showShare.value = true;
    } catch (error: any) {
        uni.showToast({
            title: error.message,
            icon: "none"
        });
    }
};

const handleToDetail = (id: number) => {
    redirect({
        url: `/pages/product/groupBuying?grouponPromotionRecordId=${id}`
    });
};

const handleMenuType = (data: any) => {
    currentType.value = data.type;
    filterParams.page = 1;
    filterParams.joinType = currentType.value;
    getList();
};

onLoad((option) => {
    if (option?.keyWords) {
        filterParams.keyword = option.keyWords;
        getList();
    } else {
        getList();
    }
});
</script>

<style lang="scss" scoped>
.placeholder {
    height: 88rpx;
}

.badgecolor {
    :deep(.uni-badge--error) {
        background-color: var(--general);
    }
}

.groupon-menu {
    background-color: #fff;
    box-sizing: border-box;
    position: fixed;
    width: 100%;
    z-index: 88;
    height: 88rpx;
}

.groupon-list {
    padding: 20rpx;
    display: flex;
    flex-direction: column;
    gap: 20rpx;
}

.groupon-item {
    .groupon-item__wrap {
        padding: 0 20rpx;
        background-color: #fff;
        border-radius: 8rpx;
        overflow: hidden;
    }

    .groupon-content {
        padding: 24rpx 0;
        display: flex;
        gap: 16rpx;
    }

    .groupon-img-box {
        width: 200rpx;
        height: 200rpx;
        position: relative;
        border-radius: 8rpx;
        overflow: hidden;
    }

    .groupon-info {
        flex: 1;
        display: flex;
        flex-direction: column;
        justify-content: space-between;
        padding-top: 6rpx;

        .main-info {
            row-gap: 20rpx;
        }

        .groupon-tit {
            color: #333333;
            font-size: 30rpx;
            font-weight: bold;
            width: 100%;
            padding-right: 30rpx;
        }

        .detail-group {
            display: flex;
            align-items: baseline;
            gap: 0 10rpx;

            .btn {
                padding: 4rpx 9rpx;
                border-radius: 4rpx;
                border: 1rpx solid var(--primary-light-3);
                font-weight: 500;
                font-size: 24rpx;
                line-height: 24rpx;
                color: var(--primary-light-3);
            }

            .num {
                font-weight: 500;
                font-size: 24rpx;
                line-height: 24rpx;
                color: #9a9a9a;
            }
        }

        .pro-txt-small {
            font-size: 25rpx;
            color: #666666;
            margin-bottom: 10rpx;
            width: 100%;

            .attr-tag {
                width: 25rpx;
                height: 25rpx;
                margin-right: 8rpx;
                margin-top: 3rpx;
            }

            .text {
                width: 400rpx;
            }
        }

        .group-price {
            font-weight: bold;
            font-size: 24rpx;
            line-height: 24rpx;
            color: var(--price);

            .price-text {
                margin-right: 11rpx;
                color: var(--main-bg);
            }
        }

        .original-price {
            font-size: 24rpx;
            line-height: 24rpx;
            color: #9a9a9a;
            margin-left: 16rpx;
        }
    }

    .groupon-footer {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 30rpx 0;
        border-top: 1rpx solid #f4f4f4;

        .groupon-desc {
            font-size: 28rpx;
            font-weight: 500;
            color: #2a2a2a;
        }
    }
}

.empty-box {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 100%;
    width: 100%;
}
</style>
