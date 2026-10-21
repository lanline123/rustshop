// 进件管理（微信特约商户进件）相关类型

/** 进件申请列表项 / 详情 */
export interface MerchantApplicationItem {
    id?: number | string;
    shopId?: number | string;
    shopName?: string;
    /** 申请主体类型：SHOP 店铺/门店 / VENDOR 供应商 */
    applicantType?: string;
    /** 申请主体ID */
    applicantId?: number | string;
    /** 申请主体名称（店铺名/供应商名） */
    applicantName?: string;
    payChannel?: string;
    /** 业务申请编号 */
    businessCode?: string;
    /** 微信申请单号 */
    applymentId?: string;
    /** 状态 DRAFT/PENDING_REVIEW/SUBMITTED/CHECKING/AUDITING/ACCOUNT_NEED_VERIFY/NEED_SIGN/FINISH/REJECTED */
    status?: string;
    statusDesc?: string;
    /** 驳回原因 */
    rejectReason?: string;
    /** 审核时间 */
    auditTime?: string;
    /** 原因标题：已驳回为"驳回原因"，其它状态（如草稿因渠道异常退回）为"失败原因" */
    reasonLabel?: string;
    /** 子商户号（FINISH 时返回） */
    subMchId?: string;
    /** 签约链接（NEED_SIGN 时返回） */
    signUrl?: string;
    merchantName?: string;
    contactName?: string;
    contactPhone?: string;
    createTime?: string;
    modifyTime?: string;
    /** 主体类型 / 营业执照 / 法人等字段 */
    organizationType?: string;
    businessLicenseNumber?: string;
    legalPerson?: string;
    licensePeriodBegin?: string;
    licensePeriodEnd?: string;
    idCardName?: string;
    idCardNumber?: string;
    cardPeriodBegin?: string;
    cardPeriodEnd?: string;
    merchantShortName?: string;
    servicePhone?: string;
    /** 店铺 Url */
    storeUrl?: string;
    settlementId?: string;
    qualificationType?: string;
    bankAccountType?: string;
    bankAccountName?: string;
    bankAccountNumber?: string;
    bankName?: string;
    bankBranchName?: string;
    contactType?: string;
    contactIdNumber?: string;
    contactEmail?: string;
    /** 营业执照照片 */
    licenseCopy?: string;
    /** 身份证人像面 */
    idCardCopy?: string;
    /** 身份证国徽面 */
    idCardNational?: string;
    /** 提交快照（后端可能返回提交时的完整 JSON） */
    requestSnapshot?: Record<string, any> | string;
}

/** 进件列表查询参数 */
export interface MerchantApplicationListParams {
    page: number;
    size: number;
    sortField?: string;
    sortOrder?: string;
    shopId?: number | string;
    /** 申请主体类型：SHOP / VENDOR */
    applicantType?: string;
    status?: string;
    /** 状态集合(逗号分隔)，用于按列表场景限定可见状态范围 */
    statusList?: string;
}

/** 进件列表结果 */
export interface MerchantApplicationListResult {
    records: MerchantApplicationItem[];
    total: number;
}

/** 进件提交表单 */
export interface MerchantApplicationSubmitForm {
    shopId?: number | string;
    /** 支付渠道：WECHAT 微信 / HUIFU 汇付 */
    payChannel?: string;
    /** 主体类型：企业 "2" / 个体工商户 "4" / 个人 "2401" */
    organizationType?: string;
    merchantName?: string;
    merchantShortName?: string;
    businessLicenseNumber?: string;
    legalPerson?: string;
    licensePeriodBegin?: string;
    licensePeriodEnd?: string;
    idCardName?: string;
    idCardNumber?: string;
    cardPeriodBegin?: string;
    cardPeriodEnd?: string;
    servicePhone?: string;
    /** 店铺 Url */
    storeUrl?: string;
    settlementId?: string;
    qualificationType?: string;
    /** 银行账户类型："74" 对公 / "75" 对私 */
    bankAccountType?: string;
    bankAccountName?: string;
    bankAccountNumber?: string;
    bankName?: string;
    bankBranchName?: string;
    /** 营业执照照片 */
    licenseCopy?: string;
    /** 身份证人像面 */
    idCardCopy?: string;
    /** 身份证国徽面 */
    idCardNational?: string;
    /** 超管类型："65" 经营者/法人 */
    contactType?: string;
    contactName?: string;
    contactIdNumber?: string;
    contactPhone?: string;
    contactEmail?: string;
}


