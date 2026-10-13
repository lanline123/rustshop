import { computed } from "vue";

type AnyPromotion = any;

export type UseCouponUsabilityOptions = {
    /**
     * true: 只要外部“传了 enableUsePromotion”（即使是 []），就启用限制
     * false: 只有 enableUsePromotion 里能解析出至少 1 个 couponId 才启用限制
     */
    restrictWhenProvided?: boolean;
    /** 从券对象取 couponId 的函数（默认取 coupon.couponId） */
    getCouponId?: (coupon: any) => number;
};

/**
 * 从结算侧 enableUsePromotion 中解析“当前订单可用的 couponId 集合”，并提供可用性判断函数。
 *
 * 约定：
 * - enableUsePromotion === undefined 表示“外部未传入限制信息”（不限制）
 * - enableUsePromotion === [] 表示“外部已传入限制信息，但当前订单没有可用券”（限制且全部不可用）
 */
export function useCouponUsability(enableUsePromotionGetter: () => AnyPromotion[] | undefined, opts?: UseCouponUsabilityOptions) {
    const restrictWhenProvided = opts?.restrictWhenProvided ?? true;
    const getCouponId = opts?.getCouponId ?? ((c: any) => Number(c?.couponId || 0));

    const provided = computed(() => enableUsePromotionGetter() !== undefined);
    const promotionList = computed<AnyPromotion[]>(() => {
        const raw = enableUsePromotionGetter();
        return Array.isArray(raw) ? raw : [];
    });

    const enableCouponIdSet = computed<Set<number>>(() => {
        const set = new Set<number>();
        for (const x of promotionList.value) {
            if (Number((x as any)?.type) !== 2) continue;
            const cid = Number((x as any)?.data?.couponId || 0);
            if (Number.isFinite(cid) && cid > 0) set.add(cid);
        }
        return set;
    });

    const isRestrictionEnabled = computed(() => {
        if (restrictWhenProvided) return provided.value;
        return enableCouponIdSet.value.size > 0;
    });

    const isCouponUsable = (coupon: any) => {
        if (!isRestrictionEnabled.value) return true;
        const cid = getCouponId(coupon);
        if (!cid) return false;
        return enableCouponIdSet.value.has(cid);
    };

    return {
        isRestrictionEnabled,
        enableCouponIdSet,
        getCouponId,
        isCouponUsable
    };
}


