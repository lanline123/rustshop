/**
 * PC 商城端接口封装。
 *
 * 所有路径逐字符对齐 `rust/docs/api-contract.json` 与
 * `rust/crates/api/public/src`（`/api` 前缀由 `@/utils/request` 的 baseURL 提供）。
 * 统一使用 `@/utils/request`，成功响应已被解包为 `data`。
 */
import request from '@/utils/request';
import type {
  AddressPayload,
  AddressVO,
  AftersaleDetail,
  AftersaleOrder,
  AftersalesApplyData,
  AftersalesConfig,
  AftersalesCreateParam,
  CartSummary,
  CategoryNode,
  CheckCoupon,
  ClaimableCoupon,
  CollectProduct,
  CommentItem,
  CommentSubNum,
  DecorateIndex,
  HistoryProduct,
  LoginVO,
  MemberCenter,
  OrderCheckParam,
  OrderCheckVO,
  OrderCreateParam,
  OrderCreateVO,
  OrderDetail,
  OrderInvoice,
  OrderNum,
  OrderVO,
  PageResult,
  PayCreate,
  PayIndex,
  PaymentType,
  Paylog,
  ProductDetail,
  ProductListParams,
  ProductVO,
  Region,
  ShippingTypeMap,
  SiteConfig,
  UnreadMessageCount,
  UploadedPicture,
  UserCoupon,
  UserMessage,
  UserSignInfo,
} from './types';

// ————————————————————— 首页 / 分类 —————————————————————

export const homeApi = {
  /** `GET /api/home/home/pcIndex`：PC 首页装修数据。 */
  pcIndex() {
    return request<DecorateIndex>({ url: '/home/home/pcIndex', method: 'get' });
  },
  /** `GET /api/category/category/all`：前台分类树。 */
  categoryTree() {
    return request<CategoryNode[]>({ url: '/category/category/all', method: 'get' });
  },
  /** `GET /api/common/config/base`：站点基础配置。 */
  siteConfig() {
    return request<SiteConfig>({ url: '/common/config/base', method: 'get' });
  },
};

// ————————————————————— 商品 —————————————————————

export const productApi = {
  /** `GET /api/product/product/list`：商品分页（前台仅上架商品）。 */
  list(params: ProductListParams) {
    return request<PageResult<ProductVO>>({
      url: '/product/product/list',
      method: 'get',
      params: { productStatus: 1, ...params },
    });
  },
  /** `GET /api/product/product/detail?id=`：商品详情。 */
  detail(id: number) {
    return request<ProductDetail>({
      url: '/product/product/detail',
      method: 'get',
      params: { id },
    });
  },
};

// ————————————————————— 购物车 —————————————————————

export const cartApi = {
  /** `GET /api/cart/cart/list`：购物车汇总（含行与勾选金额）。 */
  list() {
    return request<CartSummary>({ url: '/cart/cart/list', method: 'get' });
  },
  /** `GET /api/cart/cart/getCount`：购物车数量角标。 */
  count() {
    return request<number>({ url: '/cart/cart/getCount', method: 'get' });
  },
  /** `POST /api/cart/cart/addToCart`：加入购物车（返回 cartId）。 */
  add(payload: { productId: number; skuId?: number | null; quantity?: number }) {
    return request<number>({
      url: '/cart/cart/addToCart',
      method: 'post',
      data: { quantity: 1, ...payload },
    });
  },
  /** `POST /api/cart/cart/updateItem`：修改数量。 */
  updateItem(cartId: number, quantity: number) {
    return request<string>({
      url: '/cart/cart/updateItem',
      method: 'post',
      data: { cartId, quantity },
    });
  },
  /** `POST /api/cart/cart/updateCheck`：勾选/取消勾选。 */
  updateCheck(cartId: number, checked: boolean) {
    return request<string>({
      url: '/cart/cart/updateCheck',
      method: 'post',
      data: { cartId, checked },
    });
  },
  /** `POST /api/cart/cart/removeItem`：删除指定行。 */
  remove(cartIds: number[]) {
    return request<string>({
      url: '/cart/cart/removeItem',
      method: 'post',
      data: { cartIds },
    });
  },
  /** `POST /api/cart/cart/clear`：清空购物车。 */
  clear() {
    return request<string>({ url: '/cart/cart/clear', method: 'post' });
  },
};

// ————————————————————— 结算 / 订单 —————————————————————

export const orderApi = {
  /** `POST /api/order/check/index`：结算页数据装配（游客可预览，登录态优先）。 */
  checkIndex(param: OrderCheckParam) {
    return request<OrderCheckVO>({
      url: '/order/check/index',
      method: 'post',
      data: param,
    });
  },
  /** `POST /api/order/check/update`：地址/配送/券变化后重算。 */
  checkUpdate(param: OrderCheckParam) {
    return request<OrderCheckVO>({
      url: '/order/check/update',
      method: 'post',
      data: param,
    });
  },
  /** `POST /api/order/check/updateCoupon`：优惠券变化后重算。 */
  updateCoupon(param: OrderCheckParam) {
    return request<OrderCheckVO>({
      url: '/order/check/updateCoupon',
      method: 'post',
      data: param,
    });
  },
  /** `GET /api/order/check/getInvoice`：获得上次订单发票信息（含标题/税号等）。 */
  getInvoice(params: { invoiceType?: number; titleType?: number }) {
    return request<OrderInvoice | null>({
      url: '/order/check/getInvoice',
      method: 'get',
      params,
    });
  },
  /** `GET /api/order/check/getAvailablePaymentType`：可用支付方式。 */
  paymentTypes(flowType?: number) {
    return request<PaymentType[]>({
      url: '/order/check/getAvailablePaymentType',
      method: 'get',
      params: { flowType },
    });
  },
  /** `GET /api/order/check/getStoreShippingType`：店铺配送方式。 */
  storeShippingType(params: { shopId?: number; addressId?: number; payTypeId?: number; flowType?: number }) {
    return request<ShippingTypeMap>({
      url: '/order/check/getStoreShippingType',
      method: 'get',
      params,
    });
  },
  /** `POST /api/order/check/submit`：提交订单。 */
  submit(param: OrderCreateParam) {
    return request<OrderCreateVO>({
      url: '/order/check/submit',
      method: 'post',
      data: param,
    });
  },
  /** `GET /api/user/order/orderNum`：订单数量角标（待付款/待收货/评论/售后等）。 */
  orderNum() {
    return request<OrderNum>({ url: '/user/order/orderNum', method: 'get' });
  },
  /** `GET /api/user/order/list`：我的订单分页。 */
  list(params: { orderStatus?: number; page?: number; pageSize?: number }) {
    return request<PageResult<OrderVO>>({
      url: '/user/order/list',
      method: 'get',
      params,
    });
  },
  /** `GET /api/user/order/detail?id=`：订单详情（order + items）。 */
  detail(id: number) {
    return request<OrderDetail>({
      url: '/user/order/detail',
      method: 'get',
      params: { id },
    });
  },
  /** `POST /api/user/order/cancelOrder`：取消订单。 */
  cancel(orderId: number) {
    return request<string>({
      url: '/user/order/cancelOrder',
      method: 'post',
      data: { orderId },
    });
  },
  /** `POST /api/user/order/confirmReceipt`：确认收货。 */
  confirmReceipt(id: number) {
    return request<void>({
      url: '/user/order/confirmReceipt',
      method: 'post',
      data: { id },
    });
  },
  /** `POST /api/user/order/delOrder`：删除订单。 */
  remove(id: number) {
    return request<void>({
      url: '/user/order/delOrder',
      method: 'post',
      data: { id },
    });
  },
  /** `POST /api/user/order/buyAgain`：再次购买（加入购物车）。 */
  buyAgain(id: number) {
    return request<void>({
      url: '/user/order/buyAgain',
      method: 'post',
      data: { id },
    });
  },
};

// ————————————————————— 支付 —————————————————————

export const payApi = {
  /** `GET /api/order/pay/index?id=`：收银台数据。 */
  index(orderId: number) {
    return request<PayIndex>({
      url: '/order/pay/index',
      method: 'get',
      params: { id: orderId },
    });
  },
  /** `GET /api/order/pay/getPayLog?id=`：支付单信息。 */
  paylog(orderId: number) {
    return request<Paylog>({
      url: '/order/pay/getPayLog',
      method: 'get',
      params: { id: orderId },
    });
  },
  /** `GET /api/order/pay/checkStatus?id=`：轮询支付状态（返回订单支付状态码）。 */
  checkStatus(orderId: number) {
    return request<number>({
      url: '/order/pay/checkStatus',
      method: 'get',
      params: { id: orderId },
    });
  },
  /** `GET /api/order/pay/create?id=&type=`：创建支付单（渠道未迁移时后端返回明确错误）。 */
  create(orderId: number, type: string) {
    return request<PayCreate>({
      url: '/order/pay/create',
      method: 'get',
      params: { id: orderId, type },
    });
  },
};

// ————————————————————— 用户 / 地址 / 地区 —————————————————————

export const userApi = {
  /** `POST /api/user/login/signin`：会员登录。 */
  login(payload: { username: string; password: string; captchaVerification?: string }) {
    return request<LoginVO>({
      url: '/user/login/signin',
      method: 'post',
      data: payload,
    });
  },
  /** `GET /api/user/user/detail`：个人资料。 */
  profile() {
    return request<Record<string, any> | null>({
      url: '/user/user/detail',
      method: 'get',
    });
  },
  /** `GET /api/user/user/memberCenter`：用户中心概览（含订单角标）。 */
  memberCenter() {
    return request<MemberCenter>({ url: '/user/user/memberCenter', method: 'get' });
  },
  /** `POST /api/user/user/uploadImg`：上传凭证图片（multipart `file`）。 */
  uploadImage(file: File) {
    const form = new FormData();
    form.append('file', file);
    return request<UploadedPicture>({
      url: '/user/user/uploadImg',
      method: 'post',
      data: form,
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },
  /** `GET /api/user/user/creditDetail`：账期支付信息。 */
  creditDetail() {
    return request<Record<string, any> | null>({
      url: '/user/user/creditDetail',
      method: 'get',
    });
  },
};

export const messageApi = {
  /** `GET /api/user/message/list`：站内信分页。 */
  list(params: { page?: number; size?: number; unread?: number } = {}) {
    return request<PageResult<UserMessage>>({
      url: '/user/message/list',
      method: 'get',
      params,
    });
  },
  /** `GET /api/user/message/unreadCount`：未读消息统计。 */
  unreadCount() {
    return request<UnreadMessageCount>({ url: '/user/message/unreadCount', method: 'get' });
  },
  /** `POST /api/user/message/updateMessageRead`：单条已读。 */
  updateRead(id: number) {
    return request<boolean>({
      url: '/user/message/updateMessageRead',
      method: 'post',
      data: { id },
    });
  },
  /** `POST /api/user/message/updateAllRead`：全部已读。 */
  updateAllRead() {
    return request<void>({ url: '/user/message/updateAllRead', method: 'post' });
  },
  /** `POST /api/user/message/updateOrderMessageRead`：订单消息已读。 */
  updateOrderRead() {
    return request<void>({ url: '/user/message/updateOrderMessageRead', method: 'post' });
  },
  /** `POST /api/user/message/del`：删除消息。 */
  remove(id: number) {
    return request<void>({
      url: '/user/message/del',
      method: 'post',
      data: { id },
    });
  },
};

export const commentApi = {
  /** `GET /api/user/comment/subNum`：评价数量统计。 */
  subNum() {
    return request<CommentSubNum>({ url: '/user/comment/subNum', method: 'get' });
  },
  /** `GET /api/user/comment/list`：已评价列表。 */
  list(params: { page?: number; pageSize?: number } = {}) {
    return request<PageResult<CommentItem>>({
      url: '/user/comment/list',
      method: 'get',
      params,
    });
  },
  /** `GET /api/user/comment/showedList`：待晒单列表（返回订单分页）。 */
  showedList(params: { page?: number; size?: number } = {}) {
    return request<PageResult<any>>({
      url: '/user/comment/showedList',
      method: 'get',
      params,
    });
  },
};

export const signApi = {
  /** `GET /api/user/sign/index`：签到页信息。 */
  index() {
    return request<UserSignInfo>({ url: '/user/sign/index', method: 'get' });
  },
  /** `GET /api/user/sign/sign`：执行签到。 */
  sign() {
    return request<void>({ url: '/user/sign/sign', method: 'get' });
  },
};

export const addressApi = {
  /** `GET /api/user/address/list`：当前用户收货地址。 */
  list() {
    return request<PageResult<AddressVO>>({ url: '/user/address/list', method: 'get' });
  },
  /** `GET /api/user/address/tagList`：常用标签。 */
  tags() {
    return request<string[]>({ url: '/user/address/tagList', method: 'get' });
  },
  /** `POST /api/user/address/create`：新增地址（返回 addressId）。 */
  create(payload: AddressPayload) {
    return request<number>({
      url: '/user/address/create',
      method: 'post',
      data: payload,
    });
  },
  /** `POST /api/user/address/update`：修改地址。 */
  update(payload: AddressPayload) {
    return request<void>({
      url: '/user/address/update',
      method: 'post',
      data: payload,
    });
  },
  /** `POST /api/user/address/del`：删除地址。 */
  remove(id: number) {
    return request<void>({
      url: '/user/address/del',
      method: 'post',
      data: { id },
    });
  },
  /** `POST /api/user/address/setSelected`：设为选中地址。 */
  setSelected(id: number) {
    return request<void>({
      url: '/user/address/setSelected',
      method: 'post',
      data: { id },
    });
  },
};

export const regionApi = {
  /** `GET /api/sys/region/getProvinceList`：省级列表。 */
  provinces() {
    return request<Region[]>({ url: '/sys/region/getProvinceList', method: 'get' });
  },
  /** `GET /api/sys/region/getRegionListByRegionId?regionId=`：下级地区。 */
  children(regionId: number) {
    return request<Region[]>({
      url: '/sys/region/getRegionListByRegionId',
      method: 'get',
      params: { regionId },
    });
  },
};

// ————————————————————— 会员中心扩展 —————————————————————

export const couponApi = {
  /** `GET /api/user/coupon/list`：我领取的优惠券（分页）。 */
  myCoupons(params: { page?: number; pageSize?: number } = {}) {
    return request<PageResult<UserCoupon>>({
      url: '/user/coupon/list',
      method: 'get',
      params,
    });
  },
  /** `GET /api/user/coupon/getList`：领券中心（可领取优惠券）。 */
  claimable(params: { page?: number; pageSize?: number } = {}) {
    return request<PageResult<ClaimableCoupon>>({
      url: '/user/coupon/getList',
      method: 'get',
      params,
    });
  },
  /** `POST /api/user/coupon/claim`：领取优惠券（请求体 `{ couponId }`）。 */
  claim(couponId: number) {
    return request<number>({
      url: '/user/coupon/claim',
      method: 'post',
      data: { couponId },
    });
  },
};

export const favoriteApi = {
  /** `GET /api/user/collectProduct/list`：商品收藏分页。 */
  list(params: { page?: number; size?: number } = {}) {
    return request<PageResult<CollectProduct>>({
      url: '/user/collectProduct/list',
      method: 'get',
      params,
    });
  },
  /** `POST /api/user/collectProduct/cancel`：取消收藏（请求体 `{ productId }`）。 */
  cancel(productId: number) {
    return request<void>({
      url: '/user/collectProduct/cancel',
      method: 'post',
      data: { productId },
    });
  },
  /** `POST /api/user/collectProduct/save`：收藏商品（请求体 `{ productId }`）。 */
  save(productId: number) {
    return request<number>({
      url: '/user/collectProduct/save',
      method: 'post',
      data: { productId },
    });
  },
};

export const historyApi = {
  /** `GET /api/user/user/historyProduct`：最近浏览商品。 */
  list() {
    return request<HistoryProduct[]>({ url: '/user/user/historyProduct', method: 'get' });
  },
};

export const aftersaleApi = {
  /** `GET /api/user/aftersales/list`：售后记录（按订单分页）。 */
  list(params: { page?: number; size?: number } = {}) {
    return request<PageResult<AftersaleOrder>>({
      url: '/user/aftersales/list',
      method: 'get',
      params,
    });
  },
  /** `GET /api/user/aftersales/detail?id=`：售后详情。 */
  detail(id: number) {
    return request<AftersaleDetail>({
      url: '/user/aftersales/detail',
      method: 'get',
      params: { id },
    });
  },
  /** `GET /api/user/aftersales/applyData?orderId=`：售后申请页数据（可售后商品 + 订单）。 */
  applyData(orderId: number) {
    return request<AftersalesApplyData>({
      url: '/user/aftersales/applyData',
      method: 'get',
      params: { orderId },
    });
  },
  /** `GET /api/user/aftersales/config`：售后类型与原因配置。 */
  config() {
    return request<AftersalesConfig>({ url: '/user/aftersales/config', method: 'get' });
  },
  /** `POST /api/user/aftersales/create`：提交售后申请。 */
  create(param: AftersalesCreateParam) {
    return request<Record<string, any>>({
      url: '/user/aftersales/create',
      method: 'post',
      data: param,
    });
  },
  /** `GET /api/user/aftersales/getRecord`：申请记录分页（`AftersalesVO`）。 */
  getRecord(params: { page?: number; size?: number; status?: number } = {}) {
    return request<PageResult<any>>({
      url: '/user/aftersales/getRecord',
      method: 'get',
      params,
    });
  },
};
