import type { SendUserTarget } from "@/types/user/memberAudience";

// 发送范围：0-全部会员，1-部分会员
const SCOPE_ALL = 0;
const SCOPE_PART = 1;
// 旧 MemberAudience.audienceType：0-全部，1-按等级，2-按标签
const AUDIENCE_RANK = 1;
const AUDIENCE_TAG = 2;

/** 生成空的发送/参与人群结构（默认全部会员） */
export const emptySendUserTarget = (): SendUserTarget => ({
    scope: SCOPE_ALL,
    userIds: [],
    users: [],
    rankIds: [],
    tagIds: [],
    tagMatchMode: 0
});

/**
 * 归一化人群结构
 * @description 兼容三种入参：新版 SendUserTarget、旧版 MemberAudience（audienceType 互斥模型）、空值。
 *              旧版数据用于历史记录回显：全部→scope0；按等级/按标签→scope1 + 对应集合。
 */
export const normalizeSendUserTarget = (raw: any): SendUserTarget => {
    if (!raw || typeof raw !== "object") {
        return emptySendUserTarget();
    }

    // 新版结构：带 scope 字段
    if ("scope" in raw) {
        return {
            scope: Number(raw.scope) === SCOPE_PART ? SCOPE_PART : SCOPE_ALL,
            userIds: Array.isArray(raw.userIds) ? raw.userIds.map(Number) : [],
            users: Array.isArray(raw.users) ? raw.users : [],
            rankIds: Array.isArray(raw.rankIds) ? raw.rankIds.map(Number) : [],
            tagIds: Array.isArray(raw.tagIds) ? raw.tagIds.map(Number) : [],
            tagMatchMode: Number(raw.tagMatchMode) === 1 ? 1 : 0
        };
    }

    // 旧版结构：MemberAudience（audienceType 互斥）
    if ("audienceType" in raw) {
        const audienceType = Number(raw.audienceType);
        const target = emptySendUserTarget();
        if (audienceType === AUDIENCE_RANK) {
            target.scope = SCOPE_PART;
            target.rankIds = Array.isArray(raw.rankIds) ? raw.rankIds.map(Number) : [];
        } else if (audienceType === AUDIENCE_TAG) {
            target.scope = SCOPE_PART;
            target.tagIds = Array.isArray(raw.tagIds) ? raw.tagIds.map(Number) : [];
            target.tagMatchMode = Number(raw.tagMatchMode) === 1 ? 1 : 0;
        }
        return target;
    }

    return emptySendUserTarget();
};
