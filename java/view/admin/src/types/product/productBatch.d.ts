
export interface ProductBatchImageFormState {
    dealRange: number;
    isReplace: number;
    dealType: number;
    rangeIds: number[];
}

export interface ProductBatchExportFormState {
    dealRange: number;
    dealType: number;
    rangeIds: string;
}

export interface ProductBatchModifyFormState {
    isAutoCat: number;
    isAutoBrand: number;
    isChangePic: number;
    isUpload: number;
    file: any;
}