/**
 * @file 分享登录门控
 *
 * 供各分享入口统一调用，避免登录判断散落在各页面。
 */
import { useConfigStore } from "@/store/config";
import { useUserStore } from "@/store/user";

/**
 * 校验分享是否可以继续。
 * 返回 false 表示已跳转登录页，调用方需中断后续逻辑。
 *
 * @param otherPlatformNeedLogin 微信小程序以外的端是否仍要求登录，默认不要求
 */
export const ensureShareLogin = (otherPlatformNeedLogin = false): boolean => {
    const userStore = useUserStore();
    let needLogin = otherPlatformNeedLogin;

    // #ifdef MP-WEIXIN
    const configStore = useConfigStore();
    // 后端可能下发字符串 "1"，用 Number 兼容，避免 === 1 误判为不需要登录
    needLogin = Number(configStore.wechatShareNeedLogin) === 1;
    // #endif

    // store 与本地缓存双读，避免退出后某一侧未同步导致门控失效
    const hasToken = Boolean(userStore.token || uni.getStorageSync("token"));
    if (needLogin && !hasToken) {
        uni.navigateTo({
            url: "/pages/login/index"
        });
        return false;
    }
    return true;
};
