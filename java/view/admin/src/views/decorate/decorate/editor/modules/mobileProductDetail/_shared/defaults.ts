/**
 * 商品详情模块 - 默认值
 *
 * 三个风格模块共享同一套字段默认值，仅 decorateStyle 不同。
 */

import { cloneDeep } from "lodash";
import type { ProductDetailModule, ProductDetailStyle } from "./types";

/** 基础默认值（不含 decorateStyle，供预览兜底合并使用） */
export const productDetailBaseDefaults: Omit<ProductDetailModule, "decorateStyle"> = {
    imageRatio: "4:3",
    imageMargin: 16,
    imageMode: 1,
    imageRadiusStyle: "square",
    titleSize: "medium",
    titleColor: "#1a1a1a",
    titleMaxLines: 2,
    priceColor: "#ff4d4f",
    marketPriceColor: "#999",
    moduleSortList: [
        { key: "activity", label: "活动信息", visible: true },
        { key: "spec", label: "规格参数", visible: true },
        { key: "review", label: "评价入口", visible: true },
        { key: "afterSale", label: "售后入口", visible: true },
        { key: "coupon", label: "领劵/优惠", visible: true }
    ],
    buyBarStyle: 1,
    buttonRadius: 5,
    showHome: true,
    showService: true,
    showCart: true,
    showShop: true,
    backgroundColor: "#ffffff",
    contentBackgroundColor: "#ffffff"
};

/** 按风格生成默认配置（供 registerModule 的 getDefaultValue 使用） */
export function createProductDetailDefaults(style: ProductDetailStyle): ProductDetailModule {
    const defaults: ProductDetailModule = {
        ...cloneDeep(productDetailBaseDefaults),
        decorateStyle: style
    };

    if (style === "A1") {
        defaults.backgroundColor = "transparent";
        defaults.contentBackgroundColor = "#ffffff";
    }

    return defaults;
}
