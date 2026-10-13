<template>
    <tig-layout :title="$t('商品收藏')" :custom-back="true" @back="handleBack">
        <view v-if="collectList.length > 0" class="collect-product">
            <uni-swipe-action>
                <block v-for="item in collectList" :key="item.productId">
                    <view class="move-item">
                        <uni-swipe-action-item :threshold="0" auto-close>
                            <view class="collect-product-item" @click="handleLink(item.productId, item.shopId)">
                                <view class="collect-product-item-left">
                                    <view class="img-box">
                                        <tig-image :src="pickPic(item)" />
                                        <image
                                            v-if="styleMap[item.productId]?.borderImage"
                                            class="product-style-border"
                                            :src="styleMap[item.productId].borderImage"
                                            mode="scaleToFill"
                                        />
                                    </view>
                                </view>
                                <view class="collect-product-item-right">
                                    <view class="title">{{ item.productName }}</view>
                                    <view class="item-bottom">
                                        <view class="price">
                                            <format-price
                                                :decimals-style="{
                                                    fontSize: '25rpx'
                                                }"
                                                :currency-style="{
                                                    fontSize: '23rpx'
                                                }"
                                                :price-data="item.price"
                                            />
                                        </view>
                                    </view>
                                </view>
                            </view>
                            <template #right>
                                <view class="collect-move-box">
                                    <view class="btn-del" @click="__delCollect(item.productId)"
                                        ><text>{{ $t("取消收藏") }}</text></view
                                    >
                                </view>
                            </template>
                        </uni-swipe-action-item>
                    </view>
                </block>
            </uni-swipe-action>
        </view>
        <view v-if="collectList.length === 0 && loadend === true" class="empty-box">
            <view class="pictrue"><image :src="staticResource('common/data_empty.png')" /></view>
            <view class="txt">{{ $t("暂无收藏记录！") }}</view>
        </view>
        <loading-box v-model="loaded" :page="filterParams.page" :length="collectList.length" />
    </tig-layout>
</template>

<script setup lang="ts">
import { pickPic } from "@/utils/format";
/**
 * 商品收藏列表页
 * - 展示用户收藏的商品列表
 * - 支持左滑取消收藏
 * - 点击商品跳转商品详情
 */
import { ref, reactive } from "vue";
import { onLoad, onReachBottom } from "@dcloudio/uni-app";
import { getCollectProductList, delCollectProduct } from "@/api/user/collectProduct";
import type { CollectProductList, CollectProductFilterParams } from "@/types/user/collectProduct";
import { staticResource } from "@/utils";
import { useProductStyle } from "@/hooks/useProductStyle";
import { useI18n } from "vue-i18n";

const { t } = useI18n();

const filterParams = reactive<CollectProductFilterParams>({
    //初使化用于查询的参数
    page: 1,
    size: 10,
    keyword: ""
});
const total = ref(0);
const loaded = ref(false);
const loadend = ref(false);
const collectList = ref<CollectProductList[]>([]);
const { styleMap } = useProductStyle(collectList);
const __getCollectProductList = async () => {
    if (filterParams.page > 1) {
        loaded.value = true;
    } else {
        uni.showLoading({
            title: t("加载中")
        });
    }
    try {
        const result = await getCollectProductList({ ...filterParams });
        total.value = result.total;
        collectList.value = [...collectList.value, ...result.records];
        // collectList.value = Object.assign(collectList.value, result.records)
    } catch (error) {
        console.error(error);
    } finally {
        uni.hideLoading();
        loaded.value = false;
        loadend.value = true;
    }
};

const handleLink = (id: number, shopId?: number) => {
    let url = `/pages/product/index?id=${id}`;
    if (shopId && shopId > 0) {
        url += `&shopId=${shopId}`;
    }
    uni.navigateTo({ url });
};

const handleBack = () => {
    const pageList = getCurrentPages();
    const currentRoute = pageList[pageList.length - 1]?.route;
    const prevRoute = pageList[pageList.length - 2]?.route;
    const delta = prevRoute === currentRoute ? 2 : 1;

    // #ifdef H5
    if (pageList.length > delta - 1) {
        history.go(-delta);
    } else {
        uni.switchTab({
            url: "/pages/member/index"
        });
    }
    // #endif

    // #ifndef H5
    if (pageList.length > delta - 1) {
        uni.navigateBack({
            delta
        });
    } else {
        uni.switchTab({
            url: "/pages/member/index"
        });
    }
    // #endif
};

const __delCollect = (id: number) => {
    uni.showModal({
        title: t("提示"),
        content: t("确定取消收藏吗？"),
        confirmText: t("确定"),
        cancelText: t("取消"),
        success: async (res) => {
            if (res.confirm) {
                deleteASiteCollection(id);
            }
        }
    });
};

const deleteASiteCollection = async (value: number) => {
    try {
        await delCollectProduct({ id: value });
        uni.showToast({
            title: t("取消收藏成功"),
            icon: "none"
        });

        filterParams.page = 1;
        collectList.value = [];
        __getCollectProductList();
    } catch (error: any) {
        uni.showToast({
            title: error.message,
            icon: "none",
            duration: 1000
        });
    }
};

onLoad(() => {
    __getCollectProductList();
});

onReachBottom(() => {
    if (filterParams.page < Math.ceil(total.value / filterParams.size)) {
        filterParams.page++;
        __getCollectProductList();
    }
});
</script>

<style lang="scss" scoped>
.collect-product {
    padding: 25rpx 25rpx;
    .move-item {
        margin-bottom: 20rpx;
    }

    .collect-product-item {
        padding: 10rpx;
        background-color: #fff;
        border-radius: 20rpx;
        display: flex;
        overflow: hidden;

        .collect-product-item-left {
            .img-box {
                position: relative;
                width: 150rpx;
                height: 150rpx;
            }
        }

        .collect-product-item-right {
            width: 100%;
            padding-top: 15rpx;
            padding-left: 12rpx;
            .title {
                width: 500rpx;
                font-size: 28rpx;
                overflow: hidden;
                white-space: nowrap;
                text-overflow: ellipsis;
            }

            .item-bottom {
                display: flex;
                justify-content: space-between;
                align-items: center;
                padding-top: 25rpx;

                .price {
                    font-size: 32rpx;
                    color: var(--general);
                }
            }
        }
    }
}
.collect-move-box {
    width: 150rpx;
    height: 100%;
    color: #fff;
    text-align: center;
    vertical-align: middle;
    display: flex;
    text {
        display: block;
        position: absolute;
        top: 50%;
        margin-top: -20rpx;
        font-size: 24rpx;
        text-align: center;
        width: 100%;
    }
    .btn-collect {
        width: 150rpx;
        background: var(--vice-bg);
        color: var(--general);
        display: inline-block;
        height: 100%;
        vertical-align: middle;
        display: table-cell;
        position: relative;
    }
    .btn-del {
        width: 150rpx;
        background: var(--general);
        display: inline-block;
        height: 100%;
        vertical-align: middle;
        display: table-cell;
        position: relative;
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
</style>
