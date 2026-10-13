<template>
    <view>
        <tig-layout ref="layoutRef" :title="pageTitle">
            <template v-if="!failLoad">
                <view v-show="!loadEndStatus" class="groupon-info-container">
                    <view class="card-box product-card">
                        <view class="image" @click="toProductPage(product.productId)">
                            <tig-image :src="product.picThumb" mode="heightFix" />
                        </view>
                        <view class="detail">
                            <view class="header" @click="toProductPage(product.productId)">
                                <view class="line2">
                                    <text class="detail-title">{{ grouponRecordDetail.productName }}</text>
                                </view>
                                <view class="detail-group">
                                    <text class="btn">{{ grouponRecordDetail.minGroupNum }}{{ $t("人团") }}</text>
                                    <text class="num">{{ $t("已有") + grouponRecordDetail.joinNumCount + $t("人参团") }}</text>
                                </view>
                            </view>
                            <view class="flex">
                                <view class="group-price">
                                    <text class="price-text">{{ $t("拼团价") }}:</text>
                                    <format-price
                                        :currency-style="{
                                            fontSize: '23rpx'
                                        }"
                                        :price-data="grouponRecordDetail.grouponPrice"
                                    />
                                </view>
                                <view class="original-price">
                                    <format-price
                                        :font-style="{ 'text-decoration': 'line-through' }"
                                        :currency-style="{
                                            fontSize: '23rpx'
                                        }"
                                        :price-data="grouponRecordDetail.productPrice"
                                    />
                                </view>
                            </view>
                        </view>
                    </view>
                    <view class="card-box group-card">
                        <view class="group-sub-title">
                            <view v-if="grouponRecordDetail.joinStatus === 1" class="text">
                                <text>{{ $t("还差") }}</text>
                                <text class="num">{{
                                    joinUsers.length > grouponRecordDetail.minGroupNum ? 0 : grouponRecordDetail.minGroupNum - joinUsers.length
                                }}</text>
                                <text>{{ $t("人成团") }}</text>
                            </view>
                            <view v-if="grouponRecordDetail.joinStatus === 2" class="flex align-center">
                                <text class="text">{{ $t("拼团成功") }}</text>
                                <text class="msg">{{ $t("商家会尽快发货") }}</text>
                            </view>
                            <view v-if="grouponRecordDetail.joinStatus === 3" class="flex align-center">
                                <text class="text">{{ $t("拼团失败") }}</text>
                                <text class="msg">{{ $t("超时未成团，系统将原路退款") }}</text>
                            </view>
                            <view v-if="!isGroupEnd" class="group-end-info flex align-center">
                                <view class="countdown-title">{{ $t("距结束") }}</view>
                                <tig-countdown
                                    style="font-size: 24rpx"
                                    background-color="var(--primary-light-9)"
                                    splitor-color="#1A1A1A"
                                    color="var(--ump-price)"
                                    :end-time="grouponRecordDetail.endTime"
                                    :font-size="11"
                                />
                            </view>
                        </view>
                        <scroll-view class="join-users" scroll-x="true" show-scrollbar="true" enable-flex="true">
                            <view class="userinfo-container">
                                <view v-for="(item, index) in joinUsers" :key="item.userId || index" class="userinfo">
                                    <view class="userinfo-avatar">
                                        <view class="image">
                                            <tig-image :src="imageFormat(item?.avatar || configStore.defaultAvatar)" />
                                        </view>
                                        <view v-if="item.leader" class="image-leader">{{ $t("团长") }}</view>
                                    </view>
                                    <text class="name">
                                        {{ hideUserName(item.username || "") }}
                                    </text>
                                </view>
                                <view
                                    v-if="grouponRecordDetail.joinStatus === 1 && grouponRecordDetail.joinNumCount < grouponRecordDetail.minGroupNum"
                                    class="userinfo"
                                    @click="handleShare"
                                >
                                    <view class="userinfo-avatar">
                                        <view class="image">
                                            <view class="btn-add">
                                                <view class="iconfont-h5 icon-zengjia" />
                                            </view>
                                        </view>
                                    </view>
                                    <text class="name">
                                        {{ $t("待加入") }}
                                    </text>
                                </view>
                            </view>
                        </scroll-view>
                    </view>
                    <view class="card-box process-card">
                        <view class="sub-title">
                            <text>{{ $t("拼团流程") }}</text>
                        </view>
                        <view class="process">
                            <view class="process-item">
                                <image class="process-image" :src="staticResource('groupon/groupon_open.png')" />
                                <view class="step-name"
                                    ><text>{{ $t("1.开团/参加") }}</text></view
                                >
                            </view>
                            <view class="process-line">
                                <view v-for="i in 3" :key="i" class="process-line-item" />
                            </view>
                            <view class="process-item">
                                <image class="process-image" :src="staticResource('groupon/groupon_invite.png')" />
                                <view class="step-name"
                                    ><text>{{ $t("2.邀友参团") }}</text></view
                                >
                            </view>
                            <view class="process-line">
                                <view v-for="i in 3" :key="i" class="process-line-item" />
                            </view>
                            <view class="process-item">
                                <image class="process-image" :src="staticResource('groupon/groupon_success.png')" />
                                <view class="step-name"
                                    ><text>{{ $t("3.人满成团") }}</text></view
                                >
                            </view>
                        </view>
                    </view>
                    <view v-if="shopInfo.shopId" class="card-box store-card">
                        <view class="shop-info">
                            <tig-image class="shop-image" :src="imageFormat(shopInfo.shopLogo || '')" />
                            <text class="shop-title">{{ shopInfo.shopTitle }}</text>
                        </view>
                        <view class="store">
                            <view class="btn" @click="handleShopCollection"
                                ><text>{{ collectText }}</text></view
                            >
                            <view class="btn" @click="handleToShop">
                                <image :src="staticResource('product/shop.png')" class="img" />
                                <text>{{ $t("进店逛逛") }}</text>
                            </view>
                        </view>
                    </view>
                    <view class="footer">
                        <view :style="{ 'padding-bottom': configStore.safeBottom + 'rpx' }" class="bottom-bar-box">
                            <view class="bottom-bar flex align-center">
                                <view class="icon-box">
                                    <view class="label my-groupon" @click.stop="toPage('/pages/groupon/mylist')">
                                        <view>
                                            <image :src="staticResource('groupon/groupon.png')" class="img" />
                                        </view>
                                        <view class="label-text">
                                            <text class="my-group-text">{{ $t("我的拼团") }}</text>
                                        </view>
                                    </view>
                                    <template v-if="shopInfo.shopId">
                                        <view class="label" @click="handleToShop">
                                            <view>
                                                <image :src="staticResource('product/shop.png')" class="img" />
                                            </view>
                                            <view class="label-text">
                                                <text>{{ $t("门店") }}</text>
                                            </view>
                                        </view>
                                    </template>
                                </view>
                                <view class="btn-box">
                                    <template v-if="product.productStatus === 0 || productStock === 0">
                                        <view class="other-products-btn" @click="handleToProductList">{{ $t("查看其他商品") }} </view>
                                    </template>
                                    <template v-else>
                                        <template v-if="product.productType === 1 || grouponRecordDetail.joinStatus === 1">
                                            <!-- 待成团 -->
                                            <template v-if="grouponRecordDetail.joinStatus === 1">
                                                <!-- 参与类型是参团 -->
                                                <view v-if="!isUserJoined" class="other-products-btn" @click="hanldeJoinGroup">
                                                    {{ $t("去参团") }}
                                                </view>
                                                <!-- 参与类型是开团 -->
                                                <view v-else class="other-products-btn" @click="handleShare">
                                                    {{ $t("邀请好友参团") }}
                                                </view>
                                            </template>
                                            <!-- 拼团成功 -->
                                            <template v-else-if="grouponRecordDetail.joinStatus === 2">
                                                <view class="other-products-btn" @click="toMoreGroupon">
                                                    {{ $t("更多拼团") }}
                                                </view>
                                            </template>
                                            <!-- 拼团失败 -->
                                            <template v-else-if="grouponRecordDetail.joinStatus === 3">
                                                <view class="other-products-btn" @click="showSpecification">
                                                    {{ $t("原价购买") }}
                                                </view>
                                            </template>
                                        </template>
                                        <template v-else>
                                            <view class="other-products-btn" @click="showSpecification">{{ $t("立即购买") }}</view>
                                        </template>
                                    </template>
                                </view>
                                <template v-if="product.productStatus === 0 || productStock === 0">
                                    <view class="product-status">
                                        {{ $t(product.productStatus === 0 ? "商品已下架" : "商品已售罄") }}
                                    </view>
                                </template>
                            </view>
                        </view>
                    </view>
                    <grouponShare
                        v-model="showShare"
                        :product-id="Number(productId)"
                        :product-info="product"
                        :product-price="productPrice"
                        :groupon-promotion-id="grouponRecordDetail.grouponPromotionId"
                        :groupon-promotion-record-id="grouponPromotionRecordId"
                    />
                    <specification
                        ref="specificationRef"
                        v-model="specificationId"
                        v-model:sku-id="skuId"
                        :groupon-enable="grouponEnable"
                        :groupon-record-info="grouponRecordInfo"
                        @load-end="getLoadStatus"
                        @send-value="getValue"
                    />
                </view>
            </template>
            <template v-else>
                <error />
                <recommend title-bg="#fff" />
            </template>
        </tig-layout>
        <tig-loadingpage v-model="loadEndStatus" />
    </view>
</template>
<script setup lang="ts">
/**
 * 拼团详情页
 * - 展示拼团活动信息和参团成员
 * - 倒计时显示拼团剩余时间
 * - 支持分享邀请好友参团
 * - 显示拼团状态（进行中/成功/失败）
 */
import { computed, ref, nextTick } from "vue";
import { onLoad, onShareAppMessage, onShareTimeline } from "@dcloudio/uni-app";
import { useI18n } from "vue-i18n";
import { useUserStore } from "@/store/user";
import { useConfigStore } from "@/store/config";
import { redirect, staticResource } from "@/utils";
import type { ProductItem } from "@/types/product/product";
import type { ShopDetailItem } from "@/types/shop/shop";
import type { GrouponRecordDetail, GrouponJoinUser } from "@/types/groupon/groupon";
import { imageFormat, hideUserName } from "@/utils/format";
import { shopCollection, getShopDetail } from "@/api/shop/shop";
import { getProductDetail } from "@/api/product/product";
import { getGrouponRecordDetail } from "@/api/groupon/groupon";
import specification from "@/components/product/specificationV1.vue";
import grouponShare from "@/pages/groupon/src/grouponShare.vue";
import error from "./src/error.vue";
import recommend from "@/components/recommend/index.vue";
import { useRegionStore } from "@/store/region";

const { t } = useI18n();
const userStore = useUserStore();
const pageTitle = ref(t("拼团"));
const configStore = useConfigStore();
const shopInfo = ref<ShopDetailItem>({} as ShopDetailItem);
const grouponPromotionRecordId = ref(0);
const grouponRecordDetail = ref<GrouponRecordDetail>({} as GrouponRecordDetail);
const product = ref<ProductItem>({
    productId: 0,
    productStock: 0
});
const productStock = ref(10);
const specificationId = ref(0);
const skuId = ref(0);
const grouponEnable = ref(true);
const specificationRef = ref();
const grouponRecordInfo = ref<{
    grouponPromotionRecordId: number;
    minGroupNum: number;
    skuId: number;
}>({
    grouponPromotionRecordId: 0,
    minGroupNum: 0,
    skuId: 0
});
const user = userStore.userInfo;
const productId = ref(0);
const showShare = ref(false);
const layoutRef = ref();
const loadEndStatus = ref(true);
const failLoad = ref(false);
const productPrice = ref<string>("0");
const productOriginPrice = ref<string | null>("0");
const productNumber = ref<number>(1);
const isCollect = ref(false);
const joinUsers = ref<GrouponJoinUser[]>([]);

interface Ival {
    productOriginPrice: string | null;
    productPrice: string;
    productStock: number;
    productNumber: number;
    productSkuId?: number;
    seckillValue: any;
    discountValue: any;
    discountEndTime: string;
}
const getValue = (val: Ival) => {
    productStock.value = val.productStock;
    productPrice.value = val.productPrice;
    productOriginPrice.value = val.productOriginPrice;
    productNumber.value = val.productNumber;
};

const getLoadStatus = () => {
    loadEndStatus.value = false;
};

const __getProductDetail = async (id: number) => {
    try {
        let result: any = {};
        result = await getProductDetail(id);
        productStock.value = result.item.productStock;
        productPrice.value = result.item.productPrice || "0";
        specificationId.value = result.item.productId;
        productId.value = result.item.productId;
        product.value = result.item;

        if (result.item.shopId) {
            getShopInfo(result.item.shopId);
        }
        nextTick(() => {
            layoutRef.value?.handleLog();
        });
    } catch (error: any) {
        console.error(error);
        failLoad.value = true;
        loadEndStatus.value = false;
    }
};

/**
 * 当前用户是否参过团
 */
const isUserJoined = computed(() => {
    if (!grouponRecordDetail.value) {
        return false;
    }
    return grouponRecordDetail.value.joinUsers.some((item) => item.userId === user.userId);
});

/**
 * 团是否结束了
 */
const isGroupEnd = computed(() => {
    // 不是待成团的状态，拼团结束了
    if (grouponRecordDetail.value.joinStatus !== 1) {
        return true;
    }
    if (!grouponRecordDetail.value.endTime) {
        return true;
    }
    const endTime = dateStringToTimestamp(grouponRecordDetail.value.endTime);
    return endTime < new Date().getTime();
});

/*时间字符串转时间戳 */
const dateStringToTimestamp = (dateString: any) => {
    const dateParts = dateString.split(" ");
    const [year, month, day] = dateParts[0].split("-");
    const [hour, minute, second] = dateParts[1].split(":");
    const date = new Date(year, month - 1, day, hour, minute, second);
    return date.getTime();
};

// 获取店铺信息
const getShopInfo = async (id: number) => {
    try {
        const result = await getShopDetail(id);
        shopInfo.value = result;
        isCollect.value = result.collectShop;
    } catch (error: any) {
        console.error(error);
    }
};

const imagePreview = (url: string) => {
    uni.previewImage({
        urls: [url]
    });
};

const toProductPage = (id: number) => {
    redirect({
        url: `/pages/product/index?id=${id}`
    });
};

// 跳转
const toPage = (url: string) => {
    redirect({ url });
};

const regionStore = useRegionStore();

// 跳转店铺
const handleToShop = () => {
    if (shopInfo.value.shopId) {
        uni.navigateTo({
            url: `/pages/store/index?id=${shopInfo.value.shopId}&latitude=${regionStore.currentLocationInfo.latitude}&longitude=${regionStore.currentLocationInfo.longitude}`
        });
    }
};

/**
 * 加入拼团
 * @param item
 */
const hanldeJoinGroup = () => {
    grouponRecordInfo.value.grouponPromotionRecordId = grouponRecordDetail.value.grouponPromotionRecordId;
    grouponRecordInfo.value.minGroupNum = grouponRecordDetail.value.minGroupNum;
    grouponRecordInfo.value.skuId = grouponRecordDetail.value.skuId;
    grouponEnable.value = true;
    specificationRef.value.handleShowPopup();
};

const collectText = computed(() => {
    return isCollect.value ? t("取消收藏") : t("收藏门店");
});

/**
 * 处理店铺收藏
 */
const handleShopCollection = async () => {
    await shopCollection({ shopId: shopInfo.value.shopId });
    uni.showToast({
        title: isCollect.value ? t("取消收藏成功") : t("收藏成功"),
        icon: "none"
    });
    await getShopInfo(shopInfo.value.shopId);
};

/**
 * 处理分享
 */
const handleShare = () => {
    if (!userStore.token) {
        uni.navigateTo({
            url: "/pages/login/index"
        });
        return;
    }
    showShare.value = true;
};

// 拿到拼团记录详情
const getDetail = async () => {
    try {
        const result = await getGrouponRecordDetail(grouponPromotionRecordId.value);
        grouponRecordDetail.value = result;
        joinUsers.value = result.joinUsers.sort((a, b) => (b.leader ? 1 : -1));
        switch (result.joinStatus) {
            case 2:
                pageTitle.value = t("拼团成功");
                break;
            case 3:
                pageTitle.value = t("拼团失败");
                break;
            case 1:
            default:
                pageTitle.value = t("拼团");
        }
        __getProductDetail(result.productId);
        skuId.value = result.skuId;
        // 如果有商铺id
        if (result.shopId) {
            await getShopInfo(result.shopId);
        }
    } catch (error: any) {
        failLoad.value = true;
        loadEndStatus.value = false;
        console.error(error);
    }
};

const showSpecification = () => {
    if (specificationRef.value) {
        grouponEnable.value = false;
        specificationRef.value.handleShowPopup();
    }
};

/**
 * 查看其他商品
 */
const handleToProductList = () => {
    uni.navigateTo({ url: "/pages/search/result" });
};

/**
 * 查看更多拼团
 */
const toMoreGroupon = () => {
    redirect({ url: `/pages/groupon/list?id=${grouponRecordDetail.value.grouponPromotionId}`, mode: "redirectTo" });
};

// 页面加载
onLoad((option) => {
    if (option?.grouponPromotionRecordId) {
        grouponPromotionRecordId.value = Number(option.grouponPromotionRecordId);
        getDetail();
    } else {
        failLoad.value = true;
        loadEndStatus.value = false;
    }
});

onShareAppMessage(() => {
    return {
        title: grouponRecordDetail.value.productName || product.value.productName || t("拼团"),
        path: `/pages/product/groupBuying?grouponPromotionRecordId=${grouponPromotionRecordId.value}`,
        imageUrl: imageFormat(product.value.picUrl || product.value.picThumb || "")
    };
});

onShareTimeline(() => {
    return {
        title: grouponRecordDetail.value.productName || product.value.productName || t("拼团"),
        query: `grouponPromotionRecordId=${grouponPromotionRecordId.value}`,
        imageUrl: imageFormat(product.value.picUrl || product.value.picThumb || "")
    };
});
</script>

<style scoped lang="scss">
.groupon-info-container {
    display: flex;
    flex-direction: column;
    gap: 20rpx;
    padding: 20rpx;

    .card-box {
        flex: 1;
        border-radius: 8rpx;
        background-color: #fff;
        padding: 0 20rpx;
    }
}

.product-card {
    display: flex;
    gap: 12rpx;
    padding: 20rpx 50rpx 20rpx 20rpx !important;

    .image {
        width: 200rpx;
        height: 200rpx;
        border-radius: 8rpx;
        flex-shrink: 0;
        overflow: hidden;
    }

    .detail {
        display: flex;
        flex-direction: column;
        justify-content: space-between;

        .header {
            display: flex;
            flex-direction: column;
            gap: 20rpx 0;
        }

        .detail-title {
            width: 423rpx;
            height: 74rpx;
            font-weight: bold;
            font-size: 30rpx;
            color: #2a2a2a;
            text-overflow: ellipsis;
        }

        .detail-group {
            display: flex;
            align-items: baseline;
            gap: 0 10rpx;

            .btn {
                text-align: center;
                padding: 4rpx 9rpx;
                background: rgba(255, 226, 225, 0);
                border-radius: 4rpx;
                border: 1px solid var(--primary-light-3);
                font-weight: 500;
                font-size: 24rpx;
                line-height: 24rpx;
                color: var(--primary-light-3);
            }

            .num {
                height: 23rpx;
                font-weight: 500;
                font-size: 24rpx;
                color: #9a9a9a;
            }
        }

        .group-price {
            font-weight: bold;
            font-size: 24rpx;
            color: var(--price);

            .price-text {
                margin-right: 11rpx;
            }
        }

        .original-price {
            font-size: 24rpx;
            color: #9a9a9a;
            margin-left: 16rpx;
        }
    }
}

.group-card {
    padding: 0 26rpx !important;

    .group-sub-title {
        display: flex;
        justify-content: space-between;
        border-bottom: 2rpx solid #f4f4f4;
        padding: 28rpx 0;

        .text {
            font-weight: 800;
            font-size: 32rpx;
            color: #2a2a2a;
        }

        .msg {
            font-size: 28rpx;
            color: #9a9a9a;
            margin-left: 26rpx;
        }

        .group-end-info {
            display: flex;
        }

        .countdown-title {
            font-size: 24rpx;
            font-weight: bold;
            color: #1a1a1a;
            margin-right: 15rpx;
        }

        .num {
            color: var(--main-bg);
        }

        .common {
            display: inline-block;
            width: 36rpx;
            height: 36rpx;
            text-align: center;
            line-height: 36rpx;
            background: rgba(238, 10, 36, 0.07);
            border-radius: 6rpx;
            font-weight: bold;
            font-size: 22rpx;
            color: var(--primary-light-3);
        }
    }

    .join-users {
        padding: 33rpx 0;
        white-space: nowrap;
        width: 100%;

        /* #ifdef H5 || APP-PLUS */
        /* 尽量让 H5/App(WebView) 可见滚动条；是否“常驻”仍受系统/浏览器策略影响 */
        padding-bottom: 14rpx;
        /* scroll-view 真正的滚动容器是内部的 .uni-scroll-view，scoped 下需 deep */
        ::v-deep .uni-scroll-view {
            scrollbar-width: thin;
            scrollbar-color: rgba(0, 0, 0, 0.25) transparent;
            padding-bottom: 13rpx;
        }
        ::v-deep .uni-scroll-view::-webkit-scrollbar {
            height: 6px;
        }
        ::v-deep .uni-scroll-view::-webkit-scrollbar-thumb {
            background: rgba(0, 0, 0, 0.25);
            border-radius: 999px;
        }
        ::v-deep .uni-scroll-view::-webkit-scrollbar-track {
            background: transparent;
        }
        /* #endif */

        .userinfo-container {
            display: inline-flex;
            gap: 0 43rpx;
            padding-right: 20rpx;
        }

        .userinfo {
            display: flex;
            flex-direction: column;
            align-items: center;
            gap: 23rpx 0;

            .userinfo-avatar {
                position: relative;

                .image {
                    width: 100rpx;
                    height: 100rpx;
                    background: #f4f4f4;
                    border-radius: 50%;
                    overflow: hidden;
                    position: relative;

                    .btn-add {
                        width: 100%;
                        height: 100%;
                        display: flex;
                        align-items: center;
                        justify-content: center;

                        .icon-zengjia {
                            font-size: 36rpx;
                            color: #c3c3c3;
                        }
                    }
                }

                .image-leader {
                    position: absolute;
                    bottom: 0;
                    left: 50%;
                    transform: translateX(-50%) translateY(40%);
                    background-color: var(--main-bg);
                    color: #fff;
                    border-radius: 3rpx;
                    padding: 3rpx 8rpx;
                    white-space: nowrap;
                    font-size: 18rpx;
                    z-index: 99;
                }
            }

            .name {
                max-width: 100rpx;
                font-weight: 500;
                font-size: 24rpx;
                line-height: 24rpx;
                text-align: center;
                color: #9a9a9a;
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
            }

            &:first-child .image {
                border: 2rpx solid var(--main-bg);
            }
        }
    }
}

.process-card {
    padding: 0 26rpx !important;

    .sub-title {
        font-weight: bold;
        font-size: 28rpx;
        color: #2a2a2a;
        padding: 28rpx 0;
    }

    .process {
        display: flex;
        justify-content: space-between;
        padding-bottom: 46rpx;

        .process-item {
            display: flex;
            flex-direction: column;
            align-items: center;
            gap: 22rpx 0;
            position: relative;

            .step-name {
                font-weight: 400;
                font-size: 24rpx;
                color: #595959;
                line-height: 38rpx;
            }

            .process-image {
                width: 64rpx;
                height: 64rpx;
                background: #f5f5f5;
                border-radius: 50%;
            }
        }

        .process-line {
            display: flex;
            gap: 13rpx;
            padding-top: 27rpx;

            .process-line-item {
                display: block;
                width: 10rpx;
                height: 10rpx;
                border-radius: 50%;
                background: #f5f5f5;
            }
        }
    }
}

.store-card {
    padding: 28rpx 26rpx !important;
    padding-bottom: 32rpx;
    flex-direction: column;

    .shop-info {
        display: flex;
        gap: 20rpx;
        align-items: center;

        .shop-image {
            width: 64rpx;
            height: 64rpx;
            background: #f5f5f5;
            border-radius: 50%;
            overflow: hidden;
        }

        .shop-title {
            font-size: 26rpx;
        }
    }

    .store {
        display: flex;
        justify-content: space-between;
        margin-top: 20rpx;

        .img {
            width: 30rpx;
            height: 30rpx;
            margin-right: 10rpx;
        }

        .btn {
            width: 46%;
            display: flex;
            align-items: center;
            justify-content: center;
            border: 1px solid #d7d7d7;
            border-radius: 5px;
            height: 56rpx;
            line-height: 22rpx;
            font-size: 22rpx;
            color: #686868;
        }
    }
}

.footer {
    width: 750rpx;
    background: #ffffff;
    box-shadow: 0rpx -4rpx 6rpx 0rpx rgba(234, 234, 234, 0.55);
}

.bottom-bar-box {
    position: fixed;
    bottom: 0;
    left: 0;
    width: 100%;
    height: 110rpx;
    background-color: #fff;
    border-top: 1rpx solid #f5f5f5;
    box-sizing: content-box;

    .bottom-bar {
        height: 110rpx;
        padding: 0 20rpx;
        position: relative;
        justify-content: center;

        .product-status {
            position: absolute;
            top: -60rpx;
            left: 0;
            width: 100%;
            height: 60rpx;
            background-color: #fff7cc;
            color: #f85;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 24rpx;
        }

        .icon-box {
            display: flex;
            align-items: center;
        }

        .btn-box {
            display: flex;
            padding-left: 20rpx;
            flex: 1;

            .other-products-btn {
                flex: 1;
                font-weight: bold;
                color: var(--main-text);
                background-color: var(--main-bg);
                height: 70rpx;
                border-radius: 100rpx;
                display: flex;
                align-items: center;
                justify-content: center;
                font-size: 28rpx;
            }
        }

        .label {
            font-size: 20rpx;
            text-align: center;
            width: 80rpx;

            &.my-groupon {
                width: 90rpx;
            }

            .img {
                width: 39rpx;
                height: 39rpx;
            }

            .label-text {
                padding-top: 5rpx;
                color: #2f2f2f;
                font-weight: 500;
                font-size: 22rpx;

                .my-group-text {
                    white-space: nowrap;
                }
            }
        }

        .btn {
            flex: 1;
            text-align: center;
            line-height: 70rpx;
            height: 70rpx;
            color: #fff;
            font-size: 24rpx;
        }

        .cart {
            background: var(--vice-bg);
            border-radius: 100rpx 0 0 100rpx;
            color: var(--vice-text);
        }

        .buy {
            background: var(--general);
            border-radius: 0 100rpx 100rpx 0;
            color: var(--main-text);
        }
    }
}

.product-card-row {
    background-color: #fff;
    padding: 20rpx;
    border-radius: 20rpx;
    margin: 20rpx 0;
    display: flex;
    flex-direction: column;
    row-gap: 25rpx;

    .card-row-flex {
        display: flex;
        flex-direction: column;
        row-gap: 25rpx;
        padding-top: 25rpx;
    }

    .cart-item {
        font-size: 26rpx;

        .title {
            min-width: 110rpx;
            color: #81838e;
            padding-right: 10rpx;
        }

        .attr_text {
            display: block;
            width: 66vw;
        }

        .label {
            .tips {
                color: var(--general);
                font-size: 24rpx;
                margin-top: 5rpx;
            }
        }
    }

    .traceability-item {
        padding: 10rpx;
        background-color: #fdf6f0;
        color: #ff9627;
        font-size: 24rpx;

        image {
            width: 115rpx;
            height: 35rpx;
            margin-right: 20rpx;
        }
    }

    .tab-box {
        padding: 10rpx 0 20rpx 0;
        margin-bottom: 20rpx;
        font-size: 24rpx;

        .active {
            color: var(--general);
        }
    }

    .default {
        color: #9b9c9f;
        font-size: 24rpx;
        padding: 10rpx;

        .desc-pic-item {
            .slide-image {
                display: block;
                width: 100%;
            }
        }
    }
}
</style>
