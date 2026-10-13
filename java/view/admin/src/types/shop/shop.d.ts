// 列表查询时筛选参数类型
export interface ShopFilterParams {
    page: number;
    size: number;
    shopType: number;
    sortField?: string,
    sortOrder?: string,
    keyword?: string;
    mainAccount?: string;
    account?: string;
    corporateName?: string;
    status?: number | string;
    shopTitle?: string;
    endTime?: string;
    startTime?: string;
    shopId?: number | string;
    storeParentId?: number | string;
    addEndTime?: string;
    addStartTime?: string;
    areaStoreManagerId?: string | number;
    shopRegionId?: string | number;
    includeSelfOperated?: number;
}
export interface ShopSelectFilterParams {
    keyword?: string;
    size?: number;
}

// 获取列表返回参数类型
export interface ShopFilterResult {
    records: ShopFilterState[];
    filter: {
        page: number;
    };
    total: number;
}
export interface ShopFilterState {
    shopId: number;
    check?: boolean;
    shopTitle?: string;
    storeTitle?: string;
    storeLogo?: string;
    userName?: string;
    isSelf?: number;
    shopRankId?: number;
    shopRankName?: string;
    shopStatus?: number;
}

// 获取详情返回参数类型
export interface ShopFormResult {
    item: ShopFormState[];
}
export interface ShopFormState {
    shopId?: number;
    shopTips?: number[];
    storeParentId?: number | string;
    merchantId?: number | string;
    shopTitle: string;
    shopLogo?: string;
    shopCoverPicture?: string;
    description?: string;
    contactMobile?: string;
    status?: number;
    shopContactConfig?: ShopContactConfig[];
    shopLatitude?: number | string;
    shopLongitude?: number | string;
    shopOpenCloseConfig?: ShopOpenCloseConfig;
    shopRegionIds?: number[];
    shopShowPicture?: any[];
    tips?: any[];
    shopDetailedAddress?: string;
    shopType?: number;
    shopRegionNames?: string[];

}

/**
 * ShopContactConfig，联系方式（JSON）
 */
export interface ShopContactConfig {
    /**
     * 类型 1 = 普通座机号 2 = 企业座机号 3 = 手机号码
     */
    type?: number;
    /**
     * 号码 使用 - 隔开，前面为区号，后面为号码
     */
    values?: string;
    [property: string]: any;
}

/**
 * ShopOpenCloseConfig，运营时间（JSON）
 */
export interface ShopOpenCloseConfig {
    times?: TimeRange[];
    /**
     * 营业模式类型 1 = 全天 2 = 每日固定时间 3 = 按星期时间
     */
    type?: number;
    [property: string]: any;
}

/**
 * TimeRange
 */
export interface TimeRange {
    /**
     * 星期（1=周一，7=周日），type=3 时必填，其他模式可为 null
     */
    dayOfWeek?: string;
    /**
     * 结束时间（HH:mm 格式）
     */
    end?: string;
    /**
     * 开始时间（HH:mm 格式）
     */
    start?: string;
    [property: string]: any;
}



export interface ShopSettlementFormState {
    code?: string;
    dateType?: number;
    useDay?: number;
    shopId?: number;
}