<template>
    <view :style="decorateVars">
        <tig-navbar v-if="!isOverseasSkin" :show-left="false" :title="$t('产品分类')" />
        <view v-if="loading || !partAllLoading" class="page-loading">
            <view class="ico" />
        </view>
        <view v-if="partAllLoading" class="pageMain">
            <block v-if="showCatLevel == 0">
                <view class="productSort" :class="{ 'productSort--overseas': isOverseasSkin }">
                    <view v-if="!isOverseasSkin" class="header acea-row row-center-wrapper">
                        <view class="acea-row row-between-wrapper input" @click.stop="toSearch">
                            <text class="iconfont-h5 icon-sousuo" />
                            <view class="txt">{{ $t(searchPlaceholder) }}</view>
                        </view>
                    </view>
                    <view class="aside" :style="asideOffsetStyle">
                        <view v-if="showRecommendEntry" :class="'item acea-row row-center-wrapper ' + (catId == 0 ? 'on' : '')" @click="changeCat(0)">
                            <text>{{ $t(recommendText) }}</text>
                        </view>
                        <block v-for="(item, index) in cateList" :key="index">
                            <view
                                :class="'item acea-row row-center-wrapper ' + (catId == item.categoryId ? 'on' : '')"
                                :data-cat_id="item.categoryId"
                                @click="changeCat(item.categoryId, item)"
                            >
                                <text v-if="item.catShortName">{{ item.catShortName }}</text>
                                <text v-else>{{ item.categoryName }}</text>
                            </view>
                        </block>
                    </view>
                    <view class="conter" :style="conterOffsetStyle">
                        <block v-if="!loading">
                            <block v-if="catId == 0 && showRecommendEntry">
                                <view class="listw">
                                    <view class="title acea-row row-center-wrapper">
                                        <view class="name">{{ $t("热门分类") }}</view>
                                    </view>
                                    <view class="list acea-row">
                                        <block v-for="(hot, index) in hotCat" :key="index">
                                            <navigator
                                                hover-class="none"
                                                :url="'/pages/search/result?categoryId=' + hot.categoryId"
                                                class="item acea-row row-column row-middle"
                                            >
                                                <view class="picture">
                                                    <tig-image :src="hot.categoryPic" mode="aspectFill" />
                                                </view>
                                                <view v-if="moduleConfig.showCategoryName !== false" class="name line1">{{ hot.categoryName }}</view>
                                            </navigator>
                                        </block>
                                    </view>
                                </view>
                            </block>
                            <block v-if="catId > 0">
                                <view v-for="(cat, index) in childCat" :id="'b' + index" :key="index" class="listw">
                                    <view class="title acea-row row-center-wrapper">
                                        <navigator
                                            hover-class="none"
                                            :url="'/pages/search/result?categoryId=' + cat.categoryId"
                                            class="item acea-row row-column row-middle"
                                        >
                                            <view class="name">{{ cat.categoryName }}</view>
                                        </navigator>
                                    </view>
                                    <view class="list acea-row">
                                        <block v-for="(childCatItem, index1) in cat.children" :key="index1">
                                            <navigator
                                                hover-class="none"
                                                :url="'/pages/search/result?categoryId=' + childCatItem.categoryId"
                                                class="item acea-row row-column row-middle"
                                            >
                                                <view v-if="childCatItem.categoryPic" class="picture">
                                                    <tig-image :src="childCatItem.categoryPic" />
                                                </view>
                                                <view v-if="moduleConfig.showCategoryName !== false" class="name line1">{{ childCatItem.categoryName }}</view>
                                            </navigator>
                                        </block>
                                    </view>
                                </view>
                            </block>
                        </block>
                    </view>
                </view>
            </block>
        </view>
    </view>
</template>

<script lang="ts" setup>
import { useI18n } from "vue-i18n";

/**
 * 分类样式一组件
 * - 左右分栏布局
 * - 左侧一级分类，右侧二级分类宫格
 * - 支持 cateCenterV1 装修配置
 */
import { ref, computed, type PropType } from "vue";
import { getCategoryAll, getCategoryHot } from "@/api/productCate/productCate";
import type { filterSeleted } from "@/types/productCate/productCate";
import type { CateCenterV1Data } from "@/components/modules/cate-center/cate-center-v1/types";
import { buildCateCenterV1Vars, cateCenterV1Defaults } from "@/components/modules/cate-center/cate-center-v1/config";
import { deepMerge } from "@/utils/deepMerge";
import { useConfigStore } from "@/store/config";

const { t } = useI18n();
const configStore = useConfigStore();

const catId = defineModel("catId", {
    type: Number,
    default: 0
});

const props = defineProps({
    /** 装修配置 */
    module: {
        type: Object as PropType<CateCenterV1Data>,
        default: () => ({})
    }
});

const moduleConfig = computed<CateCenterV1Data>(() => {
    return deepMerge({ ...cateCenterV1Defaults }, props.module || {});
});

const decorateVars = computed(() => buildCateCenterV1Vars(moduleConfig.value));

const searchPlaceholder = computed(() => moduleConfig.value.searchPlaceholder || cateCenterV1Defaults.searchPlaceholder || t("点击搜索商品信息"));
const recommendText = computed(() => (moduleConfig.value.recommendText ?? "").trim());
const showRecommendEntry = computed(() => moduleConfig.value.showRecommend !== false && recommendText.value.length > 0);

/** 外贸皮肤 mainHeader 占位（fixed 后脱离文档流） */
const mainHeaderSpacerStyle = computed(() => {
    return undefined;
});

/** 左侧栏 fixed 偏移：海外顶在 mainHeader 下，国内仍为导航 + 搜索条 */
const asideOffsetStyle = computed(() => {
    return undefined;
});

/** 右侧内容区：海外不再预留国内搜索条的 98rpx */
const conterOffsetStyle = computed(() => {
    return undefined;
});

const cateList = ref<any>([]);
const childCat = ref<filterSeleted[]>([]);
const hotCat = ref<filterSeleted[]>([]);
const partAllLoading = ref(false);
const loading = ref(true);
const showCatLevel = ref(0);

const toSearch = () => {
    uni.navigateTo({
        url: "/pages/search/index"
    });
};
const getAllCategory = async () => {
    partAllLoading.value = true;
    try {
        const result = await getCategoryAll();
        cateList.value = result || [];
        // 推荐入口关闭时，默认选中第一个分类，避免右侧空白
        if (!showRecommendEntry.value && cateList.value.length > 0 && catId.value === 0) {
            const first = cateList.value[0];
            catId.value = Number(first.categoryId) || 0;
            childCat.value = first.children;
        }
    } catch (err) {
        console.error(err);
    }
};

const changeCat = (id: number, item?: any) => {
    catId.value = id;
    if (item) {
        childCat.value = item.children;
    }
};
const getHotCatList = async () => {
    try {
        const result = await getCategoryHot();
        hotCat.value = result || [];
        loading.value = false;
    } catch (err) {
        console.error(err);
    }
};

getAllCategory();
getHotCatList();
</script>

<style lang="scss" scoped>
.main-header-spacer {
    width: 100%;
    flex-shrink: 0;
}

.productSort .header {
    width: 100%;
    height: 99rpx;
    background-color: #fff;
    position: fixed;
    left: 0;
    right: 0;
    z-index: 9;
    border-bottom: 1rpx solid #f5f5f5;
    top: var(--nav-height);
}

.productSort .header .input {
    width: 700rpx;
    height: 60rpx;
    background-color: #f5f5f5;
    border-radius: 50rpx;
    box-sizing: border-box;
    padding: 0 25rpx;
}

.productSort .header .input .iconfont {
    font-size: 35rpx;
    color: #555;
}

.productSort .header .input .txt {
    color: #999;
    width: 93%;
    font-size: 26rpx;
}

.productSort .aside {
    position: fixed;
    width: 180rpx;
    left: 0;
    bottom: 0;
    top: var(--nav-height);
    margin-top: 98rpx;
    background-color: var(--cate-side-bg, #f7f7f7);
    overflow-y: auto;
    overflow-x: hidden;
    padding-bottom: var(--tabbar-height);
}

.productSort .aside .item {
    height: 80rpx;
    width: 100%;
    font-size: 26rpx;
    color: var(--cate-side-text, #424242);
    text-align: center;
}

.productSort .aside .item.on {
    background-color: var(--cate-side-active-bg, #fff);
    width: 100%;
    text-align: center;
    color: var(--cate-side-active-text, var(--cate-theme, var(--general)));
    font-weight: bold;
}

.productSort .conter {
    margin: 98rpx 0 0 180rpx;
    padding: 0 14rpx;
}

.productSort .conter .listw {
    padding-top: 20rpx;
}

.productSort .conter .listw .title {
    height: 50rpx;
    justify-content: left;
}

.productSort .conter .listw .title .line {
    width: 100rpx;
    height: 2rpx;
    background-color: #999;
}

.productSort .conter .listw .title .name {
    font-size: 28rpx;
    color: var(--cate-group-title, #333);
    margin: 0 30rpx;
    font-weight: bold;
}

.productSort .conter .list {
    flex-wrap: wrap;
}

.productSort .conter .list .item {
    width: 177rpx;
    margin-top: 26rpx;
}

.productSort .conter .list .item .picture {
    width: 120rpx;
    height: 120rpx;
    border-radius: 50%;
    overflow: hidden;
}

.productSort .conter .list .item .picture image {
    width: 100%;
    height: 100%;
}

.productSort .conter .list .item .name {
    font-size: 24rpx;
    color: var(--cate-name, #333);
    height: 56rpx;
    line-height: 56rpx;
    width: 120rpx;
    text-align: center;
}

/* ========== 海外皮肤：对齐 Admin ViewOverseas 分块布局，颜色走 --cate-* ========== */
/* @vort-if overseas.h5Style */
.productSort--overseas {
    .aside {
        background-color: var(--cate-side-bg, #f7f8fc);
        padding-top: 8rpx;

        .item {
            height: 72rpx;
            margin: 16rpx 12rpx;
            color: var(--cate-side-text, #555555);
            border-radius: 12rpx;
            display: flex;
            align-items: center;
            justify-content: center;
            box-sizing: border-box;

            &.on {
                background-color: var(--cate-side-active-bg, #fff);
                color: var(--cate-side-active-text, var(--cate-theme, #1a1a1a));
                font-weight: bold;
                box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
            }
        }
    }

    .conter {
        margin-left: 180rpx;
        margin-top: 0;
        padding: 16rpx 16rpx calc(40rpx + var(--tabbar-height));
        background-color: #f7f8fc;
        box-sizing: border-box;
        display: flex;
        flex-direction: column;
        row-gap: 20rpx;
        min-height: calc(100vh - var(--tabbar-height, 0px));

        .listw {
            width: 100%;
            padding: 24rpx;
            background-color: #fff;
            border-radius: 16rpx;
            box-sizing: border-box;

            .title {
                height: auto;
                min-height: 50rpx;
                margin-bottom: 20rpx;
                justify-content: flex-start;

                .name {
                    margin: 0;
                    font-size: 26rpx;
                    color: var(--cate-group-title, #1a1a1a);
                    font-weight: bold;
                }
            }
        }

        .list {
            display: flex;
            flex-wrap: wrap;
            gap: 24rpx 0;

            .item {
                width: 33.33%;
                margin-top: 0;
                box-sizing: border-box;

                .picture {
                    width: 120rpx;
                    height: 120rpx;
                    border-radius: 12rpx;
                    overflow: hidden;
                    background-color: #f9f9f9;
                }

                .name {
                    margin-top: 12rpx;
                    height: auto;
                    min-height: 32rpx;
                    line-height: 32rpx;
                    width: 100%;
                    padding: 0 8rpx;
                    box-sizing: border-box;
                    font-size: 22rpx;
                    color: var(--cate-name, #333);
                }
            }
        }
    }
}
/* @vort-endif */
</style>
