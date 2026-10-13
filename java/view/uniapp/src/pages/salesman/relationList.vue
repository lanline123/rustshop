<template>
    <tig-layout :need-safe-bottom="false">
        <view class="customer-page" :class="{ 'empty-page': !loading && !list.length }">
            <view ref="filterHeaderRef" class="filter-header">
                <view class="search-wrap" :style="searchWrapStyle">
                    <up-search
                        v-model="params.keyword"
                        :placeholder="$t('请输入手机号或有效客户昵称搜索')"
                        :show-action="false"
                        @search="reload"
                        @clear="reload"
                    />
                </view>
                <view class="tabs"
                    ><up-tabs
                        v-model:current="activeIndex"
                        :list="tabs"
                        key-name="label"
                        :scrollable="false"
                        line-color="var(--general)"
                        :active-style="{ color: 'var(--general)' }"
                        @change="changeTab"
                /></view>
                <view class="time-tabs">
                    <view
                        v-for="item in timeTabs"
                        :key="item.value"
                        class="time-tab"
                        :class="{ active: timeType === item.value }"
                        @click="changeTime(item.value)"
                        >{{ $t(item.label) }}</view
                    >
                    <view class="time-tab custom" @click="showSelectDate = true"
                        ><text :class="{ selected: customDates.length }">{{ customDateText || $t("自定义时间") }}</text
                        ><up-icon name="arrow-down" :color="customDates.length ? 'var(--general)' : '#969799'" size="8"
                    /></view>
                </view>
                <selectDate v-model="showSelectDate" @confirm="confirmCustomDate" />
            </view>
            <view v-if="list.length" class="total">{{ $t("共") }}<text class="total-count">{{ total }}</text>{{ $t("个有效客户") }}</view>
            <empty v-if="!loading && !list.length" :styles="{ width: '320rpx', height: '320rpx' }" />
            <view v-for="item in list" :key="item.userId" class="customer-card" @click="openDetail(item.userId)">
                <view class="avatar"><tig-image :src="imageFormat(item.avatar || configStore.defaultAvatar)" /></view>
                <view class="customer-info">
                    <view class="name">{{ item.nickname || item.username || "-" }}</view>
                    <view class="line"
                        >{{ $t("成交额") }}：<text>{{ money(item.orderAmount) }}</text> {{ $t("订单数") }}：<text>{{ item.orderCount || 0 }}</text></view
                    >
                    <view class="line">{{ $t("最近下单时间") }}：{{ formatMinute(item.lastOrderTime) || $t("无") }}</view>
                    <view class="line">{{ $t("渠道来源") }}：{{ item.channel || "-" }}</view>
                </view>
            </view>
            <view v-if="loading" class="loading">{{ $t("加载中") }}</view>
            <view v-else-if="list.length && !hasMore()" class="loading">{{ $t("没有更多了") }}</view>
        </view>
    </tig-layout>
</template>
<script setup lang="ts">
import { computed, nextTick, onMounted, reactive, ref } from "vue";
import { onLoad, onReachBottom } from "@dcloudio/uni-app";
import { useI18n } from "vue-i18n";
import selectDate from "@/pages/salesman/src/selectDate.vue";
import empty from "@/pages/salesman/src/empty.vue";
import { getCustomerList } from "@/api/salesman/salesmanCenter";
import type { CustomerItem, CustomerListParams } from "@/types/salesman/customer";
import { useSaveTopBoxHeight } from "@/hooks";
import { useConfigStore } from "@/store/config";
import { imageFormat } from "@/utils/format";
import { ensureViewCustomerInfo } from "@/pages/salesman/src/salesmanPrivacy";
const { t } = useI18n();
const { height: topHeight } = useSaveTopBoxHeight();
const configStore = useConfigStore();
const filterHeaderRef = ref<HTMLElement | null>(null);
const filterHeight = ref(300);
const filterTopHeight = computed(() => {
    const menu = configStore.menuButtonInfo;
    // 胶囊坐标的 top 已包含状态栏，搜索框与胶囊同一行，只需占位到胶囊顶部
    return menu.top ? Math.max(0, menu.top - uni.upx2px(24)) : topHeight.value;
});
onMounted(async () => {
    await nextTick();
    uni.createSelectorQuery().select(".filter-header").boundingClientRect((rect) => {
        if (rect?.height) filterHeight.value = rect.height;
    }).exec();
});
const searchWrapStyle = computed(() => {
    // 微信小程序原生胶囊位于右侧，搜索框预留间距后占据胶囊左侧区域
    // #ifdef MP-WEIXIN
    const { screenWidth } = configStore.windowInfo;
    const { left } = configStore.menuButtonInfo;
    if (screenWidth && left) return { paddingRight: `${screenWidth - left + 8}px` };
    // #endif
    return {};
});
const tabs = computed(() => [
    { label: t("全部客户"), value: "all" },
    { label: t("下单客户"), value: "ordered" }
]);
const timeTabs = [
    { label: "全部", value: "all" },
    { label: "今日", value: "today" },
    { label: "昨日", value: "yesterday" },
    { label: "近七日", value: "seven" }
];
const showSelectDate = ref(false);
const customDates = ref<string[]>([]);
const customDateText = computed(() => customDates.value.map((date) => date.replace(/-/g, ".")).join("-\n"));
const activeTab = ref<CustomerListParams["customerType"]>("all");
const activeIndex = ref(0);
const timeType = ref("all");
const list = ref<CustomerItem[]>([]);
const total = ref(0);
const loading = ref(false);
const params = reactive<CustomerListParams>({ page: 1, size: 15, customerType: "all", keyword: "" });
const dateText = (date: Date) => {
    const y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, "0");
    const d = String(date.getDate()).padStart(2, "0");
    return `${y}-${m}-${d}`;
};
const applyTime = () => {
    const now = new Date();
    params.startTime = undefined;
    params.endTime = undefined;
    if (timeType.value === "today") params.startTime = params.endTime = dateText(now);
    if (timeType.value === "yesterday") {
        const day = new Date(now.getTime() - 86400000);
        params.startTime = params.endTime = dateText(day);
    }
    if (timeType.value === "seven") {
        params.startTime = dateText(new Date(now.getTime() - 6 * 86400000));
        params.endTime = dateText(now);
    }
};
const load = async (reset = false) => {
    if (loading.value || (!reset && list.value.length && list.value.length >= total.value)) return;
    if (reset) {
        params.page = 1;
        list.value = [];
    }
    loading.value = true;
    try {
        const result = await getCustomerList({ ...params });
        list.value = [...list.value, ...(result.records || [])];
        total.value = result.total || 0;
    } catch {
        uni.showToast({ title: t("加载失败"), icon: "none" });
    } finally {
        loading.value = false;
    }
};
const reload = () => load(true);
const changeTab = (item: { value: CustomerListParams["customerType"] }) => {
    activeTab.value = item.value;
    params.customerType = item.value;
    reload();
};
const changeTime = (value: string) => {
    timeType.value = value;
    customDates.value = [];
    applyTime();
    reload();
};
const confirmCustomDate = (dates: string[]) => {
    customDates.value = dates;
    timeType.value = "custom";
    params.startTime = dates[0];
    params.endTime = dates[1];
    reload();
};
const money = (value?: number) => Number(value || 0).toFixed(2);
const formatMinute = (value?: string | null) => (value ? value.replace(/:\d{2}(?=\s*$)/, "") : "");
const openDetail = (userId: number) => uni.navigateTo({ url: `/pages/salesman/customerDetail?userId=${userId}` });
const hasMore = () => list.value.length < total.value;
onLoad(async (options: any) => {
    const allowed = await ensureViewCustomerInfo();
    if (!allowed) return;
    if (options?.customerType === "today") timeType.value = "today";
    applyTime();
    load(true);
});
onReachBottom(() => {
    if (!loading.value && hasMore()) {
        params.page += 1;
        load();
    }
});
</script>
<style lang="scss" scoped>
.customer-page {
    min-height: 100vh;
    box-sizing: border-box;
    padding-top: v-bind("filterHeight + 'px'");
    padding-bottom: 30rpx;
}
/* #ifdef MP-WEIXIN */
.customer-page.empty-page {
    height: 100vh;
    box-sizing: border-box;
    overflow: hidden;
}
/* #endif */
.filter-header {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    z-index: 10;
    padding-top: v-bind("filterTopHeight + 'px'");
    background: #fff;
}
.search-wrap {
    padding: 24rpx 10px 12rpx;
    background: #fff;
}
.tabs,
.time-tabs {
    display: flex;
    align-items: center;
    gap: 28rpx;
    padding: 0 10px;
    background: #fff;
    overflow-x: auto;
}
.time-tabs {
    padding: 20rpx 10px;
    box-shadow: 0 6rpx 12rpx rgba(125, 126, 128, 0.16);
    gap: 14rpx;
}
.time-tab {
    box-sizing: border-box;
    height: 22px;
    line-height: 22px;
    padding: 0 10px;
    border-radius: 24rpx;
    color: #999;
    background: #f7f8fa;
    white-space: nowrap;
    font-size: 24rpx;
    &.active {
        color: var(--general);
        background: #fff0e6;
    }
    &.custom {
        margin-left: auto;
        flex-shrink: 0;
        padding-right: 0;
        display: flex;
        align-items: center;
        gap: 8rpx;
        color: #999;
        background: transparent;
        text {
            white-space: nowrap;
            &.selected {
                font-size: 24rpx;
                color: var(--general);
                white-space: pre-line;
                line-height: 1.2;
            }
        }
    }
}
.total {
    padding: 10px 24rpx;
    color: #999;
}
.total-count {
    color: #333;
}
.customer-card {
    display: flex;
    box-sizing: border-box;
    min-width: 0;
    overflow: hidden;
    gap: 22rpx;
    margin: 0 24rpx 20rpx;
    padding: 28rpx 24rpx;
    border-radius: 10rpx;
    background: #fff;
}
.avatar {
    display: block;
    width: 84rpx;
    height: 84rpx;
    border-radius: 50%;
    overflow: hidden;
    flex: 0 0 84rpx;
    min-width: 84rpx;
    :deep(.base-image) {
        opacity: 1;
    }
    :deep(.base-image-box) {
        width: 100%;
        height: 100%;
    }
}
.customer-info {
    min-width: 0;
    width: 0;
    overflow: hidden;
    flex: 1;
}
.name {
    margin-bottom: 12rpx;
    font-size: 32rpx;
    color: #333;
}
.line {
    margin-top: 10rpx;
    color: #999;
    font-size: 24rpx;
    word-break: break-all;
    text {
        color: #333;
        font-weight: 600;
    }
}
.empty,
.loading {
    padding: 80rpx 0;
    text-align: center;
    color: #999;
}
</style>
