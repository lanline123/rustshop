/**
 * 批发价阶梯条单档
 * @description 金额由后端给文案（priceText），前端不做 discount→金额换算
 */
export interface WholesalePriceLadder {
    /** 触发数量「N 件及以上」 */
    qty: number;
    /** 后端格式化后的金额文案，如 "¥45.00" */
    priceText: string;
    /** 原始数值（仅排序/比较用，业务展示走 priceText） */
    rawPrice?: number;
}

/**
 * 详情页价格区（仅一级阶梯时取 min/max 显示区间；多级阶梯 ladders.length > 1）
 */
export interface WholesalePriceDisplay {
    /** 阶梯档位列表 */
    ladders: WholesalePriceLadder[];
    /** 跨 SKU 的最低单价文案（仅一级阶梯 + 多 SKU 时用） */
    minPriceText?: string;
    /** 跨 SKU 的最高单价文案（仅一级阶梯 + 多 SKU 时用） */
    maxPriceText?: string;
    /** 跨 SKU 的最低单价原始数值 */
    rawMinPrice?: number;
    /** 跨 SKU 的最高单价原始数值 */
    rawMaxPrice?: number;
}
