<template>
    <!--
      根节点 module-ad-con：选中蓝框画在这里。
      模块外边距只作用商品列表 __content（对齐 C 端 goods-container），不包搜索头/筛选。
    -->
    <div class="module-ad-con dec-mobile-search-result" :style="cssVars">
        <div class="dec-mobile-search-result__inner">
            <!-- 搜索页不再使用独立 Page 头部；按 C 端运行环境把真实头部收敛到搜索结果模块预览 -->
            <MobilePageHeaderPreview
                :variant="headerLayoutStyle === 1 ? 'domestic' : 'overseas'"
                :title="$t('商品搜索')"
                :cart-count="headerLayoutStyle === 2 ? 21 : ''"
            />

            <!-- 搜索栏：门店模式固定国内版；商品模式按 layoutStyle -->
            <div v-if="headerLayoutStyle === 1" class="dec-mobile-search-result__header">
                <div class="dec-mobile-search-result__input-wrapper">
                    <div class="dec-mobile-search-result__input-area">
                        <span class="dec-mobile-search-result__search-text">卫衣</span>
                    </div>
                    <div class="dec-mobile-search-result__search-btn">搜索</div>
                </div>
            </div>

            <!-- 搜索栏 - 样式 2 (海外)：右侧放大镜 -->
            <div v-else class="dec-mobile-search-result__header dec-mobile-search-result__header--style2">
                <i class="iconfont-h5 icon-fanhui dec-mobile-search-result__back-icon" aria-hidden="true"></i>
                <div class="dec-mobile-search-result__input-wrapper">
                    <div class="dec-mobile-search-result__input-area">
                        <span class="dec-mobile-search-result__search-text">卫衣</span>
                    </div>
                    <i class="iconfont-h5 icon-sousuo1 dec-mobile-search-result__search-icon-right" aria-hidden="true"></i>
                </div>
            </div>

            <!-- 排序/筛选栏（门店搜索没有商品排序，与 C 端一致不展示） -->
            <div v-if="headerLayoutStyle === 1 && !isStoreResult" class="dec-mobile-search-result__tabs">
                <div class="dec-mobile-search-result__tab-item active">
                    <span>默认</span>
                </div>
                <div class="dec-mobile-search-result__tab-item">
                    <span>销量</span>
                </div>
                <div class="dec-mobile-search-result__tab-item dec-mobile-search-result__tab-item--price">
                    <span>价格</span>
                    <div class="dec-mobile-search-result__price-icons">
                        <i class="iconfont-h5 icon-up" aria-hidden="true"></i>
                        <i class="iconfont-h5 icon-arrowdown" aria-hidden="true"></i>
                    </div>
                </div>
                <div class="dec-mobile-search-result__tab-item dec-mobile-search-result__tab-item--filter">
                    <span>筛选</span>
                    <i class="iconfont-h5 icon-gengduo1 dec-mobile-search-result__filter-arrow" aria-hidden="true"></i>
                </div>
            </div>

            <div v-else-if="headerLayoutStyle === 2 && !isStoreResult" class="dec-mobile-search-result__tabs dec-mobile-search-result__tabs--style2">
                <div class="dec-mobile-search-result__tabs-scroll">
                    <div class="dec-mobile-search-result__tab-item-box">
                        <i class="iconfont-h5 icon-a-gengduo11 dec-mobile-search-result__filter-ico" aria-hidden="true"></i>
                        <span>筛选</span>
                    </div>
                    <div v-for="name in overseasFilterTabs" :key="name" class="dec-mobile-search-result__tab-item-box">
                        <span>{{ name }}</span>
                        <i class="iconfont-h5 icon-arrowdown dec-mobile-search-result__tab-arrow" aria-hidden="true"></i>
                    </div>
                </div>
                <div class="dec-mobile-search-result__tabs-more">
                    <i class="iconfont-h5 icon-arrowdown dec-mobile-search-result__tab-arrow" aria-hidden="true"></i>
                </div>
            </div>

            <!-- 门店列表 -->
            <div
                v-if="isStoreResult"
                class="dec-mobile-search-result__content dec-mobile-search-result__content--store"
                :style="{ rowGap: storeGap + 'px' }"
            >
                <div
                    v-for="store in mockStores"
                    :key="store.id"
                    class="dec-mobile-search-result__store"
                    :style="storeCardStyle"
                >
                    <div class="dec-mobile-search-result__store-header">
                        <div class="dec-mobile-search-result__store-logo" :style="{ borderRadius: storeLogoRadius + '%' }">
                            <img :src="store.logo" :alt="store.title" />
                        </div>
                        <div class="dec-mobile-search-result__store-main">
                            <div class="dec-mobile-search-result__store-title-row">
                                <span v-if="storeVisible.showRating" class="dec-mobile-search-result__store-tag">星级门店</span>
                                <span class="dec-mobile-search-result__store-title" :style="storeTitleStyle">{{ store.title }}</span>
                                <span v-if="storeVisible.showFollow" class="dec-mobile-search-result__store-follow">
                                    <i class="iconfont-h5 icon-guanzhu" aria-hidden="true"></i>
                                    关注
                                </span>
                            </div>
                            <div class="dec-mobile-search-result__store-meta">
                                <span v-if="storeVisible.showRating" class="dec-mobile-search-result__store-score">{{ store.score }}分</span>
                                <span v-if="storeVisible.showMinOrder" class="dec-mobile-search-result__store-meta-item">起送 ¥{{ store.minOrder }}</span>
                                <span v-if="storeVisible.showDeliveryFee" class="dec-mobile-search-result__store-meta-item"
                                    >配送费 ¥{{ store.deliveryFee }}</span
                                >
                                <span v-if="storeVisible.showDistance" class="dec-mobile-search-result__store-meta-item">距离 {{ store.distance }}</span>
                            </div>
                        </div>
                    </div>

                    <div class="dec-mobile-search-result__store-products" :style="{ columnGap: storeProductGap + 'px' }">
                        <div v-for="item in store.products" :key="item.id" class="dec-mobile-search-result__store-product">
                            <div class="dec-mobile-search-result__store-product-image">
                                <img :src="item.image" :alt="item.title" />
                            </div>
                            <div
                                v-if="storeVisible.showProductName"
                                class="dec-mobile-search-result__store-product-name"
                                :class="`dec-mobile-search-result__store-product-name--line${storeProductNameRow}`"
                            >
                                {{ item.title }}
                            </div>
                            <div v-if="storeVisible.showProductPrice" class="dec-mobile-search-result__store-product-price">¥{{ item.price }}</div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 商品列表 -->
            <div v-else class="dec-mobile-search-result__content" :class="[`dec-mobile-search-result__content--list${moduleConfig.listStyle}`]">
                <div v-for="item in mockProducts" :key="item.id" class="dec-mobile-search-result__item">
                    <div class="dec-mobile-search-result__item-image">
                        <img :src="item.image" :alt="item.title" />
                    </div>
                    <div class="dec-mobile-search-result__item-info">
                        <!-- 单列严格对齐 UniApp：info > detail / tips / activity / action -->
                        <template v-if="moduleConfig.listStyle === 2">
                            <div class="dec-mobile-search-result__item-title" :style="{ color: moduleConfig.productTitleColor }">{{ item.title }}</div>
                            <div v-if="moduleConfig.showSales !== false" class="dec-mobile-search-result__item-sales">已售 {{ item.quantity || 0 }}</div>
                            <div class="dec-mobile-search-result__item-action">
                                <div class="dec-mobile-search-result__item-price">
                                    <span class="dec-mobile-search-result__price-symbol">¥</span>
                                    <span class="dec-mobile-search-result__price-value">{{ item.price }}</span>
                                </div>
                                <div v-if="moduleConfig.showShopTag !== false" class="dec-mobile-search-result__item-shop">官方自营店</div>
                                <div v-if="moduleConfig.showCartButton !== false" class="dec-mobile-search-result__item-cart">
                                    <div class="dec-mobile-search-result__cart-btn">添加购物车</div>
                                </div>
                            </div>
                        </template>

                        <template v-else>
                            <div class="dec-mobile-search-result__item-title" :style="{ color: moduleConfig.productTitleColor }">{{ item.title }}</div>

                            <!-- 对齐 uniapp activityCard 票券结构；底色沿用主题色 15% 透明 -->
                            <div v-if="moduleConfig.showCoupon !== false" class="dec-mobile-search-result__item-coupon">
                                <span class="dec-mobile-search-result__coupon-type">
                                    券
                                    <i class="dec-mobile-search-result__coupon-line" />
                                </span>
                                <span class="dec-mobile-search-result__coupon-content">满 100.00 减 10.00</span>
                            </div>

                            <div class="dec-mobile-search-result__item-bottom">
                                <div class="dec-mobile-search-result__item-price">
                                    <span class="dec-mobile-search-result__price-symbol">¥</span>
                                    <span class="dec-mobile-search-result__price-value">{{ item.price }}</span>
                                </div>
                            </div>

                            <!--
                              双列购物车按钮 1-8。
                              样式 1-4 必须用 iconfont-pc：admin 全局后加载的 iconfont-pc 会覆盖同名 content，
                              若用 iconfont-h5 + icon-gouwuche1，content 落到 \e665，在 h5 字体里是「眼睛」glyph。
                            -->
                            <div v-if="buyBtnStyle > 0" class="dec-mobile-search-result__buy-btn-box">
                                <a
                                    href="javascript:;"
                                    class="dec-mobile-search-result__buy-btn"
                                    :class="[`dec-mobile-search-result__buy-btn--style${buyBtnStyle}`]"
                                >
                                    <template v-if="buyBtnStyle === 5 || buyBtnStyle === 6">
                                        <span>购买</span>
                                    </template>
                                    <template v-else-if="buyBtnStyle === 7 || buyBtnStyle === 8">
                                        <span>马上抢</span>
                                    </template>
                                    <template v-else-if="buyBtnStyle === 1">
                                        <i class="iconfont-pc icon-gouwuche"></i>
                                    </template>
                                    <template v-else-if="buyBtnStyle === 2">
                                        <i class="iconfont-pc icon-gouwuche1"></i>
                                    </template>
                                    <template v-else-if="buyBtnStyle === 3">
                                        <i class="iconfont-pc icon-jia"></i>
                                    </template>
                                    <template v-else-if="buyBtnStyle === 4">
                                        <i class="iconfont-pc icon-jia1"></i>
                                    </template>
                                </a>
                            </div>
                        </template>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
/**
 * 移动端搜索结果模块 - 预览组件
 *
 * searchResultType 1/2：商品列表 / 门店列表；layoutStyle 1/2：国内/海外头部与筛选；
 * listStyle 1/2：双列/单列商品卡；buyBtnStyle 0–8。
 */
import { computed, type Ref } from "vue";
import type { MobileSearchResultModule } from "./types";
import { mobileSearchResultDefaults } from "./types";
import storeTagBg from "./assets/tag-bg.png";
import MobilePageHeaderPreview from "@/views/decorate/decorate/editor/components/MobilePageHeaderPreview.vue";

const module = defineModel<MobileSearchResultModule>("module", { default: () => ({}) }) as Ref<MobileSearchResultModule>;

/** 星级门店标底图（与 C 端 static/mini/images/store/tag-bg.png 同图） */
const storeTagBgStyle = `url(${storeTagBg})`;

/** 兼容历史草稿/接口把 boolean、number 存成字符串的情况 */
const toBool = (v: unknown, fallback: boolean) => {
    if (v === undefined || v === null || v === "") return fallback;
    if (typeof v === "boolean") return v;
    if (typeof v === "number") return v !== 0;
    if (typeof v === "string") {
        const s = v.trim().toLowerCase();
        if (s === "true" || s === "1") return true;
        if (s === "false" || s === "0") return false;
    }
    return fallback;
};
const toNum = (v: unknown, fallback: number) => {
    if (v === undefined || v === null || v === "") return fallback;
    const n = typeof v === "number" ? v : Number(v);
    return Number.isFinite(n) ? n : fallback;
};

// 合并默认值后的模块配置（预览取值只读；布尔/数字已归一化）
const moduleConfig = computed<MobileSearchResultModule>(() => {
    const raw = module.value ?? {};
    const d = mobileSearchResultDefaults;
    return {
        ...d,
        ...raw,
        showShopTag: toBool(raw.showShopTag, d.showShopTag!),
        showSales: toBool(raw.showSales, d.showSales!),
        showCartButton: toBool(raw.showCartButton, d.showCartButton!),
        showCoupon: toBool(raw.showCoupon, d.showCoupon!),
        layoutStyle: toNum(raw.layoutStyle, d.layoutStyle!) as 1 | 2,
        listStyle: toNum(raw.listStyle, d.listStyle!) as 1 | 2,
        productTitleLine: toNum(raw.productTitleLine, d.productTitleLine!) as 1 | 2,
        buyBtnStyle: toNum(raw.buyBtnStyle, d.buyBtnStyle!),
        searchItemPaddingLeftRight: toNum(raw.searchItemPaddingLeftRight, d.searchItemPaddingLeftRight!),
        margin: toNum(raw.margin, d.margin!),
        padding: toNum(raw.padding, d.padding!),
        paddingTopBottom: toNum(raw.paddingTopBottom, toNum(raw.padding, d.paddingTopBottom!)),
        paddingLeftRight: toNum(raw.paddingLeftRight, toNum(raw.padding, d.paddingLeftRight!)),
        moduleRadius: toNum(raw.moduleRadius, d.moduleRadius!),
        productImageRadius: toNum(raw.productImageRadius, d.productImageRadius!),
        searchResultType: toNum(raw.searchResultType, d.searchResultType!) as 1 | 2,
        storeShowRating: toNum(raw.storeShowRating, d.storeShowRating!),
        storeShowMinOrder: toNum(raw.storeShowMinOrder, d.storeShowMinOrder!),
        storeShowDeliveryFee: toNum(raw.storeShowDeliveryFee, d.storeShowDeliveryFee!),
        storeShowDistance: toNum(raw.storeShowDistance, d.storeShowDistance!),
        storeShowFollow: toNum(raw.storeShowFollow, d.storeShowFollow!),
        storeShowProductName: toNum(raw.storeShowProductName, d.storeShowProductName!),
        storeProductNameRow: toNum(raw.storeProductNameRow, d.storeProductNameRow!) as 1 | 2,
        storeShowProductPrice: toNum(raw.storeShowProductPrice, d.storeShowProductPrice!),
        storeProductGap: toNum(raw.storeProductGap, d.storeProductGap!),
        storeGap: toNum(raw.storeGap, d.storeGap!),
        storePaddingTopBottom: toNum(raw.storePaddingTopBottom, d.storePaddingTopBottom!),
        storePaddingLeftRight: toNum(raw.storePaddingLeftRight, d.storePaddingLeftRight!),
        storeCardRadius: toNum(raw.storeCardRadius, d.storeCardRadius!),
        storeLogoRadius: toNum(raw.storeLogoRadius, d.storeLogoRadius!),
        storeShopTitleFontSize: toNum(raw.storeShopTitleFontSize, d.storeShopTitleFontSize!),
        storeShopTitleFontBold: toNum(raw.storeShopTitleFontBold, d.storeShopTitleFontBold!)
    };
});

const buyBtnStyle = computed(() => Number(moduleConfig.value.buyBtnStyle ?? 0));

const isStoreResult = computed(() => moduleConfig.value.searchResultType === 2);
/** 门店聚合搜索固定国内版搜索头，商品模式才读 layoutStyle */
const headerLayoutStyle = computed(() => (isStoreResult.value ? 1 : Number(moduleConfig.value.layoutStyle ?? 1)));

/** 门店配置项统一按 1 显示、其它值不显示 */
const storeVisible = computed(() => {
    const cfg = moduleConfig.value;
    return {
        showRating: cfg.storeShowRating === 1,
        showMinOrder: cfg.storeShowMinOrder === 1,
        showDeliveryFee: cfg.storeShowDeliveryFee === 1,
        showDistance: cfg.storeShowDistance === 1,
        showFollow: cfg.storeShowFollow === 1,
        showProductName: cfg.storeShowProductName === 1,
        showProductPrice: cfg.storeShowProductPrice === 1
    };
});

const storeProductGap = computed(() => Math.max(0, moduleConfig.value.storeProductGap ?? 8));
const storeGap = computed(() => Math.max(0, moduleConfig.value.storeGap ?? 10));
const storeCardRadius = computed(() => Math.max(0, moduleConfig.value.storeCardRadius ?? 8));
const storeCardStyle = computed(() => ({
    borderRadius: `${storeCardRadius.value}px`,
    overflow: "hidden",
    padding: `${Math.max(0, moduleConfig.value.storePaddingTopBottom ?? 12)}px ${Math.max(0, moduleConfig.value.storePaddingLeftRight ?? 12)}px`
}));
const storeLogoRadius = computed(() => Math.min(50, Math.max(0, moduleConfig.value.storeLogoRadius ?? 50)));
const storeProductNameRow = computed(() => (moduleConfig.value.storeProductNameRow === 2 ? 2 : 1));
const storeTitleStyle = computed(() => {
    const cfg = moduleConfig.value;
    const size = cfg.storeShopTitleFontSize ?? 15;
    return {
        color: cfg.storeShopTitleColor || "#1a1a1a",
        fontSize: `${size > 0 ? size : 15}px`,
        fontWeight: String(cfg.storeShopTitleFontBold ?? 700)
    };
});

function clampNumber(n: number, min: number, max: number) {
    return Math.min(max, Math.max(min, n));
}

/**
 * 将颜色转换为 rgba（优惠券浅底：主题色 + 透明度，与改前一致）
 */
function toRgba(color: string | undefined, alpha: number) {
    const a = clampNumber(alpha, 0, 1);
    const fallback = `rgba(255, 77, 79, ${a})`;
    if (!color) return fallback;

    const c = color.trim();
    if (!c) return fallback;

    if (/^rgba?\(/i.test(c)) {
        const match = c.match(/rgba?\(\s*([0-9.]+)\s*,\s*([0-9.]+)\s*,\s*([0-9.]+)(?:\s*,\s*([0-9.]+)\s*)?\)/i);
        if (!match) return fallback;
        const r = Number(match[1]);
        const g = Number(match[2]);
        const b = Number(match[3]);
        if ([r, g, b].some((v) => Number.isNaN(v))) return fallback;
        return `rgba(${r}, ${g}, ${b}, ${a})`;
    }

    if (c.startsWith("#")) {
        const hex = c.slice(1);
        const normalized =
            hex.length === 3
                ? hex
                      .split("")
                      .map((ch) => ch + ch)
                      .join("")
                : hex;
        if (!/^[0-9a-fA-F]{6}$/.test(normalized)) return fallback;
        const r = parseInt(normalized.slice(0, 2), 16);
        const g = parseInt(normalized.slice(2, 4), 16);
        const b = parseInt(normalized.slice(4, 6), 16);
        return `rgba(${r}, ${g}, ${b}, ${a})`;
    }

    return fallback;
}

/** CSS 变量：上下、左右内边距独立配置，并兼容历史 padding */
const cssVars = computed(() => {
    const data = moduleConfig.value;
    const themeColor = data.themeColor || "#ff4d4f";

    return {
        "--dec-search-result-bg": data.backgroundColor || "#f5f5f5",
        "--dec-search-item-bg": data.searchItemBackgroundColor || "#ffffff",
        "--dec-search-item-padding-lr": (data.searchItemPaddingLeftRight ?? 10) + "px",
        "--dec-search-result-item-bg": data.itemBackgroundColor || "#ffffff",
        "--dec-search-result-theme": themeColor,
        // 优惠券浅底：主题色 15% 透明（改前正确配色）
        "--dec-search-result-theme-50": toRgba(themeColor, 0.15),
        // 虚线分隔：更淡，贴近 uniapp 半透明浅色线（避免满主题色过抢眼）
        "--dec-search-result-coupon-line": toRgba(themeColor, 0.35),
        "--dec-search-result-price-color": data.priceColor || themeColor,
        "--dec-search-result-margin": (data.margin ?? 0) + "px",
        "--dec-search-result-padding-tb": (data.paddingTopBottom ?? data.padding ?? 10) + "px",
        "--dec-search-result-padding-lr": (data.paddingLeftRight ?? data.padding ?? 10) + "px",
        "--dec-search-result-image-radius": (data.productImageRadius ?? 4) + "px",
        "--dec-product-title-line": data.productTitleLine || 2,
        "--dec-store-tag-bg": storeTagBgStyle
    };
});

// 海外版筛选栏标签（预览用静态数据）
const overseasFilterTabs = ["排序", "品牌", "图案", "厚薄"];

// Mock 商品数据
const mockProducts = [
    {
        id: 1,
        title: "小米（MI）Xiaomi Watch S3 银色 国家补贴 澎湃OS 全天血氧监测 智能运动手表 男款女款",
        image: "https://oss.tigshop.com/img/item/demo/1680762243Myvkn91XZndQaLrMTf!!pic400x400.jpeg",
        price: "649",
        quantity: 1
    },
    {
        id: 2,
        title: "羽绒服 冬季加厚保暖 大鹅绒填充 户外防风外套",
        image: "https://oss.tigshop.com/img/item/demo/1680589546tPrOZyKK683sfEOCq7!!pic400x400.jpeg",
        price: "5499",
        quantity: 1
    },
    {
        id: 3,
        title: "女秋冬加绒加厚大喇叭裤黑色西装裤子高腰修身显瘦垂感休闲拖地裤",
        image: "https://oss.tigshop.com/img/item/demo/16807622759lrB7tSYTaDMl21ver!!pic400x400.jpeg",
        price: "3679",
        quantity: 1
    },
    {
        id: 4,
        title: "双面羊绒大衣 秋冬新款 通勤百搭 中长款毛呢外套",
        image: "https://oss.tigshop.com/img/item/demo/1680762215SVhVhwTAK3TCRQdOQC!!pic400x400.jpeg",
        price: "2799",
        quantity: 1
    }
];

// Mock 门店数据（searchResultType=2）
const mockStores = [
    {
        id: 1,
        title: "Tigshop 官方旗舰门店（望京店）",
        logo: "https://oss.tigshop.com/img/item/demo/1680762243Myvkn91XZndQaLrMTf!!pic400x400.jpeg",
        score: "4.9",
        minOrder: "20.00",
        deliveryFee: "3.00",
        distance: "860m",
        products: mockProducts
    },
    {
        id: 2,
        title: "Tigshop 品牌直营店（中关村店）",
        logo: "https://oss.tigshop.com/img/item/demo/1680589546tPrOZyKK683sfEOCq7!!pic400x400.jpeg",
        score: "4.8",
        minOrder: "30.00",
        deliveryFee: "5.00",
        distance: "2.30km",
        products: mockProducts
    }
];
</script>

<style lang="less" src="./preview.less" scoped />
