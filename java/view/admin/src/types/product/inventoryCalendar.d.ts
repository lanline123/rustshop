export interface InventoryCalendarFilterParams {
    page?: number;
    size?: number;
    sortField?: string;
    sortOrder?: string;
    keyword?: string;
    startDate: string;
    endDate?: string;
    shopIds?: string;
    productName?: String;
    shopScopeType?: number;
    timeType?: string;
}

export interface InventoryCalendarFilterState {}

export interface InventoryCalendarFilterDateStock {
    stockDate: string;
    totalStock: number;
    usedStock: number;
    availableStock: number;
}

export interface InventoryCalendarFilterResult {
    total: number;
    records: any;
}

export interface InventoryCalendarFilterDailyStockRequest {
    productId: number;
    skuId: number;
    shopId: number;
    stockDate: string;
}

export interface InventoryCalendarFilterDailyStockState {
    productReservationTimeId: number;
    startTime: string;
    endTime: string;
    totalStock: number;
    usedStock: number;
    availableStock: number;
    stockChange: number;
}

export interface InventoryCalendarFilterDailyStockResult {
    productId: number;
    shopId: number;
    stockDate: string;
    timeSlotStockList: InventoryCalendarFilterDailyStockState[];
}

export interface ProductTimeSlotStock {
    productReservationTimeId: number;
    startTime: string;
    endTime: string;
    totalStock?: number;
    usedStock?: number;
    availableStock?: number;
    quantity?: number;
}

export interface ProductInventoryCalendarDetail {
    productId: number;
    productName: string;
    picUrl: string;
    picThumb: string;
    picOriginal: string;
    timeSlotStockList: ProductTimeSlotStock[];
}

export interface ShopSelectionDTO {
    selectionType: number;
    shopIds?: number[];
}

export interface DateExclusionDTO {
    excludeWeekdays?: number[];
    excludeMonthDays?: number[];
    excludeSpecificDates?: string[];
}

export interface TimeSlotStockItem {
    productReservationTimeId: number;
    quantity: number;
}

export interface UnifiedStockSelectedProduct {
    productId: number;
    skuIds?: number[];
}

export interface SetUnifiedStockRequest {
    productId: number;
    skuId?: number;
    shopSelection: ShopSelectionDTO;
    startDate: string;
    endDate: string;
    dateExclusion?: DateExclusionDTO;
    quantity: number;
}

export interface SetTimeSlotStockRequest {
    productId: number;
    skuId: number;
    shopSelection?: ShopSelectionDTO;
    startDate: string;
    endDate: string;
    dateExclusion?: DateExclusionDTO;
    timeSlotStockItems: TimeSlotStockItem[];
}

export interface FormState {
    productId: number;
    selectionType: number;
    quantity: number;
    dateRange: string[];
    excludeType: number;
    excludeDay: number[];
}

export interface ExcludeOption {
    key: number;
    label: string;
}

export interface ShopInfo {
    shopId: number;
    shopTitle: string;
}
