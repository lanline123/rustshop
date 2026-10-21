import { defineStore } from "pinia";
import { ref } from "vue";

export interface useDecorate {
    decorateId: number;
    localeId: number;
    isDefault: boolean;
}

export const useDecorateStore = defineStore(
    "decorate",
    () => {
        // 状态定义
        const decorateInfo = ref<useDecorate>({
            decorateId: 0,
            localeId: 0,
            isDefault: false,
        })

        function setDecorateId(data: any) {
            decorateInfo.value.decorateId = data;
        }

        function setLocaleId(data: any) {
            decorateInfo.value.localeId = data;
        }

        function setIsDefault(data: any) {
            decorateInfo.value.isDefault = data;
        }

        function resetDecorateInfo() {
            decorateInfo.value = {
                decorateId: 0,
                localeId: 0,
                isDefault: false,
            };
        }


        return {
            decorateInfo,
            setDecorateId,
            setLocaleId,
            setIsDefault,
            resetDecorateInfo
        };
    },
    {
        persist: {
            enabled: true,
            strategies: [
                { storage: localStorage, paths: ["decorateInfo"] } // 用 localstorage存储（长效）
            ]
        }
    }
);
