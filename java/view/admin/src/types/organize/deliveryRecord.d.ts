// 配送记录相关类型定义

/** 配送记录列表查询参数 */
export interface DeliveryRecordFilterParams {
    page: number;
    size: number;
    keyword?: string;
    deliverymanId?: number;
    deliverymanName?: string;
    orderSn?: string;
    orderStatus?: number;
    startDate?: string;
    endDate?: string;
}

/** 配送记录列表项 */
export interface DeliveryRecordFilterState {
    id: number;
    orderId: number;
    orderSn: string;
    deliverymanId: number;
    deliverymanName: string;
    deliverymanPhone: string;
    shopId: number;
    shopName: string;
    orderStatus: number;
    orderStatusName: string;
    pickupAddress: string;
    deliveryAddress: string;
    deliveryDistance?: number;
    contactName: string;
    contactPhone: string;
    customerRemark?: string;
    totalAmount: number;
    createTime: string;
    assignTime?: string;
    pickupTime?: string;
    completeTime?: string;
    afterImages?: string[];
}

/** 配送记录列表返回结果 */
export interface DeliveryRecordFilterResult {
    records: DeliveryRecordFilterState[];
    total: number;
}

