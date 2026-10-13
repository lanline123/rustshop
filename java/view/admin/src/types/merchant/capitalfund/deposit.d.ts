/**
 * @file 店铺/商家后台 保证金类型
 * @description 字段对齐后端接口文档（deposit/shop/* 或 deposit/vendor/*、deposit/recharge/apply|vendorApply、deposit/refund/apply|vendorApply）
 */

/** 保证金总览 DepositShopOverviewVO（deposit/shop/overview 或 deposit/vendor/overview） */
export interface ShopDepositOverview {
    required: number; // 应缴保证金
    paid: number; // 已缴保证金（保证金总余额）
    pending: number; // 待补缴 = max(0, 应缴-已缴)
    withdrawable: number; // 可提现 = max(0, 已缴-应缴)（可用余额/可退额）
    pendingWithdraw?: number; // 待审核提现金额
    frozen: number; // 冻结金额 = min(已缴, 应缴)
    status?: number; // 账户状态
    penaltyStatus?: number; // 扣罚状态（0 正常）
    statusText?: string; // 派生状态文案：正常/待补缴/已处罚
    supplementDueTime?: number; // 补缴到期时间戳（秒，0 未触发）
    supplementDueTimeText?: string; // 补缴到期时间（格式化）
    hasProtocol?: boolean; // 是否存在保证金协议
}

/** 保证金预警横幅 DepositShopWarningVO（deposit/shop/warning 或 deposit/vendor/warning） */
export interface ShopDepositWarning {
    needWarning: boolean; // 是否需要预警（待补缴 > 0）
    pendingAmount: number; // 待补缴金额
    supplementDueTime?: number; // 补缴截止时间戳（秒，0 未触发）
    supplementDueTimeText?: string; // 补缴截止时间（格式化）
    message?: string; // 预警文案
}

/** 保证金协议 DepositShopProtocolVO（deposit/shop/protocol 或 deposit/vendor/protocol） */
export interface ShopDepositProtocol {
    enable: boolean; // 协议是否开启
    content: string; // 协议富文本内容
}

/** 保证金流水筛选（deposit/shop/logList 或 deposit/vendor/logList） */
export interface ShopDepositLogFilterParams {
    page: number;
    size: number;
    keyword?: string; // 业务编号/订单编号
    type?: number | string; // 交易类型筛选（DepositLogType）
    sortField?: string;
    sortOrder?: string;
    startTime?: number; // 时间筛选-开始（秒级时间戳）
    endTime?: number; // 时间筛选-结束（秒级时间戳）
}

/** 保证金流水 DepositShopLogVO */
export interface ShopDepositLogState {
    shopDepositLogId: number; // 流水ID
    type: number; // 交易类型
    typeText?: string; // 交易类型文案
    changeAmount: number; // 变动金额（带符号）
    beforePaid?: number; // 变动前实缴
    afterPaid?: number; // 变动后实缴（现金总余额）
    orderId?: number; // 关联订单ID
    relatedId?: number; // 关联单ID（充值/退还单等）
    remark?: string; // 备注
    addTime?: number; // 发生时间戳（秒）
    addTimeText?: string; // 发生时间（格式化）
    sourceAccount?: string; // 来源账户
    destAccount?: string; // 去向账户
}

export interface ShopDepositLogResult {
    records: ShopDepositLogState[];
    total: number;
}

/** 充值申请 DepositRechargeApplyDTO */
export interface ShopDepositRechargeParams {
    amount?: number; // 充值金额
    payVoucher?: string; // 转账/支付凭证
    payeeAccountId: number; // 平台收款账户id
    remark?: string; // 备注
}

/** 充值可选收款账户查询参数（deposit/recharge/payeeAccountOptions） */
export interface PayeeAccountOptionParams {
    accountType?: number; // 收款方式 1 银行卡 / 2 支付宝 / 3 微信
}

/** 充值可选收款账户 DepositPayeeAccountOptionVO */
export interface PayeeAccountOption {
    id: number;
    accountType: number;
    accountTypeText?: string;
    accountName?: string;
    accountNo?: string;
    bankName?: string;
    bankBranch?: string;
    status?: number;
    sortOrder?: number;
}

/** 全额退还预检 DepositFullRefundCheckVO */
export interface DepositFullRefundCheck {
    canRefund: boolean; // 是否可全额退还
    refundableAmount: number; // 可全额退还金额（不可退时为 0）
    afterSalesLimitDays: number; // 末单确认收货满 N 天（文案展示用）
    unmetReasons?: string[]; // 不满足全额退还的原因
}

/** 退还/提现申请 DepositRefundApplyDTO */
export interface ShopDepositRefundParams {
    type: number; // 退还类型 1 提现 / 2 全额退还
    amount?: number; // 退还金额
    payeeInfo?: string; // 收款信息
    reason?: string; // 退还原因
}

/** 冻结明细筛选 */
export interface ShopDepositFrozenFilterParams {
    page: number;
    size: number;
    keyword?: string; // 查询参数
    type?: number | string; // 资金类型/交易类型筛选（DepositLogType，可空）
    sortField?: string; // 排序字段
    sortOrder?: string; // 显示顺序
    tradeNo?: string; // 业务编号（按订单号 order_id 或关联单号 related_id 精确匹配）
    startTime?: number; // 时间起（秒级时间戳，按 add_time 过滤）
    endTime?: number; // 时间止（秒级时间戳）
}

/** 冻结明细 DepositShopFrozenDetailVO */
export interface ShopDepositFrozenState {
    shopDepositLogId: number; // 流水ID
    type: number; // 资金类型 DepositLogType：1充值 2提现 3退还 4平台扣除 5平台调整
    typeText?: string; // 资金类型文案
    deductAmount: number; // 扣款金额（正数，展示用；非扣款类为变动金额绝对值）
    changeAmount?: number; // 原始变动金额（带符号，通常为负）
    reason?: string; // 原因/备注（流水备注）
    orderId?: number; // 关联订单ID
    relatedId?: number; // 关联单ID（充值/退还单等，业务编号展示用）
    addTime?: number; // 扣款时间戳（秒）
    addTimeText?: string; // 扣款时间（格式化）
}

export interface ShopDepositFrozenResult {
    records: ShopDepositFrozenState[];
    total: number;
}

/**
 * 款项审核列表筛选
 * deposit/recharge/shopPaymentAuditList（店铺端）、deposit/recharge/vendorPaymentAuditList（供应商端）
 */
export interface ShopDepositAuditFilterParams {
    page: number;
    size: number;
    auditType?: number | string; // 审核类型 1 充值 / 2 退还；不传 = 全部混合
    status?: number | string; // 审核状态 0 待审核 / 1 已通过 / 2 未通过；不传 = 全部
    startTime?: number; // 提交开始时间（秒级时间戳，按提交时间过滤）
    endTime?: number; // 提交结束时间（秒级时间戳）
}

/** 款项审核列表项 / 详情 DepositPaymentAuditVO（充值 + 退还混合，字段统一） */
export interface ShopDepositAuditState {
    auditType: number; // 审核类型 1 充值 / 2 退还
    auditTypeText?: string; // 审核类型文案：保证金充值 / 保证金退还
    bizId: number; // 业务单id（充值=充值单id，退还=退还单id；查详情用）
    amount: number; // 交易金额
    status: number; // 审核状态 0 待审核 / 1 已通过 / 2 未通过（充值/退还统一同码）
    statusText?: string; // 审核状态文案：待审核 / 已通过 / 未通过
    refundType?: number; // 退还类型 1 部分退还 / 2 全部退还（仅退还行有值）
    refundTypeText?: string; // 退还类型文案（仅退还行）
    remark?: string; // 备注（充值=申请备注，退还=退还原因）
    payVoucher?: string; // 关联凭证图片url（充值=转账凭证，退还=打款凭证；多张逗号分隔）
    auditRemark?: string; // 审核备注 / 驳回原因
    auditTime?: number; // 审核时间戳（秒）
    auditTimeText?: string; // 审核时间（格式化）
    addTime?: number; // 提交时间戳（秒）
    addTimeText?: string; // 提交时间（格式化）
}

export interface ShopDepositAuditResult {
    records: ShopDepositAuditState[];
    total: number;
}

/**
 * 款项审核详情入参
 * deposit/recharge/shopPaymentAuditDetail（店铺端）、deposit/recharge/vendorPaymentAuditDetail（供应商端）
 */
export interface ShopDepositAuditDetailParams {
    auditType: number; // 审核类型 1 充值 / 2 退还（取列表行 auditType）
    bizId: number; // 业务单id（取列表行 bizId）
}
