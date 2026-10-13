// ==================== 列表相关类型 ====================

export interface LotteryActivityListParams {
    page: number;
    size: number;
    activityName?: string;
    status?: number;
}

export interface LotteryActivityListItem {
    activityId: number;
    activityName: string;
    status: number; // 0-未开始 1-进行中 2-已结束
    statusText: string;
    participantCount: number;
    startTime: string;
    endTime: string;
    createTime: string;
    canEdit: boolean;
    canDelete: boolean;
}

// ==================== 装修配置类型 ====================

export interface DecorateConfig {
    bgImage: string;
    bgColor: string;
    prizeEntryImage: string;
    showWinnerList: number; // 0-否 1-是
    activityRules: string;
}

// ==================== 九宫格配置类型 ====================

export interface GridPosition {
    position: number; // 1-8
    tempIndex?: number | null; // 关联奖品的临时索引（与 prizeList 中的 tempIndex 对应）
    probability: number; // 0-100
    prizeName?: string;
    prizeImage?: string;
    prizeType?: number;
    prizeValue?: string;
}

// ==================== 奖品相关类型 ====================

// 接口返回的奖品类型
export interface LotteryPrize {
    prizeId: number;
    activityId: number;
    prizeName: string;
    prizeType: number; // 1-积分 2-商品 3-优惠券
    prizeTypeText?: string;
    prizeValue: string;
    prizeImage: string;
    prizeStock: number;
    prizeStockUsed?: number;
    remainStock?: number;
    createTime?: string;
}

// 前端表单使用的奖品类型（统一使用 tempIndex 关联九宫格）
export interface PrizeItem {
    /** 临时索引：用于与九宫格关联（加载时把后端的 prizeId 赋值给 tempIndex，新增时由 Step2Awards 分配） */
    tempIndex?: number;
    /** 后端奖品ID：编辑活动时用于回传给后端做更新（新增奖品没有该字段） */
    prizeId?: number;
    /** 奖品名称 */
    prizeName: string;
    /** 奖品类型：1-积分 2-商品 3-优惠券 */
    prizeType: number;
    /** 奖品值(积分数量/商品ID/优惠券ID) */
    prizeValue: string;
    /** 奖品图片URL */
    prizeImage: string;
    /** 奖品库存(必须大于0) */
    prizeStock: number;
    /** 已发放(可选，用于展示/回填) */
    prizeStockUsed?: number;

    // ===== 以下为前端展示/回填用的可选扩展字段（不属于接口必填字段） =====
    couponType?: number;
    couponMoney?: number;
    couponDiscount?: number;
    productName?: string;
    productSn?: string;
    productPrice?: number;
    picThumb?: string;
    /** 商品规格ID */
    skuId?: number;
    /** 商品规格文本（用于展示） */
    skuText?: string;
}

/** @deprecated 使用 PrizeItem 替代 */
export type AwardItem = PrizeItem;

// ==================== 活动详情类型 ====================

export interface LotteryActivityDetail {
    activityId: number;
    activityName: string;
    startTime: string;
    endTime: string;
    costType?: number; // 0-无限制(免费) 1-消耗积分
    costPoints: number;
    dailyLimit: number;
    totalLimit: number;
    winLimit: number;
    status: number;
    statusText?: string;
    decorateConfig: DecorateConfig;
    prizeList: LotteryPrize[]; // 奖品列表
    gridConfig: GridPosition[];
    noWinName: string;
    noWinImage: string;
    noWinTips: string;
    participantCount?: number;
    winCount?: number;
    createTime?: string;
}

// ==================== 表单状态类型 ====================

export interface LotteryFormState {
    activityId?: number;
    activityName: string;
    timeRange: string[]; // 前端辅助字段
    startTime: string;
    endTime: string;
    costPoints: number;
    dailyLimit: number;
    totalLimit: number;
    winLimit: number;
    // 前端辅助控制字段
    winLimitType: number; // 0-无限制 1-有限制
    drawLimitType: number; // 0-无限制 1-有限制
    isConsumePoints: number; // 0-否 1-是
    // 奖品列表
    prizeList: PrizeItem[];
    // 九宫格配置
    gridConfig: GridPosition[];
    // 未中奖配置
    noWinName: string;
    noWinImage: string;
    noWinTips: string;
    // 装修配置
    decorateConfig: DecorateConfig;
}

// ==================== 提交参数类型 ====================

/** 奖品项参数 - 用于创建/编辑活动时提交（只传 tempIndex，后端会创建 prizeId） */
export interface LotteryPrizeItemParam {
    /** 临时索引（必填，用于与九宫格关联） */
    tempIndex: number;
    /** 奖品ID（编辑活动时回传；新增活动/新增奖品可不传） */
    prizeId?: number;
    /** 奖品名称 */
    prizeName: string;
    /** 奖品类型: 1-积分, 2-商品, 3-优惠券 */
    prizeType: number;
    /** 奖品值(积分数量/商品ID/优惠券ID) */
    prizeValue: string;
    /** 奖品图片URL */
    prizeImage: string;
    /** 奖品库存(必须大于0) */
    prizeStock: number;
}

/** 九宫格位置参数 - 用于创建/编辑活动时提交 */
export interface GridPositionParam {
    /** 位置(1-8) */
    position: number;
    /** 临时索引（关联奖品时使用，与 prizeList 中的 tempIndex 对应） */
    tempIndex?: number | null;
    /** 中奖概率(0-100) */
    probability: number;
}

// ==================== 兼容旧类型（逐步废弃） ====================

/** @deprecated 使用 LotteryActivityListParams 替代 */
export type LuckyWheelFilterParams = LotteryActivityListParams;

/** @deprecated 使用 LotteryActivityListItem 替代 */
export type LuckyWheelFilterState = LotteryActivityListItem;

/** @deprecated 使用 LotteryFormState 替代 */
export type LuckyWheelFormState = LotteryFormState;

/** @deprecated 使用 GridPosition 替代 */
export interface WheelSector {
    position: number;
    award_pool_id: number | null;
    probability: number;
}

/** @deprecated 使用直接字段替代 */
export interface MissConfig {
    name: string;
    image: string;
    prompt_text: string;
}

/** @deprecated 使用 DecorateConfig 替代 */
export interface DecorationConfig {
    background_image: string;
    background_color?: string;
    show_winner_list?: number;
    rule_text: string;
}
