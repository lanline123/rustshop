<template>
    <div class="module-ad-con dec-product-detail" :style="cssVars">
        <div class="dec-product-detail__navbar">
            <div class="dec-product-detail__navbar-left">
                <svg viewBox="0 0 16 16" width="14" height="14" fill="none" style="color: rgb(136, 139, 148); transform: rotate(180deg)">
                    <path d="M6 3l5 5-5 5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" />
                </svg>
                <span>{{ $t("商品详情") }}</span>
            </div>
            <div class="dec-product-detail__navbar-right">
                <svg viewBox="0 0 24 24" width="16" height="16" fill="none" style="color: #272c34">
                    <path
                        d="M12 21l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.18L12 21z"
                        stroke="currentColor"
                        stroke-width="1.6"
                    />
                </svg>
                <svg viewBox="0 0 24 24" width="20" height="20" fill="#272c34">
                    <circle cx="5" cy="12" r="1.8" />
                    <circle cx="12" cy="12" r="1.8" />
                    <circle cx="19" cy="12" r="1.8" />
                </svg>
            </div>
        </div>

        <div class="dec-product-detail__hero">
            <div class="dec-product-detail__image-wrap">
                <div class="dec-product-detail__image">
                    <img
                        src="https://oss.vortmall.com/upload/c4ca4238/img/item/demo/1680589052jGYXpahRm1SOtsPK8k!!pic800x800.jpeg?x-oss-process=image/resize,m_pad,h_800,h_800"
                        :alt="$t('商品图')"
                    />
                </div>
            </div>

            <div class="dec-product-detail__dots" aria-hidden="true">
                <span v-for="index in 4" :key="index" :class="['dec-product-detail__dot', { 'dec-product-detail__dot--active': index === 1 }]"></span>
            </div>

            <div class="dec-product-detail__title-section">
                <div class="dec-product-detail__title">{{ $t("竖条纹衬衫女长袖春季翻领设计感落肩袖斑马纹衬衣女文艺时尚新款") }}</div>
                <div class="dec-product-detail__sales">{{ $t("已售31件") }}</div>
            </div>

            <div class="dec-product-detail__price-section">
                <div class="dec-product-detail__price">
                    <span class="dec-product-detail__price-symbol">$</span>
                    <span class="dec-product-detail__price-value">220</span>
                    <span class="dec-product-detail__price-decimal">.40</span>
                </div>
                <div class="dec-product-detail__market-price">$288.00</div>
            </div>


            <div class="dec-product-detail__coupon-card">
                <span class="dec-product-detail__coupon-text">{{ $t("黄金会员满100元减20元") }}</span>
                <span class="dec-product-detail__coupon-more">
                    <span>{{ $t("更多优惠券") }}</span>
                    <svg viewBox="0 0 16 16" width="8" height="8" fill="none" style="color: rgb(136, 139, 148)">
                        <path d="M6 3l5 5-5 5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" />
                    </svg>
                </span>
            </div>
        </div>

        <div class="dec-product-detail__service-card-box">
            <div class="dec-product-detail__service-card">
                <div v-for="item in serviceItems" :key="item" class="dec-product-detail__service-row">
                    <span class="dec-product-detail__service-text">{{ item }}</span>
                    <svg viewBox="0 0 16 16" width="8" height="8" fill="none" style="color: rgb(136, 139, 148)">
                        <path d="M6 3l5 5-5 5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" />
                    </svg>
                </div>
            </div>
        </div>

        <ShopInfoPreview v-if="isMerchant() && module.showShop !== false" variant="vortmall" />

        <div class="spec-card-box" style="padding-bottom: 0">
            <div class="dec-product-detail__spec-card">
                <span class="dec-product-detail__spec-title">{{ $t("规格参数") }}</span>
                <svg viewBox="0 0 16 16" width="8" height="8" fill="none" style="color: rgb(136, 139, 148); transform: rotate(90deg)">
                    <path d="M6 3l5 5-5 5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" />
                </svg>
            </div>
        </div>

        <div class="spec-card-box" style="padding-bottom: 0">
            <div class="dec-product-detail__review-card">
                <div class="dec-product-detail__review-header">
                    <div class="dec-product-detail__review-left">
                        <span class="dec-product-detail__review-title">{{ $t("评价") }}</span>
                        <span class="dec-product-detail__review-tag">
                            <img width="8" height="8" src="https://overseas.vortmall.cn/api/static/mini/images/common/safety.png" alt="" />
                            {{ $t("真实用户评价") }}
                        </span>
                    </div>
                    <span class="dec-product-detail__review-more">
                        <span>{{ $t("全部") }}</span>
                        <svg viewBox="0 0 16 16" width="8" height="8" fill="none" style="color: rgb(136, 139, 148)">
                            <path d="M6 3l5 5-5 5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" />
                        </svg>
                    </span>
                </div>
                <div class="dec-product-detail__review-score">
                    <span class="dec-product-detail__review-stars">★★★★★</span>
                    <span class="dec-product-detail__review-grade">5</span>
                    <span class="dec-product-detail__review-count">{{ $t("(2条评价)") }}</span>
                </div>
            </div>
        </div>

        <div class="spec-card-box" style="padding-bottom: 0">
            <div class="dec-product-detail__spec-card">
                <span class="dec-product-detail__spec-title">{{ $t("售后服务") }}</span>
                <svg viewBox="0 0 16 16" width="8" height="8" fill="none" style="color: rgb(136, 139, 148); transform: rotate(90deg)">
                    <path d="M6 3l5 5-5 5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" />
                </svg>
            </div>
        </div>

        <div class="spec-card-box" style="padding-bottom: 0">
            <div class="dec-product-detail__spec-card">
                <span class="dec-product-detail__spec-title">{{ $t("商品描述") }}</span>
                <svg viewBox="0 0 16 16" width="8" height="8" fill="none" style="color: rgb(136, 139, 148); transform: rotate(-90deg)">
                    <path d="M6 3l5 5-5 5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" />
                </svg>
            </div>
        </div>

        <div class="image-list-box">
            <div class="image-list-item" v-for="item in imageList" :key="item.src">
                <img :src="item.src" alt="" />
            </div>
        </div>

        <div class="dec-product-detail__buy-bar" :class="buyBarClass">
            <div v-if="showAnyTool" class="dec-product-detail__buy-bar-tools">
                <div v-if="module.showHome !== false" class="dec-product-detail__buy-bar-tool">
                    <span class="dec-product-detail__buy-bar-tool-icon-wrap">
                        <img class="dec-product-detail__buy-bar-tool-icon" :src="productActionIcons.home" alt="" />
                    </span>
                    <span>{{ $t("首页") }}</span>
                </div>

                <div v-if="module.showService !== false" class="dec-product-detail__buy-bar-tool">
                    <span class="dec-product-detail__buy-bar-tool-icon-wrap">
                        <img class="dec-product-detail__buy-bar-tool-icon" :src="productActionIcons.service" alt="" />
                    </span>
                    <span>{{ $t("客服") }}</span>
                </div>

                <div v-if="module.showCart !== false" class="dec-product-detail__buy-bar-tool dec-product-detail__buy-bar-tool--cart">
                    <span class="dec-product-detail__buy-bar-tool-icon-wrap">
                        <img class="dec-product-detail__buy-bar-tool-icon" :src="productActionIcons.cart" alt="" />
                        <span class="dec-product-detail__buy-bar-tool-badge">9</span>
                    </span>
                    <span>{{ $t("购物车") }}</span>
                </div>
            </div>

            <div :class="['dec-product-detail__buy-bar-actions', { 'dec-product-detail__buy-bar-actions--full': !showAnyTool }]">
                <div class="dec-product-detail__buy-bar-group">
                    <div class="dec-product-detail__buy-bar-btn dec-product-detail__buy-bar-btn--cart">{{ $t("加入购物车") }}</div>
                    <div class="dec-product-detail__buy-bar-btn dec-product-detail__buy-bar-btn--buy">{{ $t("立即购买") }}</div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
/**
 * 商品详情 A1（标准风）- 预览组件
 * 结构与样式对齐 Vort-Admin product-detail 模块。
 */
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import { mergeDefaultModule } from "@/views/decorate/decorate/src/modules/index";
import { createProductDetailDefaults } from "../_shared/defaults";
import type { ProductDetailModule } from "../_shared/types";
import ShopInfoPreview from "../_shared/ShopInfoPreview.vue";
import { isMerchant } from "@/utils/version";

defineOptions({ name: "ProductDetailA1Preview" });

const { t } = useI18n();
const props = defineProps<{ module: ProductDetailModule }>();

const module = props.module;
mergeDefaultModule(module, createProductDetailDefaults("A1"));

/** 与 A2 及移动端商品详情共用同一套购买栏图标，避免预览图形出现偏差。 */
const staticResource = (path: string) => {
    const baseUrl = String(import.meta.env.VITE_BASE_URL || "").replace(/\/+$/, "");
    const staticPath = String(import.meta.env.VITE_STATIC_RESOURCE || "static/mini").replace(/^\/+|\/+$/g, "");
    return `${baseUrl}/${staticPath}/images/${path}`;
};

const productActionIcons = {
    home: staticResource("product/home.png"),
    service: staticResource("product/service.png"),
    cart: staticResource("product/cart.png")
};

const titleSizeMap: Record<string, string> = {
    large: "19px",
    medium: "17px",
    small: "15px"
};

const imageRatioMap: Record<string, string> = {
    "4:3": "4 / 3",
    "4:5": "4 / 5",
    "1:1": "1 / 1"
};

const imageRadiusMap: Record<string, string> = {
    square: "0px",
    round: "8px",
    large: "16px"
};

const imageList = computed(() => {
    return [
        { src: "https://oss.vortmall.com/upload/c4ca4238/img/demo/desc/202304/1680589050BrWSyP9OxygOCIhonN!!pic.png" },
        { src: "https://oss.vortmall.com/upload/c4ca4238/img/demo/desc/202304/16805890506ZadzrY6JyGORlDwBW!!pic.png" },
        { src: "https://oss.vortmall.com/upload/c4ca4238/img/demo/desc/202304/16805890501AO5Y0RVcw3O1rnIZD!!pic.png" },
        { src: "https://oss.vortmall.com/upload/c4ca4238/img/demo/desc/202304/1680589050DBrEFcXHc1eSkUu1H1!!pic.png" },
        { src: "https://oss.vortmall.com/upload/c4ca4238/img/demo/desc/202304/16805890502KtJ7dRDutTfc2Esp5!!pic.png" },
        { src: "https://oss.vortmall.com/upload/c4ca4238/img/demo/desc/202304/1680589050fMuHuRcZVJ7wc03mMD!!pic.png" }
    ];
});

const serviceItems = computed(() => [t("售全球"), t("商家发货&售后"), t("准时到达"), t("7天无理由退换货"), t("24小时发货")]);

const cssVars = computed(() => {
    const data = module;
    const ratio = imageRatioMap[data.imageRatio || "4:3"] || "4 / 3";
    const fit = data.imageMode === 2 ? "cover" : "contain";
    return {
        "--dec-pd-page-bg": data.backgroundColor || "transparent",
        "--dec-pd-content-bg": data.contentBackgroundColor || "#ffffff",
        "--dec-pd-accent-color": data.priceColor || "#ff4d4f",
        "--dec-pd-market-price-color": data.marketPriceColor || "#999",
        "--dec-pd-image-ratio": ratio,
        "--dec-pd-image-margin": `${Math.max(0, Number(data.imageMargin ?? 16))}px`,
        "--dec-pd-image-fit": fit,
        "--dec-pd-image-radius": imageRadiusMap[data.imageRadiusStyle || "square"] || "0px",
        "--dec-pd-title-size": titleSizeMap[data.titleSize || "medium"] || "17px",
        "--dec-pd-title-color": data.titleColor || "#666666",
        "--dec-pd-title-lines": String(data.titleMaxLines || 2),
        "--dec-pd-button-radius": `${data.buttonRadius ?? 25}px`
    };
});

const showAnyTool = computed(() => {
    const data = module;
    return data.showHome !== false || data.showService !== false || data.showCart !== false;
});

const isClassicBuyBar = computed(() => module.buyBarStyle === 2);

const buyBarClass = computed(() => {
    return isClassicBuyBar.value ? "dec-product-detail__buy-bar--classic" : "dec-product-detail__buy-bar--simple";
});
</script>

<style lang="scss" scoped>
@import "./preview.scss";

.dec-product-detail__store-logo-image {
    object-fit: contain;
}
</style>
