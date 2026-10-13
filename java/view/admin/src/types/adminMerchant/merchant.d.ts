// 列表查询时筛选参数类型
export interface MerchantFilterParams {
    page?: number;
    size?: number;
    sortField?: string;
    sortOrder?: string;
    keyword?: string;
    isShow?: number;
    applyIsHot?: number;
    firstWord?: string;
    merchantId?: number;
}

// 获取列表返回参数类型
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

export interface MerchantFilterResult {
    records: MerchantFilterState[];
    filter: MerchantFilterParams;
    total: number;
}

interface MerchantData {
    contactPhone: string;
    merchantName: string;
    customerServicePhone: string;
    accountOpeningPermitTemp: string;
    businessLicenseImgTemp: string;
    detailedAddress: string;
    accountOpeningPermit: PicData[];
    contactName: string;
    businessAddress: any[];
    businessAddressName: string;
    contactEmail: string;
    bankDeposit: string;
    businessLicenseImg: PicData[];
    bankBranch: string;
    corporateName: string | null;
    bankCard: string;
    fullDetailedAddress: string;
}
interface PicData {
    picThumb?: string;
    picUrl: string;
    picName?: string;
    url?: string;
}

interface ShopData {
    shopLogo: PicData[];
    contactMobile: string;
    shopTitle: string;
    description: string;
    supplementaryInformation: PicData[];
    shopName: string;
    customerServicePhone: string;
    detailedAddress: string;
}
interface BaseData {
    type: number;
    certificateValidityPeriod: number;
    certificateValidityPeriodDate: string[];
    operatingTermTypeDate: string[];
    certificateValidityPeriodEnd: string;
    licenseAddrProvince: any[];
    residentialAddress: string;
    frontOfPhoto: PicData[];
    frontOfPhotoTemp: string;
    backOfPhotoTemp: string;
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
    fullBusinessLicenseAddress: string;
}

interface User {
    userId: number | null;
    username: string;
}
// 获取详情返回参数类型
export interface MerchantFormState {
    statusText: string;
    merchantId: number;
    merchantApplyId: number;
    userId: number;
    addTime: string;
    shopName: string;
    status: number;
    type: number;
    contactName: string | null;
    contactMobile: string | null;
    auditTime: string | null;
    shopData: ShopData;
    baseData: BaseData;
    merchantData: MerchantData;
    companyName: string | null;
    admin: Admin;
}
interface Admin {
    adminId: number | null;
    userId: number | null;
    type: number;
}
