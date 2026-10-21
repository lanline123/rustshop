// 服务记录相关类型定义

/** 服务记录列表查询参数 */
export interface ServiceRecordFilterParams {
    page: number;
    size: number;
    keyword?: string;
    servicemanId?: number;
    servicemanName?: string;
    orderSn?: string;
    orderStatus?: number;
    startDate?: string;
    endDate?: string;
}

/** 服务记录列表项 */
export interface ServiceRecordFilterState {
    id: number;
    orderId: number;
    orderSn: string;
    servicemanId: number;
    servicemanName: string;
    servicemanPhone: string;
    shopId: number;
    shopName: string;
    orderStatus: number;
    orderStatusName: string;
    serviceAddress: string;
    contactName: string;
    contactPhone: string;
    customerRemark?: string;
    totalAmount: number;
    createTime: string;
    assignTime?: string;
    startTime?: string;
    completeTime?: string;
    beforeImages?: string[];
    afterImages?: string[];
}

/** 服务记录列表返回结果 */
export interface ServiceRecordFilterResult {
    records: ServiceRecordFilterState[];
    total: number;
}

