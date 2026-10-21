<template>
    <template v-if="mode === 'list'">
        <view class="product-card">
            <view class="image" @click="toDetail(item.productId)">
                <tig-lazy-image :src="pickPic(item)" mode="aspectFill" />
                <image v-if="productStyle?.borderImage" class="product-style-border" :src="productStyle.borderImage" mode="scaleToFill" />
            </view>
            <view class="detail">
                <view class="flex flex-column main-info" @click="toDetail(item.productId)">
                    <view class="line2">
                        <text class="detail-title">{{ item.productName }}</text>
                    </view>
                    <view class="detail-group">
                        <text class="group-num">{{ maxGroupNum }}{{ $t("人团") }}</text>
                        <text class="num">{{ $t("已有") + item.joinNumCount + $t("人参团") }}</text>
                    </view>
                </view>
                <view class="flex align-end">
                    <view class="group-price">
                        <!-- <text class="price-text">{{ $t("拼团价") }}:</text> -->
                        <format-price
                            :currency-style="{
                                fontSize: '22rpx'
                            }"
                            :price-data="item.minGrouponPrice"
                        />
                    </view>
                    <view class="original-price">
                        <format-price :font-style="{ 'text-decoration': 'line-through' }" :price-data="item.productPrice" />
                    </view>
                </view>
                <view class="act-btn">
                    <tig-button
                        v-if="isActivityStart && !isActivityEnd"
                        color="var(--main-bg)"
                        :custom-style="{ height: '58rpx' }"
                        :plain="true"
                        @click="onStartGroup(item)"
                    >
                        {{ $t("我要开团") }}
                    </tig-button>
                    <tig-button v-else color="var(--main-bg)" :custom-style="{ height: '58rpx' }" :plain="true" @click="onBuy(item)">
                        {{ $t("原价购买") }}
                    </tig-button>
                </view>
            </view>
        </view>
    </template>
    <template v-if="mode === 'waterfall'">
        <div class="waterfall-item">
            <view class="product-card">
                <view class="image" @click="toDetail(item.productId)">
                    <tig-lazy-image :src="pickPic(item)" mode="widthFix" />
                    <image v-if="productStyle?.borderImage" class="product-style-border" :src="productStyle.borderImage" mode="scaleToFill" />
                </view>
                <view class="detail">
                    <view class="flex flex-column main-info" @click="toDetail(item.productId)">
                        <view class="line2">
                            <text class="group-num">{{ maxGroupNum }}{{ $t("人团") }}</text>
                            <text class="detail-title">{{ item.productName }}</text>
                        </view>
                        <view class="detail-group">
                            <text class="num">{{ $t("已有") + item.joinNumCount + $t("人参团") }}</text>
                        </view>
                    </view>
                    <view class="flex price-box">
                        <view class="group-price">
                            <!-- <text class="price-text">{{ $t("拼团价") }}:</text> -->
                            <format-price
                                :currency-style="{
                                    fontSize: '20rpx'
                                }"
                                :decimals-style="{ fontSize: '22rpx' }"
                                :price-data="item.minGrouponPrice"
                            />
                        </view>
                        <view class="original-price">
                            <format-price :font-style="{ 'text-decoration': 'line-through' }" :price-data="item.productPrice" />
                        </view>
                    </view>
                </view>
            </view>
        </div>
    </template>
</template>

<script lang="ts" setup>
import { pickPic } from "@/utils/format";
/**
 * 拼团商品卡片组件
 * - 展示拼团商品信息
 * - 支持列表和瀑布流两种样式
 */
import { computed, withDefaults } from "vue";
import { redirect } from "@/utils";
import { getMaxGroupNumFromJson } from "@/utils/groupon";
import type { GrouponItem } from "@/types/home/home";
import type { ProductActiveStyleVO } from "@/types/product/productStyle";

interface IProps {
    mode: "list" | "waterfall";
    item: GrouponItem;
    isActivityStart: boolean; // 是否活动开始
    isActivityEnd: boolean; // 是否活动结束
    productStyle?: ProductActiveStyleVO | null;
}

interface IEmits {
    (e: "startGroup", item: GrouponItem): void;
    (e: "buy", item: GrouponItem): void;
}
const props = withDefaults(defineProps<IProps>(), {
    productStyle: null
});
const emit = defineEmits<IEmits>();

const maxGroupNum = computed(() => getMaxGroupNumFromJson(props.item.groupNum));

/**
 * 跳转到商品详情
 * @param productId 商品id
 */
const toDetail = (productId: number) => {
    let url = `/pages/product/index?id=${productId}`;
    redirect({ url });
};

/**
 * 开团
 * @param item 团购商品信息
 */
const onStartGroup = (item: GrouponItem) => {
    emit("startGroup", item);
};

/**
 * 原价购买
 * @param item 团购商品信息
 */
const onBuy = (item: GrouponItem) => {
    emit("buy", item);
};
</script>

<style lang="scss" scoped>
.product-card {
    background-color: #fff;
    display: flex;
    gap: 11rpx;
    padding: 30rpx 30rpx 30rpx 20rpx;

    border-radius: 8rpx;

    .image {
        position: relative;
        width: 200rpx;
        height: 200rpx;
        border-radius: 8rpx;
        flex-shrink: 0;
        overflow: hidden;
        .product-style-border {
            position: absolute;
            left: 0;
            top: 0;
            width: 100%;
            height: 100%;
            z-index: 5;
            pointer-events: none;
        }
    }

    .detail {
        flex: 1;
        display: flex;
        flex-direction: column;
        justify-content: space-between;
        position: relative;

        .main-info {
            row-gap: 20rpx;

            .line2 {
                padding-right: 30rpx;
            }
        }

        .detail-title {
            font-weight: bold;
            font-size: 30rpx;
            color: #2a2a2a;
            text-overflow: ellipsis;
            word-break: normal;
        }

        .group-num {
            text-align: center;
            padding: 4rpx 9rpx;
            border-radius: 4rpx;
            border: 1px solid var(--primary-light-3);
            font-weight: 500;
            font-size: 24rpx;
            line-height: 24rpx;
            color: var(--primary-light-3);
            box-sizing: border-box;
            display: inline-flex;
        }

        .detail-group {
            display: flex;
            align-items: baseline;
            gap: 0 10rpx;

            .num {
                font-weight: 500;
                font-size: 24rpx;
                color: #9a9a9a;
            }
        }

        .group-price {
            font-weight: bold;
            font-size: 24rpx;
            line-height: 24rpx;
            color: var(--price);

            .price-text {
                margin-right: 11rpx;
            }
        }

        .original-price {
            font-size: 24rpx;
            line-height: 24rpx;
            color: #9a9a9a;
            margin-left: 16rpx;
        }

        .act-btn {
            position: absolute;
            right: 0;
            bottom: 0;
        }
    }
}

.waterfall-item {
    .line2 {
        line-height: 40rpx;
    }

    .group-num {
        margin-right: 10rpx;
    }

    .product-card {
        padding: 0;
        flex-direction: column;
        border-radius: 10rpx;
        box-shadow: 0rpx 2rpx 14rpx 0rpx rgba(0, 0, 0, 0.06);
        background: #fff;
        overflow: hidden;
        gap: 0;

        .image {
            width: 100%;
            height: 345rpx;
            border-radius: 0;
            height: auto;
        }

        .detail {
            padding: 20rpx;
            display: flex;
            flex-direction: column;
            gap: 28rpx;

            .detail-title {
                font-size: 28rpx;
                color: #333;
            }
        }
    }
}
</style>
