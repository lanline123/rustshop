<template>
    <!-- 后台装修「模块背景色」下发到 page，容器与 page 同色时不会露出默认白底 -->
    <page-meta :page-style="pageMetaStyle" />
    <tig-layout :title="layoutStyle === 2 ? '' : navbarTitle" :shop-id="filterParams.shopId" :shop-category-id="filterParams.shopCategoryId">
        <!-- layout-content 有 overflow:hidden，sticky 会失效；与下方搜索头一样用 fixed + 占位 -->
        <mainHeader v-if="layoutStyle === 2" class="main-header" position="fixed" />
        <view v-if="layoutStyle === 2" class="main-header-spacer" :style="mainHeaderSpacerStyle" />
        <view class="pageMain productSort" :style="themeVars">
            <!-- 装修配置到达前不渲染头部：否则会先按默认的国内版画一遍，拿到海外版配置后整块跳变 -->
            <view v-if="!decorateLoaded" class="decorate-placeholder" />
            <template v-else>
                <!-- 国内版：搜索头 + 排序筛选整块 fixed（sticky 在 layout 内会失效） -->
                <template v-if="layoutStyle === 1">
                    <view class="search-result-domestic-fixed" :style="topFixedStyle">
                        <headerDomestic
                            :keyword="filterParams.keyword"
                            :filter-params="filterParams"
                            :brand-name="brandName"
                            :coupon-name="couponInfo?.couponName"
                            :tree-list="treeList"
                            :intro-list="introList"
                            :show-back="false"
                            @to-search="toSearch"
                            @back="handleSearchBack"
                            @delete="del"
                            @delete-attr="delAttr"
                            @delete-category="delCategory"
                        />
                        <filterDomestic v-if="!isStoreStyle" :tab-list="tabList" :tab-index="tabIndex" @change-tab="onChangeTab" @show-drawer="showDrawer" />
                    </view>
                    <!-- 门店无筛选栏，占位更矮；白底挡住 page-meta 模块背景色，避免加载时出现黄/彩条空屏 -->
                    <view class="search-result-domestic-spacer" :class="{ 'search-result-domestic-spacer--store': isStoreStyle }" />
                </template>
                <template v-else>
                    <view class="search-result-overseas-fixed" :style="topFixedStyle">
                        <headerOverseas :keyword="filterParams.keyword" :scroll-left="keywordScrollLeft" @to-search="toSearch" />
                        <filterOverseas
                            v-if="!isStoreStyle"
                            :selected-attrs="attrs"
                            :sort="filterParams.sort"
                            :brand-list="brandList"
                            :brand-ids="brandIds"
                            :attr-list="attrsList"
                            :sort-popup="sortPopup"
                            :brand-popup="brandPopup"
                            :attr-popup="attrPopup"
                            :active-attr-index="activeAttrIndex"
                            @show-drawer="showOverseasFilter"
                            @handle-sort="handleSort"
                            @brand-click="brandClick"
                            @attr-click="attrClick"
                            @toggle-new="toggleNewProductFilter"
                        />
                    </view>
                    <view v-if="!isStoreStyle" class="search-result-overseas-spacer" />
                </template>

                <!-- 海外版下拉面板：排序 / 品牌 / 属性，点遮罩关闭 -->
                <template v-if="layoutStyle === 2">
                    <view v-if="sortPopup" class="popup-mask" :style="popupMaskStyle" @click="closeAllPopup">
                        <sortFilter @sort-change="getSortChange" />
                    </view>
                    <view v-if="brandPopup" class="popup-mask" :style="popupMaskStyle" @click="closeAllPopup">
                        <brandFilter
                            v-model="brandIds"
                            :brand-list="brandList"
                            :total="total"
                            :is-loading="isLoading"
                            @change="applyOverseasFilter"
                            @brand-reset="brandReset"
                            @close="closeAllPopup"
                        />
                    </view>
                    <view v-if="attrPopup && currentAttr" class="popup-mask" :style="popupMaskStyle" @click="closeAllPopup">
                        <attrFilter
                            :attr-item="currentAttr"
                            :selected-values="currentAttrSelectedValues"
                            :total="total"
                            :is-loading="isLoading"
                            @change="onOverseasAttrChange"
                            @attr-reset="attrReset"
                            @close="closeAllPopup"
                        />
                    </view>
                </template>
            </template>

            <!-- 门店聚合搜索 -->
            <template v-if="isStoreStyle">
                <view class="store-container" :style="storeContainerStyle">
                    <!-- 平台自营门店不计入 total，列表长度才是有关结果的依据 -->
                    <template v-if="!storeIsLoading && storeList.length > 0">
                        <template v-for="item in storeList" :key="item.shopId">
                            <storeSearchCard
                                :store="item"
                                manage-list-gap
                                :has-location="hasLocation"
                                :show-rating="storeDecorate.showRating"
                                :show-min-order="storeDecorate.showMinOrder"
                                :show-delivery-fee="storeDecorate.showDeliveryFee"
                                :show-distance="storeDecorate.showDistance"
                                :show-follow="storeDecorate.showFollow"
                                :show-product-name="storeDecorate.showProductName"
                                :product-name-row="storeDecorate.productNameRow"
                                :show-product-price="storeDecorate.showProductPrice"
                                :product-gap="storeDecorate.productGap"
                                :shop-title-color="storeDecorate.shopTitleColor"
                                :shop-logo-radius="storeDecorate.shopLogoRadius"
                                :card-radius="storeDecorate.cardRadius"
                                :card-padding-top-bottom="storeDecorate.cardPaddingTopBottom"
                                :card-padding-left-right="storeDecorate.cardPaddingLeftRight"
                                :shop-title-font-size="storeDecorate.shopTitleFontSize"
                                :shop-title-font-bold="storeDecorate.shopTitleFontBold"
                                :item-background-color="storeDecorate.itemBackgroundColor"
                                :theme-color="storeDecorate.themeColor"
                                :product-image-radius="storeDecorate.productImageRadius"
                                apply-card-style
                            />
                        </template>
                    </template>
                    <template v-if="!storeIsLoading && storeList.length === 0">
                        <view class="empty-container">
                            <image class="empty-image" :src="staticResource('empty/emptyProduct.png')" mode="aspectFit" />
                            <text>{{ $t("没有找到相关的门店") }}</text>
                            <tig-button2 class="empty-btn" @click="toHomePage">{{ $t("返回首页") }}</tig-button2>
                        </view>
                    </template>
                </view>
                <loading-box v-model="storeIsLoadMore" :page="storeParams.page" :length="storeList.length" />
            </template>

            <!-- 加载商品模块 -->
            <template v-if="!isStoreStyle">
                <template v-if="!isLoading && total > 0">
                    <view class="goods-container" :style="goodsContainerStyle">
                        <!-- 单列（listStyle=2）走搜索页专用列表组件，双列沿用通用瀑布流 -->
                        <searchProductList v-if="listStyle === 2" :product-list="productList" :promotion-list="promotionList" :config="decorateConfig" />
                        <masonry
                            v-else
                            :commodity-list="productList"
                            :promotion-list="promotionList"
                            :style-map="styleMap"
                            :product-title-line="listDecorate.productTitleLine"
                            :buy-btn-style="listDecorate.buyBtnStyle"
                            :show-coupon="listDecorate.showCoupon"
                            :price-color="listDecorate.priceColor"
                            :product-title-color="listDecorate.productTitleColor"
                            :product-image-radius="listDecorate.productImageRadius"
                            :item-background-color="listDecorate.itemBackgroundColor"
                            :theme-color="listDecorate.themeColor"
                        />
                    </view>
                </template>
                <template v-if="!isLoading && total === 0">
                    <view class="empty-container">
                        <image class="empty-image" :src="staticResource('empty/emptyProduct.png')" mode="aspectFit" />
                        <text>{{ $t("没有找到相关的商品") }}</text>
                        <tig-button2 class="empty-btn" @click="toHomePage">{{ $t("返回首页") }}</tig-button2>
                    </view>
                </template>

                <loading-box v-model="isLoadMore" :page="filterParams.page" :length="productList.length" />
            </template>

            <!-- 国内版：右侧筛选抽屉 -->
            <drawerDomestic
                v-if="layoutStyle === 1"
                v-model:show="showDrawerRef"
                :shop-id="filterParams.shopId"
                :category-list="categoryList"
                :shop-category-list="shopCategoryList"
                :brand-list="brandList"
                :brand-ids="brandIds"
                :attrs-list="attrsList"
                :attrs="attrs"
                :cat="filterParams.cat"
                :shop-category-id="filterParams.shopCategoryId"
                :min="filterParams.min"
                :max="filterParams.max"
                @submit="handleDomesticDrawerSubmit"
                @reset="handleDomesticDrawerReset"
            />

            <!-- 海外版：底部「所有筛选条件」弹层 -->
            <drawerOverseas
                v-if="layoutStyle === 2"
                ref="overseasDrawerRef"
                v-model:show="showAttrRef"
                :brand-list="brandList"
                :brand-ids="brandIds"
                :attrs-list="attrsList"
                :attrs="attrs"
                :sort="filterParams.sort"
                :max="filterParams.max"
                :max-price="maxPrice"
                :total="total"
                :is-loading="isLoading"
                @brand-change="onOverseasBrandChange"
                @attr-click="onOverseasPopupAttrClick"
                @toggle-new="toggleNewProductFilter"
                @reset="handleOverseasReset"
                @slider-many="sliderMany"
            />
        </view>
    </tig-layout>
</template>

<script lang="ts" setup>
/**
 * 搜索结果页
 * - 展示商品搜索结果
 * - 支持分类、品牌、属性筛选
 * - 支持价格排序、销量排序
 */
import { ref, reactive, computed, nextTick, watch } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import masonry from "@/components/masonry/masonry.vue";
import type { Brand, filterSeleted, ProductFilterParams, attrsData, attrsListData } from "@/types/search/search";
import type { StoreSearchParams, StoreSearchRecord } from "@/types/search/storeSearch";
import { getCategoryTree, getCategoryProductFilter, getCategoryProduct, getShopCategoryTree, getSearchEsFilter, getStoreProduct } from "@/api/search/search";
import { getMobileSearchStyle } from "@/api/common";
import { useList } from "@/hooks";
import { useProductStyle } from "@/hooks/useProductStyle";
import { useI18n } from "vue-i18n";
import headerDomestic from "./components/search-result/header-domestic.vue";
import headerOverseas from "./components/search-result/header-overseas.vue";
import filterDomestic from "./components/search-result/filter-domestic.vue";
import filterOverseas from "./components/search-result/filter-overseas.vue";
import drawerDomestic from "./components/search-result/drawer-domestic.vue";
import drawerOverseas from "./components/search-result/drawer-overseas.vue";
import sortFilter, { type SortRadioOption } from "./components/sortFilter.vue";
import brandFilter from "./components/brandFilter.vue";
import attrFilter from "./components/attrFilter.vue";
import searchProductList from "./components/search-result/list.vue";
import storeSearchCard from "@/components/storeSearchCard/index.vue";
import { useBrandSelection, useAttrsSelection } from "./composables";
import { useConfigStore } from "@/store/config";
import { useRegionStore } from "@/store/region";
import { useSearchDecorateStore } from "@/store/searchDecorate";
import { staticResource } from "@/utils";
import mainHeader from "@/components/mainHeader/index.vue";
import { setPageMeta } from "@/utils/seo";

const configStore = useConfigStore();
const regionStore = useRegionStore();

const { t } = useI18n();

// ===================== 搜索页装修配置（Decorate 页 H5 + pageType=6）=====================
const searchDecorateStore = useSearchDecorateStore();
const decorateConfig = computed(() => searchDecorateStore.config);
/** 后台未配置过搜索页装修时，页面保持接入装修前的原样 */
const hasDecorate = computed(() => searchDecorateStore.hasDecorate);
/** 装修接口是否已返回（成功或失败都算），用于避免头部按默认布局先画一遍 */
const decorateLoaded = computed(() => searchDecorateStore.isLoaded);
/** 后台按 px 配置，uniapp 按 rpx 渲染，换算比例固定 1:2 */
const toRpx = (px: number | undefined, fallback: number) => `${(px ?? fallback) * 2}rpx`;

// 后台「模块背景色」下发到 page；列表数据未到之前不铺色，避免搜索头下方先闪出彩条空屏
const pageMetaStyle = computed(() => {
    if (!hasDecorate.value) return "";
    // isStoreStyle / *IsLoading 在下方声明，computed 惰性求值，首次渲染前已完成初始化
    const waiting = isStoreStyle.value ? storeIsLoading.value : isLoading.value;
    if (waiting) return "";
    const bg = decorateConfig.value.backgroundColor;
    return bg ? `background-color:${bg}` : "";
});

// 搜索页主容器：主题变量 + 模块背景。
// 背景直接落到 pageMain，并撑满导航以下的剩余视口；否则 H5 的 tig-layout 白底会在短列表下方露出。
// 搜索头占位自身有白底，因此模块背景仍从结果区开始，与 Admin 预览一致。
const themeVars = computed(() => {
    if (!hasDecorate.value) return {};
    const cfg = decorateConfig.value;
    const waiting = isStoreStyle.value ? storeIsLoading.value : isLoading.value;
    const style: Record<string, string> = {
        "--search-theme": cfg.themeColor || "var(--general)",
        "--search-price-color": cfg.priceColor || cfg.themeColor || "var(--general)",
        "--search-item-bg": cfg.searchItemBackgroundColor || "#ffffff",
        "--search-item-padding-lr": toRpx(cfg.searchItemPaddingLeftRight, 10),
        minHeight: layoutStyle.value === 2 ? `calc(100vh - ${configStore.commonHeaderHeight || 0}px)` : "calc(100vh - var(--nav-height))"
    };
    if (!waiting && cfg.backgroundColor) {
        style.backgroundColor = cfg.backgroundColor;
    }
    return style;
});

// 商品容器：外边距 / 内边距 / 圆角均来自后台
const goodsContainerStyle = computed(() => {
    if (!hasDecorate.value) return {};
    const cfg = decorateConfig.value;
    const paddingTopBottom = cfg.paddingTopBottom ?? cfg.padding ?? 10;
    const paddingLeftRight = cfg.paddingLeftRight ?? cfg.padding ?? 10;
    return {
        margin: toRpx(cfg.margin, 0),
        padding: `${toRpx(paddingTopBottom, 10)} ${toRpx(paddingLeftRight, 10)}`,
        borderRadius: toRpx(cfg.moduleRadius, 0),
        backgroundColor: cfg.backgroundColor || ""
    };
});

/** 门店列表容器：外边距 / 内边距与商品列表同口径（对齐 Admin __content） */
const storeContainerStyle = computed(() => {
    if (!hasDecorate.value) return {};
    const cfg = decorateConfig.value;
    const paddingTopBottom = cfg.paddingTopBottom ?? cfg.padding ?? 10;
    const paddingLeftRight = cfg.paddingLeftRight ?? cfg.padding ?? 10;
    return {
        margin: toRpx(cfg.margin, 0),
        padding: `${toRpx(paddingTopBottom, 10)} ${toRpx(paddingLeftRight, 10)}`,
        rowGap: toRpx(cfg.storeGap, 10)
    };
});

/** 列表样式：1-双列瀑布流，2-单列列表 */
const listStyle = computed(() => Number(decorateConfig.value.listStyle ?? 1));

/**
 * 透传给通用瀑布流的装修字段。
 * masonry 被首页/门店/分类等 8 个页面共用，未配置装修时这里全给 undefined，
 * 组件内的 CSS fallback 保证那些页面视觉不变。
 */
const listDecorate = computed(() => {
    if (!hasDecorate.value) return {} as Record<string, never>;
    const cfg = decorateConfig.value;
    return {
        productTitleLine: cfg.productTitleLine,
        buyBtnStyle: cfg.buyBtnStyle,
        showCoupon: cfg.showCoupon,
        priceColor: cfg.priceColor,
        productTitleColor: cfg.productTitleColor,
        productImageRadius: cfg.productImageRadius,
        itemBackgroundColor: cfg.itemBackgroundColor,
        themeColor: cfg.themeColor
    };
});
/**
 * 透传给门店卡片的装修字段。
 * 未配置装修时全给 undefined，卡片自身的 props 默认值保证观感不变。
 * 扩展设置（商品项背景色 / 主题色 / 商品卡圆角）与商品列表共用字段。
 */
const storeDecorate = computed(() => {
    if (!hasDecorate.value) return {} as Record<string, never>;
    const cfg = decorateConfig.value;
    return {
        showRating: cfg.storeShowRating,
        showMinOrder: cfg.storeShowMinOrder,
        showDeliveryFee: cfg.storeShowDeliveryFee,
        showDistance: cfg.storeShowDistance,
        showFollow: cfg.storeShowFollow,
        showProductName: cfg.storeShowProductName,
        productNameRow: cfg.storeProductNameRow,
        showProductPrice: cfg.storeShowProductPrice,
        productGap: cfg.storeProductGap,
        shopLogoRadius: cfg.storeLogoRadius,
        cardRadius: cfg.storeCardRadius,
        cardPaddingTopBottom: cfg.storePaddingTopBottom,
        cardPaddingLeftRight: cfg.storePaddingLeftRight,
        shopTitleColor: cfg.storeShopTitleColor,
        shopTitleFontSize: cfg.storeShopTitleFontSize,
        shopTitleFontBold: cfg.storeShopTitleFontBold,
        itemBackgroundColor: cfg.itemBackgroundColor,
        themeColor: cfg.themeColor,
        productImageRadius: cfg.productImageRadius
    };
});

/** 海外版顶部栏占位（fixed 后脱离文档流） */
const mainHeaderSpacerStyle = computed(() => {
    if (layoutStyle.value === 2) {
        return { height: `${configStore.commonHeaderHeight || 0}px` };
    }
    return undefined;
});

/**
 * 搜索顶栏 fixed 偏移。
 * - 国内：CSS 默认 top: var(--nav-height)
 * - 海外版：顶在 mainHeader 下方
 */
const topFixedStyle = computed(() => {
    if (layoutStyle.value === 2) {
        return { top: `${configStore.commonHeaderHeight || 0}px` };
    }
    return undefined;
});

/** 海外版筛选弹层遮罩 top：按 mainHeader 高度定位 */
const popupMaskStyle = computed(() => {
    if (layoutStyle.value === 2) {
        return { top: `calc(${configStore.commonHeaderHeight || 0}px + 176rpx)` };
    }
    return undefined;
});

const toHomePage = () => {
    uni.switchTab({
        url: "/pages/index/index"
    });
};

const navbarTitle = ref(t("商品搜索"));

const filterParams = reactive<ProductFilterParams>({
    //初始化用于查询的参数
    page: 1,
    size: 10,
    sort: "",
    order: "asc",
    keyword: "",
    max: "",
    min: "",
    cat: 0,
    couponId: 0,
    brand: [],
    attrs: [],
    intro: "",
    pageType: "search"
});
const brandList = ref<Brand[]>([]);
const brandIds = ref<number[]>([]);
const brandName = ref("");
const categoryList = ref<filterSeleted[]>([]);
const shopCategoryList = ref<filterSeleted[]>([]);
const categoryTree = ref<filterSeleted[]>([]);
const attrsList = ref<attrsListData[]>([]);
const attrs = ref<attrsData[]>([]);
const maxPrice = ref(0);
const introList: { [key: string]: string } = {
    new: t("新品"),
    hot: t("热销"),
    best: t("精品")
};

/** 搜索框内分类路径：平台分类走 parentTree，店铺分类走店铺 parentTree */
const treeList = computed(() => {
    if (filterParams.cat) {
        return categoryTree.value;
    }
    if (filterParams.shopCategoryId) {
        return shopCategoryList.value;
    }
    return [];
});

const keywordScrollLeft = ref(0);
const scrollKeywordToEnd = () => {
    keywordScrollLeft.value = 0;
    nextTick(() => {
        keywordScrollLeft.value = 99999;
    });
};
watch(
    () => filterParams.keyword,
    () => {
        scrollKeywordToEnd();
    }
);
const {
    getList,
    total,
    data: productList,
    promotionList,
    isLoadMore,
    isLoading
} = useList(getCategoryProduct, {
    params: filterParams,
    path: {
        dataKey: "records"
    },
    needPromotionList: true
});
const { styleMap } = useProductStyle(productList);

// 后台「移动端搜索风格」接口值；0 表示尚未取到配置
const configSearchStyle = ref(0);
/** 搜索风格：1 普通商品搜索，2 门店聚合搜索。配置过装修以装修为准，否则回落后台接口 */
const searchStyle = computed(() => {
    if (hasDecorate.value) {
        return Number(decorateConfig.value.searchResultType) === 2 ? 2 : 1;
    }
    return configSearchStyle.value;
});
const isStoreStyle = computed(() => searchStyle.value === 2);

/** 布局样式：1-国内版，2-海外版；门店聚合搜索固定国内版搜索头 */
const layoutStyle = computed(() => {
    if (isStoreStyle.value) return 1;
    return Number(decorateConfig.value.layoutStyle ?? 1);
});

const storeParams = reactive<StoreSearchParams>({
    page: 1,
    size: 5
});

const {
    getList: getStoreList,
    data: storeList,
    isLoading: storeIsLoading,
    isLoadMore: storeIsLoadMore
} = useList<StoreSearchRecord>(getStoreProduct, {
    params: storeParams,
    path: {
        dataKey: "records"
    }
});

const currentLocationParams = computed(() => {
    const longitude = Number(regionStore.currentLocationInfo?.longitude);
    const latitude = Number(regionStore.currentLocationInfo?.latitude);
    const isValidLocation =
        Number.isFinite(longitude) &&
        Number.isFinite(latitude) &&
        longitude >= -180 &&
        longitude <= 180 &&
        latitude >= -90 &&
        latitude <= 90 &&
        (longitude !== 0 || latitude !== 0);

    return isValidLocation ? { longitude, latitude } : {};
});

// 未定位时接口返回的距离不可信，不展示
const hasLocation = computed(() => "longitude" in currentLocationParams.value && "latitude" in currentLocationParams.value);

const getProductRequestParams = (extraData: Record<string, any> = {}) => ({
    brand: brandIds.value.join(","),
    ...extraData,
    ...currentLocationParams.value
});

const loadStoreList = () => {
    const extraData: Record<string, any> = {
        keyword: filterParams.keyword,
        ...currentLocationParams.value
    };
    if (filterParams.shopId) {
        extraData.shopId = filterParams.shopId;
    }
    getStoreList(extraData);
};

const loadSearchStyle = async () => {
    try {
        const result = await getMobileSearchStyle();
        configSearchStyle.value = Number(result) === 2 ? 2 : 1;
    } catch (error) {
        console.error(error);
        configSearchStyle.value = 1;
    }
};

// 搜索数据加载完成后设置 SEO meta（仅 H5）
watch(
    isLoading,
    (val) => {
        if (!val) {
            // #ifdef H5
            setPageMeta({
                title: `"${filterParams.keyword}" 的搜索结果 - ${configStore.baseInfo.shopTitle}`,
                description: `在${configStore.baseInfo.shopTitle}搜索"${filterParams.keyword}"的相关商品`,
                robots: "noindex,follow"
            });
            // #endif
        }
    },
    { immediate: true }
);

// 获取列表的查询结果
const loadFilter = async () => {
    try {
        filterParams.brand = brandIds.value.join(",");
        const productFilter = await getCategoryProductFilter({ ...filterParams });
        const listFilter = productFilter.filter ?? {};
        const selected = productFilter.filterSelected ?? {};

        brandList.value = Array.isArray(listFilter.brand) ? listFilter.brand : [];
        // 已选分类后，后端按「子分类」返回；叶子分类会是 []。此时保留上一份列表，避免抽屉里分类整块消失
        const nextCategory = Array.isArray(listFilter.category) ? listFilter.category : [];
        if (nextCategory.length > 0 || !filterParams.cat) {
            categoryList.value = nextCategory;
        }
        shopCategoryList.value = Array.isArray(listFilter.shopCategory) ? listFilter.shopCategory : [];

        const rawMax = listFilter.maxPrice ?? (listFilter as { max_price?: number }).max_price;
        maxPrice.value = rawMax === undefined || rawMax === null || rawMax === "" ? 0 : typeof rawMax === "string" ? Number(rawMax) : Number(rawMax);
        brandName.value = selected.brand || "";
        filterParams.attrs = selected.attrs;
        attrs.value = Array.isArray(selected.attrs)
            ? selected.attrs.map((item: attrsData) => ({
                  attrName: item.attrName,
                  attrValue: [...(item.attrValue || [])]
              }))
            : [];
        if (filterParams.cat) {
            const tree = await getCategoryTree(filterParams.cat);
            categoryTree.value = Array.isArray(tree) ? tree : [];
        } else {
            categoryTree.value = [];
        }
        if (filterParams.shopCategoryId) {
            const shopTree = await getShopCategoryTree(filterParams.shopCategoryId);
            shopCategoryList.value = Array.isArray(shopTree) ? shopTree : [];
        }
    } catch (error: any) {
        uni.showToast({
            title: error.message,
            icon: "none"
        });
    }
};
const tabIndex = ref("default");
const tabList = ref([
    {
        label: t("默认"),
        value: "default"
    },
    {
        label: t("销量"),
        value: "sale"
    },
    {
        label: t("价格"),
        value: "price",
        order: "desc"
    }
]);
const onChangeTab = (item: any) => {
    if (item.value == "price") {
        item.order = item.order === "desc" ? "asc" : "desc";
    } else {
        item.order = "";
    }
    tabIndex.value = item.value;
    filterParams.sort = item.value;
    filterParams.order = item.order;
    loadFilter();
    resetList();
    getList(() => getProductRequestParams());
};

const del = (type: string) => {
    switch (type) {
        case "brand":
            brandIds.value = [];
            brandName.value = "";
            break;
        case "price":
            filterParams.min = "";
            filterParams.max = "";
            break;
        case "keyword":
            filterParams.keyword = "";
            break;
        case "intro":
            filterParams.intro = "";
            break;
        case "couponInfo":
            couponInfo.value = {};
            filterParams.couponId = 0;
            break;
    }
    _getSearchFilter();
    loadFilter();
    resetList();
    getList(() => getProductRequestParams());
};

const delAttr = (index: number) => {
    filterParams.attrs.splice(index, 1);
    attrs.value = filterParams.attrs;
    _getSearchFilter();
    loadFilter();
    resetList();
    getList(() => getProductRequestParams({ attrs: attrs.value }));
};

/** 点掉搜索框内某一级分类：index=0 清空，否则回退到上一级 */
const delCategory = (index: number) => {
    if (index > 0) {
        if (filterParams.shopCategoryId) {
            filterParams.shopCategoryId = shopCategoryList.value[index - 1].categoryId;
        } else {
            filterParams.cat = categoryTree.value[index - 1].categoryId;
        }
    } else {
        if (filterParams.shopCategoryId) {
            filterParams.shopCategoryId = 0;
        } else {
            filterParams.cat = 0;
        }
    }
    _getSearchFilter();
    loadFilter();
    resetList();
    getList(() => getProductRequestParams());
};

const showDrawerRef = ref<boolean>(false);
const showDrawer = () => {
    showDrawerRef.value = true;
};

const toSearch = () => {
    // 跳转到搜索页面，传递关键词和店铺ID
    let url = "/pages/search/index";
    const params: string[] = [];
    if (filterParams.shopId) {
        params.push("shopId=" + filterParams.shopId);
    }
    if (filterParams.keyword) {
        params.push("keyWords=" + filterParams.keyword);
    }
    if (params.length > 0) {
        url += "?" + params.join("&");
    }
    uni.redirectTo({
        url
    });
};

/** 跨境头无系统返回时，搜索框左侧返回（国内/海外装修共用） */
const handleSearchBack = () => {
    const pages = getCurrentPages();
    if (pages.length > 1) {
        uni.navigateBack();
        return;
    }
    uni.switchTab({
        url: "/pages/index/index"
    });
};
const couponInfo = ref<any>({});

const _getSearchFilter = async () => {
    if (!configStore.enableAttributeFilter) {
        return;
    }
    try {
        const result = await getSearchEsFilter({ keyword: filterParams.keyword, cat: filterParams.cat });
        attrsList.value = result.attributes;
    } catch (error) {
        console.error(error);
    }
};

onLoad(async (option: any) => {
    // 搜索风格由装修决定，须先拿到配置再决定拉商品还是拉门店；previewId 存在时读草稿（后台预览链路）
    await searchDecorateStore.fetchConfig(option?.previewId);
    if (option) {
        if (option.keyWords) {
            filterParams.keyword = option.keyWords;
        }
        if (option.intro) {
            filterParams.intro = option.intro;
        }
        const categoryId = Number(option.cat ?? option.categoryId);
        if (Number.isFinite(categoryId) && categoryId > 0) {
            filterParams.cat = categoryId;
        }
        if (option.brandId) {
            brandIds.value.push(option.brandId);
        }
        if (option.shopId) {
            filterParams.shopId = option.shopId;
            navbarTitle.value = t("店铺商品搜索");
        }
        if (option.shopCategoryId) {
            filterParams.shopCategoryId = option.shopCategoryId;
        }
        if (option.couponInfo) {
            couponInfo.value = JSON.parse(decodeURIComponent(option.couponInfo));
            filterParams.couponId = couponInfo.value.couponId;
        }
    }
    if (!hasDecorate.value) {
        await loadSearchStyle();
    }
    if (isStoreStyle.value) {
        loadStoreList();
        return;
    }
    // H5 刷新/小程序冷启动时，全局配置可能尚未完成初始化。
    // 等待配置就绪后再判断属性筛选开关，避免默认值 0 导致属性筛选被永久跳过。
    await configStore.whenConfigReady();
    await _getSearchFilter();
    loadFilter();
    getList(() => getProductRequestParams());
});
const { onChangeBrand } = useBrandSelection(brandIds);
const { onAttrClick } = useAttrsSelection(attrs);

/** 国内版抽屉提交：回写草稿并刷新列表 */
const handleDomesticDrawerSubmit = async (data: {
    brandIds: number[];
    attrs: attrsData[];
    cat: number | string;
    shopCategoryId: number | string;
    min: string | number;
    max: string | number;
}) => {
    brandIds.value = data.brandIds;
    attrs.value = data.attrs;
    filterParams.attrs = data.attrs;
    filterParams.cat = Number(data.cat) || 0;
    filterParams.shopCategoryId = data.shopCategoryId as number | undefined;
    filterParams.min = data.min;
    filterParams.max = data.max;
    await loadFilter();
    resetList();
    getList(() => getProductRequestParams({ attrs: attrs.value }));
};

/** 国内版抽屉重置：只清条件，等点「确定」再出结果 */
const handleDomesticDrawerReset = () => {
    filterParams.min = "";
    filterParams.max = "";
    brandIds.value = [];
    attrs.value = [];
    filterParams.attrs = [];
    filterParams.cat = 0;
    filterParams.shopCategoryId = undefined;
    categoryTree.value = [];
};

// ===================== 海外版筛选（layoutStyle=2）=====================
const sortPopup = ref(false);
const brandPopup = ref(false);
const attrPopup = ref(false);
const activeAttrIndex = ref(-1);
const showAttrRef = ref(false);
const overseasDrawerRef = ref<{ initSlider?: () => void } | null>(null);

const currentAttr = computed(() => attrsList.value[activeAttrIndex.value]);
const currentAttrSelectedValues = computed(() => {
    const name = currentAttr.value?.name;
    if (!name) return [];
    return attrs.value.find((item) => item.attrName === name)?.attrValue ?? [];
});

const closeAllPopup = () => {
    sortPopup.value = false;
    brandPopup.value = false;
    attrPopup.value = false;
    activeAttrIndex.value = -1;
};

/** 打开海外版「所有筛选条件」底部弹层 */
const showOverseasFilter = () => {
    closeAllPopup();
    showAttrRef.value = true;
};

/** 面板互斥：打开一个即关掉其它，避免多层遮罩叠加 */
const openPopup = (target: "sort" | "brand" | "attr", attrIndex = -1) => {
    const next = {
        sort: target === "sort" ? !sortPopup.value : false,
        brand: target === "brand" ? !brandPopup.value : false,
        // 点同一属性再次收起，点另一属性直接切过去
        attr: target === "attr" ? !(attrPopup.value && activeAttrIndex.value === attrIndex) : false
    };
    sortPopup.value = next.sort;
    brandPopup.value = next.brand;
    attrPopup.value = next.attr;
    activeAttrIndex.value = next.attr ? attrIndex : -1;
};

const handleSort = () => openPopup("sort");
const brandClick = () => openPopup("brand");
const attrClick = (_attr: attrsListData, index: number) => openPopup("attr", index);

/** 海外面板改动后立即出结果，不像国内抽屉那样等「确定」 */
const applyOverseasFilter = () => {
    filterParams.attrs = attrs.value;
    loadFilter();
    resetList();
    getList(() => {
        return { brand: brandIds.value.join(","), attrs: attrs.value };
    });
};

const onOverseasBrandChange = (item: Brand) => {
    onChangeBrand(item);
    applyOverseasFilter();
};

const onOverseasPopupAttrClick = (name: string, value: string) => {
    onAttrClick(name, value);
    applyOverseasFilter();
};

const sliderMany = (e: { minPrice: number; maxPrice: number }) => {
    filterParams.min = e.minPrice;
    filterParams.max = e.maxPrice;
    filterParams.sort = "price";
    applyOverseasFilter();
};

const handleOverseasReset = () => {
    filterParams.min = "";
    filterParams.max = "";
    filterParams.sort = "";
    filterParams.order = "asc";
    filterParams.attrs = [];
    attrs.value = [];
    brandIds.value = [];
    filterParams.cat = 0;
    overseasDrawerRef.value?.initSlider?.();
    _getSearchFilter();
    applyOverseasFilter();
};

const getSortChange = (option: SortRadioOption) => {
    filterParams.sort = option.sort;
    filterParams.order = option.value;
    closeAllPopup();
    applyOverseasFilter();
};

const brandReset = () => {
    brandIds.value = [];
    applyOverseasFilter();
};

const onOverseasAttrChange = (value: string) => {
    const name = currentAttr.value?.name;
    if (!name) return;
    onAttrClick(name, value);
    applyOverseasFilter();
};

const attrReset = () => {
    const name = currentAttr.value?.name;
    if (!name) return;
    attrs.value = attrs.value.filter((item) => item.attrName !== name);
    applyOverseasFilter();
};

/** 「新品」pill：按 sort=time 切换，再点一次回默认排序 */
const toggleNewProductFilter = () => {
    closeAllPopup();
    if (filterParams.sort === "time") {
        filterParams.sort = "";
        filterParams.order = "asc";
    } else {
        filterParams.sort = "time";
        filterParams.order = "desc";
    }
    applyOverseasFilter();
};

const resetList = () => {
    total.value = 0;
    filterParams.page = 1;
    filterParams.intro = "";
    productList.value = [];
    promotionList.value = {};
};
</script>

<style lang="scss" scoped>
/* 装修配置到达前的占位：搜索头 99rpx + 排序栏约 77rpx，避免头部渲染出来时页面整体上跳 */
.decorate-placeholder {
    height: 176rpx;
    background-color: #fff;
}

/* 海外版 mainHeader 为 fixed，高度由 inline style 写入 */
.main-header-spacer {
    width: 100%;
    flex-shrink: 0;
}

/* 国内版：搜索头 + 排序筛选整块钉在导航下方；外贸皮肤由 topFixedStyle 覆盖 top */
.search-result-domestic-fixed,
.search-result-overseas-fixed {
    position: fixed;
    left: 0;
    right: 0;
    width: 100%;
    box-sizing: border-box;
    top: var(--nav-height);
    z-index: 11;
    background-color: var(--search-item-bg, #fff);

    /*
     * 导航栏与搜索区是两个独立 fixed 图层。nav-height 由状态栏 px 换算为 rpx，
     * H5 在小数像素边界分别栅格化时偶发不足 1px 的透明缝；向上覆盖 1px 消除透底，
     * 不调整 top，避免搜索区及列表整体位移。
     */
    &::before {
        content: "";
        position: absolute;
        left: 0;
        right: 0;
        top: -1px;
        height: 1px;
        background-color: var(--search-item-bg, #fff);
        pointer-events: none;
    }
}

/* 给 fixed 顶栏腾出文档流高度，避免商品列表被挡住；白底挡住 page-meta 模块背景色 */
.search-result-domestic-spacer,
.search-result-overseas-spacer {
    height: 176rpx;
    width: 100%;
    background-color: var(--search-item-bg, #fff);
}

/* 门店无筛选栏，仅搜索头约 99rpx */
.search-result-domestic-spacer--store {
    height: 99rpx;
}

/* 海外版下拉面板遮罩：从筛选栏下方铺满剩余屏幕 */
.popup-mask {
    position: fixed;
    left: 0;
    right: 0;
    /* fixed 搜索头 99rpx + 筛选栏一行约 77rpx */
    top: calc(var(--nav-height) + 176rpx);
    bottom: 0;
    background-color: rgba(0, 0, 0, 0.5);
    z-index: 100;
}
.goods-container {
    /* padding/margin/圆角由后台装修下发（goodsContainerStyle），这里只保留兜底 */
    padding: 20rpx;
    box-sizing: border-box;
}
.store-container {
    /* padding/margin/门店间距由后台装修下发（storeContainerStyle），这里只保留兜底 */
    display: flex;
    flex-direction: column;
    row-gap: 20rpx;
    padding: 20rpx;
    box-sizing: border-box;
}
.empty-container {
    padding-top: 150rpx;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    .empty-image {
        height: 300rpx;
        flex-shrink: 0;
    }
    text {
        font-size: 28rpx;
        color: #999;
    }
    .empty-btn {
        width: auto;
        min-width: 200rpx;
        height: 60rpx;
        padding: 0 28rpx;
        white-space: nowrap;
        border: 1px solid var(--search-theme, var(--general));
        color: var(--search-theme, var(--general));
        border-radius: 30rpx;
        font-size: 26rpx;
        margin-top: 40rpx;
    }
}
.sousuo {
    color: #c3c0c0 !important;
}
</style>
