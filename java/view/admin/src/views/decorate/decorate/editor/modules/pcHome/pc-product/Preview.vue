<template>
    <div class="dec-pc-product" :class="moduleClasses" :style="cssVars">
        <div class="dec-pc-product__content">
            <div v-if="!productList || productList.length === 0" class="dec-pc-product__empty">
                <div class="dec-pc-product__empty-icon"></div>
                <div class="dec-pc-product__empty-text">点击添加商品</div>
            </div>

            <ModuleTitle v-if="moduleConfig.title?.showTitle" :title="moduleConfig.title" />

            <div v-if="productList && productList.length > 0" class="dec-pc-product__wrapper">
                <div class="dec-pc-product__list">
                    <div v-for="item in productList" :key="item.productId" class="dec-pc-product__item">
                        <a class="dec-pc-product__card" href="javascript:;">
                            <div class="dec-pc-product__photo">
                                <img class="dec-pc-product__img" :src="formatImage(pickPic(item))" :alt="item.productName" />
                            </div>
                            <div class="dec-pc-product__info">
                                <div v-if="moduleConfig.showName" class="dec-pc-product__name">
                                    {{ item.productName }}
                                </div>
                                <div v-if="moduleConfig.showBrief" class="dec-pc-product__brief">
                                    {{ item.brief || "商品描述" }}
                                </div>
                                <div v-if="moduleConfig.showPrice" class="dec-pc-product__price">
                                    {{ formatPrice(item.productPrice) }}
                                </div>
                            </div>
                        </a>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
/**
 * PC 商品模块 - 预览组件
 * 取数模式对齐 Vort（1=指定商品/2=分类/3=标签），参数名以 Tigshop product/product/list 接口为准
 */
import { computed, ref, watchEffect } from "vue";
import type { PcProductModule, ProductItem } from "./types";
import { pcProductDefaults } from ".";
import { ModuleTitle } from "../_shared";
import { deepMerge } from "../../../utils/object";
import { getProductList } from "@/api/product/product";
import { imageFormat, pickPic } from "@/utils/format";

defineOptions({ name: "PcProductPreview" });

const props = defineProps<{
    module: Record<string, unknown>;
}>();

const moduleConfig = computed<PcProductModule>(() => {
    return deepMerge(pcProductDefaults, props.module as Partial<PcProductModule>);
});

const productList = ref<ProductItem[]>([]);

const columns = computed(() => {
    const style = Number(moduleConfig.value.style ?? 1);
    return style === 2 ? 5 : 4;
});

const moduleClasses = computed(() => {
    const style = Number(moduleConfig.value.style ?? 1);
    return [`dec-pc-product--style-${style}`];
});

const cssVars = computed(() => {
    const data = moduleConfig.value;
    const frame = data.frame;
    return {
        // 扩展设置（ModuleContainerSettings）写入的是 frame，预览需优先读取 frame
        "--dec-box-padding": `${frame?.boxPadding ?? data.boxPadding ?? 10}px`,
        "--dec-box-padding-top": `${frame?.boxPaddingTop ?? data.boxPaddingTop ?? 5}px`,
        "--dec-box-padding-bottom": `${frame?.boxPaddingBottom ?? data.boxPaddingBottom ?? 5}px`,
        "--dec-box-radius": `${frame?.boxRadius ?? data.boxRadius ?? 0}px`,
        "--dec-bg-color": frame?.backgroundColor || data.backgroundColor || "#ffffff",
        // "左右内边距"来自 frame.leftAndRightPadding，仅作用于内容区左右
        "--dec-inner-padding": `${frame?.leftAndRightPadding ?? frame?.innerPadding ?? data.innerPadding ?? 0}px`,
        "--dec-goods-padding": `${data.goodsPadding ?? 5}px`,
        "--dec-columns": String(columns.value)
    };
});

const fetchProductList = async () => {
    const products = moduleConfig.value.products;
    if (!products) {
        productList.value = [];
        return;
    }

    const getRecordKey = (item: Partial<ProductItem>): string => {
        const rec = item as Record<string, unknown>;
        const key = rec.productId ?? rec.id;
        return key === undefined || key === null ? "" : String(key);
    };

    const sortRecordsByIds = (records: ProductItem[], ids: Array<string | number>): ProductItem[] => {
        const order = ids.map((v) => String(v));
        if (order.length === 0) return records;
        const map = new Map<string, ProductItem>();
        records.forEach((r) => map.set(getRecordKey(r), r));
        const sorted: ProductItem[] = [];
        order.forEach((id) => {
            const hit = map.get(id);
            if (hit) sorted.push(hit);
        });
        // 兜底：把未命中的记录补到末尾（避免接口返回了额外数据时丢失）
        records.forEach((r) => {
            const k = getRecordKey(r);
            if (k && !order.includes(k)) sorted.push(r);
        });
        return sorted;
    };

    const param: Record<string, unknown> = {};

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

    // 参数名映射以 Tigshop 接口为准：productIds → ids（逗号分隔字符串）、productCategoryId → categoryId、productTag → introType
    if (type === 1) {
        const idArr = Array.isArray(products.productIds) ? products.productIds : String(products.productIds).split(",").filter(Boolean);
        param.ids = idArr.join(",");
        param.size = idArr.length || 15;
    } else {
        param.page = 1;
        if (type === 2) {
            param.categoryId = products.productCategoryId;
        } else {
            param.introType = products.productTag;
        }
        param.size = products.productNumber ?? 15;
    }

    try {
        const result = await getProductList({ productStatus: 1, checkStatus: 1, ...param });
        const records = (result.records || []) as unknown as ProductItem[];
        // 选商品模式：预览必须按 productIds 的顺序展示（拖拽排序回显）
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
        return 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" width="100" height="100"><rect fill="%23f5f5f5" width="100" height="100"/></svg>';
    }
    // 接口返回的 picUrl/picThumb 多为相对路径，需补 storageUrl 域名前缀（http 链接原样返回）
    return imageFormat(url);
}

function formatPrice(price?: number | string): string {
    if (price === undefined || price === null) return "¥0.00";
    const num = typeof price === "string" ? parseFloat(price) : price;
    return `¥${Number.isFinite(num) ? num.toFixed(2) : "0.00"}`;
}
</script>

<style lang="less" src="./preview.less" />
