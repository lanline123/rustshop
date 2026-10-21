/**
 * 会员/会员等级表格选择器的数据结构
 * @description 字段做了兼容，既适配 user/user/list（UserDTO），也适配 promotion/coupon/issue/users（CouponIssueUserVO）
 */
export interface MemberPickerUser {
    userId: number;
    /** 会员名称（登录名），降级映射到按会员名发送时使用 */
    username?: string;
    /** 会员展示名 */
    memberName?: string;
    nickname?: string;
    mobile?: string;
    email?: string;
    avatar?: string;
    /** 优惠券发放接口字段 */
    isCertified?: number;
    /** 会员列表接口字段 */
    mobileValidated?: number;
    status?: number;
    statusName?: string;
}

export interface MemberPickerRank {
    rankId?: number;
    rankName?: string;
    rankLevel?: number;
    rankLogo?: string;
}

/** 已选集合，key 为 userId / rankId */
export type MemberPickerUserMap = Record<number, MemberPickerUser>;
export type MemberPickerRankMap = Record<number, MemberPickerRank>;
