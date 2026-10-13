// 获取列表返回参数类型
export interface InventoryFilterState {
    productId: number;
    productName: string;
    productSkuName: string;
    productSkuId: number;
    productUnit: string;
    currentProductStock: number;
    productCost: number;
    productPicThumb: string;
    quantity: number;
    inventoryId: number;
}

export interface InventoryFilterParams {
    page?: number;
    size?: number;
    sortField?: string;
    sortOrder?: string;
    keyword?: string;
    productName?: string;
    stockSearch?: number;
}


export interface InventoryFilterResult {
    records: InventoryFilterState[];
    filter: InventoryFilterParams;
    total: number;
    waitingCheckedCount: number;
}
