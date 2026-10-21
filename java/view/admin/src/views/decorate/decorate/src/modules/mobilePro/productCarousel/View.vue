<template>
    <div class="module-ad-con module-product_carousel" :style="outerStyle">
        <div v-if="module.showModuleTitle && module.moduleTitle" class="pc-module-title" :style="titleStyle">{{ module.moduleTitle }}</div>
        <div class="pc-wrap">
            <div class="pc-empty" v-if="!displayList || displayList.length === 0">
                <span>{{ $t('请先选择商品') }}</span>
            </div>
            <Swiper v-else :key="swiperKey" class="pc-swiper" :swiperOption="swiperOption" :modelValue="displayList">
                <template v-slot:default="{ item }">
                    <div class="pc-item" :style="itemStyle">
                        <div class="pc-photo">
                            <div class="pc-img-box" :style="imgBoxStyle">
                                <img :src="productImage(item)" alt="" :style="imgStyle" />
                            </div>
                        </div>
                        <div class="pc-info" :style="infoStyle">
                            <div v-if="module.showName" class="pc-name" :style="nameStyle">{{ item.productName }}</div>
                            <div v-if="module.showDesc && item.productDesc" class="pc-desc" :style="descStyle">{{ item.productDesc }}</div>
                            <div v-if="module.showSales" class="pc-sales" :style="salesStyle">{{ $t('已售{getSales}', { getSales: getSales(item) }) }}</div>
                            <div class="pc-bottom">
                                <div class="pc-prices" v-if="module.showPrice">
                                    <span class="price-current" :style="priceStyle">
                                        <i>¥</i><b>{{ priceFormat(Number(item.productPrice), false) }}</b>
                                    </span>
                                    <span
                                        v-if="module.showMarketPrice && Number(item.marketPrice) > Number(item.productPrice)"
                                        class="price-market"
                                        :style="{ color: module.marketPriceColor }"
                                    >
                                        ¥{{ priceFormat(Number(item.marketPrice), false) }}
                                    </span>
                                </div>
                                <CommonBuyButton v-if="module.buyBtnStyle?.showBtn" :styleOptions="module.buyBtnStyle"></CommonBuyButton>
                            </div>
                        </div>
                    </div>
                </template>
            </Swiper>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref, computed, Ref, watchEffect } from "vue";
import { cloneDeep } from "lodash";
import { Swiper } from "@/components/swiper";
import { priceFormat, productImage } from "@/utils/format";
import { ModuleType, ProductCarouselType } from "@/types/decorate/decorate.d";
import { getProductList } from "@/api/product/product";
import { mergeDefaultModule, defaultProductCarousel, defaultProductCarouselDemoItems, CommonBuyButton } from "@/views/decorate/decorate/src/modules/";
import "swiper/css";

const module = defineModel<ModuleType & ProductCarouselType>("module") as Ref<ModuleType & ProductCarouselType>;
mergeDefaultModule(module.value, defaultProductCarousel);
// 兼容历史草稿：buyBtnStyle 曾为数字，mergeDefaultModule 不会覆盖非空旧值，这里强制回退为默认对象
if (!module.value.buyBtnStyle || typeof module.value.buyBtnStyle !== "object") {
    module.value.buyBtnStyle = cloneDeep(defaultProductCarousel.buyBtnStyle) as any;
}

const displayList = ref<any[]>([]);

const getSales = (item: any) => Number(item.salesVolume ?? item.virtualSales ?? 0);

const ratioPaddingMap: Record<string, string> = {
    default: "",
    "1:1": "100%",
    "3:4": "133.333%",
    "3:2": "66.667%"
};

const outerStyle = computed(() => {
    const f: any = module.value.frame || {};
    const url = module.value.moduleBgImage?.picUrl;
    const parts = [
        `padding-left:${f.boxPadding ?? 0}px;`,
        `padding-right:${f.boxPadding ?? 0}px;`,
        `padding-top:${f.boxPaddingTop ?? 0}px;`,
        `padding-bottom:${f.boxPaddingBottom ?? 0}px;`,
        f.backgroundColor ? `background-color:${f.backgroundColor};` : ""
    ];
    if (url) {
        const mode = module.value.bgImageMode || "normal";
        if (mode === "repeat") {
            parts.push(`background-image:url(${url});background-repeat:repeat;background-position:top center;`);
        } else if (mode === "fixed") {
            parts.push(
                `background-image:url(${url});background-size:cover;background-position:center;background-repeat:no-repeat;background-attachment:fixed;`
            );
        } else {
            parts.push(`background-image:url(${url});background-size:cover;background-position:center;background-repeat:no-repeat;`);
        }
    }
    return parts.join("");
});

const titleStyle = computed(
    () =>
        `color:${module.value.moduleTitleColor || "#fff"};margin-bottom:${module.value.titleContentGap ?? 20}px;text-align:${module.value.textAlign || "left"};`
);

const itemStyle = computed(() => {
    const r = module.value.goodsRadius ?? 12;
    const pad = module.value.innerPadding ?? 10;
    let base = "";
    switch (module.value.cardStyle) {
        case "noBorderWhite":
            base = "background-color:#fff;";
            break;
        case "shadow":
            base = "background-color:#fff;box-shadow:0 2px 4px rgba(0,0,0,0.06);";
            break;
        case "border":
            base = "background-color:#fff;border:1px solid rgba(0,0,0,0.08);";
            break;
        default:
            base = "background-color:transparent;";
    }
    return `${base}border-radius:${r}px;padding:${pad}px;`;
});

const imgBoxStyle = computed(() => {
    const r = module.value.goodsRadius ?? 12;
    const radiusCss = module.value.picBottomRadius ? `border-radius:${r}px;` : `border-radius:${r}px ${r}px 0 0;`;
    const ratio = ratioPaddingMap[module.value.productPicRatio || "1:1"];
    if (ratio) return `${radiusCss}padding-top:${ratio};height:0;`;
    return radiusCss;
});

const imgStyle = computed(() => {
    const ratio = ratioPaddingMap[module.value.productPicRatio || "1:1"];
    if (ratio) return "position:absolute;inset:0;width:100%;height:100%;object-fit:cover;display:block;";
    return "width:100%;height:auto;display:block;";
});

const infoStyle = computed(() => `text-align:${module.value.textAlign || "left"};`);
const nameStyle = computed(() => `color:${module.value.productTitleColor || "#666"};font-weight:${module.value.textWeight || 400};`);
const descStyle = computed(() => `color:${module.value.descColor || "#999"};`);
const salesStyle = computed(() => `color:${module.value.salesColor || "#999"};`);
const priceStyle = computed(() => `color:${module.value.priceColor || "#fd70a0"};`);

// 单卡宽度（slidesPerView:'auto' 下由 CSS 决定卡宽，配合 centeredSlides 实现两侧露出）
const cardWidthCss = computed(() => `${module.value.cardWidthPercent || 82}%`);

// Swiper 露邻轮播：slidesPerView 'auto'（兼容 loop）+ centeredSlides 居中两侧露出；无指示器
const swiperOption = computed(() => {
    const count = displayList.value.length;
    return {
        slidesPerView: "auto",
        centeredSlides: true,
        spaceBetween: module.value.itemGap ?? 3,
        loop: count > 2,
        autoplay: module.value.autoplay ? { delay: 3000, disableOnInteraction: false, pauseOnMouseEnter: true } : false,
        pagination: false
    };
});
// 关键轮播参数变化时强制重挂 Swiper，保证 admin 画布即时预览
const swiperKey = computed(() => `${module.value.cardWidthPercent}_${module.value.itemGap}_${module.value.autoplay}_${displayList.value.length}`);

// 取数：收集绑定的 productId 拉最新价格/名称/图（自动加载商品信息）；demo（productId:0）用快照；无有效商品回退 demo
const buildDisplay = async (items: any[]) => {
    const ids = items.filter((i) => Number(i.productId) > 0).map((i) => i.productId);
    const fresh: Record<number, any> = {};
    if (ids.length) {
        try {
            const result = await getProductList({ productStatus: 1, checkStatus: 1, ids: ids.join(","), size: 100, page: 1 });
            (result.records || []).forEach((r: any) => {
                fresh[r.productId] = r;
            });
        } catch (error) {
            // 拉取失败时退回各 item 快照
        }
    }
    displayList.value = items.map((it) => {
        const f = Number(it.productId) > 0 ? fresh[it.productId] : null;
        return {
            ...it,
            productName: f?.productName ?? it.productName,
            productDesc: f?.productDesc ?? it.productDesc,
            productPrice: f?.productPrice ?? it.productPrice,
            marketPrice: f?.marketPrice ?? it.marketPrice,
            salesVolume: f?.salesVolume ?? it.salesVolume,
            picUrl: f?.picUrl ?? it.picUrl,
            picThumb: f?.picThumb ?? it.picThumb
        };
    });
};

watchEffect(() => {
    const raw = module.value.productList;
    const snap = raw && raw.length ? JSON.parse(JSON.stringify(raw)) : JSON.parse(JSON.stringify(defaultProductCarouselDemoItems));
    buildDisplay(snap);
});
</script>
<style lang="less" scoped>
.module-product_carousel {
    background-size: cover;
    background-repeat: no-repeat;
    background-position: center;
    .pc-module-title {
        font-size: 15px;
        font-weight: 600;
        padding: 0 12px;
        line-height: 1.4;
    }
    .pc-wrap {
        position: relative;
        box-sizing: border-box;
        min-height: 120px;
    }
    .pc-empty {
        padding: 40px 0;
        text-align: center;
        color: #999;
        font-size: 12px;
    }
    .pc-swiper {
        width: 100%;
        :deep(.swiper-slide) {
            width: v-bind(cardWidthCss);
            box-sizing: border-box;
            height: auto;
            .pc-item {
                transition:
                    transform 0.3s ease,
                    opacity 0.3s ease;
                transform: scale3d(0.85, 0.85, 1);
                opacity: 0.8;
            }
            &.swiper-slide-active {
                .pc-item {
                    transform: scale3d(1, 1, 1);
                    opacity: 1;
                }
            }
        }
    }
    .pc-item {
        box-sizing: border-box;
        display: flex;
        flex-direction: column;
        height: 100%;
        overflow: hidden;
    }
    .pc-photo {
        position: relative;
        width: 100%;
    }
    .pc-img-box {
        position: relative;
        width: 100%;
        overflow: hidden;
        background: #f5f5f5;
        transform: translateZ(0);
        -webkit-transform: translateZ(0);
    }
    .pc-info {
        box-sizing: border-box;
        display: flex;
        flex-direction: column;
        flex: 1;
        margin-top: 8px;
    }
    .pc-name {
        font-size: 13px;
        line-height: 1.4;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
        word-break: break-all;
    }
    .pc-desc {
        font-size: 12px;
        line-height: 1.3;
        margin-top: 4px;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
    }
    .pc-sales {
        font-size: 11px;
        line-height: 1.3;
        margin-top: 4px;
    }
    .pc-bottom {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 6px;
        margin-top: 8px;
        flex-wrap: wrap;
    }
    .pc-prices {
        display: flex;
        align-items: baseline;
        gap: 4px;
        flex-wrap: wrap;
        line-height: 1.1;
        .price-current {
            white-space: nowrap;
            i {
                font-style: normal;
                font-size: 0.7em;
                margin-right: 1px;
            }
            b {
                font-size: 16px;
                font-weight: 700;
            }
        }
        .price-market {
            font-size: 11px;
            text-decoration: line-through;
            font-weight: 400;
        }
    }
}
</style>
