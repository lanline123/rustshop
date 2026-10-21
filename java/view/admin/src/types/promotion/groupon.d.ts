import { AnyObject } from "ant-design-vue/es/_util/type";
import { ShopFilterState } from "@/types/shop/shop";
import type { SendUserTarget } from "@/types/user/memberAudience";

export interface GrouponFilterParams {
    page: number;
    size: number;
    sortField?: string,
    sortOrder?: string,
    keyword?: string;
    grouponName?: string;
    productName?: string;
    productSn?: string;
    shopId?: string;
    status?: number | string;
    startTime?: string;
    endTime?: string;
    groupType?: number;
}

// 获取列表返回参数类型
export interface GrouponFilterResult {
    records: GrouponFilterState[];
    filter: {
        page: number;
    };
    total: number;
}

export interface GrouponFilterState {
    grouponPromotionId?: number
    name?: string
    startTime?: string
    endTime?: string
    groupType?: number
    shopId?: number
    statusName?: string
    shopName?: string
    receiveAmount?: string
    groupSuccessCount?: string
    payOrderCount?: string
}

// 获取详情返回参数类型
export interface GrouponFormState {
    groupType?: number
    name?: string
    startTime?: string
    endTime?: string
    expirationTime?: number
    grouponImage?: string
    pcGrouponImage?: string
    shareImage?: string
    grouponExplain?: string,
    shopIds?: number[],
    shops?: ShopFilterState[],
    memberRange?: number;
    memberMarks?: number[];
    sendUserTarget?: SendUserTarget;
}

export interface GrouponItemFilterParams {
    page: number;
    size: number;
    sortField?: string;
    keyword?: string;
    sortOrder?: string;
    grouponPromotionId?: number;
    productName?: string;
    brandId?: number;
    productGroupId?: number;
}

export interface GrouponItemProductFilterParams {
    page: number;
    size: number;
    sortField?: string;
    keyword?: string;
    sortOrder?: string;
    grouponPromotionId?: number;
    productName?: string;
    brandId?: number;
    productGroupId?: number;
}

export interface GrouponItemFilterResult {
    records: GrouponItemFilterState[];
    filter: {
        page: number;
    };
    total: number;
}

export interface GrouponItemProductSkus {
    skuId?: number;
    productId?: number;
    skuValue?: string;
    skuData?: string;
    skuSn?: string;
    skuStock?: number;
    skuTsn?: string;
    skuPrice?: number;
    marketPrice?: number;
    costPrice?: number;
    vendorProductSkuId?: number;
}

export interface GrouponItemProductFilterState {
    isAdded?: number;
    product: {
        productId?: number;
        productName?: string;
        productSn?: string;
        productStock?: number;
        productPrice?: number;
        marketPrice?: number;
        shippingTplId?: number;
        productStatus?: number;
        productType?: number;
        categoryId?: number;
        brandId?: number;
    },
    productSkus: GrouponItemProductSkus[]
}

export interface GrouponItemProductFilterResult {
    records: GrouponItemProductFilterState[];
    filter: {
        page: number;
    };
    total: number;
}

export interface GrouponItemFilterState {
    grouponPromotionItemId: number;
    productId?: number;
    groupNum?: string;
    sort?: number;
    productName?: string;
    productSn?: string;
    picThumb?: string;
    picUrl?: string;
    picLarge?: string;
    productPrice?: string;
    minGroupPrice?: string;
    maxGroupPrice?: string;
    minGroupNum?: string;
    maxGroupNum?: string;
    minLimitNum?: string;
    maxLimitNum?: string;
}

export interface groupNumState {
    groupNumKey?: number;
    groupNum?: number;
}

export interface groupPromotionDetailPriceState {
    groupNumKey?: number;
    price?: string | number;
}

export interface grouponPromotionDetailState {
    grouponDetailId?: number;
    productId?: number;
    skuId?: number;
    limitNum?: number;
    isJoin?: number;
    productPrice?: number;
    marketPrice?: number;
    productStock?: number;
    skuData?: string;
    groupPromotionDetailPrices: groupPromotionDetailPriceState[]
}

export interface GrouponItemFormState {
    grouponPromotionId?: number;
    grouponPromotionItemId?: number;
    productId?: number;
    groupType?: number;
    orderLimitNum?: number | string | undefined;
    isAutoGroup?: number;
    minGroupNum?: number;
    minGroupPrice?: number | string;
    sort?: number;
    groupNums?: groupNumState[];
    productSkus?: object;
    productPrice?: number;
    productStock?: number;
    grouponPromotionDetails: grouponPromotionDetailState[]
}

export interface GrouponRecordGroupMemberFilterParams {
    grouponPromotionRecordId?: number;
}

export interface GrouponRecordGroupMemberFilterState {
    joinType: number;// 1-开团，2-参团
    shopId: number;// 所属店铺
    addTime: number;// 创建时间
    username: string;// 用户名
    avatar: string;// 头像
    shopTitle: string;// 门店名称
}

export interface GrouponRecordGroupMemberFilterResult {
    records: GrouponRecordGroupMemberFilterState[];
    filter: {
        page: number;
    };
    total: number;
}

export interface GrouponRecordOpenJoinFilterParams {
    page: number;
    size: number;
    sortField?: string;
    keyword?: string;
    sortOrder?: string;
    grouponPromotionRecordId?: number;
    productName?: string;
    joinStatus?: number;// 1-待成团，2-拼团成功，3-拼团失败
    grouponPromotionId: number;// 拼团ID
}

export interface GrouponRecordOpenJoinFilterState {
    grouponPromotionRecordId: number;  // 拼团记录ID
    openGroupTime: number;// 开团时间
    productName: string;// 产品名称
    joinStatus: number;// 1-待成团，2-拼团成功，3-拼团失败
    groupCaptainName: string;// 团长名称
    joinExpirationTime: number;// 成团截止时间
    minGroupNum: number;// 成团最低人数
    // grouponPromotionItemId?: number
    // productId?: number
    // groupNum?: string
    // sort?: number
    // productName?: string
    // productSn?: string
    // picThumb?: string
    // productPrice?: string
    // minGroupPrice?: string
    // maxGroupPrice?: string
    // minGroupNum?: string
    // maxGroupNum?: string
    // minLimitNum?: string
    // maxLimitNum?: string
}

export interface GrouponRecordOpenJoinFilterResult {
    records: GrouponRecordOpenJoinFilterState[];
    filter: {
        page: number;
    };
    total: number;
}

export interface GrouponRecordOrderFilterParams {
    page: number;
    size: number;
    keyword?: string;
    grouponPromotionRecordId?: number;
    joinStatus?: number;
    grouponPromotionId: number;// 拼团ID
}

export interface GrouponRecordOrderFilterState {
    joinType: number;
    groupCaptainName: string;
    productName: string;
    orderSn: string;
    addTime: number;
    joinStatus: number;
    username: string;
    orderStatus: number;
}

export interface GrouponRecordOrderFilterResult {
    records: GrouponRecordOrderFilterState[];
    filter: {
        page: number;
    };
    total: number;
}

/**
 * 批量操作参数
 */
export interface GrouponItemBatchParams {
    ids: number[];
    type: string;
}
