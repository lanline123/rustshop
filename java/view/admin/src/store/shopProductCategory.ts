import { defineStore } from "pinia";

import { ShopProductCategoryFilterState } from "@/types/product/shopProductCategory";
export const useShopProductCategoryStore = defineStore("shopProductCategory", {
    state: () => {
        return {
            shopProductCategoryTree: null as ShopProductCategoryFilterState[] | null
        };
    },
    getters: {},
    actions: {
        clearShopProductCategory() {
            this.shopProductCategoryTree = null;
        }
    },
    persist: {
        enabled: true,
        strategies: [
            { storage: sessionStorage, paths: [] } //用sessionStorage存储
        ]
    }
});
