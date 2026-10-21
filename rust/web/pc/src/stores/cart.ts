/**
 * 购物车状态（Pinia）。
 *
 * 数据源全部为真实接口：
 * - `GET  /api/cart/cart/list`（行 + 勾选数量/金额）
 * - `GET  /api/cart/cart/getCount`（头部角标）
 * - `POST /api/cart/cart/{addToCart,updateItem,updateCheck,removeItem,clear}`
 */
import { defineStore } from 'pinia';
import { cartApi } from '@/api';
import type { CartItem, CartSummary } from '@/api/types';

interface CartState {
  summary: CartSummary | null;
  count: number;
  loading: boolean;
}

const EMPTY_SUMMARY: CartSummary = {
  items: [],
  checkedQuantity: 0,
  checkedAmount: '0',
  totalQuantity: 0,
};

export const useCartStore = defineStore('cart', {
  state: (): CartState => ({
    summary: null,
    count: 0,
    loading: false,
  }),

  getters: {
    items: (state): CartItem[] => state.summary?.items ?? [],
    checkedItems: (state): CartItem[] => (state.summary?.items ?? []).filter((it) => it.checked),
    checkedCount: (state): number => state.summary?.checkedQuantity ?? 0,
    checkedAmount: (state): number | string => state.summary?.checkedAmount ?? 0,
    totalQuantity: (state): number => state.summary?.totalQuantity ?? 0,
    allChecked: (state): boolean => {
      const items = state.summary?.items ?? [];
      return items.length > 0 && items.every((it) => it.checked);
    },
  },

  actions: {
    /** 加载购物车汇总；未登录时清空。 */
    async fetchSummary(token: string) {
      if (!token) {
        this.summary = null;
        this.count = 0;
        return;
      }
      this.loading = true;
      try {
        this.summary = await cartApi.list();
        this.count = this.summary?.totalQuantity ?? 0;
      } finally {
        this.loading = false;
      }
    },

    /** 仅刷新头部角标（Header 使用）。 */
    async fetchCount(token: string) {
      if (!token) {
        this.count = 0;
        return;
      }
      try {
        const value = await cartApi.count();
        this.count = Number(value ?? 0) || 0;
      } catch {
        this.count = 0;
      }
    },

    /** 加购后刷新汇总与角标。 */
    async add(token: string, payload: { productId: number; skuId?: number | null; quantity?: number }) {
      const cartId = await cartApi.add(payload);
      await this.fetchSummary(token);
      await this.fetchCount(token);
      return cartId;
    },

    async updateQuantity(token: string, cartId: number, quantity: number) {
      await cartApi.updateItem(cartId, quantity);
      await this.fetchSummary(token);
      await this.fetchCount(token);
    },

    async toggle(token: string, cartId: number, checked: boolean) {
      await cartApi.updateCheck(cartId, checked);
      await this.fetchSummary(token);
    },

    /** 全选/取消全选（逐行调用更新接口）。 */
    async toggleAll(token: string, checked: boolean) {
      const items = this.items;
      for (const item of items) {
        if (item.checked !== checked) {
          await cartApi.updateCheck(item.cartId, checked);
        }
      }
      await this.fetchSummary(token);
    },

    async remove(token: string, cartIds: number[]) {
      await cartApi.remove(cartIds);
      await this.fetchSummary(token);
      await this.fetchCount(token);
    },

    async clear(token: string) {
      await cartApi.clear();
      this.summary = { ...EMPTY_SUMMARY, items: [] };
      this.count = 0;
    },

    reset() {
      this.summary = null;
      this.count = 0;
    },
  },
});
