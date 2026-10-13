<template>
    <template v-if="buyerShowEnabled === 1">
        <tig-layout :title="configStore.buyerShowMobileShowName || $t('买家秀')" :show-left="false">
            <view class="buyerShowBox">
                <view class="header acea-row row-center-wrapper">
                    <view class="acea-row row-between-wrapper input" @click.stop="toSearch">
                        <text class="iconfont-h5 icon-sousuo" />
                        <input v-model="filterParams.keyword" readonly class="txt" type="text" :placeholder="$t('点击搜索')" clearable />
                    </view>
                </view>
                <view class="tabs">
                    <horizontal-tabs v-model:current-tab="currentTab" :tabs="tabList" />
                </view>
                <view class="container">
                    <waterfall-layout
                        :list="buyerShowListData"
                        :has-more="hasMore"
                        :loading="loading"
                        :layout="configStore.buyerShowTemplateStyle ? 'single' : 'double'"
                        @load-more="loadMoreData"
                        @item-click="onProductClick"
                    >
                        <!-- 自定义项目渲染 -->
                        <template #item="{ item, index }">
                            <buyerShowItem :buyer-show-item="item" :buyer-show-index="index" :buyer-show-list-data="buyerShowListData" />
                        </template>
                        <template #itemRight="{ item, index }">
                            <buyerShowItem :buyer-show-item="item" :buyer-show-index="index" :buyer-show-list-data="buyerShowListData" />
                        </template>
                    </waterfall-layout>
                </view>
                <!--            判断是否已登录-->
                <template v-if="!hasToken">
                    <view class="publish" :style="publishFabStyle" @click="handleLogin">
                        <view class="iconfont-h5 icon-xiangji1" />
                        <view class="text">{{ $t("发布") }}</view>
                    </view>
                </template>
                <!--            判断是否有购买的商品-->
                <template v-else-if="getHistoryProductTotal">
                    <view class="publish" :style="publishFabStyle" @click="addBuyerShow">
                        <view class="iconfont-h5 icon-xiangji1" />
                        <view class="text">{{ $t("发布") }}</view>
                    </view>
                </template>
                <!--            <tig-loadingpage v-model="isLoading" />-->
                <loading-box v-model="isLoadMore" :page="filterParams.page" :length="buyerShowListData.length" />
            </view>
        </tig-layout>
        <template v-if="!isLoading && total === 0">
            <view class="empty-box">
                <up-empty :icon="staticResource('salesman/no_order.png')" :text="$t('暂无数据')" />
            </view>
        </template>
    </template>

    <template v-else>
        <tig-layout :title="$t('买家秀')" :show-left="false" style="height: 100vh">
            <buyerShowCloseTip v-model:buyer-show-enabled="buyerShowEnabled" />
        </tig-layout>
    </template>
</template>

<script setup lang="ts">
/**
 * 买家秀列表页
 * - 瀑布流展示买家秀内容
 * - 支持分类筛选
 * - 点击进入买家秀详情
 */
import { computed, ref, watch } from "vue";
import HorizontalTabs from "./buyerShowCompnents/horizontal-tabs.vue";
import WaterfallLayout from "./buyerShowCompnents/WaterfallLayout.vue";
import TigLayout from "@/components/tig/tig-layout/tig-layout.vue";
import buyerShowItem from "./src/buyerShowItem.vue";
import buyerShowCloseTip from "./src/buyerShowCloseTip.vue";
import { redirect, staticResource } from "@/utils";
import { useList } from "@/hooks";
import { buyerShowList, getBuyerShowGroupList, getHistoryProduct } from "@/api/buyerShow/buyerShow";
import type { BuyerShowFilterState, BuyerShowListFilter, BuyerShowListParams } from "@/types/buyerShow/buyerShow";
import { prefetchVideoFeedEntry } from "./composables/videoFeedPrefetch";
import { isVideoBuyerShow } from "./utils/videoFeed";
import { onLoad, onShow } from "@dcloudio/uni-app";
import { useConfigStore } from "@/store/config";
import { useTabbarStore } from "@/store/tabbar";
import { useUserStore } from "@/store/user";
import { handleLogin } from "@/utils/handleLogin";
import { useI18n } from "vue-i18n";
const configStore = useConfigStore();
const { t } = useI18n();
const tabbarStore = useTabbarStore();

/**
 * 发布按钮距底部的偏移，避开 tabbar 与底部安全区。
 * 小程序对 scoped 样式里的 calc(var(--x) + rpx) 支持不稳定，这里直接算出 px。
 */
const publishFabStyle = computed(() => {
    const gapPx = uni.upx2px(20);
    const tabBarPx = parseFloat(String(tabbarStore.tabbarHeight).replace(/px$/i, "")) || tabbarStore.tabbarHeightNum + configStore.safeBottom;
    const bottomPx = tabbarStore.currentActiveValue > -1 ? tabBarPx + gapPx : configStore.safeBottom + gapPx;
    return { bottom: `${bottomPx}px` };
});

const BUYER_SHOW_LIST_NEED_REFRESH_KEY = "buyerShow:listNeedRefresh";

uni.hideTabBar();
onShow(() => {
    uni.hideTabBar();
    // 仅当详情页发生“点赞状态变化”时，返回列表才刷新
    const needRefresh = Boolean(uni.getStorageSync(BUYER_SHOW_LIST_NEED_REFRESH_KEY));
    if (needRefresh) {
        uni.removeStorageSync(BUYER_SHOW_LIST_NEED_REFRESH_KEY);
        // 如果已有数据，说明不是首次加载；此时刷新列表即可
        if (buyerShowListData.value.length > 0) {
            filterParams.value.page = 1;
            getList({ ...filterParams.value });
        }
    }
});

const buyerShowEnabled = ref<number | undefined>(1);
watch(
    () => configStore,
    (val) => {
        if (val.initLoading) {
            buyerShowEnabled.value = val.buyerShowEnabled;
        }
    },
    { deep: true, immediate: true }
);

const userStore = useUserStore();
const hasToken = computed(() => {
    getHistoryProductList();
    return userStore.token || uni.getStorageSync("token");
});

const { total: getHistoryProductTotal, getList: getHistoryProductList } = useList(getHistoryProduct, {
    params: { page: 1, size: 1 },
    path: {
        dataKey: "records"
    },
    needReachBottom: false
});
const handleLink = (type: string) => {
    if (type === "home") {
        redirect({ url: "/pages/index/index" });
    } else {
        // #ifdef H5
        history.back();
        // #endif

        // #ifndef H5
        uni.navigateBack();
        // #endif
    }
};

const toSearch = () => {
    redirect({
        url: "/pages/search/index",
        param: {
            redirectPath: "/pages/buyerShow/index",
            searchHistoryKey: "buyerShowSearchList",
            navbarTitle: t("搜索买家秀"),
            placeholder: t("点击搜索"),
            isSwitchTab: true
        }
    });
};

const addBuyerShow = () => {
    redirect({ url: "/pages/buyerShow/addBuyerShow" });
};

const tabList = ref<any[]>([{ title: t("全部") }]);
const currentTab = ref(0);

const hasMore = ref(true);
const loading = ref(false);
const loadMoreData = async () => {
    console.log("loadMoreData");
};
/**
 * 点击列表项：视频类进视频流并定位到该条，图文类进图文详情
 */
const onProductClick = (item: BuyerShowFilterState) => {
    if (isVideoBuyerShow(item)) {
        // 进页前备好首屏并预取 feed，缩短打开时的等待
        prefetchVideoFeedEntry(item);
        redirect({ url: "/pages/buyerShow/videoFeed", param: { buyerShowId: item.buyerShowId } });
        return;
    }
    redirect({ url: "/pages/buyerShow/buyerShowDetail", param: { buyerShowId: item.buyerShowId } });
};

const filterParams = ref<BuyerShowListParams>({
    page: 1,
    size: 10,
    sortField: "addTime",
    sortOrder: "desc",
    keyword: ""
});

const {
    getList,
    data: buyerShowListData,
    isLoading,
    isLoadMore,
    total
} = useList<BuyerShowListFilter>(buyerShowList, {
    params: filterParams,
    path: {
        dataKey: "records"
    },
    immediate: false
});

// 获取买家秀分组列表
const getGroupList = () => {
    getBuyerShowGroupList().then((res: any) => {
        tabList.value = res.map((item: any) => {
            return {
                ...item,
                title: item.groupName
            };
        });
        tabList.value.unshift({ title: t("全部") });
    });
};

// 监听分组切换
watch(
    currentTab,
    (newValue) => {
        filterParams.value.goodsId = tabList.value[newValue]?.buyerShowGroupId;
        getList({ ...filterParams.value });
    },
    { deep: true }
);

onLoad((options: any) => {
    if (options.keyWords) {
        filterParams.value.keyword = options.keyWords;
        getList({ ...filterParams.value });
    } else {
        getList();
    }
    getGroupList();
});
</script>

<style scoped lang="scss">
.buyerShowBox {
    .header {
        width: 100%;
        height: 99rpx;
        background-color: #fff;
        position: fixed;
        left: 0;
        right: 0;
        z-index: 9;
        border-bottom: 1rpx solid #f5f5f5;
        top: var(--nav-height);
        .input {
            width: 700rpx;
            height: 60rpx;
            background-color: #f5f5f5;
            border-radius: 50rpx;
            box-sizing: border-box;
            padding: 0 25rpx;
            .iconfont {
                font-size: 35rpx;
                color: #555;
            }
            .txt {
                color: #999;
                width: 93%;
                font-size: 26rpx;
            }
        }
    }
    .tabs {
        height: 100rpx;
        position: fixed;
        top: calc(var(--nav-height) + 99rpx);
        left: 0;
        right: 0;
        z-index: 9;
    }
    .container {
        // #ifdef H5
        margin-top: calc(var(--nav-height) + 80rpx);
        // #endif

        // #ifdef APP-PLUS
        margin-top: calc(var(--nav-height) + 20rpx);
        // #endif
        // #ifdef MP-WEIXIN
        margin-top: calc(var(--nav-height));
        // #endif
        box-sizing: border-box;
    }
}

.publish {
    position: fixed;
    right: 10rpx;
    z-index: 9;
    width: 115rpx;
    height: 115rpx;
    //padding: 20rpx 0 20rpx;
    box-sizing: content-box;
    background: #fff;
    border-radius: 50%;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    .icon-xiangji1 {
        font-size: 38rpx;
        margin-bottom: 5rpx;
    }
    .text {
        font-size: 22rpx;
        color: #000;
    }
}

.popupBox {
    padding: 45rpx 80rpx;
    box-sizing: border-box;
    font-size: 32rpx;
    display: flex;
    flex-direction: column;
    align-content: center;
    justify-content: center;
    width: 590rpx;
    height: 340rpx;
    background: #ffffff;
    border-radius: 16rpx;
    .title {
        text-align: center;
        font-weight: bold;
        font-size: 32rpx;
        color: #262626;
        margin-bottom: 40rpx;
    }
    .tip {
        font-weight: 500;
        font-size: 28rpx;
        color: #8c8c8c;
        margin-bottom: 76rpx;
        text-align: center;
    }
    .btn {
        text-align: center;
        // 下划线
        text-decoration: underline;
        color: var(--general);
        font-weight: 500;
        font-size: 28rpx;
    }
}
</style>
