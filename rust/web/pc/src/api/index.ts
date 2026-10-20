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
  CartSummary,
  CategoryNode,
  DecorateIndex,
  LoginVO,
  MemberCenter,
  OrderCheckParam,
  OrderCheckVO,
  OrderCreateParam,
  OrderCreateVO,
  OrderDetail,
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
  SiteConfig,
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
    return request<Record<string, any[]>>({
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
