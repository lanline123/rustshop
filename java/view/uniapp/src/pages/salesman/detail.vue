<template>
    <view>
        <tig-layout>
            <view v-if="detailData.pics" class="salesman-pro-detail-imgs">
                <view class="product-bg">
                    <swiper :indicator-dots="false" :circular="true" interval="3000" duration="500" @change="swiperChange">
                        <block v-for="(item, index) in detailData.pics" :key="index">
                            <swiper-item>
                                <image
                                    mode="aspectFill"
                                    lazy-load
                                    :src="imageFormat(item.picUrl || '')"
                                    class="slide-image"
                                    @click="swiperImagePreview(index)"
                                />
                            </swiper-item>
                        </block>
                    </swiper>
                    <view class="pages show">
                        <text>{{ currentPic }}</text>
                        /{{ detailData.pics.length || 1 }}
                    </view>
                </view>
            </view>
            <view class="salesman-pro-detail-info">
                <view class="salesman-pro-detail-price">
                    <view class="salesman-pro-detail-price-num">
                        <format-price :price-data="detailData.productPrice" />
                    </view>
                    <view v-if="Number(detailData.finalCommissionPrice) > 0" class="salesman-pro-detail-price-tip">
                        <image class="price-tip-img" :src="staticResource('salesman/priceTip.png')" />
                        <view class="salesman-pro-detail-price-tip-text">
                            {{ $t("预计可赚") }} <format-price base :price-data="detailData.finalCommissionPrice" />
                        </view>
                    </view>
                </view>
                <view class="salesman-pro-detail-name">{{ detailData.productName }}</view>
            </view>

            <tig-fixed-placeholder background-color="#fff">
                <view class="bottom-btns">
                    <view class="bottom-btn" @click="handleLink(`/pages/product/index?id=${detailData.productId}`)">
                        <view class="bottom-btn-text"> {{ $t("立即购买") }} </view>
                    </view>
                    <view class="bottom-btn special" @click="showShare = true">
                        <view class="bottom-btn-text">
                            <view> {{ $t("分享赚钱") }}</view>
                            <text v-if="Number(detailData.finalCommissionPrice) > 0" class="bottom-btn-text-tip"
                                >{{ $t("分享后预计可赚") }} <format-price base :price-data="detailData.finalCommissionPrice"
                            /></text>
                        </view>
                    </view>
                </view>
            </tig-fixed-placeholder>

            <share v-model:show-share-card="showShare" :current-data="detailData" />
        </tig-layout>
    </view>
</template>

<script setup lang="ts">
/**
 * 推广商品详情页
 * - 展示推广商品详细信息
 * - 显示佣金比例和预估收益
 * - 提供分享推广功能
 */
import { ref } from "vue";
import { onLoad, onUnload } from "@dcloudio/uni-app";
import { salesmanProductDetail } from "@/api/salesman/salesman";
import type { SalesmanProductDetailItem } from "@/types/salesman/salesman";
import { imageFormat } from "@/utils/format";
import { staticResource } from "@/utils";
import share from "./src/share.vue";

onLoad((options) => {
    if (options && options.id) {
        getSalesmanProductDetail(options.id);
    }
});

const detailData = ref<SalesmanProductDetailItem>({} as SalesmanProductDetailItem);

const getSalesmanProductDetail = async (id: number) => {
    try {
        const result = await salesmanProductDetail(id);
        detailData.value = result;
    } catch (error) {
        console.error(error);
    }
};

const currentPic = ref(1);
const swiperChange = (e: any) => {
    currentPic.value = e.detail.current + 1;
};
const swiperImagePreview = (index: number) => {
    const images = detailData.value.pics.map((item) => imageFormat(item.picUrl || ""));
    uni.previewImage({
        current: images[index],
        urls: images
    });
};

const handleLink = (url: string) => {
    uni.navigateTo({
        url
    });
};

const showShare = ref(false);

// 关闭预览图片
onUnload(() => {
    // #ifdef  H5 || APP
    uni.closePreviewImage();
    // #endif
});
</script>

<style lang="scss" scoped>
.salesman-pro-detail-imgs {
    .product-bg {
        width: 100%;
        height: 50vh;
        position: relative;
        background: #fff;
        swiper {
            width: 100%;
            height: 100%;
            position: relative;
        }
        .slide-image {
            width: 100%;
            height: 100%;
        }
        .pages {
            visibility: hidden;
            transition: all 500ms ease-in-out 0s;
            position: absolute;
            background-color: rgba(0, 0, 0, 0.2);
            height: 40rpx;
            padding: 0 40rpx 0 30rpx;
            border-radius: 40rpx 0 0 40rpx;
            right: 0;
            bottom: 30rpx;
            line-height: 40rpx;
            font-size: 20rpx;
            color: #fff;
        }
        .pages.show {
            visibility: visible;
        }
        .pages text {
            font-weight: bold;
            font-size: 30rpx;
        }
        .slide-navigator .slide-image {
            width: 100%;
            height: 100%;
            border-radius: 15rpx;
        }
        .wx-swiper-dot {
            width: 20rpx;
            height: 5rpx;
            border-radius: 3rpx;
        }
        .wx-swiper-dots.wx-swiper-dots-horizontal {
            margin-bottom: 5rpx;
        }
    }
}

.salesman-pro-detail-info {
    position: relative;
    padding: 30rpx 32rpx 20rpx;
    background-color: #fff;

    .salesman-pro-detail-price {
        display: flex;
        color: #f44;
        margin: 4rpx 0 16rpx;
        align-items: center;
        .salesman-pro-detail-price-num {
            margin-right: 20rpx;
            font-size: 30rpx;
        }

        .salesman-pro-detail-price-tip {
            display: flex;
            align-items: center;
            background: linear-gradient(90deg, #ff6060, #f44);
            border-radius: 24rpx;
            position: relative;
            padding-right: 10rpx;
            .price-tip-img {
                width: 25px;
                height: 20px;
            }

            .salesman-pro-detail-price-tip-text {
                opacity: 0.89;
                font-size: 22rpx;
                color: #fff;
                font-weight: 700;
            }
        }
    }

    .salesman-pro-detail-name {
        padding: 20rpx 0;
        font-size: 32rpx;
    }
}

.salesman-pro-detail-attr {
    padding: 0 32rpx;
    background-color: #fff;
    margin-top: 20rpx;

    .salesman-pro-detail-attr-title {
        padding: 30rpx 0;
        font-size: 28rpx;
    }

    .salesman-pro-detail-attr-list {
        display: flex;
        flex-wrap: wrap;

        .salesman-pro-detail-attr-item {
            padding: 10rpx 20rpx;
        }
    }
}

.bottom-btns {
    height: 100%;
    width: 100%;
    display: flex;
    justify-content: space-around;
    align-items: center;
    padding: 0 15rpx;

    .bottom-btn {
        width: 48%;
        height: 74rpx;
        border-radius: 1998rpx;
        padding: 0 30rpx;
        font-size: 28rpx;
        color: #323233;
        background-color: #fff;
        border: 2rpx solid #ebedf0;
        display: flex;
        justify-content: center;
        align-items: center;
        font-size: 30rpx;
        .bottom-btn-text {
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;

            .bottom-btn-text-tip {
                font-size: 18rpx;
                padding-top: 4rpx;
            }
        }

        &.special {
            background-color: #f44;
            color: #fff;
            border: none;
        }

        &:active {
            opacity: 0.6;
        }
    }
}
</style>
