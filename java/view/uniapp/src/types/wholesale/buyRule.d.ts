/**
 * @file 商品起批规则类型
 */

/** SKU 维度起批 / 倍数规则（任何 dimension 下倍数都按 SKU） */
export interface SkuBuyRule {
    skuId: number;
    /** SKU 维度的起订量；dimension=1（SPU）时可能为 0 */
    moqSku: number;
    /** 倍数购买的倍数；1=不限制 */
    multiple: number;
}

/**
 * 商品起批规则
 * - 由 GET /api/wholesale/product/buyRule 返回
 * - ⚠️ 只回起批规则，不含金额（金额走详情接口的 wholesalePriceList）
 */
export interface BuyRule {
    productId: number;
    /** 是否配置了批发能力；false=本商品没有批发功能，前端按零售渲染 */
    configured: boolean;
    /** 起批维度：1=SPU（整商品累加达 moq）/ 2=SKU（按 skuRules[].moqSku） */
    dimension: 1 | 2;
    /** SPU 维度整体起订量（后端字段名为 moq，与 PC 对齐）；dimension=2 时通常为 0 */
    moq: number;
    /** 各 SKU 起批数 + 倍数 */
    skuRules: SkuBuyRule[];
    /** 后端已组装好的提示文案，前端直接展示 */
    hint?: string;
}
