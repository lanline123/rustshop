// 批发商品管理

/** 批发商品分页查询入参（GET /product/page） */
export interface WholesaleProductPageQuery {
    page?: number;
    size?: number;
    /** 商品名/货号 */
    keyword?: string;
    /** 起批维度 1=SPU / 2=SKU 筛选 */
    moqDimension?: number;
    sortField?: string;
    sortOrder?: string;
}

/** 批发商品列表项（WholesaleProductVO） */
export interface WholesaleProductVO {
    productId: number;
    productName: string;
    productSn?: string;
    productNo?: string;
    picThumb?: string;
    /** 零售价范围（展示参考） */
    retailPriceRange?: string;
    /** 批发价范围（已格式化，折扣或金额），未配显示「未设置」 */
    wholesalePriceDisplay?: string;
    /** 起批维度 1=SPU / 2=SKU */
    moqDimension?: number;
    moqDimensionName?: string;
    /** 起批数量（SPU=moq_spu；SKU=各 SKU min 值） */
    moqDisplay?: number;
    /** 倍数购买是否已设置（任意 SKU multiple>1） */
    multipleSet?: boolean;
    stock?: number;
    /** 上下架状态（复用 product.shelf_status） */
    shelfStatus?: number;
    /** 批发配置启停 */
    configStatus?: number;
}

/** 批发商品分页结果（IPage<WholesaleProductVO>） */
export interface WholesaleProductPageResult {
    records: WholesaleProductVO[];
    total: number;
}

/** 可选批发商品分页查询入参（GET /product/selectable/page；平台商品 + 剔除已配置批发） */
export interface SelectableWholesaleProductQuery {
    page?: number;
    size?: number;
    /** 关键词：模糊匹配 productName / productSn / productTsn */
    keyword?: string;
    /** 分类 ID 过滤 */
    categoryId?: number | string;
    /** 品牌 ID 过滤 */
    brandId?: number;
    /** 上架状态：1=上架（默认）/ 0=下架 */
    productStatus?: number;
}

/** 可选批发商品列表项（ProductListResDTO 中选品所需字段） */
export interface SelectableWholesaleProductVO {
    productId: number;
    productName?: string;
    productSn?: string;
    picThumb?: string;
    productPrice?: number | string;
}

/** 可选批发商品分页结果（IPage<ProductListResDTO>） */
export interface SelectableWholesaleProductPageResult {
    records: SelectableWholesaleProductVO[];
    total: number;
}

/** 添加批发商品入参（POST /product/add） */
export interface WholesaleProductAddDTO {
    productId: number;
    /** 默认 1=商品级混批 / 2=SKU 级；单规格商品忽略 */
    multiSpecPriceMode?: 1 | 2;
    /** 默认 1=SPU / 2=SKU */
    moqDimension?: number;
}

/** 批量添加批发商品入参（POST /product/batchAdd） */
export interface WholesaleProductBatchAddDTO {
    /** 商品 ID 数组（1-50 个，正整数） */
    productIds: number[];
}

/** 批量添加批发商品结果（WholesaleProductBatchAddVO） */
export interface WholesaleProductBatchAddVO {
    /** 成功转换为批发商品的数量 */
    successCount: number;
    /** 跳过的 productId 列表（已是批发商品，不计失败） */
    skippedIds: number[];
    /** 未添加成功的商品及原因（包含跨店 5314、不支持类型 5315） */
    failedItems: WholesaleProductBatchAddFailedItem[];
}

export interface WholesaleProductBatchAddFailedItem {
    productId: number;
    errorCode: number;
    errorMessage: string;
}

/** 起批量设置入参（POST /product/buyRule/save，统一字段 moq） */
export interface WholesaleBuyRuleSaveDTO {
    productId: number;
    /** 1=SPU / 2=SKU */
    dimension: number;
    /** 起批数：dimension=1 时为加总起批；dimension=2 时为每 SKU 起批（应用到所有 SKU），≥1 */
    moq: number;
}

/** 批量起批量设置入参（POST /product/buyRule/batchSave：对所选商品统一设置同一起批维度+起批数） */
export interface WholesaleBuyRuleBatchSaveDTO {
    /** 目标商品 ID 列表 */
    productIds: number[];
    /** 1=SPU / 2=SKU */
    dimension: number;
    /** 统一起批数：dimension=1 加总起批；dimension=2 每 SKU 起批（套用到所有 SKU），≥1 */
    moq: number;
}

/** 倍数购买保存入参（POST /product/multiple/save，永远按 SKU） */
export interface WholesaleMultipleSaveDTO {
    productId: number;
    /** multiple=1 表示不限制 */
    skuMultiples: { skuId: number; multiple: number }[];
}

/** 起批/倍数 SKU 规则（buyRule/detail skuRules 行） */
export interface WholesaleSkuRuleVO {
    skuId: number;
    /** SKU 起批数 */
    moqSku?: number;
    /** 倍数购买，1=不限制 */
    multiple?: number;
}

/** 起批规则详情（GET /product/buyRule/detail，WholesaleProductBuyRuleVO） */
export interface WholesaleProductBuyRuleVO {
    productId: number;
    /** 是否已配置批发能力 */
    configured?: boolean;
    /** 起批维度：1=SPU / 2=SKU */
    dimension?: number;
    /** 统一起批数；回填优先取此字段 */
    moq?: number;
    /** SPU 起批数（dimension=1，旧字段，兼容回填） */
    moqSpu?: number;
    /** SKU 维度规则（含起批数与倍数） */
    skuRules?: WholesaleSkuRuleVO[];
    /** 前端展示提示文案（后端组装） */
    hint?: string;
}

/** 商品 SKU 简要信息（起批量/倍数抽屉回填用） */
export interface WholesaleProductSku {
    skuId: number;
    skuName?: string;
    skuSn?: string;
    /** 起批数（SKU 维度） */
    moqSku?: number;
    /** 倍数 */
    multiple?: number;
}
