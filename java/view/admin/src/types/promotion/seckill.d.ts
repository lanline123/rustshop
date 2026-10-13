import type { SendUserTarget } from "@/types/user/memberAudience";

// 列表查询时筛选参数类型
export interface SeckillFilterParams {
    page: number;
    size: number;
    sortField: string,
    sortOrder: string,
    keyword?: string;
}

// 获取列表返回参数类型
export interface SeckillFilterResult {
    records: SeckillFilterState[];
    filter: {
        page: number;
    };
    total: number;
}
export interface SeckillFilterState {
    statusName?: string
    seckillId?: number
    seckillName?: string
    seckillStartTime?: string
    seckillEndTime?: string
    seckillLimitNum?: number
    productId?: number
    productName?: string
}


// 装修预览接口返回的活动信息（listForHomePreview，与 C 端首页同口径）
export interface SeckillPreviewActivity {
    seckillId?: number;
    seckillName?: string;
    seckillStartTime?: string;
    seckillEndTime?: string;
    seckillLimitNum?: number;
    productId?: number;
    shopId?: number;
}

// 装修预览接口返回的秒杀商品行（listForHomePreview，与 C 端首页同口径）
export interface SeckillPreviewState {
    seckillId?: number;
    productId?: number;
    productName?: string;
    productSn?: string;
    picThumb?: string;
    picUrl?: string;
    picLarge?: string;
    // 服务端 getSeckillProductList 存在 productPrice / marketPrice 交叉赋值，
    // 划线原价须按 C 端口径取 productPrice ?? marketPrice，不能按字段名取 marketPrice
    productPrice?: string | number;
    marketPrice?: string | number;
    seckillPrice?: string | number;
    orgProductPrice?: string | number;
    skuId?: number;
    skuSn?: string;
    seckillStock?: number;
    productStock?: number;
    seckkillData?: SeckillPreviewActivity;
}

// 装修预览接口返回的分页结构
export interface SeckillPreviewResult {
    records: SeckillPreviewState[];
    total: number;
    size: number;
    current: number;
    pages: number;
}

// 获取详情返回参数类型
export interface SeckillFormState {
    statusName?: string
    seckillId?: number
    seckillName?: string
    seckillStartTime?: string
    seckillEndTime?: string
    seckillLimitNum?: number
    productId?: number
    productName?: string
    picThumb?: string
    seckillItem:SeckillProductState[]
    memberRange?: number;
    memberMarks?: number[];
    sendUserTarget?: SendUserTarget;
}

export interface SeckillProductState {
    skuId: number
    productId: number
    picThumb: string
    productName: string
    tipValue: string
    skuValue: string
    skuData: SkuState[]
    skuSn: string
    skuStock: number
    skuTsn: string
    skuPrice: string
    secondsSeckill: number;
    seckillPrice: string;
    productPrice: string;
    seckillStock: number;
    productStock: number;
}
export  interface SkuState{
    name:string;
    value:string;
}
