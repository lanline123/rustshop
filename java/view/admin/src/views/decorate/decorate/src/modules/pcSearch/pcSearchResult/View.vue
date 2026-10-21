<template>
    <div class="module-ad-con dec-pc-search-result" :class="contentStyleClass" :style="cssVars">
        <div v-if="!isFilterSide" class="dec-pc-search-result__top-filter">
            <div class="dec-pc-search-result__top-filter-body">
                <div class="dec-pc-search-result__category-list">
                    <div
                        v-if="showCategoryLeftArrow"
                        class="dec-pc-search-result__category-arrow dec-pc-search-result__category-arrow--left"
                        @click="scrollCategory('left')"
                    >
                        <span class="dec-pc-search-result__category-arrow-btn">‹</span>
                    </div>
                    <div ref="categoryTrackRef" class="dec-pc-search-result__category-track" @scroll="updateCategoryArrows">
                        <div
                            v-for="(item, index) in topFilterCategories"
                            :key="item.id"
                            class="dec-pc-search-result__category-card"
                            :class="{ 'dec-pc-search-result__category-card--active': index === 0 }"
                        >
                            <img v-if="item.image" class="dec-pc-search-result__category-card-image" :src="item.image" :alt="item.name" />
                            <span class="dec-pc-search-result__category-card-name" :style="{ paddingLeft: item.image ? '0' : '12px' }">{{ item.name }}</span>
                        </div>
                    </div>
                    <div
                        v-if="showCategoryRightArrow"
                        class="dec-pc-search-result__category-arrow dec-pc-search-result__category-arrow--right"
                        @click="scrollCategory('right')"
                    >
                        <span class="dec-pc-search-result__category-arrow-btn">›</span>
                    </div>
                </div>
            </div>
        </div>
        <div class="dec-pc-search-result__main">
            <div v-if="isFilterSide" class="dec-pc-search-result__sidebar">
                <div class="dec-pc-search-result__sidebar-title">筛选</div>
                <div class="dec-pc-search-result__filter-group">
                    <div class="dec-pc-search-result__filter-label">价格区间</div>
                    <div class="dec-pc-search-result__price-range">
                        <div class="dec-pc-search-result__price-text-row">
                            <span class="dec-pc-search-result__price-text">0</span>
                            <span class="dec-pc-search-result__price-text">1000</span>
                        </div>
                        <div class="dec-pc-search-result__price-slider-row">
                            <div class="dec-pc-search-result__price-slider">
                                <div class="dec-pc-search-result__price-track"></div>
                                <div class="dec-pc-search-result__price-handle dec-pc-search-result__price-handle--left"></div>
                                <div class="dec-pc-search-result__price-handle dec-pc-search-result__price-handle--right"></div>
                            </div>
                            <button class="dec-pc-search-result__price-btn">确定</button>
                        </div>
                    </div>
                </div>
                <div v-for="group in sidebarFilterGroups" :key="group.name" class="dec-pc-search-result__filter-group">
                    <div class="dec-pc-search-result__filter-label dec-pc-search-result__filter-label--expandable">
                        {{ group.name }}
                        <svg class="dec-pc-search-result__filter-icon" viewBox="0 0 14 14" width="24" height="24" aria-hidden="true">
                            <path
                                d="M10.063 5.469 7 8.53 3.938 5.47"
                                fill="none"
                                stroke="currentColor"
                                stroke-linecap="round"
                                stroke-linejoin="round"
                                stroke-width="1.313"
                            />
                        </svg>
                    </div>
                    <div class="dec-pc-search-result__filter-options">
                        <label v-for="option in group.options" :key="option" class="dec-pc-search-result__checkbox-item">
                            <span class="dec-pc-search-result__checkbox"></span>
                            <span>{{ option }}</span>
                        </label>
                    </div>
                </div>
            </div>
            <div class="dec-pc-search-result__content">
                <div v-if="!isFilterSide" class="dec-pc-search-result__tabs">
                    <div class="dec-pc-search-result__tabs-left">
                        <div class="dec-pc-search-result__tab-item dec-pc-search-result__tab-item--active">全部商品</div>
                        <div class="dec-pc-search-result__tab-item">店铺</div>
                    </div>
                    <div class="dec-pc-search-result__tabs-right">
                        <div class="dec-pc-search-result__page-info">
                            <span class="dec-pc-search-result__page-arrow dec-pc-search-result__page-arrow--disabled">‹</span>
                            <span class="dec-pc-search-result__page-number">
                                <em class="dec-pc-search-result__page-current">1</em>
                                <span class="dec-pc-search-result__page-separator">/</span>
                                <span class="dec-pc-search-result__page-total">19</span>
                            </span>
                            <span class="dec-pc-search-result__page-arrow">›</span>
                        </div>
                    </div>
                </div>
                <div v-if="isFilterSide" class="dec-pc-search-result__side-sort">
                    <div class="dec-pc-search-result__side-sort-item dec-pc-search-result__side-sort-item--active">
                        <span>推荐</span>
                        <span class="dec-pc-search-result__side-sort-icon">
                            <span class="dec-pc-search-result__side-tri dec-pc-search-result__side-tri--active"></span>
                        </span>
                    </div>
                    <div class="dec-pc-search-result__side-sort-item">
                        <span>价格</span>
                        <span class="dec-pc-search-result__side-sort-icon">
                            <span class="dec-pc-search-result__side-tri dec-pc-search-result__side-tri--up"></span>
                            <span class="dec-pc-search-result__side-tri"></span>
                        </span>
                    </div>
                </div>
                <div v-else class="dec-pc-search-result__sort">
                    <div class="dec-pc-search-result__sort-left">
                        <div class="dec-pc-search-result__sort-item dec-pc-search-result__sort-item--active">默认排序</div>
                        <div class="dec-pc-search-result__sort-item">销量</div>
                        <div class="dec-pc-search-result__sort-item">价格 <span class="dec-pc-search-result__sort-arrow">›</span></div>
                        <div v-if="showTopPriceRange" class="dec-pc-search-result__sort-item">区间 <span class="dec-pc-search-result__sort-arrow">›</span></div>
                        <div class="line"></div>
                        <div class="dec-pc-search-result__sort-item">新品</div>
                        <div class="dec-pc-search-result__sort-item">品牌 <span class="dec-pc-search-result__sort-arrow">›</span></div>
                    </div>
                    <div class="dec-pc-search-result__sort-right">
                        <div class="dec-pc-search-result__sort-item">筛选</div>
                    </div>
                </div>
                <div class="dec-pc-search-result__grid">
                    <div v-for="item in mockProducts" :key="item.id" class="dec-pc-search-result__item">
                        <div class="dec-pc-search-result__item-image">
                            <img :src="item.image" :alt="item.title" />
                        </div>
                        <div class="dec-pc-search-result__item-info">
                            <div class="dec-pc-search-result__item-title">{{ item.title }}</div>
                            <div v-if="moduleConfig.showSales" class="dec-pc-search-result__item-sales">已售 {{ item.sales }}</div>
                            <div class="dec-pc-search-result__item-bottom">
                                <div class="dec-pc-search-result__item-price">
                                    <span class="dec-pc-search-result__price-symbol">¥</span>
                                    <span class="dec-pc-search-result__price-value">{{ item.price }}</span>
                                    <span v-if="item.decimal" class="dec-pc-search-result__price-decimal">.{{ item.decimal }}</span>
                                </div>
                                <span v-if="moduleConfig.showCartButton" class="dec-pc-search-result__cart-btn" :class="buyBtnClass">
                                    <!-- 对齐前台 SearchProductCard：buy-jia / buy-cart / buy-cart-circle -->
                                    <svg
                                        v-if="buyIconName === 'buy-jia'"
                                        class="dec-pc-search-result__cart-icon"
                                        viewBox="0 0 1027 1024"
                                        aria-hidden="true"
                                    >
                                        <path
                                            fill="currentColor"
                                            d="M515.2 0C232.448 0 3.2 229.248 3.2 512s229.248 512 512 512 512-229.248 512-512-229.248-512-512-512z m237.056 534.656c-5.824 5.76-13.824 9.344-22.656 9.344H547.2v182.4c0 17.664-14.336 32-32 32-8.832 0-16.832-3.584-22.656-9.344-5.76-5.824-9.344-13.824-9.344-22.656V544H300.8c-17.664 0-32-14.336-32-32 0-8.832 3.584-16.832 9.344-22.656 5.824-5.76 13.824-9.344 22.656-9.344h182.4V297.6c0-17.664 14.336-32 32-32 8.832 0 16.832 3.584 22.656 9.344 5.76 5.824 9.344 13.824 9.344 22.656V480H729.6c17.664 0 32 14.336 32 32 0 8.832-3.584 16.832-9.344 22.656z"
                                        />
                                    </svg>
                                    <svg
                                        v-else-if="buyIconName === 'buy-cart'"
                                        class="dec-pc-search-result__cart-icon"
                                        viewBox="0 0 1024 1024"
                                        aria-hidden="true"
                                    >
                                        <path
                                            fill="currentColor"
                                            d="M352.53359 829.922816c-35.238572 0-63.782675 28.54615-63.782675 63.786769 0 35.237549 28.545126 63.781652 63.782675 63.781652 35.240619 0 63.781652-28.545126 63.781652-63.781652C416.315242 858.467943 387.775232 829.922816 352.53359 829.922816zM799.016411 829.922816c-35.240619 0-63.787792 28.54615-63.787792 63.786769 0 35.237549 28.54615 63.781652 63.787792 63.781652 35.235502 0 63.781652-28.545126 63.781652-63.781652C862.798063 858.467943 834.251913 829.922816 799.016411 829.922816zM862.28641 798.034549 344.276533 798.034549c-45.957705 0-86.36295-36.354999-91.976808-82.759889l-54.087872-380.784516L167.436942 156.535149c-1.913583-15.526625-16.074094-28.223808-29.913287-28.223808l-40.627303 0c-17.607006 0-31.894408-14.287401-31.894408-31.887244 0-17.605983 14.287401-31.894408 31.894408-31.894408l40.627303 0c46.530756 0 87.383186 36.356023 93.028766 82.760912l30.710442 177.281659 54.313 382.409526c1.784646 14.799054 15.176654 27.270087 28.700669 27.270087l518.010901 0c17.635659 0 31.893384 14.253632 31.893384 31.888268C894.179794 783.776823 879.923092 798.034549 862.28641 798.034549zM384.140449 670.466128c-16.549931 0-30.550807-12.754488-31.766494-29.562293-1.304715-17.572214 11.895935-32.848129 29.435403-34.122145l432.643628-31.895431c15.878642-0.092098 29.27372-12.564154 31.026644-26.754341l50.228985-287.404756c1.27504-10.715039-1.687432-22.452361-8.096399-29.722952-4.116761-4.656043-9.28548-7.016811-15.309684-7.016811L320.133669 223.987401c-17.605983 0-31.893384-14.287401-31.893384-31.894408 0-17.599843 14.287401-31.887244 31.893384-31.887244l552.167838 0c24.364921 0 46.749744 10.107195 63.046918 28.511357 18.627243 21.017686 27.172873 50.6424 23.440875 81.352843l-50.258661 287.439548c-5.421476 44.712341-45.797046 81.068364-91.753727 81.068364l-430.278767 31.795147C385.702014 670.436452 384.903835 670.466128 384.140449 670.466128z"
                                        />
                                    </svg>
                                    <svg
                                        v-else
                                        class="dec-pc-search-result__cart-icon"
                                        viewBox="0 0 1024 1024"
                                        aria-hidden="true"
                                    >
                                        <path
                                            fill="currentColor"
                                            d="M756 657.1l-373.1-1L326 309.9c-4.1-32.1-31.6-56.3-63.9-56.3h-54.7c-17.9 0-32.3 14.5-32.3 32.3 0 17.9 14.5 32.3 32.3 32.3l54.6 1 57 346.3c4.1 32.1 31.6 56.3 63.9 56.3H756c17.9 0 32.3-14.5 32.3-32.3 0.1-17.9-14.4-32.4-32.3-32.4zM714.7 742.9c-22.8 0-41.2 18.5-41.2 41.2 0 22.8 18.5 41.2 41.2 41.2s41.2-18.5 41.2-41.2c0.1-22.7-18.4-41.2-41.2-41.2zM415.7 742.9c-22.8 0-41.2 18.5-41.2 41.2 0 22.8 18.5 41.2 41.2 41.2 22.8 0 41.2-18.5 41.2-41.2 0-22.7-18.5-41.2-41.2-41.2z"
                                        />
                                        <path
                                            fill="currentColor"
                                            d="M834 321.3c-12.3-14.6-30.3-23-49.4-23H397.5c-17.9 0-32.3 14.5-32.3 32.3 0 17.9 14.5 32.3 32.3 32.3l387.1-1-36.8 183.5H438.7c-17.9 0-32.3 14.5-32.3 32.3 0 17.9 14.5 32.3 32.3 32.3h309.1c31.3 0 58-22.4 63.3-52.5l37-183.7c3.3-18.7-1.8-37.9-14.1-52.5z"
                                        />
                                        <path
                                            fill="currentColor"
                                            d="M512.1 0C229.8 0 0.2 229.6 0.2 511.9c0 282.3 229.6 511.9 511.9 511.9 282.3 0 511.9-229.6 511.9-511.9C1024 229.6 794.4 0 512.1 0z m0 959.1c-246.6 0-447.2-200.6-447.2-447.2S265.5 64.6 512.1 64.6s447.3 200.6 447.3 447.3c0 246.6-200.7 447.2-447.3 447.2z"
                                        />
                                    </svg>
                                </span>
                            </div>
                            <div v-if="moduleConfig.showShopTag" class="dec-pc-search-result__item-shop">
                                <span class="dec-pc-search-result__shop-tag">{{ item.shop.shopTitle }}</span>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="dec-pc-search-result__pagination">
                    <div
                        class="dec-pc-search-result__pagination-item dec-pc-search-result__pagination-item--arrow dec-pc-search-result__pagination-item--disabled"
                    >
                        ‹
                    </div>
                    <div
                        class="dec-pc-search-result__pagination-item dec-pc-search-result__pagination-item--active dec-pc-search-result__pagination-item--current"
                    >
                        1
                    </div>
                    <div class="dec-pc-search-result__pagination-item">2</div>
                    <div class="dec-pc-search-result__pagination-item">3</div>
                    <div class="dec-pc-search-result__pagination-item">4</div>
                    <div class="dec-pc-search-result__pagination-item">5</div>
                    <div class="dec-pc-search-result__pagination-ellipsis">...</div>
                    <div class="dec-pc-search-result__pagination-item">19</div>
                    <div class="dec-pc-search-result__pagination-item dec-pc-search-result__pagination-item--arrow">›</div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed, ref, watch, onMounted, nextTick, type Ref } from "vue";
import type { PcSearchResultModule } from "./types";
import { pcSearchResultDefaults } from "./types";
import brand1 from "@/assets/decorate/search/brand1.png";
import brand2 from "@/assets/decorate/search/brand2.png";
import brand3 from "@/assets/decorate/search/brand3.png";
import brand4 from "@/assets/decorate/search/brand4.png";
import brand5 from "@/assets/decorate/search/brand5.png";
import brand6 from "@/assets/decorate/search/brand6.png";
import brand7 from "@/assets/decorate/search/brand7.png";

const module = defineModel<PcSearchResultModule>("module", { default: () => ({}) }) as Ref<PcSearchResultModule>;

// 合并默认值后的模块配置（预览取值只读）
const moduleConfig = computed<PcSearchResultModule>(() => ({
    ...pcSearchResultDefaults,
    ...module.value
}));

const cssVars = computed(() => {
    const data = moduleConfig.value;
    return {
        "--dec-pc-search-result-bg": data.backgroundColor || "#fff",
        "--dec-pc-search-result-theme": "#ff4d4f",
        "--dec-pc-search-result-margin-top": `${data.componentMarginTop ?? 8}px`,
        "--dec-pc-search-result-margin-bottom": `${data.componentMarginBottom ?? 8}px`
    };
});

const showTopPriceRange = computed(() => moduleConfig.value.showPriceRange !== false);

const isFilterSide = computed(() => Number(moduleConfig.value.filterStyle ?? 2) === 1);

// ========== 顶部分类筛选条：溢出滚动 + 动态左右箭头（对齐首页商品分组） ==========
const categoryTrackRef = ref<HTMLElement | null>(null);
const showCategoryLeftArrow = ref(false);
const showCategoryRightArrow = ref(false);

function updateCategoryArrows() {
    const el = categoryTrackRef.value;
    if (!el) {
        showCategoryLeftArrow.value = false;
        showCategoryRightArrow.value = false;
        return;
    }
    showCategoryLeftArrow.value = el.scrollLeft > 0;
    showCategoryRightArrow.value = el.scrollLeft + el.clientWidth < el.scrollWidth - 1;
}

function scrollCategory(direction: "left" | "right") {
    const el = categoryTrackRef.value;
    if (!el) return;
    const step = el.clientWidth * 0.6;
    el.scrollBy({ left: direction === "left" ? -step : step, behavior: "smooth" });
}

watch(isFilterSide, () => {
    nextTick(updateCategoryArrows);
});

onMounted(() => {
    nextTick(updateCategoryArrows);
});

const resolvedFilterTemplate = computed(() => Number(moduleConfig.value.filterTemplate ?? 2));

// 左侧筛选布局的静态展示分组（品牌/属性），仅供预览
const sidebarFilterGroups = [
    { name: "品牌", options: ["Apple", "华为", "小米", "OPPO"] },
    { name: "屏幕尺寸", options: ["6.1 英寸", "6.7 英寸", "6.8 英寸"] },
    { name: "运行内存", options: ["8GB", "12GB", "16GB"] }
];

const contentStyleClass = computed(() => {
    const style = moduleConfig.value.goodsStyle ?? 1;
    const radiusStyle = moduleConfig.value.goodsRadiusStyle ?? 1;
    return {
        "dec-pc-search-result--filter-top": !isFilterSide.value,
        "dec-pc-search-result--filter-side": isFilterSide.value,
        "dec-pc-search-result--template-5": resolvedFilterTemplate.value === 1,
        "dec-pc-search-result--template-6": resolvedFilterTemplate.value === 2,
        "dec-pc-search-result--goods-flat": style === 1,
        "dec-pc-search-result--goods-card": style === 2,
        "dec-pc-search-result--goods-outline": style === 3,
        "dec-pc-search-result--radius-0": radiusStyle === 1,
        "dec-pc-search-result--radius-8": radiusStyle === 2,
        "dec-pc-search-result--radius-16": radiusStyle === 3
    };
});

const buyBtnClass = computed(() => {
    const style = Number(moduleConfig.value.buyStyle ?? 1);
    return {
        "dec-pc-search-result__cart-btn--style-1": style === 1,
        "dec-pc-search-result__cart-btn--style-2": style === 2,
        "dec-pc-search-result__cart-btn--style-3": style === 3
    };
});

/** 对齐前台 SearchProductCard.buyIconName */
const buyIconName = computed(() => {
    const style = Number(moduleConfig.value.buyStyle ?? 1);
    if (style === 2) return "buy-cart";
    if (style === 3) return "buy-cart-circle";
    return "buy-jia";
});

// 顶部分类品牌图（预览用静态数据）
const topFilterCategories = [
    { id: "1", name: "女装", image: brand1 },
    { id: "2", name: "男装", image: brand2 },
    { id: "3", name: "内衣", image: brand3 },
    { id: "4", name: "鞋包", image: brand4 },
    { id: "5", name: "母婴", image: brand5 },
    { id: "6", name: "美妆", image: brand6 },
    { id: "7", name: "家居", image: brand7 }
];

// Mock 商品数据
const mockProducts = [
    {
        id: "1",
        title: "醋酸羊毛呢阔腿裤女秋冬季加绒2025新款窄版直筒小个子弯刀卫裤子",
        image: "https://oss.vortmall.com/demo/upload/c4ca4238/2026/01/28/30a964814ee1455fa5fe3550e2deea6e.jpg",
        price: "89",
        decimal: "69",
        sales: 0,
        shop: { shopTitle: "自营门店" }
    },
    {
        id: "2",
        title: "羽绒服",
        image: "https://oss.vortmall.com/demo/2026/01/26/85874e8c826946fd9334a36b0855e2a8.png",
        price: "89",
        decimal: "00",
        sales: 0,
        shop: { shopTitle: "自营门店" }
    },
    {
        id: "3",
        title: "水卫士清洁剂",
        image: "https://oss.vortmall.com/demo/upload/c4ca4238/2026/01/28/29c8e16093654de2bbf2ebdc15c2010f.jpg",
        price: "18",
        decimal: "00",
        sales: 0,
        shop: { shopTitle: "屈臣氏" }
    },
    {
        id: "4",
        title: "平台卡密商品",
        image: "https://oss.vortmall.com/demo/upload/c4ca4238/2026/01/30/e5b67d5f2e59440fbb8fcb58e73204b1.jpg",
        price: "1",
        decimal: "00",
        sales: 0,
        shop: { shopTitle: "自营门店" }
    },
    {
        id: "5",
        title: "儿童羽绒服红色款",
        image: "https://oss.vortmall.com/demo/upload/c4ca4238/2026/01/29/fbe9c0f59cd441949a3e4e163bae2495.jpg",
        price: "240",
        decimal: "00",
        sales: 0,
        shop: { shopTitle: "自营门店" }
    },
    {
        id: "6",
        title: "珀莱雅紫瓶套装",
        image: "https://oss.vortmall.com/demo/upload/33877088/2026/01/29/df197f19325946bab0c914020d153e0d.jpg",
        price: "550",
        decimal: "89",
        sales: 0,
        shop: { shopTitle: "美妆全球购" }
    },
    {
        id: "7",
        title: "女秋冬加绒加厚大喇叭裤黑色西装裤子高腰修身显瘦垂感休闲拖地裤",
        image: "https://oss.vortmall.com/demo/upload/c4ca4238/2026/01/28/a9eca7ab94a34795aed33e3b5aca3983.jpg",
        price: "185",
        decimal: "39",
        sales: 0,
        shop: { shopTitle: "自营门店" }
    },
    {
        id: "8",
        title: "预约上门保洁",
        image: "https://oss.vortmall.com/demo/upload/c4ca4238/2026/01/28/d760d8982a344b94a7651d6bfeccc409.jpg",
        price: "350",
        decimal: "00",
        sales: 0,
        shop: { shopTitle: "自营门店" }
    },
    {
        id: "9",
        title: "预约到店美甲",
        image: "https://oss.vortmall.com/demo/upload/c4ca4238/2026/01/28/89ffd902fd9b4e0f9df1564030ba136c.jpg",
        price: "158",
        decimal: "00",
        sales: 0,
        shop: { shopTitle: "自营门店" }
    },
    {
        id: "10",
        title: "阿玛尼口红999",
        image: "https://oss.vortmall.com/demo/upload/c4ca4238/2026/01/27/4afcfd3267b045d9a6eff3ca1c1338e6.jpg",
        price: "131",
        decimal: "00",
        sales: 0,
        shop: { shopTitle: "屈臣氏" }
    },
    {
        id: "11",
        title: "小小购物篮",
        image: "https://oss.vortmall.com/demo/upload/c4ca4238/2026/01/28/ac972439f912458ab62672062b25b471.jpg",
        price: "10",
        decimal: "80",
        sales: 0,
        shop: { shopTitle: "屈臣氏" }
    },
    {
        id: "12",
        title: "双面羊绒大衣",
        image: "https://oss.vortmall.com/demo/2026/01/26/43f2c60165cb4112b850169040d14b7c.jpg",
        price: "1200",
        decimal: "00",
        sales: 0,
        shop: { shopTitle: "屈臣氏" }
    }
];
</script>

<style lang="less" src="./preview.less" scoped />
