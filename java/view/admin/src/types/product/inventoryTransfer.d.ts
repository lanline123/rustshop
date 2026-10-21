// 获取列表返回参数类型
export interface InventoryFilterState {
    allocateId: number;
    allocateCode: string;
    warehousingOrganizationId: number;
    outboundOrganizationId: number;
    status: number;
    addBy: string;
    stockInComment: string;
    stockOutComment: string;
    outboundOrganizationName: string;
    warehousingOrganizationName: string;
    canAudit: number;
}

export interface InventoryFilterParams{
    page?: number;
    size?: number;
    keyword?: string;
    status?: number | string;
    addBy?: string;
    sortField?: string;
    sortOrder?: string;
}

export interface InventoryFilterResult {
    records: InventoryFilterState[];
    filter: InventoryFilterParams;
    total: number;
    waitingCheckedCount: number;
}

export interface FormState {
    productStockAllocateRecordDetailList: any;
    stockOutComment?: string;
    warehousingOrganizationId?: number;
    outboundOrganizationId?: any;
    comment?: string;
}

export interface FormDetailState {
    stockInComment?: string;
    stockOutComment?: string;
    comment?: string;
    outboundOrganizationName?: string;
    warehousingOrganizationName?: string;
    status?: number;
    productAllocateDetailVOList?: any[];
}


export interface AuditAllocate {
    id?: number;
    auditType?: number;
    remark?: string;
}
