// 列表查询时筛选参数类型
import type { UserRankFilterState } from "@/types/user/userRank";

export interface UserFilterParams {
    /** 用户类型：0 正式会员，1 游客 */
    userType?: 0 | 1;
    page?: number;
    size?: number;
    sortField?: string;
    sortOrder?: string;
    fromTag?: number | string;
    mark?: number | string;
    balance?: number | string;
    pointsGt?: number | string;
    pointsLt?: number | string;
    rankId?: number;
    keyword?: string;
    userId?: number;
    username?: string;
    isExact?: boolean;
    tagIds?: number[];
    tagMatchMode?: number;
}

export interface UserFilterResult {
    records: UserFilterState[];
    filter: UserFilterParams;
    total: number;
}

// 获取列表返回参数类型
export interface UserFilterState {
    userId: number;
    username: string;
    fromTag: number;
    fromTagName: string;
    rankId: number;
    rankIco: string;
    rankName: string;
    mobileValidated: number;
    emailValidated: number;
    mobile: string;
    email: string;
    balance: number;
    frozenBalance: number;
    points: number;
    orderCount: number;
    orderAmount: number;
    regTime: string;
    adminNote?: string;
    mark?: number;
}

// 获取详情返回参数类型
export interface UserFormState {
    userId?: number;
    username?: string;
    nickname?: string;
    fromTag?: number;
    fromTagName?: string;
    rankId?: number;
    rankIco?: string;
    rankName?: string;
    birthday?: string;
    growthPoints?: number;
    mobileValidated?: number;
    emailValidated?: number;
    mobile?: string;
    email?: string;
    balance: number;
    frozenBalance: number;
    points?: number;
    orderCount?: number;
    orderAmount?: number;
    regTime?: string;
    password?: string;
    pwdConfirm?: string;
    avatar: string;
    userAddress?: UserAddress;
    adminNote?: string;
    mark?: number;
    memberTags?: UserMemberTagInfo[];
}

export interface UserMemberTagInfo {
    tagId: number;
    tagName: string;
    groupId: number;
    groupName: string;
    createTime: string;
}

export interface UserAddress {
    address: string;
    addressId: number;
    consignee: string;
    email: string;
    isDefault: number;
    isSelected: number;
    mobile: string;
    regionName: string;
    regionNames: string[];
    telephone: string;
    userId: number;
    regionIds: number[];
}

// 编辑表单
export interface UserFundManagementFormState {
    id?: number;
    changeDesc?: string;
    typeBalance?: number;
    balance?: number;
    typeFrozenBalance?: number;
    frozenBalance?: number;
    typePoints?: number;
    points?: number;
    typeGrowthPoints?: number;
    growthPoints?: number;
}

export interface UserFundFilterResult {
    records: UserFundList[];
    filter: UserFilterParams;
    total: number;
}

export interface UserFundList {
    changeTypeName: string;
    beforeBalance: string;
    beforeFrozenBalance: string;
    logId: number;
    userId: number;
    balance: string;
    frozenBalance: string;
    changeTime: string;
    changeDesc: string;
    changeType: number;
    username: string;
    afterBalance: string;
    afterFrozenBalance: string;
}
