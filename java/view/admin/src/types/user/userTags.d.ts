// ==================== 标签组相关 ====================

export interface UserTagFilterParams {
    page: number;
    size: number;
    sortField: string;
    sortOrder: string;
    keyword?: string;
    groupName?: string;
    groupId?: number;
}

export interface TagItem {
    id?: number;
    groupId?: number;
    tagName?: string;
    createTime?: string;
}

export interface UserTagFilterState {
    id?: number;
    groupName?: string;
    tagValues?: string;
    tags?: TagItem[];
    isTop?: number;
    customerCount?: number;
    createTime?: string;
}

export interface UserTagFilterResult {
    records: UserTagFilterState[];
    total: number;
}

export interface UserTagDetailResult {
    id?: number;
    groupName?: string;
    createTime?: string;
    tags?: TagItem[];
}

export interface UserTagFormState {
    groupName?: string;
    tags?: TagItem[];
}

export interface MemberTagGroupCreateParams {
    groupName: string;
    tagNames: string[];
}

export interface MemberTagGroupUpdateTag {
    id?: number;
    tagName: string;
}

export interface MemberTagGroupUpdateParams {
    id: number;
    groupName: string;
    tags: MemberTagGroupUpdateTag[];
}

// ==================== 单标签管理相关 ====================

export interface MemberTagCreateParams {
    groupId: number;
    tagName: string;
}

export interface MemberTagUpdateParams {
    id: number;
    tagName: string;
}

// ==================== 用户标签关联相关 ====================

export interface UserMemberTagItem {
    tagId: number;
    tagName: string;
    groupId: number;
    groupName: string;
    createTime: string;
}

export interface BindTagsParams {
    userId: number;
    tagIds: number[];
}

export interface UnbindTagsParams {
    userId: number;
    tagIds: number[];
}

export interface BatchBindTagsParams {
    userIds: number[];
    tagIds: number[];
}

export interface BatchUnbindTagsParams {
    userIds: number[];
    tagIds: number[];
}
