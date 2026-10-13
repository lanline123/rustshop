
export interface AdminRoleFilterParams {
    page: number;
    size: number;
    sortField?: string;
    sortOrder?: string;
    keyword?: string;
}

export interface AdminRoleFilterResult {
    records: AdminRoleFilterState[];
    filter: {
        page: number;
    };
    total: number;
}

export interface AdminRoleFilterState {
    roleId: number;
    roleName?: string;
    roleDesc?: string;
    isSystem?: boolean | number;
}

export interface AdminRoleFormState {
    roleId?: number;
    roleName?: string;
    roleDesc?: string;
    authorityList?: string[];
    adminType?: string;
    merchantId?: number | null;
    shopId?: number | null;
    isSystem?: number;
}
