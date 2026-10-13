// 直播商品列表筛选参数
export interface WechatLiveProductFilterParams {
    page: number;
    size: number;
    keyword?: string; // 商品名称/商品ID
    liveRoomId?: number; // 直播间ID
    auditStatus?: number; // 审核状态
    onSaleStatus?: number; // 上架状态
    sortField?: string;
    sortOrder?: string;
}

// 列表返回
export interface WechatLiveProductFilterResult {
    records: WechatLiveProductState[];
    total: number;
}

// 直播商品列表项
export interface WechatLiveProductState {
    id: number;
    wechatGoodsId?: number; // 微信商品ID
    productId: number; // 商品ID
    productSkuId?: number; // 商品SKU ID
    productName: string; // 商品名称
    productImage?: string; // 商品图片
    price?: number; // 商品价格
    stock?: number; // 库存
    liveRoomId?: number; // 直播间ID
    liveRoomName?: string; // 直播间名称
    roomIdStr?: string; // 房间号
    auditStatus?: number; // 审核状态 0待审核 1审核中 2审核通过 3审核驳回
    auditStatusText?: string;
    onSaleStatus?: number; // 上架状态 0下架 1上架
    onSaleStatusText?: string;
    addTime?: string; // 添加时间
}
