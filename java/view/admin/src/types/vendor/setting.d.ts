import type { ShipReceiveContactInfo } from "@/types/setting/shipInfo";

export interface BaseAdminVendorConfig {
    vendorProductNeedCheck: number;
    vendorMaxSubAdministrator: string;
    vendorSetPriceType: number;
    vendorSetPriceAutoValue: string | number;
}

export interface AdminUserFilterParams {
    page: number;
    size: number;
    sortField?: string;
    sortOrder?: string;
    keyword?: string;
    vendorId?: number | string;
}

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
    vendorId?: number | string;
    adminId?: number;
    isAdmin?: number;
    userId?: string;
    vendorLogo?: string;
    vendorName?: string;
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
    vendorData?: any;
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

export interface VendorConfig {
    status: number;
    kefuPhone: string;
    afterSalesReview?: number;
    shipInfo?: ShipReceiveContactInfo;
    receiveInfo?: ShipReceiveContactInfo;
}

export interface StaffFrom {
    usingUser?: number;
    stopUsingUser?: number;
    residue?: number;
    adminLog: AdminLog[];
}
export interface AdminLog {
    logId?: number;
    userId?: number;
    logInfo?: string;
    logTime?: string;
    ipAddress?: string;
    username?: string;
}
