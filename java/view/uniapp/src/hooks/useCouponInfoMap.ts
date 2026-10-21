import { ref } from "vue";
import { getCouponListByIds } from "@/api/coupon/coupon";
import type { CouponFilterResult } from "@/types/coupon/coupon";

// module-level 单例，跨同页多个模块（如新人二重礼）共享，避免重复请求
const couponInfoMap = ref<Map<number, CouponFilterResult>>(new Map());
const inFlight = new Map<string, Promise<void>>();

/**
 * 优惠券信息 Map（按 couponId 索引），用于装修等场景批量回显
 *
 * - 多次 fetchCouponInfo 同一批 ids 的并发调用会合并为一次 HTTP 请求
 * - 已缓存的 id 自动跳过，避免重复拉取
 * - 与 admin 端 useCouponInfoMap 同接口同语义，便于 admin/mobile 双向对照
 */
export function useCouponInfoMap() {
    const fetchCouponInfo = async (ids: number[]) => {
        const unique = Array.from(new Set(ids))
            .filter((id) => Number(id) > 0 && !couponInfoMap.value.has(id))
            .sort((a, b) => a - b);
        if (!unique.length) return;
        const key = unique.join(",");
        let promise = inFlight.get(key);
        if (!promise) {
            promise = (async () => {
                try {
                    const fresh = await getCouponListByIds(unique);
                    if (Array.isArray(fresh)) {
                        fresh.forEach((c: any) => {
                            if (c?.couponId) couponInfoMap.value.set(c.couponId, c);
                        });
                    }
                } catch {
                    // 接口报错时静默降级（缺失 info 的项由 displayCoupons 自动隐藏，不显示空壳卡）
                } finally {
                    inFlight.delete(key);
                }
            })();
            inFlight.set(key, promise);
        }
        await promise;
    };

    const removeCouponInfo = (couponId: number) => {
        couponInfoMap.value.delete(couponId);
    };

    return { couponInfoMap, fetchCouponInfo, removeCouponInfo };
}
