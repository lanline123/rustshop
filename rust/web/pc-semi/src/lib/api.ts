import type { HomeDecorateResponse } from '@/decorate/types';

const SERVER_API_BASE = (process.env.PC_API_BASE || 'https://battery.recy.cc').replace(/\/$/, '');

interface RequestOptions {
  previewId?: number;
  revalidate?: number;
}

async function readData<T>(response: Response): Promise<T> {
  const text = await response.text();
  let json: any;
  try {
    json = text ? JSON.parse(text) : {};
  } catch {
    throw new Error(`接口返回不是 JSON（HTTP ${response.status}）`);
  }
  if (!response.ok || (json.code !== undefined && json.code !== 0)) {
    throw new Error(json.msg || json.message || `请求失败（HTTP ${response.status}）`);
  }
  return (json.data ?? json) as T;
}

/** PC 首页装修：服务端直连后端，客户端可经 Next rewrite 走 /api。 */
export async function getHomeDecorate(options: RequestOptions = {}): Promise<HomeDecorateResponse> {
  const params = new URLSearchParams();
  if (options.previewId) params.set('previewId', String(options.previewId));
  const query = params.toString() ? `?${params.toString()}` : '';
  const response = await fetch(`${SERVER_API_BASE}/api/home/home/pcIndex${query}`, {
    cache: options.previewId ? 'no-store' : 'force-cache',
    next: options.previewId ? undefined : { revalidate: options.revalidate ?? 60 },
  });
  return readData<HomeDecorateResponse>(response);
}

/** PC 公共头部：通常已经包含在 pageModule.pcHeader 中，此接口用于独立刷新。 */
export async function getPcHeader(previewId?: number): Promise<Record<string, unknown> | null> {
  const query = previewId ? `?previewId=${previewId}` : '';
  const response = await fetch(`${SERVER_API_BASE}/api/home/home/pcHeader${query}`, {
    cache: previewId ? 'no-store' : 'force-cache',
    next: previewId ? undefined : { revalidate: 60 },
  });
  const data = await readData<{ pcHeader?: Record<string, unknown> | null }>(response);
  return data?.pcHeader ?? null;
}

export interface ProductDetailResponse {
  item: Record<string, any>;
  skuList: Array<Record<string, any>>;
  picList: Array<Record<string, any>>;
  attrList?: {
    normal?: Array<Record<string, any>>;
    spe?: Array<Record<string, any>>;
    extra?: Array<Record<string, any>>;
  };
  descArr?: Array<{ type?: string; pic?: string; html?: string }>;
  serviceList?: Array<Record<string, any>>;
  deliveryModeText?: string;
  shopId?: number;
}

/** 商品详情：服务端 SSR 拉取，SEO 和首屏都走服务端。 */
export async function getProductDetail(id: number): Promise<ProductDetailResponse> {
  const response = await fetch(`${SERVER_API_BASE}/api/product/product/detail?id=${id}`, {
    cache: 'no-store',
  });
  return readData<ProductDetailResponse>(response);
}

/** 加入购物车：客户端调用，依赖登录 cookie。 */
export async function addToCart(payload: {
  id: number;
  number: number;
  skuId?: number;
  isQuick?: number;
}): Promise<Record<string, unknown>> {
  const response = await fetch('/api/cart/cart/addToCart', {
    method: 'POST',
    credentials: 'include',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });
  return readData<Record<string, unknown>>(response);
}

export interface ProductListItem {
  productId: number;
  productName?: string;
  productSn?: string;
  productPrice?: number | string;
  picThumb?: string;
  picUrl?: string;
  brief?: string;
  productStock?: number;
  sales?: number;
}

export interface ProductListResponse {
  records: ProductListItem[];
  total: number;
}

/** 商品列表：搜索页 / 分类页 SSR 共用。 */
export async function getProducts(params: {
  page?: number;
  size?: number;
  keyword?: string;
  categoryId?: number | string;
  sortField?: string;
  sortOrder?: 'asc' | 'desc';
}): Promise<ProductListResponse> {
  const query = new URLSearchParams();
  query.set('page', String(params.page || 1));
  query.set('size', String(params.size || 24));
  if (params.keyword) query.set('keyword', params.keyword);
  if (params.categoryId) query.set('categoryId', String(params.categoryId));
  if (params.sortField) query.set('sortField', params.sortField);
  if (params.sortOrder) query.set('sortOrder', params.sortOrder);
  const response = await fetch(`${SERVER_API_BASE}/api/product/product/list?${query.toString()}`, {
    cache: 'no-store',
  });
  const data = await readData<{ records?: ProductListItem[]; total?: number }>(response);
  return { records: data.records || [], total: Number(data.total || 0) };
}
