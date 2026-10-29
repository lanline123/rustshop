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
