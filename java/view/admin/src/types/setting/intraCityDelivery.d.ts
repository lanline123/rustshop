/**
 * AmapAllVO，地图设置参数
 */
export interface CityDeliveryResponse {
    /**
     * 高德地图Key
     */
    amapJsKey?: string;
    /**
     * 高德地图SECRET
     */
    amapJsSecret?: string;
    /**
     * 高德地图Key
     */
    amapMiniKey?: string;
    /**
     * 高德地图SECRET
     */
    amapMiniSecret?: string;
    /**
     * 高德地图Key
     */
    amapWebKey?: string;
    /**
     * 高德地图SECRET
     */
    amapWebSecret?: string;
    [property: string]: any;
}

// ===== 平台同城配送总控配置（文档 4.3，GET/SAVE 字段一致） =====
export interface IntraCityPlatformConfig {
    /** 同城配送功能总开关 0/1（默认 1） */
    intraCityDeliveryEnabled?: number;
    /** 启用商家自配送（自建配送员运力）0/1（默认 1） */
    intraCitySelfDeliveryEnabled?: number;
    /** 启用第三方配送（美团等第三方运力）0/1（默认 0） */
    intraCityThirdPartyDeliveryEnabled?: number;
    [property: string]: any;
}

// ===== 距离溢价规则 =====
export interface DistancePricingRule {
    shopLocalDeliveryDistancePricingId?: number;
    shopLocalDeliveryConfigId?: number;
    /** 超过多少公里 */
    distanceThreshold: number;
    /** 每增加多少公里 */
    distanceUnit: number;
    /** 配送增加多少元 */
    additionalFee: number;
    /** 排序 */
    sortOrder?: number;
}

// ===== 同城配送配置详情VO（后端返回） =====
export interface ShopLocalDeliveryConfigDetailVO {
    shopLocalDeliveryConfigId?: number;
    shopId?: number;
    /** 是否启用同城配送：0-禁用，1-启用 */
    enabled: number;
    /** 配送半径（公里） */
    deliveryRadius?: number;
    /** 起送价（元） */
    minOrderAmount?: number;
    /** 基础运费（元） */
    baseDeliveryFee?: number;
    /** 是否开启叠加溢价：0-关闭，1-开启 */
    distancePricingEnabled?: number;
    /** 派单模式：1-抢单模式，2-手动派单 */
    orderDispatchMode?: number;
    /** 派单模式名称 */
    orderDispatchModeName?: string;
    /** 是否开启取货时拍照：0-关闭，1-开启 */
    pickupPhotoEnabled?: number;
    /** 是否开启配送完成拍照：0-关闭，1-开启 */
    deliveryCompletePhotoEnabled?: number;
    /** 距离叠加溢价配置列表 */
    distancePricingList?: DistancePricingRule[];
}

// ===== 同城配送配置保存参数（前端提交） =====
export interface ShopLocalDeliveryConfigSaveParam {
    /** 是否启用同城配送：0-禁用，1-启用 */
    enabled: number;
    /** 配送半径（公里） */
    deliveryRadius?: number;
    /** 起送价（元） */
    minOrderAmount?: number;
    /** 基础运费（元） */
    baseDeliveryFee?: number;
    /** 是否开启叠加溢价：0-关闭，1-开启 */
    distancePricingEnabled?: number;
    /** 派单模式：1-抢单模式，2-手动派单 */
    orderDispatchMode?: number;
    /** 是否开启取货时拍照：0-关闭，1-开启 */
    pickupPhotoEnabled?: number;
    /** 是否开启配送完成拍照：0-关闭，1-开启 */
    deliveryCompletePhotoEnabled?: number;
    /** 距离叠加溢价配置列表 */
    distancePricingList?: DistancePricingRule[];
}

// ===== 自营门店信息 =====
export interface SelfShopDetailResponse {
    storeSelfOperatedName: string;
    storeSelfOperatedAddress: string;
    storeSelfOperatedRegion: string;
    storeSelfOperatedLogo: string;
    storeSelfOperatedLongitude: string;
    storeSelfOperatedLatitude: string;
    storeSelfOperatedRegions: string[];
    storeSelfOperatedPhone: string;
    /** 门头封面 */
    storeSelfOperatedCover?: string;
    /** 门店公告 */
    storeSelfOperatedDescription?: string;
    /** 营业状态：1=开业，4=暂停运营，10=关店 */
    storeSelfOperatedStatus?: number;
    /** 门店标签 tipId 列表 */
    shopTips?: number[];
    /** 联系方式配置 */
    shopContactConfig?: any[];
    /** 营业时间配置 */
    shopOpenCloseConfig?: any;
    /** 门店标签详情（回显用） */
    tips?: { tipId: number; tipName: string }[];
}

// ===== 自营门店保存入参 =====
export interface SelfShopSaveParam {
    storeSelfOperatedName?: string;
    storeSelfOperatedLogo?: string;
    storeSelfOperatedRegion?: string;
    storeSelfOperatedAddress?: string;
    storeSelfOperatedLongitude?: string;
    storeSelfOperatedLatitude?: string;
    storeSelfOperatedPhone?: string;
    storeSelfOperatedCover?: string;
    storeSelfOperatedDescription?: string;
    storeSelfOperatedStatus?: number;
    shopContactConfig?: any[];
    shopOpenCloseConfig?: any;
    shopTips?: number[];
}
