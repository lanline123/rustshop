<template>
    <tig-layout>
        <template v-if="!failLoad">
            <view v-if="!isLoading" class="groupon-container">
                <view class="groupon-header">
                    <view class="header acea-row row-center-wrapper">
                        <view class="left">
                            <view class="iconfont icon-houtui" @click="handleIcon" />
                        </view>
                        <view class="acea-row row-between-wrapper input" @click.stop="toSearch">
                            <text class="iconfont-h5 icon-sousuo" />
                            <input v-model="filterParams.keyword" readonly class="txt" type="text" :placeholder="$t('商品名称搜索')" clearable />
                        </view>
                    </view>
                    <view class="groupon-title">
                        <image class="groupon-img" :src="imageFormat(grouponPromotion.grouponImage)" mode="heightFix" />
                        <view v-if="grouponPromotion.endTime" class="group-timeinfo flex align-center">
                            <view v-if="!isActivityEnd" class="flex" style="align-items: baseline">
                                <view class="countdown-title">{{ grouponPromotionTimeText }}</view>
                                <view class="countdown">
                                    <commonCountdown
                                        countdown-type="specifyTime"
                                        :specify-time="dateStringToTimestamp(isActivityStart ? grouponPromotion.endTime : grouponPromotion.startTime)"
                                    >
                                        <template #default="{ days, hours, minutes, seconds }">
                                            <view class="countdown-text">
                                                <text v-if="days">{{ days }}</text>
                                                <text v-if="days">{{ $t("天") }}&nbsp;</text>
                                                <text class="countdown-bg">{{ formatTime(hours) }}</text>
                                                <text>&nbsp;:&nbsp;</text>
                                                <text class="countdown-bg">{{ formatTime(minutes) }}</text>
                                                <text>&nbsp;:&nbsp;</text>
                                                <text class="countdown-bg">{{ formatTime(seconds) }}</text>
                                            </view>
                                        </template>
                                    </commonCountdown>
                                </view>
                            </view>
                            <view v-else>{{ $t("活动已结束") }}</view>
                            <view class="flex group-desc" style="align-items: baseline" @click="showGrouponPromotionPlain">
                                <view class="group-timeinfo-text" style="word-break: break-all">{{ $t("活动规则") }}</view>
                                <text class="iconfont-h5 icon-gengduo1" style="font-size: 22rpx; color: #bfbfbf" />
                            </view>
                        </view>
                    </view>
                </view>
                <view class="placeholder" />
                <view class="tab-box">
                    <!-- <view class="gap"></view> -->
                    <view class="item-list flex align-center justify-between">
                        <template v-for="item in tabList" :key="item.value">
                            <view class="item flex align-center" :class="{ active: tabIndex === item.value }" @click="onChangeTab(item)">
                                <text>{{ $t(item.label) }}</text>
                                <view v-show="item.value === 'price'" class="price-ico-box flex flex-column">
                                    <text class="iconfont-h5 icon-up" style="font-size: 14rpx" :class="{ order: item.order === 'asc' }" />
                                    <text class="iconfont-h5 icon-arrowdown" style="font-size: 14rpx" :class="{ order: item.order === 'desc' }" />
                                </view>
                            </view>
                        </template>
                        <view class="layout-change-icon" @click="changeLayout">
                            <image v-show="layoutMode === 'list'" class="icon" :src="staticResource('groupon/list.png')" />
                            <image v-show="layoutMode === 'waterfall'" class="icon" :src="staticResource('groupon/waterfall.png')" />
                        </view>
                    </view>
                </view>
                <view class="list-box">
                    <scroll-view :scroll-y="true" class="list-scroll" @scrolltolower="reachBottom">
                        <view v-if="grouponItemList.length">
                            <grouponListLayout :groupon-item-list="grouponItemList" :layout-mode="layoutMode">
                                <template #default="{ item }">
                                    <grouponItem
                                        :mode="layoutMode"
                                        :item="item"
                                        :is-activity-start="isActivityStart"
                                        :is-activity-end="isActivityEnd"
                                        :product-style="styleMap[item.productId]"
                                        @start-group="startGroup"
                                        @buy="buy"
                                    />
                                </template>
                            </grouponListLayout>
                        </view>
                        <loading-box v-model="isLoadMore" color="#fff" :page="filterParams.page" :length="grouponItemList.length" />
                        <template v-if="!isLoading && total === 0">
                            <view class="empty-box">
                                <up-empty :icon="staticResource('salesman/no_order.png')" :text="$t('暂无数据')" />
                            </view>
                        </template>
                    </scroll-view>
                </view>
            </view>
        </template>
        <template v-else>
            <error />
            <recommend title-bg="#fff" />
        </template>
        <tig-loadingpage v-model="isLoading" />
        <tig-popup v-model:show="showProductDesc">
            <scroll-view scroll-y="true" class="grouponpromotion-plain">
                <view class="popup-title title">{{ $t("活动说明") }}</view>
                <view class="popup-content">
                    {{ grouponPromotion.grouponExplain }}
                </view>
            </scroll-view>
        </tig-popup>
        <specification ref="specificationRef" v-model="specificationId" :groupon-enable="grouponEnable" />
    </tig-layout>
</template>

<script lang="ts" setup>
/**
 * 拼团商品列表页
 * - 展示拼团活动商品列表
 * - 支持列表和瀑布流两种布局
 * - 显示拼团价格和成团人数
 */
import { ref, reactive, computed } from "vue";
import { useI18n } from "vue-i18n";
import { onMounted } from "vue";
import { imageFormat } from "@/utils/format";
import { setPageMeta } from "@/utils/seo";
import { useConfigStore } from "@/store/config";
import { redirect, staticResource } from "@/utils";
import { getGrouponPromotionDetail, getGrouponItemList } from "@/api/groupon/groupon";
import type { GrouponItem } from "@/types/home/home";
import type { GrouponPromotionDetail, GrouponItemListParams } from "@/types/groupon/groupon";
import grouponItem from "./src/grouponItem.vue";
import grouponListLayout from "./src/grouponListLayout.vue";
import commonCountdown from "@/components/commonCountdown/index.vue";
import specification from "@/components/product/specification.vue";
import TigPopup from "@/components/tig/tig-popup/tig-popup.vue";
import error from "./src/error.vue";
import recommend from "@/components/recommend/index.vue";
import { useList } from "@/hooks";
import { useProductStyle } from "@/hooks/useProductStyle";
import { onLoad } from "@dcloudio/uni-app";
import { getIndex } from "@/api/home/home";

const { t } = useI18n();
const configStore = useConfigStore();
const specificationRef = ref();

onMounted(() => {
    // #ifdef H5
    setPageMeta({
        title: `${t("团购活动")} - ${configStore.baseInfo.shopTitle}`,
        description: `${configStore.baseInfo.shopTitle}${t("团购优惠活动")}`
    });
    // #endif
});
const grouponPromotionId = ref(0);
const isLoading = ref(true);
const failLoad = ref(false);
const grouponEnable = ref(true); // 默认开启拼团
const filterParams = reactive<GrouponItemListParams>({
    //初始化用于查询的参数
    page: 1,
    size: 20,
    sortField: "",
    keyword: "",
    sortOrder: "",
    grouponPromotionId: 0
});
const grouponPromotion = ref<GrouponPromotionDetail>({
    endTime: "",
    grouponExplain: "",
    grouponImage: "",
    startTime: "",
    shopId: 0,
    shopTitle: ""
});

const tabIndex = ref("default");
const tabList = ref([
    {
        label: t("综合"),
        value: "default",
        order: "desc"
    },
    {
        label: t("销量"),
        value: "sale",
        order: "desc"
    },
    {
        label: t("价格"),
        value: "price",
        order: "desc"
    }
]);

const specificationId = ref(0);
const showProductDesc = ref(false);
const layoutMode = ref<"list" | "waterfall">("list");

const toSearch = () => {
    redirect({
        url: "/pages/search/index",
        param: {
            redirectPath: "/pages/groupon/list?id=4",
            searchHistoryKey: "grouponSerchHistory",
            navbarTitle: t("搜索拼团商品"),
            placeholder: t("点击搜索"),
            isSwitchTab: true
        }
    });
};

/**
 * 是否活动开始
 */
const isActivityStart = computed(() => {
    if (!grouponPromotion.value.startTime) {
        return false;
    }
    const startTime = dateStringToTimestamp(grouponPromotion.value.startTime);
    return startTime < new Date().getTime();
});

/**
 * 是否活动结束
 */
const isActivityEnd = computed(() => {
    if (!grouponPromotion.value.endTime) {
        return true;
    }
    const endTime = dateStringToTimestamp(grouponPromotion.value.endTime);
    return endTime < new Date().getTime();
});
/**
 * 活动时间文本
 */
const grouponPromotionTimeText = computed(() => {
    return isActivityStart.value ? t("距拼团结束") : t("距拼团开始");
});

/*时间字符串转时间戳 */
const dateStringToTimestamp = (dateString: any) => {
    const dateParts = dateString.split(" ");
    const [year, month, day] = dateParts[0].split("-");
    const [hour, minute, second] = dateParts[1].split(":");
    const date = new Date(year, month - 1, day, hour, minute, second);
    return date.getTime();
};

const handleIcon = () => {
    // #ifdef H5
    history.back();
    // #endif
    // #ifndef H5
    uni.navigateBack();
    // #endif
};

const formatTime = (time: string): string => {
    return time?.toString().padStart(2, "0") || "00";
};

const showGrouponPromotionPlain = () => {
    showProductDesc.value = true;
};

/**
 * 开团
 * @param productId 商品ID
 */
const startGroup = (item: GrouponItem) => {
    specificationId.value = item.productId;
    grouponEnable.value = true;
    specificationRef.value.handleShowPopup();
};

/**
 * 原价购买
 * @param item
 */
const buy = (item: GrouponItem) => {
    specificationId.value = item.productId;
    grouponEnable.value = false;
    specificationRef.value.handleShowPopup();
};

const {
    isLoadMore,
    getList,
    data: grouponItemList,
    reachBottom,
    total
} = useList<GrouponItem>(getGrouponItemList, {
    params: filterParams,
    path: {
        dataKey: "records"
    },
    needReachBottom: false
});

const { styleMap } = useProductStyle(grouponItemList);

// 拿到拼团活动详情
const getDetail = async () => {
    try {
        const result = await getGrouponPromotionDetail(grouponPromotionId.value);
        grouponPromotion.value = result;
        filterParams.grouponPromotionId = result.grouponPromotionId;
        await getList();
    } catch (error: any) {
        failLoad.value = true;
    } finally {
        isLoading.value = false;
    }
};

const resetList = () => {
    filterParams.page = 1;
    filterParams.sortOrder = "desc";
    grouponItemList.value = [];
};

const onChangeTab = (item: any) => {
    if (tabIndex.value !== item.value) {
        item.order = "desc";
    } else {
        if (item.value == "price") {
            item.order = item.order === "desc" ? "asc" : "desc";
        }
    }
    resetList();
    tabIndex.value = item.value;
    filterParams.sortField = item.value;
    filterParams.sortOrder = item.order;
    getList();
};

const changeLayout = async () => {
    layoutMode.value = layoutMode.value === "list" ? "waterfall" : "list";
};

const initPage = async () => {
    try {
        const res = await getIndex();
        if (res.moduleList?.length) {
            // 找到拼团的配置
            const grouponModule = res.moduleList.find((item) => item.type === "groupon");
            if (grouponModule && grouponModule.module && grouponModule.module.grouponActivityItem) {
                grouponPromotionId.value = Number(grouponModule.module.grouponActivityItem.grouponPromotionId);
            }
            await getDetail();
            return;
        }
    } catch (error: any) {
        failLoad.value = true;
    } finally {
        isLoading.value = false;
    }
};

onLoad(async (option) => {
    if (option && option.keyWords) {
        filterParams.keyword = option.keyWords;
    }
    initPage();
});
</script>
<style scoped lang="scss">
.groupon-header {
    // position: fixed;
    width: 100%;
    z-index: 99;
    background: #fff;
    height: 599rpx;
    left: 0;

    .header {
        width: 100%;
        height: 99rpx;
        background-color: #fff;
        display: flex;
        padding-right: 20rpx;

        .left {
            width: 80rpx;
            height: 100%;
            display: flex;
            justify-content: center;
            align-items: center;

            .icon-houtui {
                font-size: 34rpx;
            }
        }

        .input {
            flex: 1;
            height: 50rpx;
            background-color: #f5f5f5;
            border-radius: 50rpx;
            box-sizing: border-box;
            padding: 0 25rpx;

            .iconfont-h5 {
                font-size: 26rpx;
                color: #8c8c8c;
            }

            .txt {
                color: #8c8c8c;
                width: 93%;
                font-size: 26rpx;
            }
        }
    }

    .groupon-title {
        width: 100%;
        display: flex;
        flex-direction: column;

        .groupon-img {
            width: 100%;
            height: 400rpx;
        }

        .group-timeinfo {
            width: 100%;
            height: 100rpx;
            padding: 0 20rpx;
            padding-right: 10rpx;
            background: #fff;
            display: flex;
            align-items: center;
            justify-content: space-between;
            font-size: 24rpx;

            .countdown-title {
                font-weight: bold;
                color: #1a1a1a;
                margin-right: 15rpx;
            }

            .countdown-text {
                font-size: 22rpx;
                font-weight: bold;
                display: flex;
                align-items: baseline;
            }

            .countdown-bg {
                color: var(--ump-price);
                background-color: var(--primary-light-9);
                width: 32rpx;
                height: 32rpx;
                padding: 5rpx;
                border-radius: 6rpx;
                display: inline-flex;
                overflow: hidden;
                justify-content: center;
            }

            .group-desc {
                color: #999;
            }
        }
    }
}

.placeholder {
    // height: 699rpx;
}

.tab-box {
    margin-top: 20rpx;
    background-color: #fff;
    // position: fixed;
    height: 80rpx;
    width: 100%;
    z-index: 99;
    top: 599rpx;

    .gap {
        background-color: #f5f5f5;
        height: 20rpx;
    }

    .item-list {
        position: relative;
        // padding: 30rpx 0;
    }

    .item {
        height: 80rpx;
        padding: 0 28rpx;
        flex: 1;
        color: #333;
        font-size: 28rpx;
        align-items: center;
        justify-content: left;
        cursor: pointer;

        .price-ico-box {
            margin-left: 10rpx;
            gap: 0;
        }

        .sanjiaoright {
            color: #c3c0c0 !important;
            margin-left: 10rpx;
        }
    }

    .active {
        color: var(--general);

        .iconfont-h5 {
            color: #c3c0c0 !important;
        }

        .order {
            color: var(--general) !important;
        }
    }

    .layout-change-icon {
        padding: 0 20rpx;
        height: 80rpx;
        cursor: pointer;
        display: flex;
        align-items: center;

        .icon {
            height: 32rpx;
            width: 32rpx;
        }
    }
}

.list-box {
    height: calc(100vh - 699rpx);
    background-color: #fff;

    .list-scroll {
        height: 100%;
    }
}

.grouponpromotion-plain {
    height: 60vh;
    padding-top: 20rpx;

    .popup-content {
        padding: 30rpx;
    }
}

.empty-box {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 100%;
    width: 100%;
    margin: 0;
}
</style>
