import request from "@/utils/request";
import type { WechatLiveProductFilterParams, WechatLiveProductFilterResult } from "@/types/content/wechatLiveProduct.d";

const normalizeWechatLiveProduct = (item: any) => {
    return {
        id: Number(item.id || 0),
        wechatGoodsId: item.wechatGoodsId !== undefined ? Number(item.wechatGoodsId) : undefined,
        productId: Number(item.productId || 0),
        productSkuId: item.productSkuId !== undefined ? Number(item.productSkuId) : undefined,
        productName: item.productName || item.goodsName || "-",
        productImage: item.productImage || item.coverImg || "",
        price: item.price !== undefined ? Number(item.price) / 100 : 0,
        stock: item.stock !== undefined ? Number(item.stock) : undefined,
        liveRoomId: item.liveRoomId !== undefined ? Number(item.liveRoomId) : undefined,
        liveRoomName: item.liveRoomName || "",
        roomIdStr: item.roomIdStr || "",
        auditStatus: item.auditStatus !== undefined ? Number(item.auditStatus) : undefined,
        auditStatusText: item.auditStatusText || "",
        onSaleStatus: item.onSaleStatus !== undefined ? Number(item.onSaleStatus) : undefined,
        onSaleStatusText: item.onSaleStatusText || "",
        addTime: item.addTime || ""
    };
};

/**
 * 获取直播商品列表（内容-小程序直播-直播商品）
 */
export const getWechatLiveProductList = (params: WechatLiveProductFilterParams) => {
    return request<WechatLiveProductFilterResult>({
        url: "content/wechatLiveGoods/list",
        method: "get",
        params
    }).then((result: any) => {
        return {
            records: Array.isArray(result.records) ? result.records.map(normalizeWechatLiveProduct) : [],
            total: Number(result.total || 0)
        } as WechatLiveProductFilterResult;
    });
};

/**
 * 导入直播商品到主表
 */
export const importWechatLiveProduct = (data: {
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
 * 批量操作直播商品（如上架/下架/删除）
 */
export const batchSubmitWechatLiveProduct = (type: string, data: object) => {
    return request({
        url: "content/wechatLiveProduct/batch",
        method: "post",
        data: { type, ...data }
    });
};

/**
 * 更新直播商品字段
 */
export const updateWechatLiveProductField = (data: object) => {
    return request({
        url: "content/wechatLiveProduct/updateField",
        method: "post",
        data
    });
};
