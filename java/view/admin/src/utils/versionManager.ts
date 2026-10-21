import { version } from '../../package.json'
import { useUserStore } from "@/store/user";
import { needsVersionMismatchClear } from "./versionCheck";
const store = useUserStore() as any;
interface VersionManagerOptions {
    storageKey?: string
    forceRefresh?: boolean
    clearOnMismatch?: boolean
}

const DEFAULT_OPTIONS = {
    storageKey: 'version',
    forceRefresh: true,
    clearOnMismatch: true
}

export { needsVersionMismatchClear } from "./versionCheck";

export function useVersionManager(options?: VersionManagerOptions) {
    const config = { ...DEFAULT_OPTIONS, ...options }
    const currentVersion = version

    const clearStorage = () => {
        store.logout();
    }

    const checkVersion = () => {
        const storedVersion = localStorage.getItem(config.storageKey)

        // 首次访问：只落当前版本，避免误触发 logout（如商家端 SSO 首次进后台）
        if (!storedVersion) {
            localStorage.setItem(config.storageKey, currentVersion)
            return false
        }

        if (needsVersionMismatchClear(storedVersion, currentVersion)) {
            if (config.clearOnMismatch) {
                clearStorage()
            }
            localStorage.setItem(config.storageKey, currentVersion)
            return true
        }
        return false
    }

    const forceRefresh = () => {
        if (config.forceRefresh) {
            window.location.href = `${window.location.pathname}`
        }
    }

    const init = () => {
        const versionChanged = checkVersion()
        if (versionChanged) {
            forceRefresh()
        }
    }

    init()

    return {
        currentVersion,
        storedVersion: localStorage.getItem(config.storageKey),
        forceRefresh
    }
}
