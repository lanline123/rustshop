// 客服人员管理 & 接入规则 相关类型

export interface KefuAdminUser {
    adminId?: number;
    mobile?: string;
    mobileAreaCode?: string;
    avatar?: string;
    merchantId?: number;
    username?: string;
}

export interface KefuRole {
    roleId?: number;
    roleName?: string;
    roleCode?: string;
    shopId?: number;
}

// 客服列表查询参数
export interface AdminUserKefuFilterParams {
    page: number;
    size: number;
    sortField?: string;
    sortOrder?: string;
    keyword?: string;
    isUsing?: number | string;
    searchStartTime?: string;
    searchEndTime?: string;
}

// 客服列表行数据
export interface AdminUserKefuFilterState {
    id: number;
    adminId?: number;
    shopId?: number;
    username?: string;
    email?: string;
    avatar?: string;
    mobile?: string;
    isUsing?: number;
    roleId?: number;
    authList?: string[];
    addTime?: string;
    adminUser?: KefuAdminUser;
    role?: KefuRole;
    user?: any;
}

// 客服列表返回
export interface AdminUserKefuFilterResult {
    records: AdminUserKefuFilterState[];
    total: number;
    size?: number;
    current?: number;
}

// 客服新增/编辑表单
export interface AdminUserKefuFormState {
    id?: number;
    username?: string;
    mobile?: string;
    email?: string;
    avatar?: string;
    defAvatar?: string;
    isUsing?: number;
    roleId?: number;
    authList?: string[];
    adminUser?: KefuAdminUser;
}

// 接入规则配置
export interface KefuAccessConfig {
    shopId?: number;
    autoAccessMode: number;
    returnCustomerAccess: number;
}

// 接入规则保存参数
export interface KefuAccessConfigSaveParams {
    shopId?: number;
    autoAccessMode?: number;
    returnCustomerAccess?: number;
}
