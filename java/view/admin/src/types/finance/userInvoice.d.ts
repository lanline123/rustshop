
export interface UserInvoiceFilterParams {
    page: number,
    size: number,
    sortField: string,
    sortOrder: string,
    keyword?: string;
    status?: number | string;
}

// 获取列表返回参数类型
export interface UserInvoiceFilterState {
    invoiceId: number;
    username?: string;
    companyName?: string;
    companyCode?: string;
    companyAddress?: string;
    companyPhone?: string;
    companyBank?: string;
    companyAccount?: string;
    applyReply?: string;
    statusName?: string;
    status?: number;
}
export interface UserInvoiceFilterResult {
    records: UserInvoiceFilterState[];
    filter: UserInvoiceFilterParams;
    total: number;
}

// 获取详情返回参数类型
export interface UserInvoiceFormState {
    id?:number;
    invoiceId?:number;
    statusName?: string;
    username?: string;
    companyName?: string;
    companyCode?: string;
    companyAddress?: string;
    companyPhone?: string;
    companyBank?: string;
    companyAccount?: string;
    applyReply?: string;
    status?: number;
}





