<template>
    <!-- 商品分组模块 - 预览组件 -->
    <div class="dec-pc-product-group" :class="moduleClasses" :style="cssVars">
        <div class="dec-pc-product-group__container">
            <!-- 分组标题栏 -->
            <template v-if="showGroupTitle">
                <div class="dec-pc-product-group__tabs-wrapper">
                    <!-- 左箭头 -->
                    <div v-if="showLeftArrow" class="dec-pc-product-group__arrow dec-pc-product-group__arrow--left" @click="scrollTabs('left')">
                        <span class="dec-pc-product-group__arrow-btn">
                            <svg
                                class="dec-pc-product-group__arrow-icon dec-pc-product-group__arrow-icon--flip"
                                xmlns="http://www.w3.org/2000/svg"
                                width="16"
                                height="16"
                                fill="none"
                                viewBox="0 0 16 16"
                            >
                                <path stroke="#1A1A1A" stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M6.25 4.5 9.75 8l-3.5 3.5" />
                            </svg>
                        </span>
                    </div>
                    <div ref="tabsRef" class="dec-pc-product-group__tabs" @scroll="updateArrows">
                        <div
                            v-for="(item, index) in tabList"
                            :key="index"
                            class="dec-pc-product-group__tab"
                            :class="{ 'dec-pc-product-group__tab--active': index === activeIndex }"
                            @click="handleTabClick(index)"
                        >
                            <!-- 分隔线 -->
                            <div v-if="index > 0" class="dec-pc-product-group__tab-line"></div>
                            <div class="dec-pc-product-group__tab-content">
                                <!-- 标题图片 -->
                                <img v-if="productImage(item.titlePic)" class="dec-pc-product-group__tab-icon" :src="productImage(item.titlePic)" alt="" />
                                <!-- 标题文字（与 showGroupTitle 同口径，兼容字符串 "0"/"1"） -->
                                <div v-if="showGroupTitle" class="dec-pc-product-group__tab-title">
                                    {{ item.title }}
                                </div>
                            </div>
                        </div>
                    </div>
                    <!-- 右箭头 -->
                    <div v-if="showRightArrow" class="dec-pc-product-group__arrow dec-pc-product-group__arrow--right" @click="scrollTabs('right')">
                        <span class="dec-pc-product-group__arrow-btn">
                            <svg
                                class="dec-pc-product-group__arrow-icon"
                                xmlns="http://www.w3.org/2000/svg"
                                width="16"
                                height="16"
                                fill="none"
                                viewBox="0 0 16 16"
                            >
                                <path stroke="#1A1A1A" stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M6.25 4.5 9.75 8l-3.5 3.5" />
                            </svg>
                        </span>
                    </div>
                </div>
            </template>

            <!-- 商品列表 -->
            <div class="dec-pc-product-group__products">
                <Transition :name="slideDirection" mode="out-in">
                    <div :key="activeIndex" class="dec-pc-product-group__list">
                        <div v-for="(product, index) in currentProductList" :key="index" class="dec-pc-product-group__item" :style="itemWidthStyle(index)">
                            <div class="dec-pc-product-group__card">
                                <!-- 商品图片 -->
                                <div class="dec-pc-product-group__image">
                                    <div
                                        class="dec-pc-product-group__image-inner"
                                        :style="{
                                            backgroundImage: `url(${formatImage(pickPic(product))})`
                                        }"
                                    ></div>
                                </div>
                                <!-- 商品信息 -->
                                <div class="dec-pc-product-group__info">
                                    <!-- 商品名称 -->
                                    <div v-if="showProductName" class="dec-pc-product-group__name">
                                        {{ product.productName }}
                                    </div>
                                    <!-- 已售/状态 -->
                                    <div v-if="showSales" class="dec-pc-product-group__status-wrap">
                                        <div class="dec-pc-product-group__sales">已售 {{ product.virtualSales ?? 0 }}</div>
                                    </div>
                                    <!-- 价格区域 -->
                                    <div class="dec-pc-product-group__price-box">
                                        <div class="dec-pc-product-group__price-row">
                                            <!-- 价格 -->
                                            <span v-if="showProductPrice" class="dec-pc-product-group__price">
                                                <span class="dec-pc-product-group__price-currency">¥</span>
                                                <span class="dec-pc-product-group__price-int">{{ formatPriceParts(product.productPrice).int }}</span>
                                                <span class="dec-pc-product-group__price-dec">.{{ formatPriceParts(product.productPrice).dec }}</span>
                                            </span>
                                            <!-- 划线价 -->
                                            <span v-if="showCrossedOutPrice" class="dec-pc-product-group__origin-price">
                                                <span class="dec-pc-product-group__price-originCurrency">¥</span>
                                                {{ product.marketPrice }}
                                            </span>
                                        </div>
                                        <!-- 购买按钮 -->
                                        <span v-if="showBuyBtn" class="dec-pc-product-group__cart-btn" :class="buyBtnClass">
                                            <span class="dec-pc-product-group__cart-icon iconfont-pc" :class="buyIconClass"></span>
                                        </span>
                                    </div>
                                    <!-- 商品店铺 -->
                                    <div v-if="showShopTag" class="dec-pc-product-group__shop">
                                        {{ product.shopTitle || "官方自营" }}
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </Transition>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
/**
 * 商品分组模块 - 预览组件（移植自 Vort pc-product-group）
 * 使用 CSS 变量传递动态样式，样式文件可跨端复用
 *
 * API 适配：Vort productApi.getList（product/product/list）→ Tigshop getProductList（同端点）。
 * 后端支持 productGroupId 与 ids 参数（参数类型声明未列全，但 mobilePro/productGrouping 已在用），
 * 取数失败时兜底展示配置内的演示商品数据。
 */
import { computed, ref, watch, onMounted, nextTick } from "vue";
import type { ProductGroupModule, ProductItem, ProductGroupItem } from "./types";
import { productGroupDefaults } from ".";
import { deepMerge } from "../../../utils/object";
import { getProductList } from "@/api/product/product";
import { imageFormat, pickPic, productImage } from "@/utils/format";

defineOptions({ name: "PcProductGroupPreview" });

const props = defineProps<{
    module: Record<string, unknown>;
}>();
const DEFAULT_PRICE_WEIGHT = 400;
const DEFAULT_PRICE_INT_SIZE = 28;
const DEFAULT_PRICE_MARGIN_TOP = 10;

/**
 * 合并默认值后的模块配置
 */
const moduleConfig = computed<ProductGroupModule>(() => {
    return deepMerge(productGroupDefaults, props.module as Partial<ProductGroupModule>);
});

// 当前激活的分组索引
const activeIndex = ref(0);

// ========== 箭头滚动 ==========
const tabsRef = ref<HTMLElement | null>(null);
const showLeftArrow = ref(false);
const showRightArrow = ref(false);

function updateArrows() {
    const el = tabsRef.value;
    if (!el) {
        showLeftArrow.value = false;
        showRightArrow.value = false;
        return;
    }
    showLeftArrow.value = el.scrollLeft > 0;
    showRightArrow.value = el.scrollLeft + el.clientWidth < el.scrollWidth - 1;
}

function scrollTabs(direction: "left" | "right") {
    const el = tabsRef.value;
    if (!el) return;
    const step = el.clientWidth * 0.6;
    el.scrollBy({ left: direction === "left" ? -step : step, behavior: "smooth" });
}

/**
 * Tab 列表
 */
const tabList = computed(() => {
    return moduleConfig.value.productGroups || [];
});

watch(tabList, () => {
    nextTick(updateArrows);
});

onMounted(() => {
    nextTick(updateArrows);
});

const showGroupTitle = computed(() => {
    const gt = moduleConfig.value.groupTitle;
    return Number(gt?.showTitle ?? 1) === 1;
});

// 显示开关统一做数字归一化：保存后重新载入时这些值可能为字符串 "0"/"1"，
// 直接真值判断会失效，故统一 Number() 转换后判断（缺省按显示处理）
const showProductName = computed(() => Number(moduleConfig.value.showProductName ?? 1) !== 0);
const showProductPrice = computed(() => Number(moduleConfig.value.showProductPrice ?? 1) !== 0);
const showCrossedOutPrice = computed(() => Number(moduleConfig.value.crossedOutPrice ?? 1) !== 0);
const showSales = computed(() => Number(moduleConfig.value.showSales ?? 1) !== 0);
const showShopTag = computed(() => Number(moduleConfig.value.showShopTag ?? 1) !== 0);
const showBuyBtn = computed(() => Number(moduleConfig.value.buyBtnStyle?.showBtn ?? 1) !== 0);

/**
 * 当前分组的商品列表
 */
const currentProductList = ref<ProductItem[]>([]);

/**
 * 获取商品列表数据
 */
const fetchProductList = async () => {
    const config = moduleConfig.value;
    const index = activeIndex.value;

    const productGroups = config.productGroups || [];
    if (!productGroups.length || !productGroups[index]) {
        currentProductList.value = [];
        return;
    }

    const group = productGroups[index] as ProductGroupItem;
    const sourceType = group.sourceType || "products";

    if (sourceType === "productGroup") {
        if (!group.productGroupId) {
            currentProductList.value = [];
            return;
        }
        try {
            const cols = getColumnsCount();
            const result = await getProductList({
                productGroupId: group.productGroupId,
                page: 1,
                size: cols * 3
            });
            currentProductList.value = (result.records || []) as ProductItem[];
        } catch {
            currentProductList.value = [];
        }
        return;
    }

    // 手动选择商品
    const productIds = group.productList?.map((p) => p.productId).filter((id): id is number => !!id && id > 0);
    if (productIds && productIds.length > 0) {
        try {
            const result = await getProductList({ ids: productIds.join(","), page: 1, size: 100 });
            currentProductList.value = result.records?.length ? (result.records as ProductItem[]) : group.productList;
        } catch {
            currentProductList.value = group.productList || [];
        }
    } else {
        currentProductList.value = group.productList || [];
    }
};

// 监听配置变化，重新获取商品列表
// 使用 watch 并开启 deep 选项来追踪深层嵌套对象的变化
watch(
    [() => moduleConfig.value.productGroups, activeIndex],
    () => {
        fetchProductList();
    },
    { immediate: true, deep: true }
);

/**
 * 根据展示样式获取每行列数
 */
function getColumnsCount(): number {
    const listStyle = moduleConfig.value.listStyle ?? 4;
    const map: Record<number, number> = { 4: 4, 6: 5, 7: 6 };
    return map[listStyle] || 4;
}

// 滑动方向
const slideDirection = ref("slide-left");

/**
 * Tab 点击事件
 */
const handleTabClick = (index: number) => {
    slideDirection.value = index > activeIndex.value ? "slide-left" : "slide-right";
    activeIndex.value = index;
};

/**
 * 模块类名
 */
const moduleClasses = computed(() => {
    const classes: string[] = [];
    const data = moduleConfig.value;

    // 商品卡片样式
    classes.push(`dec-pc-product-group--card-style-${data.productCardStyle ?? 1}`);
    // 列表样式
    classes.push(`dec-pc-product-group--list-style-${data.listStyle ?? 2}`);

    return classes;
});

/**
 * CSS 变量
 */
const cssVars = computed(() => {
    const data = moduleConfig.value;
    const pc = data.productColor;
    const gt = data.groupTitle;
    const ms = data.moduleStyle;
    const cs = data.contentStyle;

    return {
        // 模块样式
        "--dec-box-padding": `${ms?.boxPadding ?? 10}px`,
        "--dec-box-padding-top": `${ms?.boxPaddingTop ?? 5}px`,
        "--dec-box-padding-bottom": `${ms?.boxPaddingBottom ?? 5}px`,
        "--dec-module-bg": ms?.backgroundColor || "transparent",

        // 内容样式
        "--dec-content-padding-top": `${cs?.paddingTop ?? 0}px`,
        "--dec-content-padding-bottom": `${cs?.paddingBottom ?? 0}px`,
        "--dec-content-padding-lr": `${cs?.paddingLeftRight ?? 0}px`,
        "--dec-content-radius": `${cs?.boxRadius ?? 0}px`,
        "--dec-content-bg": cs?.backgroundColor || "transparent",
        "--dec-content-shadow": cs?.boxShadow ? "0 2px 8px rgba(93, 113, 127, 0.08)" : "none",

        // 商品卡片样式
        "--dec-card-padding": `${data.cardPadding ?? 0}px`,
        "--dec-card-radius": `${data.cardRadius ?? 4}px`,
        "--dec-card-bg": pc?.backgroundColor || "#ffffff",

        // 商品信息样式
        "--dec-item-gap": `${pc?.infoLeftRightPadding ?? 3}px`,
        "--dec-info-padding-lr": `${pc?.infoLeftRightPadding ?? 3}px`,
        "--dec-title-color": pc?.titleColor || "#19191b",
        "--dec-title-weight": data.productNameWeight ?? 400,
        "--dec-title-size": `${data.productNameSize ?? 16}px`,
        "--dec-title-rows": data.productNameRow ?? 1,
        "--dec-price-color": pc?.priceColor || "var(--main-bg, #ff4d4f)",
        "--dec-price-weight": String(DEFAULT_PRICE_WEIGHT),
        "--dec-price-size": `${DEFAULT_PRICE_INT_SIZE}px`,
        "--dec-price-dec-size": `${DEFAULT_PRICE_INT_SIZE - 2}px`,
        "--dec-price-margin-top": `${DEFAULT_PRICE_MARGIN_TOP}px`,
        "--dec-origin-price-color": pc?.crossedOutPriceColor || "#999999",

        // 分组标题样式
        "--dec-tab-margin-lr": `${gt?.marginLeftRight ?? 0}px`,
        "--dec-tab-title-size": `${gt?.bigTitleFontSize ?? 14}px`,
        "--dec-tab-title-color": gt?.titleColor || "#444444",
        "--dec-tab-title-active-color": gt?.activeTitleColor || "#FF0F23",
        "--dec-tab-active-bg": gt?.activeTitleBgColor || "#ffebf1",
        "--dec-tab-title-min-width": `${gt?.titleMinWidth ?? 80}px`,
        "--dec-tab-title-padding-tb": `${gt?.titlePaddingTopBottom ?? 8}px`,
        "--dec-tab-title-padding-lr": `${gt?.titlePaddingLeftRight ?? 12}px`,
        "--dec-tab-title-radius": `${gt?.titleRadius ?? 4}px`,
        "--dec-tab-line-color": gt?.splitLineColor || "transparent"
    };
});

/**
 * 购买按钮样式
 */
const buyBtnClass = computed(() => {
    const style = Number(moduleConfig.value.buyBtnStyle?.btnType ?? 1);
    return {
        "dec-pc-product-group__cart-btn--style-1": style === 1,
        "dec-pc-product-group__cart-btn--style-2": style === 2,
        "dec-pc-product-group__cart-btn--style-3": style === 3
    };
});

const buyIconClass = computed(() => {
    const style = Number(moduleConfig.value.buyBtnStyle?.btnType ?? 1);
    if (style === 2) return "icon-gouwuche";
    if (style === 3) return "icon-gouwuche1";
    return "icon-jia1";
});

/**
 * 计算商品项宽度（支持一大两小布局）
 */
const itemWidthStyle = (index: number) => {
    const listStyle = moduleConfig.value.listStyle ?? 2;
    const gap = "var(--dec-item-gap, 3px)";
    if (listStyle === 5) {
        // 一大两小布局：第一个占100%，后续占50%
        return {
            width: index % 3 === 0 ? "100%" : `calc((100% - ${gap}) / 2)`
        };
    }
    if (listStyle === 6) {
        // 一行五个
        return { width: `calc((100% - (${gap} * 4)) / 5)` };
    }
    if (listStyle === 7) {
        // 一行六个
        return { width: `calc((100% - (${gap} * 5)) / 6)` };
    }
    if (listStyle >= 1 && listStyle <= 4) {
        return { width: `calc((100% - (${gap} * ${listStyle - 1})) / ${listStyle})` };
    }
    return {};
};

/**
 * 价格拆分为整数和小数部分
 */
function formatPriceParts(price?: number | string): { int: string; dec: string } {
    if (price === undefined || price === null) return { int: "0", dec: "00" };
    const num = typeof price === "string" ? parseFloat(price) : price;
    if (!Number.isFinite(num)) return { int: "0", dec: "00" };
    const text = num.toFixed(2);
    const parts = text.split(".");
    return { int: parts[0] || "0", dec: parts[1] || "00" };
}

/**
 * 图片格式化
 */
function formatImage(url?: string): string {
    return imageFormat(url);
}
</script>

<style lang="less" src="./preview.less" />
