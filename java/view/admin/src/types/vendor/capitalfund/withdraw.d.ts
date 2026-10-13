// 列表查询时筛选参数类型
export interface WithdrawFilterParams {
    page: number;
    size: number;
    sortField?: string;
    sortOrder?: string;
    /** 供应商ID（用于按主体筛选明细） */
    vendorId?: number | string;
    addTimeStart?: string;
    addTimeEnd?: string;
    status?: string;
}

// 获取列表返回参数类型
export interface WithdrawFilterResult {
    records: WithdrawFilterState[];
    filter: {
        page: number;
    };
    total: number;
}
export interface WithdrawFilterState {
    shopWithdrawLogId?: number;
    shopId?: number;
    /** 供应商端也可能返回门店/店铺名称（用于后台统一展示） */
    shopTitle?: string;
    amount?: string;
    /** 手续费（接口若返回） */
    handlingFee?: number | string;
    /** 手续费率（接口若返回） */
    handlingRate?: number | string;
    status?: number;
    /** 状态文案（接口若返回） */
    statusText?: string;
    addTime?: string;
    /** 前端历史字段名：remarks */
    remarks?: string;
    /** 接口常用字段名：remark */
    remark?: string;
    /** 审核/拒绝原因 */
    auditRemark?: string;
    /** 打款凭证 */
    paymentVoucher?: string;
    merchantAccountId?: number;
    accountData?: accountForm;
}

// 获取详情返回参数类型
export interface WithdrawFormResult {
    item: WithdrawFormState;
}
export interface WithdrawFormState {
    amount?: number | string;
    status?: number;
    merchantAccountId?: number | string;
    vendorAccountId?: number | string;
    auditRemark?: string;
    addTime?: string;
    paymentVoucher?: string;
    remark: string;
    accountData?: accountForm;
    supplierWithdrawalFeeRate?: number;
}
export interface accountForm {
    accountTypeText: string;
    accountId: number;
    merchantId: number;
    accountType: number;
    accountName: string;
    accountNo: string;
    bankName: string;
    addTime: string;
    bankBranch: string;
}
