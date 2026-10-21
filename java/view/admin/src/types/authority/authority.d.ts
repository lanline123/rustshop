// 列表筛选
export interface AuthorityFilterParams {
    page: number,
    size: number,
    sortField?: string;
    sortOrder?: string;
    keyword?: string;
    parentId: number;
    adminType?: string;
}

export interface AuthorityFilterState {
    authorityId: number;
    authorityName?: string;
    authoritySn?: string;
    categoryIco?: string;
    routeLink?: string;
    hasChildren?: number;
    isShow?: number;
    isSystem?: number;
    parentId?: number;
    sortOrder?: number;
    storeAvailable?: number;
    authorityIco?: string;
    childAuth?: ChildAuthItem[];
}


export interface AuthorityFilterResult {
    records: AuthorityFilterState[];
    filter: {
        page: number;
    };
    parentName: string;
    total: number;
}


export interface AuthorityFormState {
    authorityId: number;
    authorityName?: string;
    authoritySn?: string;
    isCheck?: boolean;
    indeterminate?: boolean;
    categoryIco?: string;
    routeLink?: string;
    hasChildren?: number;
    isShow?: number;
    isSystem?: number;
    parentId?: number;
    sortOrder?: number;
    storeAvailable?: number;
    authorityIco?: string;
    adminType?: string;
    children?: AuthorityFormState[];
    childAuth?: ChildAuthItem[];
}

export interface ChildAuthItem {
    authName?: string;
    authSn?: string;
    isCheck?: boolean;
    disabled?: boolean;
    indeterminate?: boolean;
}
