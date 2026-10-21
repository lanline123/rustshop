/**
 * 规格维度选项
 */
export interface WholesaleSpecOption {
    /** 选项 ID（attributesId） */
    optionId: number;
    /** 选项显示名（如「红色」「8 骨墨绿」） */
    label: string;
    /** 选项缩略图（有图属性时存在） */
    image?: string;
}

/**
 * 规格维度（按商品定义顺序排列；最后一维为末维列表，其余为图块/标签选择器）
 */
export interface WholesaleSpecDimension {
    /** 维度 ID */
    specId: number;
    /** 维度名（如「颜色」「尺寸」） */
    specName: string;
    /** 是否图属性（true 展缩略图卡片，false 展文字标签） */
    hasImage: boolean;
    /** 该维度的所有选项 */
    options: WholesaleSpecOption[];
}

/**
 * 末维展开的可选数 SKU 行
 */
export interface WholesaleSkuPick {
    /** SKU ID（单 SKU 商品时由调用方塞默认 SKU） */
    skuId: number;
    /** 完整规格名（叠加前置已选维度） */
    skuName: string;
    /** 缩略图（可选） */
    thumb?: string;
    /** 后端给的展示单价文案 */
    unitPriceText: string;
    /** 可售库存 */
    stock: number;
    /** 库存提示文案（如「库存充足」/「仅剩 5 件」） */
    stockText: string;
}

/**
 * 末维选数变更事件载荷
 */
export interface WholesaleQtyChangePayload {
    /** 全局选数：skuId -> qty（跨前置维度保持） */
    pickedQty: Record<number, number>;
    /** 已选 SKU 款数（qty > 0 的 SKU 数） */
    pickedKinds: number;
    /** 已选总件数 */
    pickedTotal: number;
    /** 本次变更的 SKU（用于驱动阶梯条展示该 SKU 的价格阶梯）；reset 时为 undefined */
    changedSkuId?: number;
}
