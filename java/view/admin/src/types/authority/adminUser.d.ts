// 列表查询时筛选参数类型


export interface AdminUserFilterParams {
    page: number;
    size: number;
    sortField?: string,
    sortOrder?: string,
    keyword?: string;
    adminType?: string;
    adminUserType?: string;
    suppliersId?: number | string;
}

// 获取列表返回参数类型

export interface AdminUserFilterResult {
    records: AdminUserFilterState[];
    filter: {
        page: number;
    };
    total: number;
    keyword?: string;
}
export interface AdminUserFilterState {
    adminId: number;
    id?: number;
    username?: string;
    mobile?: string;
    avatar?: string;
    password?: string;
    email?: string;
    addTime?: string;
    authList?: string[];
    suppliersId?: number;
    roleId?: number;
    shopId?: number;
    parentId?: number;
    menuTag?: string;
    orderExport?: string;
    extra?: string;
    isAdmin?: number;
}

export interface AdminUserFormState {
    adminId?: number;
    isAdmin?: number;
    userId: string;
    username?: string;
    mobile?: string;
    avatar?: string;
    defAvatar?: string;
    password?: string;
    pwdConfirm?: string;
    oldPassword?: string;
    email?: string;
    addTime?: string;
    authList?: string[];
    suppliersId?: number;
    roleId?: number;
    shopId?: number;
    parentId?: number;
    menuTag?: string[];
    orderExport?: string[];
    extra?: string[];
    roleList?: AdminUserRoleListItem[];
    user?: any;
    isUsing?: number;
    adminUser?: Admin_user;
    userShop?: any;
}
export interface Admin_user {
	adminId: number;
	mobile: string;
	avatar: string;
	merchantId: number;
}
export interface AdminUserRoleListItem {
    roleId: number;
    roleName: string;
}


