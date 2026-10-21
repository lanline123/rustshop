/**
 * @file 账期订单（账期支付订单）类型定义
 * @description 对应接口文档：docs/账期支付/03_接口文档.md
 */

/** 3.1 账期订单列表 - Query 参数 */
export interface CreditOrderListQuery {
    page?: number;
    size?: number;
    /** 订单号（模糊查询） */
    orderSn?: string;
    /** 会员ID */
    userId?: number;
    /** 会员用户名（模糊查询） */
    username?: string;
    /** 结算状态：0-未结算，1-已结算 */
    settlementStatus?: 0 | 1;
    /** 下单开始时间（YYYY-MM-DD） */
    orderTimeStart?: string;
    /** 下单结束时间（YYYY-MM-DD） */
    orderTimeEnd?: string;
}

/** 账期订单记录 */
export interface CreditOrderRecord {
    creditOrderId: number;
    orderId: number;
    orderSn: string;
    userId: number;
    username: string;
    mobile: string;
    orderAmount: number;
    creditAmount: number;
    orderStatus: number;
    orderStatusText: string;
    settlementStatus: 0 | 1;
    settlementStatusText: string;
    /** 回款流水单号（已结算时有值） */
    settlementSn: string | null;
    /** 下单时间 */
    orderTime: string;
    /** 结算时间（已结算时有值） */
    settlementTime: string | null;
}

/** 3.1 账期订单列表 - 响应 data */
export interface CreditOrderListPage {
    records: CreditOrderRecord[];
    total: number;
    size: number;
    current: number;
    pages: number;
}

