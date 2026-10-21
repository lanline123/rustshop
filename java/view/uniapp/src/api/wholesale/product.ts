import request from "@/utils/request";
import type { BuyRule } from "@/types/wholesale/buyRule";

/**
 * @description 商品起批规则（仅起批规则，不含金额）
 * - 金额走 GET /api/product/product/detail 的 wholesalePriceList[]
 * - 非批发商也可查（前端预览用）
 * @param productId 商品 ID
 */
export const getWholesaleBuyRule = (productId: number) => {
    return request<{ code: number; message: string; data: BuyRule }>({
        url: "wholesale/product/buyRule",
        method: "get",
        params: { productId },
        noSkipLogin: true
    });
};
