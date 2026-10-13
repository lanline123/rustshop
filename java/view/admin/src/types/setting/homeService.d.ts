/**
 * 预约上门服务配置类型定义
 */

// ===== 距离溢价规则 =====
export interface HomeServiceDistancePricingRule {
    shopHomeServiceDistancePricingId?: number;
    shopHomeServiceConfigId?: number;
    /** 超过多少公里 */
    distanceThreshold: number;
    /** 每增加多少公里 */
    distanceUnit: number;
    /** 上门费增加多少元 */
    additionalFee: number;
    /** 排序 */
    sortOrder?: number;
}

// ===== 上门服务配置详情VO（后端返回） =====
export interface ShopHomeServiceConfigDetailVO {
    shopHomeServiceConfigId?: number;
    shopId?: number;
    /** 是否启用预约上门服务：0-禁用，1-启用 */
    enabled: number;
    /** 服务半径（公里） */
    serviceRadius?: number;
    /** 上门费（元） */
    homeFee?: number;
    /** 是否开启距离溢价：0-关闭，1-开启 */
    distancePricingEnabled?: number;
    /** 派单模式：1-抢单模式，2-手动派单 */
    orderDispatchMode?: number;
    /** 派单模式名称 */
    orderDispatchModeName?: string;
    /** 是否开启服务前拍照：0-关闭，1-开启 */
    beforeServicePhotoEnabled?: number;
    /** 是否开启服务后拍照：0-关闭，1-开启 */
    afterServicePhotoEnabled?: number;
    /** 距离溢价配置列表 */
    distancePricingList?: HomeServiceDistancePricingRule[];
}

// ===== 保存上门服务配置参数 =====
export interface ShopHomeServiceConfigSaveParam {
    /** 是否启用预约上门服务：0-禁用，1-启用 */
    enabled: number;
    /** 服务半径（公里） */
    serviceRadius?: number;
    /** 上门费（元） */
    homeFee?: number;
    /** 是否开启距离溢价：0-关闭，1-开启 */
    distancePricingEnabled?: number;
    /** 派单模式：1-抢单模式，2-手动派单 */
    orderDispatchMode?: number;
    /** 是否开启服务前拍照：0-关闭，1-开启 */
    beforeServicePhotoEnabled?: number;
    /** 是否开启服务后拍照：0-关闭，1-开启 */
    afterServicePhotoEnabled?: number;
    /** 距离溢价配置列表 */
    distancePricingList?: HomeServiceDistancePricingRule[];
}

