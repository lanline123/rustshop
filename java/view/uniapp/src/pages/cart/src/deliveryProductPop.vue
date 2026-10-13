<template>
    <tig-popup v-model:show="show" :z-index="1000" background-color="#fff" :show-close="false">
        <view class="delivery-pop">
            <view class="popup-title-box">
                <view class="title"> {{ $t("选择配送方式") }} </view>
                <view class="close-icon" @click="show = false">
                    <image src="@/static/images/common/close.png" />
                </view>
            </view>
            <view class="tip">{{ $t("不同的配送方式的商品，暂不支持同时结算下单") }}</view>
            <view class="content">
                <template v-if="deliveryList.length > 0">
                    <view class="content-title">
                        <view class="iconfont-h5 icon-daishouhuo1" />
                        <view class="title-text">{{ $t(configStore.defaultLogisticsName || "商家配送") }}</view>
                        <view class="title-num"
                            >{{ $t("共") }}<text>{{ deliveryList.length }}</text
                            >{{ $t("件") }}</view
                        >
                    </view>

                    <view class="list-content">
                        <view class="list">
                            <scroll-view :scroll-x="true" class="list-scroll">
                                <template v-for="item in deliveryList" :key="item.productId">
                                    <view class="list-item">
                                        <image :src="productImage(item)" />
                                    </view>
                                </template>
                            </scroll-view>
                        </view>
                        <view class="btn" @click="hanleItemClick(1)">{{ $t("去结算") }}</view>
                    </view>
                </template>

                <template v-if="pickupList.length > 0">
                    <view class="content-title">
                        <view class="iconfont-h5 icon-mendianziti" />
                        <view class="title-text">{{ $t("到店自提") }}</view>
                        <view class="title-num"
                            >{{ $t("共") }}<text>{{ pickupList.length }}</text
                            >{{ $t("件") }}</view
                        >
                    </view>

                    <view class="list-content">
                        <view class="list">
                            <scroll-view :scroll-x="true" class="list-scroll">
                                <template v-for="item in pickupList" :key="item.productId">
                                    <view class="list-item">
                                        <image :src="productImage(item)" />
                                    </view>
                                </template>
                            </scroll-view>
                        </view>
                        <view class="btn" @click="hanleItemClick(2)">{{ $t("去结算") }}</view>
                    </view>
                </template>

                <template v-if="localDeliveryList.length > 0">
                    <view class="content-title">
                        <view class="iconfont-h5 icon-tongcheng1" />
                        <view class="title-text">{{ $t("同城配送") }}</view>
                        <view class="title-num"
                            >{{ $t("共") }}<text>{{ localDeliveryList.length }}</text
                            >{{ $t("件") }}</view
                        >
                    </view>

                    <view class="list-content">
                        <view class="list">
                            <scroll-view :scroll-x="true" class="list-scroll">
                                <template v-for="item in localDeliveryList" :key="item.productId">
                                    <view class="list-item">
                                        <image :src="productImage(item)" />
                                    </view>
                                </template>
                            </scroll-view>
                        </view>
                        <view class="btn" @click="hanleItemClick(3)">{{ $t("去结算") }}</view>
                    </view>
                </template>
            </view>
        </view>
    </tig-popup>
</template>

<script setup lang="ts">
/**
 * 凑单商品弹窗组件
 * - 显示凑单/满减活动所需商品列表
 * - 可选择商品加入购物车凑单
 */
import { computed } from "vue";
import { productImage } from "@/utils/format";
import type { PropType } from "vue";
import { useConfigStore } from "@/store/config";

const configStore = useConfigStore();

const show = defineModel({
    type: Boolean,
    default: false
});

const props = defineProps({
    pickupProductData: {
        type: Array as PropType<any[]>,
        default: () => []
    },
    deliveryProducts: {
        type: Array as PropType<any[]>,
        default: () => []
    },
    localDeliveryProducts: {
        type: Array as PropType<any[]>,
        default: () => []
    }
});

const emit = defineEmits(["orderCheckType"]);

const pickupList = computed(() => {
    return props.pickupProductData;
});

const deliveryList = computed(() => {
    return props.deliveryProducts;
});
console.log(deliveryList, "deliveryList");

const localDeliveryList = computed(() => {
    return props.localDeliveryProducts;
});

const hanleItemClick = (type: number) => {
    emit("orderCheckType", type);
    show.value = false;
};
</script>

<style lang="scss" scoped>
.popup-title-box {
    display: flex;
    justify-content: center;
    padding: 35rpx 28rpx 28rpx;
    background-color: #fff;

    .title {
        flex: 1;
        display: flex;
        justify-content: center;
        font-size: 32rpx;
        line-height: 42rpx;
        font-weight: 700;
    }

    .close-icon {
        display: flex;
        align-items: center;

        image {
            width: 35rpx;
            height: 35rpx;
        }
    }
}

.delivery-pop {
    border-radius: 32rpx 32rpx 0rpx 0rpx;
    display: flex;
    flex-direction: column;
    overflow: hidden;
    height: 885rpx;
    overflow-y: auto;
    max-width: 100vw;

    .tip {
        font-size: 24rpx;
        color: #9a9a9a;
        text-align: center;
        padding-bottom: 50rpx;
    }

    .content {
        padding: 0 20rpx;
    }

    .content-title {
        display: flex;
        column-gap: 20rpx;
        align-items: center;
        color: #6a6a6a;
        padding-bottom: 35rpx;

        .icon-daishouhuo1,
        .icon-mendianziti,
        .icon-tongchengpeisong-xuanzhong {
            font-size: 32rpx;
        }

        .title-text {
            font-weight: bold;
            font-size: 26rpx;
        }

        .title-num {
            font-size: 22rpx;
            color: #9a9a9a;

            text {
                color: var(--general);
            }
        }
    }

    .list-content {
        background: #f5f5f5;
        border-radius: 16rpx;
        padding: 28rpx 32rpx;
        display: flex;
        width: 100%;
        align-items: center;
        margin-bottom: 60rpx;
        overflow: hidden;
        column-gap: 30rpx;

        .list {
            width: calc(100% - 128rpx);

            .list-scroll {
                width: 100%;
                white-space: nowrap;

                .list-item {
                    display: inline-block;
                    width: 100rpx;
                    height: 100rpx;
                    background: #ffffff;
                    border-radius: 8rpx;
                    margin-right: 20rpx;
                    box-sizing: content-box;

                    &:last-child {
                        margin-right: 0;
                    }

                    image {
                        width: 100%;
                        height: 100%;
                        border-radius: 8rpx;
                        overflow: hidden;
                    }
                }
            }
        }

        .btn {
            width: 98rpx;
            height: 50rpx;
            background: #ee0a24;
            border-radius: 4rpx;
            color: #fff;
            font-size: 22rpx;
            text-align: center;
            line-height: 50rpx;
        }
    }
}
</style>
