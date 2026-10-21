/**
 * 拼团商品列表数据拉取 Composable（移植自 Vort-Admin _shared/groupon）
 *
 * API 适配：Vort 的 grouponItemApi.getList（marketing/groupon/item/list）
 * 对应 Tigshop 的 getGrouponItemList（promotion/groupon/item/list），
 * 返回字段（productName/picThumb/productPrice/minGroupPrice/minGroupNum）两端一致；
 * grouponPromotionId 在 Tigshop 侧为 number，请求前做 Number 转换。
 */
import { ref, watchEffect, type Ref, type ComputedRef } from "vue";
import { getGrouponItemList } from "@/api/promotion/groupon";
import type { GrouponItemFilterState } from "@/types/promotion/groupon";
import type { GrouponProductItem } from "../types";

export interface UseGrouponItemListOptions<T = GrouponProductItem> {
    /** 拼团活动 ID（响应式） */
    promotionId: Ref<string> | ComputedRef<string>;
    /** 每页数量（响应式或静态） */
    size?: Ref<number> | ComputedRef<number> | number;
    /** 自定义映射函数，将接口返回项转换为目标类型 */
    mapItem?: (item: GrouponItemFilterState, activityTime?: { startTime?: string; endTime?: string }) => T;
    /** 活动时间（可选，用于填充到商品项） */
    activityTime?: Ref<{ startTime?: string; endTime?: string } | undefined> | ComputedRef<{ startTime?: string; endTime?: string } | undefined>;
}

/**
 * 默认映射函数：将接口返回项归一化为 GrouponProductItem
 */
export function defaultMapGrouponItem(item: GrouponItemFilterState, activityTime?: { startTime?: string; endTime?: string }): GrouponProductItem {
    const minGroupNum = Number.parseInt(String(item.minGroupNum || ""), 10);
    return {
        productId: Number.parseInt(String(item.productId || "0"), 10) || 0,
        productName: item.productName || "",
        picUrl: String(item.picUrl || ""),
        picThumb: String(item.picThumb || ""),
        picLarge: String(item.picLarge || ""),
        productPrice: item.productPrice || "0.00",
        minGroupPrice: item.minGroupPrice || "0.00",
        minGroupNum: Number.isFinite(minGroupNum) && minGroupNum > 0 ? minGroupNum : 2,
        startTime: activityTime?.startTime,
        endTime: activityTime?.endTime
    };
}

/**
 * 拼团商品列表数据拉取
 * @param options 配置项
 * @returns { list, loading, reload }
 */
export function useGrouponItemList<T = GrouponProductItem>(options: UseGrouponItemListOptions<T>) {
    const { promotionId, size = 10, mapItem, activityTime } = options;

    const list = ref<T[]>([]) as Ref<T[]>;
    const loading = ref(false);

    const getSize = (): number => {
        if (typeof size === "number") return size;
        return size.value;
    };

    const getActivityTime = () => {
        if (!activityTime) return undefined;
        return activityTime.value;
    };

    const fetchList = async () => {
        const id = promotionId.value;
        if (!id) {
            list.value = [];
            return;
        }
        loading.value = true;
        try {
            const res = await getGrouponItemList({
                grouponPromotionId: Number(id),
                page: 1,
                size: getSize()
            });
            const records = res.records || [];
            const mapper = mapItem || ((item: GrouponItemFilterState) => defaultMapGrouponItem(item, getActivityTime()) as unknown as T);
            list.value = records.map((item) => mapper(item, getActivityTime()));
        } catch {
            list.value = [];
        } finally {
            loading.value = false;
        }
    };

    // 监听 promotionId 变化自动拉取
    watchEffect(() => {
        fetchList();
    });

    return {
        /** 商品列表 */
        list,
        /** 加载状态 */
        loading,
        /** 手动刷新 */
        reload: fetchList
    };
}
