// 列表查询时筛选参数类型

export interface ProductFilterParams {
    page?: number;
    size?: number;
    sortField?: string;
    sortOrder?: string;
    keyword?: string;
    shopId?: number | string;
    categoryId?: number | string;
    brandId?: number | string;
    introType?: string;
    productStatus?: number;
    checkStatus?: number;
    isDelete?: number;
    searchShop?: number;
    searchStartTime?: string;
    searchEndTime?: string;
    ids?: string;
    reservationProduct?: number;
    type?: number | null;
    showVendorProduct?: number | null;
    productType?: number | null;
    saleMode?: number; // 新增：销售模式筛选
    isFilterPresaleProduct?: number;
    /** 排除的商品类型（1普通商品 2虚拟商品 3卡密商品 4付费内容 5预约上门 6预约到店） */
    excludeProductTypes?: number[];
}

// 获取列表返回参数类型
export interface ProductFilterState {
    productId: number;
    shippingTplId: any;
    productName: string;
    productSn: string;
    url: string;
    picThumb: string;
    brandName: string;
    storeTitle: string;
    type: number;
    suppliersName: string;
    checkStatus: number;
    productPrice?: number;
    status: number;
    isBest: number;
    isNew: number;
    isHot: number;
    productStock: number;
    productExist: number;
    sortOrder: number;
    ids: number[];
    skuIds?: number[];
    check?: boolean;
    productSku: ProductListSkuState[];
    vendorProductId: number;
    inventoryId: number;
    saleMode?: number; // 新增：销售模式
    saleModeText?: string; // 新增：销售模式文本
    presaleTag?: string | null; // 新增：预售标签
}

export interface ProductListSkuState {
    skuId: number;
    skuStock: number | string | null;
    skuData?: Array<{ name: string; value: string }>;
    isEnabled?: 0 | 1;
    [key: string]: any;
}

export interface ProductSkuFormState {
    skuId?: number;
    productId?: number;
    skuValue: string;
    skuData?: Array<{ name: string; value: string }>;
    attrs?: Array<{ attrName: string; attrValue: string }>;
    attrNames?: string[];
    attrValues?: string[];
    skuSn?: string;
    skuTsn?: string;
    skuPrice?: number | string | null;
    marketPrice?: number | string | null;
    costPrice?: number | string | null;
    isEnabled?: 0 | 1;
    [key: string]: any;
}
export interface ProductFilterResult {
    records: ProductFilterState[];
    filter: ProductFilterParams;
    total: number;
    waitingCheckedCount: number;
}

// 获取详情返回参数类型
export interface ProductFormState {
    productType?: number;
    snType?: number;
    shopCategoryId?: number;
    shopId?: number;
    productId?: string;
    picThumb?: string;
    productName?: string;
    imgList?: Array<ImgListItem>;
    productVideoInfo?: any[];
    productStock?: number | null;
    categoryId?: number | string;
    productBrief?: string;
    productPrice?: number | string;
    marketPrice?: number;
    productSn?: string;
    productTsn?: string;
    brandId?: number | string;
    vendorId?: number | string;
    suppliersList?: SuppliersListType[];
    suppliersId?: number;
    shippingTplId?: number | string;
    shippingTplList?: ShippingTplListType[];
    freeShipping?: number;
    seckillMaxNum?: number;
    productStatus?: number;
    noShipping?: number;
    fixedShippingType?: number;
    fixedShippingFee?: number;
    isShopPickup?: number;
    isLogistics?: number;
    isShopDelivery?: number;
    // ==
    limitNumber?: number | string;
    isBest?: number;
    isNew?: number;
    isHot?: number;
    isPromote?: number;
    promotePrice?: number;
    volumeList?: Object[]; //==
    userRankList?: UserRankListType[]; //==
    giveIntegral?: number;
    rankIntegral?: number;
    integral?: number;
    // ==
    productVideo?: string;
    productDescArr?: ProductDescArrItem[];
    // ==
    attrList: any;
    productList?: ProductSkuFormState[];
    attrChanged?: boolean;
    // ==
    virtualSales?: number;
    commentTag?: string;
    seoProductTitle?: string;
    productWeight?: number;
    warnNumber?: number;
    isAloneSale?: number;
    keywords?: string;
    productInfo?: string;
    remark?: string;
    productCare?: string;
    serviceList: ServiceList[];
    productServiceIds?: number[];
    isSupportCod?: number;
    isSupportReturn?: number;
    // ==
    productRelated?: number[];
    productArticleList?: number[];
    eCardList?: any[];
    cardGroupId?: number;
    virtualSample?: string;
    paidContent: ProductDescArrItem[];
    vendorProductId: number;
    //==
    reservationTplId?: number | null | undefined;
    reservationTimes?: TimePeriod[];
    costPrice?: number;
    productUnit?: string;
    //==
    seoIdentification?: string;
    // 新增：预售相关
    saleMode?: number; // 销售模式
    presaleConfig?: PresaleConfig; // 预售配置
    presaleInfo?: PresaleInfo; // 预售信息（编辑时从详情返回）
    giftCardGroupsId?: number;
    attributesTplId: number | null;
    skuType?: number;
    timedOffShelf?: number;
    autoOfflineTime?: string;
    autoOnlineTime?: string;
    /** 交付模式：1-单次交付，2-多次交付（周期购/次卡） */
    deliveryMode?: number;
}


export interface TimePeriod {
    startTime: string;
    endTime: string;
}
export interface UserRankListType {
    rankId: number;
    rankName: string;
    price: number;
}

export interface SuppliersListType {
    suppliersId?: number;
    suppliersName?: string;
}
export interface ShippingTplListType {
    shippingTplId: number;
    isDefault: number;
    shippingTplName: string;
}

export interface ImgListItem {
    picDesc: string;
    picLarge: string;
    picOriginal: string;
    picThumb: string;
    picUrl: string;
    picId: number;
    productId: number;
    sortOrder: number;
}

export interface ProductDescArrItem {
    type: string;
    pic?: string;
    html?: string;
    videoUrl?: string;
    cover?: string;
}

export interface ServiceList {
    check: number;
    productServiceId: number;
    productServiceName: string;
}
export interface ProductFormResult {
    serviceList: ServiceList[];
    shippingTplList?: ShippingTplListType[];
    suppliersList?: SuppliersListType[];
    eCardList?: any[];
    item: {
        userRankList: Object[];
        productServiceIds: number[];
    };
}

export interface ProductParticipleFormResult {
    keywords: string;
}

export interface AuditProductFormState {
    id: number;
    checkStatus: number;
    checkReason: string;
}

// ==================== 预售功能相关类型定义 ====================

/** 销售模式 */
export enum SaleMode {
    SPOT = 0, // 现货销售
    FULL_PRESALE = 1, // 全款预售
    DEPOSIT_PRESALE = 2 // 定金预售
}

/** 定金收取方式 */
export enum DepositType {
    RATIO = 1, // 按百分比
    FIXED_AMOUNT = 2 // 按固定金额
}

/** 时间类型 */
export enum TimeType {
    RELATIVE = 1, // 相对时间
    FIXED = 2 // 固定时间
}

/** 预售配置 */
export interface PresaleConfig {
    presaleId?: number;
    saleMode: 1 | 2;
    depositType?: 1 | 2;
    depositRatio?: number;
    isRoundDown?: 0 | 1;
    depositFixedAmount?: number;
    finalPaymentType?: 1 | 2;
    finalPaymentRelativeDays?: number;
    finalPaymentDurationDays?: number;
    finalPaymentDurationHours?: number;
    finalPaymentStartTime?: number | string;
    finalPaymentEndTime?: number | string;
    deliveryType: 1 | 2;
    deliveryRelativeDays?: number;
    deliveryRelativeHours?: number;
    deliveryDate?: number | string;
    status?: 0 | 1;
}

/** 预售信息（详情返回） */
export interface PresaleInfo extends PresaleConfig {
    presaleId: number;
    depositAmount?: number;
}
/**
 * SKU商品对象类型
 */
export interface SkuProductFilterState {
    productId: number;
    productSn: string;
    productName: string;
    categoryId: number;
    picUrl: string;
    skuId: number;
    skuData: string;
    shopId: number;
}

/**
 * SKU商品列表返回参数类型
 */
export interface SkuProductFilterResult {
    records: SkuProductFilterState[];
    filter: ProductFilterParams;
    total: number;
}
