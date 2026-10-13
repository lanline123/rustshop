// 列表查询时筛选参数类型
export interface productFilterParams {
    page: number;
    size: number;
    sortField?: string;
    sortOrder?: string;
    productName?: string;
    status?: string;
    salesmanProductType?: number | string;
    productGroupId?: number;
}

// 获取列表返回参数类型
export interface productFilterResult {
    records: productFilterState[];
    filter: {
        page: number;
    };
    total: number;
}

export interface Pic {
	picId: number;
	productId: number;
	picUrl: string;
	picDesc: string;
	picThumb: string;
	picOriginal: string;
	picLarge: string;
	sortOrder: number;
}

export interface SalesmanProduct {
	salesmanProductId: number;
	productId: number;
	isJoin: number;
	commissionType: number;
	commissionData: string;
	addTime?: any;
	updateTime?: any;
	shopId?: any;
}

export interface productFilterState {
	productId: number;
	productName: string;
	productSn: string;
	productTsn: string;
	productStock: number;
	productPrice: string;
	marketPrice: string;
	shippingTplId: number;
	productStatus: number;
	productType: number;
	categoryId: number;
	brandId: number;
	shopId: number;
	keywords: string;
	shopCategoryId: number;
	checkStatus: number;
	checkReason: string;
	clickCount: number;
	productWeight: string;
	isPromote: number;
	isPromoteActivity: number;
	promotePrice: string;
	seckillMaxNum: number;
	productBrief: string;
	productDesc: string;
	picUrl: string;
	picThumb: string;
	picOriginal: string;
	commentTag?: any;
	freeShipping: number;
	integral: number;
	addTime: number;
	sortOrder: number;
	storeSortOrder: number;
	isDelete: number;
	isBest: number;
	isNew: number;
	isHot: number;
	lastUpdate: number;
	remark: string;
	giveIntegral: number;
	rankIntegral: number;
	suppliersId: number;
	virtualSales: number;
	limitNumber: number;
	productCare: string;
	productRelated: any[];
	productServiceIds: number[];
	isSupportReturn: number;
	isSupportCod: number;
	productVideo: string;
	prepayPrice: string;
	pics: Pic[];
	salesmanProduct: SalesmanProduct;
}

// 获取详情返回参数类型
export interface productFormState {
    productId?: number;
	isJoin: number;
	commissionType: number;
	commissionData: any;
}
