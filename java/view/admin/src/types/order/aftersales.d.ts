// 列表查询时筛选参数类型
export interface AftersalesFilterParams {
    page: number;
    size: number;
    sortField: string;
    sortOrder: string;
    keyword?: string;
    /** 交易单号 */
    outTradeNo?: string;
    aftersalesSn?: string;
    aftersaleType?: number | string;
    status?: number | string;
}

// 获取列表返回参数类型
export interface AftersalesFilterResult {
    records: AftersalesFilterState[];
    statusList: any[];
    typeList: any[];
    filter: {
        page: number;
    };
    total: number;
}

// 获取详情返回参数类型
export interface AftersalesFilterState {
    aftersaleId?: number;
    vendorId?: number;
    /** 关联订单ID（表头「订单详情」入口依赖，需后端在列表接口返回） */
    orderId?: number;
    /** 订单标记（表头「标记」入口回显，需后端在列表接口返回） */
    mark?: number;
    /** 管理员备注（表头「标记」入口回显） */
    adminNote?: string;
    aftersalesSn?: number;
    orderSn?: number;
    /** 交易单号 */
    outTradeNo?: string;
    orderItemId?: number;
    aftersaleType?: number;
    status?: number;
    number?: number;
    pics?: string[];
    description?: string;
    reply?: string;
    addTime?: string;
    shippingTime?: string;
    trackingNo?: string;
    logisticsName?: string;
    userName?: string;
    aftersaleReason?: string;
    aftersalesTypeName?: string;
    statusName?: string;
    /** 建议退款金额（列表内联「同意退款」校验上限，需后端在列表接口返回） */
    suggestRefundAmount?: number;
    /** 退款金额（确认收货弹窗退款明细展示，需后端在列表接口返回） */
    refundAmount?: number;
    /** 实际退款金额（优先于 refundAmount 展示） */
    actualRefundAmount?: number;
    /** 拒绝/拒收快捷理由（列表内联弹窗理由标签，需后端在列表接口返回） */
    refuseReason?: string[];
    /** 退货地址（列表内联「同意并发送退货地址」预填，缺失时前端按身份拉取） */
    returnAddress?: string;
    aftersalesItems: productList[];
}
export interface productList {
    aftersalesItemId?: number;
    orderItemId?: number;
    number?: number;
    aftersaleId?: number;
    orderSn?: string;
    productName?: string;
    orderId?: number;
    picThumb?: string;
    productSn?: string;
    productId?: number;
    quantity?: number;
    price?: number;
    supplyPrice?: number | string | null;
}

export interface FormState {
    aftersaleId?: number;
    aftersaleType?: number;
    addTime?: string;
    shippingTime?: string;
    aftersalesTypeConfig?: object[];
    aftersalesLog?: aftersalesLog[];
    aftersalesTypeName?: string;
    description?: string;
    logisticsName?: string;
    number: number;
    modifyNumber?: number;
    shopId?: number;
    vendorId?: number;
    orderId?: number;
    orderItemId?: number;
    orderSn?: string;
    picThumb: string;
    pics?: string;
    productName?: string;
    productSn?: string;
    reply?: string;
    status?: number;
    refundAmount?: number;
    suggestRefundAmount?: number;
    statusName?: string;
    /** 状态副文案（顶部状态卡副行，后端可选返回，缺失走本地兜底） */
    statusDescription?: string;
    statusDesc?: string;
    /** 自动同意退款截止时间（处理中且开启自动同意退货时后端返回，驱动顶部倒计时） */
    autoAgreeEndTime?: string;
    /** 服务器当前时间（用于校准本地时钟漂移，配合 autoAgreeEndTime 计算倒计时） */
    serverTime?: string;
    /** 订单标记 / 备注（「标记订单」弹窗回显） */
    mark?: number;
    adminNote?: string;
    statusConfig?: object[];
    returnAddress?: string;
    trackingNo?: string;
    userName?: string;
    aftersaleReason?: string;
    refuseReason?: string[];
    aftersalesItems?: productList[];
    refund?: any;
    actualRefundAmount?: number;
}

export interface aftersalesLog {
    logId?: number;
    aftersaleId?: number;
    logInfo?: string;
    addTime?: string;
    adminName?: string;
    refundMoney?: string;
    refundType?: number;
    refundDesc?: string;
    userName?: string;
    returnPic: returnPic[];
    shopId: number;
    vendorId: number;
}

export interface returnPic {
    picId?: number;
    picThumb?: string;
    picUrl?: string;
    picName?: string;
}
