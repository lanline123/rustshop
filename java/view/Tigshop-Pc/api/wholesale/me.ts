import { asyncRequest } from "@/utils/request";
import type { WholesaleIdentity } from "@/types/wholesale/identity";

/**
 * 当前用户批发身份
 * @description GET wholesale/me
 * - 未登录态前端不应调用（由 composable 守门），后端可返回 401
 * - 总开关关时返回 globalEnabled=false，前端不渲染批发能力
 */
export const getWholesaleIdentity = () =>
    asyncRequest<WholesaleIdentity>({
        url: "wholesale/me",
        method: "get",
        noSkipLogin: true
    });
