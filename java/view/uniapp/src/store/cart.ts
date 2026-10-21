import { defineStore } from "pinia";
import { ref } from "vue";
import { asyncGetCartCount } from "@/api/cart/cart";
import { useUserStore } from "./user";

export const useCartStore = defineStore("cart", () => {
    const cartCount = ref<number>(0);
    // 多个页面/底部导航可能同时触发刷新，只保留同一时间窗口内的一个请求。
    let pendingCountRequest: Promise<void> | null = null;
    // 头部和底部导航可能在同一页面先后触发刷新，短时间内复用已获取的结果。
    let lastFetchedAt = 0;
    const cacheDuration = 1000;

    function fetchCartCount(force = false): Promise<void> {
        if (!useUserStore().token) {
            cartCount.value = 0;
            return Promise.resolve();
        }
        if (pendingCountRequest) return pendingCountRequest;
        if (!force && Date.now() - lastFetchedAt < cacheDuration) return Promise.resolve();

        pendingCountRequest = (async () => {
            try {
                const result = await asyncGetCartCount();
                cartCount.value = result ?? 0;
                lastFetchedAt = Date.now();
            } catch (error) {
                console.error(error);
            } finally {
                pendingCountRequest = null;
            }
        })();

        return pendingCountRequest;
    }

    function setCartCount(count: number) {
        cartCount.value = count;
        lastFetchedAt = Date.now();
    }

    return {
        cartCount,
        fetchCartCount,
        setCartCount
    };
});
