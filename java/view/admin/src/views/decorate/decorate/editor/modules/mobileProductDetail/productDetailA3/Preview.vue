<template>
    <div class="module-ad-con dec-pd3" :style="cssVars">

        <!-- 商品详情导航：返回 + 标题 + 收藏 + 更多 -->
        <div class="dec-pd3__nav">
            <i class="iconfont-h5 icon-fanhui dec-pd3__nav-back"></i>
            <div class="dec-pd3__nav-title">{{ $t("商品详情") }}</div>
            <i class="iconfont-h5 icon-shoucang2 dec-pd3__nav-collect"></i>
            <i class="iconfont-h5 icon-gengduo2 dec-pd3__nav-more"></i>
        </div>

        <!-- 商品标题（跨境风：标题在主图上方） -->
        <div class="dec-pd3__name-box">
            <div class="dec-pd3__name">
                <span class="dec-pd3__name-text">{{ $t("[商场同款]蹀愫女鞋春秋透气玛丽珍晚晚风方头平底女单鞋 舒适百搭") }}</span>
            </div>
        </div>
        <div class="dec-pd3__soldout">{{ $t("已售 19 件") }}</div>

        <!-- 主图区 -->
        <div class="dec-pd3__hero">
            <div class="dec-pd3__image">
                <img
                    src="https://oss.vortmall.com/upload/c4ca4238/img/item/demo/1680589052jGYXpahRm1SOtsPK8k!!pic800x800.jpeg?x-oss-process=image/resize,m_pad,h_800,h_800"
                    :alt="$t('商品图')"
                />
            </div>
            <div class="dec-pd3__dots">
                <i class="dec-pd3__dot dec-pd3__dot--active"></i>
                <i class="dec-pd3__dot"></i>
                <i class="dec-pd3__dot"></i>
                <i class="dec-pd3__dot"></i>
                <i class="dec-pd3__dot"></i>
            </div>
        </div>

        <!-- 价格区（跨境风双行价格） -->
        <div class="dec-pd3__price-area">
            <div class="dec-pd3__price">
                <span class="dec-pd3__price-symbol">$</span>
                <span class="dec-pd3__price-value">489</span>
                <span class="dec-pd3__price-decimal">.00</span>
                <span class="dec-pd3__market-price">$599.00</span>
            </div>
        </div>

        <!-- 优惠券 -->
        <div class="dec-pd3__activity">
            <div class="dec-pd3__activity-inner">
                <span class="dec-pd3__activity-text">{{ $t("满100减10元") }}</span>
                <span class="dec-pd3__activity-more">{{ $t("更多优惠券") }}</span>
                <i class="iconfont-h5 icon-a-gengduo20 dec-pd3__activity-arrow"></i>
            </div>
        </div>


        <ShopInfoPreview v-if="isMerchant() && module.showShop !== false" variant="overseas" />

        <!-- 规格参数（可折叠标题） -->
        <div class="dec-pd3__section">
            <span class="dec-pd3__section-title">{{ $t("规格参数") }}</span>
            <i class="iconfont-h5 icon-a-gengduo20 dec-pd3__section-arrow"></i>
        </div>

        <!-- 评价 -->
        <div class="dec-pd3__review">
            <div class="dec-pd3__review-head">
                <div class="dec-pd3__review-left">
                    <span class="dec-pd3__review-title">{{ $t("评价") }}</span>
                    <span class="dec-pd3__review-real">
                        <span class="dec-pd3__review-safe">✓</span>
                        {{ $t("真实用户评价") }}
                    </span>
                </div>
                <span class="dec-pd3__review-all">
                    {{ $t("全部") }}
                    <i class="iconfont-h5 icon-a-gengduo20"></i>
                </span>
            </div>
            <div class="dec-pd3__review-score">
                <span class="dec-pd3__stars">
                    <span v-for="n in 5" :key="n" class="dec-pd3__star">☆</span>
                </span>
                <span class="dec-pd3__score-num">0</span>
                <span class="dec-pd3__score-total">{{ $t("(0 条评价)") }}</span>
            </div>
        </div>

        <!-- 售后 -->
        <div class="dec-pd3__section">
            <span class="dec-pd3__section-title">{{ $t("售后保障") }}</span>
            <span class="dec-pd3__section-value">{{ $t("7天无理由退货 · 运费险") }}</span>
            <i class="iconfont-h5 icon-a-gengduo20 dec-pd3__section-arrow"></i>
        </div>

        <!-- 商品详情（商品介绍 + 详情图） -->
        <div class="dec-pd3__detail">
            <div class="dec-pd3__detail-title">{{ $t("商品详情") }}</div>
            <div class="dec-pd3__detail-imgs">
                <img v-for="item in imageList" :key="item" :src="item" alt="" />
            </div>
        </div>

        <!-- 底部购买栏：跨境风为两枚整宽按钮 -->
        <div class="dec-pd3__footer" :class="buyBarClass">
            <div class="dec-pd3__footer-btn dec-pd3__footer-btn--cart">{{ $t("加入购物车") }}</div>
            <div class="dec-pd3__footer-btn dec-pd3__footer-btn--buy">{{ $t("立即购买") }}</div>
        </div>
    </div>
</template>

<script setup lang="ts">
/**
 * 商品详情 A3（跨境风）- 预览组件
 * 结构与样式 100% 还原 tigshop-overseas-uniapp 商品详情默认页：
 * 商城头部 + 「‹ 商品详情」导航、标题在主图上方、双行价格、优惠券、规格参数折叠、评价、底部两枚整宽按钮。
 * 图标统一使用后台已支持的 iconfont-h5 字体；尺寸按移动端 rpx÷2 折算为预览 px。
 */
import { computed } from "vue";
import { mergeDefaultModule } from "@/views/decorate/decorate/src/modules/index";
import { productDetailBaseDefaults } from "../_shared/defaults";
import type { ProductDetailModule } from "../_shared/types";
import ShopInfoPreview from "../_shared/ShopInfoPreview.vue";
import { isMerchant } from "@/utils/version";

defineOptions({ name: "ProductDetailA3Preview" });

const props = defineProps<{ module: ProductDetailModule }>();

const module = props.module;
mergeDefaultModule(module, productDetailBaseDefaults);

const titleSizeMap: Record<string, string> = {
    large: "16px",
    medium: "15px",
    small: "14px"
};

const imageRatioMap: Record<string, string> = {
    "4:3": "4 / 3",
    "4:5": "4 / 5",
    "1:1": "1 / 1"
};

const imageRadiusMap: Record<string, string> = {
    square: "0px",
    round: "10px",
    large: "18px"
};

const imageList = computed(() => [
    "https://oss.vortmall.com/upload/c4ca4238/img/demo/desc/202304/1680589050BrWSyP9OxygOCIhonN!!pic.png",
    "https://oss.vortmall.com/upload/c4ca4238/img/demo/desc/202304/16805890506ZadzrY6JyGORlDwBW!!pic.png"
]);

const cssVars = computed(() => {
    const data = module;
    const ratio = imageRatioMap[data.imageRatio || "1:1"] || "1 / 1";
    const fit = data.imageMode === 2 ? "cover" : "contain";
    return {
        "--dec-pd3-page-bg": data.backgroundColor || "#f5f5f5",
        "--dec-pd3-content-bg": data.contentBackgroundColor || "#ffffff",
        "--dec-pd3-price-color": data.priceColor || "#1a1a1a",
        "--dec-pd3-market-price-color": data.marketPriceColor || "#999",
        "--dec-pd3-image-ratio": ratio,
        "--dec-pd3-image-margin": `${Math.max(0, Number(data.imageMargin ?? 16))}px`,
        "--dec-pd3-image-fit": fit,
        "--dec-pd3-image-radius": imageRadiusMap[data.imageRadiusStyle || "square"] || "0px",
        "--dec-pd3-title-size": titleSizeMap[data.titleSize || "small"] || "14px",
        "--dec-pd3-title-color": data.titleColor || "#1a1a1a",
        "--dec-pd3-title-lines": String(data.titleMaxLines || 2),
        "--dec-pd3-button-radius": `${data.buttonRadius ?? 6}px`
    };
});

const buyBarClass = computed(() => (module.buyBarStyle === 2 ? "dec-pd3__footer--square" : "dec-pd3__footer--round"));
</script>

<style lang="scss" scoped>
@import "./preview.scss";

.dec-pd3__store-logo-image {
    object-fit: contain;
}
</style>
