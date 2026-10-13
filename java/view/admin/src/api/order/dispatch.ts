// 订单派单 API
import request from '@/utils/request';
import type {
    AvailableStaffParams,
    AvailableStaffResult,
    DispatchAssignParams,
    DispatchAssignResult
} from '@/types/order/dispatch';

/**
 * 获取可派单人员列表
 */
export const getAvailableStaff = (params: AvailableStaffParams) => {
    return request<AvailableStaffResult>({
        url: 'order/dispatch/availableStaff',
        method: 'get',
        params
    });
};

/**
 * 派单操作
 */
export const dispatchAssign = (data: DispatchAssignParams) => {
    return request<DispatchAssignResult>({
        url: 'order/dispatch/assign',
        method: 'post',
        data
    });
};

