
export interface AdminLogFilterParams {
    page: number;
    size: number;
    sortField?: string;
    sortOrder?: string;
    logTime?: number;
    keyword?: string;
    userId?: number;
}

export interface AdminLogFilterResult {
    records: AdminLogFilterState[];
    filter: {
        page: number;
    };
    total: number;
}

export interface AdminLogFilterState {
    logId?: number;
    userId?: number;
    ipAddress?: string;
    logTime?: string;
    username?: string;
    logInfo?: string;
}

