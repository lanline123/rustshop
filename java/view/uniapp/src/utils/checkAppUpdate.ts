import { useAppVersionStore } from "@/store/appVersion";

export default function checkAppUpdate() {
    const appVersionStore = useAppVersionStore();
    appVersionStore
        .getAppUpdateStatus()
        .then((res: string) => {
            console.log("App update check result:", res);
            if (res) {
                appVersionStore.setShowUpdateAppPop(true);
                appVersionStore.setUpdateUrl(res);
            } else {
                appVersionStore.setShowUpdateAppPop(false);
                appVersionStore.setUpdateUrl("");
            }
        })
        .catch((err) => {
            console.error("App update check failed:", err);
            appVersionStore.setShowUpdateAppPop(false);
            appVersionStore.setUpdateUrl("");
        });
}
