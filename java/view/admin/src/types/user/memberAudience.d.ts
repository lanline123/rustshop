/**
 * 会员标签二期：人群圈选结构（与后端 MemberAudienceDTO 一致）
 */
export interface MemberAudience {
    /** 人群类型：0-全部会员，1-按会员等级，2-按会员标签 */
    audienceType: number;
    /** audienceType=1 时生效：会员等级ID列表 */
    rankIds?: number[];
    /** audienceType=2 时生效：会员标签ID列表 */
    tagIds?: number[];
    /** audienceType=2 时生效：0-满足任一标签，1-满足全部标签 */
    tagMatchMode?: number;
}

/** 人群圈选类型枚举 */
export const AudienceType = {
    ALL: 0,
    RANK: 1,
    TAG: 2
} as const;

/** 标签匹配模式枚举 */
export const TagMatchMode = {
    ANY: 0,
    ALL: 1
} as const;

/** 已选中的指定会员（携带名称用于回显与降级提交） */
export interface SendUserTargetUser {
    userId: number;
    username: string;
    mobile?: string;
}

/**
 * 发送会员范围（站内信 / 消息推送 / 营销活动人群等场景）
 * @description 指定会员、会员标签、会员等级三者互斥，一次只能按其中一种圈选，
 *              因此 userIds/users、tagIds、rankIds 中最多只有一组有值
 */
export interface SendUserTarget {
    /** 发送范围：0-全部会员，1-部分会员 */
    scope: number;
    /** 指定会员ID列表（按指定会员圈选时有值） */
    userIds: number[];
    /** 指定会员明细（按指定会员圈选时有值） */
    users: SendUserTargetUser[];
    /** 会员等级ID列表（按会员等级圈选时有值） */
    rankIds: number[];
    /** 会员标签ID列表（按会员标签圈选时有值） */
    tagIds: number[];
    /** 标签匹配模式：0-满足任一，1-满足全部（仅按会员标签圈选时生效） */
    tagMatchMode: number;
}

/** 发送范围枚举 */
export const SendUserScope = {
    ALL: 0,
    PART: 1
} as const;
