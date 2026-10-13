export interface getProductListFilterState {
    productList: ProductList[];
    errcode: number;
    message: string;
}

export interface ProductList {
    productId: number;
    picThumb: string;
    productName: string;
    checkStatus: number;
    shopId: number;
    suppliersId: number;
    productType: number;
    productSn: string;
    productPrice: string;
    marketPrice: string;
    productStatus: number;
    isBest: number;
    isNew: number;
    isHot: number;
    productStock: number;
    sortOrder: number;
    seckillPrice: null;
}
