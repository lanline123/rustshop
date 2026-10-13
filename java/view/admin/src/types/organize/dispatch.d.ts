/**
 * 派单相关类型定义
 */
import type { StaffType } from "@/types/organize/staff";

/** 派单请求参数 */
export interface DispatchParams {
    /** 订单ID */
    orderId: number;
    /** 人员ID（配送员或服务员） */
    staffId: number;
    /** 人员类型 */
    staffType: StaffType;
}

/** 派单响应 */
export interface DispatchResponse {
    /** 是否成功 */
    success: boolean;
    /** 消息 */
    message: string;
    /** 订单状态 */
    orderStatus?: number;
}

/** 可用人员列表参数 */
export interface AvailableStaffParams {
    /** 门店ID */
    shopId: number;
    /** 人员类型 */
    staffType: StaffType;
    /** 关键词（姓名/手机号） */
    keyword?: string;
    /** 订单ID（用于计算距离） */
    orderId?: number;
}

/** 可用人员项（含今日统计） */
export interface AvailableStaffItem {
    /** 人员ID */
    id: number;
    /** 用户ID */
    userId: number;
    /** 姓名 */
    name: string;
    /** 手机号 */
    phone: string;
    /** 头像 */
    avatar: string;
    /** 门店ID */
    shopId: number;
    /** 门店名称 */
    shopName: string;
    /** 账号状态：1-启用，0-禁用 */
    status: number;
    /** 服务状态：0-空闲，1-配送中/服务中，2-下班 */
    serviceStatus: number;
    /** 今日已完成数 */
    todayCount: number;
    /** 距离订单地址距离（米） */
    distance?: number;
}

/** 可用人员列表响应 */
export interface AvailableStaffResponse {
    /** 人员列表 */
    records: AvailableStaffItem[];
    /** 总数 */
    total: number;
}

/** 检查订单是否可派单响应 */
export interface CheckDispatchableResponse {
    /** 是否可派单 */
    dispatchable: boolean;
    /** 人员类型 */
    staffType: StaffType | null;
    /** 发单模式：1-抢单，2-派单 */
    dispatchMode: number | null;
    /** 消息 */
    message?: string;
}

