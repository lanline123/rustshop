import { asyncRequest } from "@/utils/request";

/**
 * 批量加车单项入参（保留类型，便于未来「列表批量勾选 → 一键加车」复用）
 */
export interface WholesaleAddBatchItem {
    productId: number;
    skuId: number;
    quantity: number;
}

/**
 * 批量加车失败明细（部分失败不回滚）
 */
export interface WholesaleAddBatchFailedItem {
    productId: number;
    skuId: number;
    code: number;
    reason: string;
}

export interface WholesaleAddBatchResult {
    successCount: number;
    failedCount: number;
    failedItems: WholesaleAddBatchFailedItem[];
}

/**
 * 批量加车（批发）— 本期前端不调用
 * @description
 * - 后端已实现 `POST /api/wholesale/cart/addBatch`，但当前阶段前端不接入
 * - 详情页加车走「循环调零售 /api/cart/add」（见 hooks/useWholesaleOrder.doAddLoop）
 * - 端点保留供未来「批发商品列表批量勾选 → 一键加车」场景复用
 */
export const addWholesaleCartBatch = (items: WholesaleAddBatchItem[]) =>
    asyncRequest<WholesaleAddBatchResult>({
        url: "wholesale/cart/addBatch",
        method: "post",
        data: { items }
    });
