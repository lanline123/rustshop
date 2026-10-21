<template>
    <div class="module-ad-con dec-pd2" :style="cssVars">
        <!-- 主图区：满宽正方形 + 浮层导航 + 贴边数字指示器 -->
        <div class="dec-pd2__hero">
            <div class="dec-pd2__nav-bg"></div>
            <div class="dec-pd2__navbar">
                <span class="dec-pd2__nav-btn dec-pd2__nav-btn--plain">
                    <svg viewBox="0 0 16 16" width="17" height="17" fill="none" style="transform: rotate(180deg)">
                        <path d="M6 3l5 5-5 5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" />
                    </svg>
                </span>
                <div class="dec-pd2__nav-right">
                    <span class="dec-pd2__nav-btn">
                        <svg class="dec-pd2__nav-share" viewBox="0 0 1024 1024" fill="currentColor" aria-hidden="true">
                            <path
                                d="M508-63Q389-63 285-3Q207 42 153 112Q99 182 76 269Q53 356 64.5 443.5Q76 531 120.5 609Q165 687 235.5 741Q306 795 393 818Q502 847 610 822Q618 820 622.5 813Q627 806 625 798.5Q623 791 616 786.5Q609 782 602 784Q502 807 403 781Q324 759 259.5 710Q195 661 154 590Q113 519 102.5 438.5Q92 358 113.5 279Q135 200 184 136Q233 72 304 31Q375-10 455.5-21Q536-32 615-10.5Q694 11 758 60Q822 109 863 180Q904 251 914.5 331.5Q925 412 904 491Q902 499 906 506Q910 513 918 515Q926 517 933 513Q940 509 942 501Q965 414 953.5 326.5Q942 239 897 161Q852 83 782 29Q712-25 625-48Q567-63 508-63ZM846 705H730Q722 705 716 710.5Q710 716 710 724Q710 732 716 738Q722 744 730 744H846Q854 744 859.5 738Q865 732 865 724Q865 716 859.5 710.5Q854 705 846 705ZM846 589Q838 589 832 594.5Q826 600 826 609V724Q826 732 832 738Q838 744 846 744Q854 744 859.5 738Q865 732 865 724V609Q865 600 859.5 594.5Q854 589 846 589ZM509 366Q501 366 495 371.5Q489 377 489 385Q489 393 495 399L771 674Q776 680 784 680Q792 680 798 674.5Q804 669 804 661Q804 653 798 647L522 371Q517 366 509 366Z"
                                transform="translate(0 896) scale(1 -1)"
                            />
                        </svg>
                    </span>
                    <span class="dec-pd2__nav-btn">
                        <i class="iconfont-h5 icon-shoucang1 dec-pd2__nav-icon"></i>
                    </span>
                    <span class="dec-pd2__nav-btn">
                        <i class="iconfont-h5 icon-gengduo2 dec-pd2__nav-icon dec-pd2__nav-icon--more"></i>
                    </span>
                </div>
            </div>

            <div class="dec-pd2__image">
                <img
                    src="https://oss.vortmall.com/upload/c4ca4238/img/item/demo/1680589052jGYXpahRm1SOtsPK8k!!pic800x800.jpeg?x-oss-process=image/resize,m_pad,h_800,h_800"
                    :alt="$t('商品图')"
                />
            </div>
            <span class="dec-pd2__indicator"><b>1</b>/6</span>
        </div>

        <div class="dec-pd2__body">
            <!-- 价格 / 优惠券 / 标题（同一张卡） -->
            <div class="dec-pd2__info-card">
                <div class="dec-pd2__price">
                    <span class="dec-pd2__price-symbol">¥</span>
                    <span class="dec-pd2__price-value">489</span>
                    <span class="dec-pd2__price-decimal">.00</span>
                    <span class="dec-pd2__market-price">¥599.00</span>
                </div>

                <div class="dec-pd2__coupon-row">
                    <span class="dec-pd2__coupon-tag">
                        <span class="dec-pd2__coupon-type">
                            {{ $t("券") }}
                            <i class="dec-pd2__coupon-line"></i>
                        </span>
                        <span class="dec-pd2__coupon-content">{{ $t("满200.00减3...") }}</span>
                    </span>
                    <span class="dec-pd2__coupon-tag">
                        <span class="dec-pd2__coupon-type">
                            {{ $t("券") }}
                            <i class="dec-pd2__coupon-line"></i>
                        </span>
                        <span class="dec-pd2__coupon-content">{{ $t("满100.00减2...") }}</span>
                    </span>
                    <span class="dec-pd2__coupon-more">
                        {{ $t("领券") }}
                        <svg viewBox="0 0 16 16" width="8" height="8" fill="none">
                            <path d="M6 3l5 5-5 5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" />
                        </svg>
                    </span>
                </div>

                <div class="dec-pd2__title">
                    <span class="dec-pd2__title-text">{{ $t("[商场同款]蹀愫女鞋春秋透气玛丽珍晚晚风方头平底女单鞋") }}</span>
                </div>
            </div>


            <!-- 规格 / 销量 / 参数 -->
            <div class="dec-pd2__attr-card">
                <div class="dec-pd2__attr-row">
                    <span class="dec-pd2__attr-label">{{ $t("规格") }}</span>
                    <span class="dec-pd2__attr-value">{{ $t("1件") }}</span>
                    <span class="dec-pd2__attr-arrow">
                        <img class="dec-pd2__more" src="https://overseas.vortmall.cn/api/static/mini/images/common/more.png" alt="" />
                    </span>
                </div>
                <div class="dec-pd2__attr-row">
                    <span class="dec-pd2__attr-label">{{ $t("销量") }}</span>
                    <span class="dec-pd2__attr-value">{{ $t("1021件") }}</span>
                </div>
                <div class="dec-pd2__attr-row">
                    <span class="dec-pd2__attr-label">{{ $t("参数") }}</span>
                    <span class="dec-pd2__attr-value dec-pd2__attr-value--ellipsis">{{ $t("皮革工艺/鞋头款式/闭合方式/风格/流行...") }}</span>
                    <span class="dec-pd2__attr-arrow">
                        <img class="dec-pd2__more" src="https://overseas.vortmall.cn/api/static/mini/images/common/more.png" alt="" />
                    </span>
                </div>
            </div>

            <ShopInfoPreview v-if="isMerchant() && module.showShop !== false" variant="o2o" />

            <!-- 评价 -->
            <div class="dec-pd2__review-card">
                <div class="dec-pd2__review-head">
                    <span class="dec-pd2__review-title">{{ $t("评价") }} <em>(0)</em></span>
                    <span class="dec-pd2__review-rate">{{ $t("好评") }}<em>0%</em></span>
                </div>
                <div class="dec-pd2__review-empty">{{ $t("暂无评价~") }}</div>
            </div>

            <!-- 商品详情 -->
            <div class="dec-pd2__detail-card">
                <div class="dec-pd2__detail-tabs">
                    <span class="dec-pd2__detail-tab dec-pd2__detail-tab--active">{{ $t("商品介绍") }}</span>
                    <span class="dec-pd2__detail-tab">{{ $t("售后服务") }}</span>
                </div>
                <div class="dec-pd2__detail-imgs">
                    <img v-for="item in imageList" :key="item" :src="item" alt="" />
                </div>
            </div>
        </div>

        <!-- 底部购买栏 -->
        <div class="dec-pd2__buy-bar" :class="buyBarClass">
            <div v-if="showAnyTool" class="dec-pd2__buy-tools">
                <div v-if="module.showHome !== false" class="dec-pd2__buy-tool">
                    <img class="dec-pd2__buy-tool-icon" :src="productActionIcons.home" alt="" />
                    <span>{{ $t("首页") }}</span>
                </div>
                <div v-if="module.showService !== false" class="dec-pd2__buy-tool">
                    <img class="dec-pd2__buy-tool-icon" :src="productActionIcons.service" alt="" />
                    <span>{{ $t("客服") }}</span>
                </div>
                <div v-if="module.showCart !== false" class="dec-pd2__buy-tool">
                    <img class="dec-pd2__buy-tool-icon" :src="productActionIcons.cart" alt="" />
                    <span>{{ $t("购物车") }}</span>
                </div>
            </div>
            <div class="dec-pd2__buy-actions">
                <div class="dec-pd2__buy-btn dec-pd2__buy-btn--cart">{{ $t("加入购物车") }}</div>
                <div class="dec-pd2__buy-btn dec-pd2__buy-btn--buy">{{ $t("立即购买") }}</div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
/**
 * 商品详情 A2（门店风）- 预览组件
 * 结构与样式基本 100% 还原 Tigshop-o2o-Uniapp 商品详情默认页（门店风卡片式布局）。
 * 尺寸按移动端 rpx÷2 折算为预览 px。
 */
import { computed } from "vue";
import { mergeDefaultModule } from "@/views/decorate/decorate/src/modules/index";
import { productDetailBaseDefaults } from "../_shared/defaults";
import type { ProductDetailModule } from "../_shared/types";
import ShopInfoPreview from "../_shared/ShopInfoPreview.vue";
import { isMerchant } from "@/utils/version";

defineOptions({ name: "ProductDetailA2Preview" });

const props = defineProps<{ module: ProductDetailModule }>();

const module = props.module;
mergeDefaultModule(module, { ...productDetailBaseDefaults, imageMargin: 0 });

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
    large: "18px",
    medium: "15.5px",
    small: "14px"
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

const imageList = computed(() => [
    "https://oss.vortmall.com/upload/c4ca4238/img/demo/desc/202304/1680589050BrWSyP9OxygOCIhonN!!pic.png",
    "https://oss.vortmall.com/upload/c4ca4238/img/demo/desc/202304/16805890506ZadzrY6JyGORlDwBW!!pic.png"
]);

const cssVars = computed(() => {
    const data = module;
    const ratio = imageRatioMap[data.imageRatio || "1:1"] || "1 / 1";
    const fit = data.imageMode === 2 ? "cover" : "contain";
    const pageBackground = data.backgroundColor && data.backgroundColor !== "transparent" ? data.backgroundColor : "#f5f5f5";
    return {
        "--dec-pd2-page-bg": pageBackground,
        "--dec-pd2-content-bg": data.contentBackgroundColor || "#ffffff",
        "--dec-pd2-price-color": data.priceColor || "#fa3534",
        "--dec-pd2-market-price-color": data.marketPriceColor || "#999",
        "--dec-pd2-image-ratio": ratio,
        "--dec-pd2-image-margin": `${Math.max(0, Number(data.imageMargin ?? 0))}px`,
        "--dec-pd2-image-fit": fit,
        "--dec-pd2-image-radius": imageRadiusMap[data.imageRadiusStyle || "square"] || "0px",
        "--dec-pd2-title-size": titleSizeMap[data.titleSize || "medium"] || "15.5px",
        "--dec-pd2-title-color": data.titleColor || "#2a2a2a",
        "--dec-pd2-title-lines": String(data.titleMaxLines || 2),
        "--dec-pd2-button-radius": `${data.buttonRadius ?? 8}px`
    };
});

const showAnyTool = computed(() => {
    const data = module;
    return data.showHome !== false || data.showService !== false || data.showCart !== false;
});

const buyBarClass = computed(() => (module.buyBarStyle === 2 ? "dec-pd2__buy-bar--square" : "dec-pd2__buy-bar--round"));
</script>

<style lang="scss" scoped>
@import "./preview.scss";

.dec-pd2__store-logo-image {
    object-fit: contain;
}
</style>
