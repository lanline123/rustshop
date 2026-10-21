/**
 * @file 批发数量核心（单一真理源）
 * @description
 * 集中实现批发「倍数 / 起批量 / 吸附 / 步进」的纯计算逻辑，供多处复用，消除重复：
 * - 购物车改量步进器 `WholesaleCartStepper`
 * - 结算起批/倍数校验 `useWholesaleCart.validateCheckout`
 *
 * 关键口径（与总纲 §5.1.1 / §5.1 对齐）：
 * - 合法值集合：{0} ∪ {multiple 整数倍}；0 为合法特例
 * - 步长 = multiple（≥1，=1 退化普通 ±1）
 * - 点 +/-：先把当前值吸附到最近合法倍数，再 ±step（解决"旧倍数遇新倍数"问题）
 * - 起批量（moq）不在步进层强制，仅作判定函数供结算/校验调用
 */

export const useWholesaleQuantity = () => {
    /** 步长 = multiple（≥1） */
    const stepOf = (multiple?: number | null): number => Math.max(1, multiple ?? 1);

    /**
     * 就近吸附到最近合法倍数
     * - raw<=0 → 0；超 max 向下取不超过 max 的最大合法倍数
     * - 不强制起批量下限（moq 在校验层处理）
     */
    const snapToValid = (raw: number, step: number, max?: number | null): number => {
        if (!Number.isFinite(raw) || raw <= 0) return 0;
        const s = Math.max(1, step);
        let v = Math.max(1, Math.round(raw / s)) * s;
        if (max != null && v > max) {
            const downK = Math.floor(max / s);
            v = downK >= 1 ? downK * s : 0;
        }
        return Math.max(0, v);
    };

    /**
     * 点「+」目标值；无法再增加（库存不足/售罄）返回 null
     * - 0 → 一个 step（首档不跳到 moq）
     * - n → 先吸附 n 到合法倍数，再 +step；超 max 取不超过 max 的最大合法倍数
     */
    const plusTarget = (value: number, multiple?: number | null, max?: number | null): number | null => {
        const s = stepOf(multiple);
        if (max != null && max <= 0) return null;
        if (value <= 0) {
            const target = s;
            if (max != null && target > max) return null;
            return target;
        }
        const base = snapToValid(value, s, max);
        let next = base + s;
        if (max != null && next > max) {
            next = snapToValid(max, s, max);
            if (next <= value) return null;
        }
        return next;
    };

    /**
     * 点「−」目标值
     * - 先吸附当前值到合法倍数，再 −step；允许减到 0（不卡 moq 下限）
     */
    const minusTarget = (value: number, multiple?: number | null): number => {
        const s = stepOf(multiple);
        const base = snapToValid(value, s, null);
        return Math.max(0, base - s);
    };

    /** 手输/外部值吸附到合法倍数 */
    const normalize = (raw: number, multiple?: number | null, max?: number | null): number => snapToValid(raw, stepOf(multiple), max);

    /** 数量是否为合法倍数（0 视为合法；multiple≤1 恒合法） */
    const isMultipleValid = (qty: number, multiple?: number | null): boolean => {
        const s = stepOf(multiple);
        if (qty <= 0) return true;
        return s <= 1 || qty % s === 0;
    };

    /** 数量是否达到起批量（moq≤0 视为不限制） */
    const isMoqMet = (qty: number, moq?: number | null): boolean => !moq || moq <= 0 || qty >= moq;

    /**
     * 起购数量（列表卡片直加 / 计步器初值与下限的单一真理源）
     * - 同时有起批量与倍数：向上取到 ≥ 起批量的最小倍数 `ceil(moq/multiple)*multiple`
     * - 仅起批量：moq；仅倍数：multiple；都没有：1
     */
    const startQtyOf = (moq?: number | null, multiple?: number | null): number => {
        const q = moq && moq > 0 ? moq : 0;
        const m = multiple && multiple > 1 ? multiple : 0;
        if (q && m) return Math.ceil(q / m) * m;
        if (q) return q;
        if (m) return m;
        return 1;
    };

    return { stepOf, snapToValid, plusTarget, minusTarget, normalize, isMultipleValid, isMoqMet, startQtyOf };
};
