<template>
    <view>
        <tig-layout :need-safe-top="true" safe-top-bg-color="#f5f5f5">
            <view class="salesmanCard-top">
                <view class="salesmanCard-top-content">
                    <view class="salesmanCard-top-left">
                        <view class="salesmanCard-top-left-img">
                            <tig-image :src="userInfo.avatar" />
                        </view>
                    </view>
                    <view class="salesmanCard-top-right">
                        <view class="salesmanCard-top-right-mobile">{{ userInfo.mobile }}</view>
                        <view class="salesmanCard-top-right-name">{{ userInfo.nickname }}</view>
                    </view>
                </view>
            </view>

            <view class="salesmanCard-list">
                <view class="salesmanCard-list-title"> {{ $t("推荐商品") }} </view>
                <view class="salesmanCard-list-content">
                    <block v-for="item in list" :key="item.productId">
                        <view class="salesmanCard-list-content-item" @click="handleLink(item.productId)">
                            <view class="salesmanCard-list-content-item-img">
                                <tig-image :src="item.picUrl" />
                            </view>
                            <view class="content-item-text">
                                <view class="item-text-name line2">{{ item.productName }}</view>
                                <view class="item-text-price">
                                    <format-price :price-data="item.productPrice" />
                                    <text class="iconfont-h5 icon-gouwuche3" />
                                </view>
                            </view>
                        </view>
                    </block>
                </view>
                <empty v-if="!loading && total === 0" :styles="{ width: '320rpx', height: '320rpx' }" />
            </view>

            <tig-fixed-placeholder background-color="#fff">
                <view class="bottom-btns">
                    <block v-for="(item, index) in tabbarList" :key="index">
                        <view class="bottom-btns-item" :class="{ active: index === 0 }" @click="handleTabbar(item.url)">
                            <view class="bottom-btns-item-icon iconfont-h5" :class="item.icon" />
                            <view class="bottom-btns-item-text">{{ $t(item.name) }}</view>
                        </view>
                    </block>
                </view>
            </tig-fixed-placeholder>
        </tig-layout>
    </view>
</template>

<script setup lang="ts">
/**
 * 分销员名片页
 * - 展示分销员名片和推广商品
 * - 供客户查看分销员信息
 * - 支持商品浏览和购买
 */
import { ref, reactive } from "vue";
import { onShow, onLoad, onReachBottom } from "@dcloudio/uni-app";
import { salesmanProductList, salesmanUserinfo, acceptCardInvite } from "@/api/salesman/salesman";
import type { SalesmanProductListFilterResult } from "@/types/salesman/salesman";
import { redirect } from "@/utils";
import empty from "@/pages/salesman/src/empty.vue";
import { useUserStore } from "@/store/user";
import { useI18n } from "vue-i18n";

const { t } = useI18n();
const userStore = useUserStore();
const userInfo = ref<any>({});
const list = ref<SalesmanProductListFilterResult[]>([]);
const filterParams = reactive({
    page: 1,
    pageSize: 15
});
const total = ref(0);
const salesmanId = ref<null | number>(null);
const tabbarList = [
    {
        name: t("名片"),
        icon: "icon-mingpian",
        url: ""
    },
    {
        name: t("商城"),
        icon: "icon-shangcheng-",
        url: "/pages/index/index"
    },
    {
        name: t("我的"),
        icon: "icon-wode",
        url: "/pages/member/index"
    }
];
const loading = ref(false);
const getSalesmanProductList = async () => {
    loading.value = true;
    try {
        const result = await salesmanProductList(filterParams);
        list.value = [...list.value, ...result.records];
        total.value = result.total;
    } catch (error) {
        console.error(error);
    } finally {
        loading.value = false;
    }
};

const getSalesmanUserinfo = async () => {
    try {
        const result = await salesmanUserinfo(salesmanId.value!);
        userInfo.value = result;
    } catch (error) {
        console.error(error);
    }
};
const handleLink = (id: number) => {
    let url = salesmanId.value ? `/pages/product/index?id=${id}&salesmanId=${salesmanId.value}` : `/pages/product/index?id=${id}`;
    uni.navigateTo({
        url
    });
};

const handleTabbar = (url: string) => {
    if (!url) return;
    // 未登录时跳转登录页，并把当前名片页路径作为 url 参数传过去；登录成功后会自动跳回
    if (!userStore.token) {
        const backUrl = salesmanId.value ? `/pages/salesman/salesmanCard?salesmanId=${salesmanId.value}` : `/pages/salesman/salesmanCard`;
        uni.navigateTo({
            url: `/pages/login/index?url=${encodeURIComponent(backUrl)}`
        });
        return;
    }
    redirect({
        url
    });
};

// 微商模式名片邀请：已登录时即时绑定为下级分销员；
// 未开启微商模式/名片邀请、已是分销员等场景由后端处理，失败静默
const tryAcceptCardInvite = async () => {
    if (!salesmanId.value || !userStore.token) return;
    try {
        await acceptCardInvite(salesmanId.value);
    } catch (error) {
        console.error(error);
    }
};

onLoad((option) => {
    if (option && option.salesmanId) {
        salesmanId.value = Number(option.salesmanId);
        // const bindSalesmanId = uni.getStorageSync("bindSalesmanId");
        // if (!bindSalesmanId) {
        uni.setStorageSync("bindSalesmanId", salesmanId.value);
        // }
        getSalesmanUserinfo();
        tryAcceptCardInvite();
    }
    getSalesmanProductList();
});

onReachBottom(() => {
    if (filterParams.page < Math.ceil(total.value / filterParams.pageSize)) {
        filterParams.page++;
        getSalesmanProductList();
    }
});
</script>

<style lang="scss" scoped>
.salesmanCard-top {
    margin: 30rpx 30rpx 0 30rpx;

    .salesmanCard-top-content {
        background-color: #fff;
        display: flex;
        border-radius: 16rpx;
        box-shadow: 0 4rpx 8rpx 0 rgba(85, 105, 153, 0.1);

        .salesmanCard-top-left {
            .salesmanCard-top-left-img {
                height: 240rpx;
                width: 240rpx;
                margin: 30rpx 24rpx 30rpx 30rpx;
                border-radius: 50%;
                overflow: hidden;
                box-shadow: 0 4rpx 8rpx 0 rgba(85, 105, 153, 0.1);
            }
        }

        .salesmanCard-top-right {
            padding-left: 30rpx;
            .salesmanCard-top-right-mobile {
                padding-top: 44rpx;
                padding-right: 90rpx;
                word-break: break-all;
                font-size: 30rpx;
                line-height: 36rpx;
            }
            .salesmanCard-top-right-name {
                word-break: break-all;
                padding-top: 40rpx;
            }
        }
    }
}

.salesmanCard-list {
    background-color: #fff;
    padding: 0 30rpx 30rpx;
    margin-top: 40rpx;
    .salesmanCard-list-title {
        font-size: 30rpx;
        text-align: center;
        padding: 36rpx 0;
        font-weight: 500;
        &::after,
        &::before {
            position: relative;
            content: "";
            display: inline-block;
            top: -8rpx;
            width: 16rpx;
            height: 2rpx;
            background: #000;
        }
    }

    .salesmanCard-list-content-item {
        display: flex;
        margin-bottom: 20rpx;
        &:last-child {
            margin-bottom: 0;
        }

        .salesmanCard-list-content-item-img {
            height: 276rpx;
            width: 276rpx;
            margin-right: 16rpx;
            // box-shadow: 0 4rpx 8rpx 0 rgba(85, 105, 153, 0.1);
        }

        .content-item-text {
            width: calc(100% - 292rpx);
            display: flex;
            flex-direction: column;
            justify-content: space-between;
            .content-item-text-name {
                max-height: 80rpx;
                font-size: 28rpx;
                line-height: 36rpx;
                line-height: 40rpx;
            }

            .item-text-price {
                color: #f44;
                display: flex;
                font-size: 36rpx;
                justify-content: space-between;
                align-items: center;

                .icon-gouwuche3 {
                    font-size: 44rpx;
                }
            }
        }
    }
}

.bottom-btns {
    height: 100%;
    width: 100%;
    display: flex;

    .bottom-btns-item {
        flex: 1;
        display: flex;
        align-items: center;
        justify-content: center;
        flex-direction: column;
        color: #969799;

        .bottom-btns-item-icon {
            font-size: 50rpx;
        }
        .bottom-btns-item-text {
            font-size: 23rpx;
        }

        &.active {
            color: #f44;
        }
    }
}
</style>
