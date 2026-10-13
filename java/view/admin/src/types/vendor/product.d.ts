// 列表查询时筛选参数类型
import type {SeckillProductState} from "@/types/promotion/seckill";

export interface ProductFilterParams {
    page?: number;
    size?: number;
    sortField?: string,
    sortOrder?: string,
    keyword?: string;
    shopId?: number | string;
    vendorId?: number | string;
    categoryId?: number | string;
    getProductClientPage?: number | string;
    brandId?: number | string;
    introType?: string;
    productStatus?: number | string;
    productState?: number | string;
    auditState?: number | string;
    isRecycle?: number;
    searchStartTime?: string;
    searchEndTime?: string;
    startDate?: string;
    endDate?: string;
    ids?: string;
    isCanImport?: number | string;
    productBrandId?: number | string;
    productCategoryId?: number | string;
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
    productPrice?: number | string;
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
    productSku: any[];
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
    skuType?: number;
    productId?: string;
    productName?: string;
    galleries?: Array<ImgListItem>;
    video?: any[];
    productSnGenerateType?: number;
    productCategoryId?: number | string;
    productPrice?: number | string;
    productBrief?: string;
    productBrandId?: number | string;
    brandId?: number | string;
    freeShipping?: number;
    seckillMaxNum?: number;
    productState?: number;
    noShipping?: number;
    fixedShippingType?:number;
    fixedShippingFee?: number;
// ==
    promotePrice?: number;
    volumeList?: Object[]; //==
    userRankList?: UserRankListType[]; //==
    giveIntegral?: number;
    rankIntegral?: number;
    integral?: number;
// ==
    productVideo?: string;
    productDesc: ProductDescArrItem[];
// ==
    attrList?: Object;
    productList?: SeckillProductState[];
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
    isSupportCod?: number;
    isSupportReturn?: number;
// ==
    productRelated?: number[];
    productArticleList?: number[];
    eCardList?: any[];
    cardGroupId?: number;
    virtualSample?: string;

    skus: any[]
    skuAttrs?: any[]
}
export interface UserRankListType {
    rankId:number;
    rankName:string;
    price:number;
}

export interface SuppliersListType{
    suppliersId?:number;
    suppliersName?:string;
}
export interface ShippingTplListType{
    shippingTplId:number;
    isDefault:number;
    shippingTplName:string;
}

export interface ImgListItem{
    picDesc:string;
    picLarge:string;
    picOriginal:string;
    picThumb:string;
    picUrl:string;
    picId:number;
    productId:number;
    sortOrder:number;
}

export interface ProductDescArrItem {
    type:string;
    pic:string;
    html:string;
}

export interface ServiceList {
    check:number;
    productServiceId:number;
    productServiceName:string;
}
export interface ProductFormResult {
    attrTplList:Object[];
    serviceList:ServiceList[];
    shippingTplList?: ShippingTplListType[];
    suppliersList?: SuppliersListType[];
    eCardList?: any[];
    item: {
        userRankList:Object[];
        productServiceIds:number[];
    }
}

export interface ProductParticipleFormResult {
    keywords:string
}


export interface AuditProductFormState {
    vendorProductId: number;
    auditState: number;
    auditFailReason: string;
}


