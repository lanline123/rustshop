import { useUserStore } from "@/store/user";
import { useConfigStore } from "@/store/config";
import { useMenusStore } from "@/store/menu";
import { useThemeStore } from "@/store/theme";
import { getMenu } from "@/utils/menus";
import { processRoutes } from "@/utils/authorize";
const configStore = useConfigStore();
const userStore = useUserStore();
const menusStore = useMenusStore();
const themeStore = useThemeStore();

export function useVersionConfigManager() {
    const checkVersion = () => {
        const currentVersion = configStore.get("version") || "";
        const storedVersion = localStorage.getItem("versionKey") || "";
        if (storedVersion !== currentVersion || !storedVersion) {
            localStorage.setItem("versionKey", currentVersion)
            return true
        }
        return false
    }
    const forceRefresh = async () => {
        const routers = await getMenu();
        menusStore.setRouters(processRoutes(routers) || []);
        // 更新后台设置项
        userStore.updateUserInfo()
        themeStore.getThemeInfo()
    }
    const init = async () => {
        await configStore.updateConfig()
        const versionChanged = checkVersion()
        if (versionChanged) {
            forceRefresh()
        }
    }
    init()
}