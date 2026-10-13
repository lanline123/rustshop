// 列表查询时筛选参数类型
export interface SalesmanOrderFilterParams {
    page: number;
    size: number;
    sortField?: string;
    sortOrder?: string;
    orderSn?: string;
    status?: number | string;
    timeType?: number;
    orderTimeStart?: string;
    orderTimeEnd?: string;
}

// 获取列表返回参数类型
export interface SalesmanOrderFilterResult {
    records: SalesmanOrderFilterState[];
    filter: {
        page: number;
    };
    total: number;
}

// export interface SalesmanOrderFilterState {
// 	addTime: number;
// 	sortOrder: number;
// 	storeSortOrder: number;
// 	isDelete: number;
// 	isBest: number;
// 	isNew: number;
// 	isHot: number;
// 	lastUpdate: number;
// 	remark: string;
// 	giveIntegral: number;
// 	rankIntegral: number;
// 	suppliersId: number;
// 	virtualSales: number;
// 	limitNumber: number;
// 	SalesmanOrderCare: string;
// 	SalesmanOrderRelated: any[];
// 	SalesmanOrderServiceIds: number[];
// 	isSupportReturn: number;
// 	isSupportCod: number;
// 	SalesmanOrderVideo: string;
// 	prepayPrice: string;
// }

export interface Level_arr {
	level: number;
	downSalesmanRate: string;
	rate: number;
}

export interface Table_data {
	levelArr: Level_arr[];
}

export interface Commission_data {
	tableData: Table_data[];
}

export interface Salesman_product_data {
	salesmanProductId: number;
	productId: number;
	isJoin: number;
	commissionType: number;
	commissionData: Commission_data;
	addTime: string;
	updateTime: string;
	shopId: number;
}

export interface Salesman {
	salesmanId: number;
	userId: number;
	level: number;
	groupId: number;
	pid: number;
	addTime: string;
	shopId: number;
	saleAmount: string;
	mobile: string;
	nickname: string;
	distributionRegisterTime: number;
	groupName: string;
	shopTitle?: any;
}

export interface User {
	username: string;
	nickname: string;
	userId: number;
	mobile: string;
}

export interface Order_user_info {
	orderId: number;
	orderSn: string;
	userId: number;
	totalAmount: string;
	addTime: string;
	payTime: string;
	orderStatus: number;
	user: User;
}

export interface UserOrder {
	orderId: number;
	orderSn: string;
	userId: number;
	parentOrderId: number;
	parentOrderSn: string;
	orderStatus: number;
	shippingStatus: number;
	payStatus: number;
	addTime: string;
	consignee: string;
	address: string;
	regionIds: string;
	regionNames: string;
	addressData: string;
	mobile: string;
	email: string;
	buyerNote: string;
	adminNote: string;
	shippingMethod: number;
	logisticsId: number;
	logisticsName: string;
	shippingTypeId: number;
	shippingTypeName: string;
	trackingNo: string;
	shippingTime: string;
	receivedTime: string;
	payTypeId: number;
	payTime: string;
	usePoints: number;
	isNeedCommisson: number;
	distributionStatus: number;
	referrerUserId: number;
	isDel: number;
	shopId: number;
	isStoreSplited: number;
	commentStatus: number;
	totalAmount: string;
	paidAmount: string;
	unpaidAmount: string;
	unrefundAmount: string;
	productAmount: string;
	couponAmount: string;
	pointsAmount: string;
	discountAmount: string;
	balance: string;
	onlinePaidAmount: string;
	offlinePaidAmount: string;
	serviceFee: string;
	shippingFee: string;
	invoiceFee: string;
	orderExtension: string;
	orderSource: string;
	invoiceData: string;
	outTradeNo: string;
	isSettlement: number;
}

export interface UserOrderItem {
	itemId: number;
	orderId: number;
	orderSn: string;
	userId: number;
	price: string;
	quantity: number;
	productId: number;
	productName: string;
	productSn: string;
	picThumb: string;
	skuId: number;
	skuData: string;
	deliveryQuantity: number;
	productType: number;
	isGift: number;
	shopId: number;
	isPin: number;
	prepayPrice: string;
	commission: string;
	originPrice: string;
}

export interface UserOrderRefund {
	refundTypeText: string;
	logId: number;
	refundApplyId: number;
	refundType: number;
	refundPayCode: string;
	transactionId: string;
	refundAmount: string;
	addTime: string;
	description?: any;
	userId: number;
	orderId: number;
}

export interface SalesmanOrderFilterState {
	statusText: string;
	salesmanOrderId: number;
	orderId: number;
	salesmanId: number;
	amount: string;
	status: number;
	addTime?: any;
	itemId: number;
	salesmanProductData: Salesman_product_data;
	orderAmount: string;
	salesmanSettlementData?: any;
	settlementTime: string;
	refundTime: string;
	salesman: Salesman;
	orderUserInfo: Order_user_info;
	userOrder: UserOrder;
	userOrderItem: UserOrderItem;
	userOrderRefund: UserOrderRefund;
}


// 获取详情返回参数类型
export interface SalesmanOrderFormResult {
    item: SalesmanOrderFormState;
}
export interface SalesmanOrderFormState {
    SalesmanOrderId: number;
	isJoin: number;
	commissionType: number;
	commissionData: any[];
}
