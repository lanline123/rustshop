/** 销售明细 */
export interface statisticsSalesFilterParams {
    /** 开始时间 */
    startTime?: string;
    /** 结束时间 */
    endTime?: string;
}

export interface statisticsSalesFilterState {
    salesData: SalesData;
    salesStatisticsData: SalesStatisticsData;
    errcode: number;
    message: string;
}

export interface SalesData {
    productView: number;
    productViewGrowthRate: string;
    productVisitor: number;
    productVisitorGrowthRate: string;
    orderNum: number;
    orderNumGrowthRate: string;
    paymentAmount: number;
    paymentAmountGrowthRate: string;
    refundAmount: number;
    refundAmountGrowthRate: string;
    refundQuantity: number;
    refundQuantityGrowthRate: string;
}

export interface SalesStatisticsData {
    horizontalAxis: string[];
    longitudinalAxisPaymentAmount: number[];
    longitudinalAxisRefundAmount: number[];
    longitudinalAxisProductView: number[];
    longitudinalAxisProductVisitor: number[];
}

/** 销售商品明细 */
export interface SaleDetaillistFilterParams {
    startTime: string;
    endTime: string;
    keyword: string;
    page: number;
    size: number;
    isExport?: string;
    sortField?: string;
    sortOrder?: string;
}

export interface SaleDetaillistFilterState {
    records: SaleDetaillistFilterResult[];
    filter: SaleDetaillistFilter;
    total: number;
    errcode: number;
    message: string;
}

export interface SaleDetaillistFilter {
    startTime: string;
    endTime: string;
    keyword: string;
    isExport: number;
    page: number;
    size: number;
    sortField: string;
    sortOrder: string;
}

export interface SaleDetaillistFilterResult {
    itemId: number;
    orderId: number;
    orderSn: string;
    userId: number;
    price: string;
    quantity: number;
    productId: number;
    productName: string;
    productSn: string;
    picThumb: string;
    skuId: number;
    skuData: SkuData | null;
    deliveryQuantity: number;
    productType: number;
    isGift: number;
    shopId: number;
    isPin: number;
    prepayPrice: string;
    subtotal: string;
    order: Order;
}

export interface Order {
    orderSn: string;
    addTime: string;
}

export interface SkuData {
    name: string;
    value: string;
}
