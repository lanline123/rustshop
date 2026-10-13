
/**
 * ShopPickupConfigParam，自提设置项VO
 */
export interface pickupSettingData {
    pickupEndJson: PickupEndJson;
    /**
     * 提货有效期状态：0-禁用，1-启用
     */
    pickupEndStatus?: number;
    pickupTimeJson:  PickupTimeJson;
    /**
     * 自提时间状态：0-禁用，1-启用
     */
    pickupTimeStatus?: number;
    /**
     * 状态：0-禁用，1-启用
     */
    status?: number;
    /**
     * 完成备货状态：0-禁用，1-启用
     */
    stockingStatus?: number;
    [property: string]: any;
}

/**
 * PickupEndJson，提货有效期
 */
export interface PickupEndJson {
    /**
     * 天数
     */
    day?: number;
    /**
     * 小时
     */
    hour?: number;
    /**
     * 1 停止提货时间
     */
    type?: number;
    [property: string]: any;
}

/**
 * PickupTimeJson，自提时间
 */
export interface PickupTimeJson {
    timeList: TimeStartAndEnd[];
    /**
     * 1=每天的固定时间段
     */
    type?: number;
    [property: string]: any;
}

/**
 * TimeStartAndEnd，时间段
 */
export interface TimeStartAndEnd {
    /**
     * 结束时间
     */
    endTime?: string;
    /**
     * 开始时间
     */
    startTime?: string;
    [property: string]: any;
}