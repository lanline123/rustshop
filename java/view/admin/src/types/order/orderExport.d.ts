
// 列表查询时筛选参数类型
export interface ExportItemListFilterParams {
    keyword?: string;
    userId?: number;
    shopId?: number;
    orderStatus?: number;
    payStatus?: number;
    shippingStatus?: number;
    address?: string;
    email?: string;
    mobile?: string;
    logisticsId?: number;
    isExchangeOrder?: number;
    exportItem?: string;
	addEndTime?: string;
    addStartTime?: string;
    payTime?: string;

}
export interface ExportItemList {
	orderSn?: string;
	addTime?: string;
	orderStatusName?: string;
	payStatusName?: string;
	storeTitle?: string;
	username?: string;
	consignee?: string;
	mobile?: string;
	address?: string;
	country?: string;
	totalAmount?: string;
	balance?: string;
	discountAmount?: string;
	pointsAmount?: string;
	couponAmount?: string;
	payTime?: string;
	payTypeId?: string;
	paidAmount?: string;
	shippingTime?: string;
	shippingFee?: string;
	logisticsName?: string;
	trackingNo?: string;
	buyerNote?: string;
	adminNote?: string;
	productInfo?: string;
	shippingStatusName?: string;
	productWeight?: string;
	usePoints?: string;
}
export interface ExportItemListFilterState {
	exportItemList: ExportItemList;
}
export interface ExportItem {
    orderSn?: string;
	addTime?: string;
	orderStatusName?: string;
	payStatusName?: string;
	storeTitle?: string;
	username?: string;
	consignee?: string;
	mobile?: string;
	address?: string;
	country?: string;
	totalAmount?: string;
	balance?: string;
	discountAmount?: string;
	pointsAmount?: string;
	couponAmount?: string;
	payTime?: string;
	payTypeId?: string;
	paidAmount?: string;
	shippingTime?: string;
	shippingFee?: string;
	logisticsName?: string;
	trackingNo?: string;
	buyerNote?: string;
	adminNote?: string;
	productInfo?: string;
	shippingStatusName?: string;
	productWeight?: string;
	usePoints?: string;
}
export interface SaveExportItemFilterResult{
	exportItem: string;
}
