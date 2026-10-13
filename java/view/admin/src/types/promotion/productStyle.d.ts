export interface ProductStyleFilterParams {
    page: number;
    size: number;
    sortField: string;
    sortOrder: string;
    keyword?: string;
    styleType?: number | string;
    activityStatus?: number | string;
    startTime?: string;
    endTime?: string;
}

export interface ProductStyleFilterResult {
    records: ProductStyleFilterState[];
    total: number;
}

export interface ProductStyleFilterState {
    styleId: number;
    styleName: string;
    styleType: number;
    styleTypeName: string;
    styleImage: string;
    pcImage: string;
    startTime: number;
    endTime: number;
    status: number;
    shopId: number;
    addTime: number;
    updateTime: number;
    productCount: number;
}

export interface ProductStyleDetailVO extends ProductStyleFilterState {
    productIds: number[];
    groupIds: number[];
    brandIds: number[];
    productList: ProductStyleItemVO[];
    groupList: GroupItemVO[];
    brandList: BrandItemVO[];
}

export interface ProductStyleItemVO {
    productId: number;
    productName: string;
    picThumb: string;
    productPrice: number;
}

export interface GroupItemVO {
    productGroupId: number;
    productGroupName: string;
}

export interface BrandItemVO {
    brandId: number;
    brandName: string;
    brandLogo?: string;
}

export interface BrandProductCountVO {
    brandId: number;
    brandName: string;
    productCount: number;
}

export interface ProductStyleFormState {
    styleId?: number;
    styleName: string;
    styleType: number;
    styleImage: string;
    pcImage: string;
    startTime: string;
    endTime: string;
    status: number;
    productIds: number[];
    groupIds: number[];
    brandIds: number[];
}
