/**
 * 起批维度
 * @description 1=SPU 整体起批（合计达 moq） / 2=SKU 单规格起批（按 skuRules[].moqSku）
 */
export type WholesaleDimension = 1 | 2;

/**
 * 单个 SKU 的起批 / 倍数规则
 */
export interface WholesaleSkuRule {
    /** SKU ID；商品无规格时为 0 */
    skuId: number;
    /** 该 SKU 的起订数量（SKU 维度生效；SPU 维度通常为 0） */
    moqSku: number;
    /**
     * 倍数购买的倍数（永远按 SKU 配置，与起批维度无关）
     * @remarks 1 = 不限制
     */
    multiple: number;
}

/**
 * 商品起批规则
 * @description 接口 GET wholesale/product/buyRule
 */
export interface WholesaleBuyRule {
    /** 商品 ID */
    productId?: number;
    /** 后端是否已配置批发规则；false 时前端按零售商品处理 */
    configured: boolean;
    /** 起批维度 */
    dimension: WholesaleDimension;
    /** SPU 维度的整体起订量（后端字段名为 moq）；SKU 维度通常为 0 */
    moq: number;
    /** 各 SKU 的起批 / 倍数规则 */
    skuRules: WholesaleSkuRule[];
    /** 后端预格式化的提示文案，可直接展示 */
    hint?: string;
}
