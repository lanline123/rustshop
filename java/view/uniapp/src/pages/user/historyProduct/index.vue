<template>
    <tig-layout :title="$t('浏览记录')">
        <up-checkbox-group v-model="checkboxValue" active-color="var(--general)" placement="column" @change="checkboxChange">
            <template v-if="historyList.length > 0">
                <view class="history-product">
                    <template v-for="item in historyList" :key="item.productId">
                        <view class="history-product-item" @click="handleLink(item.productId, item.shopId)">
                            <view class="history-product-item-top">
                                <view class="img-box">
                                    <tig-image :src="pickPic(item)" mode="aspectFill" />
                                    <image
                                        v-if="styleMap[item.productId]?.borderImage"
                                        class="product-style-border"
                                        :src="styleMap[item.productId].borderImage"
                                        mode="scaleToFill"
                                    />
                                </view>
                            </view>
                            <view class="history-product-item-bottom">
                                <view class="detail line2">
                                    {{ item.productName }}
                                </view>
                                <view class="price">
                                    <format-price
                                        :font-style="{ fontSize: '32rpx' }"
                                        :currency-style="{
                                            fontSize: '22rpx'
                                        }"
                                        :decimals-style="{
                                            fontSize: '25rpx'
                                        }"
                                        :price-data="item.price"
                                    />
                                </view>
                            </view>
                            <view v-show="edit" class="item-btn" @click.stop="">
                                <view class="btn-box">
                                    <up-checkbox :custom-style="{ border: 'none' }" size="20" shape="circle" :name="item.productId" />
                                </view>
                            </view>
                        </view>
                    </template>
                </view>
            </template>
        </up-checkbox-group>

        <template v-if="!loading && historyList.length == 0">
            <view class="empty-box">
                <view class="pictrue"><image :src="staticResource('common/data_empty.png')" /></view>
                <view class="txt">{{ $t("暂无浏览记录！") }}</view>
            </view>
        </template>

        <tig-fixed-placeholder background-color="#fff">
            <view class="footer-box">
                <template v-if="!edit">
                    <view class="footer-text">
                        <view class="footer-num"
                            >{{ $t("共") }}<text class="num">{{ historyList.length }}</text
                            >{{ $t("件商品") }}</view
                        >
                        <view class="footer-text-btn">
                            <tig-button class="btn" @click="edit = true">{{ $t("管理") }}</tig-button>
                        </view>
                    </view>
                </template>
                <template v-else>
                    <view class="footer-edit">
                        <view class="all-check">
                            <tig-checkbox v-model:checked="allChecked" :checked-text="$t('全选')" @change="handleAll" />
                        </view>
                        <view class="edit-btn">
                            <tig-button class="btn" plain @click="edit = false">{{ $t("取消") }}</tig-button>
                            <tig-button class="btn" :disabled="!checkboxValue.length" @click="handleDel">{{ $t("删除") }}</tig-button>
                        </view>
                    </view>
                </template>
            </view>
        </tig-fixed-placeholder>
    </tig-layout>
</template>

<script setup lang="ts">
import { pickPic } from "@/utils/format";
/**
 * 浏览记录页
 * - 展示用户浏览过的商品列表（瀑布流布局）
 * - 支持管理模式：全选、批量删除
 * - 点击商品跳转商品详情
 */
import { historyProductList, delHistoryProduct } from "@/api/user/historyProduct";
import { ref } from "vue";
import type { HistoryProductList } from "@/types/user/historyProduct";
import { staticResource } from "@/utils";
import { useProductStyle } from "@/hooks/useProductStyle";
import { useI18n } from "vue-i18n";

const { t } = useI18n();

const loading = ref(true);
const historyList = ref<HistoryProductList[]>([]);
const { styleMap } = useProductStyle(historyList);
const checkboxValue = ref<number[]>([]);
const edit = ref(false);
const allChecked = ref(false);

const checkboxChange = (val: any) => {
    if (val.length == historyList.value.length) {
        allChecked.value = true;
    } else {
        allChecked.value = false;
    }
};
const handleAll = () => {
    if (allChecked.value) {
        checkboxValue.value = historyList.value.map((item) => item.productId);
        return;
    }
    checkboxValue.value = [];
};

const handleDel = async () => {
    try {
        await delHistoryProduct(checkboxValue.value);
        uni.showToast({
            title: t("删除成功"),
            icon: "none"
        });
        getHistoryProductList();
    } catch (error) {
        console.error(error);
    }
};
const getHistoryProductList = async () => {
    uni.showLoading({
        title: t("加载中")
    });
    try {
        const result = await historyProductList();
        historyList.value = result;
        if (allChecked.value) {
            checkboxValue.value = historyList.value.map((item) => item.productId);
            return;
        }
    } catch (error) {
        console.error(error);
    } finally {
        uni.hideLoading();
        loading.value = false;
    }
};
getHistoryProductList();
const handleLink = (id: number, shopId?: number) => {
    let url = `/pages/product/index?id=${id}`;
    if (shopId && shopId > 0) {
        url += `&shopId=${shopId}`;
    }
    uni.redirectTo({ url });
};
</script>

<style lang="scss" scoped>
.history-product {
    padding: 25rpx 25rpx;
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    grid-gap: 25rpx;

    .history-product-item {
        background-color: #fff;
        border-radius: 20rpx;
        overflow: hidden;
        padding: 20rpx;
        position: relative;

        .item-btn {
            position: absolute;
            width: 100%;
            height: 100%;
            top: 0;
            left: 0;
            z-index: 2;
            .btn-box {
                position: absolute;
                right: 10rpx;
                top: 10rpx;
            }
        }

        .history-product-item-top {
            .img-box {
                position: relative;
                width: 100%;
                aspect-ratio: 1;
                overflow: hidden;

                :deep(.base-image-box),
                :deep(.base-image) {
                    width: 100%;
                    height: 100%;
                }
            }
        }

        .history-product-item-bottom {
            .detail {
                font-weight: bold;
                color: #2a3145;
                font-size: 26rpx;
                line-height: 40rpx;
                height: 80rpx;
                margin-bottom: 10rpx;
            }

            .price {
                color: var(--general);
            }
        }
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

.footer-box {
    height: 100%;

    .footer-text {
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 0 30rpx;

        .num {
            color: var(--general);
            padding: 0 10rpx;
        }
    }

    .footer-edit {
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 0 30rpx;

        .all-check {
            display: flex;
            align-items: center;
        }

        .edit-btn {
            display: flex;
        }
    }

    .btn {
        height: 65rpx;
        width: 160rpx;

        &:first-child {
            margin-right: 15rpx;
        }
    }
}
</style>
