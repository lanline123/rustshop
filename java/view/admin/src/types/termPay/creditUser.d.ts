/**
 * @file 授信会员（授信客户及额度）类型定义
 * @description 对应接口文档：docs/账期支付/03_接口文档(1).md（2.1~2.5）
 */

/** 2.1 授信会员列表 - Query */
export interface CreditUserListQuery {
    page?: number;
    size?: number;
    /** 会员类型：1-个人，2-企业 */
    type?: 1 | 2;
    /** 会员用户名（模糊查询） */
    username?: string;
    /** 状态：0-禁用，1-启用 */
    status?: 0 | 1;
    sortField?: string;
    sortOrder?: "asc" | "desc";
}

/** 授信会员列表记录 */
export interface CreditUserRecord {
    creditUserId: number;
    userId: number;
    username: string;
    mobile: string;
    type: 1 | 2;
    typeText: string;
    creditLimit: number;
    usedAmount: number;
    availableAmount: number;
    settlementType: 1 | 2;
    settlementTypeText: string;
    contactName: string;
    companyName: string;
    status: 0 | 1;
    statusText: string;
    createTime: string;
}

/** 2.1 授信会员列表 - 响应 data */
export interface CreditUserListPage {
    records: CreditUserRecord[];
    total: number;
    size: number;
    current: number;
    pages: number;
}

/** 2.2 授信会员详情 - Query */
export interface CreditUserDetailQuery {
    creditUserId: number;
}

/** 2.2 授信会员详情 */
export interface CreditUserDetail extends CreditUserRecord {
    settlementDays: number;
    updateTime: string;
}

/** 2.3 添加授信会员 - Body */
export interface CreditUserCreateBody {
    userId: number;
    creditLimit: number;
    settlementType: 1 | 2;
    status: 0 | 1;
}

/** 2.4 修改授信会员 - Body */
export interface CreditUserUpdateBody {
    creditUserId: number;
    creditLimit?: number;
    settlementType?: 1 | 2;
    status?: 0 | 1;
}

/** 2.5 删除授信会员 - Body */
export interface CreditUserDeleteBody {
    /** 授信会员ID（creditUserId） */
    id: number;
}

/** 7.x 授信会员启用/禁用（字段更新）- Body */
export interface CreditUserUpdateFieldBody {
    /** 授信会员ID（credit_user_id） */
    id: number;
    /** 字段名：仅允许 status */
    field: "status";
    /** 字段值：0-禁用，1-启用（后端文档为 string，这里兼容 number） */
    val: 0 | 1 | "0" | "1";
}
