export const needsDirectBuyFlow = (detail: any) => {
    const productType = Number(detail?.item?.productType ?? 1);
    if (productType !== 1) return true;
    if (Number(detail?.item?.saleMode ?? 0) > 0) return true;
    return false;
};
