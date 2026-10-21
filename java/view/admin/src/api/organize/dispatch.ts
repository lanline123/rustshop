/**
 * 派单相关 API
 */
import request from "@/utils/request";
import type {
    DispatchParams,
    DispatchResponse,
    AvailableStaffParams,
    AvailableStaffResponse,
    CheckDispatchableResponse
} from "@/types/organize/dispatch";
import type { StaffType } from "@/types/organize/staff";

/**
 * 获取可派单人员列表
 */
export function getAvailableStaff(params: AvailableStaffParams) {
    return request<AvailableStaffResponse>({
        url: "o2o/dispatch/availableStaff",
        method: "get",
        params
    });
}

/**
 * 执行派单操作
 */
export function dispatchOrder(data: DispatchParams) {
    return request<DispatchResponse>({
        url: "o2o/dispatch/assign",
        method: "post",
        data
    });
}

/**
 * 检查订单是否可派单
 */
export function checkDispatchable(orderId: number) {
    return request<CheckDispatchableResponse>({
        url: "o2o/dispatch/check",
        method: "get",
        params: { orderId }
    });
}

