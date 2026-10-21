import { useConfigStore } from "@/store/config";
import { useTabbarStore } from "@/store/tabbar";
import { useThemeStore } from "@/store/theme";
import { useUserStore } from "@/store/user";
import i18n from "@/i18n";
import { isOverseas, isO2o } from "@/utils";
import { setIsInit } from "@/utils/request";
import { useWholesaleStore } from "@/store/wholesale";

export default async function init() {
    const configStore = useConfigStore();
    const arr: Promise<unknown>[] = [
        configStore.getBaseConfigData(),
        useTabbarStore().getTabbarList(),
        useThemeStore().getThemeSettings(),
        useUserStore().getServiceConfigData()
    ];
    arr.push(useWholesaleStore().loadIdentity());

    try {
        uni.setLocale("zh-Hans");
        await Promise.all(arr);
    } catch (error) {
        console.error("Initialization failed:", error);
    } finally {
        setIsInit(false);
    }
}
