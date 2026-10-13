export interface AppVersionFormResult {
    item: AppVersionFormState;
}

export interface AppVersionFormState {
    androidLink?: string;
    androidVersion?: string;
    iosLink?: string;
    iosVersion?: string;
    hotUpdateType?: number;
    hotUpdateLink?: string;
}
