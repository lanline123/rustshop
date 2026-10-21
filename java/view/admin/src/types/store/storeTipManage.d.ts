export interface RequestList {
    /**
     * 查询参数
     */
    keyword?: string;
    /**
     * 当前页码
     */
    page?: number;
    /**
     * 分页大小
     */
    size?: number;
    /**
     * 排序字段
     */
    sortField?: string;
    /**
     * desc|asc
     * 显示顺序
     */
    sortOrder?: string;
    /**
     * 0 禁用，1 启用
     */
    status?: number | string;
    [property: string]: any;
}

export interface Response {
    records?: Record[];
    size?: number;
    total?: number;
    [property: string]: any;
}
/**
 * 区域门店管理VO
 */
export interface Record {
    /**
     * 添加时间
     */
    addTime?: string;
    /**
     * 状态
     */
    status?: number;
    /**
     * id
     */
    tipId?: number;
    /**
     * 名称
     */
    tipName?: string;
    [property: string]: any;
}

/**
 * AreaStoreManagerCreateParam，区域门店管理创建参数
 */
export interface RequestInfo {
    tipName: string;
    status: number;
}

