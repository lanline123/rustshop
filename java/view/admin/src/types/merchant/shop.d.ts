
export interface MerchantFilterState {
    merchantId: number;
    applyId: number;
    applyName: string;
    applyLogo?: string;
    firstWord?: string;
    applyIsHot?: boolean;
    isShow?: boolean;
    sortOrder?: number;
}

export interface MerchantSearchFilterResult {
    applyList: MerchantFilterState[];
    firstwordList: string[];
    message: string;
    errcode: number;
}
