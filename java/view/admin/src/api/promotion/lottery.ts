import request from '@/utils/request';
import type { LotteryPrizeItemParam, GridPositionParam } from '@/types/promotion/luckyWheel.d';

// ==================== 后台管理接口 ====================
// 基础路径: /adminapi/promotion/lotteryActivity

/**
 * 2.1 抽奖活动列表
 * @param params { page, size, activityName, status }
 */
export function getLotteryActivityList(params: {
    page?: number;
    size?: number;
    activityName?: string;
    status?: number;
}) {
    return request({
        url: '/promotion/lotteryActivity/list',
        method: 'get',
        params,
    });
}

/**
 * 2.2 抽奖活动详情
 * @param activityId 活动ID
 */
export function getLotteryActivityDetail(activityId: number) {
    return request({
        url: '/promotion/lotteryActivity/detail',
        method: 'get',
        params: { activityId },
    });
}

/**
 * 2.3 创建抽奖活动
 */
export function createLotteryActivity(data: {
    activityName: string;
    startTime: string;
    endTime: string;
    costType?: number;
    costPoints: number;
    dailyLimit?: number;
    totalLimit?: number;
    winLimit?: number;
    decorateConfig?: {
        bgImage?: string;
        bgColor?: string;
        showWinnerList?: number;
        activityRules?: string;
    };
    prizeList: LotteryPrizeItemParam[];
    gridConfig: GridPositionParam[];
    noWinName: string;
    noWinImage: string;
    noWinTips?: string;
}) {
    return request({
        url: '/promotion/lotteryActivity/create',
        method: 'post',
        data,
    });
}

/**
 * 2.4 编辑抽奖活动
 */
export function updateLotteryActivity(data: {
    activityId: number;
    activityName: string;
    startTime: string;
    endTime: string;
    costType?: number;
    costPoints: number;
    dailyLimit?: number;
    totalLimit?: number;
    winLimit?: number;
    decorateConfig?: {
        bgImage?: string;
        bgColor?: string;
        showWinnerList?: number;
        activityRules?: string;
    };
    prizeList: LotteryPrizeItemParam[];
    gridConfig: GridPositionParam[];
    noWinName: string;
    noWinImage: string;
    noWinTips?: string;
}) {
    return request({
        url: '/promotion/lotteryActivity/update',
        method: 'post',
        data,
    });
}

/**
 * 2.5 删除抽奖活动
 * @param activityId 活动ID
 */
export function deleteLotteryActivity(activityId: number) {
    return request({
        url: '/promotion/lotteryActivity/delete',
        method: 'post',
        data: { activityId },
    });
}

/**
 * 2.6 结束抽奖活动
 * @param activityId 活动ID
 */
export function endLotteryActivity(activityId: number) {
    return request({
        url: '/promotion/lotteryActivity/end',
        method: 'post',
        data: { activityId },
    });
}

/**
 * 2.7 用户管理列表
 * GET /adminapi/promotion/lotteryActivity/userManage
 */
export function getLotteryActivityUserManage(params: {
    activityId: number;
    userName?: string;
    startTime?: string;
    endTime?: string;
    page?: number;
    size?: number;
}) {
    return request<any>({
        url: '/promotion/lotteryActivity/userManage',
        method: 'get',
        params
    }).then((res: any) => ({
        records: res?.records ?? [],
        total: res?.total ?? 0
    }));
}

/**
 * 2.8 用户中奖详情列表
 * GET /adminapi/promotion/lotteryActivity/userRecord
 */
export function getLotteryActivityUserRecord(params: {
    activityId: number;
    userId: number;
    page?: number;
    size?: number;
}) {
    return request<any>({
        url: '/promotion/lotteryActivity/userRecord',
        method: 'get',
        params
    }).then((res: any) => ({
        records: res?.records ?? [],
        total: res?.total ?? 0
    }));
}

