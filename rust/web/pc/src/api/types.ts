/**
 * PC 商城端接口类型。
 *
 * 字段名全部与 Rust 后端 `camelCase` 序列化一致；所有路径来自
 * `rust/docs/api-contract.json` 与 `rust/crates/api/public/src` 实际路由。
 * 后端统一响应包由 `@tigshop/shared/request` 已解包，因此这里的类型都是 `data` 载荷。
 */
import type { PageResult } from '@tigshop/shared';

export type { PageResult };

/** 分页结果取数组（兼容 records/list 两种形状）。 */
export function pageItems<T>(page: PageResult<T> | T[] | null | undefined): T[] {
  if (!page) return [];
  if (Array.isArray(page)) return page;
  return page.records ?? page.list ?? [];
}

// ————————————————————— 首页装修 —————————————————————

/** `GET /api/home/home/pcIndex` 返回。 */
export interface DecorateIndex {
  decorateId?: number | null;
  moduleList?: DecorateModule[] | null;
  pageModule?: Record<string, any> | null;
  isHome?: number | null;
}

/** 装修模块（后端为松散 JSON：`{ type, moduleIndex, isShow, module, style }`）。 */
export interface DecorateModule {
  type?: string;
  moduleIndex?: number;
  isShow?: boolean;
  module?: Record<string, any> | null;
  style?: Record<string, any> | null;
  [key: string]: any;
}

// ————————————————————— 分类 / 商品 —————————————————————

/** 分类树节点（`GET /api/category/category/all`）。 */
export interface CategoryNode {
  categoryId: number;
  categoryName?: string | null;
  parentId?: number | null;
  sort?: number | null;
  children?: CategoryNode[];
}

/** 商品列表行（`GET /api/product/product/list`）。 */
export interface ProductVO {
  productId: number;
  productName: string;
  productSn: string;
  productPrice: number | string;
  marketPrice: number | string;
  productStock: number;
  productStatus: number;
  categoryId: number;
  brandId: number;
  shopId: number;
  picUrl: string;
  picThumb: string;
  sales: number;
  addTime: number;
}

/** 商品列表查询参数（字段名对齐 `ProductListQuery` 的 camelCase）。 */
export interface ProductListParams {
  keyword?: string;
  categoryId?: number;
  brandId?: number;
  sortBy?: string;
  sortOrder?: 'asc' | 'desc';
  priceMin?: number;
  priceMax?: number;
  productStatus?: number;
  page?: number;
  pageSize?: number;
}

/** SKU 行。 */
export interface ProductSku {
  skuId: number;
  productId: number;
  skuValue: string;
  skuData?: any;
  skuSn?: string | null;
  skuStock: number;
  skuPrice?: number | string | null;
  marketPrice?: number | string | null;
  isEnabled: number;
}

/** 商品图集。 */
export interface ProductGallery {
  picId: number;
  picUrl: string;
  picThumb: string;
  picOriginal: string;
  picLarge: string;
  sortOrder: number;
}

/** 商品详情（`GET /api/product/product/detail` 的 `ClientProductDetailVO`）。 */
export interface ProductDetail {
  shopId: number;
  item: {
    productId: number;
    productName: string;
    productSn: string;
    productStock: number;
    productPrice: number | string;
    marketPrice: number | string;
    productBrief?: string;
    picUrl: string;
    picThumb: string;
    categoryId?: number;
    brandId?: number;
    sales?: number;
    virtualSales?: number;
    integral?: number;
    [key: string]: any;
  };
  descArr: Array<{ type: string; pic?: string | null; html?: string | null; videoUrl?: string | null }>;
  skuList: ProductSku[];
  picList: ProductGallery[];
  attrList?: { normal?: any[]; spe?: any[]; extra?: any[] };
  seckillDetail?: any[];
  serviceList?: any[];
  checkedValue?: string[];
  deliveryMode?: number;
  deliveryModeText?: string;
  [key: string]: any;
}

// ————————————————————— 购物车 —————————————————————

/** 购物车行（`GET /api/cart/cart/list`）。 */
export interface CartItem {
  cartId: number;
  productId: number;
  productName: string;
  picThumb: string;
  skuId?: number | null;
  skuData?: string | null;
  price: number | string;
  quantity: number;
  subtotal: number | string;
  checked: boolean;
  shopId: number;
  stockEnough: boolean;
}

/** 购物车汇总。 */
export interface CartSummary {
  items: CartItem[];
  checkedQuantity: number;
  checkedAmount: number | string;
  totalQuantity: number;
}

// ————————————————————— 结算 / 订单 —————————————————————

/** 结算页入参（`OrderCheckParam`）。 */
export interface OrderCheckParam {
  userId?: number;
  flowType?: number;
  shopId?: number;
  deliveryType?: number;
  addressId?: number;
  payTypeId?: number;
  usePoint?: number;
  useBalance?: number | string;
  useCredit?: number;
  useCouponIds?: number[];
  selectUserCouponIds?: number[];
  useDefaultCouponIds?: number;
  shippingType?: any;
  buyerNote?: string;
  selectFrom?: number;
  enableChangePrice?: boolean;
}

/** 结算页数据（`OrderCheckVO` 的常用子集，其余字段透传）。 */
export interface OrderCheckVO {
  item?: Record<string, any>;
  cartList?: Array<{
    shopId: number;
    shopTitle: string;
    noShipping: number;
    minOrderAmountDiff?: string;
    isNotReachMinOrderAmount?: boolean;
    carts: Array<Record<string, any>>;
    total?: Record<string, any>;
    gift?: any[];
  }>;
  total?: Record<string, any>;
  couponList?: { enableCoupons?: CheckCoupon[]; disableCoupons?: any[] };
  availablePoints?: number;
  message?: string | null;
  hasChangePrice?: boolean;
  useCouponIds?: number[];
  selectUserCouponIds?: number[];
  creditPaymentInfo?: CreditPaymentInfo | null;
  productPresale?: any;
  couponExcludeAmount?: string;
  pointsExcludeAmount?: string;
  [key: string]: any;
}

/** 可用支付方式（`PaymentTypeVO`）。 */
export interface PaymentType {
  typeId: number;
  typeName: string;
  disabled: boolean;
  disabledDesc: string;
  isShow: boolean;
}

/** 提交订单入参（`OrderCreateDTO`）。 */
export interface OrderCreateParam {
  cartIds: number[];
  addressId?: number;
  shippingMethod?: number;
  deliveryType?: number;
  couponId?: number;
  usePoint?: number;
  useBalance?: number | string;
  buyerNote?: string;
  payTypeId?: number;
  useCouponIds?: number[];
  invoiceData?: InvoiceData;
}

/** 提交订单结果（`OrderCreateVO`）。 */
export interface OrderCreateVO {
  orderId: number;
  orderSn: string;
  totalAmount: number | string;
  onlineAmount: number | string;
  outTradeNo: string;
  returnType?: number;
  balance?: number | string;
}

/** 会员订单行（`OrderVO`）。 */
export interface OrderVO {
  orderId: number;
  orderSn: string;
  orderStatus: number;
  statusLabel: string;
  payStatus: number;
  shippingStatus: number;
  totalAmount: number | string;
  paidAmount: number | string;
  consignee: string;
  mobile: string;
  addTime: number;
  itemCount: number;
}

/** 订单数量角标（`OrderNumVO`）。 */
export interface OrderNum {
  awaitPay: number;
  awaitShipping: number;
  awaitReceived: number;
  awaitComment: number;
  orderCompleted: number;
  productCollect: number;
  shopCollect: number;
  awaitAftersalesCollect: number;
}

/** 订单明细行（`OrderItemVO`）。 */
export interface OrderItem {
  itemId: number;
  orderId: number;
  orderSn: string;
  productId: number;
  productName: string;
  productSn: string;
  picThumb: string;
  skuId: number;
  skuData: string;
  price: number | string;
  quantity: number;
  deliveryQuantity: number;
  shopId: number;
}

/** 订单详情响应（`GET /api/user/order/detail`）。 */
export interface OrderDetail {
  order: OrderVO & Record<string, any>;
  items: OrderItem[];
}

// ————————————————————— 支付 —————————————————————

export interface PayIndex {
  order?: Record<string, any> | null;
  paymentList: string[];
  offlinePaymentList?: Record<string, string>;
}

export interface PayCreate {
  orderId?: number | null;
  orderSn?: string | null;
  orderAmount?: number | string | null;
  payInfo?: any;
  code?: string | null;
}

export interface Paylog {
  paylogId?: number | null;
  paySn?: string | null;
  payName?: string | null;
  orderId?: number | null;
  orderSn?: string | null;
  orderAmount?: number | string | null;
  payAmount?: number | string | null;
  payStatus?: number | null;
  addTime?: number | null;
}

// ————————————————————— 用户 / 地址 —————————————————————

/** 登录响应（`LoginVO`）。 */
export interface LoginVO {
  token: string;
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  adminId: number;
  username: string;
}

/** 用户中心概览（`MemberCenterVO` 常用字段）。 */
export interface MemberCenter {
  userId: number;
  username: string;
  nickname?: string | null;
  avatar: string;
  rankId: number;
  balance: number | string;
  points: number;
  rankName?: string | null;
  awaitPay: number;
  awaitShipping: number;
  awaitReceived: number;
  awaitComment: number;
  awaitCoupon: number;
  [key: string]: any;
}

/** 收货地址（`UserAddressVO`）。 */
export interface AddressVO {
  addressId: number;
  address: string;
  positionAddress?: string | null;
  consignee: string;
  email?: string | null;
  isDefault: number;
  isSelected: number;
  mobile: string;
  mobileAreaCode?: string | null;
  regionName: string;
  regionNames: number[] | string[];
  telephone?: string | null;
  userId: number;
  regionIds: number[];
  postcode?: string | null;
  addressTag: string;
  isSystem?: number | null;
}

/** 地址创建/更新载荷（后端 `UserAddressCreateDTO/UpdateDTO`）。 */
export interface AddressPayload {
  addressId?: number;
  addressTag?: string;
  address?: string;
  positionAddress?: string;
  postcode?: string;
  consignee?: string;
  email?: string;
  mobile?: string;
  telephone?: string;
  isDefault?: number;
  isSelected?: number;
  regionIds?: number[];
}

/** 地区节点（`Region` 实体，snake_case 序列化）。 */
export interface Region {
  region_id: number;
  level?: number | null;
  region_name: string;
  parent_id: number;
  is_hot: number;
  sort_order: number;
  first_word: string;
}

/** 站点基础配置。 */
export interface SiteConfig {
  kefuPhone?: string;
  shopIcpNo?: string;
  shopIcpNoUrl?: string;
  shopName?: string;
  [key: string]: any;
}

// ————————————————————— 结算增强：优惠券 / 发票 —————————————————————

/** 结算页可用优惠券（`CheckCouponVO`）。 */
export interface CheckCoupon {
  id: number;
  couponId: number;
  couponName: string;
  couponType: number;
  couponMoney: number | string;
  minOrderAmount: number | string;
  couponDesc: string;
  isGlobal: boolean;
  couponDiscount: number | string;
  shopId: number;
  endDate: string;
  userCouponId: number;
  selected: boolean;
}

/** 结算页发票信息（`GET /api/order/check/getInvoice` 返回的 `OrderInvoiceVO` 子集）。 */
export interface OrderInvoice {
  id: number;
  orderId: number;
  invoiceType: number;
  invoiceTypeName: string;
  status: number;
  statusName: string;
  titleType: number;
  companyName: string;
  companyCode: string;
  invoiceContent: string;
  amount: number | string;
  email: string;
  mobile: string;
  [key: string]: any;
}

/** 提交订单时的发票数据（`InvoiceDataDTO`）。 */
export interface InvoiceData {
  invoiceType?: number;
  titleType?: number;
  title?: string;
  taxNo?: string;
  content?: string;
  taxPhone?: string;
  companyName?: string;
  email?: string;
  mobile?: string;
}

// ————————————————————— 我的优惠券 —————————————————————

/** 我已领取的优惠券（`UserCouponVO`）。 */
export interface UserCoupon {
  id: number;
  couponId: number;
  couponSn: string;
  couponName: string;
  couponMoney: number | string;
  couponDiscount: number | string;
  couponDesc: string;
  couponType: number;
  minOrderAmount: number | string;
  sendRange: number;
  sendRangeData: number[];
  startDate: number;
  endDate: number;
  usedTime: number;
  orderId: number;
  status: number;
  statusName: string;
}

/** 领券中心可领取优惠券（`CouponClientVO`）。 */
export interface ClaimableCoupon {
  couponId: number;
  couponName: string;
  couponMoney: number | string;
  couponDiscount: number | string;
  couponDesc: string;
  couponType: number;
  minOrderAmount: number | string;
  useStartDate: number;
  useEndDate: number;
  isReceive: number;
  enabledClickGet: number;
  limitNum: number;
  receiveNum: number;
  [key: string]: any;
}

// ————————————————————— 收藏 / 浏览历史 —————————————————————

/** 商品收藏（`CollectProductVO`）。 */
export interface CollectProduct {
  collectId: number;
  productId: number;
  shopId: number;
  addTime: number;
  productName?: string | null;
  picThumb?: string | null;
}

/** 浏览历史（`HistoryProductVO` 子集）。 */
export interface HistoryProduct {
  productId: number;
  productName: string;
  productSn: string;
  productPrice: number | string;
  marketPrice: number | string;
  picUrl: string;
  picThumb: string;
  productStock: number;
  productStatus: number;
  virtualSales: number;
  [key: string]: any;
}

// ————————————————————— 售后 —————————————————————

/** 售后列表行（`GET /api/user/aftersales/list` 的 `AfterSalesClientVO`）。 */
export interface AftersaleOrder {
  orderId: number;
  orderSn: string;
  shippingTime: string;
  items: Array<Record<string, any>>;
}

/** 售后详情（`AfterSalesClientDetailVO` 子集）。 */
export interface AftersaleDetail {
  aftersaleId: number;
  status: number;
  statusName?: string | null;
  statusDesc?: string | null;
  addTime: string;
  aftersaleReason?: string | null;
  aftersaleType: number;
  aftersalesSn?: string | null;
  aftersalesTypeName: string;
  description: string;
  refundAmount: string;
  reply: string;
  pics: any[];
  logisticsName: string;
  trackingNo: string;
  returnAddress?: string | null;
  orderId?: number | null;
  aftersalesItems: Array<Record<string, any>>;
  aftersalesLog: Array<Record<string, any>>;
  canCancel?: boolean;
  [key: string]: any;
}

/** 售后申请页数据（`GET /api/user/aftersales/applyData?orderId=` 的 `AfterSalesApplyDataVO`）。 */
export interface AftersalesApplyData {
  list: Array<{
    itemId: number;
    picThumb: string;
    productSn: string;
    productId: number;
    productName: string;
    price: number | string;
    quantity: number;
    subtotal: number | string;
    canApplyQuantity: number;
    skuData?: Array<Record<string, any>>;
    [key: string]: any;
  }>;
  order: OrderVO;
}

/** 售后配置（`GET /api/user/aftersales/config` 的 `AftersalesConfigVO`）。 */
export interface AftersalesConfig {
  aftersaleType: Record<string, string>;
  aftersaleReason: string[];
  afterSalesReasonEnabled: number;
}

/** 售后申请入参（`CreateAftersalesParam`）。 */
export interface AftersalesCreateParam {
  orderId?: number;
  aftersaleType?: number;
  aftersaleReason?: string;
  description?: string;
  refundAmount?: number | string;
  items?: Array<{ orderItemId?: number; number?: number }>;
}

// ————————————————————— 第三轮：配送 / 支付 / 会员核心 —————————————————————

/** 店铺配送方式（`ShippingTypeVO`）。 */
export interface ShippingType {
  shippingTypeId: number;
  shippingTypeName?: string | null;
  shippingDefaultId?: number | null;
  shippingTypeDesc?: string | null;
  shippingTimeDesc?: string | null;
  isSupportCod?: number | null;
  shopId?: number | null;
  [key: string]: any;
}

/** `GET /order/check/getStoreShippingType`：店铺 ID → 配送方式列表。 */
export type ShippingTypeMap = Record<string, ShippingType[]>;

/** 结算页选中的配送方式（对齐原版 `formState.shippingType` 结构）。 */
export interface SelectedShippingType {
  typeId: number;
  shopId: number;
  typeName: string;
}

/** 账期支付信息（`creditPaymentInfo`）。 */
export interface CreditPaymentInfo {
  canUseCredit: boolean;
  availableAmount: number | string;
  creditPaymentStatus: number;
  [key: string]: any;
}

/** 会员站内信（`UserMessageVO`）。 */
export interface UserMessage {
  messageId: number;
  title: string;
  messageLogId: number;
  userId: number;
  userRank: number;
  isRead: number;
  content: string;
  link?: any;
  addTime: string;
  addTimeDate: string;
  addTimeHms: string;
  orderInfo?: any;
}

/** 未读消息统计（`UnreadMessageCountVO`）。 */
export interface UnreadMessageCount {
  userMessageCount: number;
  orderMessageCount: number;
  imMessageCount: number;
}

/** 签到设置项（`SignInSettingVO`）。 */
export interface SignInSetting {
  id: number;
  name?: string | null;
  points?: number | null;
  dayNum?: number | null;
  sortOrder: number;
  createTime: number;
}

/** 签到页信息（`UserSignInfoVO`）。 */
export interface UserSignInfo {
  days: SignInSetting[];
  record: number;
  isSign: number;
  signPoints: number;
  recommendGoods: any[];
}

/** 评价统计（`CommentSubNumVO`）。 */
export interface CommentSubNum {
  awaitComment: number;
  showPics: number;
  commented: number;
}

/** 评价行（`CommentVO` 子集）。 */
export interface CommentItem {
  commentId?: number | null;
  productId?: number | null;
  productName?: string | null;
  productSn?: string | null;
  picThumb?: string | null;
  content?: string | null;
  commentRank?: number | null;
  addTime?: string | null;
  isAnonymous?: number | null;
  showPics?: any[];
  skuData?: any[];
  replies?: any[];
  [key: string]: any;
}

/** 图片上传结果（`GalleryPicUploadVO`）。 */
export interface UploadedPicture {
  picThumb: string;
  picUrl: string;
  picName: string;
  picId: number;
  picWidth?: number;
  picHeight?: number;
  picSize?: number;
  galleryId?: number;
  fileType?: string;
}
