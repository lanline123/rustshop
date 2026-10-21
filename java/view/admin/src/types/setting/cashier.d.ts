
export interface PosFilterParams {
    page: number;
    size: number;
    sortField?: string;
    sortOrder?: string;
    status?: string | number;
    keyword?: string;
    addEndTime?: string;
    addStartTime?: string;
}

export interface PosFilterResult {
    records: PosFilterState[];
    filter: {
        page: number;
    };
    total: number;
}

export interface PosFilterState {
    id: number;
    adminId: number;
    userId: number;
    shopId: number;
    username?: string;
    email?: string;
    avatar?: string;
    addTime?: string;
    isUsing?: number;
    isAdmin?: number;
    roleId?: number;
    authList?: any[];
    user: {
        userId: number;
        username: string;
        avatar: string;
        mobile: string;
        nickname: string;
    };
    adminUser: {
        adminId: number;
        merchantId: number;
        username: string;
        avatar: string;
        mobile: string;
        nickname: string;
    };
    role: {
        roleId: number;
        shopId: number;
        roleName: string;
    }
    deviceCode?: string;
    deviceName?: string;
    status?: number;
    lastUsedTimeText?: string;
}

export interface PosFormState {
    adminId?: number;
    userId?: number;
    shopId?: number;
    username?: string;
    mobile?: string;
    email?: string;
    avatar?: string;
    addTime?: string;
    isUsing?: number;
    isAdmin?: number;
    roleId?: number;
    authList?: any[];
}
