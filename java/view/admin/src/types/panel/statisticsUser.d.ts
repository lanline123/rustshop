export interface statisticsUserFilterParams {
    startTime: string;
    isExport?: string;
    endTime: string;
}

export interface statisticsUserFilterState {
    records: FilterResult;
    errcode: number;
    message: string;
}

export interface FilterResult {
    visitNum: number | string;
    visitGrowthRate: number;
    viewNum: number;
    viewGrowthRate: number;
    addUserNum: number;
    addUserGrowthRate: number;
    dealUserNum: number;
    dealUserGrowthRate: unknown;
    visitToUser: number;
    visitToUserRate: number;
    rechargeUserNum: number;
    rechargeUserGrowthRate: unknown;
}
