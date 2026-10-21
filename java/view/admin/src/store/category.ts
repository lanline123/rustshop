import { defineStore } from "pinia";

import { CategoryFilterState } from "@/types/product/category";
export const useCategoryStore = defineStore("category", {
    state: () => {
        return {
            categoryTree: null as CategoryFilterState[] | null
        };
    },
    getters: {},
    actions: {
        clearCategory() {
            this.categoryTree = null;
        }
    },
    persist: {
        enabled: true,
        strategies: [
            { storage: sessionStorage, paths: [] } //用sessionStorage存储
        ]
    }
});
