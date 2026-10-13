<template>
    <tig-layout :title="$t('订单评价')" :custom-back="true" @back="handleBack">
        <view v-if="commentEnabled" class="order">
            <view class="order-menu">
                <template v-for="item in menuList" :key="item.type">
                    <view class="order-menu-item" :class="{ active: currentType === item.type }" @click="handleMenuType(item.type)">
                        <uni-badge v-if="item.num > 0" class="badgecolor" :text="item.num" absolute="rightTop" size="small">
                            <text class="order-menu-item-text">{{ item.value }}</text>
                        </uni-badge>
                        <text v-else class="order-menu-item-text">{{ item.value }}</text>
                    </view>
                </template>
            </view>
            <view class="placeholder" />
            <template v-if="currentType < 2">
                <view class="order-list">
                    <template v-for="item in filterState" :key="item.orderId">
                        <view class="order-list-item">
                            <view class="order-list-item-header">
                                <text class="special-text"
                                    >{{ $t("订单编号") }}：<text class="special-color">{{ item.orderSn }}</text>
                                </text>
                            </view>
                            <view class="order-list-item-content">
                                <view class="item-content-product">
                                    <template v-for="subItem in item.items" :key="subItem.productId">
                                        <navigator :url="'/pages/product/index?id=' + subItem.productId" hover-class="navigator-hover">
                                            <view class="item-content-product-item">
                                                <view class="item-content-product-img">
                                                    <tig-image v-if="pickPic(subItem)" :src="pickPic(subItem)" mode="aspectFill" />
                                                </view>
                                                <view class="item-content-product-detail">
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
                                                                :price-data="subItem.price"
                                                            />
                                                            <view class="product-item-quantity">x {{ subItem.quantity }} </view>
                                                        </view>
                                                    </view>
                                                    <view class="sku-card" :class="{ nodata: !hasSkuText(subItem.skuData) }">
                                                        <view v-if="hasSkuText(subItem.skuData)" class="sku-item line1">{{ getSkuText(subItem.skuData) }}</view>
                                                    </view>
                                                </view>
                                            </view>
                                        </navigator>
                                    </template>
                                </view>
                            </view>
                            <view class="order-list-item-btn">
                                <view class="item-btn-box">
                                    <tig-button
                                        :plain="true"
                                        :custom-style="{ height: '55rpx' }"
                                        :plain-main-color="true"
                                        @click="handleEvaluate(item.orderId)"
                                    >
                                        {{ currentType == 0 ? $t("去评价") : $t("待晒单") }}
                                    </tig-button>
                                </view>
                            </view>
                        </view>
                    </template>
                </view>
            </template>
            <template v-else>
                <view class="comment-list">
                    <view v-for="item in filterState" :key="item.orderId" class="comment-item flex">
                        <view class="comment-header">
                            <tig-image v-if="pickPic(item)" class="head" :src="pickPic(item)" mode="aspectFill" />
                        </view>
                        <view class="comment-info">
                            <view class="comment-label flex align-center">
                                <view class="name">{{ $t("评价") }}:</view>
                                <view class="content flex align-center justify-between">
                                    <uni-rate :value="item.commentRank" color="#bfbfbf" :readonly="true" active-color="#ffa800" size="18" />
                                    <view class="time">{{ item.addTime }}</view>
                                </view>
                            </view>
                            <view class="comment-label flex" @click="handleToDetail(item.productId!)">
                                <view class="name">{{ $t("商品名称") }}:</view>
                                <view class="content">
                                    <view class="product-name line1">{{ item.productName || "--" }}</view>
                                </view>
                            </view>
                            <view v-if="item.skuData && item.skuData.length > 0" class="comment-label flex">
                                <view class="name">{{ $t("商品规格") }}:</view>
                                <view class="content flex-wrap">
                                    <view v-for="skuItem in item.skuData" :key="skuItem.value" class="product-tag">{{ skuItem.value }}</view>
                                </view>
                            </view>
                            <view class="comment-label flex">
                                <view class="name">{{ $t("评价标签") }}:</view>
                                <view class="content flex-wrap">
                                    <view v-for="tag in item.commentTag" :key="tag" class="product-tag">{{ tag }}</view>
                                </view>
                            </view>
                            <view class="comment-label flex">
                                <view class="name">{{ $t("评价内容") }}:</view>
                                <view class="content">
                                    <view class="cont">{{ item.content }}</view>
                                </view>
                            </view>
                            <view v-if="item.replies && item.replies.length > 0" class="reply-section">
                                <view class="reply-card">
                                    <view :id="'reply-m-' + item.commentId" class="reply-measure">
                                        <text>{{ $t("客服回复") }}：{{ item.replies[0]?.content }}</text>
                                    </view>
                                    <view
                                        :id="'reply-b-' + item.commentId"
                                        class="reply-body"
                                        :class="{ 'is-collapsed': !expandedReplies.has(item.commentId!) }"
                                    >
                                        <text class="reply-label">{{ $t("客服回复") }}：</text>
                                        <text class="reply-text">{{ item.replies[0]?.content }}</text>
                                        <text
                                            v-if="overflowReplies.has(item.commentId!) && expandedReplies.has(item.commentId!)"
                                            class="reply-toggle"
                                            @click.stop="toggleReplies(item.commentId!)"
                                            >{{ $t("收起") }}</text
                                        >
                                    </view>
                                    <view
                                        v-if="overflowReplies.has(item.commentId!) && !expandedReplies.has(item.commentId!)"
                                        class="reply-expand"
                                        @click.stop="toggleReplies(item.commentId!)"
                                    >
                                        <text class="reply-expand-text">...</text> {{ $t("展开") }}</view
                                    >
                                </view>
                            </view>
                            <view class="comment-label flex">
                                <view class="name">{{ $t("晒单") }}:</view>
                                <view v-if="item.showPics?.length > 0" class="content flex-wrap">
                                    <view
                                        v-for="product in item.showPics"
                                        :key="product.picUrl"
                                        class="product-pic"
                                        @click="imagePreview(imageFormat(product.picUrl))"
                                    >
                                        <tig-image :src="pickPic(product)" mode="aspectFill" />
                                    </view>
                                </view>
                            </view>
                        </view>
                    </view>
                </view>
            </template>
            <view v-if="!isLoading && filterState.length == 0" class="empty-box">
                <view class="pictrue">
                    <image :src="staticResource('common/data_empty.png')" />
                </view>
                <view class="txt">{{ $t("暂无评价") }}</view>
            </view>
            <loading-box v-model="loaded" :page="filterParams.page" :length="filterState.length" />
        </view>
        <empty-box v-else center-content height="calc(100vh - var(--nav-height) - var(--safe-bottom, 0px))" mode="list" :text="$t('评价功能已关闭')" />
    </tig-layout>
</template>

<script setup lang="ts">
/**
 * 评价管理列表页
 * - 三个Tab切换：待评价/待晒单/已评价
 * - 待评价：已完成订单未评价的商品
 * - 待晒单：已评价但未上传图片的订单
 * - 已评价：展示已发表的评价和商家回复
 */
import { computed, reactive, ref, nextTick } from "vue";
import { getOrderList } from "@/api/user/order";
import { getShowedList, getCommentList, getCommentSubNum } from "@/api/user/comment";
import type { CommentFilterState, CommentFilterParams } from "@/types/user/comment.d";
import { onLoad, onReachBottom, onUnload } from "@dcloudio/uni-app";
import { imageFormat, pickPic } from "@/utils/format";
import { staticResource, redirect, getSkuText, hasSkuText } from "@/utils";
import { useI18n } from "vue-i18n";
import { useConfigStore } from "@/store/config";

const { t } = useI18n();
const configStore = useConfigStore();
const commentEnabled = computed(() => configStore.productCommentEnabled === 1);

interface MenuType {
    type: string;
    value: string;
    num: number;
}
const menuList = reactive<any>([
    { type: 0, value: t("待评价"), num: 0 },
    { type: 1, value: t("待晒单"), num: 0 },
    { type: 2, value: t("已评价"), num: 0 }
]);
const imagePreview = (url: string) => {
    uni.previewImage({
        urls: [url]
    });
};
const getSubNum = async () => {
    try {
        const result = await getCommentSubNum();
        menuList[0].size = result.awaitComment;
        menuList[1].size = result.showPics;
        menuList[2].size = result.commented;
    } catch (error: any) {
        uni.showToast({
            title: error.message,
            icon: "none",
            duration: 1000
        });
    }
};
const expandedReplies = ref<Set<number>>(new Set());
const overflowReplies = ref<Set<number>>(new Set());

const toggleReplies = (commentId: number) => {
    const next = new Set(expandedReplies.value);
    if (next.has(commentId)) {
        next.delete(commentId);
    } else {
        next.add(commentId);
    }
    expandedReplies.value = next;
};

const checkReplyOverflow = () => {
    nextTick(() => {
        setTimeout(() => {
            const items = filterState.value.filter((item) => item.replies?.length && item.commentId);
            if (items.length === 0) return;
            const newSet = new Set<number>(overflowReplies.value);
            let count = 0;
            items.forEach((item) => {
                const id = item.commentId!;
                const query = uni.createSelectorQuery();
                query.select(`#reply-m-${id}`).boundingClientRect();
                query.select(`#reply-b-${id}`).boundingClientRect();
                query.exec((res: any[]) => {
                    const measureRect = res[0];
                    const bodyRect = res[1];
                    if (measureRect && bodyRect && measureRect.height > bodyRect.height + 1) {
                        newSet.add(id);
                    }
                    count++;
                    if (count === items.length) {
                        overflowReplies.value = newSet;
                    }
                });
            });
        }, 200);
    });
};

const currentType = ref<number>(0);
const filterParams = reactive<CommentFilterParams>({
    page: 1,
    size: 10
});
const handleToDetail = (id: number) => {
    redirect({
        url: `/pages/product/index?id=${id}`
    });
};
const loaded = ref(false);
const isLoading = ref(false);
const filterState = ref<CommentFilterState[]>([]);
const total = ref(0);
const handleMenuType = (type: number) => {
    console.log("type", type);
    currentType.value = type;
    filterState.value = [];
    filterParams.page = 1;
    loadFilter();
};
/**
 * 加载列表数据
 * - type=0 调用订单接口获取待评价订单
 * - type=1 调用晒单接口获取待晒单订单
 * - type=2 调用评论接口获取已评价记录
 */
const loadFilter = async () => {
    if (filterParams.page > 1) {
        loaded.value = true;
    } else {
        isLoading.value = true;
        uni.showLoading({
            title: t("加载中")
        });
    }
    try {
        let result: any = [];
        // 三种类型对应不同的接口
        if (currentType.value == 0) {
            result = await getOrderList({ ...filterParams, commentStatus: 0 });
        }
        if (currentType.value == 1) {
            result = await getShowedList({ ...filterParams });
        }
        if (currentType.value == 2) {
            result = await getCommentList({ ...filterParams });
        }
        filterState.value = [...filterState.value, ...result.records];
        total.value = result.total;
    } catch (error: any) {
        uni.showToast({
            title: error.message,
            icon: "none",
            duration: 1000
        });
    } finally {
        uni.hideLoading();
        isLoading.value = false;
        loaded.value = false;
        if (currentType.value === 2) {
            checkReplyOverflow();
        }
    }
};

const handleEvaluate = (id: number) => {
    uni.navigateTo({
        url: `/pages/user/comment/info?id=${id}`
    });
};

const handleBack = () => {
    redirect({
        url: "/pages/member/index"
    });
};

onLoad(async (options) => {
    // 读取初始化后的配置后再请求评价数据
    await configStore.whenConfigReady();
    if (!commentEnabled.value) {
        isLoading.value = false;
        return;
    }
    if (options && options.currentType) {
        currentType.value = Number(options.currentType);
    }
    uni.$on("commentFinished", (data) => {
        if (data) {
            currentType.value = 2;
            filterState.value = [];
            filterParams.page = 1;
            loadFilter();
        }
    });
    getSubNum();
    loadFilter();
});

onReachBottom(() => {
    if (filterParams.page < Math.ceil(total.value / filterParams.size)) {
        filterParams.page++;
        loadFilter();
    }
});

// 关闭预览图片
onUnload(() => {
    // #ifdef  H5 || APP
    uni.closePreviewImage();
    // #endif
    // 页面卸载时移除监听器
    uni.$off("commentFinished");
});
</script>

<style lang="scss" scoped>
.placeholder {
    height: 100rpx;
}

.badgecolor {
    :deep(.uni-badge--error) {
        background-color: var(--general);
    }
}

.order-menu {
    background-color: #fff;
    display: flex;
    box-sizing: border-box;
    margin-bottom: 30rpx;
    position: fixed;
    width: 100%;
    z-index: 88;

    .order-menu-item {
        flex: 1;
        display: flex;
        align-items: center;
        justify-content: center;
        height: 88rpx;
        position: relative;

        .order-menu-item-text {
            padding: 0 10rpx 0 0;
        }

        &.active {
            color: #333;
            font-weight: bold;

            &:after {
                content: "";
                position: absolute;
                bottom: 0;
                height: 5rpx;
                width: 60rpx;
                background-color: var(--general);
                border-radius: 10rpx;
            }
        }
    }
}

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
                    flex-shrink: 0;
                    overflow: hidden;
                    border-radius: 10rpx;
                }

                .item-content-product-detail {
                    flex: 1;
                    min-width: 0;
                }

                .item-content-product-info {
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
                .sku-card {
                    margin-left: 20rpx;
                    margin-top: 10rpx;
                    min-width: 0;
                    max-width: calc(100% - 20rpx);
                    box-sizing: border-box;

                    .sku-item {
                        min-width: 0;
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

.comment-list {
    padding: 0 20rpx;

    .comment-item {
        padding: 20rpx;
        background-color: #fff;
        border-radius: 20rpx;
        margin-bottom: 20rpx;
    }

    .comment-header {
        width: 120rpx;
        height: 120rpx;
        margin-right: 20rpx;
        flex-shrink: 0;
        overflow: hidden;
        border-radius: 20rpx;

        .head {
            width: 120rpx;
            height: 120rpx;
            border-radius: 20rpx;
        }
    }

    .comment-info {
        flex: 1;
        min-width: 0;
        font-size: 24rpx;
        color: #333;

        .reply-section {
            margin-bottom: 15rpx;
        }

        .reply-card {
            position: relative;
            background-color: #f7f7f7;
            border-radius: 12rpx;
            padding: 20rpx 24rpx;
            margin-bottom: 10rpx;
            font-size: 24rpx;
            line-height: 1.6;

            .reply-measure {
                position: absolute;
                left: 24rpx;
                right: 24rpx;
                visibility: hidden;
                pointer-events: none;
            }

            .reply-body {
                word-wrap: break-word;

                &.is-collapsed {
                    max-height: 1.6em;
                    overflow: hidden;
                }
            }

            .reply-label {
                color: #999;
            }

            .reply-text {
                word-wrap: break-word;
            }

            .reply-toggle {
                color: #000;
                margin-left: 10rpx;
            }

            .reply-expand {
                position: absolute;
                right: 24rpx;
                bottom: 20rpx;
                color: #000;
                background: #f7f7f7;
                padding-left: 0;
                .reply-expand-text {
                    color: #333;
                }
            }
        }

        .comment-label {
            margin-bottom: 15rpx;
            align-items: center;

            .name {
                margin-right: 20rpx;
            }

            .content {
                flex: 1;
                min-width: 0;

                &.flex {
                    flex-wrap: nowrap;
                }

                .time {
                    color: #999;
                    font-size: 22rpx;
                    flex-shrink: 0;
                    white-space: nowrap;
                    margin-left: 12rpx;
                }

                .product-name {
                    width: 100%;
                    color: var(--tag-text);
                }

                .product-tag {
                    background-color: #f5f5f5;
                    border-radius: 5rpx;
                    padding: 5rpx 10rpx;
                    margin: 0 0 5rpx 5rpx;
                    text-align: center;
                    flex-shrink: 0;
                    white-space: nowrap;
                }

                .cont {
                    word-wrap: break-word;
                }

                .product-pic {
                    width: 80rpx;
                    height: 80rpx;
                    border-radius: 10rpx;
                    overflow: hidden;
                    margin-right: 10rpx;
                    margin-bottom: 10rpx;
                }
            }
        }
    }
}
</style>
