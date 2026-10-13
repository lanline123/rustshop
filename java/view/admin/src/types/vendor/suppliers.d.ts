// 列表筛选
export interface SuppliersFilterParams {
    page: number,
    size: number,
    sortField?: string;
    sortOrder?: string;
    keyword?: string;
    mainAccount: string;
    account: string;
    status?: number | string;
    vendorIds?: string;
}

export interface SuppliersFilterResult {
    records: SuppliersFilterState[];
    filter: {
        page: number;
    };
    total: number;
}


export interface SuppliersFilterState {
    /**
    * 添加时间
    */
    addTime?: string;
    /**
     * 联系人手机
     */
    contactMobile?: string;
    /**
     * 联系人姓名
     */
    contactName?: string;
    /**
     * 登录账号
     */
    loginAccount?: string;
    /**
     * 供应商状态 1开启 2关闭
     */
    status?: number;
    /**
     * 所属性质 1个人 2企业
     */
    type?: number;
    /**
     * 供应商Id
     */
    vendorId?: number;
    /**
     * 供应商logo
     */
    vendorLogo?: string;
    /**
     * 供应商名称
     */
    vendorName?: string;
    [property: string]: any;

}

/**
 * VendorCreateParam
 */
export interface SuppliersFormState {
    personData: PersonData;
    /**
     * 类型 1个人 2企业
     */
    type: number;
    vendorData: VendorData;
    /**
     * 供应商logo
     */
    vendorLogo: string;
    /**
     * 供应商名称
     */
    vendorName: string;

    [property: string]: any;
}

/**
 * PersonData，个人信息
 */
export interface PersonData {
    /**
     * 补充信息
     */
    additionalImg: PicInfo[];
    /**
     * 背面照片
     */
    backOfPhoto?: string;
    /**
     * 出生日期
     */
    birthday?: string;
    /**
     * 证书有效期
     */
    certificateValidityPeriod?: number;
    /**
     * 证书有效期日期
     */
    certificateValidityPeriodDate?: string[];
    /**
     * 姓名
     */
    documentName?: string;
    /**
     * 证件号码
     */
    documentNumber?: string;
    /**
     * 证件类型
     */
    documentType?: number | string;
    /**
     * 正面照片
     */
    frontOfPhoto?: string;
    /**
     * 居住地址
     */
    residentialAddress?: string;
    /**
     * 性别
     */
    sex?: number;
    [property: string]: any;
}

/**
 * PicInfo，图片信息
 */
export interface PicInfo {
    /**
     * 图片名称
     */
    picName?: string;
    /**
     * 图片缩略图
     */
    picThumb?: string;
    /**
     * 图片URL
     */
    picUrl?: string;
    /**
     * 状态
     */
    status?: string;
    /**
     * UID
     */
    uid?: number;
    /**
     * URL
     */
    url?: string;
    [property: string]: any;
}

/**
 * Photo，照片
 */
export interface Photo {
    /**
     * 图片URL
     */
    picUrl?: string;
    [property: string]: any;
}

/**
 * VendorData，供应商信息
 */
export interface VendorData {
    /**
     * 补充信息
     */
    additionalImg: PicInfo[];
    /**
     * 支行名称
     */
    bankBranch?: string;
    /**
     * 银行卡号
     */
    bankCard?: string;
    /**
     * 开户银行
     */
    bankDeposit?: string;
    /**
     * 联系人邮箱
     */
    contactEmail?: string;
    /**
     * 联系人姓名
     */
    contactName?: string;
    /**
     * 联系人手机
     */
    contactPhone?: string;
    /**
     * 客服电话
     */
    customerServicePhone?: string;
    /**
     * 详细地址
     */
    detailedAddress?: string;
    /**
     * 供应商地址
     */
    vendorAddress?: string[];
    /**
     * 供应商名称
     */
    vendorName?: string;
    companyName?: string;
    licenseAddrProvince?: string[];
    businessLicenseAddress?: string;
    businessScope?: string;
    businessLicenseId?: string;
    operatingTermType?: number;
    operatingTermTypeDate: string[];
    operatingTermTypeEnd?: string;
    [property: string]: any;
}
