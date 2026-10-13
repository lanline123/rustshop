/**
 * @file 购物车批发 hook（薄壳）
 * @description
 * 仅负责购物车页「批发行判定」与「结算起批/倍数校验」两件事；
 * 单行改量（倍数/吸附/步进/删除）已下沉到 `WholesaleCartStepper` 组件（内部复用 useWholesaleQuantity）。
 */
import { useWholesaleQuantity } from "@/hooks/useWholesaleQuantity";
import { sumWholesaleSpuQuantities, wholesaleSpuScopeKey } from "@/utils/wholesaleContract";

/** 购物车批发行最小入参（仅约束本 hook 用到的字段） */
export interface WholesaleCartLine {
    cartId: number;
    shopId?: number;
    productId?: number;
    skuId?: number;
    quantity?: number;
    stock?: number;
    isChecked?: boolean;
    /** 批发身份（稳定）：是否批发商品，用于控件分流/数量逻辑判定 */
    wholesaleScope?: boolean;
    /** 当前是否按批发价成交（动态，勿用于分流） */
    isWholesaleLine?: boolean;
    wholesaleMoq?: number;
    wholesaleMoqDimension?: 1 | 2;
    wholesaleMultiple?: number;
}

export const useWholesaleCart = () => {
    const { t } = useI18n();
    const qty = useWholesaleQuantity();

    /**
     * 该行是否走批发改量逻辑（控件分流的唯一依据）
     * - 用稳定的 `wholesaleScope`（批发商品身份）判定，**不用** `isWholesaleLine`
     * - 否则数量减到不达阶梯时 `isWholesaleLine` 翻 false，会错误退回零售 ±1 逻辑
     */
    const isWholesaleLine = (line?: WholesaleCartLine | null): boolean => !!line?.wholesaleScope;

    /**
     * 结算前本地校验：只校验「倍数」与「起批量」，**不读后端 `wholesaleHint`**，文案前端自生成
     * - 倍数（按 SKU）：quantity 必须是 wholesaleMultiple 整数倍
     * - 起批量：dimension=2(SKU) 逐行 quantity >= moq；dimension=1(SPU) 同 productId 勾选行合计 >= moq
     * - 返回首条不满足的提示，全部满足返回 null
     * @param checkedLines 已勾选的购物车行（含零售行，内部自行过滤批发行）
     */
    const validateCheckout = (checkedLines: WholesaleCartLine[]): string | null => {
        const wholesale = (checkedLines || []).filter(isWholesaleLine);
        if (!wholesale.length) return null;
        // ① 倍数校验
        for (const l of wholesale) {
            const m = l.wholesaleMultiple ?? 1;
            if (!qty.isMultipleValid(l.quantity || 0, m)) return t("商品需按{0}的倍数购买", [m]);
        }
        // ② 起批量校验
        const spuSum = sumWholesaleSpuQuantities(wholesale);
        for (const l of wholesale) {
            const moq = l.wholesaleMoq ?? 0;
            if (moq <= 0) continue;
            const cur = l.wholesaleMoqDimension === 1 && l.productId != null ? (spuSum[wholesaleSpuScopeKey(l)] ?? 0) : l.quantity || 0;
            if (!qty.isMoqMet(cur, moq)) return t("未达到起批量");
        }
        return null;
    };

    return { isWholesaleLine, validateCheckout };
};
