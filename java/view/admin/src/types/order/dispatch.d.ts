// 订单派单相关类型定义

/** 人员类型 */
export type StaffType = 'delivery' | 'service';

/** 可派单人员列表查询参数 */
export interface AvailableStaffParams {
    orderId: number;
    type: StaffType;
    shopId?: number;
}

/** 可派单人员项 */
export interface AvailableStaffItem {
    id: number;
    userId: number;
    name: string;
    phone: string;
    avatar: string;
    shopId: number;
    shopName: string;
    serviceStatus: number;
    distance?: number;
    completedCount: number;
    rating?: number;
}

/** 可派单人员列表返回结果 */
export interface AvailableStaffResult {
    records: AvailableStaffItem[];
}

/** 派单请求参数 */
export interface DispatchAssignParams {
    orderId: number;
    staffId: number;
    type: StaffType;
    remark?: string;
}

/** 派单返回结果 */
export interface DispatchAssignResult {
    code: number;
    message: string;
}

