import type {UserInvoiceFormState} from "@/types/finance/userInvoice";

export interface OrderInvoiceFilterParams {
    page: number,
    size: number,
    sortField: string,
    sortOrder: string,
    keyword?: string;
    status?: number | string;
    invoiceType?: number | string;
    shopType?: number | string;
    shopId?: number;
}

// 获取列表返回参数类型
export interface OrderInvoiceFilterState {
    id: number;
    username?: string;
    invoiceType?: number;
    invoiceTypeName?: string;
    companyName?: string;
    amount?: string;
    orderId?: number;
    orderSn?: string;
    addTime?: string;
    companyCode?: string;
    companyAddress?: string;
    companyPhone?: string;
    companyBank?: string;
    companyAccount?: string;
    applyReply?: string;
    statusName?: string;
    status?: number;
}
export interface OrderInvoiceFilterResult {
    records: OrderInvoiceFilterState[];
    filter: OrderInvoiceFilterParams;
    total: number;
}

// 获取详情返回参数类型
export interface OrderInvoiceFormState {
    id?: number;
    username?: string;
    invoiceType?: number;
    invoiceTypeName?: string;
    companyName?: string;
    amount?: string;
    titleType?: number;
    orderId?: number;
    orderSn?: string;
    addTime?: string;
    companyCode?: string;
    invoiceContent?: string;
    companyAddress?: string;
    companyPhone?: string;
    companyBank?: string;
    companyAccount?: string;
    applyReply?: string;
    statusName?: string;
    mobile?: string;
    email?: string;
    userAddress?: string;
    status?: number;
    userInvoice?: UserInvoiceFormState;
    invoiceAttachment? : object[]
}





