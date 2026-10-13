export interface ReservationTplFilterParams {
    page: number;
    size: number;
    sortField?: string;
    sortOrder?: string;
    keyword?: string;
}

export interface ReservationTplFilterResult {
    records?: ReservationTplFilterState[];
    data?: ReservationTplFilterState[];
    filter: {
        page: number;
    };
    total: number;
}

export interface ReservationTplFilterState {
    /**
     * 需提前预约的天数，仅当reservation_type=1时有效
     */
    advanceNumber?: number | string;
    /**
     * 是否默认
     */
    isDefault?: number;
    /**
     * 最长允许预约天数
     */
    maxDays?: number | string;
    advanceUnit?: "d" | "h";
    /**
     * 预约字段配置详情
     */
    reservationTplDetailDTOs?: ReservationTplDetailDTO[];
    /**
     * 预约模板ID
     */
    reservationTplId?: number;
    /**
     * 预约时间类型：0=无需提前,1=需提前N天,2=根据下单时间配置
     */
    reservationType?: number;
    /**
     * 店铺id
     */
    shopId?: number;
    /**
     * 模板名称
     */
    templateName?: string;
    [property: string]: any;
}

export interface ReservationTplDetailDTO {
    /**
     * 字段自定义名称
     */
    customName: string;
    /**
     * 字段名称
     */
    fieldName: string;
    /**
     * 是否必填,1表示必填，0非必填
     */
    isRequired: number;
    /**
     * 是否显示,1表示展示,0不展示
     */
    isShow: number;
    [property: string]: any;
}
