import { getPromotion } from "@/api/product/product";
interface PromotionFilterParams {
    products: Products[];
    shopId?: number;
    from?: string;
}
interface Products {
    productId: number;
    skuId?: number;
}
const getPromotionList = async (data: PromotionFilterParams) => {
    try {
        const result = await getPromotion({
            products: data.products,
            shopId: data.shopId,
            from: data.from
        });
        return result;
    } catch (error) {
        console.error(error);
        throw error;
    }
};

export default getPromotionList;
