/**
 * @file 店铺保证金 平台端类型与枚举
 * @description 覆盖保证金设置/规则/账户/查询/审核/明细全部页面的类型定义
 */

/** 保证金状态（店铺账户）depositStatus：1正常 2不足 3冻结 4未缴纳 */
export enum DepositAccountStatus {
    Normal = 1, // 正常（余额 ≥ 应缴）
    Insufficient = 2, // 不足（余额 < 应缴）
    Frozen = 3, // 冻结
    Unpaid = 4 // 未缴纳
}

/** 充值单状态 */
export enum RechargeAuditStatus {
    Pending = 0, // 待审核
    Passed = 1, // 已通过
    Rejected = 2 // 未通过
}

/** 退还单状态（一步式：0待审核 1已退还 2驳回） */
export enum RefundAuditStatus {
    Pending = 0, // 待审核
    Completed = 1, // 已退还（已打款/完成）
    Rejected = 2 // 审核驳回
}

/** 退还类型 */
export enum RefundType {
    Partial = 1, // 部分退还
    Full = 2 // 全额退还
}

/** 平台收款账户类型 */
export enum PayAccountType {
    Bank = 1, // 银行卡
    Alipay = 2, // 支付宝
    Wechat = 3 // 微信
}

/** 交易类型（明细 / 缴纳记录）DepositLogType：1缴纳(充值) 2提现 3退还 4扣除 5调整 */
export enum DepositTxType {
    Recharge = 1, // 缴纳（充值）
    Withdraw = 2, // 提现
    Refund = 3, // 退还
    Deduct = 4, // 扣除
    Adjust = 5 // 调整
}

/** 主体类型 */
export enum MerchantType {
    Personal = 1, // 个人
    Enterprise = 2 // 企业
}

/** 适用组织 */
export enum OrgScope {
    Shop = 1, // 店铺
    Supplier = 2 // 供应商
}

/** 收取规则（保证金设置） */
export enum CollectRule {
    HighestCategory = 1 // 按店铺类目保证金最高收取（当前唯一）
}

/** 收取节点 */
export enum CollectNode {
    OnEnter = 1, // 店铺上架商品时
    OnFirstOrder = 2 // 店铺售卖第一单
}

/** 缴存缓冲 / 补缴缓冲 - 超时处置 */
export enum BufferAction {
    LimitNewSpu = 1, // 限制上架新品
    OffAllSpu = 2 // 全店商品下架
}

/* ---------------- 5.1 保证金设置 ---------------- */

export interface DepositSettingState {
    /** 保证金功能开关 1开启 0关闭 */
    depositEnable: number;
    /** 收取规则（固定：按主营类目最高=1） */
    depositCollectRule: CollectRule;
    /** 收取节点 1上架时 2首单时 */
    depositCollectNode: CollectNode;
    /** 收取节点缓冲天数 */
    depositCollectBufferDays?: number;
    /** 收取缓冲到期处罚 */
    depositCollectBufferAction?: BufferAction;
    /** 补缴缓冲天数 */
    depositSupplementBufferDays?: number;
    /** 补缴缓冲到期处罚 */
    depositSupplementBufferAction?: BufferAction;
    /** 保证金协议开关 1开启 0关闭 */
    depositProtocolEnable: number;
    /** 保证金协议内容（富文本） */
    depositProtocolContent?: string;
}

/* ---------------- 5.2 保证金规则 ---------------- */

export interface DepositRuleFilterParams {
    page: number;
    size: number;
    sortField?: string;
    sortOrder?: string;
    ruleName?: string;
    status?: number | string;
    /** 生效时间筛选-开始（UI 日期，提交时转秒级时间戳 startTime） */
    startDate?: string;
    /** 生效时间筛选-结束（UI 日期，提交时转秒级时间戳 endTime） */
    endDate?: string;
}

export interface DepositRuleFilterState {
    shopDepositRuleId: number;
    ruleName: string;
    /** 适用主体类型 1个人 2企业，多选 */
    merchantTypes?: number[];
    merchantTypeText?: string;
    /** 适用店铺类型 1店铺 2供应商，多选 */
    shopTypes?: number[];
    shopTypeText?: string;
    depositAmount: number;
    /** 生效开始时间（秒级时间戳） */
    startTime?: number;
    /** 生效结束时间（秒级时间戳，0 表示长期） */
    endTime?: number;
    startTimeText?: string;
    endTimeText?: string;
    status: number;
    statusText?: string;
    categoryIds?: number[];
    categoryNames?: string[];
    addTime?: string;
}

export interface DepositRuleFilterResult {
    records: DepositRuleFilterState[];
    total: number;
}

export interface DepositRuleFormState {
    shopDepositRuleId?: number;
    ruleName: string;
    merchantTypes: number[];
    /** 适用店铺类型 1店铺 2供应商 */
    shopTypes: number[];
    categoryIds: number[];
    depositAmount?: number;
    effectiveMode: number; // 1 区间有效 / 2 长期有效（仅 UI 使用，不提交后端）
    startDate?: string;
    endDate?: string;
    status: number;
}

/* ---------------- 5.3 保证金账户（平台收款账户） ---------------- */

export interface AccountFilterParams {
    page?: number;
    size?: number;
    sortField?: string;
    sortOrder?: string;
    keyword?: string;
    accountType?: number | string;
    status?: number | string;
}

export interface AccountFilterResult {
    records: AccountFilterState[];
    total: number;
}

export interface AccountFilterState {
    /** 账户ID */
    id?: number;
    /** 账户类型 1银行卡 2支付宝 3微信 */
    accountType: number;
    /** 账户名称（户名） */
    accountName?: string;
    /** 账户号 */
    accountNo?: string;
    /** 银行名称 */
    bankName?: string;
    /** 银行支行 */
    bankBranch?: string;
    /** 状态 1启用 0禁用 */
    status?: number;
    /** 排序值，越小越靠前 */
    sortOrder?: number;
    /** 添加时间（秒级时间戳） */
    addTime?: number;
}

export interface AccountFormState {
    /** 账户ID（更新时必填） */
    id?: number;
    /** 账户类型 1银行卡 2支付宝 3微信 */
    accountType?: number;
    /** 账户名称（户名） */
    accountName?: string;
    /** 账户号 */
    accountNo?: string;
    /** 银行名称 */
    bankName?: string;
    /** 银行支行 */
    bankBranch?: string;
    /** 状态 1启用 0禁用 */
    status?: number;
    /** 排序值，越小越靠前 */
    sortOrder?: number;
}

export interface AccountTypeList {
    accountType: number;
    accountTypeText: string;
}

/* ---------------- 5.4 保证金查询 ---------------- */

/** 持有者维度参数（店铺/供应商）；shopId 为兼容字段 */
export interface DepositOwnerParams {
    /** 持有者类型 1店铺 2供应商 */
    ownerType?: number;
    /** 持有者id（店铺为shopId，供应商为vendorId） */
    ownerId?: number;
    /** 店铺id（兼容字段，可不传） */
    shopId?: number;
}

export interface DepositQueryFilterParams {
    page: number;
    size: number;
    sortField?: string;
    sortOrder?: string;
    /** 查询参数 */
    keyword?: string;
    /** 持有者类型 null全部 1店铺 2供应商 */
    ownerType?: number | string;
    /** 持有者名称（模糊，统一搜索） */
    ownerName?: string;
    /** 店铺名称（模糊） */
    shopTitle?: string;
    /** 店铺id（兼容字段） */
    shopId?: number | string;
    /** 持有者id（店铺为shopId，供应商为vendorId） */
    ownerId?: number | string;
    /** 主体类型 null全部 1个人 2企业；店铺按商户性质、供应商按供应商性质筛选 */
    subjectType?: number | string;
    /** 经营类目id */
    categoryId?: number | string;
    /** 保证金状态 1正常 2不足 3冻结 4未缴纳 */
    depositStatus?: number | string;
    /** 入驻时间起（UI 日期，提交时转秒级时间戳 joinTimeStart） */
    startDate?: string;
    /** 入驻时间止（UI 日期，提交时转秒级时间戳 joinTimeEnd） */
    endDate?: string;
}

export interface DepositQueryFilterState {
    /** 持有者类型 1店铺 2供应商 */
    ownerType: number;
    ownerTypeText?: string;
    /** 持有者id（店铺为shopId，供应商为vendorId） */
    ownerId: number;
    /** 持有者名称（店铺名或供应商名，统一展示） */
    ownerName?: string;
    /** 店铺id（兼容字段，仅店铺行填充） */
    shopId: number;
    /** 店铺名称（兼容字段，仅店铺行填充） */
    shopTitle?: string;
    /** 商户名称（兼容字段，仅店铺行填充） */
    merchantName?: string;
    /** 主体类型 1个人 2企业 */
    merchantType?: number;
    merchantTypeText?: string;
    /** 经营类目（在售商品类目名称，逗号分隔） */
    businessCategory?: string;
    /** 应缴金额 */
    required: number;
    /** 已交金额 */
    paid: number;
    /** 待补缴金额 */
    pending: number;
    /** 保证金状态 1正常 2不足 3冻结 4未缴纳 */
    depositStatus: number;
    depositStatusText?: string;
}

export interface DepositQueryFilterResult {
    records: DepositQueryFilterState[];
    total: number;
}

export interface DepositQueryDetail {
    /** 持有者类型 1店铺 2供应商 */
    ownerType?: number;
    ownerTypeText?: string;
    ownerId?: number;
    ownerName?: string;
    shopId: number;
    shopTitle?: string;
    /** 店铺类型 1店铺 2门店 3自提点 */
    shopType?: number;
    shopTypeText?: string;
    /** 经营类目（逗号分隔） */
    businessCategory?: string;
    /** 开店时间 */
    openTime?: string;
    /** 所属商户名称 */
    merchantName?: string;
    /** 主体类型 1个人 2企业 */
    merchantType?: number;
    merchantTypeText?: string;
    /** 主体类型文案（个人/企业） */
    subjectTypeText?: string;
    /** 保证金状态 1正常 2不足 3冻结 4未缴纳 */
    depositStatus?: number;
    depositStatusText?: string;
    /** 应缴金额 */
    required?: number;
    /** 已交金额 */
    paid?: number;
    /** 冻结金额 min(已交, 应缴) */
    frozen?: number;
    /** 可用金额 max(0, 已交 - 应缴) */
    available?: number;
    /** 待补缴金额 */
    pending?: number;
    /** 店铺状态文案 */
    shopStatusText?: string;
    /** 供应商状态文案 */
    vendorStatusText?: string;
}

export interface PaymentLogParams extends DepositOwnerParams {
    page: number;
    size: number;
}

/** 保证金交易明细行（DepositLogVO） */
export interface PaymentLogState {
    /** 流水id */
    shopDepositLogId: number;
    ownerType?: number;
    ownerTypeText?: string;
    ownerId?: number;
    ownerName?: string;
    shopId?: number;
    shopTitle?: string;
    merchantName?: string;
    /** 交易类型 DepositTxType */
    type?: number;
    typeText?: string;
    /** 变动金额（带符号） */
    changeAmount?: number;
    /** 变动前实缴 */
    beforePaid?: number;
    /** 变动后实缴 */
    afterPaid?: number;
    /** 关联单id */
    relatedId?: number;
    /** 关联订单id */
    orderId?: number;
    /** 操作管理员id */
    adminId?: number;
    remark?: string;
    /** 关联单备注（充值=申请备注 / 退还=退款原因，仅充值/退还类） */
    relatedRemark?: string;
    /** 凭证 */
    payVoucher?: string;
    /** 时间 */
    addTime?: string;
}

export interface PaymentLogResult {
    records: PaymentLogState[];
    total: number;
}

export interface AdjustLogParams extends DepositOwnerParams {
    page: number;
    size: number;
}

/** 保证金应缴调整记录行（DepositAdjustLogVO） */
export interface AdjustLogState {
    /** 调整记录id */
    shopDepositAdjustId: number;
    ownerType?: number;
    ownerTypeText?: string;
    ownerId?: number;
    ownerName?: string;
    shopId?: number;
    /** 调整前应缴 */
    beforeRequired?: number;
    /** 调整后应缴 */
    afterRequired?: number;
    /** 来源 1按类目最高 2手动固定 */
    source?: number;
    sourceText?: string;
    /** 操作管理员id */
    adminId?: number;
    /** 操作管理员名称 */
    adminName?: string;
    /** 操作管理员头像 */
    adminAvatar?: string;
    /** 调整原因 */
    reason?: string;
    /** 时间 */
    addTime?: string;
}

export interface AdjustLogResult {
    records: AdjustLogState[];
    total: number;
}

/** 手动调整应缴参数（DepositAdjustDTO） */
export interface AdjustDepositParams extends DepositOwnerParams {
    /** 调整方式 1按类目最高(清空手动值) 2手动固定 */
    adjustType: number;
    /** 手动固定应缴金额（adjustType=2 时必填） */
    requiredManual?: number;
    /** 调整原因 */
    reason: string;
}

/** 赔付扣除参数（DepositDeductDTO） */
export interface DeductDepositParams extends DepositOwnerParams {
    /** 扣除金额（正数） */
    amount: number;
    /** 扣除原因 */
    reason: string;
    /** 关联订单id（可选） */
    orderId?: number;
}

/** 发送保证金预警参数（DepositSendWarningDTO） */
export interface SendWarningParams extends DepositOwnerParams {
    /** 预警标题（不传则自动生成） */
    title?: string;
    /** 预警内容（不传则自动生成） */
    content?: string;
}

/* ---------------- 5.5 款项审核 ---------------- */

export interface RechargeAuditFilterParams {
    page: number;
    size: number;
    /** 查询参数（后端 keyword 通用） */
    keyword?: string;
    /** 持有者类型 1店铺 2供应商 */
    ownerType?: number | string;
    /** 持有者id */
    ownerId?: number;
    /** 店铺id（兼容字段） */
    shopId?: number;
    /** 状态 0待审核 1已入账 2驳回 */
    status?: number | string;
    /** 申请开始时间（UI 日期，提交时转秒级时间戳 startTime） */
    startDate?: string;
    /** 申请结束时间（UI 日期，提交时转秒级时间戳 endTime） */
    endDate?: string;
    /** 仅 UI 使用：后端充值列表不支持店铺名/商户名/支付方式筛选 */
    shopName?: string;
    merchantName?: string;
    /** 主体类型 1个人 2企业 */
    subjectType?: number | string;
    payType?: number | string;
}

/** 充值单 VO（DepositRechargeVO） */
export interface RechargeAuditFilterState {
    /** 充值单id */
    shopDepositRechargeId: number;
    ownerType?: number;
    ownerTypeText?: string;
    ownerId?: number;
    /** 持有者名称（店铺名/供应商名） */
    ownerName?: string;
    shopId?: number;
    /** 店铺名称（兼容字段，仅店铺维度回填） */
    shopName?: string;
    /** 商户名称（后端充值单 VO 暂未返回，UI 兼容占位） */
    merchantName?: string;
    /** 主体类型 1个人 2企业 */
    merchantType?: number;
    /** 主体类型文案 个人/企业 */
    subjectTypeText?: string;
    /** 支付方式文案（后端充值单 VO 暂未返回，UI 兼容占位） */
    payTypeText?: string;
    /** 充值金额 */
    amount: number;
    /** 支付凭证（逗号分隔） */
    payVoucher?: string;
    /** 充值时填写的备注（交易备注） */
    remark?: string;
    /** 收款账户id */
    payeeAccountId?: number;
    /** 收款账户名称（快照） */
    payeeAccountName?: string;
    payeeAccountNo?: string;
    payeeAccountType?: number;
    payeeAccountTypeText?: string;
    payeeBankName?: string;
    payeeBankAccount?: string;
    payeeAccountHolder?: string;
    payeeQrCode?: string;
    /** 状态 0待审核 1已入账 2驳回 */
    status: number;
    statusText?: string;
    auditAdminId?: number;
    auditTime?: number;
    auditTimeText?: string;
    /** 审核备注 */
    auditRemark?: string;
    /** 申请时间（秒级时间戳） */
    addTime?: number;
    /** 申请时间文本 */
    addTimeText?: string;
}

export interface RechargeAuditFilterResult {
    records: RechargeAuditFilterState[];
    total: number;
}

export type RechargeAuditDetail = RechargeAuditFilterState;

export interface RefundAuditFilterParams {
    page: number;
    size: number;
    keyword?: string;
    ownerType?: number | string;
    ownerId?: number;
    shopId?: number;
    /** 退还类型 1提现 2全额退还 */
    type?: number | string;
    /** 状态 0待审核 1已退还 2驳回 */
    status?: number | string;
    startDate?: string;
    endDate?: string;
    /** 仅 UI 使用：后端退还列表不支持店铺名/商户名筛选 */
    shopName?: string;
    merchantName?: string;
    /** 主体类型 1个人 2企业 */
    subjectType?: number | string;
    payeeAccountNo?: string;
}

/** 退还/提现单 VO（DepositRefundVO） */
export interface RefundAuditFilterState {
    /** 退还单id */
    shopDepositRefundId: number;
    ownerType?: number;
    ownerTypeText?: string;
    ownerId?: number;
    ownerName?: string;
    shopId?: number;
    shopName?: string;
    /** 商户名称（后端退还单 VO 暂未返回，UI 兼容占位） */
    merchantName?: string;
    /** 主体类型 1个人 2企业 */
    merchantType?: number;
    /** 主体类型文案 个人/企业 */
    subjectTypeText?: string;
    /** 退还类型 1提现 2全额退还 */
    type: number;
    typeText?: string;
    /** 退还金额 */
    amount: number;
    /** 收款信息 */
    payeeInfo?: string;
    /** 退还原因 */
    reason?: string;
    /** 状态 0待审核 1已退还 2驳回 */
    status: number;
    statusText?: string;
    /** 打款凭证 */
    payVoucher?: string;
    auditAdminId?: number;
    auditTime?: number;
    auditTimeText?: string;
    auditRemark?: string;
    /** 申请时间（秒级时间戳） */
    addTime?: number;
    addTimeText?: string;
}

export interface RefundAuditFilterResult {
    records: RefundAuditFilterState[];
    total: number;
}

export type RefundAuditDetail = RefundAuditFilterState;

/** 款项审核页（充值 + 退还双 tab）合并筛选参数 */
export interface PaymentAuditFilterParams extends RechargeAuditFilterParams, RefundAuditFilterParams {
    /** 组织名称（统一搜索，对应充值/退还列表 ownerName） */
    ownerName?: string;
    /** 支付方式（收款账户类型，仅充值 tab 使用） */
    payeeAccountType?: number | string;
}

/** 款项审核页（充值 + 退还双 tab）合并列表项 */
export type PaymentAuditFilterState = RechargeAuditFilterState | RefundAuditFilterState;

/** 充值审核参数（DepositRechargeAuditDTO） */
export interface AuditRechargeParams {
    id: number;
    /** 是否通过 true通过入账 false驳回 */
    pass: boolean;
    /** 审核备注/驳回原因 */
    auditRemark?: string;
}

/** 退还审核参数（DepositRefundAuditDTO）：一步式，通过时 payVoucher 必填 */
export interface AuditRefundParams {
    id: number;
    /** 是否通过 true通过并打款 false驳回 */
    pass: boolean;
    /** 打款凭证（通过时必填，逗号分隔） */
    payVoucher?: string;
    /** 审核备注/驳回原因 */
    auditRemark?: string;
}

/* ---------------- 5.6 交易明细 ---------------- */

export interface DepositTransactionFilterParams {
    page: number;
    size: number;
    /** 查询参数（店铺名称/交易单号 共用 keyword） */
    keyword?: string;
    /** 持有者名称（组织名称，模糊匹配） */
    ownerName?: string;
    /** 持有者类型 null全部 1店铺 2供应商 */
    ownerType?: number | string;
    /** 交易单号（按关联订单id/关联单id精确匹配） */
    tradeNo?: string;
    /** 商户名称（模糊） */
    merchantName?: string;
    /** 主体类型 1个人 2企业 */
    subjectType?: number | string;
    /** 交易类型 DepositLogType：1缴纳 2提现 3退还 4扣除 5调整 */
    type?: number | string;
    /** 交易时间起（UI 日期，提交时转秒级时间戳 startTime） */
    startDate?: string;
    /** 交易时间止（UI 日期，提交时转秒级时间戳 endTime） */
    endDate?: string;
}

/** 保证金交易明细行（DepositLogVO） */
export interface DepositTransactionFilterState {
    /** 流水id */
    shopDepositLogId: number;
    ownerType?: number;
    ownerTypeText?: string;
    ownerId?: number;
    /** 持有者名称（店铺名或供应商名） */
    ownerName?: string;
    shopId?: number;
    /** 店铺名称（兼容字段，仅店铺行填充） */
    shopTitle?: string;
    /** 商户名称（兼容字段，仅店铺行填充） */
    merchantName?: string;
    /** 主体类型 1个人 2企业 */
    merchantType?: number;
    /** 主体类型文案 个人/企业 */
    subjectTypeText?: string;
    /** 交易类型 DepositLogType */
    type: number;
    typeText?: string;
    /** 支付方式文案 */
    payTypeText?: string;
    /** 收款账户类型文案（列表/详情展示） */
    payeeAccountTypeText?: string;
    /** 变动金额（带符号） */
    changeAmount?: number;
    beforePaid?: number;
    afterPaid?: number;
    /** 关联单id（充值/退还单等） */
    relatedId?: number;
    /** 关联订单id（平台扣除/赔付时） */
    orderId?: number;
    adminId?: number;
    remark?: string;
    /** 关联单备注（充值/退还单备注） */
    relatedRemark?: string;
    /** 凭证（逗号分隔，W4 充值/退还入账后回填，当前为空占位） */
    payVoucher?: string;
    /** 时间 */
    addTime?: string;
}

export interface DepositTransactionFilterResult {
    records: DepositTransactionFilterState[];
    total: number;
}

export type DepositTransactionDetail = DepositTransactionFilterState;
