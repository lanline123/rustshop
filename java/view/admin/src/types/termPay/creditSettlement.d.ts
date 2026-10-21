/**
 * @file 账期结算管理类型定义
 * @description 对应接口文档：docs/账期支付/03_接口文档.md（4.1~4.3）
 */

import type { CreditOrderRecord } from "@/types/termPay/creditOrder.d";

/** 4.1 结算会员列表 - Query */
export interface CreditSettlementUserListQuery {
    /** 搜索关键字（用户名/手机号） */
    keyword?: string;
    page?: number;
    size?: number;
}

/** 结算会员记录 */
export interface CreditSettlementUserRecord {
    creditUserId: number;
    userId: number;
    username: string;
    mobile: string;
    /** 会员类型：1-个人，2-企业 */
    type: 1 | 2;
    typeText: string;
    /** 联系人名称 */
    contactName: string;
    /** 企业名称 */
    companyName: string;
    /** 信用额度 */
    creditLimit: number;
    /** 已使用额度 */
    usedAmount: number;
    /** 可用额度 */
    availableAmount: number;
    /** 待结算订单数 */
    pendingOrderCount: number;
    /** 待结算金额 */
    pendingAmount: number;
}

/** 4.1 结算会员列表 - 响应 data */
export interface CreditSettlementUserListPage {
    records: CreditSettlementUserRecord[];
    total: number;
    size: number;
    current: number;
    pages: number;
}

/** 4.2 待结算订单列表 - Query */
export interface CreditSettlementPendingOrdersQuery {
    userId: number;
    page?: number;
    size?: number;
    /** 订单编号（或关键字） */
    orderSn?: string;
    /** 下单开始时间（YYYY-MM-DD） */
    orderTimeStart?: string;
    /** 下单结束时间（YYYY-MM-DD） */
    orderTimeEnd?: string;
}

/** 4.2 待结算订单列表 - 响应 data（同 3.2） */
export type CreditSettlementPendingOrderRecord = CreditOrderRecord;

/** 4.2 待结算订单列表 - 响应 data（分页） */
export interface CreditSettlementPendingOrdersPage {
    records: CreditSettlementPendingOrderRecord[];
    total: number;
    size: number;
    current: number;
    pages: number;
}

/** 4.3 确认结算 - Body */
export interface CreditSettlementSubmitBody {
    userId: number;
    /** 回款流水单号（唯一） */
    settlementSn: string;
    /** 结算时间（YYYY-MM-DD） */
    settlementTime: string;
    /** 备注 */
    remark?: string;
    /** 要结算的订单ID列表 */
    orderIds: number[];
}

/** 4.4 历史结算列表 - Query */
export interface CreditSettlementHistoryQuery {
    page?: number;
    size?: number;
    /** 会员ID */
    userId?: number;
    /** 回款流水单号（模糊查询） */
    settlementSn?: string;
    /** 结算状态：1-已结算，2-已撤销 */
    settlementStatus?: 1 | 2;
    /** 结算开始时间（YYYY-MM-DD） */
    settlementTimeStart?: string;
    /** 结算结束时间（YYYY-MM-DD） */
    settlementTimeEnd?: string;
}

/** 4.4 历史结算记录 */
export interface CreditSettlementHistoryRecord {
    settlementId: number;
    settlementSn: string;
    userId: number;
    username: string;
    settlementAmount: number;
    orderCount: number;
    remark: string;
    settlementTime: string;
    settlementStatus: 1 | 2;
    settlementStatusText: string;
    cancelTime: string | null;
    cancelReason: string | null;
    operatorName: string;
    createTime: string;
}

/** 4.4 历史结算列表 - 响应 data */
export interface CreditSettlementHistoryPage {
    records: CreditSettlementHistoryRecord[];
    total: number;
    size: number;
    current: number;
    pages: number;
}

/** 4.6 历史结算订单明细 - Query */
export interface CreditSettlementHistoryOrdersQuery {
    /** 结算记录ID */
    settlementId: number;
    page?: number;
    size?: number;
    /** 订单号（模糊查询） */
    orderSn?: string;
    /** 下单开始时间（YYYY-MM-DD） */
    orderTimeStart?: string;
    /** 下单结束时间（YYYY-MM-DD） */
    orderTimeEnd?: string;
    /** 排序字段 */
    sortField?: string;
    /** 排序方向：asc/desc */
    sortOrder?: string;
}

/** 4.6 历史结算订单明细 - 响应记录 */
export type CreditSettlementHistoryOrderRecord = CreditOrderRecord;

/** 4.6 历史结算订单明细 - 响应 data（分页） */
export interface CreditSettlementHistoryOrdersPage {
    records: CreditSettlementHistoryOrderRecord[];
    total: number;
    size: number;
    current: number;
    pages: number;
}

/** 4.5 撤销结算 - Body */
export interface CreditSettlementCancelBody {
    settlementId: number;
    cancelReason?: string;
}
