<template>
    <div class="module-ad-con pd-preview" :class="`pd-preview--${styleVariant}`" :style="rootStyle">
        <!-- 顶部导航 -->
        <div class="pd-navbar">
            <i class="iconfont-h5 icon-fanhui"></i>
            <div class="pd-navbar__tabs">
                <span class="active">{{ $t("商品") }}</span>
                <span>{{ $t("详情") }}</span>
                <span>{{ $t("评价") }}</span>
                <span>{{ $t("推荐") }}</span>
            </div>
        </div>
        <!-- 主图 -->
        <div class="pd-hero" :class="heroRadiusClass" :style="heroStyle">
            <div class="pd-hero__img"></div>
            <div class="pd-hero__indicator">1/5</div>
            <div v-if="styleVariant === 'overseas'" class="pd-hero__badge">{{ $t("跨境直邮") }}</div>
        </div>
        <!-- 价格区 -->
        <div class="pd-price-section" :class="{ 'pd-card': styleVariant === 'overseas' }">
            <div class="pd-price" :style="{ color: module.priceColor || '#ff4d4f' }">
                <template v-if="styleVariant === 'overseas'">
                    <span class="pd-price__symbol">$</span>
                    <span class="pd-price__value">28.90</span>
                    <span class="pd-price__cny">≈￥199</span>
                    <span class="pd-tag">{{ $t("包税") }}</span>
                </template>
                <template v-else>
                    <span class="pd-price__symbol">￥</span>
                    <span class="pd-price__value">199</span>
                    <span class="pd-price__origin">￥299</span>
                </template>
            </div>
            <div class="pd-title" :style="titleStyle">{{ $t("这里是商品标题这里是商品标题这里是商品标题这里是商品标题") }}</div>
        </div>
        <!-- 门店风：门店信息条 -->
        <div v-if="styleVariant === 'o2o'" class="pd-store">
            <i class="iconfont-h5 icon-dianpu"></i>
            <div class="pd-store__info">
                <div class="pd-store__name">{{ $t("示例门店（旗舰店）") }}</div>
                <div class="pd-store__addr">{{ $t("距您 1.2km · 支持到店自提 / 同城配送") }}</div>
            </div>
            <span class="pd-store__go">{{ $t("进店") }}</span>
        </div>
        <!-- 模块排序区 -->
        <div class="pd-modules">
            <div v-for="mod in visibleModules" :key="mod.key" class="pd-module-row" :class="{ 'pd-card': styleVariant === 'overseas' }">
                <span class="pd-module-row__label">{{ $t(mod.label) }}</span>
                <i class="iconfont-h5 icon-jiantou-you"></i>
            </div>
        </div>
        <!-- 底部购买栏 -->
        <div class="pd-buybar" :class="{ 'pd-buybar--square': module.buyBarStyle === 2 }">
            <div class="pd-buybar__icons">
                <div v-if="module.showHome" class="pd-buybar__icon">
                    <i class="iconfont-h5 icon-shouye"></i><span>{{ $t("首页") }}</span>
                </div>
                <div v-if="module.showService" class="pd-buybar__icon">
                    <i class="iconfont-h5 icon-kefu"></i><span>{{ $t("客服") }}</span>
                </div>
                <div v-if="module.showCart" class="pd-buybar__icon">
                    <i class="iconfont-h5 icon-gouwuche"></i><span>{{ $t("购物车") }}</span>
                </div>
            </div>
            <div class="pd-buybar__btns" :style="btnStyle">
                <div class="pd-buybar__btn pd-buybar__btn--cart">{{ $t("加入购物车") }}</div>
                <div class="pd-buybar__btn pd-buybar__btn--buy">{{ buyText }}</div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import { mergeDefaultModule } from "@/views/decorate/decorate/src/modules/index";
import { productDetailBaseDefaults } from "./defaults";
import type { ProductDetailModule } from "./types";

/** 仅用于共享预览的内部视觉变体，不属于接口持久化契约。 */
type ProductDetailPreviewVariant = "vortmall" | "o2o" | "overseas";

const { t } = useI18n();

const props = defineProps<{
    /** 模块数据 */
    module: ProductDetailModule;
    /** 风格变体：vortmall / o2o / overseas */
    styleVariant: ProductDetailPreviewVariant;
}>();

const module = props.module;
mergeDefaultModule(module, productDetailBaseDefaults);

const rootStyle = computed(() => ({
    backgroundColor: module.backgroundColor || "#ffffff"
}));

const heroStyle = computed(() => {
    const ratioMap: Record<string, string> = { "4:3": "75%", "1:1": "100%", "4:5": "125%" };
    return { paddingBottom: ratioMap[module.imageRatio || "4:3"] };
});

const heroRadiusClass = computed(() => `pd-hero--${module.imageRadiusStyle || "square"}`);

const titleStyle = computed(() => {
    const sizeMap: Record<string, string> = { large: "18px", medium: "16px", small: "14px" };
    return {
        fontSize: sizeMap[module.titleSize || "medium"],
        color: module.titleColor || "#1a1a1a",
        display: "-webkit-box",
        WebkitLineClamp: String(module.titleMaxLines || 2),
        WebkitBoxOrient: "vertical" as const,
        overflow: "hidden"
    };
});

const btnStyle = computed(() => ({
    "--pd-btn-radius": (module.buttonRadius ?? 5) + "px"
}));

const visibleModules = computed(() => module.moduleSortList || []);

const buyText = computed(() => (props.styleVariant === "o2o" ? t("到店购买") : t("立即购买")));
</script>
<style lang="scss" scoped>
.pd-preview {
    width: 375px;
    margin: 0 auto;
    font-size: 13px;
    color: #333;
    overflow: hidden;
    padding-bottom: 60px;
    position: relative;
}
.pd-navbar {
    display: flex;
    align-items: center;
    height: 44px;
    padding: 0 12px;
    gap: 12px;
    .icon-fanhui {
        font-size: 18px;
    }
    &__tabs {
        display: flex;
        gap: 16px;
        span {
            color: #999;
            &.active {
                color: #333;
                font-weight: 600;
            }
        }
    }
}
.pd-hero {
    position: relative;
    width: 100%;
    height: 0;
    background: #f2f3f5;
    &__img {
        position: absolute;
        inset: 0;
        background: linear-gradient(135deg, #e6e8eb, #f5f6f7);
    }
    &--round &__img {
        border-radius: 8px;
    }
    &--large &__img {
        border-radius: 16px;
    }
    &__indicator {
        position: absolute;
        right: 10px;
        bottom: 10px;
        padding: 2px 8px;
        font-size: 11px;
        color: #fff;
        background: rgba(0, 0, 0, 0.4);
        border-radius: 10px;
    }
    &__badge {
        position: absolute;
        left: 10px;
        top: 10px;
        padding: 2px 8px;
        font-size: 11px;
        color: #fff;
        background: #ff6a00;
        border-radius: 4px;
    }
}
.pd-card {
    margin: 8px 10px;
    border-radius: 10px;
    box-shadow: 0 1px 6px rgba(0, 0, 0, 0.05);
}
.pd-price-section {
    padding: 12px;
    background: #fff;
}
.pd-price {
    display: flex;
    align-items: baseline;
    gap: 6px;
    font-weight: 700;
    &__symbol {
        font-size: 14px;
    }
    &__value {
        font-size: 24px;
    }
    &__origin {
        font-size: 12px;
        font-weight: 400;
        color: #999;
        text-decoration: line-through;
    }
    &__cny {
        font-size: 12px;
        font-weight: 400;
        color: #999;
    }
}
.pd-tag {
    font-size: 11px;
    font-weight: 400;
    color: #ff6a00;
    border: 1px solid #ffd0a8;
    border-radius: 3px;
    padding: 0 4px;
    margin-left: 4px;
}
.pd-title {
    margin-top: 8px;
    line-height: 1.4;
    font-weight: 600;
}
.pd-store {
    display: flex;
    align-items: center;
    gap: 10px;
    margin: 8px 10px;
    padding: 12px;
    background: #fff;
    border-radius: 10px;
    .icon-dianpu {
        font-size: 22px;
        color: var(--tig-primary, #e93b3d);
    }
    &__info {
        flex: 1;
    }
    &__name {
        font-weight: 600;
    }
    &__addr {
        margin-top: 2px;
        font-size: 12px;
        color: #999;
    }
    &__go {
        font-size: 12px;
        color: var(--tig-primary, #e93b3d);
        border: 1px solid var(--tig-primary, #e93b3d);
        border-radius: 12px;
        padding: 3px 10px;
    }
}
.pd-modules {
    margin-top: 8px;
}
.pd-module-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 14px 12px;
    background: #fff;
    border-bottom: 1px solid #f5f5f5;
    &__label {
        font-size: 13px;
    }
    .icon-jiantou-you {
        color: #c0c4cc;
    }
}
.pd-buybar {
    position: absolute;
    left: 0;
    right: 0;
    bottom: 0;
    display: flex;
    align-items: center;
    height: 56px;
    padding: 0 10px;
    background: #fff;
    box-shadow: 0 -1px 6px rgba(0, 0, 0, 0.06);
    &__icons {
        display: flex;
        gap: 14px;
    }
    &__icon {
        display: flex;
        flex-direction: column;
        align-items: center;
        font-size: 10px;
        color: #666;
        i {
            font-size: 18px;
        }
    }
    &__btns {
        flex: 1;
        display: flex;
        justify-content: flex-end;
        gap: 8px;
        margin-left: 10px;
    }
    &__btn {
        min-width: 90px;
        height: 38px;
        line-height: 38px;
        text-align: center;
        color: #fff;
        font-size: 14px;
        border-radius: 19px;
        &--cart {
            background: #ffb100;
        }
        &--buy {
            background: #ff4d4f;
        }
    }
    &--square .pd-buybar__btn {
        border-radius: var(--pd-btn-radius, 5px);
    }
}
</style>
