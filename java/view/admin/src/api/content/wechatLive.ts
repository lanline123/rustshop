import request from "@/utils/request";
import type {
    WechatLiveGoodsState,
    WechatLivePromoteState,
    WechatLivePushUrlState,
    WechatLiveRoomFilterParams,
    WechatLiveRoomFilterResult,
    WechatLiveRoomState
} from "@/types/content/wechatLive.d";

/** 与后端 WechatLiveStatus 一致：101 直播中 … 107 已过期 */
const liveStatusMap: Record<number, string> = {
    101: "直播中",
    102: "未开始",
    103: "已结束",
    104: "禁播",
    105: "暂停",
    106: "异常",
    107: "已过期"
};

const auditStatusMap: Record<number, string> = {
    0: "未审核",
    1: "审核中",
    2: "审核通过",
    3: "审核驳回"
};

const normalizeWechatLiveRoom = (item: any): WechatLiveRoomState => {
    const liveStatus = Number(item.liveStatus ?? 102);
    const auditStatus = item.auditStatus !== undefined ? Number(item.auditStatus) : undefined;
    const roomId = Number(item.roomId ?? 0);

    return {
        wechatLiveId: Number(item.wechatLiveId ?? item.id ?? roomId),
        roomId,
        roomIdStr: String(item.roomIdStr ?? roomId),
        name: item.name || item.wechatLiveTitle || "",
        wechatLiveTitle: item.wechatLiveTitle || item.name || "",
        coverImg: item.coverImg || item.backgroundWall || "",
        shareImg: item.shareImg || item.shareCardCover || "",
        feedsImg: item.feedsImg || item.liveCardCover || "",
        shareCardCover: item.shareCardCover || item.shareImg || "",
        liveCardCover: item.liveCardCover || item.feedsImg || "",
        backgroundWall: item.backgroundWall || item.coverImg || "",
        anchorName: item.anchorName || "",
        anchorWechat: item.anchorWechat || item.anchorOpenid || "",
        anchorOpenid: item.anchorOpenid || item.anchorWechat || "",
        isOfficialFeed: Number(item.isOfficialFeed ?? item.isFeedsPublic ?? 0),
        isFeedsPublic: Number(item.isFeedsPublic ?? item.isOfficialFeed ?? 0),
        auditStatus,
        auditStatusText: item.auditStatusText || (auditStatus !== undefined ? auditStatusMap[auditStatus] || "-" : "-"),
        liveStatus,
        liveStatusText: item.liveStatusText || liveStatusMap[liveStatus] || "-",
        startTime: item.startTime || "",
        endTime: item.endTime || "",
        createTime: item.createTime || "",
        lastUpdateTime: item.lastUpdateTime || ""
    };
};

/**
 * 获取直播间列表（内容-小程序直播）
 */
export const getWechatLiveRoomList = (params: WechatLiveRoomFilterParams) => {
    return request<any>({
        url: "content/wechatLive/list",
        method: "get",
        params: {
            pageNum: params.page,
            pageSize: params.size,
            wechatLiveTitle: params.keyword,
            liveStatus: params.liveStatus,
            startTime: params.startTime,
            endTime: params.endTime
        }
    })
};

/**
 * 获取直播间详情
 */
export const getWechatLiveRoomDetail = (params: { id: number }) => {
    return request<any>({
        url: "content/wechatLive/detail",
        method: "get",
        params
    })
};

/**
 * 创建/更新直播间
 */
export const updateWechatLiveRoom = (operation: "create" | "update", data: object) => {
    return request<number | void>({
        url: `content/wechatLive/${operation}`,
        method: "post",
        data
    });
};

/**
 * 删除直播间
 */
export const delWechatLiveRoom = (data: { id: number }) => {
    return request({
        url: "content/wechatLive/delete",
        method: "post",
        data
    });
};

/**
 * 从小程序后台同步直播间信息
 */
export const syncWechatLiveRoom = () => {
    return request({
        url: "content/wechatLive/syncFromWechat",
        method: "get"
    });
};

/**
 * 更新直播间字段（如官方收录开关）
 */
export const updateWechatLiveRoomField = (data: { id: number; field: string; val: number | string }) => {
    const fieldPayloadMap: Record<string, Record<string, number | string>> = {
        isOfficialFeed: {
            isOfficialFeed: data.val,
            isFeedsPublic: data.val
        }
    };

    return updateWechatLiveRoom("update", {
        wechatLiveId: data.id,
        id: data.id,
        ...(fieldPayloadMap[data.field] || { [data.field]: data.val })
    });
};

/**
 * 获取推流地址
 */
export const getWechatLivePushUrl = (roomId: number) => {
    return request<WechatLivePushUrlState>({
        url: `content/wechatLive/pushUrl/${roomId}`,
        method: "get"
    });
};

/**
 * 获取直播间推广信息
 */
export const getWechatLivePromoteData = (roomId: number) => {
    return request<any>({
        url: `content/wechatLive/promote/${roomId}`,
        method: "get"
    }).then((result) => {
        return {
            roomId: Number(result.roomId ?? roomId),
            roomName: result.roomName || "",
            coverImg: result.coverImg || "",
            pagePath: result.pagePath || "",
            qrcodeUrl: result.qrcodeUrl || "",
            posterUrl: result.posterUrl || "",
            wechatShortLink: result.wechatShortLink || ""
        } as WechatLivePromoteState;
    });
};

/**
 * 同步直播间到微信
 */
export const syncWechatLiveToWechat = (data: { id: number }) => {
    return request({
        url: "content/wechatLive/syncToWechat",
        method: "post",
        params: data
    });
};

/**
 * 获取直播间商品列表
 */
export const getWechatLiveGoods = (roomId: number) => {
    return request<any[]>({
        url: `content/wechatLive/room/${roomId}`,
        method: "get"
    }).then((result) => {
        return (Array.isArray(result) ? result : []).map((item) => {
            const auditStatus = item.auditStatus !== undefined ? Number(item.auditStatus) : undefined;
            return {
                id: Number(item.id ?? 0),
                roomId: Number(item.roomId ?? roomId),
                wechatGoodsId: item.wechatGoodsId ? Number(item.wechatGoodsId) : undefined,
                productId: Number(item.productId ?? 0),
                goodsName: item.goodsName || "",
                coverImg: item.coverImg || "",
                price: item.price !== undefined ? Number(item.price) : undefined,
                priceType: item.priceType !== undefined ? Number(item.priceType) : undefined,
                url: item.url || "",
                auditStatus,
                auditStatusText: item.auditStatusText || (auditStatus !== undefined ? auditStatusMap[auditStatus] || "-" : "-"),
                sort: item.sort !== undefined ? Number(item.sort) : undefined,
                isShow: item.isShow !== undefined ? Number(item.isShow) : undefined
            } as WechatLiveGoodsState;
        });
    });
};

/**
 * 导入直播商品到主表
 */
export const importWechatLiveGoods = (data: {
    goodsItems?: Array<{ productId: number; productSkuId?: number }>;
    productIds?: number[];
}) => {
    return request({
        url: "content/wechatLiveGoods/import",
        method: "post",
        data
    });
};

/**
 * 添加已导入商品到直播间
 */
export const addWechatLiveGoods = (data: { roomId: number; goodsIds: number[] }) => {
    return request({
        url: "content/wechatLiveGoods/addToRoom",
        method: "post",
        data
    });
};

/**
 * 从直播间移除商品
 */
export const removeWechatLiveGoods = (data: { roomId: number; goodsId: number }) => {
    return request({
        url: "content/wechatLiveGoods/removeFromRoom",
        method: "post",
        params: data
    });
};
