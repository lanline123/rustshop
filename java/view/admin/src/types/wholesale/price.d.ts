// 批发价表单态与扁平结构

/** 单档阶梯：qty=「N 件及以上」；value=折扣值或金额 */
export interface Ladder {
    qty: number;
    value: number;
}

/** 阶梯单行校验错误：每个字段独立文案，谁有错谁标红 + 谁出现在输入框下方 */
export interface LadderError {
    /** qty 字段错误文案；undefined=无错 */
    qty?: string;
    /** value 字段错误文案；undefined=无错 */
    value?: string;
}

/** 等级类型售价（按批发商等级分组的阶梯） */
export interface GradeLadder {
    gradeId: number;
    gradeName: string;
    ladders: Ladder[];
}

/** 单个 SKU 的价格表单态 */
export interface SkuPriceForm {
    skuId: number;
    skuName: string;
    /** 零售价（只读展示，如 699.00） */
    retailPrice: number;
    /** 通用售价阶梯（gradeId=0）；已配类型售价时可留空 */
    generalLadders: Ladder[];
    /** 等级类型售价阶梯（gradeId>0，可空=不配类型售价） */
    gradeLadders: GradeLadder[];
}

/** 批发价主抽屉表单态 */
export interface PriceForm {
    productId: number;
    /** 是否多规格（由商品决定，只读） */
    isMultiSpec: boolean;
    /** 抽屉级单选；提交时映射为每行 1/2 */
    priceMode: "discount" | "fixed";
    /** 仅多规格有效；映射 multiSpecPriceMode（1=混批/2=SKU级） */
    calcMode: "spuMix" | "skuLevel";
    skus: SkuPriceForm[];
}

/**
 * 扁平价格行（回填 / 提交）
 * gradeId=0=通用价、skuId=0=SPU 兜底；当前模式字段为 BigDecimal 字符串，另一字段为 null
 */
export interface PriceRow {
    /** ≥0；0=SPU 兜底 */
    skuId: number;
    /** ≥0；0=通用价 */
    gradeId: number;
    /** 「N 件及以上」 */
    tierMinQty: number;
    /** 1=折扣 / 2=固定 */
    priceMode: 1 | 2;
    /** priceMode=1 时 [0.10, 9.99] */
    discount: string | null;
    /** priceMode=2 时 >0 */
    wholesalePrice: string | null;
}

/** 批发价详情出参（WholesalePriceDetailVO） */
export interface WholesalePriceDetailVO {
    productId: number;
    isMultiSpec: boolean;
    /** 1=商品级混批 / 2=SKU 级；单规格忽略 */
    multiSpecPriceMode: 1 | 2;
    /** 当前主要 mode（仅显示用） */
    priceMode: 1 | 2;
    skus: {
        skuId: number;
        skuSpecText: string;
        retailPrice: string;
        priceRows: (PriceRow & { gradeName?: string })[];
    }[];
}

/** 批发价保存入参（WholesalePriceSaveDTO，全量替换） */
export interface WholesalePriceSaveDTO {
    productId: number;
    multiSpecPriceMode: 1 | 2;
    priceRows: PriceRow[];
}

/** 批量设置档位（tiers 行） */
export interface BatchTier {
    minQty: number;
    discount?: string | null;
    wholesalePrice?: string | null;
}

/** 批量设置通用售价入参 */
export interface BatchCommonPriceDTO {
    productId: number;
    /** 应用到的 SKU；空=全 SKU */
    skuIds: number[];
    priceMode: 1 | 2;
    tiers: BatchTier[];
}

/** 批量设置类型售价入参 */
export interface BatchGradePriceDTO {
    productId: number;
    skuIds: number[];
    priceMode: 1 | 2;
    gradeRows: { gradeId: number; tiers: BatchTier[] }[];
}
