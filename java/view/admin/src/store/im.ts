import { defineStore } from "pinia";
export interface useIm {
    imPresence: number
}
export const useImStore = defineStore("im", {
    state: () => {
        return <useIm>{
            imPresence: 1
        };
    },
    actions: {
        setImPresence(data: any) {
            this.imPresence = data;
        }
    },
    persist: {
        enabled: true,
        strategies: [
            // { storage: sessionStorage, paths: [] }, //用sessionStorage存储
            { storage: localStorage, paths: ["im"] } // 用 localstorage存储（长效）
        ]
    }
});
