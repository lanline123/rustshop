/**
 * 拼团模块共享类型定义（移植自 Vort-Admin _shared/groupon）
 * 说明：统一 PC 首页拼团等模块的通用类型
 */

/**
 * 拼团活动项（选择弹窗返回值、模块配置项通用）
 */
export interface GrouponActivityItem {
    /** 拼团活动 ID（promotionId） */
    grouponPromotionId: string;
    /** 活动名称 */
    name: string;
    /** 活动开始时间 */
    startTime?: string;
    /** 活动结束时间 */
    endTime?: string;
    /** 拼团类型：1=普通团, 2=阶梯团 */
    groupType?: number;
}

/**
 * 拼团商品项（预览/展示用，归一化后的字段结构）
 */
export interface GrouponProductItem {
    /** 商品 ID */
    productId: number | string;
    /** 商品名称 */
    productName: string;
    /** 商品主图 */
    picUrl: string;
    /** 商品缩略图 */
    picThumb: string;
    /** 大图 */
    picLarge: string;
    /** 商品原价 */
    productPrice: string;
    /** 拼团价格 */
    minGroupPrice: string;
    /** 成团人数 */
    minGroupNum: number | string;
    /** 活动开始时间（可选，部分场景需要） */
    startTime?: string;
    /** 活动结束时间（可选，部分场景需要） */
    endTime?: string;
}
