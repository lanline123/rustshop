import { defineStore } from "pinia";
import { ref } from "vue";
import { getAppUpdate } from "@/api/common";

export const useAppVersionStore = defineStore("appVersion", () => {
    const showUpdateAppPop = ref(false);
    const updateUrl = ref("");
    const osName = ref("");

    const setShowUpdateAppPop = (value: boolean) => {
        showUpdateAppPop.value = value;
    };
    const setUpdateUrl = (url: string) => {
        updateUrl.value = url;
    };

    const getAppUpdateStatus = (): Promise<string> => {
        return new Promise((resolve, reject) => {
            uni.getSystemInfo({
                success: async function (res) {
                    osName.value = res.osName;
                    plus.runtime.getProperty(plus.runtime.appid, async (widgetInfo) => {
                        console.log("Current app version:", widgetInfo.version);
                        console.log("Current OS name:", res.osName);
                        try {
                            const result = await getAppUpdate({
                                type: res.osName,
                                version: widgetInfo.version
                                // version: "1.0.0"
                            });
                            resolve(result);
                        } catch (error) {
                            reject(error);
                        }
                    });
                }
            });
        });
    };

    return {
        showUpdateAppPop,
        updateUrl,
        osName,
        setShowUpdateAppPop,
        setUpdateUrl,
        getAppUpdateStatus
    };
});
