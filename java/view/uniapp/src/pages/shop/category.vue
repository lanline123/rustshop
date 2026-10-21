<template>
    <tig-layout title="店铺分类" :shop-id="shopId">
        <view class="shop-search-box" @click="handleToSearch">
            <view class="shop-search">
                <view class="search-text"> <text class="iconfont-h5 icon-sousuo sousuo-icon" />{{ $t("搜索") }} </view>
            </view>
        </view>

        <view class="category-content">
            <view class="category-content-item">
                <view class="item-box special" @click="handleToSearchResult(0)">
                    <view class="item-left">{{ $t("全部商品") }}</view>
                    <view class="item-right">
                        <text class="iconfont-h5 icon-youjiantou youjiantou-icon special" />
                    </view>
                </view>
            </view>
            <block v-for="(item, index) in categoryList" :key="item.categoryId">
                <view class="category-content-item">
                    <view class="item-box" @click="handleToSearchResult(item.categoryId)">
                        <view class="item-left">{{ item.categoryName }}</view>
                        <view class="item-right">
                            <text class="iconfont-h5 icon-youjiantou youjiantou-icon" />
                        </view>
                    </view>
                    <block v-if="item.children">
                        <view class="item-children-box">
                            <block v-for="(child, childIndex) in item.children" :key="child.categoryId">
                                <view class="children-item" @click.stop="handleToSearchResult(child.categoryId)">
                                    <view class="children-item-text">{{ child.categoryName }}</view>
                                </view>
                            </block>
                        </view>
                    </block>
                </view>
            </block>
        </view>
    </tig-layout>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import { getShopCategory } from "@/api/shop/shop";
import type { ShopCategoryList } from "@/types/shop/shop";

const shopId = ref(0);
const categoryList = ref<ShopCategoryList[]>([]);

onLoad((options) => {
    if (options && options.shopId) {
        shopId.value = options.shopId;
        __getShopCategory();
    }
});

const __getShopCategory = async () => {
    try {
        const result = await getShopCategory(shopId.value);
        categoryList.value = result;
    } catch (error) {
        console.error(error);
    }
};

const handleToSearch = () => {
    uni.navigateTo({
        url: `/pages/search/index?shopId=${shopId.value}`
    });
};

const handleToSearchResult = (shopCategoryId: number) => {
    if (shopCategoryId > 0) {
        uni.navigateTo({
            url: `/pages/search/result?shopId=${shopId.value}&shopCategoryId=${shopCategoryId}`
        });
    } else {
        uni.navigateTo({
            url: `/pages/search/result?shopId=${shopId.value}`
        });
    }
};
</script>

<style lang="scss" scoped>
.shop-search-box {
    background-color: #fff;
    padding: 20rpx;
    .shop-search {
        height: 58rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        width: 100%;
        .search-text {
            width: 100%;
            display: flex;
            align-items: center;
            justify-content: start;
            height: 100%;
            border-radius: 36rpx;
            background-color: #f0f2f5;
            font-size: 26rpx;
            color: #c8c9cc;
            padding: 0 25rpx;

            .sousuo-icon {
                padding-right: 6rpx;
            }
        }
    }
}

.category-content {
    margin: 0 15rpx;
    .category-content-item {
        background-color: #fff;
        padding: 20rpx 15rpx;
        border-radius: 15rpx;
        margin: 20rpx 0;

        .item-box {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding-bottom: 20rpx;
            padding-top: 20rpx;

            &.special {
                padding: 0;
            }

            &:last-child {
                padding-bottom: 0;
            }
            &:first-child {
                padding-top: 0;
            }
            .item-left {
                color: #232326;
                font-size: 28rpx;
                font-weight: 600;
                position: relative;
                padding-left: 15rpx;
                &::before {
                    content: "";
                    position: absolute;
                    left: 0;
                    top: 50%;
                    transform: translateY(-50%);
                    width: 5rpx;
                    height: 26rpx;
                    background-color: var(--general);
                    border-radius: 10rpx;
                }
            }

            .item-right {
                width: 60rpx;
                display: flex;
                align-items: center;
                justify-content: end;

                .youjiantou-icon {
                    font-size: 34rpx;
                    // color: #cfcfcf;
                    font-weight: bold;
                }
            }
        }

        .item-children-box {
            display: grid;
            grid-template-columns: repeat(2, 1fr);
            grid-gap: 10rpx;
            padding-left: 15rpx;

            .children-item {
                display: flex;
                align-items: center;
                height: 80rpx;
                border-radius: 10rpx;
                background-color: #f6f6f6;
                padding-left: 20rpx;
                font-size: 26rpx;
            }
        }
    }
}
</style>
