import type { SendUserTarget } from "@/types/user/memberAudience";

// 列表查询时筛选参数类型
export interface CouponFilterParams {
    page: number;
    size: number;
    sortField: string;
    sortOrder: string;
    keyword?: string;
    isShow?: number;
    shopId?: number;
}

// 获取列表返回参数类型
export interface CouponFilterResult {
    records: CouponFilterState[];
    filter: {
        page: number;
    };
    total: number;
}
export interface CouponFilterState {
    couponId?: number;
    couponName?: string;
    couponType?: number;
    isNewUser?: number;
    limitUserRank?: number;
    couponMoney?: number;
    couponDiscount?: number;
    minOrderAmount?: string;
    reduceType?: number;
    isShow?: boolean;
    sortOrder?: number;
    sendType?: number;
    delayDay?: number;
    useDay?: number;
    useStartDate?: string;
    useEndDate?: string;
    /** 状态：1=进行中 2=待生效 3=已失效 */
    status?: number;
    /** 后端状态名称（列表展示用前端文案，不直接使用该字段） */
    statusName?: string;
}

// 获取详情返回参数类型
export interface CouponRankList {
    rankId?: number;
    rankName?: string;
    rankLevel?: number;
    rankLogo?: string;
    userCount?: number;
}
export interface CouponFormState {
    sendRange?: number;
    sendRangeData: any[];
    isGlobal?: number;
    isShow?: number;
    couponDiscount?: number;
    couponType?: number;
    isNewUser?: number;
    limitUserRank?: string[];
    sendType?: number;
    useStartDate?: string;
    useEndDate?: string;
    couponName?: string;
    couponDesc?: string;
    couponMoney?: number;
    minOrderAmount?: number;
    delayDay?: number;
    useDay?: number;
    sendNum?: number;
    couponUnit?: number;
    limitNum?: number;
    reduceType?: number;
    maxOrderAmount?: number;
    statusName?: string;
    couponId?: number;
    isUse?: boolean;
    useEndDateFormat?: string;
    userId?: number;
    id?: number;
    memberRange?: number;
    memberMarks?: number[];
    sendUserTarget?: SendUserTarget;
}

export interface CouponIssueUserParams {
    page: number;
    size: number;
    keyword?: string;
    fromTag?: number | string;
}

export interface CouponIssueUserState {
    userId: number;
    memberName?: string;
    username?: string;
    nickname?: string;
    mobile?: string;
    email?: string;
    fromTag?: number;
    fromTagName?: string;
    isCertified?: number;
    isCompanyAuth?: number;
    status?: number;
    statusName?: string;
}

export interface CouponIssueUserResult {
    records: CouponIssueUserState[];
    total: number;
    size: number;
    current: number;
}

/** 手动发放模式：指定会员 / 会员标签 / 会员等级 */
export type CouponIssueType = "USER" | "TAG" | "RANK";

export interface CouponManualIssueParam {
    couponId: number;
    issueType: CouponIssueType;
    userIds?: number[];
    rankIds?: number[];
    tagIds?: number[];
    /** 标签匹配：0=满足任一，1=满足全部 */
    tagMatchMode?: number;
}

export interface CouponIssueFailDetail {
    userId: number;
    memberName?: string;
    mobile?: string;
    reason?: string;
}

export interface CouponManualIssuePreviewVO {
    couponId: number;
    issueType: CouponIssueType;
    memberCount: number;
    couponCount: number;
    failCount: number;
    failDetails: CouponIssueFailDetail[];
}

export interface CouponManualIssueResultVO {
    couponId: number;
    issueType: CouponIssueType;
    successCount: number;
    couponCount: number;
    failCount: number;
    failDetails: CouponIssueFailDetail[];
}
