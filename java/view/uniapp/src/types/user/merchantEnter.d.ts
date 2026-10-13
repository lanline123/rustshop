export interface ApplyDetailResponse {
    data: ApplyDetailItem;
    code: number;
    message: string;
}

export interface ApplyDetailItem {
    statusText: string;
    merchantApplyId: number;
    userId: number;
    addTime: string;
    shopName: string;
    status: number;
    type: number;
    contactName: null;
    contactMobile: null;
    auditTime: null;
    shopData: ShopData;
    baseData: BaseData;
    merchantData: MerchantData;
    companyName: string;
    corporateName: string;
    initialUserInfo: AnyObject;
    auditRemark: null;
}

export interface BaseData {
    type: number;
    certificateValidityPeriod: number;
    certificateValidityPeriodDate: string[];
    certificateValidityPeriodEnd: string;
    frontOfPhoto: BackOfPhoto;
    backOfPhoto: BackOfPhoto;
    documentType: number;
    corporateName: string;
    documentNumber: string;
    birthday: string;
    sex: number;
    residentialAddress: string;
}

export interface BackOfPhoto {
    picName: string;
    picThumb: string;
    picUrl: string;
}

export interface MerchantData {
    contactName: string;
    contactPhone: string;
    commonEmail: string;
    bankDeposit: string;
    bankBranch: string;
    bankCard: string;
    merchantName: string;
    customerServicePhone: string;
    businessAddress: string;
    detailedAddress: string;
    businessAddressName: string;
    contactEmail: string;
}

export interface ShopData {
    shopName: string;
    customerServicePhone: string;
    businessAddress: number[];
    detailedAddress: string;
    shopLogo: BackOfPhoto[];
    supplementaryInformation: BackOfPhoto[];
    businessAddressName: string;
    shopTitle: string;
    contactMobile: string;
    description: string;
}
