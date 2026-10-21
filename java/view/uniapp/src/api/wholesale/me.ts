import request from "@/utils/request";
import type { WholesaleIdentity } from "@/types/wholesale/identity";

/**
 * @description 当前用户批发身份
 * - 未登录态后端返回 isWholesaler=false，不报错（noSkipLogin: true）
 * - 登录后调一次，缓存到 wholesale store
 * - 切号 / 登出后需要重新调
 */
export const getWholesaleIdentity = () => {
    return request<{ code: number; message: string; data: WholesaleIdentity }>({
        url: "wholesale/me",
        method: "get",
        noSkipLogin: true
    });
};
