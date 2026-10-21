<template>
    <tig-layout :title="pageTitle">
        <view class="collection-shop">
            <template v-if="collectionShopList.length > 0">
                <uni-swipe-action>
                    <view class="collection-content">
                        <template v-for="item in collectionShopList" :key="item.shopId">
                            <view class="move-item">
                                <uni-swipe-action-item :threshold="0" auto-close>
                                    <view class="shop-item" @click="handleLink(item.shopId)">
                                        <view class="shop-item-left">
                                            <tig-image :src="item.shop.shopLogo" />
                                        </view>
                                        <view class="shop-item-right">
                                            <view class="shop-name line1">{{ item.shop.shopTitle }}</view>
                                            <view class="shop-desc line1">{{ item.shop.description ? item.shop.description : "" }}</view>
                                            <view class="shop-info">
                                                <text class="shop-info-text">{{ $t("上架商品：") }}{{ item.productCount }}</text>
                                                <text class="shop-info-text">{{ collectCountLabel }}{{ item.collectCount }}</text>
                                            </view>
                                        </view>
                                    </view>
                                    <template #right>
                                        <view class="collect-move-box">
                                            <view class="btn-del" @click="handleCancel(item.shopId)"
                                                ><text>{{ $t("取消收藏") }}</text></view
                                            >
                                        </view>
                                    </template>
                                </uni-swipe-action-item>
                            </view>
                        </template>
                    </view>
                </uni-swipe-action>
            </template>
            <!-- <empty-box v-if="collectionShopList.length == 0 && !loading && !loaded" text="暂无收藏记录！"></empty-box> -->
            <view v-if="collectionShopList.length === 0 && !loading && !loaded" class="empty-box">
                <view class="pictrue"><image :src="staticResource('common/data_empty.png')" /></view>
                <view class="txt">{{ $t("暂无收藏记录！") }}</view>
            </view>
            <loading-box v-model="loaded" :page="filterParams.page" :length="collectionShopList.length"></loading-box>
            <!-- <tig-loadingpage v-model="loading"></tig-loadingpage> -->
        </view>
    </tig-layout>
</template>

<script setup lang="ts">
/**
 * 门店/店铺收藏列表页
 * - 展示用户收藏的门店或店铺列表（按 VITE_IS_STORE 区分文案）
 * - 显示 Logo、名称、上架商品数、收藏数
 * - 支持左滑取消收藏
 */
import { reactive, ref, computed } from "vue";
import { getCollectionShop } from "@/api/user/collectionShop";
import { shopCollection } from "@/api/shop/shop";
import { onLoad, onReachBottom } from "@dcloudio/uni-app";
import type { collectionShopList } from "@/types/user/collectionShop";
import { useI18n } from "vue-i18n";
import { staticResource, toStoreOrShopHome, isO2o } from "@/utils";

const { t } = useI18n();

const pageTitle = computed(() => t(isO2o() ? t("门店收藏") : t("店铺收藏")));
const collectCountLabel = computed(() => t(isO2o() ? t("门店收藏：") : t("店铺收藏：")));

const filterParams = reactive({
    //初使化用于查询的参数
    page: 1,
    size: 10,
    excludeSelfOperated: 0,
    keyword: ""
});
const collectionShopList = ref<collectionShopList[]>([]);
const total = ref(0);
const loading = ref(false);
const loaded = ref(false);
const __getCollectionShop = async () => {
    if (filterParams.page > 1) {
        loaded.value = true;
    } else {
        loading.value = true;
    }
    try {
        const result = await getCollectionShop(filterParams);
        collectionShopList.value = result.records;
        total.value = result.total;
    } catch (error) {
        console.error(error);
    } finally {
        loaded.value = false;
        loading.value = false;
    }
};

onLoad(() => {
    __getCollectionShop();
});

const handleLink = (id: number) => {
    toStoreOrShopHome(id, { navType: "navigateTo" });
};

const handleCancel = async (id: number) => {
    try {
        const result = await shopCollection({ shopId: id });
        uni.showToast({
            title: t("取消成功"),
            icon: "none"
        });
        filterParams.page = 1;
        collectionShopList.value = [];
        __getCollectionShop();
    } catch (error) {
        console.error(error);
    }
};

onReachBottom(() => {
    if (filterParams.page < Math.ceil(total.value / filterParams.size)) {
        filterParams.page++;
        __getCollectionShop();
    }
});
</script>

<style lang="scss" scoped>
.collection-content {
    padding: 25rpx;
    .move-item {
        margin-bottom: 20rpx;
    }
    .shop-item {
        padding: 30rpx 20rpx;
        background-color: #fff;
        border-radius: 20rpx;
        display: flex;
        .shop-item-left {
            width: 130rpx;
            height: 130rpx;
            border-radius: 50%;
            overflow: hidden;
            display: flex;
            align-items: center;
            justify-content: center;
        }

        .shop-item-right {
            width: calc(100% - 150rpx);
            padding-left: 20rpx;
            .shop-name {
                font-size: 28rpx;
                width: 100%;
                white-space: nowrap;
                overflow: hidden;
                text-overflow: ellipsis;
            }

            .shop-desc {
                font-size: 24rpx;
                color: #999;
                width: 100%;
                padding-top: 15rpx;
                height: 50rpx;
            }
            .shop-info {
                font-size: 24rpx;
                color: #999;
                padding-top: 10rpx;

                .shop-info-text {
                    &:first-child {
                        padding-right: 18rpx;
                    }
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
</style>
