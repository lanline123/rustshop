/**
 * 商品卡片（列表/推荐/装修）购买行为辅助
 * @description 后端在总开关关 / 非批发商 / 非平台商品时不返 firstWholesalePrice，前端按字段存在判定批发卡片
 */
type CardProduct = {
    /** 批发首价；仅批发商命中批发商品时后端返回 */
    firstWholesalePrice?: string | null;
    productId?: number;
    id?: number;
};

export const useProductCardBuy = () => {
    /**
     * 是否按批发卡片处理（加购走批发 SKU 半窗）
     */
    const isWholesaleCard = (item?: CardProduct | null): boolean => {
        return item?.firstWholesalePrice != null;
        return false;
    };

    /** 从列表 item 取商品 ID（兼容 exchange 等 id 字段） */
    const cardProductId = (item?: CardProduct | null): number => {
        return Number(item?.productId ?? item?.id ?? 0);
    };

    return {
        isWholesaleCard,
        cardProductId
    };
};
