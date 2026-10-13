// 直播间列表筛选参数
export interface WechatLiveRoomFilterParams {
    page: number;
    size: number;
    keyword?: string; // 直播间名称/房间号
    liveStatus?: number; // 101直播中 102未开始 103已结束 104禁播 105暂停 106异常 107已过期
    startTime?: string; // 创建时间开始
    endTime?: string; // 创建时间结束
    sortField?: string;
    sortOrder?: string;
}

// 列表返回
export interface WechatLiveRoomFilterResult {
    records: WechatLiveRoomState[];
    total: number;
}

// 直播间列表项
export interface WechatLiveRoomState {
    wechatLiveId?: number;
    roomId: number;
    name: string; // 直播间名称
    wechatLiveTitle?: string;
    liveType?: number; // 直播类型
    roomIdStr?: string; // 房间号（展示用）
    coverImg?: string; // 封面图
    shareImg?: string; // 分享图
    feedsImg?: string; // 官方收录封面
    shareCardCover?: string; // 分享卡片封面
    liveCardCover?: string; // 直播卡片封面
    backgroundWall?: string; // 直播背景墙
    anchorName?: string; // 主播昵称
    anchorWechat?: string; // 主播微信账号
    anchorOpenid?: string; // 主播微信openid
    subAnchorWechat?: string; // 副主播微信账号
    isOfficialFeed?: number; // 官方收录 0否 1是
    isFeedsPublic?: number; // 官方收录 0否 1是
    auditStatus?: number; // 审核状态
    auditStatusText?: string;
    liveStatus?: number; // 101直播中 102未开始 103已结束 104禁播 105暂停 106异常 107已过期
    liveStatusText?: string;
    startTime?: string; // 最近开播开始
    endTime?: string; // 最近开播结束
    createTime?: string;
    lastUpdateTime?: string;
}

// 推流地址信息
export interface WechatLivePushUrlState {
    roomId: number;
    pushUrl?: string;
    liveCode?: string;
    roomName?: string;
}

// 直播间推广信息
export interface WechatLivePromoteState {
    roomId: number;
    roomName: string;
    coverImg?: string;
    pagePath?: string;
    qrcodeUrl?: string;
    posterUrl?: string;
    wechatShortLink?: string;
}

// 直播间商品信息
export interface WechatLiveGoodsState {
    id: number;
    roomId: number;
    wechatGoodsId?: number;
    productId: number;
    goodsName: string;
    coverImg?: string;
    price?: number;
    priceType?: number;
    url?: string;
    auditStatus?: number;
    auditStatusText?: string;
    sort?: number;
    isShow?: number;
}
