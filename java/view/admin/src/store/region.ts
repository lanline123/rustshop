import { defineStore } from 'pinia'
import { getRegionTreeList } from "@/api/setting/region";

export const useRegionStore = defineStore('region', {
    state: () => {
        return {
            allRegion: null,
        }
    },
    getters: {
    },
    actions: {
       async getRegionList() {
            if (this.allRegion === null) {
                try {
                    const result = await getRegionTreeList();
                    this.allRegion = result
                } catch (error) {
                } finally {
                }
            } else {
                return this.allRegion
            }
        },
        clearRegion() {
            this.allRegion = null
        }
    },
    persist: {
        enabled: true,
        strategies: [
            { storage: localStorage, paths: ['allRegion'] }, // 用 localstorage存储
        ],
    }
})
