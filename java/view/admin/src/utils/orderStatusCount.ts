import type { OrderStatusCount } from "@/types/order/order.d";

type OrderStatusTabLike = {
    value: number;
    count?: number;
};

/**
 * 订单状态Tab值 → 后端统计字段
 */
const STATUS_COUNT_FIELD: Record<number, keyof OrderStatusCount> = {
    "-1": "allCount",
    0: "pendingCount",
    1: "confirmedCount",
    2: "processingCount",
    3: "cancelledCount",
    4: "invalidCount",
    5: "completedCount",
    "-2": "deletedCount"
};

/**
 * 构造订单状态数量统计入参
 * @description 统计口径必须与列表筛选一致，但要剔除 orderStatus，否则角标会跟着当前选中Tab变化；
 * 分页与排序参数不影响统计结果，一并剔除
 * @param filterParams - 列表页的筛选参数
 * @returns 统计接口入参
 */
export const buildOrderStatusCountParams = (filterParams: Record<string, any>) => {
    const { orderStatus, page, size, sortField, sortOrder, ...countParams } = filterParams;
    return countParams;
};

/**
 * 把统计结果回填到显式声明 count 的订单状态 Tab
 * @description 未声明 count 的 Tab 不展示角标；未命中映射或后端未返回字段时置 0
 * @param tabs - 订单状态Tab列表
 * @param count - 统计接口返回值，传 null 表示获取失败，全部归零
 */
export const applyOrderStatusCount = (tabs: OrderStatusTabLike[], count?: OrderStatusCount | null) => {
    tabs.forEach((tab) => {
        if (!Object.prototype.hasOwnProperty.call(tab, "count")) return;
        const field = STATUS_COUNT_FIELD[tab.value];
        tab.count = field && count ? Number(count[field]) || 0 : 0;
    });
};
