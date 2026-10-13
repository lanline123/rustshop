/**
 * @file 批发购物车相关类型（纯类型声明）
 *
 * 运行时常量与判定函数（WholesaleErrorCode / isWholesaleBusinessError）见
 * @/utils/wholesale —— .d.ts 文件不能包含运行时代码。
 *
 * - AddBatch*：批量加车类型，C 端当前阶段不调用
 *   保留供未来"批发商品列表批量勾选 → 一键加车"场景启用
 */

/**
 * 单加车失败项（循环加车收集，用于逐项 toast）
 */
export interface SingleAddFailedItem {
    productId: number;
    skuId: number;
    code: number;
    message: string;
}

/** ⚠️ 以下为批量加车类型，C 端当前阶段不调用 */
export interface AddBatchItem {
    productId: number;
    skuId: number;
    quantity: number;
}
export interface AddBatchFailedItem {
    productId: number;
    skuId: number;
    /** 业务错误码 */
    code: number;
    reason: string;
}
export interface AddBatchResult {
    successCount: number;
    failedCount: number;
    failedItems: AddBatchFailedItem[];
}
