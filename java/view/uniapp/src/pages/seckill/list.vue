<template>
    <tig-layout ref="layoutRef">
        <view class="seckill-title">
            <view class="head-box">
                <text class="iconfont icon-houtui" @click="toBack" />
                <text v-if="layoutRef && layoutRef.isZh">{{ $t("秒杀") }}</text>
                <image v-else class="tit-img" :src="staticResource('seckill/overseas-seckillTit.png')" mode="heightFix" />
            </view>
            <view class="label-box">
                <view class="item">
                    <image class="fire" :src="staticResource('seckill/fire.png')" mode="heightFix" />
                    <text> {{ $t("每日特惠") }}</text>
                </view>
                <view class="item">
                    <image class="fire" :src="staticResource('seckill/fire.png')" mode="heightFix" />
                    <text> {{ $t("限时抢购") }}</text>
                </view>
            </view>
        </view>
        <view class="qianggou-list">
            <view v-for="(item, index) in seckillProductList" :key="index" class="qianggou-item" @click="toDetail(item)">
                <view class="qianggou-img-box">
                    <image class="qianggou-img" :src="productImage(item)" mode="aspectFill" />
                    <view v-if="item.seckillStock === 0" class="qianggou-out">
                        <view class="qianggou-out-txt">{{ $t("已抢完") }}</view>
                    </view>
                    <image
                        v-if="styleMap[item.productId]?.borderImage"
                        class="product-style-border"
                        :src="styleMap[item.productId].borderImage"
                        mode="scaleToFill"
                    />
                </view>
                <view class="qianggou-info">
                    <view class="qianggou-tit line2">
                        {{ item.productName }}
                    </view>
                    <view class="progress-box flex align-center">
                        <view class="progress-bar">
                            <view
                                class="progress"
                                :class="{ sellOut: item.seckillStock === 0 }"
                                :style="{
                                    width: seckillPercentage(item.seckillSales, item.seckillStock)
                                }"
                            />
                        </view>
                        <view class="progress-txt">
                            <text v-if="item.seckillStock === 0"> {{ $t("已售罄") }}</text>
                            <text v-else> {{ $t("热销") }}</text>
                        </view>
                    </view>
                    <view class="qianggou-money-box">
                        <view class="qianggou-num">
                            <view class="num"
                                ><format-price
                                    :font-style="{ fontSize: '30rpx', color: '#1a1a1a', fontWeight: 'bold' }"
                                    :currency-style="{
                                        fontSize: '30rpx',
                                        color: '#1a1a1a',
                                        fontWeight: 'bold'
                                    }"
                                    :decimals-style="{
                                        fontSize: '30rpx',
                                        color: '#1a1a1a',
                                        fontWeight: 'bold'
                                    }"
                                    :price-data="item.seckillPrice"
                            /></view>
                            <view class="del">
                                <format-price
                                    :font-style="{ 'text-decoration': 'line-through', color: '#888b94', fontSize: '20rpx', fontWeight: 'bold' }"
                                    :is-bottom="false"
                                    :price-data="item.marketPrice"
                                />
                                <view class="label"> {{ $t("优惠{0}%", [getDiscount(Number(item.seckillPrice), Number(item.marketPrice))]) }} </view>
                            </view>
                        </view>
                        <view class="qianggou-btn">
                            <view v-if="item.canJoin === false" class="sell-out-btn">
                                {{ $t("仅限会员") }}
                            </view>
                            <view v-else-if="item.seckillStock === 0" class="sell-out-btn">
                                {{ $t("已售罄") }}
                            </view>
                            <view v-else class="btn">
                                <image class="img-btn" :src="staticResource('seckill/seckill.png')" mode="heightFix" />
                            </view>
                        </view>
                    </view>
                </view>
            </view>
        </view>
        <loading-box v-model="isLoadMore" color="#fff" :page="filterParams.page" :length="seckillProductList.length" />
    </tig-layout>
</template>

<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import { ref, reactive, onMounted } from "vue";
import { productImage } from "@/utils/format";
import { getSeckill } from "@/api/seckill/seckill";
import type { SeckillFilterState } from "@/types/seckill/seckill";
import { staticResource } from "@/utils";
import { useList } from "@/hooks";
import { useProductStyle } from "@/hooks/useProductStyle";
import { useConfigStore } from "@/store/config";
import { setPageMeta } from "@/utils/seo";

const { t } = useI18n();
const configStore = useConfigStore();
const layoutRef = ref();
const filterParams = reactive({
    //初始化用于查询的参数
    page: 1,
    size: 20
});
const { isLoadMore, data: seckillProductList } = useList<SeckillFilterState>(getSeckill, {
    params: filterParams,
    path: {
        dataKey: "records"
    },
    immediate: true
});

const { styleMap } = useProductStyle(seckillProductList);

const toDetail = (data: SeckillFilterState) => {
    let url = `/pages/product/index?id=${data.productId}${data.skuId > 0 ? `&skuId=${data.skuId}` : ""}${data.shopId > 0 ? `&shopId=${data.shopId}` : ""}`;
    uni.navigateTo({
        url
    });
};

const toBack = () => {
    uni.navigateBack();
};

const seckillPercentage = (sales: number, stock: number) => {
    return Math.round((sales / (stock + sales)) * 100) + "%";
};

// 秒杀价相对市场价的折扣百分比（用于「优惠 xx%」标签）
const getDiscount = (currentPrice: number, originalPrice: number) => {
    if (originalPrice === 0 || currentPrice >= originalPrice) {
        return 0;
    }

    const discountPercentage = ((originalPrice - currentPrice) / originalPrice) * 100;

    return Math.floor(discountPercentage * 10) / 10;
};

onMounted(() => {
    // #ifdef H5
    setPageMeta({
        title: `${t("限时秒杀")} - ${configStore.baseInfo.shopTitle}`,
        description: `${configStore.baseInfo.shopTitle}${t("限时秒杀优惠商品")}`
    });
    // #endif
});
</script>
<style>
page {
    background: #ffffff;
}
</style>
<style scoped lang="scss">
.seckill-title {
    background-color: #ff0000;
    padding-top: 10px;
    padding-bottom: 8px;
    .head-box {
        color: #fff;
        font-size: 30rpx;
        font-weight: 500;
        margin-left: 12px;
        display: flex;
        flex-direction: row;
        align-items: center;
        margin-right: 16px;
        margin-bottom: 20rpx;
        .iconfont {
            font-size: 38rpx;
            margin-right: 10rpx;
        }
        .tit-img {
            height: 26px;
        }
    }
    .label-box {
        margin-left: 18px;
        display: flex;
        align-items: center;
        margin-right: 16px;
        gap: 30rpx;
        margin-bottom: 15rpx;
        .item {
            display: flex;
            align-items: center;
            color: #fff;
            font-size: 24rpx;
            font-weight: 500;
            .fire {
                width: 28rpx;
                height: 28rpx;
                margin-right: 10rpx;
            }
        }
    }
}
.qianggou-list {
    padding: 20rpx;
}
.qianggou-item {
    margin-bottom: 15rpx;
    background-color: #fff;
    border-radius: 20rpx;
    padding: 20rpx;
    display: flex;
    align-items: center;

    .qianggou-img-box {
        width: 211rpx;
        height: 211rpx;
        position: relative;

        .qianggou-img,
        .qianggou-out {
            width: 100%;
            height: 100%;
        }

        .qianggou-out {
            position: absolute;
            top: 0;
            left: 0;
            display: flex;
            justify-content: center;
            align-items: center;
            background-color: rgba(255, 255, 255, 0.5);

            .qianggou-out-txt {
                width: 120rpx;
                height: 120rpx;
                line-height: 120rpx;
                background-color: rgba(0, 0, 0, 0.5);
                color: #ffffff;
                font-size: 26rpx;
                text-align: center;
                border-radius: 100rpx;
            }
        }

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

    .qianggou-info {
        margin-left: 20rpx;
        flex: 1;
        width: calc(100% - 200rpx);
        position: relative;

        .qianggou-tit {
            color: #1a1a1a;
            font-size: 26rpx;
            margin-bottom: 10rpx;
            font-family:
                PingFangSC,
                PingFang SC;
            width: 100%;
        }

        .progress-box {
            margin-top: 15rpx;
            .progress-bar {
                width: 158rpx; /* 进度条容器的总宽度 */
                height: 10rpx; /* 进度条的高度 */
                background-color: #ffebed; /* 进度条背景色 */
                border-radius: 4rpx; /* 进度条容器的圆角 */
            }
            .progress-txt {
                font-size: 22rpx;
                margin-left: 10rpx;
                color: #888b94;
            }

            .progress {
                height: 10rpx; /* 进度条的高度 */
                background: linear-gradient(90deg, #ff475d, #ff0f23); /* 进度条的颜色 */
                border-radius: 40rpx; /* 进度部分的圆角 */
                transition: width 0.4s ease; /* 进度变化时的过渡动画效果 */
            }
            .sellOut {
                background: #c2c4cc !important;
            }
        }

        .qianggou-money-box {
            width: 100%;
            display: flex;
            justify-content: space-between;
            align-items: center;
            height: 80rpx;
            margin-top: 20rpx;
            margin-right: 16px;
            background: linear-gradient(90deg, #fff6e8, #fff9ee);
            border-radius: 8px;
            .qianggou-num {
                margin-left: 15rpx;
                .del {
                    display: flex;
                    align-items: center;
                    .label {
                        background: linear-gradient(90deg, #ff475d, #ff0f23);
                        width: auto;
                        height: 24rpx;
                        padding: 4rpx;
                        color: #fff;
                        margin-left: 11rpx;
                        text-align: center;
                        font-size: 20rpx;
                        font-weight: 400;
                        line-height: 16rpx;
                        border-radius: 4rpx;
                    }
                }
            }
            .qianggou-btn {
                .sell-out-btn {
                    background-color: #c2c4cc;
                    color: #fff;
                    width: 140rpx;
                    height: 80rpx;
                    line-height: 80rpx;
                    text-align: center;
                    border-radius: 14rpx;
                    font-size: 24rpx;
                    font-weight: bold;
                }
                .btn {
                    .img-btn {
                        height: 80rpx;
                    }
                }
            }
        }
    }
}
</style>
