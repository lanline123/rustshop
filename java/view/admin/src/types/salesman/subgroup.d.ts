// 列表查询时筛选参数类型
export interface groupFilterParams {
    page: number;
    size: number;
    sortField?: string;
    sortOrder?: string;
    groupName?: string;
}

// 获取列表返回参数类型
export interface groupFilterResult {
    records: groupFilterState[];
    filter: {
        page: number;
    };
    total: number;
}
export interface groupFilterState {
    groupId?: number;
    groupName?: string;
}

// 获取详情返回参数类型
export interface groupFormState {
    groupId?: number;
    groupName?: string;
    describe?: string;
}
