import { getProduct, updateProductFiled } from "@/api/product/product";
import { getVendorMaxPrice } from "@/api/vendor/product";
import i18n from "@/i18n";

interface PriceUpdate {
    id: number;
    field: "productPrice" | "productSku";
    val: string | number | { skuId: number; skuPrice: number }[];
}

/** 列表快捷改价沿用编辑页的供应商售价上下限，校验失败时不提交。 */
export const updateProductPrice = async (data: PriceUpdate, product: { vendorProductId?: number }) => {
    const t = i18n.global.t;
    if (Number(product.vendorProductId) > 0) {
        const detail = await getProduct("detail", { id: data.id });
        const skus = detail.productList || [];
        const changes =
            data.field === "productSku" && Array.isArray(data.val)
                ? data.val.map((change) => ({ sku: skus.find((sku: any) => Number(sku.skuId) === Number(change.skuId)), price: change.skuPrice }))
                : skus.length === 1
                  ? [{ sku: skus[0], price: data.val }]
                  : [];

        if (!changes.length) throw new Error(t("操作失败"));
        for (const { sku, price } of changes) {
            const vendorSku = sku as any;
            const supplyPrice = Number(vendorSku?.supplyPrice);
            if (!vendorSku?.vendorProductSkuId || vendorSku.supplyPrice == null || !Number.isFinite(supplyPrice)) {
                throw new Error(t("操作失败"));
            }
            const value = Number(price);
            if (!Number.isFinite(value) || value <= 0) throw new Error(t("请输入正确的商品价格"));
            if (value < supplyPrice) throw new Error(t("售价不可小于供货价 {price}", { price: supplyPrice }));
            const limit = await getVendorMaxPrice({ vendorProductSkuId: vendorSku.vendorProductSkuId });
            const maxPrice = Number(limit);
            if (limit == null || !Number.isFinite(maxPrice)) throw new Error(t("操作失败"));
            if (value > maxPrice) throw new Error(t("价格最大不可超过 {price}", { price: maxPrice }));
        }
    }
    return updateProductFiled(data);
};
