/**
 * @file 批发价阶梯组校验（通用售价 / 类型售价 / 批量设置 共用）
 * @description 纯函数：对一组阶梯做「数量 + 折扣/价格」校验，命中错误通过 mergeError 画到行内，
 *              并返回本组第一个错误（含完整文案 + 行号 + 字段），供 toast 与滚动定位使用。
 */
import type { Ladder, GradeLadder, LadderError } from "@/types/wholesale/price";

/** 判定表单字段是否「空」（TigInput 空值可能为 undefined / "" / NaN） */
export const isEmpty = (v: unknown): boolean => v === undefined || v === null || v === "" || (typeof v === "number" && Number.isNaN(v));

/** 单档阶梯是否完全未填（qty、value 均为空） */
export const isLadderEmpty = (l: Ladder): boolean => isEmpty(l.qty) && isEmpty(l.value);

/** 整组阶梯是否视为未配置（无行或各行均未填） */
export const isLadderGroupEmpty = (ladders: Ladder[]): boolean => !ladders?.length || ladders.every(isLadderEmpty);

/** SKU 是否已配置至少一个等级的类型售价 */
export const hasConfiguredGradeLadders = (gradeLadders: GradeLadder[]): boolean => gradeLadders.some((g) => g.ladders.length > 0);

/** 本组第一个错误：完整文案（含第 N 档，供 toast）+ 行号 + 字段（供滚动定位） */
export type FirstLadderErr = { msg: string; index: number; field: "qty" | "value" };

/**
 * 校验一组阶梯；mergeError 把错误画到行内（文案精简）。
 * 规则与优先级：
 *   - qty：①请填写 ②≥1 整数 ③严格递增不重复
 *   - value：①请填写 ②合法范围（折扣 0.10~9.99 / 固定价 >0 且 ≤售价）③严格递减（越往后越优惠）
 * @returns 第一个错误；无错返回 null
 */
export const validateLadderGroup = (
    ladders: Ladder[],
    priceMode: "discount" | "fixed",
    retailPrice: number,
    mergeError: (index: number, patch: LadderError) => void
): FirstLadderErr | null => {
    let first: FirstLadderErr | null = null;
    const record = (msg: string, index: number, field: "qty" | "value") => {
        if (!first) first = { msg, index, field };
    };
    let lastQty = 0;
    let lastValidValue: number | null = null;
    ladders.forEach((l, i) => {
        const valueLabel = priceMode === "discount" ? "折扣" : "价格";
        const qty = isEmpty(l.qty) ? undefined : Number(l.qty);
        const value = isEmpty(l.value) ? undefined : Number(l.value);
        // qty 校验：空 → 范围 → 递增
        if (qty === undefined || Number.isNaN(qty)) {
            mergeError(i, { qty: "请填写数量" });
            record(`第 ${i + 1} 档请填写数量`, i, "qty");
        } else if (qty < 1 || !Number.isInteger(qty)) {
            mergeError(i, { qty: "数量需为≥1的整数" });
            record(`第 ${i + 1} 档数量需为≥1的整数`, i, "qty");
        } else if (qty <= lastQty) {
            mergeError(i, { qty: "档位数量需递增且不重复" });
            record(`第 ${i + 1} 档数量需递增且不重复`, i, "qty");
            lastQty = qty;
        } else {
            lastQty = qty;
        }
        // value 基础校验：空 → 范围
        let valueOk = false;
        if (value === undefined || Number.isNaN(value)) {
            mergeError(i, { value: `请填写${valueLabel}` });
            record(`第 ${i + 1} 档请填写${valueLabel}`, i, "value");
        } else if (priceMode === "discount") {
            if (!(value >= 0.1 && value <= 9.99)) {
                mergeError(i, { value: "折扣需在0.10~9.99之间" });
                record(`第 ${i + 1} 档折扣需在0.10~9.99之间`, i, "value");
            } else {
                valueOk = true;
            }
        } else {
            if (!(value > 0)) {
                mergeError(i, { value: "批发价需大于0" });
                record(`第 ${i + 1} 档批发价需大于0`, i, "value");
            } else if (retailPrice > 0 && value > retailPrice) {
                mergeError(i, { value: `批发价不能高于售价 ${retailPrice.toFixed(2)}` });
                record(`第 ${i + 1} 档批发价不能高于售价 ${retailPrice.toFixed(2)}`, i, "value");
            } else {
                valueOk = true;
            }
        }
        // value 递减校验：仅在基础合法时比较，且只跟「上一个合法档位」比较（避免上一档非法导致连锁报错）
        if (valueOk && lastValidValue !== null && value !== undefined && value >= lastValidValue) {
            mergeError(i, { value: `${valueLabel}需小于上一档` });
            record(`第 ${i + 1} 档${valueLabel}需小于上一档`, i, "value");
        }
        if (valueOk && value !== undefined) lastValidValue = value;
    });
    return first;
};
