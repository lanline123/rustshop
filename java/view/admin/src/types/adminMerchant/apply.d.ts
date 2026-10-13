// 列表查询时筛选参数类型
export interface ApplyFilterParams {
    page: number;
    size: number;
    sortField: string,
    sortOrder: string,
    username?: string;
    status?: number;
}

// 获取列表返回参数类型
export interface ApplyFilterState {
    merchantApplyId: number;
    userId: number;
    addTime: string;
    shopName: string;
    status: number;
    statusText?: string;
    type: number;
    contactName: string | null;
    contactMobile: string | null;
    auditTime: string | null;
    auditAdminId?: number | null;
    auditAdminName?: string | null;
    shopData: ShopData;
    baseData: BaseData;
    merchantData: MerchantData;
    companyName: string | null;
    user: User;
}
interface PicData {
    picThumb: string;
    picUrl: string;
    picName: string;
}

interface ShopData {
    shopLogo: PicData[];
    supplementaryInformation: PicData[];
    businessLicenseImg: PicData[];
    businessAddress: number[];
    shopTitle: string;
    contactMobile: string;
    description: string;
    businessAddressName: string;
    customerServicePhone: string;
    detailedAddress: string;
}

interface BaseData {
    type: number;
    certificateValidityPeriod: number;
    certificateValidityPeriodDate: string[];
    operatingTermTypeDate: string[];
    certificateValidityPeriodEnd: string;
    residentialAddress: string;
    frontOfPhoto: PicData[];
    backOfPhoto: PicData[];
    supplementaryInformation: PicData[];
    documentType: number;
    operatingTermType: number;
    corporateName: string;
    documentNumber: string;
    operatingTermTypeEnd: string;
    businessLicenseId: string;
    businessScope: string;
    companyName: string;
    licenseAddrProvinceName: string;
    businessLicenseAddress: string;
    sex: number;
    birthday: string;
}

interface User {
    userId: number;
    username: string;
}
export interface ApplySearchFilterResult {
    applyList: ApplyFilterState[];
    firstwordList: string[];
    message: string;
    errcode: number;
}

export interface ApplyFilterResult {
    records: ApplyFilterState[];
    filter: ApplyFilterParams;
    total: number;
    statusList: object[];
}


interface MerchantData {
    merchantName: string;
    contactPhone: string;
    accountOpeningPermit: PicData[];
    businessLicenseImg: PicData[];
    contactName: string;
    contactEmail: string;
    bankDeposit: string;
    description: string;
    businessAddressName: string;
    detailedAddress: string;
    bankBranch: string;
    corporateName: string|null;
    bankCard: string;
    customerServicePhone: number;
}



export interface ApplyFormState {
    statusText: string;
    auditRemark: string;
    merchantApplyId: number;
    userId: number;
    addTime: string;
    shopName: string;
    corporateName: string;
    status: number;
    type: number;
    
    contactName: string|null;
    contactMobile: string|null;
    auditTime: string|null;
    auditAdminId?: number | null;
    auditAdminName?: string | null;

    shopData: ShopData;
    baseData: BaseData;
    merchantData: MerchantData;
    companyName: string|null;
}


