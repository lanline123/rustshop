import type { WholesaleDimension } from "./buyRule";

/**
 * 行 / 组级批发进度提示对象（详情页前端派生使用）
 * @description
 * - 后端 cart/order 接口当前阶段只下发 string 文案，此类型仅供详情页 useWholesaleOrder 派生 aggregateTip 使用
 * - dimension/status 用于 TipBar 渲染色块
 */
export interface WholesaleCartTip {
    productId: number;
    /** SKU 维度时存在 */
    skuId?: number;
    /** 起批维度（1=SPU / 2=SKU） */
    dimension: WholesaleDimension;
    /** 当前数量（SPU=合计；SKU=该规格） */
    currentQty: number;
    /** 起订数量 */
    minOrderQty: number;
    /** 差额（>0 表示还差） */
    gap: number;
    /** 倍数；为空表示未配置或 multiple=1 */
    step?: number;
    /** 是否满足下单门槛 */
    status: "PENDING" | "OK";
    /** 阶梯价进度文案（可选） */
    ladderTip?: string;
    /** 标准化整句文案，前端可直接展示 */
    message: string;
}
