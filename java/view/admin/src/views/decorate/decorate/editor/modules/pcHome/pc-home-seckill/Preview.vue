<template>
    <div class="dec-pc-home-seckill" :style="cssVars">
        <div v-if="moduleConfig.title?.showTitle === 1" class="dec-pc-home-seckill__top" :style="titleBarStyle">
            <div class="dec-pc-home-seckill__title-wrapper">
                <div v-if="moduleConfig.title?.titleStyle === 3" class="dec-pc-home-seckill__title-line"></div>
                <img
                    v-if="moduleConfig.title?.titleBackgroundPic?.picUrl"
                    class="dec-pc-home-seckill__title-bg-pic"
                    :src="imageFormat(moduleConfig.title.titleBackgroundPic.picUrl)"
                    alt=""
                />
                <!-- 标题兜底链与前台 PcSeckillLegacy 一致：不拿活动名兜底，直接落到「限时秒杀」 -->
                <div class="dec-pc-home-seckill__title" :style="{ color: moduleConfig.title?.titleColor || '#1a1a1a' }">
                    {{ moduleConfig.title?.titleText || moduleConfig.seckillTitle || "限时秒杀" }}
                </div>
                <div
                    v-if="moduleConfig.title?.descText"
                    class="dec-pc-home-seckill__desc"
                    :class="{ 'is-big': moduleConfig.title?.titleStyle === 2 }"
                    :style="{ color: moduleConfig.title?.descColor || '#1a1a1a' }"
                >
                    {{ moduleConfig.title.descText }}
                </div>
            </div>
            <!-- 顶部倒计时：无箭头、不跳转 -->
            <div
                v-if="titleMore.visible && titleMore.mode === MORE_TYPE_COUNTDOWN"
                class="dec-pc-home-seckill__countdown"
                :style="{ color: moduleConfig.title?.moreColor || '#1a1a1a' }"
            >
                <div class="dec-pc-home-seckill__countdown-box">
                    <span class="dec-pc-home-seckill__countdown-text">{{ countdownLabel }}:</span>
                    <template v-if="countdown.days > 0">
                        <span class="dec-pc-home-seckill__countdown-item" :class="{ days: String(countdown.days).length > 2 }">
                            {{ countdown.days }}
                        </span>
                        <span class="dec-pc-home-seckill__countdown-separator-text">天</span>
                    </template>
                    <span class="dec-pc-home-seckill__countdown-item">{{ formatTwoDigits(countdown.hours) }}</span>
                    <span class="dec-pc-home-seckill__countdown-separator">:</span>
                    <span class="dec-pc-home-seckill__countdown-item">{{ formatTwoDigits(countdown.minutes) }}</span>
                    <span class="dec-pc-home-seckill__countdown-separator">:</span>
                    <span class="dec-pc-home-seckill__countdown-item">{{ formatTwoDigits(countdown.seconds) }}</span>
                </div>
            </div>

            <!-- 更多链接：文字 + 箭头（预览态不跳转） -->
            <div v-else-if="titleMore.visible" class="dec-pc-home-seckill__more-link" :style="{ color: moduleConfig.title?.moreColor || '#1a1a1a' }">
                <span>{{ moreLinkText }}</span>
                <svg class="dec-pc-home-seckill__arrow" viewBox="0 0 16 16" width="16" height="16">
                    <path d="M5.5 2.5l5 5.5-5 5.5" stroke="currentColor" stroke-width="1.5" fill="none" />
                </svg>
            </div>
        </div>

        <div class="dec-pc-home-seckill__list-wrap">
            <div v-if="seckillList.length > 0" ref="listRef" class="dec-pc-home-seckill__list">
                <div v-for="(item, index) in seckillList" :key="item.id || index" class="dec-pc-home-seckill__item">
                    <div class="dec-pc-home-seckill__img">
                        <img :src="formatImage(pickPic(item))" :alt="item.productName" @error="handleImageError" />
                    </div>
                    <div v-if="Number(getOriginalPrice(item)) > 0" class="dec-pc-home-seckill__discount">
                        优惠{{ getDiscount(getCurrentPrice(item), getOriginalPrice(item)) }}%
                    </div>
                    <div class="dec-pc-home-seckill__name">{{ item.productName }}</div>
                    <div class="dec-pc-home-seckill__price-row">
                        <div class="dec-pc-home-seckill__price">
                            <span class="dec-pc-home-seckill__price-currency">¥</span>
                            <span class="dec-pc-home-seckill__price-int">{{ formatPriceParts(getCurrentPrice(item)).int }}</span>
                            <span class="dec-pc-home-seckill__price-dec">.{{ formatPriceParts(getCurrentPrice(item)).dec }}</span>
                        </div>
                        <div class="dec-pc-home-seckill__buy">
                            <span class="dec-pc-home-seckill__buy-btn" title="添加购物车">
                                <i class="dec-pc-home-seckill__add-icon"></i>
                            </span>
                        </div>
                    </div>
                    <div v-if="Number(getOriginalPrice(item)) > 0" class="dec-pc-home-seckill__price-origin">
                        ¥{{ formatPrice(getOriginalPrice(item)) }}
                    </div>
                </div>
            </div>

            <div v-else class="dec-pc-home-seckill__list dec-pc-home-seckill__list--skeleton">
                <div v-for="index in 6" :key="index" class="dec-pc-home-seckill__item dec-pc-home-seckill__item--skeleton">
                    <div class="dec-pc-home-seckill__img dec-pc-home-seckill__img--skeleton"></div>
                    <div class="dec-pc-home-seckill__name dec-pc-home-seckill__name--skeleton"></div>
                    <div class="dec-pc-home-seckill__price-row">
                        <div class="dec-pc-home-seckill__buy">
                            <span class="dec-pc-home-seckill__buy-btn" title="添加购物车">
                                <i class="dec-pc-home-seckill__add-icon"></i>
                            </span>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 左右切换按钮 -->
            <div class="dec-pc-home-seckill__switch-btn swiper-button-prev" @click="scrollPrev"></div>
            <div class="dec-pc-home-seckill__switch-btn swiper-button-next" @click="scrollNext"></div>
        </div>
    </div>
</template>

<script setup lang="ts">
/**
 * PC 首页秒杀模块 - 预览组件（移植自 Vort pc-home-seckill）
 *
 * 取数说明：走 promotion/seckill/listForHomePreview，该接口复用 C 端首页 home/home/getSeckill
 * 的同一服务端方法，商品图与价格一并返回，无需再查商品接口补全。
 *
 * 不能改回 promotion/seckill/list：它按 shop_id = 0 过滤（管理端 token 恒为 0，看不到店铺活动）、
 * 不筛活动状态、且排序方向与 C 端相反，会导致预览与前台选中不同活动、倒计时对不上。
 */
import { computed, onBeforeUnmount, onMounted, ref } from "vue";
import type { PcHomeSeckillModule } from "./types";
import { pcHomeSeckillDefaults } from ".";
import { deepMerge } from "../../../utils/object";
import { MORE_TYPE_COUNTDOWN, resolveCountdownLabel, resolveTitleMore } from "../../../utils/titleMore";
import { getSeckillListForHomePreview } from "@/api/promotion/seckill";
import { imageFormat, pickPic } from "@/utils/format";
import type { SeckillPreviewActivity } from "@/types/promotion/seckill";

defineOptions({ name: "PcHomeSeckillPreview" });

const props = defineProps<{
    module: Record<string, unknown>;
}>();

const moduleConfig = computed<PcHomeSeckillModule>(() => {
    return deepMerge(pcHomeSeckillDefaults, props.module as Partial<PcHomeSeckillModule>);
});

const cssVars = computed<Record<string, string>>(() => {
    const frame = moduleConfig.value.frame;
    return {
        "--dec-box-padding": `${frame?.boxPadding ?? 0}px`,
        "--dec-box-padding-top": `${frame?.boxPaddingTop ?? 0}px`,
        "--dec-box-padding-bottom": `${frame?.boxPaddingBottom ?? 0}px`,
        "--dec-box-radius": `${frame?.boxRadius ?? 8}px`,
        "--dec-bg-color": frame?.backgroundColor || "#fafafa",
        "--dec-inner-padding": `${frame?.leftAndRightPadding ?? 0}px`,
        "--dec-goods-gap": `${moduleConfig.value.goodsGap ?? 10}px`
    };
});

// 标题右侧区（更多链接 / 倒计时）
const titleMore = computed(() => resolveTitleMore(moduleConfig.value.title, "countdown"));
const countdownLabel = computed(() => resolveCountdownLabel(moduleConfig.value.title, "倒计时"));
const moreLinkText = computed(() => moduleConfig.value.title?.moreLink?.title?.trim() || "更多");

const titleBarStyle = computed(() => {
    const title = moduleConfig.value.title;
    if (!title) return {};
    const style: Record<string, string> = {};
    if (title.titleBackground2) {
        style.background = `linear-gradient(to right, ${title.titleBackground || ""}, ${title.titleBackground2})`;
    } else if (title.titleBackground) {
        style.background = title.titleBackground;
    }
    if (title.titleRadius) {
        style.borderTopLeftRadius = `${title.titleRadius}px`;
        style.borderTopRightRadius = `${title.titleRadius}px`;
    }
    style.padding = `${title.titlePaddingTop ?? 20}px calc(var(--dec-inner-padding, 0px)) ${title.titlePaddingBottom ?? 20}px`;
    return style;
});

/** 商品卡数据结构（保留 Vort 渲染链路） */
interface SeckillDecorateProduct {
    id?: number | string;
    productName?: string;
    picThumb?: string;
    picUrl?: string;
    picLarge?: string;
    productPrice?: string | number;
    marketPrice?: string | number;
    seckillPrice?: string | number;
    seckkillData?: SeckillPreviewActivity;
}

const seckillList = ref<SeckillDecorateProduct[]>([]);

const emptyImgSvg =
    "data:image/svg+xml,<svg xmlns='http://www.w3.org/2000/svg' width='180' height='180'><rect fill='%23f0f0f0' width='180' height='180'/></svg>";

const getData = async () => {
    try {
        // size 固定 15：与 C 端 HomeController#getSeckill 的默认分页窗口一致。
        // 该查询无显式 ORDER BY，分页窗口不同会改变返回顺序，进而选中不同活动。
        const result = await getSeckillListForHomePreview({ page: 1, size: 15 });
        const records = result?.records || [];
        if (!records.length) {
            seckillList.value = [];
            return;
        }

        // 服务端已筛「进行中」并过滤下架商品，此处不再做客户端筛选，否则会与前台产生分歧
        seckillList.value = records.map((item) => ({
            id: item.productId ?? item.seckillId,
            productName: item.productName || "",
            picThumb: item.picThumb || "",
            picUrl: item.picUrl || "",
            picLarge: item.picLarge || "",
            productPrice: item.productPrice,
            marketPrice: item.marketPrice,
            seckillPrice: item.seckillPrice,
            seckkillData: item.seckkillData
        }));
    } catch (error) {
        console.error("获取秒杀活动列表失败:", error);
        seckillList.value = [];
    }
};

const formatImage = (url: string) => {
    // 商品图多为相对路径，补 storageUrl 域名前缀（http 链接原样返回）
    return url ? imageFormat(url) : emptyImgSvg;
};

const handleImageError = (e: Event) => {
    const target = e.target as HTMLImageElement;
    if (!target) return;
    if (target.src === emptyImgSvg) return;
    target.onerror = null;
    target.src = emptyImgSvg;
};

const formatPrice = (price: string | number) => {
    if (typeof price === "number") {
        return price.toFixed(2);
    }
    return price || "0.00";
};

/** 前台价格为三段式展示，拆出整数与小数（口径同 pc-recommend-product） */
function formatPriceParts(price: string | number): { int: string; dec: string } {
    const parts = String(formatPrice(price)).replace("¥", "").split(".");
    return {
        int: parts[0] || "0",
        dec: parts[1] || "00"
    };
}

function getCurrentPrice(item: SeckillDecorateProduct): string | number {
    // 优先使用秒杀价；否则退回商品价
    return item.seckillPrice ?? item.productPrice ?? 0;
}

function getOriginalPrice(item: SeckillDecorateProduct): string | number {
    // 与前台 PcSeckillLegacy 同口径取 productPrice ?? marketPrice。
    // 服务端 getSeckillProductList 里 productPrice 与 marketPrice 是交叉赋值的
    // （setMarketPrice(getProductPrice()) / setProductPrice(getMarketPrice())），
    // 按字段名直觉取 marketPrice 会让划线价与折扣率与前台不一致。
    return item.productPrice ?? item.marketPrice ?? 0;
}

const getDiscount = (currentPrice: string | number, originalPrice: string | number) => {
    const current = Number(currentPrice);
    const original = Number(originalPrice);
    if (!Number.isFinite(current) || !Number.isFinite(original) || original <= 0 || current >= original) {
        return 0;
    }
    const discountPercentage = ((original - current) / original) * 100;
    return Math.floor(discountPercentage * 10) / 10;
};

const formatTwoDigits = (num: number) => {
    return String(num).padStart(2, "0");
};

function parseEndTime(endTime: string) {
    if (!endTime) return 0;
    try {
        const dateParts = endTime.split(" ");
        const datePart = dateParts[0] || "";
        const timePart = dateParts[1] || "";
        const dateArr = datePart.split("-").map(Number);
        const timeArr = timePart.split(":").map(Number);
        const year = dateArr[0] ?? 0;
        const month = dateArr[1] ?? 1;
        const day = dateArr[2] ?? 1;
        const hour = timeArr[0] ?? 0;
        const minute = timeArr[1] ?? 0;
        const second = timeArr[2] ?? 0;
        const endDate = new Date(year, month - 1, day, hour, minute, second);
        return endDate.getTime();
    } catch {
        return 0;
    }
}

const nowTick = ref(Date.now());
let timer: number | null = null;

const countdown = computed(() => {
    // 与前台 PcSeckillLegacy 同口径：取列表第一条所属活动的结束时间
    const first = seckillList.value[0];
    const endAt = parseEndTime(first?.seckkillData?.seckillEndTime || "");
    const diff = endAt - nowTick.value;
    if (!endAt || diff <= 0) {
        return { days: 0, hours: 0, minutes: 0, seconds: 0 };
    }

    const days = Math.floor(diff / (1000 * 60 * 60 * 24));
    const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
    const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
    const seconds = Math.floor((diff % (1000 * 60)) / 1000);
    return { days, hours, minutes, seconds };
});

// 横向滚动
const listRef = ref<HTMLElement | null>(null);

function getScrollStep(el: HTMLElement): number {
    const firstItem = el.querySelector<HTMLElement>(".dec-pc-home-seckill__item");
    if (!firstItem) return Math.max(220, Math.floor(el.clientWidth * 0.8));
    const style = window.getComputedStyle(el);
    const gapRaw = style.columnGap && style.columnGap !== "normal" ? style.columnGap : style.gap;
    const gap = Number.parseFloat(gapRaw || "0") || 0;
    const w = firstItem.getBoundingClientRect().width;
    return Math.max(220, Math.floor(w + gap));
}

function scrollPrev() {
    const el = listRef.value;
    if (!el) return;
    el.scrollBy({ left: -getScrollStep(el), behavior: "smooth" });
}

function scrollNext() {
    const el = listRef.value;
    if (!el) return;
    el.scrollBy({ left: getScrollStep(el), behavior: "smooth" });
}

onMounted(() => {
    getData();
    timer = window.setInterval(() => {
        nowTick.value = Date.now();
    }, 1000);
});

onBeforeUnmount(() => {
    if (timer) {
        window.clearInterval(timer);
        timer = null;
    }
});
</script>

<style lang="less" src="./preview.less" />
