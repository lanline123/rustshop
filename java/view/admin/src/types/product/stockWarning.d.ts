// 库存预警通知总开关
export interface StockWarningSetting {
    // 归属主体：0 平台自营，>0 店铺/门店；店铺端与门店端由服务端强制改写
    shopId?: number;
    notifyEnabled: 0 | 1;
    // 未配置商品沿用的默认库存下限，由后端下发，前端不写死
    defaultLowerLimit?: number;
}

export interface StockWarningSettingSaveParams {
    shopId?: number;
    notifyEnabled: 0 | 1;
}

// 列表查询参数
export interface StockWarningFilterParams {
    page?: number;
    size?: number;
    keyword?: string;
    shopId?: number;
    // -1 全部（不提交）/ 0 正常库存 / 1 低库存 / 2 高库存 / 9 未配置
    stockStatus?: number;
    // -1 全部（不提交）/ 0 已关闭 / 1 已开启
    warnEnabled?: number;
    productType?: number;
    sortField?: string;
    sortOrder?: string;
}

// 列表行数据
export interface StockWarningFilterState {
    productId: number;
    // 0 表示单规格商品，多规格商品每个 SKU 单独成行
    skuId: number;
    productName: string;
    productSkuName: string;
    productPicThumb: string;
    productSn: string;
    skuSn: string;
    // 条形码
    productTsn: string;
    shopId: number;
    shopName: string;
    // 1 自营 / 2 店铺 / 3 门店
    ownerType: number;
    ownerTypeName: string;
    currentStock: number;
    // 在途库存，当前数据模型无该字段，后端固定返回 null
    transitStock: number | null;
    // 未配置时为 null
    lowerLimit: number | null;
    upperLimit: number | null;
    // 0 正常库存 / 1 低库存 / 2 高库存 / 9 未配置
    stockStatus: number;
    stockStatusName: string;
    // 未配置时为 null
    warnEnabled: 0 | 1 | null;
    // 规则更新时间，Unix 秒；未配置时为 null
    updateTime: number | null;
    // 相关商品 ID，用于跳转商品列表的 ids 参数
    relatedProductIds: number[];
    relatedProductCount: number;
}

export interface StockWarningFilterResult {
    records: StockWarningFilterState[];
    total: number;
}

// 商品/SKU 定位
export interface StockWarningTarget {
    productId: number;
    skuId: number;
}

export interface StockWarningSaveParams extends StockWarningTarget {
    shopId?: number;
    warnEnabled: 0 | 1;
    lowerLimit: number | null;
    upperLimit: number | null;
}

export interface StockWarningBatchSaveParams {
    shopId?: number;
    targets: StockWarningTarget[];
    warnEnabled: 0 | 1;
    lowerLimit: number | null;
    upperLimit: number | null;
}

// 导出只认勾选行：ids 为 `productId_skuId` 逗号分隔，单规格商品 skuId 为 0；其余筛选条件后端忽略
export interface StockWarningExportParams {
    ids: string;
    shopId?: number;
}

export interface StockWarningBatchSaveFailItem {
    productId: number;
    skuId: number;
    reason: string;
}

export interface StockWarningBatchSaveResult {
    successCount: number;
    failCount: number;
    failItems: StockWarningBatchSaveFailItem[];
}

// 导入失败项与批量设置的失败项结构不同：这里定位到 Excel 行号与填写的编码
export interface StockWarningImportFailItem {
    // Excel 实际行号，表头为第 1 行
    rowNumber: number;
    skuSn: string;
    reason: string;
}

export interface StockWarningImportResult {
    // 有效数据行数，不含被跳过的空行
    totalCount: number;
    successCount: number;
    failCount: number;
    failItems: StockWarningImportFailItem[];
}
