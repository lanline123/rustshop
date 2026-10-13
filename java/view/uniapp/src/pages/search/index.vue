<template>
    <tig-layout :title="navbarTitle" :custom-back="true" @back="handleBack">
        <view class="searchGood" :class="{ 'searchGood--overseas': layoutStyle === 2 }">
            <!-- 国内版：灰底圆角框 + 框外「搜索」文字（无放大镜）；海外版：黑描边 + 框内 icon-neirongsousuo -->
            <view v-if="layoutStyle === 1" class="search-warp acea-row">
                <view class="search-input acea-row">
                    <input
                        v-model="keyWords"
                        :focus="true"
                        type="text"
                        :placeholder="$t(placeholder)"
                        placeholder-class="placeholder"
                        @confirm="searchSubmit"
                    />
                </view>
                <view class="bnt" @click="searchSubmit">{{ $t("搜索") }}</view>
            </view>
            <view v-else class="search-warp search-warp--overseas acea-row">
                <view class="search-input acea-row">
                    <input
                        v-model="keyWords"
                        :focus="true"
                        type="text"
                        :placeholder="$t(placeholder)"
                        placeholder-class="placeholder"
                        @confirm="searchSubmit"
                    />
                    <text class="iconfont icon-neirongsousuo search-ico" @click="searchSubmit" />
                </view>
            </view>
            <view v-if="searchHistory.length" class="search_init_box">
                <view class="search_history_box">
                    <view class="title">
                        {{ $t("搜索历史") }}
                        <view class="clear_history" @click.stop="clearHistory"><text class="iconfont-h5 icon-shanchu" /> </view>
                    </view>
                    <view class="list acea-row">
                        <template v-for="(item, index) in searchHistory" :key="index">
                            <view class="item line1" @click="handleSearch(item)">{{ item }}</view>
                        </template>
                    </view>
                </view>
            </view>
        </view>
    </tig-layout>
</template>

<script lang="ts" setup>
/**
 * 搜索页
 * - 搜索框输入关键词
 * - 搜索历史和热门搜索推荐
 * - 跳转搜索结果页
 */
import { ref, computed } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import { useI18n } from "vue-i18n";
import { redirect } from "@/utils";
import { useSearchDecorateStore } from "@/store/searchDecorate";

const { t } = useI18n();

// 搜索入口页与结果页共用同一份装修配置（Decorate 页 H5 + pageType=6）
const searchDecorateStore = useSearchDecorateStore();
/** 布局样式：1-国内版，2-海外版 */
const layoutStyle = computed(() => Number(searchDecorateStore.config.layoutStyle ?? 1));

const navbarTitle = ref(t("搜索商品"));
const placeholder = ref(t("点击搜索商品"));

const keyWords = ref("");

const searchHistory = ref<any[]>([]);

const shopId = ref("");
const isSwitchTab = ref(false);
/** 装修预览 id：从入口页再搜时透传给 result，避免丢掉草稿预览 */
const previewId = ref<string | number>("");

// 需要返回的路径
const redirectPath = ref("");
// 需要查询的历史记录字段名称
const searchHistoryKey = ref("searchHistory");

onLoad((options: any) => {
    // 装修预览链路：误进 index 时仍拉草稿，并在跳 result 时透传 previewId
    if (options?.previewId) {
        previewId.value = options.previewId;
    }
    searchDecorateStore.fetchConfig(options?.previewId);
    if (options) {
        if (options.shopId && options.shopId != "undefined") {
            shopId.value = options.shopId;
            navbarTitle.value = t("店铺搜索");
        }
        // 接收关键词并显示在输入框中
        if (options.keyWords) {
            keyWords.value = options.keyWords;
        }
        redirectPath.value = options.redirectPath || "";
        searchHistoryKey.value = options.searchHistoryKey || "searchHistory";
        navbarTitle.value = options.navbarTitle || "搜索商品";
        placeholder.value = options.placeholder || "点击搜索商品";
        isSwitchTab.value = options.isSwitchTab;
    }
    if (uni.getStorageSync(searchHistoryKey.value)) {
        searchHistory.value = uni.getStorageSync(searchHistoryKey.value);
    }
});
const searchSubmit = () => {
    if (keyWords.value) {
        searchHistory.value.unshift(keyWords.value);
        searchHistory.value = [...new Set(searchHistory.value)];
        if (searchHistory.value.length > 10) {
            searchHistory.value.splice(10, searchHistory.value.length - 10);
        }
        uni.setStorageSync(searchHistoryKey.value, searchHistory.value);
        handleSearch(keyWords.value);
    } else {
        // 门店入口进入搜索页时，允许空关键词提交（返回门店页展示全部商品）
        const isFromStore = typeof redirectPath.value === "string" && redirectPath.value.startsWith("/pages/store/index");
        if (
            redirectPath.value === "/pages/buyerShow/index" ||
            redirectPath.value.includes("/pages/groupon/list") ||
            redirectPath.value === "/pages/groupon/mylist" ||
            isFromStore
        ) {
            handleSearch(keyWords.value);
            return;
        }
        uni.showToast({
            title: t("请输入搜索内容"),
            icon: "none"
        });
    }
};
const clearHistory = () => {
    searchHistory.value = [];
    uni.removeStorageSync(searchHistoryKey.value);
};

const handleSearch = (item: any) => {
    // 如果需要返回路径存在，则返回对应页面
    if (isSwitchTab.value) {
        let url = `${redirectPath.value}${redirectPath.value.includes("?") ? "&" : "?"}keyWords=${item}`;
        uni.reLaunch({
            url
        });
        return;
    } else if (redirectPath.value) {
        // 有历史栈时：走事件通知 + navigateBack，避免"栈里叠两个上一页"导致返回按钮回不到进入前的页面
        const pages = getCurrentPages();
        if (pages.length > 1) {
            uni.$emit("searchKeyWordsResult", {
                redirectPath: redirectPath.value,
                keyWords: item
            });
            uni.navigateBack();
            return;
        }
        // 无历史栈（深链接直接进入搜索页）时：用 redirectTo 兜底
        let url = `${redirectPath.value}${redirectPath.value.includes("?") ? "&" : "?"}keyWords=${item}`;
        uni.redirectTo({ url });
        return;
    }
    let url = `/pages/search/result?keyWords=${item}`;
    shopId.value && (url += `&shopId=${shopId.value}`);
    if (previewId.value !== "" && previewId.value !== undefined && previewId.value !== null) {
        url += `&previewId=${previewId.value}`;
    }
    redirect({
        url
    });
};

const handleBack = () => {
    // 优先返回上一页，避免用 navigateTo 二次入栈导致返回死循环
    const pages = getCurrentPages();
    if (pages.length > 1) {
        uni.navigateBack();
        return;
    }
    // 无历史栈时兜底：有 redirectPath 则替换当前页跳过去；否则回首页
    if (redirectPath.value) {
        uni.redirectTo({
            url: redirectPath.value
        });
        return;
    }
    uni.switchTab({
        url: "/pages/index/index"
    });
};
</script>
<style>
page {
    background-color: #fff !important;
}
</style>
<style scoped>
.search-warp {
    padding-left: 30rpx;
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    justify-content: space-between;
}

.search-warp {
    margin-top: 20rpx;
}

.search-warp .search-input {
    width: 598rpx;
    background-color: #f7f7f7;
    border-radius: 33rpx;
    padding: 0 35rpx;
    box-sizing: border-box;
    height: 66rpx;
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    justify-content: space-between;
}

.search-warp .search-input input {
    flex: 1;
    width: 0;
    font-size: 28rpx;
}

.search-warp .search-input .placeholder {
    color: #bbb;
}

.search-warp .search-input .iconfont {
    color: #000;
    font-size: 35rpx;
}

.search-warp .bnt {
    width: 120rpx;
    text-align: center;
    height: 66rpx;
    line-height: 66rpx;
    font-size: 30rpx;
    color: #282828;
}

/* 海外版搜索栏：黑描边方角框，搜索图标在框内右侧 */
.search-warp--overseas {
    padding: 0 32rpx;
}

.search-warp--overseas .search-input {
    width: 100%;
    height: 68rpx;
    background-color: #fff;
    border: 1px solid #1a1a1a;
    border-radius: 8rpx;
    padding: 0 20rpx 0 24rpx;
}

.search-warp--overseas .search-input input {
    flex: 1;
    width: 0;
}

.search-warp--overseas .search-ico {
    flex-shrink: 0;
    font-size: 36rpx;
    color: #1a1a1a;
    font-weight: bold;
}

/* 海外版历史标签跟随方角风格 */
.searchGood--overseas .search_init_box .list .item {
    border-radius: 7rpx;
    background: #f5f6fa;
    color: #505259;
    border: 1rpx solid #f5f6fa;
}

.search_init_box .title {
    font-size: 24rpx;
    color: #333;
    margin: 50rpx 30rpx 25rpx 30rpx;
    font-weight: bold;
    position: relative;
}

.search_init_box .list {
    padding-left: 10rpx;
    padding-right: 30rpx;
}

.search_init_box .list .item {
    font-size: 24rpx;
    color: #666;
    padding: 0 28rpx;
    height: 50rpx;
    border-radius: 30rpx;
    line-height: 50rpx;
    margin: 0 0 20rpx 20rpx;
    background: #f6f6f6;
}

.clear_history {
    position: absolute;
    right: 0;
    top: 0;
    font-weight: normal;
    font-size: 26rpx;
    padding: 20rpx;
    top: -20rpx;
}
</style>
