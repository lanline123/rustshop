export interface ProductItem {
    productId: number;
    productName: string;
    description: string;
    productSn?: string;
    seoLink?: string;
    productTsn?: string;
    productStock: number;
    productPrice?: string;
    marketPrice?: string;
    shippingTplId?: number;
    productStatus?: number;
    productType?: number;
    categoryId?: number;
    brandId?: number;
    shopId?: number;
    keywords: string;
    firstWholesalePrice?: string | number | null;
    wholesaleCardMoq?: number | null;
    wholesaleMoqDimension?: 1 | 2 | null;
    wholesaleMultiple?: number | null;
    /** 批发多规格定价模式：1=商品级混批（按全规格总数匹配阶梯）/ 2=SKU 级独立；非批发/未配置为 null。由详情页从 detail 顶层灌入 */
    wholesalePriceMode?: number | null;
    storeCategoryId?: number;
    checkStatus?: number;
    checkReason?: string;
    clickCount?: number;
    productWeight?: string;
    isPromote?: number;
    isPromoteActivity?: number;
    promotePrice?: string;
    promoteStartDate?: number;
    promoteEndDate?: number;
    seckillMaxNum?: number;
    productBrief?: string;
    productDesc?: string;
    picUrl?: string;
    picThumb?: string;
    picOriginal?: string;
    commentTag?: string;
    freeShipping?: number;
    integral?: number;
    addTime?: number;
    sortOrder?: number;
    storeSortOrder?: number;
    isDelete?: number;
    isBest?: number;
    isNew?: number;
    isHot?: number;
    lastUpdate?: number;
    remark?: string;
    giveIntegral?: number;
    rankIntegral?: number;
    suppliersId?: number;
    virtualSales?: number;
    limitNumber?: number;
    productCare?: string;
    productRelated?: any;
    productServiceIds?: any;
    isSupportReturn?: number;
    isSupportCod?: number;
    productVideo?: string;
    prepayPrice?: string;
    number?: number;
    discountsPrice?: string;
    exchangeIntegral?: number;
    isSeckill?: number;
    productSku?: SkuList[];
    shop?: any;
    isBuy?: number;
    paidContent?: DescArr[];
    virtualSample?: string;
    videoList?: VideoList[];
    grouponPromotionItemId?: number;
    deliveryMode?: number;
    deliveryType?: number;
    price?: string;
    productSaleMode?: number;
    seckillStock?: number;
    limitNumber?: number;
}

export interface VideoList {
    id: number;
    videoId: number;
    productId: number;
    videoUrl: string;
    videoCover: string;
    format: string;
}

export interface ProductFilterParams {
    page?: number;
    size?: number;
}
export interface ProductFilterResult {
    item: ProductItem;
    descArr: DescArr[];
    skuList: SkuList[];
    picList: PicList[];
    serviceList: ServiceList[];
    attrList: AttrList;
    rankDetail: RankDetail;
    checkedValue: string[];
    consultationTotal: number;
    /**
     * 全等级批发阶梯价表
     * @description 仅批发商身份返回；按 priceId 升序。displayPrice 为后端已派生好的最终展示价，前端不再二次运算
     */
    wholesalePriceList?: WholesalePriceTier[] | null;
    /**
     * 批发定价模式
     * @description 1=混批（按全规格总数匹配阶梯）；2=SKU 级独立；仅批发商身份返回
     */
    wholesalePriceMode?: number | null;
}

/**
 * 批发阶梯价单档
 */
export interface WholesalePriceTier {
    priceId: number;
    /** 该阶梯起量门槛（"≥ N 件"） */
    tierMinQty: number;
    /** 1=折扣 / 2=固定金额 */
    priceMode: number;
    /** 折率，priceMode=1 时有值（如 "8.50" 表 8.5 折） */
    discount: string | null;
    /** 固定金额，priceMode=2 时有值 */
    fixedPrice: string | null;
    /** 等级 ID；0=通用 */
    gradeId: number;
    /** 等级名；通用时为「通用」 */
    gradeName: string;
    /** SKU ID；null 表 SPU 维度 */
    skuId: number | null;
    /** 后端已派生好的最终展示价，前端直接显示 */
    displayPrice: string;
}
export interface RankDetail {
    total?: number;
    averageRank?: number;
    goodPercent?: number;
}
export interface AttrList {
    normal?: Normal[];
    spe?: Spe[];
    extra?: Extra[];
}
export interface AttrList {
    normal?: Normal[];
    spe?: Spe[];
    extra?: Extra[];
}
export interface Normal {
    attrName: string;
    attrList: AttrList[];
}

export interface GuessLikeListResult {
    productList: ProductItem[];
}
export interface Spe {
    attrName: number;
    attrList: AttrList[];
}

export interface Extra {
    attrName: string;
    attrList: AttrList[];
}

export interface AttrList {
    attributesId: number;
    productId: number;
    attrType: number;
    attrName: string;
    attrValue: string;
    attrPrice: string;
    attrColor: string;
    attrPic: string;
    attrPicThumb: string;
}

export interface SkuList {
    skuId: number;
    productId: number;
    skuValue: string;
    skuData: SkuData[];
    skuSn: string;
    skuStock: number;
    skuTsn: string;
    skuPrice: string;
    price?: string;
    stock?: number;
    title?: string;
    num?: number;
}

export interface SkuData {
    name: string;
    value: string;
}

export interface PicList {
    picId?: number;
    productId?: number;
    picUrl?: string;
    picDesc?: string;
    picThumb?: string;
    picLarge?: string;
    sortOrder?: number;
    videoUrl?: string;
    videoCover?: string;
}
export interface DescArr {
    type?: string;
    pic?: string;
    html?: string;
    videoUrl?: string;
    cover?: string;
}

export interface SkuDetail {
    price: string;
    originPrice: string | null;
    marketPrice?: string | null;
    sku?: {
        marketPrice?: string | number | null;
    };
    stock: number;
    promotion: SkuPromotion[];
    errcode: number;
    message: string;
}

export interface SkuPromotion {
    promotionId: number;
    promotionName: string;
    startTime: number;
    endTime: number;
    type: number;
    shopId: number;
    relationId: number;
    range: number;
    rangeData: number[];
    skuIds: number[] | null;
    data: anyObject;
    // 二期会员标签（D1096）：false 时活动标签正常展示但提示仅限指定会员
    canJoin?: boolean;
    restrictTip?: string;
}

export interface CommentDetail {
    total?: number;
    goodCount?: number;
    moderateCount?: number;
    badCount?: number;
    showCount?: number;
    goodPercent?: number;
    moderatePercent?: number;
    badPercent?: number;
}

export interface CommentFilterResult {
    records: CommentItem[];
    total: number;
}

export interface CommentReplyItem {
    commentId?: number;
    userId?: number;
    username?: string;
    content?: string;
    parentId?: number;
    addTime?: string;
}

export interface CommentItem {
    commentId?: number;
    userId?: number;
    username?: string;
    nickname?: string;
    avatar?: string;
    productId?: number;
    orderId?: number;
    orderItemId?: number;
    commentRank?: number;
    content?: string;
    addTime?: string;
    status?: number;
    parentId?: number;
    usefull?: number;
    useless?: number;
    commentTag?: any;
    showPics?: any;
    isRecommend?: number;
    isTop?: number;
    isShowed?: number;
    isDefault?: number;
    sortOrder?: number;
    shopId?: number;
    replies?: CommentReplyItem[];
    kefuName?: string;
}

//商品优惠券
export interface ProductCouponItem {
    couponId?: number;
    couponName?: string;
    couponMoney?: string;
    couponDiscount?: number;
    couponDesc?: string;
    couponType?: number;
    sendRange?: number;
    sendRangeData?: any[];
    minOrderAmount?: string;
    sendStartDate?: string;
    sendEndDate?: string;
    sendType?: number;
    useDay?: number;
    useStartDate?: string;
    useEndDate?: string;
    isShow?: number;
    isGlobal?: number;
    isNewUser?: number;
    enabledClickGet?: number;
    limitUserRank?: number[];
    shopId?: number;
    isReceive?: number;
    limitNum: number;
    receiveNum: number;
    delayDay: number;
}
//商品咨询
export interface ProductConsultationItem {
    addTime?: string;
    username?: string;
    nickname?: string;
    content?: string;
    reply?: any;
}
export interface ProductConsultationList {
    records: ProductConsultationItem[];
    total: number;
}

export interface ProductConsultationListParams {
    productId: number;
    page?: number;
    size?: number;
    shopId?: number;
}

export interface ProductConsultationFormState {
    productId?: number;
    shopId?: number;
    content?: string;
    email?: string;
    type: number;
}

export interface ServiceList {
    productServiceId: number;
    productServiceName: string;
    icoImg: string;
}

export interface BrandInfo {
    brandId: number;
    brandName: string;
    brandLogo: string;
    siteUrl: string;
    firstWord: string;
}

interface PromotionFilterParams {
    products: Products[];
    shopId?: number;
    from?: string;
}
interface Products {
    productId: number;
    skuId?: number;
}

export interface PromotionList {
    shopId: number;
    productId: number;
    activityInfo: AnyObject;
}

export interface PromotionData {
    promotionDesc: string;
    couponId: number;
    couponName: string;
    couponMoney: string;
    couponDiscount: string;
    couponDesc: string;
    couponType: number;
    sendRange: number;
    sendRangeData: any[];
    minOrderAmount: string;
    sendStartDate: string;
    sendEndDate: string;
    sendType: number;
    useDay: number;
    useStartDate: string;
    useEndDate: string;
    isShow: number;
    isGlobal: number;
    isNewUser: number;
    enabledClickGet: number;
    limitUserRank: any[];
    shopId: number;
    isDelete: number;
    limitNum: number;
    delayDay: number;
    sendNum: number;
    maxOrderAmount: string;
    couponUnit: number;
    reduceType: number;
}

export interface ExchangeDetail {
    id?: number;
    pointsDeductedAmount?: string;
    isEnabled?: number;
    isHot?: number;
    exchangeIntegral?: number;
    discountsPrice?: string;
    productStock?: number;
}

export interface ProductAmountItem {
    count: number;
    total: string;
}

export interface SkuAvailabilitysResponse {
    data: { [key: string]: SkuAvailabilitys };
    code: number;
    message: string;
}

export interface SkuAvailabilitys {
    originPrice: null;
    price: string;
    stock: number;
    promotion: any[];
}
