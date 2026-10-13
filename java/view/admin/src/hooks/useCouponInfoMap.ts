import { ref } from "vue";
import { getCouponListByIds } from "@/api/promotion/coupon";
import type { CouponFilterState } from "@/types/promotion/coupon";

// module-level 单例，跨组件共享，避免装修编辑器中
// Edit.vue 和 View.vue 同时拉同一批优惠券导致重复请求
const couponInfoMap = ref<Map<number, CouponFilterState>>(new Map());
const inFlight = new Map<string, Promise<void>>();

/**
 * 优惠券信息 Map（按 couponId 索引），用于装修等场景批量回显
 *
 * - 多次 `fetchCouponInfo` 同一批 ids 的并发调用会合并为一次 HTTP 请求
 * - 拉取结果写入共享 `couponInfoMap`，订阅者可直接读取响应式数据
 */
export function useCouponInfoMap() {
    const fetchCouponInfo = async (ids: number[]) => {
        const unique = Array.from(new Set(ids)).sort((a, b) => a - b);
        if (!unique.length) return;
        const key = unique.join(",");
        let promise = inFlight.get(key);
        if (!promise) {
            promise = (async () => {
                try {
                    const fresh = await getCouponListByIds(unique);
                    fresh.forEach((c: any) => {
                        if (c?.couponId) couponInfoMap.value.set(c.couponId, c);
                    });
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
