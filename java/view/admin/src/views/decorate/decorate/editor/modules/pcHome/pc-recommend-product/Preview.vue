<template>
    <div class="dec-pc-recommend-product" :style="cssVars">
        <div v-if="!productList || productList.length === 0" class="dec-pc-recommend-product__empty">
            <div class="dec-pc-recommend-product__empty-icon"></div>
            <div class="dec-pc-recommend-product__empty-text">点击添加商品</div>
        </div>

        <template v-else>
            <h3 v-if="moduleConfig.titleName" class="dec-pc-recommend-product__title">{{ moduleConfig.titleName }}</h3>
            <div class="dec-pc-recommend-product__list" :class="{ 'is-no-title': !moduleConfig.titleName }">
                <a v-for="item in productList" :key="item.productId" class="dec-pc-recommend-product__card product-card-container" href="javascript:;">
                    <div class="top-wrap">
                        <div class="pro_pic">
                            <img
                                class="img"
                                :class="{ 'sold-out': isSoldOut(item) }"
                                :src="formatImage(pickPic(item))"
                                :alt="item.productName"
                                @error="handleImgError"
                            />
                        </div>
                    </div>

                    <div class="center-wrap">
                        <div class="pro_name">{{ item.productName }}</div>

                        <div class="desc-wrap desc-wrap--sales">
                            <div class="virtual-sales">已售 {{ getVirtualSales(item) }}</div>
                        </div>

                        <!-- 状态占位：不管有没有状态都占一行，保证价格/按钮对齐 -->
                        <div class="desc-wrap desc-wrap--status" :class="{ 'is-empty': !isSoldOut(item) }">
                            <div class="virtual-sales">{{ isSoldOut(item) ? "已售罄" : "" }}</div>
                        </div>

                        <div class="tag-wrap"></div>

                        <div class="price-wrap">
                            <div class="box">
                                <div class="seckill-price">
                                    <span class="price-currency">¥</span>
                                    <span class="price-int">{{ formatPriceParts(getPrice(item)).int }}</span>
                                    <span class="price-dec">.{{ formatPriceParts(getPrice(item)).dec }}</span>
                                </div>
                            </div>
                            <div class="buy">
                                <div class="view_detail" :class="{ disabled: isSoldOut(item) }">
                                    <span class="add-icon"></span>
                                </div>
                            </div>
                        </div>

                        <div class="other-wrap">
                            <span class="link">{{ getShopTitle(item) }}</span>
                        </div>
                    </div>
                </a>
            </div>
        </template>
    </div>
</template>

<script setup lang="ts">
/**
 * PC 推荐商品模块 - 预览组件
 * "标题 + 换行卡片列表"视觉结构对齐 Vort；取数逻辑对齐 Vort，
 * API 参数键按 Tigshop 约定映射（ids 逗号串 / categoryId / introType / size）
 */
import { computed, ref, watchEffect } from "vue";
import type { PcRecommendProductModule, ProductItem } from "./types";
import { pcRecommendProductDefaults } from ".";
import { deepMerge } from "../../../utils/object";
import { getProductList } from "@/api/product/product";
import { pickPic } from "@/utils/format";
import emptyImgBg from "@/assets/decorate/empty-img-bg.png";

defineOptions({ name: "PcRecommendProductPreview" });

const props = defineProps<{
    module: Record<string, unknown>;
}>();

const moduleConfig = computed<PcRecommendProductModule>(() => {
    return deepMerge(pcRecommendProductDefaults, props.module as Partial<PcRecommendProductModule>);
});

/**
 * CSS 变量（传递动态值到样式）
 */
const cssVars = computed(() => {
    const data = moduleConfig.value;
    const frame = data.frame;
    return {
        "--dec-box-padding": `${frame?.boxPadding ?? 0}px`,
        "--dec-box-padding-top": `${frame?.boxPaddingTop ?? 0}px`,
        "--dec-box-padding-bottom": `${frame?.boxPaddingBottom ?? 50}px`,
        "--dec-box-radius": `${frame?.boxRadius ?? 0}px`,
        "--dec-bg-color": frame?.backgroundColor ?? "transparent",
        "--dec-item-gap": `${frame?.itemGap ?? 16}px`,
        "--dec-columns": frame?.columns ?? 5
    };
});

type PreviewProductItem = ProductItem & {
    virtualSales?: number | string;
    isSeckill?: number | string;
    productStock?: number | string;
    seckillStock?: number | string;
    price?: number | string;
    shopTitle?: string;
    shop?: { shopTitle?: string } | null;
};

const productList = ref<PreviewProductItem[]>([]);

const fetchProductList = async () => {
    const products = moduleConfig.value.products;
    if (!products) {
        productList.value = [];
        return;
    }

    const getRecordKey = (item: Partial<PreviewProductItem>): string => {
        const key = item.productId ?? (item as { id?: string | number }).id;
        return key === undefined || key === null ? "" : String(key);
    };

    const sortRecordsByIds = (records: PreviewProductItem[], ids: Array<string | number>): PreviewProductItem[] => {
        const order = ids.map((v) => String(v));
        if (order.length === 0) return records;
        const map = new Map<string, PreviewProductItem>();
        records.forEach((r) => map.set(getRecordKey(r), r));
        const sorted: PreviewProductItem[] = [];
        order.forEach((id) => {
            const hit = map.get(id);
            if (hit) sorted.push(hit);
        });
        records.forEach((r) => {
            const k = getRecordKey(r);
            if (k && !order.includes(k)) sorted.push(r);
        });
        return sorted;
    };

    const param: Record<string, unknown> = {};
    const paramsValueMap: Record<number, string[]> = {
        1: ["productIds"],
        2: ["productCategoryId", "productNumber"],
        3: ["productTag", "productNumber"]
    };
    // Tigshop 商品列表接口约定：选商品传 ids（逗号串），与 Vort 的 idList（数组）不同
    const paramsKeyMap: Record<string, string> = {
        productIds: "ids",
        productCategoryId: "categoryId",
        productNumber: "size",
        productTag: "introType"
    };

    const type = Number(products.productSelectType || 0);
    // 未配置商品：不请求接口，不渲染列表
    if (!type) {
        productList.value = [];
        return;
    }
    if (type === 1 && (!products.productIds || products.productIds.length === 0)) {
        productList.value = [];
        return;
    }
    if (type === 2 && (products.productCategoryId === undefined || products.productCategoryId === null || products.productCategoryId === "")) {
        productList.value = [];
        return;
    }
    if (type === 3 && (!products.productTag || String(products.productTag).trim() === "")) {
        productList.value = [];
        return;
    }

    const keys = paramsValueMap[type] ?? [];
    if (type === 1 && products.productIds?.length) {
        const firstKey = keys[0];
        const apiKey = firstKey ? paramsKeyMap[firstKey] : undefined;
        if (!apiKey) {
            productList.value = [];
            return;
        }
        const idArr = Array.isArray(products.productIds) ? products.productIds : String(products.productIds).split(",").filter(Boolean);
        param[apiKey] = idArr.join(",");
        param.size = idArr.length || 20;
    } else if (type !== 1) {
        param.page = 1;
        keys.forEach((key) => {
            const value = products[key as keyof typeof products];
            if (value !== undefined && value !== null && value !== "") {
                const apiKey = paramsKeyMap[key];
                if (apiKey) {
                    param[apiKey] = value;
                }
            }
        });
        // 兜底 size，避免接口默认返回过多
        if (param.size === undefined) {
            param.size = products.productNumber ?? 20;
        }
    }

    try {
        const result = await getProductList({ productStatus: 1, checkStatus: 1, ...param });
        const records = (result.records || []) as unknown as PreviewProductItem[];
        if (Number(products.productSelectType) === 1 && Array.isArray(products.productIds)) {
            productList.value = sortRecordsByIds(records, products.productIds);
        } else {
            productList.value = records;
        }
    } catch {
        productList.value = [];
    }
};

watchEffect(() => {
    fetchProductList();
});

function formatImage(url?: string): string {
    if (!url) {
        return emptyImgBg;
    }
    return url;
}

function handleImgError(e: Event) {
    const img = e.target as HTMLImageElement | null;
    if (!img) return;
    if (img.src === emptyImgBg) return;
    img.onerror = null;
    img.src = emptyImgBg;
}

function getVirtualSales(item: PreviewProductItem): string {
    const value = item.virtualSales;
    if (value === undefined || value === null || value === "") return "0";
    return String(value);
}

function getPrice(item: PreviewProductItem): number | string | undefined {
    if (item.price !== undefined && item.price !== null && item.price !== "") return item.price;
    return item.productPrice;
}

function formatPriceParts(price?: number | string): { int: string; dec: string } {
    const text = formatPrice(price);
    const pure = text.replace("¥", "");
    const parts = pure.split(".");
    return {
        int: parts[0] || "0",
        dec: parts[1] || "00"
    };
}

function getShopTitle(item: PreviewProductItem): string {
    return item.shop?.shopTitle || item.shopTitle || "官方自营";
}

function isSoldOut(item: PreviewProductItem): boolean {
    const isSeckillRaw = item.isSeckill;
    const hasSeckillFlag = isSeckillRaw !== undefined && isSeckillRaw !== null && isSeckillRaw !== "";
    const isSeckill = hasSeckillFlag ? Number(isSeckillRaw) === 1 : false;

    const productStock = Number(item.productStock ?? 999999);
    const seckillStock = Number(item.seckillStock ?? 999999);

    if (isSeckill) {
        return Number.isFinite(seckillStock) ? seckillStock === 0 : false;
    }
    return Number.isFinite(productStock) ? productStock === 0 : false;
}

function formatPrice(price?: number | string): string {
    if (price === undefined || price === null) return "¥0.00";
    const num = typeof price === "string" ? parseFloat(price) : price;
    return `¥${Number.isFinite(num) ? num.toFixed(2) : "0.00"}`;
}
</script>

<style lang="less" src="./preview.less" />
