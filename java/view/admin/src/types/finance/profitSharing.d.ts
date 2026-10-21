// 分账结算（线上分账单）相关类型

/** 分账单列表项 */
export interface ProfitSharingOrderItem {
    id?: number | string;
    /** 分账单号（商户分账批次号 out_order_no） */
    outOrderNo?: string;
    orderId?: number | string;
    orderSn?: string;
    shopId?: number | string;
    shopName?: string;
    /** 供应商ID（0表示店铺分账单） */
    vendorId?: number | string;
    /** 供应商名称 */
    vendorName?: string;
    /** 结算主体类型：1店铺 2供应商 */
    settlementType?: number;
    /** 微信交易号 */
    transactionId?: string;
    /** 订单金额（元） */
    totalAmount?: number;
    /** 分账金额（元） */
    splitAmount?: number;
    /** 服务费（元） */
    serviceFee?: number;
    /** 分销佣金（元） */
    commissionFee?: number;
    /** 分账状态 PENDING / PROCESSING / SUCCESS / FAILED */
    status?: string;
    statusDesc?: string;
    /** 完结状态 UNFINISHED / FINISHED */
    finishStatus?: string;
    finishStatusDesc?: string;
    /** 触发方式 MANUAL / AUTO */
    triggerType?: string;
    /** 是否允许手动完结（仅分账中/分账成功且未完结时为 true，用于控制「手动完结」按钮显隐） */
    canFinish?: boolean;
    /** 结算单ID */
    settlementId?: number | string;
    /** 结果描述 */
    resultDesc?: string;
    createTime?: string;
    modifyTime?: string;
}

/** 分账单列表查询参数 */
export interface ProfitSharingListParams {
    page: number;
    size: number;
    sortField?: string;
    sortOrder?: string;
    shopId?: number | string;
    /** 结算主体类型：1店铺 2供应商 */
    settlementType?: number;
    /** 供应商ID */
    vendorId?: number | string;
    status?: string;
    finishStatus?: string;
    startDate?: string;
    endDate?: string;
}

/** 分账单列表结果 */
export interface ProfitSharingListResult {
    records: ProfitSharingOrderItem[];
    total: number;
}

/** 批量发起分账（对应后端 ProfitSharingSubmitParam） */
export interface ProfitSharingSubmitForm {
    settlementIds: number[];
}
