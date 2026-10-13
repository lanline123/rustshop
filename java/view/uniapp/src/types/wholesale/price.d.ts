/**
 * @file 批发报价类型
 */

/**
 * 详情接口扩展的阶梯报价行（金额来源真理源）
 * - 由 GET /api/product/product/detail 在批发商身份下返回 `wholesalePriceList[]`
 * - 按 priceId 升序，可同时含 gradeId=0（通用价）+ gradeId>0（等级专享价）
 *   和 skuId=null（SPU 维度）/ skuId>0（SKU 维度）
 */
export interface WholesalePriceTier {
    /** 阶梯行 ID（排序键） */
    priceId: number;
    /** 该阶梯的起量门槛（"≥ N 件"） */
    tierMinQty: number;
    /** 价格模式：1=折扣 / 2=固定金额 */
    priceMode: 1 | 2;
    /** 折率字符串（如 "8.50" 表 8.5 折）；priceMode=1 时有值 */
    discount: string | null;
    /** 固定金额字符串；priceMode=2 时有值 */
    fixedPrice: string | null;
    /** 等级 ID；0=通用（所有等级共享），>0=等级专享 */
    gradeId: number;
    /** 等级名；通用时为 "通用" */
    gradeName: string;
    /** SKU ID；null=SPU 维度，>0=该 SKU 专属 */
    skuId: number | null;
    /**
     * 后端已派生的最终展示价（BigDecimal 字符串）
     * 前端直接渲染，不做 discount→金额 换算
     */
    displayPrice: string;
}
