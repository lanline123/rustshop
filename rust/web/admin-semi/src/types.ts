export interface PageResult<T> {
  records: T[];
  total: number;
  size: number;
  current: number;
  pages: number;
}

export interface DashboardData {
  awaitAfterSale: number;
  awaitComment: number;
  awaitPay: number;
  awaitShip: number;
  buyerGrowthRate: number;
  orderAmountGrowthRate: number;
  orderGrowthRate: number;
  consoleData: {
    awaitAfterSale: number;
    awaitComment: number;
    awaitPay: number;
    awaitShip: number;
    productOnSaleNum: number;
    productTotalNum: number;
    todayBuyerNum: number;
    todayOrderAmount: number;
    todayOrderNum: number;
    todayViewNum: number;
    todayVisitNum: number;
  };
  panelStatisticalData: {
    buyerGrowthRate: number;
    orderAmountGrowthRate: number;
    orderGrowthRate: number;
    todayBuyerNum: number;
    todayOrderAmount: number;
    todayOrderNum: number;
    todayViewNum: number;
    todayVisitNum: number;
    yesterdayBuyerNum: number;
    yesterdayOrderAmount: number;
    yesterdayOrderNum: number;
    yesterdayViewNum: number;
    yesterdayVisitNum: number;
  };
}

export interface ProductRow {
  productId: number;
  productName: string;
  productSn: string;
  productPrice: string;
  marketPrice: string;
  productStock: number;
  productStatus: number;
  checkStatus: number;
  picThumb: string;
  picUrl: string;
  virtualSales: number;
  skuCount: number;
  categoryId: number;
  brandId: number;
  productBrief: string;
  shopId: number;
}

export interface OrderRow {
  orderId: number;
  orderSn: string;
  userId: number;
  orderStatus: number;
  orderStatusName: string;
  payStatus: number;
  payStatusName: string;
  shippingStatus: number;
  shippingStatusName: string;
  consignee: string;
  mobile: string;
  totalAmount: string;
  paidAmount: string;
  addTime: string;
  items?: Array<{ productName?: string; productNum?: number; productImage?: string }>;
}

export interface MemberRow {
  userId: number;
  username: string;
  nickname: string;
  mobile: string;
  email: string;
  avatar: string;
  balance: string;
  points: number;
  orderCount: number;
  orderAmount: string;
  regTime: string;
  rankName: string;
  isSvip: number;
  status?: number;
}

export interface ArticleRow {
  articleId: number;
  articleTitle: string;
  articleCategoryId: number;
  articleAuthor: string;
  description: string;
  isShow: number;
  isHot: number;
  clickCount: number;
  addTime: number;
}

export interface ArticleCategoryRow {
  articleCategoryId: number;
  articleCategoryName: string;
  parentId: number;
  categorySn: string;
}
