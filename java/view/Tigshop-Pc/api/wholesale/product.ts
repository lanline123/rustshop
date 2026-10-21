import { asyncRequest } from "@/utils/request";
import type { WholesaleBuyRule } from "@/types/wholesale/buyRule";
import { buildWholesaleBuyRuleParams } from "@/utils/wholesaleContract";

/**
 * 商品起批规则
 * @description GET wholesale/product/buyRule
 * @param productId 商品 ID
 * @param shopId 当前销售门店 ID
 */
export const getWholesaleBuyRule = (productId: number, shopId: number) =>
    asyncRequest<WholesaleBuyRule>({
        url: "wholesale/product/buyRule",
        method: "get",
        params: buildWholesaleBuyRuleParams(productId, shopId)
    });
