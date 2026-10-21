// 子商户管理相关类型

/** 子商户列表项 */
export interface SubMerchantItem {
    id?: number | string;
    shopId?: number | string;
    shopName?: string;
    /** 支付渠道，目前 WECHAT */
    payChannel?: string;
    /** 子商户号 */
    subMchId?: string;
    /** 子商户 AppID（选填） */
    subAppid?: string;
    merchantName?: string;
    /** 状态：0-待激活 1-已激活 2-已冻结 */
    status?: number;
    statusDesc?: string;
    /** 关联进件ID（有则为进件创建，无则为手动绑定） */
    applicationId?: number | string;
    createTime?: string;
    modifyTime?: string;
}

/** 子商户列表查询参数 */
export interface SubMerchantListParams {
    page: number;
    size: number;
    sortField?: string;
    sortOrder?: string;
    shopId?: number | string;
    subMchId?: string;
    payChannel?: string;
    status?: number | string;
}

/** 子商户列表结果 */
export interface SubMerchantListResult {
    records: SubMerchantItem[];
    total: number;
}

/** 子商户绑定表单 */
export interface SubMerchantBindForm {
    shopId?: number | string;
    payChannel?: string;
    subMchId?: string;
    subAppid?: string;
    merchantName?: string;
}
