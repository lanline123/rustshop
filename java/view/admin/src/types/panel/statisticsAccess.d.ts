export interface statisticsAccessFilterParams {
    startTime: string;
    endTime: string;
    isHits: number;
}

export interface statisticsAccessFilterState {
    records: FilterResult;
    errcode: number;
    message: string;
}

export interface FilterResult {
    horizontalAxis: string[];
    longitudinalAxis: number[];
}
