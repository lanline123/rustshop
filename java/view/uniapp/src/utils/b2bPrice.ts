import pinia from "@/store/index";
import { useConfigStore } from "@/store/config";
import { useUserStore } from "@/store/user";

/**
 * 是否需要显示 B2B 登录或实名认证后查看价格的提示。
 */
export const isShowB2bPriceText = () => {
    const configStore = useConfigStore(pinia);
    const userStore = useUserStore(pinia);
    if (configStore.isIdentity !== 1) {
        return false;
    }
    return Object.keys(userStore.userInfo).length === 0 || !userStore.userInfo.isCompanyAuth;
};
